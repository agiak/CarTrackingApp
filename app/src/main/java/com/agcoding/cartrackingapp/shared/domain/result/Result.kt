package com.agcoding.cartrackingapp.shared.domain.result

import com.agcoding.cartrackingapp.shared.domain.error.AppError

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
}
