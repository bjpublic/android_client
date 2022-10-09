package online.dailyq.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class EndpointLoggingInterceptor(val name: String, val urlSuffix:String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.method != "GET" || !request.url.encodedPath.endsWith(urlSuffix)) {
            return chain.proceed((request))
        }

        Log.i("DailyQ_$urlSuffix",
            """--> $name
            |${request.url}
            |${request.headers}""".trimMargin())

        val response = chain.proceed(request)

        Log.i("DailyQ_$urlSuffix",
            """<-- $name
                |Response code: ${response.code} (Network: ${response.networkResponse?.code}, Cache: ${response.cacheResponse?.code})
                |${response.headers}""".trimMargin())

        return response
    }
}
