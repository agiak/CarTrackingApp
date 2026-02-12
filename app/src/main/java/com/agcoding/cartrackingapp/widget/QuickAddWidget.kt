package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
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
class QuickAddWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("QuickAddWidget", "=== provideGlance called ===")
        Log.d("QuickAddWidget", "Context: $context")
        Log.d("QuickAddWidget", "Glance ID: $id")

        try {
            provideContent {
                GlanceTheme {
                    WidgetContent(context, id)
                }
            }
            Log.d("QuickAddWidget", "✓ Content provided successfully")
        } catch (e: Exception) {
            Log.e("QuickAddWidget", "✗ Error providing content: ${e.message}", e)
            // Provide fallback content in case of error
            provideContent {
                GlanceTheme {
                    ErrorState(e.message ?: "Unknown error")
                }
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, glanceId: GlanceId) {
        Log.d("QuickAddWidget", "=== Widget Content Render ===")
        Log.d("QuickAddWidget", "Glance ID: $glanceId")

        // Check if any cars exist and get last transaction
        val hasCars = WidgetDataProvider.hasAnyCars(context).collectAsState(initial = null)
        val lastTransaction = WidgetDataProvider.getLastTransaction(context).collectAsState(initial = null)

        Log.d("QuickAddWidget", "Has cars: ${hasCars.value}")
        Log.d("QuickAddWidget", "Last transaction: ${lastTransaction.value}")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (hasCars.value) {
                null -> LoadingState()
                false -> EmptyState(context)
                true -> QuickAddContent(context, lastTransaction.value)
            }
        }
    }

    companion object {
        val WIDGET_ID_KEY = intPreferencesKey("widget_id")
        val CAR_ID_KEY = androidx.datastore.preferences.core.longPreferencesKey("car_id")
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
    private fun LoadingState() {
        CircularProgressIndicator()
    }

    @Composable
    private fun EmptyState(context: Context) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher_cariboo1_monochrome),
                contentDescription = null,
                modifier = GlanceModifier.size(48.dp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = context.getString(R.string.widget_no_car),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(day = Color.Gray, night = Color.LightGray)
                )
            )
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
                LastTransactionWidget(context, lastTransaction)
                Spacer(modifier = GlanceModifier.height(12.dp))
            }

            // Quick Add Buttons
            QuickAddButtons(context)
        }
    }

    @Composable
    private fun LastTransactionWidget(context: Context, transaction: LastTransaction) {
        val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val formattedDate = dateFormatter.format(java.util.Date(transaction.timestamp))
        val formattedAmount = String.format(java.util.Locale.getDefault(), "€%.2f", transaction.amount)

        // Transaction preview box
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(10.dp)
                .padding(10.dp)
        ) {
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
                        colorFilter = androidx.glance.ColorFilter.tint(GlanceTheme.colors.primary)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Transaction type
                    Text(
                        text = transaction.type,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSecondaryContainer
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
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }

    @Composable
    private fun QuickAddButtons(context: Context) {
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            // Add Refill Button
            // Note: ic_gas_station_24dp IS Icons.Default.LocalGasStation
            // (Glance widgets require drawable resources instead of Material Icons)
            QuickActionButton(
                icon = R.drawable.ic_refill, // Equivalent to Icons.Default.LocalGasStation
                text = context.getString(R.string.widget_add_fuel),
                onClick = actionStartActivity(
                    QuickEntryActivity.createRefillIntent(context)
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Add Expense Button
            QuickActionButton(
                icon = R.drawable.ic_receipt_24dp,
                text = context.getString(R.string.widget_add_expense),
                onClick = actionStartActivity(
                    QuickEntryActivity.createExpenseIntent(context)
                ),
                modifier = GlanceModifier.fillMaxWidth()
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
 * Widget data model
 */
data class WidgetData(
    val noCarsAvailable: Boolean = false,
    val selectedCarName: String? = null,
    val lastSelectedCarId: Long? = null,
    val monthlyTotal: Double? = null
)

