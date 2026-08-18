package io.github.meko123456.khma.data.rss

import io.github.meko123456.khma.data.model.Podcast
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Fetches a feed URL over HTTP and hands the body to [RssParser]. */
class FeedFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
    /** Downloads and parses [feedUrl]; failure (network or malformed feed) is returned, not thrown. */
    suspend fun fetch(feedUrl: String): Result<Podcast> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(feedUrl).header("User-Agent", "Khma/0.1").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body?.string() ?: throw IOException("Empty feed")
                RssParser.parse(body, feedUrl) ?: throw IOException("Not a valid podcast feed")
            }
        }
    }
}
