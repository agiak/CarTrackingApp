package com.agcoding.cartrackingapp.presentation.refillhistory

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.agcoding.cartrackingapp.presentation.components.ActiveFilter
import com.agcoding.cartrackingapp.presentation.components.ActiveFiltersRow
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.refillhistory.components.RefillHistoryContent

enum class RefillSortOption(@StringRes val labelRes: Int) {
    MOST_RECENT(R.string.most_recent),
    OLDEST(R.string.oldest),
    MOST_EXPENSIVE(R.string.most_expensive),
    LEAST_EXPENSIVE(R.string.least_expensive),
    BEST_CONSUMPTION(R.string.best_consumption),
    WORST_CONSUMPTION(R.string.worst_consumption)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillHistoryScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit,
    viewModel: RefillHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    // Check if sort is non-default
    val hasNonDefaultSort = selectedSort != RefillSortOption.MOST_RECENT

    // Create active sort chip
    val getActiveSortChip: @Composable () -> List<ActiveFilter> = {
        if (hasNonDefaultSort) {
            listOf(
                ActiveFilter(
                    id = "sort",
                    label = stringResource(selectedSort.labelRes),
                    onRemove = { viewModel.setSortOption(RefillSortOption.MOST_RECENT) }
                )
            )
        } else {
            emptyList()
        }
    }

    val activeSortChips = getActiveSortChip()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.refill_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Box {
                        // Sort button with badge indicator
                        BadgedBox(
                            badge = {
                                if (hasNonDefaultSort) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        ) {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.sort),
                                    tint = if (hasNonDefaultSort) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            RefillSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(option.labelRes),
                                            fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSort == option)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RefillHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RefillHistoryUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Active sort chip
                    ActiveFiltersRow(
                        activeFilters = activeSortChips,
                        onClearAll = null // No clear all needed for single chip
                    )

                    // Refills list
                    RefillHistoryContent(
                        carName = state.carName,
                        refills = state.refills,
                        selectedSort = selectedSort,
                        onRefillClick = onRefillClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is RefillHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }

            is RefillHistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_refills_yet),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.add_first_refill),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

