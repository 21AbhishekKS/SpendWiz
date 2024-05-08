package com.abhi.expencetracker.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.helper.BottomNavigationItem
import com.abhi.expencetracker.navigation.Routes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(navController: NavHostController , moneyViewModel : AddScreenViewModel){


    val navController1 = rememberNavController()
    Scaffold(bottomBar = { MyBottomBar(navController1 = navController1) }){ innerPadding ->
        NavHost(navController = navController1,
            startDestination = Routes.HomeScreen.route ,
            modifier = Modifier.padding(innerPadding)){

            composable(route = Routes.HomeScreen.route){
                HomeScreen(moneyViewModel , navController1)
            }

            //composable(Routes.AddScreen.route+"?description={description}/?amount={amount}/?id={id}"){
                composable(Routes.AddScreen.route + "?description={description}&amount={amount}&id={id}&type={type}"){
                var description = it.arguments?.getString("description")
                var amount = it.arguments?.getString("amount")
                var type = it.arguments?.getString("type")
                var id = it.arguments?.getInt("id")

              //  AddScreen(moneyViewModel , description ?: "" , amount ?: "" , updateMode?: false)
                if (id != null) {
                    AddScreen(moneyViewModel , description ?: "" , amount ?: "" , id ,type?:"Spent")
                }
            }

            composable(Routes.ProfileScreen.route){
                //ProfileScreen()
                OnBoardingScreen()
            }

            composable(Routes.TransferScreen.route){
                TransferScreen()
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
        //BottomNavigationItem(
        //            "Trans",
        //            Icons.Filled.ExitToApp ,
        //            Icons.Outlined.ExitToApp ,
        //            hasNews = false,
        //            badgeCount = null
        //        ),

        BottomNavigationItem(
            "Add",
            Icons.Filled.AddCircle ,
            Icons.Outlined.AddCircle ,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            "Transaction",
            Icons.Filled.ShoppingCart ,
            Icons.Outlined.ShoppingCart ,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            "Profile",
            Icons.Filled.Person ,
            Icons.Outlined.Person ,
            hasNews = false,
            badgeCount = null
        ),
    )

    BottomAppBar {
        items.forEachIndexed { index, bottomNavigationItem ->
            val selected = bottomNavigationItem.title == backStackEntry.value?.destination?.route

            NavigationBarItem(

                selected = selected,
                onClick = {
                    navController1.navigate(bottomNavigationItem.title) {
                        popUpTo(navController1.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) bottomNavigationItem.selectedIcon else bottomNavigationItem.unSelectedIcon,
                        contentDescription = bottomNavigationItem.title.toString()
                    )
                }, label = {
                    Text(text = bottomNavigationItem.title)}
            )
        }
    }}
