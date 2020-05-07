package uk.me.danielharman.kotlinspringbot.controllers

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import uk.me.danielharman.kotlinspringbot.actors.ActorProvider
import uk.me.danielharman.kotlinspringbot.conf.Providers

@Controller
class HtmlController {

    @Autowired
    private lateinit var actorProvider: ActorProvider

    @GetMapping("/")
    fun blog(model: Model): String {

        actorProvider.getActor("test")?.tell("test", ActorRef.noSender()) ?: throw Exception()

        model["title"] = "Blog"
        return "index.html"
    }

}