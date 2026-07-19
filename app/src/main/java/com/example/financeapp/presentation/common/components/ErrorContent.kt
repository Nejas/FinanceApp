package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun ErrorContent(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

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
            modifier = Modifier.padding(bottom = spacing.sm)
        )
        Text(
            text = stringResource(R.string.load_error_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.load_error_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.contentGap, bottom = spacing.actionGap)
        )
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorContentPreview() {
    FinanceAppTheme(dynamicColor = false) {
        ErrorContent(onRetryClick = {})
    }
}
