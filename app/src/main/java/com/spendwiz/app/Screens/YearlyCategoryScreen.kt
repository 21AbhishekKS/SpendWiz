package com.spendwiz.app.Screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.ViewModels.CategoryData
import com.spendwiz.app.ViewModels.SubCategoryData
import com.spendwiz.app.Database.money.TransactionType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun YearlyCategoryScreen(
    year: String,
    type: TransactionType,
    vm: AddScreenViewModel = viewModel()
) {
    val categories = vm.getYearlyCategoryData(year, type).observeAsState(emptyList()).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Heading
        Text(
            text = "$type by Category ($year)",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Staggered Grid (like Google Keep / Pinterest)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2), // 2 columns (auto stagger)
            modifier = Modifier.fillMaxSize(),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category)
            }
        }
    }
}

@Composable
fun CategoryCard(category: CategoryData) {
    val name = category.name
    val amount = formatAmount(category.total)

    val nameStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    )
    val amountStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color(0xFFBBDEFB), MaterialTheme.shapes.medium), // light blue border
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title Row with adaptive layout
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val availablePx = with(density) { maxWidth.toPx() }
                val nameLayout = textMeasurer.measure(AnnotatedString(name), style = nameStyle)
                val amountLayout = textMeasurer.measure(AnnotatedString(amount), style = amountStyle)
                val spacingPx = with(density) { 8.dp.toPx() }

                val fitsOnOneLine =
                    nameLayout.size.width + amountLayout.size.width + spacingPx <= availablePx

                if (fitsOnOneLine) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = nameStyle,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = amount,
                            style = amountStyle,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = name,
                            style = nameStyle,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                        Text(
                            text = amount,
                            style = amountStyle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Subcategories
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                category.subCategories.forEach { sub ->
                    SubCategoryRow(sub)
                }
            }
        }
    }
}

@Composable
fun SubCategoryRow(sub: SubCategoryData) {
    val name = sub.name
    val amount = formatAmount(sub.amount) // your previously added formatter

    val nameStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    )
    val amountStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // available width in pixels
        val availablePx = with(density) { maxWidth.toPx() }

        // measure name and amount widths (in pixels)
        val nameLayout = textMeasurer.measure(AnnotatedString(name), style = nameStyle)
        val amountLayout = textMeasurer.measure(AnnotatedString(amount), style = amountStyle)

        // some spacing between name and amount (px)
        val spacingPx = with(density) { 8.dp.toPx() }

        val fitsOnOneLine = nameLayout.size.width + amountLayout.size.width + spacingPx <= availablePx

        if (fitsOnOneLine) {
            // single-line style: name takes remaining space, amount on same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = nameStyle,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Text(
                    text = amount,
                    style = amountStyle,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            // fallback: name on top (can wrap), amount on the next line right aligned
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    style = nameStyle,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2 // cap lines so cards don't get too tall; adjust if needed
                )

                Text(
                    text = amount,
                    style = amountStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "₹${formatter.format(amount)}"
}
