package io.github.meko123456.khma.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.meko123456.khma.data.db.KhmaDatabase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Streams an episode's audio to local storage, reporting percent via [setProgress].
 * Writes to a .part file and renames on success so a killed download never looks complete.
 * On network failure it retries a few times, then fails.
 */
class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val feedUrl = inputData.getString(KEY_FEED) ?: return@withContext Result.failure()
        val guid = inputData.getString(KEY_GUID) ?: return@withContext Result.failure()
        val audioUrl = inputData.getString(KEY_URL) ?: return@withContext Result.failure()

        val target = Downloads.file(applicationContext, feedUrl, guid)
        val part = java.io.File(target.parentFile, target.name + ".part")

        try {
            val request = Request.Builder().url(audioUrl).header("User-Agent", "Khma/0.1").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext retryOrFail()
                val body = response.body ?: return@withContext retryOrFail()
                val total = body.contentLength()
                body.byteStream().use { input ->
                    part.outputStream().use { output ->
                        val buf = ByteArray(64 * 1024)
                        var downloaded = 0L
                        var lastPct = -1
                        while (true) {
                            if (isStopped) { part.delete(); return@withContext Result.failure() }
                            val n = input.read(buf)
                            if (n < 0) break
                            output.write(buf, 0, n)
                            downloaded += n
                            if (total > 0) {
                                val pct = ((downloaded * 100) / total).toInt()
                                if (pct != lastPct) {
                                    lastPct = pct
                                    setProgress(workDataOf(KEY_PROGRESS to pct))
                                }
                            }
                        }
                    }
                }
            }
            if (!part.renameTo(target)) {
                part.copyTo(target, overwrite = true)
                part.delete()
            }
            KhmaDatabase.get(applicationContext).episodeDao()
                .setDownloadPath(feedUrl, guid, android.net.Uri.fromFile(target).toString())
            Result.success()
        } catch (e: Exception) {
            part.delete()
            retryOrFail()
        }
    }

    private fun retryOrFail(): Result =
        if (runAttemptCount < 3) Result.retry() else Result.failure()

    companion object {
        const val KEY_FEED = "feed"
        const val KEY_GUID = "guid"
        const val KEY_URL = "url"
        const val KEY_PROGRESS = "progress"

        // No call timeout: episode audio can be large and slow.
        private val client = OkHttpClient.Builder()
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }
}
