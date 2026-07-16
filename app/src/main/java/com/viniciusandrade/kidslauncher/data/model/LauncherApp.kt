package com.viniciusandrade.kidslauncher.data.model

import android.graphics.drawable.Drawable

/**
 * A launchable application resolved from the system, ready to be shown in the grid.
 *
 * [icon] is kept as a live [Drawable] (not a bitmap) so the launcher stays cheap:
 * icons are loaded once when the app list is queried and reused across recompositions.
 */
data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
) {
    /** Stable key used for lists and whitelist membership. */
    val key: String get() = packageName
}
