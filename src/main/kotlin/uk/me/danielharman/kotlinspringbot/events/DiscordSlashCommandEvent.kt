package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.commands.OptionType
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import java.io.InputStream

class DiscordSlashCommandEvent(private val event: SlashCommandEvent) : DiscordMessageEvent(
    event.options.fold("") { acc, opt -> "$acc ${opt.asString}" },
    event.channel,
    event.user,
    event.guild
) {

    private var hasReplied = false

    override fun reply(embed: MessageEmbed, invokerOnly: Boolean) {
        if(!hasReplied){
            event.reply(embed.title ?: "").setEphemeral(invokerOnly).complete()
            hasReplied = true;
        }
        channel.sendMessageEmbeds(embed).queue()
    }

    override fun reply(file: InputStream, filename: String) {
        if(!hasReplied){
            event.reply("Your file sir").setEphemeral(true).complete()
            hasReplied = true
        }
        try {
            this.channel.sendFile(file, filename).complete()
        } catch (e: ErrorResponseException){
            if (e.message?.contains("40005") == true){
                this.reply(Embeds.createErrorEmbed("File was too large to send"), true)
            } else {
                this.reply(Embeds.createErrorEmbed("Failed so send attachment"), true)
            }
        }
    }

    override fun reply(msg: String, invokerOnly: Boolean) {
        if(!hasReplied) {
            event.reply(msg).setEphemeral(invokerOnly).queue()
            hasReplied = true;
        }
        else{
            event.channel.sendMessage(msg).queue()
        }
    }

    override fun getParamValue(commandParameter: CommandParameter): CommandParameter {
        commandParameter.reset()

        val findFirst = this.event.options.stream()
            .filter { x -> x.name.lowercase() == commandParameter.name.lowercase() && matchType(x.type, commandParameter.type) }
            .findFirst()

        if (findFirst.isPresent) {
            val get = findFirst.get()

            val value: Any? = when (get.type) {
                OptionType.UNKNOWN -> null
                OptionType.SUB_COMMAND -> null
                OptionType.SUB_COMMAND_GROUP -> null
                OptionType.STRING -> get.asString
                OptionType.INTEGER -> get.asLong
                OptionType.BOOLEAN -> get.asBoolean
                OptionType.USER -> get.asUser
                OptionType.CHANNEL -> get.asMessageChannel
                OptionType.ROLE -> get.asRole
                OptionType.MENTIONABLE -> get.asMentionable
                else -> null
            }
            commandParameter.value = value
        }

        if (commandParameter.required && commandParameter.value == null) {
            commandParameter.error = true;
        }

        return commandParameter
    }

    private fun matchType(type: OptionType, type2: CommandParameter.ParamType): Boolean {
        return (type == OptionType.INTEGER && type2 == CommandParameter.ParamType.Long)
                || (type == OptionType.STRING && type2 == CommandParameter.ParamType.String)
                || (type == OptionType.STRING && type2 == CommandParameter.ParamType.Word)
                || (type == OptionType.MENTIONABLE && type2 == CommandParameter.ParamType.Mentionable)
                || (type == OptionType.BOOLEAN && type2 == CommandParameter.ParamType.Boolean)
    }

}