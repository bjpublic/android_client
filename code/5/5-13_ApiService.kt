package online.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import online.dailyq.api.converter.LocalDateConverterFactory
import online.dailyq.api.response.Question
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.LocalDate

interface ApiService {

    companion object {
        private var INSTANCE: ApiService? = null

        fun create(context: Context): ApiService {
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(LocalDateConverterFactory())
                .baseUrl("http://10.0.2.2:5000")
                .build()
                .create(ApiService::class.java)
        }

        fun init(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: create(context).also {
                INSTANCE = it
            }
        }

        fun getInstance(): ApiService = INSTANCE!!
    }

    @GET("/v1/questions/{qid}")
    suspend fun getQuestion(
        @Path("qid") qid: LocalDate
    ): Question
}
