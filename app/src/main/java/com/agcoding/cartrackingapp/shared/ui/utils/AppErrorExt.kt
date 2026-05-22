package com.agcoding.cartrackingapp.shared.ui.utils

import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.shared.domain.error.AppError

val AppError.simpleMessage: String get() = when (this) {
    is AppError.NoInternetConnection -> "No internet connection"
    is AppError.RequestTimeout -> "Request timed out"
    is AppError.Unauthorized -> "Unauthorized"
    is AppError.SessionExpired -> "Session expired"
    is AppError.NotFound -> "Not found"
    is AppError.HttpError -> "Server error ($code)"
    is AppError.ServerError -> "Server error"
    is AppError.DatabaseError -> cause.message ?: "Database error"
    is AppError.ValidationError -> (message as? UiText.DynamicString)?.value ?: "Validation error"
    is AppError.Unknown -> cause.message ?: "An unexpected error occurred"
}

fun AppError.toUiText(): UiText = when (this) {
    is AppError.NoInternetConnection -> UiText.StringResource(R.string.error_no_internet)
    is AppError.RequestTimeout -> UiText.StringResource(R.string.error_timeout)
    is AppError.Unauthorized -> UiText.StringResource(R.string.error_unauthorized)
    is AppError.SessionExpired -> UiText.StringResource(R.string.error_unauthorized)
    is AppError.NotFound -> UiText.StringResource(R.string.error_not_found)
    is AppError.HttpError -> UiText.StringResource(R.string.error_http, arrayOf(code))
    is AppError.ServerError -> UiText.StringResource(R.string.error_server)
    is AppError.DatabaseError -> UiText.StringResource(R.string.error_database)
    is AppError.ValidationError -> message
    is AppError.Unknown -> UiText.StringResource(R.string.error_unknown)
}
