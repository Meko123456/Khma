package io.github.meko123456.khma.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnsubscribeNoticeTest {

    @Test
    fun withNothingDownloadedOnlyTheProgressGoes() =
        assertEquals("Your place in each episode is forgotten.", UnsubscribeNotice.text(0))

    @Test
    fun oneDownloadIsSaidInTheSingular() =
        assertEquals("Your place in each episode is forgotten, and its downloaded episode is deleted.", UnsubscribeNotice.text(1))

    @Test
    fun severalDownloadsAreCounted() =
        assertEquals("Your place in each episode is forgotten, and its 3 downloaded episodes are deleted.", UnsubscribeNotice.text(3))
}
