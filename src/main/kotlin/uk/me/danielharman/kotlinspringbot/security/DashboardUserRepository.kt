package uk.me.danielharman.kotlinspringbot.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "dashboardUsers")
data class DashboardUser(var username: String, var password: String) {
    @Id
    lateinit var id: String
}

@Repository
interface DashboardUserRepository : MongoRepository<DashboardUser, String> {
    fun findByUsername(username: String): DashboardUser?
}