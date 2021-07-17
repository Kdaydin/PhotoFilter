package com.kdaydin.photofilter.data.remote

sealed class UseCaseResult<out T> {

    class Success<T>(val data: T) : UseCaseResult<T>()

    data class ServiceError(
        val statusCode: Int? = null,
        val errorCode: String? = null,
        val errorMsg: String? = null
    ) : UseCaseResult<Nothing>()

    data class GenericError(val code: Int? = null, val error: String? = null) :
        UseCaseResult<Nothing>()

    data class NetworkError(
        val type: String,
        val message: String,
        val action: (() -> Unit)? = null
    ) : UseCaseResult<Nothing>()

    class Error(ex: Throwable) : UseCaseResult<Nothing>()
}
