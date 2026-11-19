package com.thasa.moviedb.core.ui

import com.thasa.moviedb.core.domain.AppError

fun AppError.toMessage(): String = when (this) {
    AppError.NoNetwork -> "No internet connection. Check your network and try again."
    AppError.Timeout -> "The request timed out. Please try again."
    AppError.Unauthorized -> "Access denied. The API token is missing or invalid."
    AppError.NotFound -> "We couldn't find what you were looking for."
    is AppError.Server -> "The server had a problem (HTTP $code). Please try again later."
    is AppError.Unknown -> "Something went wrong. Please try again."
}
