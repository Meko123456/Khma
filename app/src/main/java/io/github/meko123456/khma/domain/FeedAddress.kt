package io.github.meko123456.khma.domain

/** What a typed or pasted feed address becomes before it is fetched. */
object FeedAddress {

    /** The schemes podcast apps put on "subscribe" links. Each is the same feed over https. */
    private val podcastSchemes = listOf("feed://", "itpc://", "pcast://", "podcast://")

    /**
     * Trims [input], gives an address typed without a scheme "https://", and turns a podcast app's
     * scheme into https. "feeds.npr.org/510289/podcast.xml" used to fail with the HTTP client's
     * "Expected URL scheme 'http' or 'https' but no scheme was found".
     */
    fun normalise(input: String): String {
        val trimmed = input.trim()
        val scheme = podcastSchemes.firstOrNull { trimmed.startsWith(it, ignoreCase = true) }
        return when {
            scheme != null -> "https://" + trimmed.substring(scheme.length)
            "://" in trimmed -> trimmed
            else -> "https://$trimmed"
        }
    }

    /**
     * The address in text another app shared: its first link, without the full stop or bracket a
     * sentence put after it, or else the text's first line, for the add dialog to show as it came.
     */
    fun fromShared(text: String): String {
        val found = sharedLink.find(text)?.value
            ?: return text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
        var end = found.length
        while (end > 0) {
            val last = found[end - 1]
            val unopened = last == ')' && found.take(end).count { it == ')' } > found.take(end).count { it == '(' }
            if (last in ".,;:!?" || unopened) end-- else break
        }
        return found.take(end)
    }

    private val sharedLink =
        Regex("""(?:(?:https?|feed|itpc|pcast|podcast)://|www\.)[^\s<>"'“”‘’«»]+""", RegexOption.IGNORE_CASE)
}
