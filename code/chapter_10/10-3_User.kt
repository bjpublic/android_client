package online.dailyq.api.response

import java.util.*

data class User(
    val id: String,
    val name: String,
    val description: String?,
    val photo: String?,
    val answerCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean?,
    val updatedAt: Date
)
