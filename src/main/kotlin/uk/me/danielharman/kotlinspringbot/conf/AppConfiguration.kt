package uk.me.danielharman.kotlinspringbot.conf

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
class AppConfiguration {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Bean
    fun actorSystem() : ActorSystem {
        val actorSystem = ActorSystem.create("akka-spring-test");
        Providers.SPRING_EXTENSION_PROVIDER.get(actorSystem).initialize(applicationContext);
        return actorSystem
    }

}