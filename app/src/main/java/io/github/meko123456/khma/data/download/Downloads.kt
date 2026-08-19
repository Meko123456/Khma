package io.github.meko123456.khma.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.github.meko123456.khma.data.db.EpisodeEntity
import java.io.File

/** Naming + enqueue helpers shared by [DownloadWorker] and the UI, so both agree on
 *  the unique work name and on-disk file for a given episode. */
object Downloads {

    fun uniqueName(feedUrl: String, guid: String): String = "dl:$feedUrl|$guid"

    fun file(context: Context, feedUrl: String, guid: String): File {
        val dir = File(context.filesDir, "downloads").apply { mkdirs() }
        return File(dir, "${feedUrl.hashCode()}_${guid.hashCode()}.audio")
    }

    /** Enqueues a download; KEEP means tapping again while one is running is a no-op. */
    fun enqueue(context: Context, e: EpisodeEntity) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_FEED to e.feedUrl,
                    DownloadWorker.KEY_GUID to e.guid,
                    DownloadWorker.KEY_URL to e.audioUrl,
                ),
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(e.feedUrl, e.guid), ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context, feedUrl: String, guid: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(feedUrl, guid))
    }
}
