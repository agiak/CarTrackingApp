package com.agcoding.cartrackingapp.presentation.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.TrashItem
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var itemToDelete by remember { mutableStateOf<TrashItem?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.trash_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.trash_empty), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.trash_empty_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.hashCode() }) { item ->
                    TrashItemCard(
                        item = item,
                        dateFormat = dateFormat,
                        onRestore = { viewModel.restore(item) },
                        onDeletePermanently = { itemToDelete = item }
                    )
                }
            }
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.trash_delete_permanently)) },
            text = { Text(stringResource(R.string.trash_permanent_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.permanentlyDelete(item)
                    itemToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun TrashItemCard(
    item: TrashItem,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val (title, subtitle, sectionLabel) = when (item) {
        is TrashItem.CarItem -> Triple(item.car.name, item.car.licensePlate, stringResource(R.string.trash_cars_section))
        is TrashItem.RefillItem -> Triple(
            "€%.2f · %.1fL".format(item.refill.amountPaid, item.refill.litersAdded),
            item.carName,
            stringResource(R.string.trash_refills_section)
        )
        is TrashItem.ExpenseItem -> Triple(
            "${item.expense.category} · €%.2f".format(item.expense.amount),
            item.carName,
            stringResource(R.string.trash_expenses_section)
        )
        is TrashItem.TripItem -> Triple(item.trip.name, item.carName, stringResource(R.string.trash_trips_section))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sectionLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.trash_deleted_on, dateFormat.format(Date(item.deletedAt))),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDeletePermanently, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.trash_delete_permanently), fontSize = 12.sp)
                }
                Button(onClick = onRestore, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.trash_restore))
                }
            }
        }
    }
}
