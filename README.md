# Kobot

Kobot is a Discord bot written in Kotlin on Spring Boot and [JDA](https://github.com/discord-jda/JDA).
It plays music in voice channels, tracks and ranks memes posted in meme channels, keeps per-member
word counts, and has moderator and bot-administrator commands for managing servers.

- Kotlin 2.3 on Java 25, Spring Boot 4, JDA 6
- MongoDB for persistence
- RabbitMQ for meme integration events (optional meme-repost feature)
- [lavaplayer](https://github.com/lavalink-devs/lavaplayer) for audio playback (YouTube, Vimeo and other sources)

## Commands

Kobot has three kinds of command.

| Kind | Where | Trigger | Who can use it |
| --- | --- | --- | --- |
| Regular | Server text channels | `discord.commandPrefix` (default `>`), or as a slash command | Everyone |
| Moderator | Server text channels | `discord.privilegedCommandPrefix` (default `$`) | Server moderators and the primary privileged user |
| Administrator | Direct message to the bot | `discord.privilegedCommandPrefix` (default `$`) | Bot administrators |

When the bot joins a server that has no moderators, the server owner is added as its first moderator.

### Regular commands

All of these are also registered as Discord slash commands (except under the `dev` profile, see below).

| Command | Description |
| --- | --- |
| `help` | List the built-in commands |
| `info` | Bot information |
| `ping` | pong |
| `clear` | Clear command invocations and bot messages |
| `wordcounts` | List member word counts |
| `memes <week\|month>` | List server memes by week or month |
| `memeranking [sort]` | List server members by their meme score |

Voice commands:

| Command | Description |
| --- | --- |
| `play <url>` | Play audio via YouTube, Vimeo etc. |
| `summon` | Make the bot join your voice channel |
| `disconnect` | Disconnect the bot from the current voice channel |
| `queue` | Get the list of queued tracks |
| `trackinfo` | Get the currently playing track |
| `skip` | Skip the currently playing track |
| `clearqueue` | Clear the track queue |
| `vol <0-100>` | Set the bot's volume level |
| `getvol` | Get the bot's current volume level |
| `voicemove` | Move everyone in the bot's voice channel to wherever the bot is moved |

The bot leaves a voice channel automatically once only bots are left in it.

### Moderator commands

| Command | Description |
| --- | --- |
| `commands` | List moderator commands |
| `ping` | pong |
| `moderators` | List the server's moderators |
| `addmoderator @user…` | Make users moderators |
| `removeadmin @user…` | Remove moderators |
| `addmemechannel` | Track memes posted in this channel |
| `removememechannel` | Stop tracking memes in this channel |
| `deafen` | Ignore regular commands in this channel |
| `undeafen` | Respond to regular commands in this channel again |
| `isdeafened` | Show whether this channel is deafened |
| `purge <n>` | Delete the last `n` messages |

### Administrator commands

Send these to the bot in a direct message.

| Command | Description |
| --- | --- |
| `ping` | pong |
| `info` | Bot information |
| `admins` | List bot administrators |
| `addadmin <user>` | Add a bot administrator |
| `removeadmin <user>` | Remove a bot administrator |
| `addrole <user> <role>` / `removerole <user> <role>` | Grant or revoke an administrator role (`Primary`, `Global`, `InspectAdmin`, `ManageAdmin`, `Logging`) |
| `inspectroles <user>` | Show a user's administrator roles |
| `restart` | Restart the bot |

## Memes

Image attachments posted in a meme channel (see `addmemechannel`) are recorded as memes. A meme's score is
its upvotes minus its downvotes; `memes` and `memeranking` report on them.

With `features.meme-repost=true`, each image posted in a meme channel is also published to the
`meme-posted-exchange` RabbitMQ exchange. Kobot listens on `meme-repost-queue` (bound to
`meme-repost-exchange`) and replies in the original channel when an external service reports that a
meme is a repost. The repost-detection service itself is not part of this repository.

## Configuration

Settings live in `src/main/resources/application.properties` and can be overridden with environment
variables using Spring's relaxed binding (for example `discord.token` → `DISCORD_TOKEN`).

| Property | Default | Description |
| --- | --- | --- |
| `discord.token` | *(none, required)* | Discord bot token |
| `discord.commandPrefix` | `>` | Prefix for regular commands |
| `discord.voiceCommandPrefix` | `<` | Shown in `help` output |
| `discord.privilegedCommandPrefix` | `$` | Prefix for moderator and administrator commands |
| `discord.primaryPrivilegedUserId` | *(none)* | Discord user ID that is always a bot administrator and can use moderator commands in every server |
| `spring.rabbitmq.addresses` | `amqp://guest:guest@localhost` | RabbitMQ connection |
| `spring.mongodb.uri` / `spring.mongodb.database` | Spring defaults (`localhost:27017`) | MongoDB connection |
| `features.meme-repost` | `false` | Publish meme-posted events and handle repost replies |

Keep secrets such as the Discord token in a `.env` file (gitignored) or in environment variables,
never in `application.properties`.

### Dev profile

Running with the `dev` Spring profile (`SPRING_PROFILES_ACTIVE=dev`) skips registering slash
commands with Discord, which is useful when testing against a shared bot account.

## Running

### Docker Compose

Create a `.env` file with at least your token:

```env
DISCORD_TOKEN=your-bot-token
DISCORD_PRIMARYPRIVILEGEDUSERID=your-discord-user-id
```

Then start the bot along with MongoDB and RabbitMQ:

```sh
docker compose up
```

The RabbitMQ management UI is available at <http://localhost:15672> (`guest` / `guest`).

### Locally

Start only the backing services, then run the bot with Gradle:

```sh
docker compose up mongo rabbitmq
DISCORD_TOKEN=your-bot-token \
SPRING_MONGODB_URI=mongodb://local:local@localhost:27017 \
SPRING_MONGODB_DATABASE=local \
./gradlew bootRun
```

A container image is published to `ghcr.io/opentoucan/kobot` by the `image-builder` workflow.

## Development

You need JDK 25 and Docker. A Nix dev shell (`nix develop`) and a dev container (`.devcontainer/`)
are provided with the JDK, Gradle, ktlint, detekt and Task installed.

```sh
./gradlew test                     # all tests, plus a JaCoCo report
./gradlew test --tests '*SpringGuildServiceTest'   # a single test class
ktlint --editorconfig .editorconfig "./src/**/*.kt" build.gradle.kts
detekt --config detekt.yaml --input ./src,build.gradle.kts
```

Persistence and service tests start MongoDB through Testcontainers, so Docker must be running.
CI runs the tests and both linters on every pull request.

### Adding a command

1. Create a class extending `Command(commandString, description, commandParameters)` in
   `command/`, annotate it with `@Component` and implement `execute(event: DiscordMessageEvent)`.
2. Implement `ISlashCommand` as well to register it as a slash command; its `CommandParameter`s
   become slash options. `PingCommand` is the minimal example.
3. Moderator and administrator commands implement `IModeratorCommand` or `IAdminCommand` instead.

Commands are discovered automatically by the command factories. Put reusable logic in a service under
`services/` and return an `OperationResult` rather than throwing for expected failures.

A Grafana dashboard for the bot's metrics is in `dashboards/overview.json`.

## Licence

[Apache License 2.0](LICENSE)
