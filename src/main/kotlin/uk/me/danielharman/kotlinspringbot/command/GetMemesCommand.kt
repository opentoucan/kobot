package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.util.stream.Collectors

class GetMemesCommand(private val memeService: MemeService) : Command {

    override fun execute(event: GuildMessageReceivedEvent) {

        var monthsMemes = memeService.getMonthsMemes(event.guild.id)

        monthsMemes = monthsMemes.stream().sorted { o1, o2 -> o1.upvotes - o2.upvotes }.collect(Collectors.toList())

        println(monthsMemes)

        event.channel.sendMessage(monthsMemes.toString()).queue()

    }

}