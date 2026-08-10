package com.agcoding.cartrackingapp.presentation.editcar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsUiState
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsViewModel
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.editcar.components.BasicInfoSection
import com.agcoding.cartrackingapp.presentation.editcar.components.InsuranceSection
import com.agcoding.cartrackingapp.presentation.editcar.components.LegalComplianceSection
import com.agcoding.cartrackingapp.presentation.editcar.components.MaintenanceSection
import com.agcoding.cartrackingapp.presentation.editcar.components.TiresSection
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import com.agcoding.cartrackingapp.util.sanitizeIntInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    onNavigateBack: () -> Unit,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is CarDetailsUiState.Loading -> {
            // Show loading state
            Scaffold(
                topBar = {
                    StyledTopAppBar(
                        title = { Text(stringResource(R.string.edit_car_title)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            return
        }
        is CarDetailsUiState.Error -> {
            // Navigate back on error
            onNavigateBack()
            return
        }
        is CarDetailsUiState.Success -> {
            // Continue with normal flow
        }
    }

    val car = (uiState as CarDetailsUiState.Success).statistics.car

    var name by remember { mutableStateOf(car.name) }
    var licensePlate by remember { mutableStateOf(car.licensePlate) }
    var odometer by remember { mutableStateOf(car.currentOdometer.toInt().toString()) }

    // Insurance fields
    var insuranceExpirationDate by remember { mutableStateOf(car.insuranceExpirationDate) }

    // Legal & Compliance fields
    var kteoExpirationDate by remember { mutableStateOf(car.kteoExpirationDate) }
    var emissionsCardExpirationDate by remember { mutableStateOf(car.emissionsCardExpirationDate) }
    var roadTaxAmount by remember { mutableStateOf(car.roadTaxAmount?.toString()?.replace('.', ',') ?: "") }
    var roadTaxDueDate by remember { mutableStateOf(car.roadTaxDueDate) }

    // Maintenance fields
    var lastServiceDate by remember { mutableStateOf(car.lastServiceDate) }
    var lastTireChangeDate by remember { mutableStateOf(car.lastTireChangeDate) }

    // Tires fields
    var tireBrand by remember { mutableStateOf(car.tireBrand ?: "") }
    var tireDimensions by remember { mutableStateOf(car.tireDimensions ?: "") }
    var tireInstallationDate by remember { mutableStateOf(car.tireInstallationDate) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.edit_car_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() && licensePlate.isNotBlank() && odometer.isNotBlank()) {
                                viewModel.updateCar(
                                    name = name,
                                    licensePlate = licensePlate,
                                    odometer = odometer,
                                    insuranceExpirationDate = insuranceExpirationDate,
                                    kteoExpirationDate = kteoExpirationDate,
                                    emissionsCardExpirationDate = emissionsCardExpirationDate,
                                    roadTaxAmount = roadTaxAmount.parseLocalizedDouble(),
                                    roadTaxDueDate = roadTaxDueDate,
                                    lastServiceDate = lastServiceDate,
                                    lastTireChangeDate = lastTireChangeDate,
                                    tireBrand = tireBrand.takeIf { it.isNotBlank() },
                                    tireDimensions = tireDimensions.takeIf { it.isNotBlank() },
                                    tireInstallationDate = tireInstallationDate
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = name.isNotBlank() && licensePlate.isNotBlank() && odometer.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.save_label)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val isTablet = screenWidthDp >= 600

        // Use centered content with max width on tablets for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                        else Modifier.fillMaxWidth()
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = if (isTablet) 24.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BasicInfoSection(
                    name = name,
                    onNameChange = { name = it },
                    licensePlate = licensePlate,
                    onLicensePlateChange = { licensePlate = it },
                    odometer = odometer,
                    onOdometerChange = { odometer = sanitizeIntInput(it) },
                    isTablet = isTablet,
                    modifier = Modifier.fillMaxWidth()
                )

                InsuranceSection(
                    insuranceExpirationDate = insuranceExpirationDate,
                    onInsuranceExpirationDateChange = { insuranceExpirationDate = it },
                    modifier = Modifier.fillMaxWidth()
                )

                LegalComplianceSection(
                    kteoExpirationDate = kteoExpirationDate,
                    onKteoExpirationDateChange = { kteoExpirationDate = it },
                    emissionsCardExpirationDate = emissionsCardExpirationDate,
                    onEmissionsCardExpirationDateChange = { emissionsCardExpirationDate = it },
                    roadTaxAmount = roadTaxAmount,
                    onRoadTaxAmountChange = { roadTaxAmount = sanitizeDecimalInput(it) },
                    roadTaxDueDate = roadTaxDueDate,
                    onRoadTaxDueDateChange = { roadTaxDueDate = it },
                    isTablet = isTablet,
                    modifier = Modifier.fillMaxWidth()
                )

                MaintenanceSection(
                    lastServiceDate = lastServiceDate,
                    onLastServiceDateChange = { lastServiceDate = it },
                    lastTireChangeDate = lastTireChangeDate,
                    onLastTireChangeDateChange = { lastTireChangeDate = it },
                    isTablet = isTablet,
                    modifier = Modifier.fillMaxWidth()
                )

                TiresSection(
                    tireBrand = tireBrand,
                    onTireBrandChange = { tireBrand = it },
                    tireDimensions = tireDimensions,
                    onTireDimensionsChange = { tireDimensions = it },
                    tireInstallationDate = tireInstallationDate,
                    onTireInstallationDateChange = { tireInstallationDate = it },
                    isTablet = isTablet,
                    modifier = Modifier.fillMaxWidth()
                )


                // Add bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
