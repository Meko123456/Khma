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
}
