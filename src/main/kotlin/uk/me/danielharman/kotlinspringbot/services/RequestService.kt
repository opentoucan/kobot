package uk.me.danielharman.kotlinspringbot.services

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.FeatureRequest
import uk.me.danielharman.kotlinspringbot.repositories.RequestRepository

@Service
class RequestService(private val requestRepository: RequestRepository) {

    fun getRequests(): List<FeatureRequest> = requestRepository.findAll()

    fun findById(id: String) = requestRepository.findByNiceId(id)

    fun createRequest(request: String) : FeatureRequest = requestRepository.save(FeatureRequest(request))

}