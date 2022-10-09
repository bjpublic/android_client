package online.dailyq.api.response

import java.util.*

data class Answer(
    val qid: String,
    val uid: String,
    val text: String?,
    val photo: String?,
    val updatedAt: Date,
    val createdAt: Date,
)
