package online.dailyq.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import online.dailyq.db.dao.UserDao
import online.dailyq.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
}
