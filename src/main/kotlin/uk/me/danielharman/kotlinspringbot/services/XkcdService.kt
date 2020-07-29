package uk.me.danielharman.kotlinspringbot.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.XkcdComic

@Service
class XkcdService {

    private val latestUrl = "https://xkcd.com/info.0.json"
    private val comicUrl = "https://xkcd.com/%d/info.0.json"

    fun getLatestComic(): XkcdComic {

        val client = HttpClient(CIO)
        val response = runBlocking{client.get<String>(latestUrl)}
        return Json.parse(XkcdComic.serializer(), response)
    }

    fun getComic(number: Int) : XkcdComic?{
        val client = HttpClient(CIO)
        val url = comicUrl.format(number)
        val response = runBlocking{client.get<HttpResponse>(url)}

        if(response.status.value == 404)
            return null

        val s = runBlocking { response.readText() }
        return Json.parse(XkcdComic.serializer(), s )
    }

}