package com.agcoding.cartrackingapp.presentation.expensecategories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.expensecategories.components.AddCategoryDialog
import com.agcoding.cartrackingapp.presentation.expensecategories.components.DeleteCategoryDialog
import com.agcoding.cartrackingapp.presentation.expensecategories.components.ManageExpenseCategoriesContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExpenseCategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageExpenseCategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.manage_expense_categories)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_category)
                )
            }
        }
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
        val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
        val useCenteredLayout = isTablet || isLandscape

        // Use centered content with max width on tablets and landscape
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (useCenteredLayout) Alignment.TopCenter else Alignment.TopStart
        ) {
            when (val state = uiState) {
            is ManageExpenseCategoriesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .then(
                            if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                            else Modifier.fillMaxWidth()
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ManageExpenseCategoriesUiState.Success -> {
                ManageExpenseCategoriesContent(
                    predefinedCategories = state.predefinedCategories,
                    customCategories = state.customCategories,
                    onDeleteCategory = { category -> showDeleteDialog = category },
                    onToggleQuickPick = { name, current -> viewModel.toggleQuickPick(name, current) },
                    isTablet = useCenteredLayout,
                    modifier = Modifier
                        .then(
                            if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                            else Modifier.fillMaxWidth()
                        )
                        .fillMaxSize()
                )
            }

            is ManageExpenseCategoriesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .then(
                            if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                            else Modifier.fillMaxWidth()
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
    // Add category dialog
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryName ->
                viewModel.addCategory(categoryName)
                showAddDialog = false
            }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { categoryName ->
        DeleteCategoryDialog(
            categoryName = categoryName,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteCategory(categoryName)
                showDeleteDialog = null
            }
        )
    }
}
