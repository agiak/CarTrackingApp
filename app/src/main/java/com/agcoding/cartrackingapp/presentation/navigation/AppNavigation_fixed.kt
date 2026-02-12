package com.agcoding.cartrackingapp.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.agcoding.cartrackingapp.presentation.editreminder.EditReminderScreen
import com.agcoding.cartrackingapp.presentation.expense.AddExpenseScreen
import com.agcoding.cartrackingapp.presentation.expensecategories.ManageExpenseCategoriesScreen
import com.agcoding.cartrackingapp.presentation.expensedetails.ExpenseDetailsScreen
import com.agcoding.cartrackingapp.presentation.expensehistory.ExpenseHistoryScreen
import com.agcoding.cartrackingapp.presentation.notifications.NotificationsScreen
import com.agcoding.cartrackingapp.presentation.onboarding.OnboardingGuideScreen
import com.agcoding.cartrackingapp.presentation.onboarding.OnboardingScreen
import com.agcoding.cartrackingapp.presentation.refill.AddRefillBottomSheet
import com.agcoding.cartrackingapp.presentation.refilldetails.RefillDetailsScreen
import com.agcoding.cartrackingapp.presentation.refillhistory.RefillHistoryScreen
import com.agcoding.cartrackingapp.presentation.settings.SettingsScreen
import com.agcoding.cartrackingapp.presentation.settings.appearance.AppearanceSettingsScreen
import com.agcoding.cartrackingapp.presentation.settings.datastorage.DataStorageSettingsScreen
import com.agcoding.cartrackingapp.presentation.settings.developer.DeveloperSettingsScreen
import com.agcoding.cartrackingapp.presentation.settings.expensecategories.ExpenseCategoriesSettingsScreen
import com.agcoding.cartrackingapp.presentation.settings.helpabout.HelpAboutSettingsScreen
import com.agcoding.cartrackingapp.presentation.statistics.MonthDetailsScreen
import com.agcoding.cartrackingapp.presentation.statistics.MonthlyTrendsScreen
import com.agcoding.cartrackingapp.presentation.statistics.StatisticsScreen
import com.agcoding.cartrackingapp.presentation.transactions.TransactionsScreen
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionType
import com.agcoding.cartrackingapp.util.PermissionUtil
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
    object CostGraph : Screen("cost_graph?carId={carId}") {
        fun createRoute(carId: Long? = null) = if (carId != null) "cost_graph?carId=$carId" else "cost_graph"
    }
    object RefillsGraph : Screen("refills_graph?carId={carId}") {
        fun createRoute(carId: Long? = null) = if (carId != null) "refills_graph?carId=$carId" else "refills_graph"
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
    object ManageExpenseCategories : Screen("manage_expense_categories")
    object AddExpense : Screen("add_expense/{carId}") {
        fun createRoute(carId: Long) = "add_expense/$carId"
    }
    object Notifications : Screen("notifications")
    object EditReminder : Screen("edit_reminder/{expenseId}") {
        fun createRoute(expenseId: Long) = "edit_reminder/$expenseId"
    }

    // Settings Group Screens
    object AppearanceSettings : Screen("settings/appearance")
    object DataStorageSettings : Screen("settings/data_storage")
    object ExpenseCategoriesSettings : Screen("settings/expense_categories")
    object HelpAboutSettings : Screen("settings/help_about")
    object DeveloperSettings : Screen("settings/developer")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    var showAddRefillSheet by remember { mutableStateOf<Long?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get pending alerts count for badge
    val pendingAlertsCount by navigationViewModel.pendingAlertsCount.collectAsStateWithLifecycle(initialValue = 0)

    // Determine if we should show bottom bar
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Cars.route,
        BottomNavItem.Transactions.route,
        BottomNavItem.Statistics.route,
        BottomNavItem.Settings.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
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
                                    // Show badge on Settings if there are pending alerts
                                    if (item is BottomNavItem.Settings && pendingAlertsCount > 0) {
                                        BadgedBox(
                                            badge = { Badge() }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(item.labelResId)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelResId)
                                        )
                                    }
                                },
                                label = { Text(stringResource(item.labelResId)) }
                            )
                        }
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
            animatedComposable(
                route = Screen.Onboarding.route,
                animationConfig = NavigationAnimations.FadeOnly
            ) {
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
            animatedComposable(
                route = Screen.GuideOnly.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
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
                    },
                    onAddServiceClick = { carId ->
                        navController.navigate(Screen.AddExpense.createRoute(carId))
                    },
                    onNavigateToReminders = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }

            composable(BottomNavItem.Transactions.route) {
                TransactionsScreen(
                    onTransactionClick = { transaction ->
                        when (transaction.type) {
                            TransactionType.REFILL -> {
                                navController.navigate(Screen.RefillDetails.createRoute(transaction.id))
                            }
                            TransactionType.EXPENSE -> {
                                navController.navigate(Screen.ExpenseDetails.createRoute(transaction.id))
                            }
                        }
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
                    onCostGraphClick = {
                        navController.navigate(Screen.CostGraph.createRoute())
                    },
                    onRefillsGraphClick = {
                        navController.navigate(Screen.RefillsGraph.createRoute())
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
                    onNavigateToAppearance = {
                        navController.navigate(Screen.AppearanceSettings.route)
                    },
                    onNavigateToDataStorage = {
                        navController.navigate(Screen.DataStorageSettings.route)
                    },
                    onNavigateToExpenseCategories = {
                        navController.navigate(Screen.ExpenseCategoriesSettings.route)
                    },
                    onNavigateToHelpAbout = {
                        navController.navigate(Screen.HelpAboutSettings.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToDeveloper = {
                        navController.navigate(Screen.DeveloperSettings.route)
                    }
                )
            }

            // Settings Group Screens
            animatedComposable(
                route = Screen.AppearanceSettings.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                AppearanceSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            animatedComposable(
                route = Screen.DataStorageSettings.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                DataStorageSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            animatedComposable(
                route = Screen.ExpenseCategoriesSettings.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                ExpenseCategoriesSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            animatedComposable(
                route = Screen.HelpAboutSettings.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                val context = LocalContext.current
                HelpAboutSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onViewGuide = {
                        navController.navigate(Screen.GuideOnly.route)
                    },
                    onViewNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onOpenSettings = {
                        PermissionUtil.openAppSettings(context)
                    }
                )
            }

            animatedComposable(
                route = Screen.DeveloperSettings.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                DeveloperSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Detail Screens (without bottom bar)
            animatedComposable(
                route = Screen.CarDetails.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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
                        navController.navigate(Screen.AddExpense.createRoute(carId))
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

            animatedComposable(
                route = Screen.EditCar.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.RefillHistory.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.ExpenseHistory.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.RefillDetails.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.ConsumptionGraph.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.DistanceGraph.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.CostGraph.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
                arguments = listOf(
                    navArgument("carId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                com.agcoding.cartrackingapp.presentation.costgraph.CostGraphScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = hiltViewModel()
                )
            }

            animatedComposable(
                route = Screen.RefillsGraph.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
                arguments = listOf(
                    navArgument("carId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                com.agcoding.cartrackingapp.presentation.refillsgraph.RefillsGraphScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = hiltViewModel()
                )
            }

            animatedComposable(
                route = Screen.EditRefill.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.ExpenseDetails.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.EditExpense.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.MonthlyTrends.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                MonthlyTrendsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onMonthClick = { month, year ->
                        navController.navigate(Screen.MonthDetails.createRoute(month, year))
                    }
                )
            }

            animatedComposable(
                route = Screen.MonthDetails.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
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

            animatedComposable(
                route = Screen.ManageExpenseCategories.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                ManageExpenseCategoriesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            animatedComposable(
                route = Screen.AddExpense.route,
                animationConfig = NavigationAnimations.VerticalSlide,
                arguments = listOf(
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                AddExpenseScreen(
                    carId = carId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            animatedComposable(
                route = Screen.Notifications.route,
                animationConfig = NavigationAnimations.HorizontalSlide
            ) {
                NotificationsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditExpense = { expenseId ->
                        navController.navigate(Screen.EditReminder.createRoute(expenseId))
                    }
                )
            }

            animatedComposable(
                route = Screen.EditReminder.route,
                animationConfig = NavigationAnimations.HorizontalSlide,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
                EditReminderScreen(
                    expenseId = expenseId,
                    onNavigateBack = {
                        navController.popBackStack()
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
    }
}
