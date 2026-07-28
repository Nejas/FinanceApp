package com.example.financeapp.presentation.bottomSheets.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.FinanceActionButton
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.base.TextOvalFrame
import com.example.financeapp.presentation.common.components.icons.FinanceAccountCardIcon
import com.example.financeapp.presentation.common.components.icons.FinanceArticleIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCalendarIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCurrencyIcon

@Composable
fun FinanceTextInputBottomSheet(
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    FinanceModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FinanceAmountInput(
                    value = value,
                    suffix = suffix,
                    onValueChange = onValueChange,
                    title = title,
                    supportingText = supportingText
                )
                content()
                Spacer(modifier = Modifier.height(43.dp))
            }

            FinanceConfirmButton(
                onClick = onConfirmClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = LocalSpacing.current.md,
                        bottom = LocalSpacing.current.sheetFloatingButtonBottom
                    )
            )
        }
    }
}

@Composable
private fun FinanceAmountInput(
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
    title: String?,
    supportingText: String?,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = spacing.s,
                bottom = spacing.lg
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title != null) {
            Text(
                modifier = Modifier.padding(bottom = spacing.lg),
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .widthIn(min = sizing.analyticsFilterValueMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue.normalizedIntegerInput())
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = AmountSuffixVisualTransformation(suffix),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                }
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(top = spacing.xxs)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = spacing.hairline
            )
        }

        supportingText?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = spacing.s),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

private class AmountSuffixVisualTransformation(
    private val suffix: String
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val rawValue = text.text
        val displayValue = rawValue.ifBlank { EMPTY_AMOUNT_VALUE }
        val transformedText = if (suffix.isBlank()) {
            displayValue
        } else {
            "$displayValue $suffix"
        }

        return TransformedText(
            text = AnnotatedString(transformedText),
            offsetMapping = AmountSuffixOffsetMapping(
                originalLength = rawValue.length,
                transformedLength = transformedText.length
            )
        )
    }

    private companion object {
        const val EMPTY_AMOUNT_VALUE = "0"
    }
}

private class AmountSuffixOffsetMapping(
    private val originalLength: Int,
    private val transformedLength: Int
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        return if (offset >= originalLength) {
            transformedLength
        } else {
            offset
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return offset.coerceAtMost(originalLength)
    }
}

private fun String.normalizedIntegerInput(): String {
    val digits = filter(Char::isDigit)
    val normalizedDigits = digits.trimStart(LEADING_ZERO)

    return normalizedDigits.ifEmpty {
        if (digits.isEmpty()) "" else ZERO_AMOUNT_VALUE
    }
}

private const val LEADING_ZERO = '0'
private const val ZERO_AMOUNT_VALUE = "0"

@Composable
private fun FinanceConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current

    FinanceActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.onSurface,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        modifier = modifier,
        icon = {
            FinanceCheckIcon(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(sizing.icon)
            )
        }
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun FinanceTransactionTextInputBottomSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceTextInputBottomSheet(
            value = "214",
            suffix = "₽",
            onValueChange = {},
            onConfirmClick = {},
            onDismissRequest = {}
        ) {
            FinanceSelectionRow(
                title = "Статья",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                leadingContent = {
                    RoundFrame(content = {
                        FinanceArticleIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "Ремонт")
                }
            )
            FinanceSelectionRow(
                title = "Дата",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                leadingContent = {
                    RoundFrame(content = {
                        FinanceCalendarIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "20 февраля")
                }
            )
            FinanceSelectionRow(
                title = "Время",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                leadingContent = {
                    RoundFrame(content = {
                        FinanceCalendarIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "23:42")
                }
            )
            FinanceSelectionRow(
                title = "Счёт",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                showDivider = false,
                leadingContent = {
                    RoundFrame(content = {
                        FinanceAccountCardIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "Сбер")
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun FinanceAccountTextInputBottomSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceTextInputBottomSheet(
            title = "Корректировка баланса",
            value = "",
            suffix = "₽",
            onValueChange = {},
            onConfirmClick = {},
            onDismissRequest = {}
        ) {
            FinanceSelectionRow(
                title = "Дата",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                leadingContent = {
                    RoundFrame(content = {
                        FinanceCalendarIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "20 февраля")
                }
            )
            FinanceSelectionRow(
                title = "Время",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                leadingContent = {
                    RoundFrame(content = {
                        FinanceCalendarIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "23:42")
                }
            )
            FinanceSelectionRow(
                title = "Валюта",
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {},
                showDivider = false,
                leadingContent = {
                    RoundFrame(content = {
                        FinanceCurrencyIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(LocalSizing.current.icon)
                        )
                    })
                },
                trailingContent = {
                    TextOvalFrame(text = "Руб.")
                }
            )
        }
    }
}
