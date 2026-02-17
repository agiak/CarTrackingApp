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
            // Fetch data BEFORE provideContent for proper Glance behavior
            val hasCars = WidgetDataProvider.hasAnyCars(context)
            val hasMicPermission = WidgetPermissionChecker.hasMicrophonePermission(context)

            Log.d("QuickAddWidget", "Has cars: $hasCars")
            Log.d("QuickAddWidget", "Has microphone permission: $hasMicPermission")

            provideContent {
                GlanceTheme {
                    WidgetContent(context, hasCars, hasMicPermission)
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
    private fun WidgetContent(context: Context, hasCars: Boolean, hasMicPermission: Boolean) {
        Log.d("QuickAddWidget", "=== Widget Content Render ===")
        Log.d("QuickAddWidget", "Has cars: $hasCars")
        Log.d("QuickAddWidget", "Has mic permission: $hasMicPermission")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!hasCars) {
                EmptyState(context)
            } else {
                QuickAddContent(context, hasMicPermission)
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
    private fun QuickAddContent(context: Context, hasMicPermission: Boolean) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App branding - Smaller for 2x2 widget
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_cariboo1_monochrome),
                    contentDescription = context.getString(R.string.app_name),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = context.getString(R.string.app_name),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Quick Add Buttons - Icon Only
            QuickAddButtons(context, hasMicPermission)
        }
    }

    // Remove LastTransactionWidget composable - no longer needed

    @Composable
    private fun QuickAddButtons(context: Context, hasMicPermission: Boolean) {
        // Icon-only buttons: Refill, Expense, and optionally Voice (if mic permission granted)
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Refill Button - Icon Only
            QuickActionButton(
                icon = R.drawable.ic_refill,
                contentDescription = context.getString(R.string.widget_add_fuel),
                onClick = actionRunCallback<RefillActionCallback>()
            )

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Add Expense Button - Icon Only
            QuickActionButton(
                icon = R.drawable.ic_receipt_24dp,
                contentDescription = context.getString(R.string.widget_add_expense),
                onClick = actionRunCallback<ExpenseActionCallback>()
            )

            // Voice Button - Only show if microphone permission is granted
            if (hasMicPermission) {
                Spacer(modifier = GlanceModifier.width(12.dp))

                QuickActionButton(
                    icon = R.drawable.ic_mic,
                    contentDescription = context.getString(R.string.voice_entry),
                    onClick = actionRunCallback<VoiceActionCallback>()
                )
            }
        }
    }

    @Composable
    private fun QuickActionButton(
        icon: Int,
        contentDescription: String,
        onClick: Action
    ) {
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(12.dp)
                .clickable(onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )
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
 * ActionCallback for voice entry from widget
 * Launches QuickEntryActivity in voice mode
 */
class VoiceActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("VoiceActionCallback", "=== Voice onAction triggered ===")
        Log.d("VoiceActionCallback", "GlanceId: $glanceId")

        try {
            // Check permission one more time before launching
            if (!WidgetPermissionChecker.hasMicrophonePermission(context)) {
                Log.w("VoiceActionCallback", "Microphone permission not granted")
                // Could launch app to permission screen, but for now just log
                return
            }

            // Launch the voice entry activity
            val intent = QuickEntryActivity.createVoiceIntent(context)

            // Start the activity
            context.startActivity(intent)

            Log.d("VoiceActionCallback", "✓ Voice activity launched successfully")

            // Immediately update widget state
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs.toMutablePreferences()
            }

            // Trigger widget refresh
            QuickAddWidget.update(context, glanceId)

            // Widget will be updated again via broadcast when activity finishes

        } catch (e: Exception) {
            Log.e("VoiceActionCallback", "Error in voice action callback: ${e.message}", e)
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
