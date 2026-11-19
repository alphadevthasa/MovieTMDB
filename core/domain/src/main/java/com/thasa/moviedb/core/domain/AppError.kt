package com.thasa.moviedb.core.domain

sealed class AppError(cause: Throwable? = null) : Throwable(cause) {
    data object NoNetwork : AppError()
    data object Timeout : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data class Server(val code: Int) : AppError()
    data class Unknown(override val cause: Throwable?) : AppError(cause)
}
