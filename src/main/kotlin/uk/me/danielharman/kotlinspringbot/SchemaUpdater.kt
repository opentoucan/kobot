package uk.me.danielharman.kotlinspringbot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import java.time.LocalDateTime

class SchemaUpdateException(msg: String) : Exception(msg)

class SchemaUpdater(
    private val mongoOperations: MongoOperations,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    data class ApplicationOpts(
        val schemaVersion: Int,
    )

    private val latestVer = 2

    fun updateSchema() {
        logger.info("[Schema] Checking schema version.")

        val findOne = getOpts()

        var schemaVer = findOne?.schemaVersion ?: 0

        logger.info("[Schema] Version $schemaVer")

        if (schemaVer < 1) schemaVer = updateTo1()
        if (schemaVer < 2) schemaVer = updateTo2()

        logger.info("[Schema] Schema now at $schemaVer")

        if (schemaVer != latestVer) {
            throw SchemaUpdateException("[Schema] Schema version mismatch! $schemaVer != $latestVer")
        }
    }

    private fun updateTo1(): Int {
        logger.info("[Update to 1] Migrating custom commands")

        val findAll = mongoOperations.findAll(SpringGuild::class.java)

        findAll.forEach { sg ->
            run {
                if (sg.savedCommands.size > 0) {
                    val update = Update()

                    sg.savedCommands.forEach { cmd ->
                        update.set(
                            "customCommands.${cmd.key}",
                            SpringGuild.CustomCommand(
                                cmd.value,
                                SpringGuild.CommandType.STRING,
                                "",
                                LocalDateTime.now(),
                            ),
                        )
                    }

                    mongoOperations.upsert(
                        query(where("_id").`is`(sg.id)),
                        update,
                        SpringGuild::class.java,
                    )
                }
            }
        }

        return setVersion(1)
    }

    private fun updateTo2(): Int {
        logger.info("[Update to 2] Migrating custom commands to DiscordCommand model")

        val findAll = mongoOperations.findAll(SpringGuild::class.java)

        findAll.forEach { sg ->
            run {
                logger.info("Migrating guild ${sg.id} ${sg.guildId}")
                sg.customCommands.entries.forEach { c ->
                    run {
                        logger.info("Migrating command ${c.key}")
                        var content: String? = null
                        var fileName: String? = null
                        var type: DiscordCommand.CommandType = DiscordCommand.CommandType.STRING

                        when (c.value.type) {
                            SpringGuild.CommandType.STRING -> {
                                content = c.value.value
                            }
                            SpringGuild.CommandType.FILE -> {
                                fileName = c.value.value
                                type = DiscordCommand.CommandType.FILE
                            }
                        }
                        val newCommand =
                            DiscordCommand(
                                sg.guildId,
                                c.key,
                                content,
                                fileName,
                                type,
                                c.value.creatorId,
                                created = c.value.created,
                            )
                        mongoOperations.save(newCommand, "DiscordCommands")
                    }
                }
            }
        }

        return setVersion(2)
    }

    private fun setVersion(ver: Int): Int {
        mongoOperations.upsert(
            query(where("_id").`is`("appopts")),
            update("schemaVersion", ver),
            ApplicationOpts::class.java,
            "ApplicationOpts",
        )

        return getOpts()?.schemaVersion ?: 0
    }

    private fun getOpts(): ApplicationOpts? = mongoOperations.findOne(
        query(where("_id").`is`("appopts")),
        ApplicationOpts::class.java,
        "ApplicationOpts",
    )
}
