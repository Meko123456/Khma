package io.github.meko123456.khma.data.download

import java.io.File
import java.net.URI

/**
 * Finds the downloads an episode still records whose audio isn't on the phone.
 *
 * Backup leaves files/downloads out (see data_extraction_rules.xml), so a restored phone has every
 * episode back, download path included, without the file. Such an episode was listed as
 * downloaded and failed to play ("Source error", with nothing on screen). Forgetting the path
 * lists it as not downloaded, and it streams.
 */
object MissingDownloads {

    /**
     * The paths in [recorded], `file:` URIs as DownloadWorker writes them, whose file doesn't
     * exist. A path that isn't a file URI is left alone.
     */
    fun find(recorded: List<String>, exists: (File) -> Boolean = File::exists): List<String> =
        recorded.filter { path ->
            val file = runCatching { File(URI(path)) }.getOrNull()
            file != null && !exists(file)
        }
}
