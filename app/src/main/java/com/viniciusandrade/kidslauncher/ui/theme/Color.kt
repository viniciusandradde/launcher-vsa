package com.viniciusandrade.kidslauncher.ui.theme

import androidx.compose.ui.graphics.Color

// Playful, high-contrast palette. Each profile gets its own accent so a child
// (and the parent) can tell at a glance which mode the device is in.

// Pequeno (4 anos) — warm, friendly orange/coral.
val PequenoPrimary = Color(0xFFFF7043)
val PequenoSecondary = Color(0xFFFFCA28)
val PequenoBackground = Color(0xFFFFF8F2)

// Junior (10 anos) — cooler, "big kid" blue/teal.
val JuniorPrimary = Color(0xFF2979FF)
val JuniorSecondary = Color(0xFF00BFA5)
val JuniorBackground = Color(0xFFF3F7FF)

// Shared neutrals.
val OnPrimary = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1B1B1F)

// --- "Pequeno" playful mode (GCompris-like / YouTube Kids cards) ---------------

/**
 * Bright, saturated tile colours. Each app gets one (stable by package hash) so
 * the grid looks like a colourful GCompris / YouTube Kids board. Every colour is
 * dark enough to carry white text/icon on top with good contrast.
 */
val KidCardColors = listOf(
    Color(0xFFEF5350), // red
    Color(0xFF42A5F5), // blue
    Color(0xFF66BB6A), // green
    Color(0xFFAB47BC), // purple
    Color(0xFFFFA726), // orange
    Color(0xFF26C6DA), // cyan
    Color(0xFFEC407A), // pink
    Color(0xFF7E57C2), // deep purple
    Color(0xFF9CCC65), // light green
    Color(0xFF5C6BC0), // indigo
)

// --- GCompris-style outdoor scene (sky, sun, clouds, hills, grass) -------------
val SkyTop = Color(0xFF4FC3F7)      // deeper blue at the top
val SkyHorizon = Color(0xFFB3E5FC)  // pale blue near the hills
val SunColor = Color(0xFFFFD54F)
val CloudColor = Color(0xFFFFFFFF)
val HillBack = Color(0xFF9CCC65)    // far, lighter hill
val HillFront = Color(0xFF7CB342)   // near, deeper hill
val GrassColor = Color(0xFF689F38)  // grass band at the very bottom
val NightSkyTop = Color(0xFF1A237E) // dusk for the "time's up" screen
val NightSkyBottom = Color(0xFF5C6BC0)
