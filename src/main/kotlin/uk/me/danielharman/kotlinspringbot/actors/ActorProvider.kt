package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.conf.Providers
import java.lang.Exception

@Component
/**
 * Since we can't bindActor() like in Play, use a singleton to keep track of singleton actors
 */
class ActorProvider(var actorSystem: ActorSystem){

    private var actors: HashMap<String, ActorRef> = hashMapOf()

    fun getActor(name: String): ActorRef = actors.getOrDefault(name, null) ?: throw Exception()

    fun createActor(beanName: String, name: String) : ActorRef? {

        return if (actors.containsKey(name)){
            logger.warn("Actor of name $name already exists.")
            actors[name]
        }
        else
        {
            logger.info("Creating actor of bean name $beanName named $name")
            val actorRef = actorSystem.actorOf(Providers.SPRING_EXTENSION_PROVIDER.get(actorSystem).props(beanName), name)
            actors[name] = actorRef
            actorRef
        }
    }

}