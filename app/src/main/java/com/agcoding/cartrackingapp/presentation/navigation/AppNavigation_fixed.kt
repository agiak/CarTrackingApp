package com.agcoding.cartrackingapp.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsScreen
import com.agcoding.cartrackingapp.presentation.carlist.CarListScreen
import com.agcoding.cartrackingapp.presentation.consumptiongraph.ConsumptionGraphScreen
import com.agcoding.cartrackingapp.presentation.distancegraph.DistanceGraphScreen
import com.agcoding.cartrackingapp.presentation.editcar.EditCarScreen
import com.agcoding.cartrackingapp.presentation.editexpense.EditExpenseScreen
import com.agcoding.cartrackingapp.presentation.editrefill.EditRefillScreen
import com.agcoding.cartrackingapp.presentation.expensedetails.ExpenseDetailsScreen
import com.agcoding.cartrackingapp.presentation.expensehistory.ExpenseHistoryScreen
import com.agcoding.cartrackingapp.presentation.onboarding.OnboardingGuideScreen
import com.agcoding.cartrackingapp.presentation.onboarding.OnboardingScreen
import com.agcoding.cartrackingapp.presentation.refill.AddRefillBottomSheet
import com.agcoding.cartrackingapp.presentation.refilldetails.RefillDetailsScreen
import com.agcoding.cartrackingapp.presentation.refillhistory.RefillHistoryScreen
import com.agcoding.cartrackingapp.presentation.settings.SettingsScreen
import com.agcoding.cartrackingapp.presentation.statistics.MonthDetailsScreen
import com.agcoding.cartrackingapp.presentation.statistics.MonthlyTrendsScreen
import com.agcoding.cartrackingapp.presentation.statistics.StatisticsScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object GuideOnly : Screen("guide_only") // For viewing guide from settings
    object CarList : Screen("car_list")
    object CarDetails : Screen("car_details/{carId}") {
        fun createRoute(carId: Long) = "car_details/$carId"
    }
    object EditCar : Screen("edit_car/{carId}") {
        fun createRoute(carId: Long) = "edit_car/$carId"
    }
    object RefillHistory : Screen("refill_history/{carId}") {
        fun createRoute(carId: Long) = "refill_history/$carId"
    }
    object ExpenseHistory : Screen("expense_history/{carId}") {
        fun createRoute(carId: Long) = "expense_history/$carId"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object ConsumptionGraph : Screen("consumption_graph?carId={carId}") {
        fun createRoute(carId: Long? = null) = if (carId != null) "consumption_graph?carId=$carId" else "consumption_graph"
    }
    object DistanceGraph : Screen("distance_graph?carId={carId}") {
        fun createRoute(carId: Long? = null) = if (carId != null) "distance_graph?carId=$carId" else "distance_graph"
    }
    object RefillDetails : Screen("refill_details/{refillId}") {
        fun createRoute(refillId: Long) = "refill_details/$refillId"
    }
    object EditRefill : Screen("edit_refill/{refillId}") {
        fun createRoute(refillId: Long) = "edit_refill/$refillId"
    }
    object ExpenseDetails : Screen("expense_details/{expenseId}") {
        fun createRoute(expenseId: Long) = "expense_details/$expenseId"
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Long) = "edit_expense/$expenseId"
    }
    object MonthlyTrends : Screen("monthly_trends")
    object MonthDetails : Screen("month_details/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "month_details/$month/$year"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var showAddRefillSheet by remember { mutableStateOf<Long?>(null) }
    var showAddExpenseSheet by remember { mutableStateOf<Long?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Determine if we should show bottom bar
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Cars.route,
        BottomNavItem.Statistics.route,
        BottomNavItem.Settings.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelResId)
                                )
                            },
                            label = { Text(stringResource(item.labelResId)) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Onboarding screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(BottomNavItem.Cars.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Guide only screen (accessed from settings)
            composable(Screen.GuideOnly.route) {
                OnboardingGuideScreen(
                    viewModel = hiltViewModel(),
                    onSkip = {
                        navController.popBackStack()
                    },
                    onComplete = {
                        navController.popBackStack()
                    }
                )
            }

            // Bottom Navigation Destinations
            composable(BottomNavItem.Cars.route) {
                CarListScreen(
                    onCarClick = { carId ->
                        navController.navigate(Screen.CarDetails.createRoute(carId))
                    },
                    onStatisticsClick = {
                        navController.navigate(BottomNavItem.Statistics.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddRefillClick = { carId ->
                        showAddRefillSheet = carId
                    }
                )
            }

            composable(BottomNavItem.Statistics.route) {
                StatisticsScreen(
                    onNavigateBack = null, // No back button when in bottom nav
                    onConsumptionGraphClick = {
                        navController.navigate(Screen.ConsumptionGraph.createRoute())
                    },
                    onDistanceGraphClick = {
                        navController.navigate(Screen.DistanceGraph.createRoute())
                    },
                    onMonthlyTrendsClick = {
                        navController.navigate(Screen.MonthlyTrends.route)
                    },
                    onMonthClick = { month, year ->
                        navController.navigate(Screen.MonthDetails.createRoute(month, year))
                    }
                )
            }

            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onViewGuide = {
                        navController.navigate(Screen.GuideOnly.route)
                    }
                )
            }

            // Detail Screens (without bottom bar)
            composable(
                route = Screen.CarDetails.route,
                arguments = listOf(
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                CarDetailsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddRefillClick = {
                        showAddRefillSheet = carId
                    },
                    onAddExpenseClick = {
                        showAddExpenseSheet = carId
                    },
                    onRefillClick = { refillId ->
                        navController.navigate(Screen.RefillDetails.createRoute(refillId))
                    },
                    onExpenseClick = { expenseId ->
                        navController.navigate(Screen.ExpenseDetails.createRoute(expenseId))
                    },
                    onEditCarClick = {
                        navController.navigate(Screen.EditCar.createRoute(carId))
                    },
                    onViewAllRefillsClick = {
                        navController.navigate(Screen.RefillHistory.createRoute(carId))
                    },
                    onViewAllExpensesClick = {
                        navController.navigate(Screen.ExpenseHistory.createRoute(carId))
                    }
                )
            }

            composable(
                route = Screen.EditCar.route,
                arguments = listOf(
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                EditCarScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.RefillHistory.route,
                arguments = listOf(
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                RefillHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRefillClick = { refillId ->
                        navController.navigate(Screen.RefillDetails.createRoute(refillId))
                    }
                )
            }

            composable(
                route = Screen.ExpenseHistory.route,
                arguments = listOf(
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                ExpenseHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onExpenseClick = { expenseId ->
                        navController.navigate(Screen.ExpenseDetails.createRoute(expenseId))
                    }
                )
            }

            composable(
                route = Screen.RefillDetails.route,
                arguments = listOf(
                    navArgument("refillId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val refillId = backStackEntry.arguments?.getLong("refillId") ?: 0L
                RefillDetailsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditClick = {
                        navController.navigate(Screen.EditRefill.createRoute(refillId))
                    }
                )
            }

            composable(
                route = Screen.ConsumptionGraph.route,
                arguments = listOf(
                    navArgument("carId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ConsumptionGraphScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.DistanceGraph.route,
                arguments = listOf(
                    navArgument("carId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                DistanceGraphScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = hiltViewModel()
                )
            }

            composable(
                route = Screen.EditRefill.route,
                arguments = listOf(
                    navArgument("refillId") { type = NavType.LongType }
                )
            ) {
                EditRefillScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ExpenseDetails.route,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
                ExpenseDetailsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditExpense = {
                        navController.navigate(Screen.EditExpense.createRoute(expenseId))
                    }
                )
            }

            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType }
                )
            ) {
                EditExpenseScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.MonthlyTrends.route) {
                MonthlyTrendsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onMonthClick = { month, year ->
                        navController.navigate(Screen.MonthDetails.createRoute(month, year))
                    }
                )
            }

            composable(
                route = Screen.MonthDetails.route,
                arguments = listOf(
                    navArgument("month") { type = NavType.IntType },
                    navArgument("year") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val month = backStackEntry.arguments?.getInt("month") ?: 0
                val year = backStackEntry.arguments?.getInt("year") ?: 2024
                MonthDetailsScreen(
                    month = month,
                    year = year,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRefillClick = { refillId ->
                        navController.navigate(Screen.RefillDetails.createRoute(refillId))
                    },
                    onExpenseClick = { expenseId ->
                        navController.navigate(Screen.ExpenseDetails.createRoute(expenseId))
                    }
                )
            }
        }

        // Add refill bottom sheet
        showAddRefillSheet?.let { carId ->
            AddRefillBottomSheet(
                carId = carId,
                onDismiss = {
                    showAddRefillSheet = null
                },
                onSuccess = {
                    // Show snackbar when refill is added successfully
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Refill added successfully",
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        // Add general expense bottom sheet
        showAddExpenseSheet?.let { carId ->
            com.agcoding.cartrackingapp.presentation.expense.AddExpenseBottomSheet(
                carId = carId,
                onDismiss = {
                    showAddExpenseSheet = null
                },
                onSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Expense added successfully",
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}
