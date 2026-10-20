package io.github.meko123456.khma.data

import io.github.meko123456.khma.data.rss.ApplePodcasts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplePodcastsTest {

    @Test
    fun `a show link gives its id however it is written`() {
        assertEquals("1200361736", ApplePodcasts.showId("https://podcasts.apple.com/us/podcast/the-daily/id1200361736"))
        assertEquals("290783428", ApplePodcasts.showId("  https://podcasts.apple.com/ge/podcast/planet-money/id290783428?uo=4 "))
        // An episode link carries the show's id in its path and the episode's in ?i=.
        assertEquals("1200361736", ApplePodcasts.showId("https://podcasts.apple.com/us/podcast/x/id1200361736?i=1000671234567"))
        assertEquals("1200361736", ApplePodcasts.showId("http://podcasts.apple.com/podcast/id1200361736"))
    }

    @Test
    fun `anything else is not a show link`() {
        assertNull(ApplePodcasts.showId("https://feeds.npr.org/510289/podcast.xml"))
        assertNull(ApplePodcasts.showId("https://example.com/podcasts.apple.com/us/podcast/x/id1"))
        assertNull(ApplePodcasts.showId("https://podcasts.apple.com/us/browse"))
    }

    @Test
    fun `the lookup reply gives the feed`() {
        val reply = """{"resultCount":1,"results":[{"kind":"podcast","collectionName":"The Daily","feedUrl":"https://feeds.simplecast.com/Sl5CSM3S","artworkUrl30":"x"}]}"""
        assertEquals("https://feeds.simplecast.com/Sl5CSM3S", ApplePodcasts.feedUrl(reply))
        assertEquals("https://a.example/feed.xml", ApplePodcasts.feedUrl("""{"results":[{"feedUrl":"https:\/\/a.example\/feed.xml"}]}"""))
    }

    @Test
    fun `a reply without a feed gives none, and says whether the show was there`() {
        val noShow = "{\n \"resultCount\":0,\n \"results\": []\n}"
        val appleOnly = """{"resultCount":1,"results":[{"kind":"podcast","collectionName":"Apple-only show"}]}"""

        assertNull(ApplePodcasts.feedUrl(noShow))
        assertFalse(ApplePodcasts.foundShow(noShow))
        assertNull(ApplePodcasts.feedUrl(appleOnly))
        assertTrue(ApplePodcasts.foundShow(appleOnly))
        assertTrue(ApplePodcasts.foundShow("""{"resultCount":10,"results":[]}"""))
    }
}
