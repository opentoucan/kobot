package uk.me.danielharman.kotlinspringbot.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class MongoUserDetailsService(val dashboardUserRepository: DashboardUserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {

        val (name, password) = dashboardUserRepository.findByUsername(username)?: throw UsernameNotFoundException("User not found")

        return User(name, password, arrayListOf(SimpleGrantedAuthority("USER")))
    }
}