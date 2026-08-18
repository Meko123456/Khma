package io.github.meko123456.khma.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RssParserTest {

    private val feed = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
          <channel>
            <title>The Sample Cast</title>
            <description>A test podcast.</description>
            <itunes:author>Merab</itunes:author>
            <itunes:image href="https://example.com/cover.jpg"/>
            <item>
              <title>Episode One</title>
              <guid>ep-1</guid>
              <description>First one.</description>
              <pubDate>Mon, 17 Aug 2026 09:00:00 +0000</pubDate>
              <itunes:duration>1:02:03</itunes:duration>
              <enclosure url="https://example.com/1.mp3" type="audio/mpeg" length="123"/>
            </item>
            <item>
              <title>Episode Two</title>
              <guid>ep-2</guid>
              <itunes:duration>754</itunes:duration>
              <enclosure url="https://example.com/2.mp3" type="audio/mpeg"/>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    @Test
    fun `parses channel-level fields`() {
        val p = RssParser.parse(feed, "https://example.com/feed.xml")!!
        assertEquals("The Sample Cast", p.title)
        assertEquals("Merab", p.author)
        assertEquals("A test podcast.", p.description)
        assertEquals("https://example.com/cover.jpg", p.imageUrl)
        assertEquals("https://example.com/feed.xml", p.feedUrl)
        assertEquals(2, p.episodes.size)
    }

    @Test
    fun `parses episode fields including audio, duration and date`() {
        val e = RssParser.parse(feed, "f")!!.episodes.first()
        assertEquals("Episode One", e.title)
        assertEquals("ep-1", e.guid)
        assertEquals("https://example.com/1.mp3", e.audioUrl)
        assertEquals(1 * 3600 + 2 * 60 + 3, e.durationSeconds)
        assertTrue(e.pubDateMillis > 0)
    }

    @Test
    fun `channel title is not confused with item titles`() {
        // getElementsByTagName would return item titles too; direct-child traversal must not.
        assertEquals("The Sample Cast", RssParser.parse(feed, "f")!!.title)
    }

    @Test
    fun `duration parses seconds, mm ss and hh mm ss`() {
        assertEquals(754, RssParser.parseDuration("754"))
        assertEquals(12 * 60 + 34, RssParser.parseDuration("12:34"))
        assertEquals(3723, RssParser.parseDuration("1:02:03"))
        assertEquals(0, RssParser.parseDuration(null))
        assertEquals(0, RssParser.parseDuration("garbage"))
    }

    @Test
    fun `invalid feed returns null`() {
        assertNull(RssParser.parse("<rss><channel></channel></rss>", "f")) // no title
        assertNull(RssParser.parse("not xml", "f"))
    }

    @Test
    fun `item without audio enclosure is skipped`() {
        val noAudio = """
            <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"><channel>
              <title>C</title>
              <item><title>No media</title></item>
              <item><title>Has media</title><enclosure url="https://x/y.mp3" type="audio/mpeg"/></item>
            </channel></rss>
        """.trimIndent()
        val p = RssParser.parse(noAudio, "f")!!
        assertEquals(1, p.episodes.size)
        assertEquals("Has media", p.episodes.first().title)
    }
}
