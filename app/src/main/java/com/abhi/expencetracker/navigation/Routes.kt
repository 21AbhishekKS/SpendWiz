package com.abhi.expencetracker.navigation

sealed class Routes( val route : String) {

    object SplashScreen : Routes("Splash")


    object HomeScreen : Routes("Home")
    object NotificationSettingsScreen : Routes("NotificationSettingsScreen")

    object ManageCategoriesScreen : Routes("ManageCategoriesScreen")
    object BottomNav : Routes("BottomNav")

    object InsightsScreen : Routes("Insights")
    object More : Routes("More")
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

    object IncomeDetailsScreen : Routes("incomeDetails/{year}") {
        fun createRoute(year: String) = "incomeDetails/$year"
    }
    object ExpenseDetailsScreen : Routes("expenseDetails/{year}") {
        fun createRoute(year: String) = "expenseDetails/$year"
    }


    object SmartSettings : Routes("SmartSettings")


}
