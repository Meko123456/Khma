package io.github.meko123456.khma.data.rss

import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Why a feed couldn't be added, in words. The dialog used to show the exception's own message:
 * "Unable to resolve host "feeds.example.com": No address associated with hostname" offline, and
 * a bare "HTTP 404" for a mistyped path.
 */
object FeedFailure {
    fun describe(error: Throwable): String = when (error) {
        is UnknownHostException, is ConnectException, is InterruptedIOException ->
            "Couldn't reach that address. Check the phone is online and the address is right."
        is HttpStatusException -> when (error.code) {
            404, 410 -> "Nothing is at that address (HTTP ${error.code}). Check it against the podcast's website."
            else -> "The feed's site answered HTTP ${error.code}. Try again later."
        }
        is NotAFeedException ->
            "That address isn't a podcast feed. Look for an RSS link on the podcast's website."
        is NoPublicFeedException ->
            "Apple Podcasts has no public feed for that show, so only Apple's app can play it."
        is NoSuchShowException ->
            "Apple Podcasts has no show at that link. Check the whole link was copied."
        // OkHttp's verdict on an address it can't parse at all.
        is IllegalArgumentException -> "That isn't a web address."
        else -> "Couldn't add that feed."
    }
}
