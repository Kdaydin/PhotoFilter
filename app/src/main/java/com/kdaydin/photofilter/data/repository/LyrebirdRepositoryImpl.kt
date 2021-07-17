package com.kdaydin.photofilter.data.repository

import android.os.Handler
import android.os.Looper
import com.kdaydin.photofilter.data.entities.Overlay
import com.kdaydin.photofilter.data.remote.HttpStatusCodeException
import com.kdaydin.photofilter.data.remote.LyrebirdApi
import com.kdaydin.photofilter.data.remote.UseCaseResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class LyrebirdRepositoryImpl(
    private val lyrebirdApi: LyrebirdApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    LyrebirdRepository {

    override suspend fun getOverlays(): UseCaseResult<List<Overlay>> {
        return safeApiCall(dispatcher, { lyrebirdApi.getOverlays().await() })
    }

    //BASE CALL FOR HANDLING ERRORS FROM SINGLE POINT
    private suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T,
        actionOnServiceError: (() -> Unit)? = null,
        isAsync: Boolean? = false,
        isUnnecessaryTimeoutControl: Boolean = false
    ): UseCaseResult<T> {
        return withContext(dispatcher) {

            try {
                val result = apiCall.invoke()

                UseCaseResult.Success(result)
            } catch (e: IOException) {

                if (isAsync == true) {

                    UseCaseResult.GenericError(null, null)
                } else {
                    when (e.cause) {
                        is HttpStatusCodeException -> {

                            when ((e.cause as HttpStatusCodeException).statusCode) {
                                400 -> {
                                    UseCaseResult.NetworkError(
                                        e.cause?.message ?: "",
                                        e.cause?.cause?.message ?: "",
                                        actionOnServiceError
                                    )
                                }

                                500 -> {
                                    UseCaseResult.GenericError(null, null)
                                }

                                else -> {
                                    UseCaseResult.NetworkError(
                                        e.cause?.message ?: "",
                                        e.cause?.cause?.message ?: "",
                                        actionOnServiceError
                                    )
                                }
                            }

                        }

                        else -> {
                            UseCaseResult.GenericError(null, null)
                        }
                    }
                }
            }
        }
    }


    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Handler(Looper.getMainLooper()).looper.thread)
            runnable.run()
        else
            Handler(Looper.getMainLooper()).post(runnable)
    }
}