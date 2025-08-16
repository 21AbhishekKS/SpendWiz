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
    object UpdateScreen : Routes("Update") {
        fun createRoute(id: Int, description: String, amount: Double, type: String): String {
            val encodedDesc = java.net.URLEncoder.encode(
                description,
                java.nio.charset.StandardCharsets.UTF_8.toString()
            )
            return "$route/$id/$encodedDesc/$amount/$type"
        }
    }


    object OnBoardingScreen : Routes("Board")


}
