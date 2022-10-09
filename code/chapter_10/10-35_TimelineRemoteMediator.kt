package online.dailyq.ui.timeline

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import online.dailyq.api.ApiService
import online.dailyq.db.AppDatabase
import online.dailyq.db.entity.QuestionEntity
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate

@OptIn(ExperimentalPagingApi::class)
class TimelineRemoteMediator(val api: ApiService, val db: AppDatabase) :
    RemoteMediator<Int, QuestionEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, QuestionEntity>
    ): MediatorResult {
        val pageSize = state.config.pageSize
        val today = LocalDate.now()

        val fromDate = when (loadType) {
            LoadType.REFRESH -> {
                today
            }
            LoadType.PREPEND -> {
                val firstItem = state.firstItemOrNull()

                if (firstItem == null) {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }

                if (firstItem.id >= today) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    firstItem.id.plusDays(pageSize.toLong())
                    val prevKey = firstItem.id
                    if (prevKey > today) {
                        today
                    } else {
                        prevKey
                    }
                }
            }
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null) {
                    today
                } else {
                    lastItem.id.minusDays(1)
                }
            }
        }

        try {
            val questions = api.getQuestions(fromDate, pageSize).body()
            val endOfPaginationReached = questions.isNullOrEmpty()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.getQuestionDao().deleteAll()
                }

                questions?.map {
                    QuestionEntity(it.id, it.text, it.answerCount, it.updatedAt, it.createdAt)
                }?.let {
                    db.getQuestionDao().insertOrReplace(it)
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: SocketTimeoutException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }
}

