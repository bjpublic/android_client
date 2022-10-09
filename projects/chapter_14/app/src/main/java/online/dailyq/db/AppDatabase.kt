package online.dailyq.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import online.dailyq.db.dao.QuestionDao
import online.dailyq.db.dao.UserDao
import online.dailyq.db.entity.QuestionEntity
import online.dailyq.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        QuestionEntity::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getQuestionDao(): QuestionDao

    companion object {
        const val FILENAME = "dailyq.db"
        var INSTANCE: AppDatabase? = null

        private fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                FILENAME
            ).fallbackToDestructiveMigration()
                .build()
        }

        fun init(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: create(context).also {
                INSTANCE = it
            }
        }

        fun getInstance(): AppDatabase = INSTANCE!!
    }

}
