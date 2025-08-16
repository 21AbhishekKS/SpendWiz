package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Screens.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController , viewModel: AddScreenViewModel){


    NavHost(navController = navController, startDestination = Routes.HomeScreen.route){


        composable(route = Routes.HomeScreen.route){
            HomeScreen(viewModel, navController, )
        }



     //
        composable(Routes.AddScreen.route + "?description={description}&amount={amount}&id={id}&type={type}"){
            var description = it.arguments?.getString("description")
            var amount = it.arguments?.getString("amount")
            var type = it.arguments?.getString("type")
            var id = it.arguments?.getInt("id")


        }




    }}


