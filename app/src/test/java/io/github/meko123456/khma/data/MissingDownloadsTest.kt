package io.github.meko123456.khma.data

import io.github.meko123456.khma.data.download.MissingDownloads
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MissingDownloadsTest {

    @get:Rule
    val folder = TemporaryFolder()

    // As DownloadWorker records it: Uri.fromFile(target).toString().
    private fun uriOf(file: File): String = file.toURI().toString()

    @Test
    fun aDownloadWhoseFileIsGoneIsFound() {
        val here = folder.newFile("1_2.audio")
        val restoredWithoutAudio = File(folder.root, "3_4.audio")

        val missing = MissingDownloads.find(listOf(uriOf(here), uriOf(restoredWithoutAudio)))

        assertEquals(listOf(uriOf(restoredWithoutAudio)), missing)
    }

    @Test
    fun theWorkersOwnFormIsRead() {
        // Uri.fromFile writes file:///data/…, three slashes; java.io.File.toURI writes file:/data/….
        val path = "file:///data/user/0/io.github.meko123456.khma/files/downloads/5_6.audio"
        val asked = mutableListOf<File>()

        val missing = MissingDownloads.find(listOf(path)) { asked += it; false }

        assertEquals(listOf(path), missing)
        assertEquals(File("/data/user/0/io.github.meko123456.khma/files/downloads/5_6.audio"), asked.single())
    }

    @Test
    fun aPathThatIsNotAFileUriIsLeftAlone() {
        val missing = MissingDownloads.find(listOf("https://example.com/episode.mp3", "not a uri")) { false }

        assertEquals(emptyList<String>(), missing)
    }
}
