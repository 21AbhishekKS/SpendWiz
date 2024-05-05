package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abhi.expencetracker.Screens.AddScreen
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Screens.ProfileScreen
import com.abhi.expencetracker.Screens.SpentScreen
import com.abhi.expencetracker.Screens.TransferScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController){
    NavHost(navController = navController, startDestination = Routes.HomeScreen.route){

       // composable(route = Routes.HomeScreen.route){
       //     HomeScreen()
       // }

      //  composable(Routes.AddScreen.route){
      //      AddScreen()
      //  }

        composable(Routes.ProfileScreen.route){
            ProfileScreen()
        }

        composable(Routes.TransferScreen.route){
            TransferScreen()
        }

        composable(Routes.SpentScreen.route){
            SpentScreen()
        }

    }}