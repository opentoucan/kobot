package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand
import uk.me.danielharman.kotlinspringbot.repositories.DiscordCommandRepository
import java.io.InputStream

@Service
class DiscordCommandService(private val repository: DiscordCommandRepository, private val guildService: GuildService, private val attachmentService: AttachmentService) {

    fun commandCount(guildId: String): Long {
        guildService.getGuild(guildId) ?: return 0
        return repository.countByGuildId(guildId);
    }

    fun getCommands(guildId: String, page: Int = 0, pageSize: Int = 20, sort: Order = Order.asc("key") ): List<DiscordCommand> {
        guildService.getGuild(guildId) ?: return listOf()
        return repository.findAllByGuildId(guildId, PageRequest.of(page, pageSize, Sort.by(sort))).toList()
    }

    fun getCommand(guildId: String, key: String): DiscordCommand? {
        guildService.getGuild(guildId) ?: return null
        return repository.findFirstByGuildIdAndKey(guildId, key)
    }

    fun createStringCommand(guildId: String, key: String, content: String, creatorId: String, overwrite: Boolean): DiscordCommand? {
        return createCommand(guildId, key, content, null, DiscordCommand.CommandType.STRING, creatorId, true)
    }

    fun createFileCommand(guildId: String, key: String, fileName: String, creatorId: String, inputStream: InputStream): DiscordCommand? {
        val command = createCommand(guildId, key, null, fileName, DiscordCommand.CommandType.FILE, creatorId, true)
                ?: return null
        attachmentService.saveFile(inputStream, guildId, fileName, key)
        return command
    }

    fun createCommand(guildId: String, key: String, content: String?, fileName: String?,
                      type: DiscordCommand.CommandType, creatorId: String, overwrite: Boolean): DiscordCommand? {
        guildService.getGuild(guildId) ?: return null

        val command: DiscordCommand? = getCommand(guildId, key)

        if (command == null || overwrite) {

            if (command != null && overwrite) {
                if (command.type == DiscordCommand.CommandType.FILE) {
                    attachmentService.deleteAttachment(guildId, command.fileName ?: "", command.key)
                }
                repository.deleteById(command.id)
            }

            return repository.save(DiscordCommand(guildId, key, content, fileName, type, creatorId))
        }
        return null
    }

    fun deleteCommand(guildId: String, key: String): Boolean {
        guildService.getGuild(guildId) ?: return false
        val command = getCommand(guildId, key) ?: return false
        repository.deleteById(command.id)
        if (command.type == DiscordCommand.CommandType.FILE) {
            attachmentService.deleteAttachment(guildId, command.fileName ?: "", command.key)
        }
        return true
    }

}