package com.agcoding.cartrackingapp.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.TrashItem
import com.agcoding.cartrackingapp.domain.usecase.trash.GetTrashItemsUseCase
import com.agcoding.cartrackingapp.domain.usecase.trash.PermanentlyDeleteTrashItemUseCase
import com.agcoding.cartrackingapp.domain.usecase.trash.RestoreTrashItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getTrashItemsUseCase: GetTrashItemsUseCase,
    private val restoreTrashItemUseCase: RestoreTrashItemUseCase,
    private val permanentlyDeleteTrashItemUseCase: PermanentlyDeleteTrashItemUseCase,
) : ViewModel() {

    private val _items = MutableStateFlow<List<TrashItem>>(emptyList())
    val items: StateFlow<List<TrashItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadTrash() }

    fun loadTrash() {
        viewModelScope.launch {
            _isLoading.value = true
            _items.value = getTrashItemsUseCase()
            _isLoading.value = false
        }
    }

    fun restore(item: TrashItem, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (item) {
                is TrashItem.CarItem -> restoreTrashItemUseCase.restoreCar(item.car.id)
                is TrashItem.RefillItem -> restoreTrashItemUseCase.restoreRefill(item.refill.id)
                is TrashItem.ExpenseItem -> restoreTrashItemUseCase.restoreExpense(item.expense.id)
                is TrashItem.TripItem -> restoreTrashItemUseCase.restoreTrip(item.trip.id)
            }
            loadTrash()
            onDone()
        }
    }

    fun permanentlyDelete(item: TrashItem) {
        viewModelScope.launch {
            when (item) {
                is TrashItem.CarItem -> permanentlyDeleteTrashItemUseCase.deleteCar(item.car.id)
                is TrashItem.RefillItem -> permanentlyDeleteTrashItemUseCase.deleteRefill(item.refill.id)
                is TrashItem.ExpenseItem -> permanentlyDeleteTrashItemUseCase.deleteExpense(item.expense.id)
                is TrashItem.TripItem -> permanentlyDeleteTrashItemUseCase.deleteTrip(item.trip.id)
            }
            loadTrash()
        }
    }
}
