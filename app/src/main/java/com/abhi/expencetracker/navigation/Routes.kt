package com.abhi.expencetracker.navigation

sealed class Routes( val route : String) {

    object SplashScreen : Routes("Splash")


    object HomeScreen : Routes("Home")
    object SettingScreen : Routes("Settings")

    object TransferScreen : Routes("Trans")
    object BottomNav : Routes("BottomNav")

    object InsightsScreen : Routes("Insights")
    object SpentScreen : Routes("History")
    object AddScreen : Routes("Add")
    object UpdateScreen : Routes("Update")

    object OnBoardingScreen : Routes("Board")


}
