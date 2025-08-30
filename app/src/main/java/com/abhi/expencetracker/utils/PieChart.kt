package com.abhi.expencetracker.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aay.compose.donutChart.PieChart
import com.aay.compose.donutChart.model.PieChartData

@Composable
fun PieChart(
    spent: Double,
    earned: Double,
    modifier: Modifier = Modifier
) {
        val testPieChartData: List<PieChartData> = listOf(
            PieChartData(
                partName = "Spent: $spent",
                data = spent,
                color = Color(0xFFF24C3D),
            ),
            PieChartData(
                partName = "Earned: $earned",
                data = earned,
                color = Color(0xFF22A699),
            )
        )
        Column(Modifier.padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally ,
            verticalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = "Income vs Spending",
                color = Color.Black, // ensure visible
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            PieChart(
                modifier = Modifier.fillMaxSize(),
                pieChartData = testPieChartData,
                ratioLineColor = Color.Gray,
                textRatioStyle = TextStyle(color = Color.Black),
            )
        }

}