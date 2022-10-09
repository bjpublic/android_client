package online.dailyq.api.response

import java.time.LocalDate
import java.util.*

data class Question(
    val id: LocalDate,
    val text: String,
    val answerCount: Int,
    val updatedAt: Date,
    val createdAt: Date
)
