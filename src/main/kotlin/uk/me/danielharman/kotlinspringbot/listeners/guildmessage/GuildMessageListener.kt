package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.factories.CommandFactory
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.mappers.toMessageEvent
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService


@Component
class GuildMessageListener(
    private val springGuildService: SpringGuildService,
    private val commandFactory: CommandFactory,
) : ListenerAdapter() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)


    //Leave a voice channel once everyone has left
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        if(event.channelLeft == null) return

        val voiceChannel = event.oldValue ?: return

        //Return if the event is triggered by the bot or by someone leaving a channel in a guild we don't have an audio manager for
        val audioManager = voiceChannel.jda.audioManagers.firstOrNull { am -> am.guild.id == voiceChannel.guild.id }

        if (event.member.id == event.jda.selfUser.id || audioManager == null) return


        val members = voiceChannel.members
        //If the channel is just us bots
        if ((members.firstOrNull { m -> !m.user.isBot } == null) && members.firstOrNull { m -> m.id == voiceChannel.jda.selfUser.id } != null) {
            audioManager.closeAudioConnection()
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        logger.info("Joined guild ${event.guild.name}")

        val getGuild = springGuildService.getGuild(event.guild.id)

        if ((getGuild is Failure) || (getGuild as Success).value.privilegedUsers.isEmpty()) {
            val owner = event.guild.retrieveOwner().complete()
            logger.info("Adding ${owner.nickname} as admin of guild")
            springGuildService.addModerator(event.guild.id, owner.id)
        }

        val defaultChannel = event.guild.defaultChannel?.asTextChannel() ?: return

        if (defaultChannel.canTalk()) {
            defaultChannel.sendTyping().queue()
            defaultChannel.sendMessage(":wave:").queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val messageEvent = event.toMessageEvent()
        try {
            commandFactory.getCommand(event.name)?.execute(messageEvent)
        } catch (e: Exception) {
            messageEvent.reply(
                "An internal error occurred while executing the command.",
                true
            )
            throw e
        }
    }
}
