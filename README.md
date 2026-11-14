# Kobot

A Kotlin Spring Boot Discord bot.

## Local setup

1. Copy `.env.example` to `.env` and fill in the values.
2. Start the required backing services:
   - MongoDB
   - RabbitMQ
3. Run the app:

   ./gradlew bootRun

## Required environment variables

- `DISCORD_TOKEN`
- `DISCORD_COMMAND_PREFIX`
- `DISCORD_VOICE_COMMAND_PREFIX`
- `DISCORD_PRIVILEGED_COMMAND_PREFIX`
- `DISCORD_PRIMARY_PRIVILEGED_USER_ID`
- `SPRING_RABBITMQ_ADDRESSES`
- `FEATURES_MEME_REPOST`

Do not commit real secrets to this repository.

## Testing

./gradlew test
