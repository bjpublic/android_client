package online.dailyq.ui.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import online.dailyq.api.ApiService
import online.dailyq.api.response.QuestionAndAnswer
import java.time.LocalDate

class UserAnswerPagingSource(val api: ApiService, val uid: String) :
    PagingSource<LocalDate, QuestionAndAnswer>() {

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, QuestionAndAnswer> {
        val userAnswersResponse = api.getUserAnswers(uid, params.key)

        return if (userAnswersResponse.isSuccessful) {
            val userAnswers = userAnswersResponse.body()!!

            val nextKey = if (userAnswers.isNotEmpty()) {
                userAnswers.minOf { it.question.id }
            } else {
                null
            }

            LoadResult.Page(
                data = userAnswers,
                prevKey = null,
                nextKey = nextKey
            )
        } else {
            LoadResult.Error(Throwable("Paging Error"))
        }
    }

    override fun getRefreshKey(state: PagingState<LocalDate, QuestionAndAnswer>): LocalDate? =
        null
}
