package com.abhi.expencetracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhi.expencetracker.Database.money.TransactionType
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


//        composable(
//            route = Routes.UpdateScreen.route +
//                    "?description={description}&amount={amount}&id={id}&type={type}" +
//                    "&category={category}&subCategory={subCategory}&date={date}",
//            arguments = listOf(
//                navArgument("description") { type = NavType.StringType; defaultValue = "" },
//                navArgument("amount") { type = NavType.FloatType; defaultValue = 0f },
//                navArgument("id") { type = NavType.IntType; defaultValue = 0 },
//                navArgument("type") { type = NavType.StringType; defaultValue = TransactionType.EXPENSE.toString() },
//                navArgument("category") {
//                    type = NavType.StringType
//                    nullable = true
//                    defaultValue = ""
//                },
//                navArgument("subCategory") {
//                    type = NavType.StringType
//                    nullable = true
//                    defaultValue = ""
//                },
//                navArgument("date") { type = NavType.StringType; defaultValue = "" }
//            )
//        ) { backStackEntry ->
//            val description = backStackEntry.arguments?.getString("description") ?: ""
//            val amount = backStackEntry.arguments?.getFloat("amount") ?: 0f
//            val id = backStackEntry.arguments?.getInt("id") ?: 0
//            val typeStr = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.toString()
//            val type = TransactionType.valueOf(typeStr)
//            val category = backStackEntry.arguments?.getString("category") ?: ""
//            val subCategory = backStackEntry.arguments?.getString("subCategory") ?: ""
//            val date = backStackEntry.arguments?.getString("date") ?: ""
//
//            UpdateScreen(
//                viewModel = viewModel,
//                navController = navController,
//                description = description,
//                amount = amount.toDouble(),
//                id = id,
//                type = type.name,
//                category = category,
//                subCategory = subCategory,
//                date = date
//            )
//        }







    }}


