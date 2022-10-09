package online.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import online.dailyq.AuthManager
import online.dailyq.api.adapter.LocalDateAdapter
import online.dailyq.api.converter.LocalDateConverterFactory
import online.dailyq.api.response.Answer
import online.dailyq.api.response.AuthToken
import online.dailyq.api.response.Image
import online.dailyq.api.response.Question
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.LocalDate
import java.util.concurrent.TimeUnit

interface ApiService {

    companion object {
        private var INSTANCE: ApiService? = null

        private fun okHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            
            return builder
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor())
                .authenticator(TokenRefreshAuthenticator())
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

    @FormUrlEncoded
    @POST("/v2/token")
    suspend fun login(
        @Field("username") uid: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
        @Tag authType: AuthType = AuthType.NO_AUTH
    ): Response<AuthToken>

    @FormUrlEncoded
    @POST("/v2/token")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Tag authType: AuthType = AuthType.NO_AUTH
    ): Call<AuthToken>

    @GET("/v2/questions/{qid}")
    suspend fun getQuestion(
        @Path("qid") qid: LocalDate
    ): Response<Question>

    @GET("/v2/questions/{qid}/answers/{uid}")
    suspend fun getAnswer(
        @Path("qid") qid: LocalDate,
        @Path("uid") uid: String? = AuthManager.uid
    ): Response<Answer>

    @FormUrlEncoded
    @POST("/v2/questions/{qid}/answers")
    suspend fun writeAnswer(
        @Path("qid") qid: LocalDate,
        @Field("text") text: String? = null,
        @Field("photo") photo: String? = null
    ): Response<Answer>

    @FormUrlEncoded
    @PUT("/v2/questions/{qid}/answers/{uid}")
    suspend fun editAnswer(
        @Path("qid") qid: LocalDate,
        @Field("text") text: String? = null,
        @Field("photo") photo: String? = null,
        @Path("uid") uid: String? = AuthManager.uid
    ): Response<Answer>

    @DELETE("/v2/questions/{qid}/answers/{uid}")
    suspend fun deleteAnswer(
        @Path("qid") qid: LocalDate,
        @Path("uid") uid: String? = AuthManager.uid
    ): Response<Unit>

    @Multipart
    @POST("/v2/images")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
    ): Response<Image>

}
