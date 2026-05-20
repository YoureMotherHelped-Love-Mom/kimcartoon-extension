package eu.kanade.tachiyomi.extension.all.kimcartoon

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.Hoster
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class KimCartoon : ParsedAnimeHttpSource() {
    override val name = "KimCartoon"
    override val baseUrl = "https://kimcartoon.si"
    override val lang = "all"
    override val supportsLatest = true

    override val client: OkHttpClient = network.client

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Referer", baseUrl)

    // ==================== Requests ====================

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/CartoonList/MostPopular?page=$page", headers)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/CartoonList/LatestUpdate?page=$page", headers)
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        return GET("$baseUrl/Search/?s=${query.replace(" ", "+")}&page=$page", headers)
    }

    // ==================== Listing Parsing ====================

    override fun popularAnimeSelector() = "div.list-cartoon > div.item"

    override fun popularAnimeFromElement(element: Element): SAnime {
        return SAnime.create().apply {
            val link = element.select("a.thumb").first()
            if (link != null) {
                title = link.select("h2.title").text()
                setUrlWithoutDomain(link.attr("href"))
                thumbnail_url = link.select("img").attr("abs:src")
            }
        }
    }

    override fun popularAnimeNextPageSelector(): String? {
        return "ul.pager a:contains(>>)"
    }

    override fun searchAnimeSelector() = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element) = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String? = "ul.pager a:contains(>>)"

    override fun latestUpdatesSelector() = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element) = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String? = "ul.pager a:contains(>>)"

    // ==================== Detail ====================

    override fun animeDetailsParse(document: Document): SAnime {
        return SAnime.create().apply {
            title = document.select("div.right_movie h1 a.bigChar").text().ifEmpty {
                document.select("h1").text()
            }
            description = document.select("div.summary p").text()
            thumbnail_url = document.select("div.left_movie img").attr("abs:src")
            genre = document.select("div.right_movie a[href^='/Genre/']").joinToString(", ") { element ->
                element.text().replace(" Cartoon", "")
            }

            val pageText = document.text()
            status = when {
                pageText.contains("Status: Completed") -> SAnime.COMPLETED
                pageText.contains("Status: Ongoing") -> SAnime.ONGOING
                else -> SAnime.UNKNOWN
            }
        }
    }

    // ==================== Episodes ====================

    override fun episodeListSelector(): String {
        return "div.listing div.full.item_ep"
    }

    override fun episodeFromElement(element: Element): SEpisode {
        val link = element.select("h3 a").first()
        return SEpisode.create().apply {
            if (link != null) {
                name = link.text()
                setUrlWithoutDomain(link.attr("href"))
            }
            episode_number = Regex("Episode (\\d+(?:\\.\\d+)?)").find(name)?.groupValues?.get(1)?.toFloatOrNull()
                ?: 0f
            val dateText = element.select("div:last-child").text()
            if (dateText.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    date_upload = dateFormat.parse(dateText)?.time ?: 0L
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = Jsoup.parse(response.body!!.string(), baseUrl)
        val episodes = document.select(episodeListSelector()).map { element ->
            episodeFromElement(element)
        }
        return episodes.reversed()
    }

    // ==================== Seasons ====================

    override fun seasonListSelector(): String {
        return "select.season option, div.season a"
    }

    override fun seasonFromElement(element: Element): SAnime {
        return SAnime.create().apply {
            title = element.text()
            val href = element.attr("href").ifEmpty { element.attr("value") }
            if (href.isNotEmpty()) {
                setUrlWithoutDomain(href)
            }
        }
    }

    // ==================== Hosters ====================

    override fun hosterListParse(response: Response): List<Hoster> {
        val document = Jsoup.parse(response.body!!.string(), baseUrl)
        return document.select("#selectServer option").map { option ->
            Hoster(
                hosterUrl = "$baseUrl${option.attr("value")}",
                hosterName = option.text(),
            )
        }
    }

    override fun videoListParse(response: Response, hoster: Hoster): List<Video> {
        val document = Jsoup.parse(response.body!!.string(), baseUrl)
        return extractVideos(document).map { video ->
            video.copy(videoTitle = hoster.hosterName)
        }
    }

    private fun extractVideos(document: Document): List<Video> {
        val iframe = document.select("iframe#iframe, iframe#player").first()
        if (iframe != null) {
            val src = iframe.attr("abs:src")
            if (src.isNotEmpty()) {
                return listOf(Video(videoUrl = src, videoTitle = "Default"))
            }
        }
        return document.select("video source").map { source ->
            Video(videoUrl = source.attr("abs:src"), videoTitle = "Default")
        }
    }

    // ==================== Video URL Resolution ====================

    @Suppress("DEPRECATION")
    override fun videoUrlParse(response: Response): String {
        val document = Jsoup.parse(response.body!!.string(), baseUrl)
        return document.select("iframe#iframe, iframe#player").attr("abs:src")
            .ifEmpty { document.select("video source").attr("abs:src") }
    }

    override fun getFilterList(): AnimeFilterList {
        return AnimeFilterList()
    }
}
