package com.agcoding.cartrackingapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Widget Configuration Activity
 * Allows user to select which car the widget should track
 */
@AndroidEntryPoint
class QuickAddWidgetConfigActivity : AppCompatActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED)

        // Get the widget ID from intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If invalid widget ID, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            CarTrackingAppTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onCarSelected = { carId ->
                        saveWidgetConfiguration(carId)
                        finishConfiguration()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }

        viewModel.loadCars()
    }

    private fun saveWidgetConfiguration(carId: Long) {
        android.util.Log.d("WidgetConfig", "=== Saving Widget Configuration ===")
        android.util.Log.d("WidgetConfig", "Widget ID: $appWidgetId")
        android.util.Log.d("WidgetConfig", "Selected Car ID: $carId")

        // Use application context to ensure consistent SharedPreferences access
        val prefs = applicationContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)

        // Use commit() instead of apply() to ensure synchronous save
        val success = prefs.edit()
            .putLong(getWidgetCarIdKey(appWidgetId), carId)
            .commit()

        android.util.Log.d("WidgetConfig", "Save commit result: $success")

        // Verify save immediately
        val savedCarId = prefs.getLong(getWidgetCarIdKey(appWidgetId), -1L)
        android.util.Log.d("WidgetConfig", "Verified saved car ID: $savedCarId")
        android.util.Log.d("WidgetConfig", "All prefs after save: ${prefs.all}")

        // Double-check by re-reading from a fresh instance
        val freshPrefs = applicationContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        val freshCarId = freshPrefs.getLong(getWidgetCarIdKey(appWidgetId), -1L)
        android.util.Log.d("WidgetConfig", "Fresh read car ID: $freshCarId")
    }

    private fun finishConfiguration() {
        android.util.Log.d("WidgetConfig", "=== Finishing Widget Configuration ===")
        android.util.Log.d("WidgetConfig", "App Widget ID: $appWidgetId")

        // Verify car selection was saved - use applicationContext for consistency
        val carPrefs = applicationContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        val savedCarId = carPrefs.getLong(getWidgetCarIdKey(appWidgetId), -1L)
        android.util.Log.d("WidgetConfig", "Saved car ID for this widget: $savedCarId")
        android.util.Log.d("WidgetConfig", "All car prefs: ${carPrefs.all}")

        if (savedCarId == -1L) {
            android.util.Log.e("WidgetConfig", "ERROR: Car ID was not saved properly!")
        }

        // Save mapping from Widget ID to Glance ID index in SharedPreferences
        val mappingPrefs = applicationContext.getSharedPreferences("widget_glance_mapping", Context.MODE_PRIVATE)

        // Update the widget with proper Glance state
        lifecycleScope.launch {
            val glanceAppWidgetManager = GlanceAppWidgetManager(this@QuickAddWidgetConfigActivity)
            val glanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)
            android.util.Log.d("WidgetConfig", "Available Glance IDs: ${glanceIds.size}")

            // Find the first available Glance ID that's not mapped to another active widget
            var targetGlanceId: androidx.glance.GlanceId? = null
            val allMappings = mappingPrefs.all
            val usedGlanceIndices = allMappings.entries
                .filter { it.key.startsWith("glance_index_") && it.key != "glance_index_$appWidgetId" }
                .map { it.value as? Int }
                .filterNotNull()
                .toSet()

            android.util.Log.d("WidgetConfig", "Used Glance indices by other widgets: $usedGlanceIndices")

            for ((index, glanceId) in glanceIds.withIndex()) {
                if (index !in usedGlanceIndices) {
                    targetGlanceId = glanceId
                    mappingPrefs.edit().putInt("glance_index_$appWidgetId", index).commit()
                    android.util.Log.d("WidgetConfig", "Mapped Widget ID $appWidgetId to Glance index $index")
                    break
                }
            }

            if (targetGlanceId == null && glanceIds.isNotEmpty()) {
                targetGlanceId = glanceIds.first()
                mappingPrefs.edit().putInt("glance_index_$appWidgetId", 0).commit()
                android.util.Log.d("WidgetConfig", "Fallback: Mapped Widget ID $appWidgetId to Glance index 0")
            }

            android.util.Log.d("WidgetConfig", "Selected Glance ID: $targetGlanceId")

            // Map this specific glanceId to the appWidgetId and store the car ID
            if (targetGlanceId != null) {
                updateAppWidgetState(this@QuickAddWidgetConfigActivity, targetGlanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(QuickAddWidget.WIDGET_ID_KEY, appWidgetId)
                        set(QuickAddWidget.CAR_ID_KEY, savedCarId)
                    }
                }
                android.util.Log.d("WidgetConfig", "✓ Set WIDGET_ID_KEY=$appWidgetId and CAR_ID_KEY=$savedCarId in Glance state")

                // Force update all widgets to refresh their data
                val allGlanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)
                for (glanceId in allGlanceIds) {
                    QuickAddWidget.update(this@QuickAddWidgetConfigActivity, glanceId)
                }
                android.util.Log.d("WidgetConfig", "✓ All widgets updated")
            } else {
                android.util.Log.e("WidgetConfig", "✗ No Glance ID available for mapping")
            }

            // Also trigger a broadcast update to ensure the widget refreshes
            QuickAddWidgetReceiver.requestUpdate(this@QuickAddWidgetConfigActivity)
            android.util.Log.d("WidgetConfig", "✓ Broadcast update requested")

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    companion object {
        const val WIDGET_PREFS_NAME = "quick_add_widget_prefs"

        fun getWidgetCarIdKey(widgetId: Int) = "widget_${widgetId}_car_id"

        fun getWidgetCarId(context: Context, widgetId: Int): Long {
            android.util.Log.d("WidgetConfig", "getWidgetCarId called for widget $widgetId")
            // Always use applicationContext to ensure we read from the same file
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
            val carId = prefs.getLong(getWidgetCarIdKey(widgetId), -1L)
            android.util.Log.d("WidgetConfig", "Retrieved car ID: $carId for widget $widgetId")
            android.util.Log.d("WidgetConfig", "Preference key used: ${getWidgetCarIdKey(widgetId)}")
            android.util.Log.d("WidgetConfig", "All saved preferences: ${prefs.all}")
            return carId
        }

        fun removeWidgetConfig(context: Context, widgetId: Int) {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(getWidgetCarIdKey(widgetId))
            }
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    viewModel: WidgetConfigViewModel,
    onCarSelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val cars by viewModel.cars.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text("Select Car for Widget") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cars.isEmpty() -> {
                    // No cars available
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Cars Available",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add a car in the app first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                }
                else -> {
                    // Show car list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Select which car this widget will track:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(cars) { car ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCarSelected(car.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = car.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _cars = MutableStateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>>(emptyList())
    val cars: StateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>> = _cars.asStateFlow()

    fun loadCars() {
        viewModelScope.launch {
            carRepository.getAllCars().collect { carList ->
                _cars.value = carList
            }
        }
    }
}

