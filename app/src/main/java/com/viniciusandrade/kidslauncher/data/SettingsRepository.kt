package com.viniciusandrade.kidslauncher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.viniciusandrade.kidslauncher.data.model.KidProfile
import com.viniciusandrade.kidslauncher.data.model.KidVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore by preferencesDataStore(name = "kids_launcher_settings")

/** Immutable snapshot of everything the launcher persists. */
data class AppSettings(
    val activeProfile: KidProfile,
    val pinHash: String?,
    /** Allowed package names per profile id. */
    val whitelist: Map<String, Set<String>>,
    val setupComplete: Boolean,
    /** Daily screen-time limit in minutes per profile id (0 = no limit). */
    val timeLimits: Map<String, Int> = emptyMap(),
    /** Calendar day (yyyy-MM-dd) the [usedSeconds] counter belongs to. */
    val usageDate: String = "",
    /** Seconds of screen time already used today. */
    val usedSeconds: Int = 0,
    /** Custom child name per profile id (blank => fall back to the profile name). */
    val childNames: Map<String, String> = emptyMap(),
    /** Parent-curated YouTube videos per profile id. */
    val videos: Map<String, List<KidVideo>> = emptyMap(),
) {
    fun allowedPackages(profile: KidProfile): Set<String> =
        whitelist[profile.id].orEmpty()

    fun videosFor(profile: KidProfile): List<KidVideo> = videos[profile.id].orEmpty()

    /** Raw stored name for the text field (may be blank). */
    fun rawChildName(profile: KidProfile): String = childNames[profile.id].orEmpty()

    /** Name to show the child; falls back to the profile's default display name. */
    fun childName(profile: KidProfile): String =
        rawChildName(profile).ifBlank { profile.displayName }

    /** Daily limit in minutes for [profile]; 0 means unlimited. */
    fun timeLimitMinutes(profile: KidProfile): Int = timeLimits[profile.id] ?: 0

    /** Whether today's screen time for [profile] is used up (limit reached). */
    fun isTimeUp(profile: KidProfile): Boolean {
        val limit = timeLimitMinutes(profile)
        return limit > 0 && usedSeconds >= limit * 60
    }

    /** Seconds still available today for [profile]; null when there is no limit. */
    fun remainingSeconds(profile: KidProfile): Int? {
        val limit = timeLimitMinutes(profile)
        if (limit <= 0) return null
        return (limit * 60 - usedSeconds).coerceAtLeast(0)
    }

    companion object {
        /** PIN used until a parent sets their own during first-run setup. */
        const val DEFAULT_PIN = "1234"

        /** Preset options (in minutes) offered to parents; 0 = no limit. */
        val TIME_LIMIT_OPTIONS = listOf(0, 15, 30, 45, 60, 90)
    }
}

