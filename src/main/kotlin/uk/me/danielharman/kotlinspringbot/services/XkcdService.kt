package uk.me.danielharman.kotlinspringbot.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.xkcd.XkcdModel

@Service
class XkcdService {
    fun getComic(urlStr: String): XkcdModel {
        val client = HttpClient(CIO)
        val response = runBlocking{client.get<String>(urlStr)}
        return Json.parse(XkcdModel.serializer(), response)
    }
}