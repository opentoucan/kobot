package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import java.io.InputStream

@Service
class AttachmentService(val gf: GridFsTemplate) {

    fun saveFile(inputStream: InputStream, guildId: String, fileName: String) {
        logger.info("[AttachmentService] Saving $guildId:$fileName")
        gf.store(inputStream, "$guildId:$fileName")
    }

    fun getFile(guildId: String, fileName: String): InputStream {
        logger.info("[AttachmentService] Getting $guildId:$fileName")
        val findOne = gf.findOne(Query(Criteria.where("filename").`is`("$guildId:$fileName")))
        return gf.getResource(findOne).inputStream
    }

}