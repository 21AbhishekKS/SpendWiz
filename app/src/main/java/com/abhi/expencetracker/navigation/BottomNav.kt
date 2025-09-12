package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.Notifications.PreferencesManager
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Screens.AddScreen
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Screens.InsightsScreen
import com.abhi.expencetracker.Screens.ManageCategoriesScreen
import com.abhi.expencetracker.Screens.MoreOptionsScreen
import com.abhi.expencetracker.Screens.NotificationSettingsScreen
import com.abhi.expencetracker.Screens.TransactionScreen
import com.abhi.expencetracker.Screens.UpdateScreen
import com.abhi.expencetracker.ViewModels.CategoryViewModel
import com.abhi.expencetracker.helper.BottomNavigationItem


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(navController: NavController,
              moneyViewModel: AddScreenViewModel,
              categoryViewModel: CategoryViewModel,
              prefs: PreferencesManager,
              onDailyToggle: (Boolean, Int, Int) -> Unit,
              onTransactionToggle: (Boolean) -> Unit) {
val context = LocalContext.current
    val navController1 = rememberNavController()
    Scaffold(
        bottomBar = { MyBottomBar(navController1 = navController1) }
    ) { innerPadding ->
        NavHost(
            navController = navController1,
            startDestination = Routes.HomeScreen.route,
            modifier = Modifier.padding(innerPadding))
            {
                composable(route = Routes.HomeScreen.route) {
                    HomeScreen(moneyViewModel, navController1)
                }

                composable(Routes.AddScreen.route) {
                    AddScreen(moneyViewModel, categoryViewModel ,navController1)
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
                            "&category={category}&subCategory={subCategory}&date={date}",
                    arguments = listOf(
                        navArgument("description") { type = NavType.StringType; defaultValue = "" },
                        navArgument("amount") { type = NavType.FloatType; defaultValue = 0f },
                        navArgument("id") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("type") { type = NavType.StringType; defaultValue = TransactionType.EXPENSE.toString() },
                        navArgument("category") { type = NavType.StringType; defaultValue = "" },
                        navArgument("subCategory") { type = NavType.StringType; defaultValue = "" },
                        navArgument("date") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    val amount = backStackEntry.arguments?.getFloat("amount") ?: 0f
                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                    val typeStr = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.toString()
                    val type = TransactionType.valueOf(typeStr)
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    val subCategory = backStackEntry.arguments?.getString("subCategory") ?: ""
                    val date = backStackEntry.arguments?.getString("date") ?: ""

                    UpdateScreen(
                        viewModel = moneyViewModel,
                        categoryViewModel = categoryViewModel,
                        navController = navController1,
                        description = description,
                        amount = amount.toDouble(),
                        id = id,
                        type = type.name,
                        category = category,
                        subCategory = subCategory,
                        date = date
                    )
                }

                composable(Routes.InsightsScreen.route) {
                    InsightsScreen(moneyViewModel , categoryViewModel ,navController1)
                }

                composable(Routes.SpentScreen.route) {
                    TransactionScreen(moneyViewModel)
                }

                composable(Routes.More.route) {
                    MoreOptionsScreen(moneyViewModel , navController1)
                }
                composable(Routes.ManageCategoriesScreen.route) {
                    ManageCategoriesScreen(categoryViewModel)
                }



            }
    }
}

@Composable
fun MyBottomBar(navController1: NavHostController) {
    val backStackEntry = navController1.currentBackStackEntryAsState()

    // Define navigation items (excluding the Add button which will be in center)
    val items = listOf(
        BottomNavigationItem(Routes.HomeScreen.route, Icons.Filled.Home, Icons.Outlined.Home, false, null),
        BottomNavigationItem(Routes.SpentScreen.route, Icons.Filled.Info, Icons.Outlined.Info, false, null),
        BottomNavigationItem(Routes.InsightsScreen.route, Icons.Filled.DateRange, Icons.Outlined.DateRange, false, null),
        BottomNavigationItem(Routes.More.route, Icons.Filled.MoreVert, Icons.Outlined.MoreVert, false, null),
    )


    Column {
        Divider(
            color = Color(0xFFDDDDDD),
            thickness = 1.dp
        )

        Box {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left side items (Home)
                items.take(2).forEach { item ->
                    val selected = item.title == backStackEntry.value?.destination?.route
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF3B6AD3)),
                        selected = selected,
                        onClick = {
                            navController1.navigate(item.title) {
                                popUpTo(navController1.graph.findStartDestination().id) {
                                    saveState = true
                                }
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

                // Spacer to push items to sides and make space for FAB
                Spacer(modifier = Modifier.weight(1f))

                // Right side items (History and Insights)
                items.drop(2).forEach { item ->
                    val selected = item.title == backStackEntry.value?.destination?.route
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF3B6AD3)),
                        selected = selected,
                        onClick = {
                            navController1.navigate(item.title) {
                                popUpTo(navController1.graph.findStartDestination().id) {
                                    saveState = true
                                }
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

            // Floating Action Button in the center
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-24).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(
                    onClick = {
                        navController1.navigate(Routes.AddScreen.route) {
                            popUpTo(navController1.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    },
                    containerColor = Color(0xFF3B6AD3), // Blue color for the FAB
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, shape = CircleShape),
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Optional: Add text label below the FAB
                Text(
                    text = "Add",
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}