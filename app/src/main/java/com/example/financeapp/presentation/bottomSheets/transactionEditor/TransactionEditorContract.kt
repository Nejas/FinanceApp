package com.example.financeapp.presentation.bottomSheets.transactionEditor

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.R
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.common.model.FinanceFieldIconType
import com.example.financeapp.presentation.common.model.FinanceFieldUi
import com.example.financeapp.presentation.common.placeholders.ScreenError
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class TransactionEditorHostState(
    val form: TransactionEditorState? = null
)

@Immutable
data class TransactionEditorState(
    val mode: TransactionEditorMode,
    val amount: String = "",
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<Account> = emptyList(),
    val activeField: TransactionEditorField? = null,
    val comment: String? = null,
    @StringRes val formMessageResId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: ScreenError? = null
) {
    val transactionType: TransactionType
        get() = mode.transactionType

    val effectiveAccount: Account?
        get() = selectedAccount ?: if (mode is TransactionEditorMode.Create) {
            availableAccounts.firstOrNull()
        } else {
            null
        }

    val currency: Currency?
        get() = effectiveAccount?.balance?.currency

    val canConfirm: Boolean
        get() = amount.toBigDecimalOrNull()?.signum() == 1 &&
            selectedCategory != null &&
            effectiveAccount != null &&
            !isLoading &&
            !isSaving
}

@Immutable
sealed interface TransactionEditorMode {
    val transactionType: TransactionType

    data class Create(
        override val transactionType: TransactionType
    ) : TransactionEditorMode

    data class Edit(
        val transactionId: Long,
        override val transactionType: TransactionType
    ) : TransactionEditorMode
}

@Immutable
sealed interface TransactionEditorField : FinanceFieldUi {
    data object Category : TransactionEditorField {
        override val titleResId: Int = R.string.editor_category
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Article
    }

    data object Date : TransactionEditorField {
        override val titleResId: Int = R.string.editor_date
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Calendar
    }

    data object Time : TransactionEditorField {
        override val titleResId: Int = R.string.editor_time
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Clock
    }

    data object Account : TransactionEditorField {
        override val titleResId: Int = R.string.editor_account
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Account
    }

    data object Description : TransactionEditorField {
        override val titleResId: Int = R.string.editor_description
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Article
    }
}

@Immutable
sealed interface TransactionEditorIntent {
    data class Open(val mode: TransactionEditorMode) : TransactionEditorIntent
    data class AmountChanged(val amount: String) : TransactionEditorIntent
    data class FieldClicked(val field: TransactionEditorField) : TransactionEditorIntent
    data object FieldDismissed : TransactionEditorIntent
    data class CategorySelected(val categoryId: Long) : TransactionEditorIntent
    data class DateChanged(val date: LocalDate) : TransactionEditorIntent
    data class TimeChanged(val time: LocalTime) : TransactionEditorIntent
    data class AccountSelected(val accountId: Long) : TransactionEditorIntent
    data class CommentChanged(val comment: String) : TransactionEditorIntent
    data object ConfirmClicked : TransactionEditorIntent
    data object DismissRequested : TransactionEditorIntent
    data object RetryClicked : TransactionEditorIntent
}

@Immutable
sealed interface TransactionEditorEffect {
    data class Saved(val transactionId: Long) : TransactionEditorEffect
    data object Close : TransactionEditorEffect
}

val TransactionEditorFields: List<TransactionEditorField> = listOf(
    TransactionEditorField.Category,
    TransactionEditorField.Date,
    TransactionEditorField.Time,
    TransactionEditorField.Account,
    TransactionEditorField.Description
)
