package com.example.financeapp.presentation.transactionEditor

import com.example.financeapp.R
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorIntent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorMode
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorReducer
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorState
import com.example.financeapp.presentation.common.model.FinanceFieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionEditorReducerTest {

    private val reducer = TransactionEditorReducer()

    @Test
    fun amountChanged_keepsOnlyDigitsAndClearsMessage() {
        val state = state().copy(formMessageResId = R.string.transaction_save_failed)

        val result = reducer.reduce(
            state = state,
            intent = TransactionEditorIntent.AmountChanged("12a 34.5")
        )

        assertEquals("12345", result.amount)
        assertNull(result.formMessageResId)
    }

    @Test
    fun amountChanged_removesLeadingZerosButKeepsSingleZero() {
        val withAmount = reducer.reduce(
            state = state(),
            intent = TransactionEditorIntent.AmountChanged("000120")
        )
        val zeroAmount = reducer.reduce(
            state = state(),
            intent = TransactionEditorIntent.AmountChanged("000")
        )

        assertEquals("120", withAmount.amount)
        assertEquals("0", zeroAmount.amount)
    }

    @Test
    fun categorySelected_usesCategoryFromAvailableOptionsAndClosesField() {
        val category = category(id = 2)
        val state = state().copy(
            availableCategories = listOf(category),
            activeField = FinanceFieldType.Category
        )

        val result = reducer.reduce(
            state = state,
            intent = TransactionEditorIntent.CategorySelected(category.id)
        )

        assertEquals(category, result.selectedCategory)
        assertNull(result.activeField)
    }

    @Test
    fun validationMessage_requiresPositiveAmountThenCategoryAndAccount() {
        val emptyState = state()
        assertEquals(
            R.string.transaction_amount_required,
            reducer.validationMessage(emptyState)
        )

        val amountState = emptyState.copy(amount = "100")
        assertEquals(
            R.string.transaction_category_required,
            reducer.validationMessage(amountState)
        )

        val categoryState = amountState.copy(selectedCategory = category(id = 1))
        assertEquals(
            R.string.transaction_account_required,
            reducer.validationMessage(categoryState)
        )
    }

    private fun state() = TransactionEditorState(
        mode = TransactionEditorMode.Create(TransactionType.EXPENSE)
    )

    private fun category(id: Long) = Category(
        id = id,
        name = "Category $id",
        emoji = "category",
        type = TransactionType.EXPENSE
    )
}
