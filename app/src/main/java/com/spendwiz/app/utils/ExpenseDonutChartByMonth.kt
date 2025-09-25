package com.spendwiz.app.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aay.compose.donutChart.DonutChart
import com.spendwiz.app.ViewModels.AddScreenViewModel

@Composable
fun ExpenseDonutChartByMonth(viewModel: AddScreenViewModel, month: String, year: String , modifier: Modifier) {
    val pieData by viewModel.getCategoryExpensesForMonth(month, year).observeAsState(emptyList())

    if (pieData.isNotEmpty()) {
        Column(
            Modifier.padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = "Detailed Breakdown of Expenses",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            DonutChart(
                modifier = modifier,
                pieChartData = pieData,
                centerTitle = "$month/$year",
                centerTitleStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                outerCircularColor = MaterialTheme.colorScheme.surfaceVariant,
                innerCircularColor = MaterialTheme.colorScheme.outline,
                ratioLineColor = MaterialTheme.colorScheme.outlineVariant,
                descriptionStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textRatioStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                )

        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No expenses for $month/$year",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}