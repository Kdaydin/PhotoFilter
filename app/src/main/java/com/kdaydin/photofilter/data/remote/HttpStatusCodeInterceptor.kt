package com.kdaydin.photofilter.data.remote

import androidx.annotation.NonNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpStatusCodeInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(@NonNull chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val newResponse: Response.Builder = response.newBuilder()
        if (response.isSuccessful.not()) {
            when (response.code) {
                in 400..499 -> {
                    throw IOException(
                        HttpStatusCodeException(
                            response.code,
                            response.message,
                            Throwable(response.message)
                        )
                    )
                }

                in 500..599 -> {
                    throw IOException(
                        HttpStatusCodeException(
                            response.code,
                            response.message,
                            Throwable(response.message)
                        )
                    )
                }
            }
            return newResponse.build()
        }
        return response
    }

}