package com.viniciusandrade.kidslauncher.util

/** Helpers for turning a pasted YouTube URL into a video id. */
object YouTube {

    private val ID = "[A-Za-z0-9_-]{11}"
    private val patterns = listOf(
        Regex("""youtu\.be/($ID)"""),
        Regex("""[?&]v=($ID)"""),
        Regex("""youtube\.com/shorts/($ID)"""),
        Regex("""youtube\.com/embed/($ID)"""),
        Regex("""youtube\.com/live/($ID)"""),
    )

    /**
     * Extract the 11-char video id from the common YouTube URL shapes (watch,
     * youtu.be, shorts, embed, live) or from a bare id. Returns null if none match.
     */
    fun extractVideoId(input: String): String? {
        val s = input.trim()
        if (s.matches(Regex(ID))) return s
        for (p in patterns) {
            val m = p.find(s)
            if (m != null) return m.groupValues[1]
        }
        return null
    }
}
