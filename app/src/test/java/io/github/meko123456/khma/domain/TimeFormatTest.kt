package io.github.meko123456.khma.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormatTest {

    // --- duration (episode list) ---------------------------------------------

    @Test
    fun durationUnderAnHourIsMinutes() = assertEquals("8 min", TimeFormat.duration(8 * 60))

    @Test
    fun durationOverAnHourIsHoursAndMinutes() =
        assertEquals("1h 46m", TimeFormat.duration(3600 + 46 * 60))

    @Test
    fun durationDropsLeftoverSeconds() =
        assertEquals("2h 6m", TimeFormat.duration(2 * 3600 + 6 * 60 + 59))

    @Test
    fun durationOfZeroOrNegativeIsUnknown() {
        assertNull(TimeFormat.duration(0))
        assertNull(TimeFormat.duration(-5))
    }

    // --- clock (playback position) -------------------------------------------

    @Test
    fun clockUnderAnHourIsMinutesAndSeconds() = assertEquals("12:34", TimeFormat.clock(754_000))

    @Test
    fun clockPadsSecondsToTwoDigits() = assertEquals("0:05", TimeFormat.clock(5_000))

    @Test
    fun clockOverAnHourIncludesHours() =
        assertEquals("2:06:46", TimeFormat.clock((2 * 3600 + 6 * 60 + 46) * 1000L))

    @Test
    fun clockPadsMinutesPastAnHour() = assertEquals("1:02:03", TimeFormat.clock(3_723_000))

    @Test
    fun clockOfZeroIsZero() = assertEquals("0:00", TimeFormat.clock(0))

    @Test
    fun negativePositionClampsToZero() = assertEquals("0:00", TimeFormat.clock(-1_000))
}
