package com.agcoding.cartrackingapp.presentation.carlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo
import com.agcoding.cartrackingapp.presentation.carlist.components.AddCarDialog
import com.agcoding.cartrackingapp.presentation.carlist.components.CarCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    onCarClick: (Long) -> Unit,
    onStatisticsClick: () -> Unit,
    onAddRefillClick: (Long) -> Unit,
    onAddServiceClick: (Long) -> Unit,
    onNavigateToReminders: () -> Unit,
    viewModel: CarListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddCarDialog by viewModel.showAddCarDialog.collectAsState()
    val todayRemindersInfo by viewModel.todayRemindersInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.car_list_title)) }
            )
        },
        floatingActionButton = {
            // Only show FAB when there are cars
            if (uiState is CarListUiState.Success) {
                FloatingActionButton(
                    onClick = { viewModel.showAddCarDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.car_list_add_car_cd)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CarListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CarListUiState.Empty -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onAddCarClick = { viewModel.showAddCarDialog() }
                    )
                }

                is CarListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 88.dp // Extra padding for FAB (56dp FAB + 32dp spacing)
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reminder Banner
                        todayRemindersInfo?.let { reminderInfo ->
                            item {
                                ReminderBanner(
                                    reminderInfo = reminderInfo,
                                    onClick = onNavigateToReminders,
                                    onDismiss = { viewModel.dismissBannerForToday() }
                                )
                            }
                        }

                        items(
                            items = state.cars,
                            key = { it.id }
                        ) { car ->
                            CarCard(
                                car = car,
                                onClick = { onCarClick(car.id) },
                                onAddRefillClick = { onAddRefillClick(car.id) },
                                onAddServiceClick = { onAddServiceClick(car.id) }
                            )
                        }
                    }
                }

                is CarListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (showAddCarDialog) {
            AddCarDialog(
                onDismiss = { viewModel.hideAddCarDialog() },
                onConfirm = { name, plate, odometer ->
                    viewModel.addCar(name, plate, odometer)
                }
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onAddCarClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large icon for visual appeal
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.car_list_no_cars_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.car_list_no_cars_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddCarClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.car_list_add_car))
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReminderBanner(
    reminderInfo: ReminderInfo,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create a descriptive message based on reminder types
    val message = when {
        reminderInfo.dateBasedCount > 0 && reminderInfo.mileageBasedCount > 0 -> {
            // Both types
            stringResource(
                R.string.reminder_banner_mixed,
                reminderInfo.dateBasedCount,
                reminderInfo.mileageBasedCount
            )
        }
        reminderInfo.dateBasedCount > 0 -> {
            // Only date-based
            if (reminderInfo.dateBasedCount == 1) {
                stringResource(R.string.reminder_banner_date_single)
            } else {
                stringResource(R.string.reminder_banner_date_multiple, reminderInfo.dateBasedCount)
            }
        }
        reminderInfo.mileageBasedCount > 0 -> {
            // Only mileage-based
            if (reminderInfo.mileageBasedCount == 1) {
                stringResource(R.string.reminder_banner_mileage_single)
            } else {
                stringResource(R.string.reminder_banner_mileage_multiple, reminderInfo.mileageBasedCount)
            }
        }
        else -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.reminder_banner_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.view_reminders),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

