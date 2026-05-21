package com.agcoding.cartrackingapp.shared.domain.error

import com.agcoding.cartrackingapp.shared.ui.utils.UiText

sealed class AppError {
    data object NoInternetConnection : AppError()
    data object RequestTimeout : AppError()
    data class HttpError(val code: Int) : AppError()
    data object ServerError : AppError()
    data object Unauthorized : AppError()
    data object SessionExpired : AppError()
    data object NotFound : AppError()
    data class DatabaseError(val cause: Throwable) : AppError()
    data class ValidationError(val field: String, val message: UiText) : AppError()
    data class Unknown(val cause: Throwable) : AppError()
}
