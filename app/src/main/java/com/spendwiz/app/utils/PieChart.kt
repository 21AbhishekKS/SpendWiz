package com.spendwiz.app.utils

import androidx.compose.foundation.layout.Arrangement
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Income vs Spending",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        PieChart(
            modifier = Modifier.fillMaxSize(),
            pieChartData = testPieChartData,
            ratioLineColor = MaterialTheme.colorScheme.outline,
            textRatioStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            descriptionStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}