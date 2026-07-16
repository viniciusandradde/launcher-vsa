package com.viniciusandrade.kidslauncher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.viniciusandrade.kidslauncher.data.model.KidProfile
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
) {
    fun allowedPackages(profile: KidProfile): Set<String> =
        whitelist[profile.id].orEmpty()

    companion object {
        /** PIN used until a parent sets their own during first-run setup. */
        const val DEFAULT_PIN = "1234"
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
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val whitelist = KidProfile.entries.associate { profile ->
            profile.id to prefs[Keys.whitelist(profile.id)].orEmpty()
        }
        AppSettings(
            activeProfile = KidProfile.fromId(prefs[Keys.ACTIVE_PROFILE]),
            pinHash = prefs[Keys.PIN_HASH],
            whitelist = whitelist,
            setupComplete = prefs[Keys.SETUP_COMPLETE] ?: false,
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

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
