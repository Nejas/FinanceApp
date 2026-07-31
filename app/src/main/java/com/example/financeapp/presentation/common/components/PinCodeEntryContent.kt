package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.testing.FinanceTestTags
import kotlinx.coroutines.delay

@Composable
fun PinCodeEntryContent(
    value: String,
    message: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    focusRequestKey: Any? = null,
    footer: @Composable (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    val pinCodeColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.pinCodeContentVertical),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.pinCodeContentGap)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outline
            },
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        PinCodeInput(
            value = value,
            indicatorColor = pinCodeColor,
            isEnabled = isEnabled,
            focusRequestKey = focusRequestKey,
            onValueChange = onValueChange
        )

        footer?.invoke()
    }
}

@Composable
fun PinCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    isEnabled: Boolean = true,
    focusRequestKey: Any? = null
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequester, isEnabled, focusRequestKey) {
        if (isEnabled) {
            delay(PinKeyboardShowDelayMillis)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (isEnabled) {
                onValueChange(newValue.filter(Char::isDigit).take(PinCodeLength))
            }
        },
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = TextStyle(color = Color.Transparent),
        modifier = modifier
            .testTag(FinanceTestTags.PinCodeInput)
            .focusRequester(focusRequester)
            .clickable(enabled = isEnabled) {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        decorationBox = {
            PinCodeDotsIndicator(
                filledCount = value.length,
                color = indicatorColor
            )
        }
    )
}

private const val PinKeyboardShowDelayMillis = 100L
