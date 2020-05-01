package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.services.TestService

@Component
@Scope("prototype")
/**
 * TestService is passed in via implicit constructor injection (recent Spring versions doesn't required @Autowired now)
 */
class TestActor(val testService: TestService) : UntypedAbstractActor() {

    init{
        logger.info("Starting TestActor")
    }

    override fun onReceive(message: Any?) = when (message) {
        "test" -> println(testService.greet("Me"))
        else -> println("received unknown message")
    }
}
