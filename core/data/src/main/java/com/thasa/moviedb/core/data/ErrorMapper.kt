package com.thasa.moviedb.core.data

import com.thasa.moviedb.core.domain.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is UnknownHostException -> AppError.NoNetwork
    is SocketTimeoutException -> AppError.Timeout
    is HttpException -> when (val code = code()) {
        401 -> AppError.Unauthorized
        404 -> AppError.NotFound
        in 500..599 -> AppError.Server(code)
        else -> AppError.Unknown(this)
    }
    is IOException -> AppError.NoNetwork
    else -> AppError.Unknown(this)
}
