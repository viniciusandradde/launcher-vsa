package com.viniciusandrade.kidslauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.viniciusandrade.kidslauncher.data.model.KidProfile

private fun schemeFor(profile: KidProfile) = when (profile) {
    KidProfile.PEQUENO -> lightColorScheme(
        primary = PequenoPrimary,
        secondary = PequenoSecondary,
        background = PequenoBackground,
        surface = SurfaceLight,
        onPrimary = OnPrimary,
        onSurface = OnSurfaceLight,
    )

    KidProfile.JUNIOR -> lightColorScheme(
        primary = JuniorPrimary,
        secondary = JuniorSecondary,
        background = JuniorBackground,
        surface = SurfaceLight,
        onPrimary = OnPrimary,
        onSurface = OnSurfaceLight,
    )
}

/**
 * Theme keyed on the active [KidProfile] so switching profiles re-colours the
 * whole launcher. We deliberately stick to a bright light scheme — kids' devices
 * are typically used in daylight and high contrast aids readability — but the
 * hook for [isSystemInDarkTheme] is left in place for a future dark variant.
 */
@Composable
fun KidsLauncherTheme(
    profile: KidProfile,
    content: @Composable () -> Unit,
) {
    @Suppress("UNUSED_VARIABLE")
    val dark = isSystemInDarkTheme() // reserved for a future dark palette
    MaterialTheme(
        colorScheme = schemeFor(profile),
        typography = KidsTypography,
        content = content,
    )
}
