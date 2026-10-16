package io.github.meko123456.khma.data.rss

import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedFailureTest {

    @Test
    fun `no connection is said in words, not as the resolver's error`() {
        val offline = "Couldn't reach that address. Check the phone is online and the address is right."
        assertEquals(offline, FeedFailure.describe(UnknownHostException("Unable to resolve host \"feeds.example.com\"")))
        assertEquals(offline, FeedFailure.describe(ConnectException("Failed to connect")))
        assertEquals(offline, FeedFailure.describe(InterruptedIOException("timeout")))
    }

    @Test
    fun `a status the site answered with is named, with what to do about it`() {
        assertEquals(
            "Nothing is at that address (HTTP 404). Check it against the podcast's website.",
            FeedFailure.describe(HttpStatusException(404)),
        )
        assertEquals("The feed's site answered HTTP 503. Try again later.", FeedFailure.describe(HttpStatusException(503)))
    }

    @Test
    fun `a page that isn't a feed, and an address that isn't one, are told apart`() {
        assertEquals(
            "That address isn't a podcast feed. Look for an RSS link on the podcast's website.",
            FeedFailure.describe(NotAFeedException()),
        )
        assertEquals("That isn't a web address.", FeedFailure.describe(IllegalArgumentException("unexpected url")))
    }
}
