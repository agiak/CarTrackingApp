package com.agcoding.cartrackingapp.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Widget Preview Activity
 * Displays a preview of the widget for development and showcase purposes
 */
class WidgetPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CarTrackingAppTheme {
                WidgetPreviewScreen(
                    lastTransactionType = "Refill",
                    lastTransactionAmount = "€70.00",
                    lastTransactionDate = "12/02/2026",
                    lastTransactionCar = "Toyota Corolla",
                    hasTransaction = true
                )
            }
        }
    }
}

