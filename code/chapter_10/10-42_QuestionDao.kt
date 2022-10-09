package online.dailyq.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import online.dailyq.db.entity.QuestionEntity
import java.time.LocalDate

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question WHERE id=:fromDate")
    suspend fun get(fromDate: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg questions: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(questions: List<QuestionEntity>)

    @Query("SELECT * FROM question ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, QuestionEntity>

    @Query("DELETE FROM question")
    suspend fun deleteAll()
}
