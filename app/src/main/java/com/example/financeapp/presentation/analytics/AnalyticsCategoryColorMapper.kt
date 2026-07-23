package com.example.financeapp.presentation.analytics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.financeapp.core.theme.AnalyticsChartPalette
import javax.inject.Inject

class AnalyticsCategoryColorMapper @Inject constructor() {

    fun map(categories: List<AnalyticsCategoryUi>): Map<Long, Color> {
        return categories.associate { category ->
            category.categoryId to colorFor(category.categoryId)
        }
    }

    private fun colorFor(categoryId: Long): Color {
        return colorByPaletteIndex(categoryId.toStablePaletteIndex())
    }

    private fun colorByPaletteIndex(index: Int): Color {
        val paletteSize = AnalyticsChartPalette.size
        val baseColor = AnalyticsChartPalette[index % paletteSize]
        val paletteRound = index / paletteSize
        if (paletteRound == 0) return baseColor

        val toneStep = ((paletteRound + 1) / 2)
        val fraction = (AnalyticsToneStep * toneStep).coerceAtMost(MaxAnalyticsToneShift)
        val targetColor = if (paletteRound % 2 == 1) Color.White else Color.Black
        return lerp(baseColor, targetColor, fraction)
    }

    private fun Long.toStablePaletteIndex(): Int {
        val mixed = this xor (this ushr 32)
        val positiveIndex = (mixed and Int.MAX_VALUE.toLong()).toInt()
        return if (positiveIndex == 0) 0 else positiveIndex - 1
    }

    private companion object {
        const val AnalyticsToneStep = 0.12f
        const val MaxAnalyticsToneShift = 0.45f
    }
}
