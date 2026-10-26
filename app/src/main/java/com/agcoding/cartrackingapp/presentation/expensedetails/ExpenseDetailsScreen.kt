package com.agcoding.cartrackingapp.presentation.expensedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.AppConfirmDialog
import com.agcoding.cartrackingapp.presentation.components.SuccessOverlay
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.presentation.expensedetails.components.ExpenseDetailsContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditExpense: () -> Unit,
    viewModel: ExpenseDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    var showDeleted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.expense_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditExpense) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ExpenseDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ExpenseDetailsUiState.Success -> {
                ExpenseDetailsContent(
                    expense = state.expense,
                    carName = state.carName,
                    carPlate = state.carPlate,
                    onEdit = onEditExpense,
                    onDelete = { viewModel.showDeleteDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ExpenseDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColorScheme.current.contentSecondary
                    )
                }
            }
        }
    }

    SuccessOverlay(
        visible = showDeleted,
        message = stringResource(R.string.expense_deleted),
        onFinished = onNavigateBack,
    )

    if (showDeleteDialog) {
        AppConfirmDialog(
            title = stringResource(R.string.delete_expense_title),
            message = stringResource(R.string.delete_expense_confirm),
            confirmLabel = stringResource(R.string.delete),
            dismissLabel = stringResource(R.string.cancel),
            icon = Icons.Default.Delete,
            destructive = true,
            // See RefillDetailsScreen: the delete used to wait on an undo
            // snackbar awaited in this screen's own scope, so leaving the
            // screen cancelled it and nothing was ever deleted.
            onConfirm = {
                viewModel.deleteExpense(
                    onSuccess = { showDeleted = true },
                    onError = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.expense_delete_failed)
                            )
                        }
                    },
                )
            },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseDetailsScreenPreview() {
    CarTrackingAppTheme {
        ExpenseDetailsScreen(
            onNavigateBack = {},
            onEditExpense = {}
        )
    }
}