/**
 * Persists parental settings (PIN, active profile, per-profile app whitelist)
 * with Jetpack DataStore. The PIN is stored only as a SHA-256 hash — the plain
 * value never touches disk.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        fun whitelist(profileId: String) = stringSetPreferencesKey("whitelist_$profileId")
        fun timeLimit(profileId: String) = intPreferencesKey("time_limit_$profileId")
        val USAGE_DATE = stringPreferencesKey("usage_date")
        val USAGE_USED_SECONDS = intPreferencesKey("usage_used_seconds")
        fun childName(profileId: String) = stringPreferencesKey("child_name_$profileId")
        fun videos(profileId: String) = stringSetPreferencesKey("videos_$profileId")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val whitelist = KidProfile.entries.associate { profile ->
            profile.id to prefs[Keys.whitelist(profile.id)].orEmpty()
        }
        val timeLimits = KidProfile.entries.associate { profile ->
            profile.id to (prefs[Keys.timeLimit(profile.id)] ?: 0)
        }
        val childNames = KidProfile.entries.associate { profile ->
            profile.id to (prefs[Keys.childName(profile.id)] ?: "")
        }
        val videos = KidProfile.entries.associate { profile ->
            profile.id to prefs[Keys.videos(profile.id)].orEmpty()
                .map { KidVideo.fromStorage(it) }
                .sortedBy { it.displayName.lowercase() }
        }
        AppSettings(
            activeProfile = KidProfile.fromId(prefs[Keys.ACTIVE_PROFILE]),
            pinHash = prefs[Keys.PIN_HASH],
            whitelist = whitelist,
            setupComplete = prefs[Keys.SETUP_COMPLETE] ?: false,
            timeLimits = timeLimits,
            usageDate = prefs[Keys.USAGE_DATE] ?: "",
            usedSeconds = prefs[Keys.USAGE_USED_SECONDS] ?: 0,
            childNames = childNames,
            videos = videos,
        )
    }

    suspend fun setActiveProfile(profile: KidProfile) {
        context.dataStore.edit { it[Keys.ACTIVE_PROFILE] = profile.id }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit {
            it[Keys.PIN_HASH] = hash(pin)
            it[Keys.SETUP_COMPLETE] = true
        }
    }

    /** True when [pin] matches the stored hash, or the default PIN if none set yet. */
    fun verifyPin(settings: AppSettings, pin: String): Boolean {
        val expected = settings.pinHash ?: hash(AppSettings.DEFAULT_PIN)
        return hash(pin) == expected
    }

    suspend fun setAppAllowed(profile: KidProfile, packageName: String, allowed: Boolean) {
        val key = Keys.whitelist(profile.id)
        context.dataStore.edit { prefs ->
            val current = prefs[key].orEmpty().toMutableSet()
            if (allowed) current.add(packageName) else current.remove(packageName)
            prefs[key] = current
        }
    }

    /** Set the daily screen-time limit (minutes; 0 = unlimited) for [profile]. */
    suspend fun setTimeLimit(profile: KidProfile, minutes: Int) {
        context.dataStore.edit { it[Keys.timeLimit(profile.id)] = minutes.coerceAtLeast(0) }
    }

    /** Set the child's custom display name for [profile] (trimmed, max 20 chars). */
    suspend fun setChildName(profile: KidProfile, name: String) {
        context.dataStore.edit { it[Keys.childName(profile.id)] = name.trim().take(20) }
    }

    /** Add a curated YouTube [video] to [profile] (replaces any entry with same id). */
    suspend fun addVideo(profile: KidProfile, video: KidVideo) {
        val key = Keys.videos(profile.id)
        context.dataStore.edit { prefs ->
            val current = prefs[key].orEmpty()
                .filterNot { KidVideo.fromStorage(it).id == video.id }
                .toMutableSet()
            current.add(video.storageKey)
            prefs[key] = current
        }
    }

    /** Remove the video with [videoId] from [profile]. */
    suspend fun removeVideo(profile: KidProfile, videoId: String) {
        val key = Keys.videos(profile.id)
        context.dataStore.edit { prefs ->
            prefs[key] = prefs[key].orEmpty()
                .filterNot { KidVideo.fromStorage(it).id == videoId }
                .toSet()
        }
    }

    /**
     * Add [delta] seconds to today's usage counter, rolling the counter over to a
     * fresh 0 when [today] differs from the stored day. Called both by the
     * per-tick ticker and when the child returns from a launched app.
     */
    suspend fun addUsageSeconds(delta: Int, today: String) {
        if (delta <= 0) return
        context.dataStore.edit { prefs ->
            val sameDay = prefs[Keys.USAGE_DATE] == today
            val base = if (sameDay) (prefs[Keys.USAGE_USED_SECONDS] ?: 0) else 0
            prefs[Keys.USAGE_DATE] = today
            prefs[Keys.USAGE_USED_SECONDS] = base + delta
        }
    }

    /** Reset today's usage to zero (parent "grant more time" / new day). */
    suspend fun resetUsage(today: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USAGE_DATE] = today
            prefs[Keys.USAGE_USED_SECONDS] = 0
        }
    }

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
