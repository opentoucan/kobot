package uk.me.danielharman.kotlinspringbot.services

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.ServerStats
import uk.me.danielharman.kotlinspringbot.repositories.StatsRepository

@Service
class StatsService(val statsRepository: StatsRepository) {

    fun addWord(serverId: String, words: List<String>) {

        val stats = statsRepository.findByServerId(serverId)

        if (stats == null) {
            val channelStats = ServerStats(serverId)
            for (word: String in words) {
                channelStats.wordCounts[word] = 1
            }
            statsRepository.save(channelStats)
        } else {
            for (word: String in words) {
                if (stats.wordCounts.containsKey(word)) {
                    stats.wordCounts[word] = stats.wordCounts[word]!! + 1
                } else {
                    stats.wordCounts[word] = 1
                }
            }
            statsRepository.save(stats)
        }
    }

    fun getStats(serverId: String): ServerStats? = statsRepository.findByServerId(serverId)

}