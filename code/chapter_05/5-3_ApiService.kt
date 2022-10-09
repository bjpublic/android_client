package online.dailyq.api

import online.dailyq.api.response.Question
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("/v1/questions/{qid}")
    suspend fun getQuestion(@Path("qid") qid: String): Question

}
