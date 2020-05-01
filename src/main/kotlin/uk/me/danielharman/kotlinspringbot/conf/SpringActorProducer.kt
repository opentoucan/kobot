package uk.me.danielharman.kotlinspringbot.conf

import akka.actor.Actor
import akka.actor.IndirectActorProducer
import org.springframework.context.ApplicationContext

class SpringActorProducer(var applicationContext: ApplicationContext, var beanActorName: String) : IndirectActorProducer {

    override fun actorClass(): Class<out Actor> {
        return applicationContext.getType(beanActorName) as Class<out Actor>
    }

    override fun produce(): Actor {
        return this.applicationContext.getBean(beanActorName) as Actor
    }

}