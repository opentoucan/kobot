package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.Guild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository

@Service
class GuildService(private val guildRepository: GuildRepository, private val mongoTemplate: MongoTemplate) {

    fun getGuild(serverId: String): Guild? = guildRepository.findByGuildId(serverId)
    fun createGuild(guildId: String): Guild = guildRepository.save(Guild(guildId))

    fun updateUserCount(guildId: String, userId: String, count: Int) {
        val update = Update()
        update.inc("userWordCounts.$userId", count)
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, Guild::class.java)
    }

    fun addWord(guildId: String, words: List<String>) {
        val stats = getGuild(guildId)

        if (words.isEmpty())
            return

        if (stats == null) {
            createGuild(guildId)
        }

        val update = Update()
        for (word: String in words) {
            update.inc("wordCounts.$word", 1)
        }
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, Guild::class.java)

    }

    fun saveCommand(guildId: String, command: String, phrase: String) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }
        val update = Update()
        update.set("savedCommands.$command", phrase)
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, Guild::class.java)
    }

    fun getCommand(guildId: String, command: String): String {
        val guild = getGuild(guildId)

        return if(guild != null){
            guild.savedCommands[command]?: "No such command"
        }
        else
            "Guild was not found"
    }

}