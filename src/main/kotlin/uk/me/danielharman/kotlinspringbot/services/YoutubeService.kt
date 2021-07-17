package uk.me.danielharman.kotlinspringbot.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties

@Service
class YoutubeService(private val properties: KotlinBotProperties) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getAudio(url: String) {
        logger.info("Getting latest comic")

        val client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = JacksonSerializer()
            }
        }
        logger.info("http://${properties.youtubeMicroserviceHost}:${properties.youtubeMicroservicePort}/download")

        val response = runBlocking {
            client.post<HttpResponse>("http://${properties.youtubeMicroserviceHost}:${properties.youtubeMicroservicePort}/download"){
                contentType(ContentType.Application.Json)
                body = hashMapOf("url" to url, "mediaType" to "audio")
            }
        }

        response.status
    }

}