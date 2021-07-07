package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

@Component
class ShowAvatarCommand(private val discordActionService: DiscordActionService) :
    Command(
        "avatar",
        "Get a user's avatar",
        listOf(CommandParameter(0, "Usertag", CommandParameter.ParamType.Mentionable, "User tag", true))
    ), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        val paramValue = event.getParamValue(commandParameters[0])
        val id = paramValue.asMentionable()

        if (paramValue.error || id == null) {
            event.reply("No users specified")
        }

        when (val member = discordActionService.getUserById(id ?: "")) {
            is Failure -> event.reply(Embeds.createErrorEmbed(member.reason))
            is Success -> event.reply(
                EmbedBuilder()
                    .setTitle("Avatar")
                    .setAuthor(member.value.asTag)
                    .setImage("${member.value.avatarUrl}?size=512")
                    .build()
            )
        }
    }

}