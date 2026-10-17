package io.github.meko123456.khma.data.download

import java.io.File

/**
 * Finds downloaded audio that no episode refers to any more and deletes it.
 *
 * Unsubscribing deleted a podcast's episodes but left their audio in files/downloads until
 * d56c3d2, and nothing in the app lists a file whose episode is gone, so those files could only
 * ever be cleared by wiping the app's storage. This clears them.
 */
object OrphanDownloads {

    /** A file this young may be a download that has landed but not been recorded yet. */
    const val GRACE_MILLIS: Long = 60 * 60 * 1000L

    /**
     * Deletes the `.audio` files in [dir] whose names are not in [referenced] and that were last
     * written over [GRACE_MILLIS] before [now]. A `.part` is left to the download that owns it.
     * Returns what was deleted.
     */
    fun sweep(dir: File, referenced: Set<String>, now: Long): List<File> {
        val orphans = dir.listFiles().orEmpty().filter {
            it.isFile && it.name.endsWith(".audio") && it.name !in referenced && now - it.lastModified() > GRACE_MILLIS
        }
        return orphans.filter { it.delete() }
    }
}
