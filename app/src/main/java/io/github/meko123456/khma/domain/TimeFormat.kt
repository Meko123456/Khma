package io.github.meko123456.khma.domain

/**
 * Time formatting shared by the episode list and the now-playing screen.
 *
 * Framework-free so it can be unit-tested — these used to be private helpers duplicated
 * inside two Compose files, where they couldn't be covered.
 */
object TimeFormat {

    /** Episode length for a list row: "1h 46m", "8 min", or null when unknown. */
    fun duration(seconds: Int): String? {
        if (seconds <= 0) return null
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "${h}h ${m}m" else "$m min"
    }

    /** Playback clock: "2:06:46" past an hour, otherwise "12:34". Negatives clamp to zero. */
    fun clock(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }
}
