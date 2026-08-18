package io.github.meko123456.khma.data.rss

import io.github.meko123456.khma.data.model.Episode
import io.github.meko123456.khma.data.model.Podcast
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

/**
 * Pure RSS-to-model parser. Uses the JVM's built-in DOM (available on Android and
 * in plain unit tests), so it needs no device or Robolectric. Reads the common
 * RSS + iTunes-namespace fields a podcast client needs.
 */
object RssParser {

    /** Parses [xml]; returns null if it isn't a usable feed (no channel/title). */
    fun parse(xml: String, feedUrl: String): Podcast? {
        val doc = runCatching {
            DocumentBuilderFactory.newInstance()
                .apply { isNamespaceAware = false }
                .newDocumentBuilder()
                .parse(InputSource(StringReader(xml)))
        }.getOrNull() ?: return null

        val channel = doc.documentElement?.directChild("channel") ?: return null
        val title = channel.directChildText("title") ?: return null

        return Podcast(
            feedUrl = feedUrl,
            title = title,
            author = channel.directChildText("itunes:author") ?: channel.directChildText("managingEditor").orEmpty(),
            description = channel.directChildText("description").orEmpty(),
            imageUrl = channel.directChild("itunes:image")?.getAttribute("href")?.ifBlank { null }
                ?: channel.directChild("image")?.directChildText("url"),
            episodes = channel.directChildren("item").mapNotNull(::parseItem),
        )
    }

    private fun parseItem(item: Element): Episode? {
        val audioUrl = item.directChildren("enclosure")
            .firstOrNull { it.getAttribute("type").startsWith("audio", ignoreCase = true) || it.getAttribute("url").isNotBlank() }
            ?.getAttribute("url")?.ifBlank { null } ?: return null
        val title = item.directChildText("title") ?: return null
        return Episode(
            guid = item.directChildText("guid") ?: item.directChildText("link") ?: audioUrl,
            title = title,
            audioUrl = audioUrl,
            description = item.directChildText("description").orEmpty(),
            imageUrl = item.directChild("itunes:image")?.getAttribute("href")?.ifBlank { null },
            durationSeconds = parseDuration(item.directChildText("itunes:duration")),
            pubDateMillis = parseDate(item.directChildText("pubDate")),
        )
    }

    /** "3600" (secs), "12:34" (m:s) or "1:02:03" (h:m:s) → seconds. */
    fun parseDuration(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        val parts = raw.trim().split(":").map { it.toIntOrNull() ?: return 0 }
        return when (parts.size) {
            1 -> parts[0]
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0
        }
    }

    /** RFC-822 pubDate → epoch millis, or 0 if unparseable. */
    fun parseDate(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0
        val text = raw.trim()
        for (pattern in DATE_PATTERNS) {
            runCatching { return SimpleDateFormat(pattern, Locale.US).parse(text)!!.time }
        }
        return 0
    }

    private val DATE_PATTERNS = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss Z",
    )

    // --- DOM helpers: direct children only (getElementsByTagName would grab descendants) ---

    private fun Element.directChildren(tag: String): List<Element> {
        val out = ArrayList<Element>()
        val kids = childNodes
        for (i in 0 until kids.length) {
            val n = kids.item(i)
            if (n.nodeType == Node.ELEMENT_NODE && (n as Element).tagName == tag) out += n
        }
        return out
    }

    private fun Element.directChild(tag: String): Element? = directChildren(tag).firstOrNull()

    private fun Element.directChildText(tag: String): String? =
        directChild(tag)?.textContent?.trim()?.ifBlank { null }
}
