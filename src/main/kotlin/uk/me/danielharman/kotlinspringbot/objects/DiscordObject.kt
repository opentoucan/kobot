package uk.me.danielharman.kotlinspringbot.objects

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.listeners.MessageListener
import uk.me.danielharman.kotlinspringbot.services.*

object DiscordObject {

    lateinit var jda: JDA
    var initialised : Boolean = false

    fun init(guildService: GuildService,
             adminCommandService: AdminCommandService,
             commandService: CommandService,
             memeService: MemeService,
             properties: KotlinBotProperties
    ) {

        ApplicationLogger.logger.info("Starting discord")

        val builder: JDABuilder = JDABuilder.create(
            properties.token,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        )
            .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "${properties.commandPrefix}help"))
            .addEventListeners(MessageListener(guildService, adminCommandService, commandService, properties, memeService))

        initialised = true
        jda = builder.build().awaitReady()
    }

    fun destroy(){
        if (initialised){
            jda.shutdown()
        }
    }
}