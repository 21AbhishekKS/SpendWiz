package com.abhi.expencetracker.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.aay.compose.donutChart.PieChart
import com.aay.compose.donutChart.model.PieChartData

@Composable
fun PieChart(
    spent: Double,
    earned: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
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

        PieChart(
            modifier = Modifier.fillMaxSize(),
            pieChartData = testPieChartData,
            ratioLineColor = Color.Gray,
            textRatioStyle = TextStyle(color = Color.Black),
        )
    }
}