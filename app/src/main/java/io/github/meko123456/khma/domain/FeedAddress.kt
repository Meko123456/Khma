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
}
