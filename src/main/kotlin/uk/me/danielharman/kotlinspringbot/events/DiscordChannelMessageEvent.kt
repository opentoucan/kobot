package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import java.io.InputStream

class DiscordChannelMessageEvent(
    event: GuildMessageReceivedEvent
) : DiscordMessageEvent(
    event.message.contentStripped,
    event.channel,
    event.message.author,
    event.guild,
    event.message.attachments,
    event.message.mentionedUsers
) {
    override fun reply(embed: MessageEmbed, invokerOnly: Boolean) {
        this.channel.sendMessageEmbeds(embed).queue()
    }

    override fun reply(file: InputStream, filename: String) {
        this.channel.sendFile(file, filename).queue()
    }

    override fun reply(msg: String, invokerOnly: Boolean) {
        this.channel.sendMessage(msg).queue()
    }

    private var paramsPointer: Int = 1

    override fun getParamValue(commandParameter: CommandParameter): CommandParameter {
        commandParameter.reset()

        val split = content.split(" ")

        if (paramsPointer < split.size) {
            val value = split[paramsPointer++]

            val finalVal: Any? = when (commandParameter.type) {
                CommandParameter.ParamType.Word -> value
                CommandParameter.ParamType.String -> {
                    val subList = split.subList(paramsPointer - 1, split.size).joinToString(" ")
                    paramsPointer = split.size
                    subList
                }
                CommandParameter.ParamType.Long -> {
                    val parseVal = value.toLongOrNull()
                    commandParameter.error = parseVal == null
                    parseVal
                }
                CommandParameter.ParamType.Boolean -> {
                    val parseVal = value.toBooleanStrictOrNull()
                    commandParameter.error = parseVal == null
                    parseVal
                }
                CommandParameter.ParamType.Mentionable -> {
                    if (!mentionedUsers.isNullOrEmpty()) {
                        mentionedUsers[0].id
                    } else {
                        null
                    }
                }
            }
            commandParameter.value = finalVal

        }

        if (commandParameter.required && commandParameter.value == null) {
            commandParameter.error = true;
        }

        return commandParameter
    }

}