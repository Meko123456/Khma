package io.github.meko123456.khma.domain

/** What an unsubscribe takes with it, said before it happens: none of it comes back on resubscribing. */
object UnsubscribeNotice {

    fun text(downloadedEpisodes: Int): String = buildString {
        append("Your place in each episode is forgotten")
        when (downloadedEpisodes) {
            0 -> append(".")
            1 -> append(", and its downloaded episode is deleted.")
            else -> append(", and its $downloadedEpisodes downloaded episodes are deleted.")
        }
    }
}
