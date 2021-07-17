package com.kdaydin.photofilter.module

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kdaydin.photofilter.application.ApplicationConstants
import com.kdaydin.photofilter.data.remote.HttpStatusCodeInterceptor
import com.kdaydin.photofilter.data.remote.LyrebirdApi
import com.kdaydin.photofilter.data.repository.LyrebirdRepository
import com.kdaydin.photofilter.data.repository.LyrebirdRepositoryImpl
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        createWebService<LyrebirdApi>(
            okHttpClient = createHttpClient(androidContext()),
            factory = RxJava2CallAdapterFactory.create(),
            baseUrl = ApplicationConstants.BASE_URL
        )
    }
    factory<LyrebirdRepository> { LyrebirdRepositoryImpl(lyrebirdApi = get()) }
}

/* Returns a custom OkHttpClient instance with interceptor. Used for building Retrofit service */
fun createHttpClient(context: Context): OkHttpClient {
    val cacheSize = (5 * 1024 * 1024).toLong()
    val myCache = Cache(context.cacheDir, cacheSize)
    val client = OkHttpClient.Builder()
    client.cache(myCache)
    client.readTimeout(5 * 60, TimeUnit.SECONDS)
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    client.addInterceptor {
        var original = it.request()
        val requestBuilder = original.newBuilder()
        if (hasNetwork(context) == true) {
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Cache-Control", "public, max-age=5")

        } else {

            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header(
                "Cache-Control",
                "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}"
            )
        }
        val request = requestBuilder.method(original.method, original.body).build()
        return@addInterceptor it.proceed(request)
    }
    client.addInterceptor(HttpStatusCodeInterceptor())
    client.addInterceptor(logging)
    return client.build()
}

fun hasNetwork(context: Context): Boolean? {
    var isConnected: Boolean? = false // Initial Value
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        isConnected = true
    return isConnected
}

/* function to build our Retrofit service */
inline fun <reified T> createWebService(
    okHttpClient: OkHttpClient,
    factory: CallAdapter.Factory,
    baseUrl: String
): T {
    val retrofit = Retrofit.Builder().baseUrl(baseUrl)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().serializeNulls()
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
            )
        )
        .addCallAdapterFactory(CoroutineCallAdapterFactory()).addCallAdapterFactory(factory)
        .client(okHttpClient).build()
    return retrofit.create(T::class.java)
}