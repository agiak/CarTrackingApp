package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.agcoding.cartrackingapp.R

/**
 * Quick Add Widget - Glance implementation
 * Provides quick access to add refills and expenses
 */
object QuickAddWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    // Widget state keys
    val WIDGET_ID_KEY = intPreferencesKey("widget_id")
    val CAR_ID_KEY = androidx.datastore.preferences.core.longPreferencesKey("car_id")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("QuickAddWidget", "=== provideGlance called ===")
        Log.d("QuickAddWidget", "Context: $context")
        Log.d("QuickAddWidget", "Glance ID: $id")

        try {
            // First, check if we have cached transaction data in widget state
            val currentState = androidx.glance.appwidget.state.getAppWidgetState(
                context,
                PreferencesGlanceStateDefinition,
                id
            )

            val cachedType = currentState[androidx.datastore.preferences.core.stringPreferencesKey("cached_transaction_type")]
            val cachedAmount = currentState[androidx.datastore.preferences.core.doublePreferencesKey("cached_transaction_amount")]
            val cachedTimestamp = currentState[androidx.datastore.preferences.core.longPreferencesKey("cached_transaction_timestamp")]
            val cachedCarName = currentState[androidx.datastore.preferences.core.stringPreferencesKey("cached_transaction_car_name")]
            val cachedCarId = currentState[androidx.datastore.preferences.core.longPreferencesKey("cached_transaction_car_id")]

            val lastTransaction = if (cachedType != null && cachedAmount != null && cachedTimestamp != null && cachedCarName != null && cachedCarId != null) {
                // Use cached transaction data (instant update!)
                Log.d("QuickAddWidget", "Using cached transaction data")
                LastTransaction(
                    type = cachedType,
                    amount = cachedAmount,
                    timestamp = cachedTimestamp,
                    carName = cachedCarName,
                    carId = cachedCarId
                )
            } else {
                // Fetch from database
                Log.d("QuickAddWidget", "Fetching transaction from database")
                WidgetDataProvider.getLastTransaction(context)
            }

            // Fetch data BEFORE provideContent for proper Glance behavior
            val hasCars = WidgetDataProvider.hasAnyCars(context)

            Log.d("QuickAddWidget", "Has cars: $hasCars")
            Log.d("QuickAddWidget", "Last transaction: $lastTransaction")

            provideContent {
                GlanceTheme {
                    WidgetContent(context, hasCars, lastTransaction)
                }
            }
        } catch (e: Exception) {
            // Handle composition cancellation gracefully
            if (e.message?.contains("composition") == true ||
                e.message?.contains("coroutine scope") == true ||
                e is kotlinx.coroutines.CancellationException) {
                Log.d("QuickAddWidget", "Composition cancelled - widget will update on next refresh")
                return
            }

            Log.e("QuickAddWidget", "✗ Error providing content: ${e.message}", e)
            // Provide fallback content in case of error
            try {
                provideContent {
                    GlanceTheme {
                        ErrorState(e.message ?: "Unknown error")
                    }
                }
            } catch (fallbackError: Exception) {
                Log.e("QuickAddWidget", "✗ Failed to provide error state: ${fallbackError.message}", fallbackError)
                // Last resort: simple text content
                provideContent {
                    Text("Widget Error")
                }
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, hasCars: Boolean, lastTransaction: LastTransaction?) {
        Log.d("QuickAddWidget", "=== Widget Content Render ===")
        Log.d("QuickAddWidget", "Has cars: $hasCars, xLast transaction: $lastTransaction")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!hasCars) {
                EmptyState(context)
            } else {
                QuickAddContent(context, lastTransaction)
            }
        }
    }

    @Composable
    private fun ErrorState(errorMessage: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Widget Error",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(day = Color.Red, night = Color.Red)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(day = Color.Gray, night = Color.LightGray)
                )
            )
        }
    }


    @Composable
    private fun EmptyState(context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App branding
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_cariboo1_monochrome),
                    contentDescription = context.getString(R.string.app_name),
                    modifier = GlanceModifier.size(48.dp)
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = context.getString(R.string.app_name),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                // No cars message
                Text(
                    text = context.getString(R.string.widget_no_car),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = context.getString(R.string.widget_add_car_first),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }

    @Composable
    private fun QuickAddContent(context: Context, lastTransaction: LastTransaction?) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_cariboo1_monochrome),
                    contentDescription = context.getString(R.string.app_name),
                    modifier = GlanceModifier.size(28.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = context.getString(R.string.app_name),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Last Transaction (if available)
            if (lastTransaction != null) {
                LastTransactionWidget(lastTransaction)
                Spacer(modifier = GlanceModifier.height(12.dp))
            }

            // Quick Add Buttons
            QuickAddButtons(context)
        }
    }

    @Composable
    private fun LastTransactionWidget(transaction: LastTransaction) {
        val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val formattedDate = dateFormatter.format(java.util.Date(transaction.timestamp))
        val formattedAmount = String.format(java.util.Locale.getDefault(), "€%.2f", transaction.amount)

        // Transaction preview as simple text (no card background)
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transaction icon
                Image(
                    provider = ImageProvider(R.drawable.ic_refill),
                    contentDescription = null,
                    modifier = GlanceModifier.size(16.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))

                // Transaction type
                Text(
                    text = transaction.type,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Amount
                Text(
                    text = formattedAmount,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Date and car name
            Text(
                text = "$formattedDate • ${transaction.carName}",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }

    @Composable
    private fun QuickAddButtons(context: Context) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Refill Button - uses ActionCallback to launch activity then refresh
            QuickActionButton(
                icon = R.drawable.ic_refill,
                text = context.getString(R.string.widget_add_fuel),
                onClick = actionRunCallback<RefillActionCallback>(),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Add Expense Button - uses ActionCallback to launch activity then refresh
            QuickActionButton(
                icon = R.drawable.ic_receipt_24dp,
                text = context.getString(R.string.widget_add_expense),
                onClick = actionRunCallback<ExpenseActionCallback>(),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }

    @Composable
    private fun QuickActionButton(
        icon: Int,
        text: String,
        onClick: Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .height(48.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(12.dp)
                .clickable(onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(icon),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSecondaryContainer
                    )
                )
            }
        }
    }
}

/**
 * ActionCallback that launches QuickEntryActivity and refreshes widget after completion
 * This is triggered when user taps the refill or expense button on the widget
 */
class RefillActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("RefillActionCallback", "=== onAction triggered ===")
        Log.d("RefillActionCallback", "GlanceId: $glanceId")

        try {
            // Launch the refill activity
            val intent = QuickEntryActivity.createRefillIntent(context)

            // Start the activity
            context.startActivity(intent)

            Log.d("RefillActionCallback", "✓ Activity launched successfully")

            // Immediately update widget state to show it's loading/pending
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs.toMutablePreferences()
            }

            // Trigger widget refresh
            QuickAddWidget.update(context, glanceId)

            // Note: Widget will be updated again via broadcast when activity finishes
            // The broadcast mechanism in QuickEntryActivity will handle the final refresh with new data

        } catch (e: Exception) {
            Log.e("RefillActionCallback", "Error in action callback: ${e.message}", e)
        }
    }
}

class ExpenseActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("ExpenseActionCallback", "=== onAction triggered ===")
        Log.d("ExpenseActionCallback", "GlanceId: $glanceId")

        try {
            // Launch the expense activity
            val intent = QuickEntryActivity.createExpenseIntent(context)

            // Start the activity
            context.startActivity(intent)

            Log.d("ExpenseActionCallback", "✓ Activity launched successfully")

            // Immediately update widget state to show it's loading/pending
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs.toMutablePreferences()
            }

            // Trigger widget refresh
            QuickAddWidget.update(context, glanceId)

            // Note: Widget will be updated again via broadcast when activity finishes
            // The broadcast mechanism in QuickEntryActivity will handle the final refresh with new data

        } catch (e: Exception) {
            Log.e("ExpenseActionCallback", "Error in action callback: ${e.message}", e)
        }
    }
}

/**
 * Widget data model
 */
data class WidgetData(
    val noCarsAvailable: Boolean = false,
    val selectedCarName: String? = null,
    val lastSelectedCarId: Long? = null,
    val monthlyTotal: Double? = null
)
