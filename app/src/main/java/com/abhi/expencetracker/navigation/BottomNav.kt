package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Screens.AddScreen
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Screens.InsightsScreen
import com.abhi.expencetracker.Screens.TransactionScreen
import com.abhi.expencetracker.Screens.UpdateScreen
import com.abhi.expencetracker.helper.BottomNavigationItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(navController: NavHostController , moneyViewModel : AddScreenViewModel){

    var isBottomBarVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val navController1 = rememberNavController()
    Scaffold(bottomBar = {MyBottomBar(navController1 = navController1)

         }){ innerPadding ->
        NavHost(navController = navController1,
            startDestination = Routes.HomeScreen.route ,
            modifier = Modifier.padding(innerPadding)){

            composable(route = Routes.HomeScreen.route){
                HomeScreen(  moneyViewModel ,navController1 )
            }





            //composable(Routes.AddScreen.route+"?description={description}/?amount={amount}/?id={id}"){
                composable(Routes.AddScreen.route){

              //  AddScreen(moneyViewModel , description ?: "" , amount ?: "" , updateMode?: false)
                    AddScreen(moneyViewModel , navController1)

            }

            composable(Routes.UpdateScreen.route + "?description={description}&amount={amount}&id={id}&type={type}"){
                var description = it.arguments?.getString("description")
                var amount = it.arguments?.getString("amount")
                var type = it.arguments?.getString("type")
                var id = it.arguments?.getInt("id")

              //  AddScreen(moneyViewModel , description ?: "" , amount ?: "" , updateMode?: false)
                if (id != null) {
                    UpdateScreen(moneyViewModel , navController1,description ?: "" , amount ?: "" , id ,type?:"Transaction")
                }
            }

            composable(Routes.InsightsScreen.route){
                InsightsScreen(moneyViewModel)
              //  OnBoardingScreen()
            }




            composable(Routes.SpentScreen.route){
                TransactionScreen(moneyViewModel )
            }

        }

    }
}
@Composable
fun MyBottomBar(navController1: NavHostController) {
    val backStackEntry = navController1.currentBackStackEntryAsState()
    val items = listOf(
        BottomNavigationItem(
            "Home",
            Icons.Filled.Home ,
            Icons.Outlined.Home ,
            hasNews = false,
            badgeCount = null
        ),


        BottomNavigationItem(
            "Add",
            Icons.Filled.AddCircle ,
            Icons.Outlined.Add ,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            "History",
            Icons.Filled.Info ,
            Icons.Outlined.Info ,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            "Insights",
            Icons.Filled.DateRange ,
            Icons.Outlined.DateRange ,
            hasNews = false,
            badgeCount = null
        ),
    )

    BottomAppBar(containerColor = Color.White,
            content ={
        items.forEachIndexed { index, bottomNavigationItem ->
            val selected = bottomNavigationItem.title == backStackEntry.value?.destination?.route

            NavigationBarItem(

                colors = NavigationBarItemDefaults.colors(
                    indicatorColor =  Color(84, 75, 100, 255),
                ),
                selected = selected,
                onClick = {
                    navController1.navigate(bottomNavigationItem.title) {
                        popUpTo(navController1.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(
                    imageVector = bottomNavigationItem.selectedIcon,
                    contentDescription = bottomNavigationItem.title,
                    tint = if (selected) Color.White else  Color(84, 75, 100, 255)
                )}, label = {
                    Text(text = bottomNavigationItem.title , color = Color.Black)
                           },


            )
        }
    } ) }
