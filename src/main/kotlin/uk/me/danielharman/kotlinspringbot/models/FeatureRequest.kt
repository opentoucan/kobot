package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.kohsuke.randname.RandomNameGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "featureRequests")
data class FeatureRequest(var requestText: String = "", var userId : String = "") {

    enum class Status {
        COMPLETED, REJECTED, PENDING
    }

    @Id
    lateinit var id: String
    var niceId : String = RandomNameGenerator().next()
    var created : DateTime = DateTime.now()
    var status: Status = Status.PENDING

}
