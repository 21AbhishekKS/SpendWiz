package com.spendwiz.app.navigation

sealed class Routes( val route : String) {

    object SplashScreen : Routes("Splash")


    object HomeScreen : Routes("Home")
    object NotificationSettingsScreen : Routes("NotificationSettingsScreen")

    object ManageCategoriesScreen : Routes("ManageCategoriesScreen")
    object BottomNav : Routes("BottomNav")

    object InsightsScreen : Routes("Insights")
    object BackupRestoreScreen : Routes("BackupRestoreScreen")
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

    object BulkUpdateScreen : Routes("BulkUpdate") {
        fun createRoute(description: String, category: String, subCategory: String?): String {
            val encodedDesc = java.net.URLEncoder.encode(
                description,
                java.nio.charset.StandardCharsets.UTF_8.toString()
            )
            val encodedCategory = java.net.URLEncoder.encode(
                category,
                java.nio.charset.StandardCharsets.UTF_8.toString()
            )
            val encodedSubCategory = java.net.URLEncoder.encode(
                subCategory ?: "",
                java.nio.charset.StandardCharsets.UTF_8.toString()
            )
            return "$route/$encodedDesc/$encodedCategory/$encodedSubCategory"
        }
    }


    object SmartSettings : Routes("SmartSettings")


}
