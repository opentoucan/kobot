package uk.me.danielharman.kotlinspringbot.models

import kotlinx.serialization.Serializable

@Serializable
data class XkcdComic(val month: Int,
                     val num: Int,
                     val link: String,
                     val year: String,
                     val news: String,
                     val safe_title: String,
                     val transcript: String,
                     val alt: String,
                     val img: String,
                     val title: String,
                     val day: Int)