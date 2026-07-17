package com.viniciusandrade.kidslauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viniciusandrade.kidslauncher.KidsLauncherApp
import com.viniciusandrade.kidslauncher.data.AppRepository
import com.viniciusandrade.kidslauncher.data.AppSettings
import com.viniciusandrade.kidslauncher.data.SettingsRepository
import com.viniciusandrade.kidslauncher.data.model.KidProfile
import com.viniciusandrade.kidslauncher.data.model.KidVideo
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import com.viniciusandrade.kidslauncher.util.YouTube
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LauncherUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(
        activeProfile = KidProfile.DEFAULT,
        pinHash = null,
        whitelist = emptyMap(),
        setupComplete = false,
    ),
    /** Whitelisted apps for the currently active profile — what the child sees. */
    val visibleApps: List<LauncherApp> = emptyList(),
    /** Every installed app — used by the parent settings screen. */
    val allApps: List<LauncherApp> = emptyList(),
    /** Curated YouTube videos for the active profile. */
    val videos: List<KidVideo> = emptyList(),
)

class KidsLauncherViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
) : AndroidViewModel(application) {

    private val installedApps = MutableStateFlow<List<LauncherApp>>(emptyList())
    private val loading = MutableStateFlow(true)

    /** Wall-clock (ms) when the child last opened an app and left the launcher. */
    private var lastAppLaunchAt: Long? = null

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val uiState: StateFlow<LauncherUiState> =
        combine(
            settingsRepository.settings,
            installedApps,
            loading,
        ) { settings, apps, isLoading ->
            val allowed = settings.allowedPackages(settings.activeProfile)
            LauncherUiState(
                loading = isLoading,
                settings = settings,
                visibleApps = apps.filter { it.packageName in allowed },
                allApps = apps,
                videos = settings.videosFor(settings.activeProfile),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LauncherUiState(),
        )

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            loading.value = true
            installedApps.value = appRepository.loadInstalledApps()
            loading.value = false
        }
    }

    fun launch(app: LauncherApp, onError: (Throwable) -> Unit = {}) {
        // Remember when we handed control to another app so we can count that
        // time towards the daily budget once the child comes back.
        lastAppLaunchAt = System.currentTimeMillis()
        appRepository.launch(app, onError)
    }

    /**
     * Called from the Activity's onResume. Rolls the usage counter to today and,
     * if the child is returning from a launched app, adds the time they spent
     * there (capped so a device left asleep overnight doesn't blow the budget).
     */
    fun onLauncherResumed() {
        val launchedAt = lastAppLaunchAt ?: return
        lastAppLaunchAt = null
        val elapsedSeconds = ((System.currentTimeMillis() - launchedAt) / 1000).toInt()
        val capped = elapsedSeconds.coerceIn(0, MAX_RETURN_GAP_SECONDS)
        viewModelScope.launch { settingsRepository.addUsageSeconds(capped, today()) }
    }

    /** Advance the usage counter while the launcher itself is on screen. */
    fun tickUsage(seconds: Int) {
        viewModelScope.launch { settingsRepository.addUsageSeconds(seconds, today()) }
    }

    fun setTimeLimit(profile: KidProfile, minutes: Int) {
        viewModelScope.launch { settingsRepository.setTimeLimit(profile, minutes) }
    }

    fun setChildName(profile: KidProfile, name: String) {
        viewModelScope.launch { settingsRepository.setChildName(profile, name) }
    }

    /**
     * Parse [url] into a YouTube video and add it to [profile]. Returns false if
     * the link isn't a recognisable YouTube URL/id (so the UI can warn the parent).
     */
    fun addVideo(profile: KidProfile, url: String, name: String): Boolean {
        val id = YouTube.extractVideoId(url) ?: return false
        viewModelScope.launch {
            settingsRepository.addVideo(profile, KidVideo(id = id, name = name.trim().take(30)))
        }
        return true
    }

    fun removeVideo(profile: KidProfile, videoId: String) {
        viewModelScope.launch { settingsRepository.removeVideo(profile, videoId) }
    }

    /** Open a curated video; counts towards the daily budget like launching an app. */
    fun openVideo(video: KidVideo, onError: (Throwable) -> Unit = {}) {
        lastAppLaunchAt = System.currentTimeMillis()
        appRepository.openUrl(video.watchUrl, onError)
    }

    /** Parent override from the "time's up" screen: clears today's usage. */
    fun grantMoreTime() {
        viewModelScope.launch { settingsRepository.resetUsage(today()) }
    }

    fun switchProfile(profile: KidProfile) {
        viewModelScope.launch { settingsRepository.setActiveProfile(profile) }
    }

    fun setAppAllowed(profile: KidProfile, packageName: String, allowed: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAppAllowed(profile, packageName, allowed)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch { settingsRepository.setPin(pin) }
    }

    /** Synchronous PIN check against the latest emitted settings snapshot. */
    fun verifyPin(pin: String): Boolean =
        settingsRepository.verifyPin(uiState.value.settings, pin)

    companion object {
        /**
         * Upper bound on the time credited for a single "left then returned" gap.
         * Stops an idle/asleep device (child walked away) from draining the whole
         * daily budget while still counting normal app sessions.
         */
        private const val MAX_RETURN_GAP_SECONDS = 20 * 60

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as KidsLauncherApp
                KidsLauncherViewModel(
                    application = app,
                    appRepository = app.appRepository,
                    settingsRepository = app.settingsRepository,
                )
            }
        }
    }
}
