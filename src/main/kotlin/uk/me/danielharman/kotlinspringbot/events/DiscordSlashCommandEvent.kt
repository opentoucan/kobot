package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import java.io.InputStream

class DiscordSlashCommandEvent(
    private val event: SlashCommandInteraction,
) : DiscordMessageEvent(
    event.options.fold("") { acc, opt -> "$acc ${opt.asString}" },
    event.channel,
    event.user,
    event.guild,
) {
    private val replyLock = Any()

    /**
     * Acknowledges the interaction with a "thinking" response if the command has not replied yet.
     * Discord fails interactions that are not acknowledged within three seconds.
     */
    fun deferIfNotAcknowledged() {
        synchronized(replyLock) {
            if (!event.isAcknowledged) event.deferReply().queue()
        }
    }

    // The first response must be the interaction reply; anything after that (including the reply
    // to a deferred interaction) has to go through the interaction hook as a follow-up.
    private fun send(
        message: MessageCreateData,
        invokerOnly: Boolean,
        waitForResult: Boolean,
    ) {
        synchronized(replyLock) {
            if (event.isAcknowledged) {
                val action = event.hook.sendMessage(message).setEphemeral(invokerOnly)
                if (waitForResult) action.complete() else action.queue()
            } else {
                val action = event.reply(message).setEphemeral(invokerOnly)
                if (waitForResult) action.complete() else action.queue()
            }
        }
    }

    override fun reply(
        embed: MessageEmbed,
        invokerOnly: Boolean,
    ) {
        val messageCreateBuilder = MessageCreateBuilder()
        messageCreateBuilder.addEmbeds(embed)
        send(messageCreateBuilder.build(), invokerOnly, waitForResult = true)
    }

    override fun reply(
        file: InputStream,
        filename: String,
    ) {
        val messageCreateBuilder = MessageCreateBuilder()
        messageCreateBuilder.addFiles(FileUpload.fromData(file, filename))
        try {
            send(messageCreateBuilder.build(), invokerOnly = false, waitForResult = true)
        } catch (e: ErrorResponseException) {
            if (e.message?.contains("40005") == true) {
                this.reply(Embeds.createErrorEmbed("File was too large to send"), true)
            } else {
                this.reply(Embeds.createErrorEmbed("Failed so send attachment"), true)
            }
        }
    }

    override fun reply(
        msg: String,
        invokerOnly: Boolean,
    ) {
        send(MessageCreateBuilder().setContent(msg).build(), invokerOnly, waitForResult = false)
    }

    override fun getParamValue(definition: CommandParameter): CommandParameter {
        // Commands share their CommandParameter definitions and can run concurrently, so fill in
        // a copy for this invocation rather than mutating the shared instance
        val commandParameter = definition.copy()

        val findFirst =
            this.event.options
                .stream()
                .filter { x ->
                    x.name.lowercase() == commandParameter.name.lowercase() &&
                        matchType(x.type, commandParameter.type)
                }.findFirst()

        if (findFirst.isPresent) {
            val get = findFirst.get()

            val value: Any? =
                when (get.type) {
                    OptionType.UNKNOWN -> null
                    OptionType.SUB_COMMAND -> null
                    OptionType.SUB_COMMAND_GROUP -> null
                    OptionType.STRING -> get.asString
                    OptionType.INTEGER -> get.asLong
                    OptionType.BOOLEAN -> get.asBoolean
                    OptionType.USER -> get.asUser
                    OptionType.CHANNEL -> get.asChannel
                    OptionType.ROLE -> get.asRole
                    OptionType.MENTIONABLE -> get.asMentionable
                    else -> null
                }
            commandParameter.value = value
        }

        if (commandParameter.required && commandParameter.value == null) {
            commandParameter.error = true
        }

        return commandParameter
    }

    private fun matchType(
        type: OptionType,
        type2: CommandParameter.ParamType,
    ): Boolean = (type == OptionType.INTEGER && type2 == CommandParameter.ParamType.Long) ||
        (type == OptionType.STRING && type2 == CommandParameter.ParamType.String) ||
        (type == OptionType.STRING && type2 == CommandParameter.ParamType.Word) ||
        (type == OptionType.MENTIONABLE && type2 == CommandParameter.ParamType.Mentionable) ||
        (type == OptionType.BOOLEAN && type2 == CommandParameter.ParamType.Boolean)
}
