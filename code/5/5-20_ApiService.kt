package online.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import online.dailyq.api.adapter.LocalDateAdapter
import online.dailyq.api.converter.LocalDateConverterFactory
import online.dailyq.api.response.Question
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.LocalDate

interface ApiService {

    companion object {
        private var INSTANCE: ApiService? = null

        private fun okHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            return builder
                .addInterceptor(logging)
                .build()
        }

        fun create(context: Context): ApiService {
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
                .create()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(LocalDateConverterFactory())
                .baseUrl("http://10.0.2.2:5000")
                .client(okHttpClient())
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
