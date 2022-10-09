package online.dailyq.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    var id: String,
    var name: String?,
    var description: String?,
    var photo: String?,
    var answerCount: Int,
    var followerCount: Int,
    var followingCount: Int,
    var isFollowing: Boolean,
    var updatedAt: Date?
)
