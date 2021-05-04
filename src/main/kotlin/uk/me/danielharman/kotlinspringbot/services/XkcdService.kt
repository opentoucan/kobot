package uk.me.danielharman.kotlinspringbot.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.SchemaUpdater
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.XkcdComic

@Service
class XkcdService(private val mongoOperations: MongoOperations) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val latestUrl = "https://xkcd.com/info.0.json"
    private val comicUrl = "https://xkcd.com/%d/info.0.json"

    data class XkcdLatest(val num: Int)

    fun getLatestComic(): OperationResult<XkcdComic, String> {
        logger.info("Getting latest comic")
        return getComic()
    }

    fun getComic(number: Int? = null): OperationResult<XkcdComic, String> {
        val client = HttpClient(CIO)
        val url = if(number == null) latestUrl else comicUrl.format(number)
        val response = runBlocking { client.get<HttpResponse>(url) }

        if (response.status.value != 200) {
            val error = "Got ${response.status.value} from XKCD server"
            logger.error(error)
            return Failure(error)
        }
        val toString = runBlocking { response.readText()}
        return Success(Json.decodeFromString(XkcdComic.serializer(), toString))
    }

    fun setLast(ver: Int): OperationResult<Int, String> {
        logger.info("Setting last comic to $ver")
        mongoOperations.upsert(
            Query.query(Criteria.where("_id").`is`("latest")),
            Update.update("num", ver), SchemaUpdater.ApplicationOpts::class.java, "XkcdLatest"
        )

        return Success((getLast() as Success).value.num)
    }

    fun getLast(): OperationResult<XkcdLatest, String> {
        val result = mongoOperations.findOne(
            Query.query(Criteria.where("_id").`is`("latest")),
            XkcdLatest::class.java, "XkcdLatest"
        )
        return if(result == null) Success(XkcdLatest(0)) else Success(result)
    }

}
