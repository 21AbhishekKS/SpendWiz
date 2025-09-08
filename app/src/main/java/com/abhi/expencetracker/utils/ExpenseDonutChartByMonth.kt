package com.abhi.expencetracker.utils
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aay.compose.donutChart.DonutChart
import com.abhi.expencetracker.ViewModels.AddScreenViewModel

@Composable
fun ExpenseDonutChartByMonth(viewModel: AddScreenViewModel, month: String, year: String , modifier: Modifier) {
    val pieData by viewModel.getCategoryExpensesForMonth(month, year).observeAsState(emptyList())

    if (pieData.isNotEmpty()) {
        Column(Modifier.padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally ,
            verticalArrangement = Arrangement.Center) {
            Text(modifier = Modifier.padding(horizontal = 12.dp),
                text = "Detailed Breakdown of Expenses",
                color = Color.Black, // ensure visible
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

        DonutChart(
            modifier = modifier,
            pieChartData = pieData,
            centerTitle = "$month/$year",
            centerTitleStyle = TextStyle(color = Color(0xFF071952)),
            outerCircularColor = Color.LightGray,
            innerCircularColor = Color.Gray,
            ratioLineColor = Color.LightGray,
        )

        }
    } else {
        Column(
            Modifier.fillMaxSize().
            padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No expenses for $month/$year",
                style = TextStyle(color = Color.Gray)
            )
        }
    }
}
