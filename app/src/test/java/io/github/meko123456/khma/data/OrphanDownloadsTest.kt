package io.github.meko123456.khma.data

import io.github.meko123456.khma.data.download.OrphanDownloads
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OrphanDownloadsTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val now = 1_800_000_000_000L
    private val dayAgo = now - 24 * 60 * 60 * 1000L

    private fun file(name: String, modified: Long): File =
        folder.newFile(name).apply { writeText("audio"); setLastModified(modified) }

    @Test
    fun audioNoEpisodeRefersToIsDeleted() {
        val kept = file("1_2.audio", dayAgo)
        val orphan = file("3_4.audio", dayAgo)

        val deleted = OrphanDownloads.sweep(folder.root, referenced = setOf("1_2.audio"), now = now)

        assertEquals(listOf("3_4.audio"), deleted.map { it.name })
        assertTrue(kept.exists())
        assertTrue(!orphan.exists())
    }

    @Test
    fun aFreshFileAndAPartialDownloadAreLeftAlone() {
        // Landed a minute ago and not recorded yet, or still being written: either way, not ours.
        val landing = file("5_6.audio", now - 60_000L)
        val partial = file("7_8.audio.part", dayAgo)

        val deleted = OrphanDownloads.sweep(folder.root, referenced = emptySet(), now = now)

        assertTrue(deleted.isEmpty())
        assertTrue(landing.exists() && partial.exists())
    }

    @Test
    fun noDownloadsFolderIsNothingToDo() {
        assertTrue(OrphanDownloads.sweep(File(folder.root, "missing"), emptySet(), now).isEmpty())
    }
}
