package uk.me.danielharman.kotlinspringbot.models.admin

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role

@Document("Administrators")
class Administrator(val discordId: String, val roles: Set<Role> = mutableSetOf(Role.Global)) {

    @Id
    lateinit var id: String
    fun addRole(role: Role) = roles.plus(role)
    fun removeRole(role: Role) = roles.minus(role)

}