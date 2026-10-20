package io.github.meko123456.khma.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FeedAddressTest {

    @Test
    fun `an address typed without a scheme is fetched over https`() {
        assertEquals("https://feeds.npr.org/510289/podcast.xml", FeedAddress.normalise("feeds.npr.org/510289/podcast.xml"))
        assertEquals("https://feeds.npr.org/510289/podcast.xml", FeedAddress.normalise("  feeds.npr.org/510289/podcast.xml \n"))
    }

    @Test
    fun `a podcast app's subscribe link becomes the same feed over https`() {
        assertEquals("https://example.com/rss", FeedAddress.normalise("feed://example.com/rss"))
        assertEquals("https://example.com/rss", FeedAddress.normalise("ITPC://example.com/rss"))
        assertEquals("https://example.com/rss", FeedAddress.normalise("pcast://example.com/rss"))
        assertEquals("https://example.com/rss", FeedAddress.normalise("podcast://example.com/rss"))
    }

    @Test
    fun `an address with a web scheme is left as it is`() {
        assertEquals("https://example.com/rss", FeedAddress.normalise("https://example.com/rss"))
        assertEquals("http://example.com/rss", FeedAddress.normalise("http://example.com/rss"))
    }

    @Test
    fun `a share gives its first link`() {
        val daily = "https://podcasts.apple.com/us/podcast/the-daily/id1200361736"
        assertEquals(daily, FeedAddress.fromShared(daily))
        assertEquals(daily, FeedAddress.fromShared("Listen to this one:\n$daily\n\nand https://example.com/other"))
        assertEquals("feed://example.com/rss", FeedAddress.fromShared("Subscribe with feed://example.com/rss today"))
        assertEquals("www.example.com/feed.xml", FeedAddress.fromShared("It's at www.example.com/feed.xml"))
    }

    @Test
    fun `a share's link loses the punctuation of the sentence around it`() {
        assertEquals("https://example.com/rss", FeedAddress.fromShared("Try https://example.com/rss."))
        assertEquals("https://example.com/rss", FeedAddress.fromShared("The Daily (https://example.com/rss)"))
        assertEquals("https://example.com/rss", FeedAddress.fromShared("«https://example.com/rss», he said"))
        assertEquals("https://example.com/rss", FeedAddress.fromShared("Here: <https://example.com/rss>!"))
        // A bracket the link opened is its own.
        assertEquals("https://example.com/show_(2024)", FeedAddress.fromShared("https://example.com/show_(2024)."))
    }

    @Test
    fun `a share without a link gives its first line`() {
        assertEquals("feeds.npr.org/510289/podcast.xml", FeedAddress.fromShared("\n  feeds.npr.org/510289/podcast.xml \nPlanet Money"))
        assertEquals("", FeedAddress.fromShared(" \n "))
    }
}
