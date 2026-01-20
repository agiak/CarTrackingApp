package com.agcoding.cartrackingapp.presentation.navigation

import androidx.lifecycle.ViewModel
import com.agcoding.cartrackingapp.domain.usecase.expense.GetPendingReminderAlertsCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    getPendingReminderAlertsCountUseCase: GetPendingReminderAlertsCountUseCase
) : ViewModel() {

    val pendingAlertsCount: Flow<Int> = getPendingReminderAlertsCountUseCase()
}
