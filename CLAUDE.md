
# CLAUDE.md

Kobot is a Discord bot written in Kotlin on Spring Boot 4 and JDA 6. It targets
Java 25 (`jvmToolchain(25)`) and Kotlin 2.3, persists to MongoDB, and uses
RabbitMQ for integration events.

## Commands

- `./gradlew test`: compile and run all tests (a JaCoCo report is generated afterwards)
- `./gradlew test --tests 'uk.me.danielharman.kotlinspringbot.services.SpringGuildServiceTest'`: run a single test class
- `./gradlew bootRun`: run the bot. It needs `DISCORD_TOKEN` (bound to `discord.token`), MongoDB and RabbitMQ
- `docker compose up mongo rabbitmq`: start only the backing services; `docker compose up` also builds and runs the bot using secrets from `.env`
- Lint (CI runs both and fails on violations):
  - `ktlint --editorconfig .editorconfig "./src/**/*.kt" build.gradle.kts`
  - `detekt --config detekt.yaml --input ./src,build.gradle.kts`

Run the smallest relevant test first, then the full suite and both linters before finishing.

## Layout

Code lives under `src/main/kotlin/uk/me/danielharman/kotlinspringbot`; tests mirror it under `src/test/kotlin/...`.

- `command/`: Discord commands, grouped into `administrators/`, `moderators/`, `memes/` and `voice/`; base types are in `command/interfaces/`
- `factories/`: `CommandFactory`, `AdminCommandFactory` and `ModeratorCommandFactory` look up commands by name from Spring-injected lists
- `listeners/`: JDA `ListenerAdapter`s. `guildmessage/GuildMessageListener` handles prefix and slash dispatch
- `events/`: wrappers over JDA events (`DiscordMessageEvent`, `DiscordSlashCommandEvent`); `events/integration/` holds RabbitMQ meme-posted and repost events
- `services/`: reusable application logic (guilds, memes, attachments, Discord actions, admin)
- `models/`, `repositories/`, `mappers/`: MongoDB documents, Spring Data repositories and JDA→event mappers
- `objects/DiscordObject.kt`: builds the JDA client and registers slash commands
- `audio/`, `provider/`: voice playback through lavaplayer
- `properties/`: `KotlinBotProperties` (`discord.*`) and `FeatureProperties` (`features.*`)
- `helpers/OperationResult.kt`: sealed `Success`/`Failure` result type returned by services

## How commands work

- Regular commands extend `Command(commandString, description, commandParameters)`, implement `execute(event: DiscordMessageEvent)` and are annotated `@Component`. `CommandFactory` picks them up automatically.
- Adding `ISlashCommand` also registers the command as a Discord slash command. Its `commandParameters` (`CommandParameter`) become slash options. `PingCommand` is the minimal example.
- Admin and moderator commands implement `IAdminCommand` or `IModeratorCommand`, take a raw JDA `MessageReceivedEvent`, and are triggered by `discord.privilegedCommandPrefix`.
- Slash commands are **not** registered with Discord when the `dev` Spring profile is active (`ApplicationInfo.isDev`, set in `SetupService`).

## Conventions

- Follow the existing Kotlin, Spring and JDA patterns in nearby code. Use constructor injection and `@Component`/`@Service`.
- Keep business logic in services, not in listeners or command handlers. Services return `OperationResult` rather than throwing for expected failures.
- Preserve explicit error reporting. Don't swallow exceptions or return fallbacks that look like success.
- Keep changes focused. Avoid unrelated reformatting or refactoring, and preserve existing command behaviour.
- Add or update tests with behaviour changes, using:
  - JUnit 5 tests with Mockito and Hamcrest (Kotest is also available)
  - for persistence and service tests, `@SpringBootTest` + `@ActiveProfiles("test")` + `@Import(TestcontainersConfiguration::class)`, which runs MongoDB through Testcontainers, cleaning repositories in `@AfterEach`. Docker must be running for `./gradlew test`
  - `assertSuccess`/`assertFailure` from the test `helpers/OperationResult.kt`
- Update `README.md` when setup, configuration or user-facing behaviour changes.

## Configuration and secrets

- Runtime config is in `src/main/resources/application.properties`; tests use `src/test/resources/application.properties`.
- Never commit credentials, tokens or `.env` contents. Keep secrets in `.env` (gitignored) or in environment variables, never in `application.properties`.
- Dependencies are managed by Renovate (`.renovaterc.json5`). Keep versions pinned explicitly in `build.gradle.kts`, matching the existing style.
