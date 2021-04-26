package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class AttachmentService(val gf: GridFsTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun saveFile(inputStream: InputStream, guildId: String, fileName: String, id: String) {
        logger.info("[AttachmentService] Saving $guildId:$fileName:$id")
        gf.store(inputStream, "$guildId:$fileName:$id")
    }

    fun getFile(guildId: String, fileName: String, id: String): InputStream {
        logger.info("[AttachmentService] Getting $guildId:$fileName:$id")
        val findOne = gf.findOne(Query(Criteria.where("filename").`is`("$guildId:$fileName:$id")))
        return gf.getResource(findOne).inputStream
    }

    fun deleteAttachment(guildId: String, fileName: String, id: String) {
        logger.info("[AttachmentService] Deleting attachment $guildId:$fileName")
        gf.delete(Query(Criteria.where("filename").`is`("$guildId:$fileName:$id")))
    }

}