package com.abhi.expencetracker.navigation

sealed class Routes( val route : String) {
    object HomeScreen : Routes("Home")
    object TransferScreen : Routes("Transfer")

    object ProfileScreen : Routes("Profile")
    object SpentScreen : Routes("Spent")
    object AddScreen : Routes("Add")
}
