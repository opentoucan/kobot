package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class AdminsListCommand(private val guildService: GuildService,
                        private val properties: KotlinBotProperties) : IAdminCommand {

    private val commandString: String = "admins"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(createAdminUsersEmbed(event)).queue()
    }

    private fun createAdminUsersEmbed(message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name
        val guild = guildService.getGuild(message.guild.id)

        return if (guild == null) {
            Embeds.createErrorEmbed("Could not find data for $guildName")
        } else {

            val stringBuilder = StringBuilder()
            val primaryAdmin = message.guild.retrieveMemberById(properties.primaryPrivilegedUserId).complete()


            stringBuilder.append("Bot controller:  ${primaryAdmin.nickname ?: primaryAdmin.user.asTag}\n")

            guild.privilegedUsers.forEach { s ->
                run {
                    val member = message.guild.retrieveMemberById(s).complete()
                    stringBuilder.append(member?.nickname ?: member.user.asTag ?: s)
                }
            }

            EmbedBuilder()
                    .appendDescription(stringBuilder.toString())
                    .setColor(0x9d03fc)
                    .setTitle("Admins for $guildName")
                    .build()
        }

    }

}