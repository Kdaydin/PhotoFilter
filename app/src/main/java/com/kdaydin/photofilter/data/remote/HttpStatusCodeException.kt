package com.kdaydin.photofilter.data.remote


class HttpStatusCodeException(val statusCode: Int?, message: String?, cause: Throwable? = null) :
    Exception(message, cause)