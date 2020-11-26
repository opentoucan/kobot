package uk.me.danielharman.kotlinspringbot.services.admin

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository

import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.failResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.successResult
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role

@Service
class AdministratorService (private val repository: AdministratorRepository, private val props: KotlinBotProperties) {

    fun getAdminById(id: String): OperationResult<Administrator?>{
        val administrator = repository.findById(id)

        if(administrator.isEmpty)
            return failResult("Administrator not found")

        return successResult(administrator.get())
    }

    fun getAdminByDiscordId(id: String): OperationResult<Administrator?>{
        val administrator = repository.getByDiscordId(id) ?: return failResult("Administrator not found")
        return successResult(administrator)
    }

    fun createAdmin(id: String, roles: Set<Role>): OperationResult<Administrator?>{
        val administrator = repository.save(Administrator(id, roles))

        return successResult(administrator)
    }

    fun removeAdmin(id: String) : OperationResult<String?>{
        if (id == props.primaryPrivilegedUserId){
            return failResult("Cannot remove primary admin")
        }
        repository.deleteByDiscordId(id)
        return successResult("Deleted")
    }

    fun addRoles(roles: Set<Role>){
        TODO()
    }

    fun removeRoles(roles: Set<Role>){
        TODO()
    }
}