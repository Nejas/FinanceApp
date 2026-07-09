package com.example.financeapp.presentation.common.model

import androidx.compose.ui.graphics.vector.ImageVector

data class FinanceListItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val leadingIcon: ImageVector,
    val trailingText: String,
    val trailingDescription: String? = null
)