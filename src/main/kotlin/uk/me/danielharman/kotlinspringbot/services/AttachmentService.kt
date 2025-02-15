package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import java.io.InputStream

@Service
class AttachmentService(
    private val gf: GridFsTemplate,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun saveFile(
        inputStream: InputStream,
        guildId: String,
        fileName: String,
        id: String,
    ): OperationResult<String, String> {
        logger.info("Saving $guildId:$fileName:$id")
        val store = gf.store(inputStream, "$guildId:$fileName:$id")
        return Success(store.toHexString())
    }

    fun getFile(
        guildId: String,
        fileName: String,
        id: String,
    ): OperationResult<InputStream, String> {
        logger.info("Getting $guildId:$fileName:$id")
        val findOne = gf.findOne(Query(Criteria.where("filename").`is`("$guildId:$fileName:$id")))
        return Success(gf.getResource(findOne).inputStream)
    }

    fun deleteAttachment(
        guildId: String,
        fileName: String,
        id: String,
    ): OperationResult<String, String> {
        logger.info("Deleting attachment $guildId:$fileName")
        gf.delete(Query(Criteria.where("filename").`is`("$guildId:$fileName:$id")))
        return Success("Removed $guildId:$fileName")
    }
}
