package uk.me.danielharman.kotlinspringbot.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.SchemaUpdater
import uk.me.danielharman.kotlinspringbot.models.XkcdComic

@Service
class XkcdService(val mongoOperations: MongoOperations) {

    private val latestUrl = "https://xkcd.com/info.0.json"
    private val comicUrl = "https://xkcd.com/%d/info.0.json"

    data class XkcdLatest(val num: Int)

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

    private fun setLast(ver: Int): Int {
        mongoOperations.upsert(Query.query(Criteria.where("_id").`is`("latest")),
                Update.update("schemaVersion", ver), SchemaUpdater.ApplicationOpts::class.java, "XkcdLatest")

        return getLast()?.num ?: 0
    }

    private fun getLast(): XkcdLatest? {
        return mongoOperations.findOne(Query.query(Criteria.where("_id").`is`("latest")),
                XkcdLatest::class.java, "XkcdLatest")
    }

}