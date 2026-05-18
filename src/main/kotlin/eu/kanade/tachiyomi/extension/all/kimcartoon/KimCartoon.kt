package eu.kanade.tachiyomi.extension.all.kimcartoon

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class KimCartoon : ParsedHttpSource() {
    override val name = "KimCartoon"
    override val baseUrl = "https://kimcartoon.si"
    override val lang = "all"
    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addCloudflareInterceptor()
        .build()

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Referer", baseUrl)
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")

    // AJAX Episode Loading
    override fun episodeListParse(response: Response): List<SEpisode> {
        // Use the AJAX URL from your logs
        val s = "hserver" 
        val ajaxUrl = "$baseUrl/ajax/anime/load_episodes_v2?s=$s"
        
        val ajaxResponse = client.newCall(GET(ajaxUrl, headers)).execute()
        val document = ajaxResponse.asJsoup()
        
        return document.select("div.episode-list a").map { element ->
            SEpisode.create().apply {
                url = element.attr("href")
                name = element.text()
            }
        }
    }

    // Video Resolution
    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        // Extract iframe src as seen in your logs
        val iframeSrc = document.select("iframe#player").attr("src")
        
        // You would typically use a library like Jsoup or regex to parse the iframe content
        // This is a placeholder for the logic to extract the M3U8/MP4
        return listOf(Video(iframeSrc, "Default", iframeSrc, headers.newBuilder().set("Referer", iframeSrc).build()))
    }

    override fun popularAnimeSelector() = "div.list-cartoon a"
    override fun popularAnimeFromElement(element: Element) = SAnime.create().apply {
        title = element.text()
        setUrlWithoutDomain(element.attr("href"))
    }
    
    // Placeholder implementation for required methods
    override fun latestUpdatesSelector() = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element) = popularAnimeFromElement(element)
    override fun popularAnimeRequest(page: Int) = GET("$baseUrl/Cartoon", headers)
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/Cartoon", headers)
    override fun searchAnimeRequest(query: String, page: Int, filters: FilterList) = GET("$baseUrl/Seach?s=$query", headers)
    override fun searchAnimeSelector() = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element) = popularAnimeFromElement(element)
    override fun animeDetailsParse(document: Document) = SAnime.create().apply {
        title = document.select("h1").text()
        description = document.select("div.description").text()
    }
}