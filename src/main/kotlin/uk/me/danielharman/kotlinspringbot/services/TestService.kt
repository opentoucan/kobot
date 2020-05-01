package uk.me.danielharman.kotlinspringbot.services

import org.springframework.stereotype.Component

@Component
class TestService{

    fun greet(name: String) = "Hello $name";

}