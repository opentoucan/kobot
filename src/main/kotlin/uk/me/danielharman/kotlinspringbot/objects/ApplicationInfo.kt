package uk.me.danielharman.kotlinspringbot.objects

import java.time.LocalDateTime


object ApplicationInfo {

    var startTime: LocalDateTime = LocalDateTime.now()
    var version: String = "dev"
    var isDev: Boolean = false;

}