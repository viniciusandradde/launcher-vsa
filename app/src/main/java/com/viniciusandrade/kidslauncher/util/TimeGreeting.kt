package com.viniciusandrade.kidslauncher.util

/**
 * Pure, side-effect-free greeting logic shared by the launcher header and the
 * Glance widget. Kept dependency-free so it is trivially unit-testable on the JVM.
 */
object TimeGreeting {

    /** Returns a Portuguese greeting for a 24-hour clock value (0..23). */
    fun forHour(hour: Int): String = when (hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
}
