package com.example.financeapp.presentation.common.placeholders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun ErrorContent(
    error: ScreenError,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = spacing.sm)
                .size(sizing.listItemIcon)
        )
        Text(
            text = stringResource(error.titleResId),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(error.descriptionResId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.contentGap, bottom = spacing.actionGap)
        )
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

private val ScreenError.titleResId: Int
    get() = when (this) {
        ScreenError.NO_INTERNET -> R.string.no_internet_error_title
        ScreenError.SERVER_ERROR -> R.string.server_error_title
        ScreenError.TIMEOUT -> R.string.timeout_error_title
        ScreenError.LOAD_FAILED -> R.string.load_error_title
    }

private val ScreenError.descriptionResId: Int
    get() = when (this) {
        ScreenError.NO_INTERNET -> R.string.no_internet_error_description
        ScreenError.SERVER_ERROR -> R.string.server_error_description
        ScreenError.TIMEOUT -> R.string.timeout_error_description
        ScreenError.LOAD_FAILED -> R.string.load_error_description
    }

@Preview(showBackground = true)
@Composable
private fun ErrorContentPreview() {
    FinanceAppTheme(dynamicColor = false) {
        ErrorContent(
            error = ScreenError.LOAD_FAILED,
            onRetryClick = {}
        )
    }
}
