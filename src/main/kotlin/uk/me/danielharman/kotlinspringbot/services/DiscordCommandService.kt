package uk.me.danielharman.kotlinspringbot.services

import me.xdrop.fuzzywuzzy.FuzzySearch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand
import uk.me.danielharman.kotlinspringbot.repositories.DiscordCommandRepository
import java.io.InputStream
import java.lang.Integer.min
import kotlin.math.ceil

@Service
class DiscordCommandService(
    private val repository: DiscordCommandRepository,
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val mongoTemplate: MongoTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun commandCount(guildId: String): OperationResult<Long, String> =
        when (val guild = springGuildService.getGuild(guildId)) {
            is Failure -> guild
            is Success -> Success(repository.countByGuildId(guildId))
        }

    fun getCommands(
        guildId: String,
        page: Int = 0,
        pageSize: Int = 20,
        sort: Order = Order.asc("key")
    ): OperationResult<List<DiscordCommand>, String> = when (val guild = springGuildService.getGuild(guildId)) {
        is Failure -> guild
        is Success -> Success(
            repository.findAllByGuildId(guildId, PageRequest.of(page, pageSize, Sort.by(sort))).toList()
        )
    }

    fun getCommand(guildId: String, key: String): OperationResult<DiscordCommand, String> =
        when (val guild = springGuildService.getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                val command = repository.findFirstByGuildIdAndKey(guildId, key)
                if (command == null) {
                    Failure("$key not found")
                } else {
                    Success(command)
                }
            }
        }

    fun getRandomCommand(guildId: String): OperationResult<DiscordCommand, String> {
        return when (val guild = springGuildService.getGuild(guildId)) {
            is Failure -> guild
            is Success -> {

                val count = repository.countByGuildId(guild.value.guildId)

                var command: DiscordCommand? = null

                var triesCounter = 0

                //The matching for the guild is done after the command is retrieved so guilds with fewer commands were less likely to
                // be returned
                if(count > 0) {
                    while (command == null && triesCounter < 100) {
                        command = mongoTemplate.aggregate(
                            Aggregation.newAggregation(
                                Aggregation.match(
                                    Criteria.where(DiscordCommand::guildId.name).`is`(guild.value.guildId)
                                ),
                                Aggregation.sample(1)
                            ),
                            "DiscordCommands",
                            DiscordCommand::class.java
                        ).uniqueMappedResult
                        triesCounter++
                    }
                }

                logger.info("Got a command in $triesCounter iterations")

                if (command == null) {
                    Failure("Did not find any commands")
                } else {
                    Success(command)
                }
            }
        }
    }

    fun createStringCommand(
        guildId: String,
        key: String,
        content: String,
        creatorId: String,
        overwrite: Boolean
    ): OperationResult<DiscordCommand, String> {
        return createCommand(guildId, key, content, null, DiscordCommand.CommandType.STRING, creatorId, true)
    }

    fun createFileCommand(
        guildId: String,
        key: String,
        fileName: String,
        creatorId: String,
        inputStream: InputStream
    ): OperationResult<DiscordCommand, String> =
        when (val command =
            createCommand(guildId, key, null, fileName, DiscordCommand.CommandType.FILE, creatorId, true)) {
            is Failure -> command
            is Success -> {
                attachmentService.saveFile(inputStream, guildId, fileName, key)
                command
            }
        }


    fun createCommand(
        guildId: String, key: String, content: String?, fileName: String?,
        type: DiscordCommand.CommandType, creatorId: String, overwrite: Boolean
    ): OperationResult<DiscordCommand, String> {

        when (val guild = springGuildService.getGuild(guildId)) {
            is Failure -> return guild
            is Success -> {
                val command = getCommand(guildId, key)

                if (command is Failure || overwrite) {

                    if (command is Success && overwrite) {
                        if (command.value.type == DiscordCommand.CommandType.FILE) {
                            attachmentService.deleteAttachment(
                                guildId,
                                command.value.fileName ?: "",
                                command.value.key
                            )
                        }
                        repository.deleteById(command.value.id)
                    }

                    return Success(
                        repository.save(
                            DiscordCommand(
                                guildId,
                                key,
                                content,
                                fileName,
                                type,
                                creatorId
                            )
                        )
                    )
                }
                return command
            }
        }
    }

    fun deleteCommand(guildId: String, key: String): OperationResult<String, String> =
        when (val guild = springGuildService.getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                when (val command = getCommand(guildId, key)) {
                    is Failure -> command
                    is Success -> {
                        repository.deleteById(command.value.id)
                        if (command.value.type == DiscordCommand.CommandType.FILE) {
                            attachmentService.deleteAttachment(
                                guildId,
                                command.value.fileName ?: "",
                                command.value.key
                            )
                        }
                        Success("Deleted $key")
                    }
                }
            }
        }

    private val CMD_PAGE_SIZE = 50

    fun searchCommand(
        guildId: String,
        searchTerm: String,
        limit: Int = 20
    ): OperationResult<List<Pair<String, Int>>, String> {

        when (val commandCount = commandCount(guildId)) {
            is Failure -> return commandCount
            is Success -> {
                if (commandCount.value <= 0) return Success(listOf())

                val commandList = mutableListOf<Pair<String, Int>>()

                val noOfPages = ceil(commandCount.value.toDouble() / CMD_PAGE_SIZE).toInt()

                //Paginated so that we aren't pulling 1000s of commands at a time if that ever happens
                for (page in 0 until noOfPages) {

                    val query =
                        Query(Criteria.where("guildId").`is`(guildId)).with(PageRequest.of(page, CMD_PAGE_SIZE))
                    val cmds = mongoTemplate.find(query, DiscordCommand::class.java)

                    commandList.addAll(cmds
                        .map { cmd -> Pair(cmd.key, FuzzySearch.ratio(searchTerm, cmd.key)) }
                        .filter { (_, ratio) -> ratio > 40 })

                }

                return Success(commandList.sortedByDescending { (_, ratio) -> ratio }
                    .subList(0, min(limit, commandList.size)))
            }
        }
    }

}