package com.example.financeapp.presentation.common.model

import androidx.annotation.StringRes
import com.example.financeapp.R
import com.example.financeapp.domain.model.Currency

data class CurrencyUiModel(
    val currency: Currency,
    val flag: String,
    @StringRes val nameResId: Int
)

fun Currency.toUiModel(): CurrencyUiModel {
    return when (this) {
        Currency.RUB -> CurrencyUiModel(
            currency = this,
            flag = "🇷🇺",
            nameResId = R.string.currency_rub_name
        )
        Currency.USD -> CurrencyUiModel(
            currency = this,
            flag = "🇺🇸",
            nameResId = R.string.currency_usd_name
        )
        Currency.EUR -> CurrencyUiModel(
            currency = this,
            flag = "🇪🇺",
            nameResId = R.string.currency_eur_name
        )
        Currency.GBP -> CurrencyUiModel(
            currency = this,
            flag = "🇬🇧",
            nameResId = R.string.currency_gbp_name
        )
        Currency.CNY -> CurrencyUiModel(
            currency = this,
            flag = "🇨🇳",
            nameResId = R.string.currency_cny_name
        )
    }
}
