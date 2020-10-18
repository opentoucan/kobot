package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command

class SetNickCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split('"')

        val mentionedMembers = event.message.mentionedMembers

        for (member in mentionedMembers) {
            try {
                member.modifyNickname(split[1]).queue()
                event.channel.sendMessage("Set member's nickname!").queue()
            } catch (e: Exception) {
                event.channel.sendMessage("Could not change ${member.nickname}'s nickname!").queue()
            }
        }
    }
}

