package online.dailyq.api

import okhttp3.Interceptor
import okhttp3.Response
import online.dailyq.AuthManager

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        AuthManager.accessToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
