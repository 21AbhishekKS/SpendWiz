package com.abhi.expencetracker.navigation

sealed class Routes( val route : String) {
    object HomeScreen : Routes("Home")
    object TransferScreen : Routes("Trans")

    object InsightsScreen : Routes("Insights")
    object SpentScreen : Routes("History")
    object AddScreen : Routes("Add")
    object UpdateScreen : Routes("Update")

    object OnBoardingScreen : Routes("Board")


}
