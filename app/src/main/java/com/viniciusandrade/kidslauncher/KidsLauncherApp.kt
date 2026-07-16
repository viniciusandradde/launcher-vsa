package com.viniciusandrade.kidslauncher

import android.app.Application
import com.viniciusandrade.kidslauncher.data.AppRepository
import com.viniciusandrade.kidslauncher.data.SettingsRepository

/**
 * Application entry point. Owns the two repositories as simple singletons — a
 * hand-rolled service locator keeps this hobby project free of a DI framework
 * while staying trivial to migrate to Hilt later (see README "Evolução").
 */
class KidsLauncherApp : Application() {

    val appRepository: AppRepository by lazy { AppRepository(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
