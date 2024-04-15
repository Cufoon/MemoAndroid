package cufoon.memo.android.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cufoon.memo.android.data.model.DailyUsageStat
import java.time.LocalDate


@Composable
fun HeatmapStat(day: DailyUsageStat) {
    val borderWidth = if (day.date == LocalDate.now()) 1.dp else 0.dp
    val color = when (day.count) {
        0 -> Color(0xFFEAEAEA)
        1 -> Color(0xFFE99BAB)
        2 -> Color(0xFFE24C6A)
        in 3..4 -> Color(0xFFC02947)
        else -> Color(0xFF880000)
    }
    var modifier = Modifier
        .fillMaxSize()
        .aspectRatio(1F, true)
        .clip(RoundedCornerShape(2.dp))
        .background(color = color)
    if (day.date == LocalDate.now()) {
        modifier = modifier.border(
            borderWidth,
            MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(2.dp)
        )
    }

    Box(modifier = modifier)
}