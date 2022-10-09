package online.dailyq.api.response

import java.time.LocalDate
import java.util.*

data class Answer(
    val qid: LocalDate,
    val uid: String,
    val text: String?,
    val photo: String?,
    val updatedAt: Date,
    val createdAt: Date,

    val answerer: User?
)
