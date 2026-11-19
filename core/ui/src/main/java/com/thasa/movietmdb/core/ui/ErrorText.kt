package com.thasa.movietmdb.core.ui

import com.thasa.movietmdb.core.domain.AppError

fun AppError.toMessage(): String = when (this) {
    AppError.NoNetwork -> "No internet connection. Check your network and try again."
    AppError.Timeout -> "The request timed out. Please try again."
    AppError.Unauthorized -> "Access denied. The API token is missing or invalid."
    AppError.NotFound -> "We couldn't find what you were looking for."
    is AppError.Server -> "The server had a problem (HTTP $code). Please try again later."
    is AppError.Unknown -> "Something went wrong. Please try again."
}

/** Paging surfaces plain Throwables; the repository layer already maps them to [AppError]. */
fun Throwable.toUiError(): AppError = this as? AppError ?: AppError.Unknown(this)
