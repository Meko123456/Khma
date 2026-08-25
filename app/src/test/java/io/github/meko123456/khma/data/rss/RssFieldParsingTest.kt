package io.github.meko123456.khma.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the two field-level helpers feeds get wrong most often: iTunes durations
 * (three different shapes in the wild) and RFC-822 pubDates (several timezone spellings).
 */
class RssFieldParsingTest {

    // --- parseDuration --------------------------------------------------------

    @Test
    fun bareSecondsAreUsedAsIs() = assertEquals(3600, RssParser.parseDuration("3600"))

    @Test
    fun minutesAndSeconds() = assertEquals(754, RssParser.parseDuration("12:34"))

    @Test
    fun hoursMinutesAndSeconds() = assertEquals(3723, RssParser.parseDuration("1:02:03"))

    @Test
    fun surroundingWhitespaceIsTolerated() = assertEquals(754, RssParser.parseDuration("  12:34 "))

    @Test
    fun missingOrJunkDurationIsZero() {
        assertEquals(0, RssParser.parseDuration(null))
        assertEquals(0, RssParser.parseDuration(""))
        assertEquals(0, RssParser.parseDuration("about an hour"))
        assertEquals(0, RssParser.parseDuration("1:2:3:4"))
    }

    // --- parseDate ------------------------------------------------------------

    @Test
    fun parsesRfc822WithNumericOffset() {
        val millis = RssParser.parseDate("Tue, 21 Jul 2026 10:00:00 +0400")
        assertTrue("expected a real timestamp, got $millis", millis > 0)
    }

    @Test
    fun parsesRfc822WithNamedZone() {
        val millis = RssParser.parseDate("Tue, 21 Jul 2026 10:00:00 GMT")
        assertTrue(millis > 0)
    }

    @Test
    fun parsesDateWithoutWeekday() {
        val millis = RssParser.parseDate("21 Jul 2026 10:00:00 +0000")
        assertTrue(millis > 0)
    }

    @Test
    fun sameInstantInDifferentZonesMatches() {
        val gmt = RssParser.parseDate("Tue, 21 Jul 2026 10:00:00 +0000")
        val plusFour = RssParser.parseDate("Tue, 21 Jul 2026 14:00:00 +0400")
        assertEquals(gmt, plusFour)
    }

    @Test
    fun missingOrUnparseableDateIsZero() {
        assertEquals(0L, RssParser.parseDate(null))
        assertEquals(0L, RssParser.parseDate(""))
        assertEquals(0L, RssParser.parseDate("last Tuesday"))
    }
}
