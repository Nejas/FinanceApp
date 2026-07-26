package com.example.financeapp.presentation.bottomSheets.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSheetPrimaryButton

@Composable
fun FinanceSingleTextInputBottomSheet(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    supportingText: String? = null
) {
    val spacing = LocalSpacing.current

    FinanceModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.sheetTitleHorizontal,
                    end = spacing.sheetTitleHorizontal,
                    bottom = spacing.sheetButtonBottom
                )
        ) {
            Text(
                modifier = Modifier.padding(vertical = spacing.sheetTitleVertical),
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = capitalization,
                    keyboardType = keyboardType
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = spacing.s),
                color = if (supportingText == null) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
                thickness = spacing.hairline
            )
            supportingText?.let { message ->
                Text(
                    modifier = Modifier.padding(top = spacing.s),
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            FinanceSheetPrimaryButton(
                text = stringResource(R.string.done),
                onClick = onConfirmClick,
                modifier = Modifier.padding(top = spacing.sheetButtonTop)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun FinanceSingleTextInputBottomSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceSingleTextInputBottomSheet(
            title = "Название",
            value = "Основной счет",
            onValueChange = {},
            onConfirmClick = {},
            onDismissRequest = {}
        )
    }
}
