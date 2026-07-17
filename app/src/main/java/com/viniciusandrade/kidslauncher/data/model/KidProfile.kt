package com.viniciusandrade.kidslauncher.data.model

/**
 * The two curated experiences shipped in a single APK.
 *
 * Each profile tunes the grid density and label behaviour to the child's age.
 * The 4-year-old ("Pequeno") gets fewer, larger icons with always-on labels;
 * the 10-year-old ("Junior") gets a denser grid closer to a normal launcher.
 */
enum class KidProfile(
    val id: String,
    val displayName: String,
    val ageLabel: String,
    /** Number of columns in the app grid. Fewer columns => bigger touch targets. */
    val gridColumns: Int,
    /** Whether to always render the app name below each icon. */
    val showLabels: Boolean,
    /**
     * When true, use the colourful "GCompris / YouTube Kids" card layout instead
     * of the plain icon grid. Reserved for the youngest child.
     */
    val playful: Boolean,
) {
    PEQUENO(
        id = "pequeno",
        displayName = "Pequeno",
        ageLabel = "4 anos",
        gridColumns = 2,
        showLabels = true,
        playful = true,
    ),
    JUNIOR(
        id = "junior",
        displayName = "Junior",
        ageLabel = "10 anos",
        gridColumns = 3,
        showLabels = true,
        playful = false,
    );

    companion object {
        val DEFAULT = PEQUENO

        fun fromId(id: String?): KidProfile =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
