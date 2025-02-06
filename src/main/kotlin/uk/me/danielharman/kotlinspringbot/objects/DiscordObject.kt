package uk.me.danielharman.kotlinspringbot.objects

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.CommandParameter.ParamType
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import java.lang.RuntimeException
import java.time.LocalDateTime

object DiscordObject {
    lateinit var jda: JDA
    var initialised: Boolean = false
    var startTime: LocalDateTime? = null
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    var listeners: List<ListenerAdapter> = listOf()

    fun init(
        properties: KotlinBotProperties,
        commands: List<ISlashCommand>,
    ) {
        logger.info("Starting discord")
        logger.info("${listeners.size} listeners registered")
        logger.info("${commands.size} commands registered")
        if (commands.size > 100) {
            throw RuntimeException("Too many commands, discord limits slash commands to 100")
        }
        val builder: JDABuilder =
            JDABuilder
                .create(
                    properties.token,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_EXPRESSIONS,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                ).setActivity(
                    Activity.of(
                        Activity.ActivityType.CUSTOM_STATUS,
                        "${properties.commandPrefix}help",
                    ),
                )

        for (listener: ListenerAdapter in listeners) {
            builder.addEventListeners(listener)
        }
        initialised = true
        startTime = LocalDateTime.now()
        jda = builder.build().awaitReady()

        // Don't register commands when running in dev mode
        if (!ApplicationInfo.isDev) {
            logger.info("Registering slash commands with Discord.")
            val updateCommands = jda.updateCommands()

            val commandData = mutableListOf<CommandData>()
            for (command: ISlashCommand in commands) {
                val data = Commands.slash(command.commandString, command.description)

                for (commandParameter: CommandParameter in command.commandParameters) {
                    data.addOption(
                        convertParamTypeToJdaOptionType(commandParameter.type),
                        commandParameter.name.lowercase(),
                        commandParameter.description,
                        commandParameter.required,
                    )
                }
                commandData.add(data)
            }
            updateCommands.addCommands(commandData).queue()
        }
    }

    fun registerListeners(listeners: List<ListenerAdapter>) {
        this.listeners = listeners
    }

    fun teardown() {
        logger.info("Discord teardown")
        if (initialised) {
            jda.shutdown()
        }
        initialised = false
        startTime = null
    }

    private fun convertParamTypeToJdaOptionType(type: ParamType): OptionType = when (type) {
        ParamType.Word -> OptionType.STRING
        ParamType.String -> OptionType.STRING
        ParamType.Long -> OptionType.INTEGER
        ParamType.Boolean -> OptionType.BOOLEAN
        ParamType.Mentionable -> OptionType.MENTIONABLE
    }
}
