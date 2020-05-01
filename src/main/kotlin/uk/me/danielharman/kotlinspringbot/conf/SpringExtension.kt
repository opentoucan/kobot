package uk.me.danielharman.kotlinspringbot.conf

import akka.actor.AbstractExtensionId
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.Props
import org.springframework.context.ApplicationContext

object Providers {
    var SPRING_EXTENSION_PROVIDER: SpringExtension = SpringExtension()
}

class SpringExtension : AbstractExtensionId<SpringExt>() {

    override fun createExtension(system: ExtendedActorSystem?): SpringExt {
        return SpringExt()
    }

}

class SpringExt : Extension {

    @Volatile
    lateinit var applicationContext: ApplicationContext

    fun initialize(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun props(actorBeanName: String): Props {
        return Props.create(SpringActorProducer::class.java, applicationContext, actorBeanName)
    }

}