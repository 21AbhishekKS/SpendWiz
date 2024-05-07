package com.abhi.expencetracker.navigation

sealed class Routes( val route : String) {
    object HomeScreen : Routes("Home")
    object TransferScreen : Routes("Trans")

    object ProfileScreen : Routes("Profile")
    object SpentScreen : Routes("Transaction")
    object AddScreen : Routes("Add")

    object OnBoardingScreen : Routes("Board")


}
