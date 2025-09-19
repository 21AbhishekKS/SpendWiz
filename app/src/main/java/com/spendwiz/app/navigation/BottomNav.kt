package com.spendwiz.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.Notifications.PreferencesManager
import com.spendwiz.app.Screens.*
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.ViewModels.CategoryViewModel
import com.spendwiz.app.helper.BottomNavigationItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(
    navController: NavController,
    moneyViewModel: AddScreenViewModel,
    categoryViewModel: CategoryViewModel,
    prefs: PreferencesManager,
    onDailyToggle: (Boolean, Int, Int) -> Unit,
    onTransactionToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val navController1 = rememberNavController()

    Scaffold(
        bottomBar = { MyBottomBar(navController1 = navController1) },
        contentWindowInsets = WindowInsets.safeDrawing // handles gesture vs 3-button insets
    ) { innerPadding ->
        NavHost(
            navController = navController1,
            startDestination = Routes.HomeScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Routes.HomeScreen.route) {
                HomeScreen(moneyViewModel, navController1)
            }
            composable(Routes.AddScreen.route) {
                AddScreen(moneyViewModel, categoryViewModel, navController1)
            }
            composable("NotificationSettingsScreen") {
                NotificationSettingsScreen(
                    prefs = PreferencesManager(context),
                    onDailyToggle = onDailyToggle,
                    onTransactionToggle = onTransactionToggle
                )
            }
            composable(
                route = Routes.UpdateScreen.route +
                        "?description={description}&amount={amount}&id={id}&type={type}" +
                        "&category={category}&subCategory={subCategory}&date={date}&time={time}",
                arguments = listOf(
                    navArgument("description") { type = NavType.StringType; defaultValue = "" },
                    navArgument("amount") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("id") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("type") { type = NavType.StringType; defaultValue = TransactionType.EXPENSE.toString() },
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("subCategory") { type = NavType.StringType; defaultValue = "" },
                    navArgument("date") { type = NavType.StringType; defaultValue = "" },
                    navArgument("time") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                UpdateScreen(
                    viewModel = moneyViewModel,
                    categoryViewModel = categoryViewModel,
                    navController = navController1,
                    description = backStackEntry.arguments?.getString("description") ?: "",
                    amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0,
                    id = backStackEntry.arguments?.getInt("id") ?: 0,
                    type = backStackEntry.arguments?.getString("type")
                        ?: TransactionType.EXPENSE.toString(),
                    category = backStackEntry.arguments?.getString("category") ?: "",
                    subCategory = backStackEntry.arguments?.getString("subCategory") ?: "",
                    date = backStackEntry.arguments?.getString("date") ?: "",
                    time = backStackEntry.arguments?.getString("time") ?: ""
                )
            }
            composable(Routes.InsightsScreen.route) {
                InsightsScreen(moneyViewModel, categoryViewModel, navController1)
            }
            composable(Routes.SpentScreen.route) {
                Annual(moneyViewModel , navController1)
            }
            composable(Routes.More.route) {
                MoreOptionsScreen(moneyViewModel, navController1)
            }
            composable(Routes.ManageCategoriesScreen.route) {
                ManageCategoriesScreen(categoryViewModel)
            }
            composable(Routes.SmartSettings.route) {
                SmartSettings(preferencesManager = PreferencesManager(context))
            }
            composable(
                route = Routes.IncomeDetailsScreen.route,
                arguments = listOf(navArgument("year") { type = NavType.StringType })
            ) { backStackEntry ->
                val year = backStackEntry.arguments?.getString("year") ?: ""
                YearlyCategoryScreen(
                    year = year,
                    type = TransactionType.INCOME,
                    vm = moneyViewModel
                )
            }

            composable(
                route = Routes.ExpenseDetailsScreen.route,
                arguments = listOf(navArgument("year") { type = NavType.StringType })
            ) { backStackEntry ->
                val year = backStackEntry.arguments?.getString("year") ?: ""
                YearlyCategoryScreen(
                    year = year,
                    type = TransactionType.EXPENSE,
                    vm = moneyViewModel
                )
            }

            composable(
                route = "${Routes.BulkUpdateScreen.route}/{description}/{category}/{subCategory}",
                arguments = listOf(
                    navArgument("description") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("subCategory") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val description = backStackEntry.arguments?.getString("description")?.let {
                    java.net.URLDecoder.decode(it, java.nio.charset.StandardCharsets.UTF_8.toString())
                } ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: "Others"
                val subCategory = backStackEntry.arguments?.getString("subCategory").takeIf { it?.isNotEmpty() == true }

                BulkUpdateScreen(
                    viewModel = moneyViewModel, // or pass your AddScreenViewModel
                    navController = navController1,
                    description = description,
                    category = category,
                    subCategory = subCategory
                )
            }



        }
    }
}

@Composable
fun MyBottomBar(navController1: NavHostController) {
    val backStackEntry = navController1.currentBackStackEntryAsState()

    val items = listOf(
        BottomNavigationItem(Routes.HomeScreen.route, Icons.Filled.Home, Icons.Outlined.Home, false, null),
        BottomNavigationItem(Routes.SpentScreen.route, Icons.Filled.Info, Icons.Outlined.Info, false, null),
        BottomNavigationItem(Routes.InsightsScreen.route, Icons.Filled.DateRange, Icons.Outlined.DateRange, false, null),
        BottomNavigationItem(Routes.More.route, Icons.Filled.MoreVert, Icons.Outlined.MoreVert, false, null),
    )

    Box {
        BottomAppBar(
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .border(
                    width = 1.dp,
                    color = Color(0xFFDDDDDD), // light gray border
                    shape = RectangleShape
                )
        ) {
            items.take(2).forEach { item ->
                val selected = item.title == backStackEntry.value?.destination?.route
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF3B6AD3)),
                    selected = selected,
                    onClick = {
                        navController1.navigate(item.title) {
                            popUpTo(navController1.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unSelectedIcon,
                            contentDescription = item.title,
                            tint = if (selected) Color.White else Color.Black
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            color = if (selected) Color(0xFF3B6AD3) else Color.Black
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            items.drop(2).forEach { item ->
                val selected = item.title == backStackEntry.value?.destination?.route
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF3B6AD3)),
                    selected = selected,
                    onClick = {
                        navController1.navigate(item.title) {
                            popUpTo(navController1.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unSelectedIcon,
                            contentDescription = item.title,
                            tint = if (selected) Color.White else Color.Black
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            color = if (selected) Color(0xFF3B6AD3) else Color.Black
                        )
                    }
                )
            }
        }

        // FAB + label in the center
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp), // lift up half
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {
                    navController1.navigate(Routes.AddScreen.route) {
                        popUpTo(navController1.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFF3B6AD3),
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

        }
    }
}
