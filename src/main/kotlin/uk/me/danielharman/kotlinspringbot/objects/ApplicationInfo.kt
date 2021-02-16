package uk.me.danielharman.kotlinspringbot.objects

import org.joda.time.DateTime

object ApplicationInfo {

    var startTime: DateTime = DateTime.now()
    var version: String = "dev"

}