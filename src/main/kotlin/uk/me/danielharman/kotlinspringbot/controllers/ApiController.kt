package uk.me.danielharman.kotlinspringbot.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.me.danielharman.kotlinspringbot.services.RequestService

@RestController
class ApiController(private val requestService: RequestService) {


    @GetMapping("/api/requests", produces = ["application/json"])
    fun getRequests(): ResponseEntity<String> {
        return ok(ObjectMapper().writeValueAsString(requestService.getRequests()))
    }

}