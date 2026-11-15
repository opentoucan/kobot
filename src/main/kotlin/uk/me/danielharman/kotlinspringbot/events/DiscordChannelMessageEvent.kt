package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.FileUpload
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.ParsedCommandParameter
import java.io.InputStream

class DiscordChannelMessageEvent(
    event: MessageReceivedEvent,
) : DiscordMessageEvent(
    event.message.contentStripped,
    event.channel,
    event.message.author,
    event.guild,
    event.message.attachments,
    event.message.mentions.users,
) {
    override fun reply(
        embed: MessageEmbed,
        invokerOnly: Boolean,
    ) {
        this.channel.sendMessageEmbeds(embed).queue()
    }

    override fun reply(
        file: InputStream,
        filename: String,
    ) {
        try {
            this.channel.sendFiles(FileUpload.fromData(file, filename)).complete()
        } catch (e: ErrorResponseException) {
            if (e.message?.contains("40005") == true) {
                this.reply(Embeds.createErrorEmbed("File was too large to send"))
            } else {
                this.reply(Embeds.createErrorEmbed("Failed so send attachment"))
            }
        }
    }

    override fun reply(
        msg: String,
        invokerOnly: Boolean,
    ) {
        this.channel.sendMessage(msg).queue()
    }

    private var paramsPointer: Int = 1

    override fun getParamValue(commandParameter: CommandParameter): ParsedCommandParameter {
        val split = content.split(" ")
        var value: Any? = null
        var error = false

        if (paramsPointer < split.size) {
            val rawValue = split[paramsPointer++]

            value =
                when (commandParameter.type) {
                    CommandParameter.ParamType.Word -> rawValue

                    CommandParameter.ParamType.String -> {
                        val subList = split.subList(paramsPointer - 1, split.size).joinToString(" ")
                        paramsPointer = split.size
                        subList
                    }

                    CommandParameter.ParamType.Long -> {
                        val parsedValue = rawValue.toLongOrNull()
                        error = parsedValue == null
                        parsedValue
                    }

                    CommandParameter.ParamType.Boolean -> {
                        val parsedValue = rawValue.toBooleanStrictOrNull()
                        error = parsedValue == null
                        parsedValue
                    }

                    CommandParameter.ParamType.Mentionable -> {
                        if (!mentionedUsers.isNullOrEmpty()) {
                            mentionedUsers[0].id
                        } else {
                            null
                        }
                    }
                }
        }

        if (commandParameter.required && value == null) {
            error = true
        }

        return ParsedCommandParameter(commandParameter, value, error)
    }
}
