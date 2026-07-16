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
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
)

class KidsLauncherViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
) : AndroidViewModel(application) {

    private val installedApps = MutableStateFlow<List<LauncherApp>>(emptyList())
    private val loading = MutableStateFlow(true)

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

    fun launch(app: LauncherApp, onError: (Throwable) -> Unit = {}) =
        appRepository.launch(app, onError)

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
