package io.github.meko123456.khma.data.rss

/**
 * A podcasts.apple.com show link, and the RSS feed Apple's public lookup API gives for it.
 *
 * Shows are mostly shared as Apple Podcasts links, and that page isn't a feed: added as it was, it
 * failed with "That address isn't a podcast feed".
 */
object ApplePodcasts {
    private val show = Regex("""^https?://(?:www\.)?podcasts\.apple\.com/\S*?/id(\d+)""", RegexOption.IGNORE_CASE)
    private val feed = Regex(""""feedUrl"\s*:\s*"((?:[^"\\]|\\.)+)"""")
    private val noResults = Regex(""""resultCount"\s*:\s*0\b""")

    /** The show's id in [address], or null when it isn't an Apple Podcasts show link. */
    fun showId(address: String): String? = show.find(address.trim())?.groupValues?.get(1)

    fun lookupUrl(showId: String): String = "https://itunes.apple.com/lookup?id=$showId&entity=podcast"

    /** The first result's feed in a lookup reply, or null when Apple has none for the show. */
    fun feedUrl(lookupReply: String): String? = feed.find(lookupReply)?.groupValues?.get(1)?.replace("\\/", "/")

    /** Whether the lookup found the show at all: a link cut short or a removed show finds nothing. */
    fun foundShow(lookupReply: String): Boolean = !noResults.containsMatchIn(lookupReply)
}

/** Apple knows the show but gives no public feed for it: one only Apple's own app can play. */
class NoPublicFeedException : java.io.IOException("No public feed for that show")

/** Apple has no show with the link's id. */
class NoSuchShowException : java.io.IOException("No Apple Podcasts show with that id")
