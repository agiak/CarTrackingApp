package com.agcoding.cartrackingapp.presentation.cardetails

import com.agcoding.cartrackingapp.domain.model.CarStatistics

sealed class CarDetailsUiState {
    object Loading : CarDetailsUiState()
    data class Success(val statistics: CarStatistics) : CarDetailsUiState()
    data class Error(val message: String) : CarDetailsUiState()
}