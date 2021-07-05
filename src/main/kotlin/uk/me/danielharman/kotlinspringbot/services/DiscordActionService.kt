package uk.me.danielharman.kotlinspringbot.services

import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject

@Service
class DiscordActionService {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getTextChannel(channelId: String): OperationResult<GuildChannel, String>{
        val channel = DiscordObject.jda.getGuildChannelById(channelId)

        return if(channel == null){
            Failure("Could not find channel")
        }else{
            Success(channel)
        }

    }

    fun getSelfUser(): OperationResult<User, String> {
        return Success(DiscordObject.jda.selfUser)
    }

    fun getUserById(creatorId: String): OperationResult<User, String> {
        return try {
            Success(DiscordObject.jda.retrieveUserById(creatorId).complete())
        } catch (e: NumberFormatException) {
            logger.error(e.message)
            Failure("Failed to get user")
        }
    }

}