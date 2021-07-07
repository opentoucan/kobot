package uk.me.danielharman.kotlinspringbot.models.audit

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(value = "Audit")
class AuditItem(val discordId: String, val administratorId: String, val action: String) {

    @Id
    lateinit var id: String

    var date: DateTime = DateTime.now()

}