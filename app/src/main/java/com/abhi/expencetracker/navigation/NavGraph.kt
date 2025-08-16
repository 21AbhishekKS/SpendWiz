package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Screens.UpdateScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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


        composable(
            route = Routes.UpdateScreen.route + "?description={description}&amount={amount}&id={id}&type={type}",
            arguments = listOf(
                navArgument("description") { type = NavType.StringType; defaultValue = "" },
                navArgument("amount") { type = NavType.StringType; defaultValue = "" },
                navArgument("id") { type = NavType.IntType; defaultValue = -1 },
                navArgument("type") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val description = backStackEntry.arguments?.getString("description") ?: ""
            val amount = backStackEntry.arguments?.getString("amount") ?: ""
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            val type = backStackEntry.arguments?.getString("type") ?: ""

            UpdateScreen(
                viewModel = viewModel,
                navController = navController,
                passedDescription = description,
                passedAmount = amount,
                id = id,
                type = type
            )
        }






    }}


