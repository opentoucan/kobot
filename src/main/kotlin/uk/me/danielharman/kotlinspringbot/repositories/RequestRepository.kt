package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.FeatureRequest

@Repository
interface RequestRepository : MongoRepository<FeatureRequest, String> {

    fun findByNiceId(niceId: String) : FeatureRequest?

}