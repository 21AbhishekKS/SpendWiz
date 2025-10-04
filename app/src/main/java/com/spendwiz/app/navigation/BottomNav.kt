package com.spendwiz.app.navigation

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.Screens.*
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.ViewModels.CategoryViewModel
import com.spendwiz.app.helper.BottomNavigationItem
import com.spendwiz.app.ui.theme.*
import com.spendwiz.app.voiceAssistant.InternalAssistant.InAppVoiceAssistantFab
import com.spendwiz.app.Notifications.PreferencesManager as NotificationPrefsManager
import com.spendwiz.app.Screens.PreferencesManager as VoicePrefsManager


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(
    navController: NavHostController,
    moneyViewModel: AddScreenViewModel,
    categoryViewModel: CategoryViewModel,
    prefs: NotificationPrefsManager,
    onDailyToggle: (Boolean, Int, Int) -> Unit,
    onTransactionToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val voicePrefs = remember { VoicePrefsManager(context) }
    var showInAppFab by remember { mutableStateOf(voicePrefs.isInAppAssistantEnabled()) }
    var isServiceEnabled by remember { mutableStateOf(voicePrefs.isServiceEnabled()) }

    val onInAppAssistantToggle: (Boolean) -> Unit = { isEnabled ->
        showInAppFab = isEnabled
        voicePrefs.setInAppAssistantEnabled(isEnabled)
    }

    val onServiceToggle: (Boolean) -> Unit = { isEnabled ->
        isServiceEnabled = isEnabled
        voicePrefs.setServiceEnabled(isEnabled)
    }

    //Dynamic start destination for navigation through voice command
    val startDestination = remember {
        val intentRoute = (context as? Activity)?.intent?.getStringExtra("NAVIGATE_TO")
        intentRoute ?: Routes.HomeScreen.route
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        showInAppFab = voicePrefs.isInAppAssistantEnabled()
        isServiceEnabled = voicePrefs.isServiceEnabled()
    }

    Scaffold(
        bottomBar = { MyBottomBar(navController = navController) },
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (showInAppFab) {
                InAppVoiceAssistantFab()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Routes.HomeScreen.route) {
                HomeScreen(moneyViewModel, navController)
            }
            composable(Routes.AddScreen.route) {
                AddScreen(moneyViewModel, categoryViewModel, navController)
            }
            composable("NotificationSettingsScreen") {
                NotificationSettingsScreen(
                    prefs = NotificationPrefsManager(context),
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
                    navController = navController,
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
                InsightsScreen(moneyViewModel, categoryViewModel, navController)
            }
            composable(Routes.AnnualScreen.route) {
                Annual(moneyViewModel , navController)
            }
            composable(Routes.More.route) {
                MoreOptionsScreen(moneyViewModel, navController)
            }
            composable(Routes.FaqScreen.route) {
                FaqScreen()
            }
            composable(Routes.ManageCategoriesScreen.route) {
                ManageCategoriesScreen(categoryViewModel)
            }
            composable(Routes.BackupRestoreScreen.route) {
                BackupRestoreScreen(addScreenViewModel = moneyViewModel)
            }
            composable(Routes.ReceiptScanScreen.route) {
                ReceiptScanScreen(viewModel = moneyViewModel , navController)
            }
            composable(Routes.VoiceAssistantSettingsScreen.route) {
                VoiceAssistantSettingsScreen(
                    isServiceEnabled = isServiceEnabled,
                    isInAppAssistantEnabled = showInAppFab,
                    onServiceToggle = onServiceToggle,
                    onInAppAssistantToggle = onInAppAssistantToggle
                )
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
                    viewModel = moneyViewModel,
                    navController = navController,
                    description = description,
                    category = category,
                    subCategory = subCategory
                )
            }
        }
    }
}


@Composable
fun MyBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val items = listOf(
        BottomNavigationItem(Routes.HomeScreen.route, Icons.Filled.Home, Icons.Outlined.Home, false, null),
        BottomNavigationItem(Routes.InsightsScreen.route, Icons.Filled.DateRange, Icons.Outlined.DateRange, false, null),
        BottomNavigationItem(Routes.AnnualScreen.route, Icons.Filled.Info, Icons.Outlined.Info, false, null),
        BottomNavigationItem(Routes.More.route, Icons.Filled.MoreVert, Icons.Outlined.MoreVert, false, null),
    )

    val indicatorColor = if (darkTheme) BluePrimaryDark else BluePrimaryLight
    val fabColor = if (darkTheme) BluePrimaryDark else BluePrimaryLight
    val selectedIconColor = if (darkTheme) BottomIconSelectedDark else BottomIconSelectedLight
    val unselectedIconColor = colorScheme.onBackground
    val selectedLabelColor = if (darkTheme) BottomLabelSelectedDark else BottomLabelSelectedLight
    val unselectedLabelColor = colorScheme.onSurface

    Box {
        BottomAppBar(
            containerColor = colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .border(
                    width = 1.dp,
                    color = if (darkTheme) BottomBarBorderDark else BottomBarBorderLight,
                    shape = RectangleShape
                )
        ) {
            items.forEachIndexed { index, item ->
                if (index == items.size / 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                val selected = item.title == backStackEntry?.destination?.route
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = indicatorColor
                    ),
                    selected = selected,
                    onClick = {
                        navController.navigate(item.title) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unSelectedIcon,
                            contentDescription = item.title,
                            tint = if (selected) selectedIconColor else unselectedIconColor
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            color = if (selected) selectedLabelColor else unselectedLabelColor
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.AddScreen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                },
                containerColor = fabColor,
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