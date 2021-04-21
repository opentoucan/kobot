package uk.me.danielharman.kotlinspringbot.helpers

import org.joda.time.DateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun DateTime.toJavaLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(
        this.year,
        this.monthOfYear,
        this.dayOfMonth,
        this.hourOfDay,
        this.minuteOfHour,
        this.secondOfMinute,
        0
    )
}

fun DateTime.toJavaZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.of(
        this.year,
        this.monthOfYear,
        this.dayOfMonth,
        this.hourOfDay,
        this.minuteOfHour,
        this.secondOfMinute,
        0,
        ZoneId.of(this.zone.id)
    )
}