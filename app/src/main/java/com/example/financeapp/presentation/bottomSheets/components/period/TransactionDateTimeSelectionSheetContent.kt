package com.example.financeapp.presentation.bottomSheets.components.period

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financeapp.R
import com.example.financeapp.core.theme.CustomPeriodActionTextStyle
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.TransactionTimePickerTitleTextStyle
import com.example.financeapp.presentation.common.components.icons.FinanceCloseIcon
import com.example.financeapp.presentation.common.components.icons.FinanceClockIcon
import com.example.financeapp.presentation.common.components.icons.FinanceEditIcon
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDateSelectionScreen(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val initialSelectedDateMillis = selectedDate.toEpochMillisUtc()
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialSelectedDateMillis,
            initialSelectedEndDateMillis = null,
            initialDisplayedMonthMillis = initialSelectedDateMillis
        )
        val spacing = LocalSpacing.current
        val sizing = LocalSizing.current
        val closeDatePickerContentDescription =
            stringResource(R.string.transaction_date_picker_close)
        val datePickerContainerColor = MaterialTheme.colorScheme.surfaceVariant
        val datePickerAccentColor = MaterialTheme.colorScheme.primary
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = datePickerContainerColor,
            titleContentColor = MaterialTheme.colorScheme.outline,
            headlineContentColor = MaterialTheme.colorScheme.onSurface,
            weekdayContentColor = MaterialTheme.colorScheme.onSurface,
            subheadContentColor = MaterialTheme.colorScheme.outline,
            navigationContentColor = MaterialTheme.colorScheme.outline,
            dayContentColor = MaterialTheme.colorScheme.onSurface,
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            selectedDayContainerColor = datePickerAccentColor,
            todayContentColor = datePickerAccentColor,
            todayDateBorderColor = datePickerAccentColor,
            dividerColor = MaterialTheme.colorScheme.outlineVariant
        )

        LaunchedEffect(dateRangePickerState.selectedEndDateMillis) {
            dateRangePickerState.selectedEndDateMillis?.let { selectedEndDateMillis ->
                dateRangePickerState.setSelection(
                    startDateMillis = selectedEndDateMillis,
                    endDateMillis = null
                )
            }
        }

                Column(modifier = Modifier.fillMaxSize()) {
                    DateRangePicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = dateRangePickerState,
                        title = null,
                        headline = {
                            Column {
                                IconButton(
                                    modifier = Modifier
                                        .padding(
                                            start = spacing.transactionDatePickerCloseStart,
                                            top = spacing.transactionDatePickerCloseTop
                                        )
                                        .size(sizing.topBarActionSize)
                                        .semantics {
                                            contentDescription = closeDatePickerContentDescription
                                        },
                                    onClick = onDismissRequest
                                ) {
                                    FinanceCloseIcon(
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(sizing.icon)
                                    )
                                }
                                Row( horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = spacing.s, start = spacing.transactionDatePickerHeaderTextStart, end = spacing.sm)) {
                                    Column(Modifier.height(60.dp)) {
                                        Text(
                                        text = stringResource(R.string.transaction_date_picker_title),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                        Text(
                                            text = dateRangePickerState.selectedStartDateMillis
                                                ?.toLocalDateUtc()
                                                ?.formatDatePickerHeadline()
                                                .orEmpty(),
                                            style = MaterialTheme.typography.titleLarge
                                        )}
                                    FinanceEditIcon(
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(sizing.topBarActionSize))
                                }
                            }
                        },
                        showModeToggle = false,
                        colors = datePickerColors
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = spacing.hairline
                    )
                    TransactionDatePickerActions(
                        containerColor = datePickerContainerColor,
                        contentColor = datePickerAccentColor,
                        onCancelClick = onDismissRequest,
                        onApplyClick = {
                            dateRangePickerState.selectedStartDateMillis
                                ?.toLocalDateUtc()
                                ?.let(onDateSelected)
                        }
                    )

        }
    }
}

@Composable
private fun TransactionDatePickerActions(
    containerColor: Color,
    contentColor: Color,
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionDatePickerActionBarHeight)
            .background(containerColor)
            .padding(horizontal = spacing.transactionDatePickerActionsHorizontal),
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.transactionDatePickerActionsGap,
            alignment = Alignment.End
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancelClick) {
            Text(
                text = stringResource(R.string.picker_cancel),
                style = CustomPeriodActionTextStyle,
                color = contentColor
            )
        }
        TextButton(onClick = onApplyClick) {
            Text(
                text = stringResource(R.string.picker_apply),
                style = CustomPeriodActionTextStyle,
                color = contentColor
            )
        }
    }
}

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

private fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun LocalDate.formatDatePickerHeadline(): String =
    format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionTimeSelectionSheetContent(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismissRequest: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )
    val timePickerContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val timePickerColors = TimePickerDefaults.colors(
        containerColor = timePickerContainerColor,
        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionTimePickerSheetHeight),
        color = timePickerContainerColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing.transactionTimePickerContentGap)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.transactionTimePickerHeaderHeight)
                    .padding(
                        start = spacing.transactionTimePickerHeaderHorizontal,
                        top = spacing.transactionTimePickerHeaderTop,
                        end = spacing.transactionTimePickerHeaderHorizontal
                    )
            ) {
                Text(
                    text = stringResource(R.string.transaction_time_picker_title),
                    style = TransactionTimePickerTitleTextStyle,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.transactionTimePickerInputHeight)
                    .padding(
                        start = spacing.transactionTimePickerInputHorizontal,
                        end = spacing.transactionTimePickerInputHorizontal
                    ),
                contentAlignment = Alignment.Center
            ) {
                TimeInput(
                    state = timePickerState,
                    colors = timePickerColors
                )
            }
            TransactionTimePickerActions(
                onCancelClick = onDismissRequest,
                onApplyClick = {
                    onTimeSelected(
                        LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun TransactionTimePickerActions(
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionTimePickerActionsHeight)
            .padding(
                start = spacing.transactionTimePickerActionsStart,
                end = spacing.transactionTimePickerActionsEnd,
                bottom = spacing.transactionTimePickerActionsBottom
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(sizing.topBarActionSize),
            contentAlignment = Alignment.Center
        ) {
            FinanceClockIcon(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(sizing.icon)
            )
        }
        Row(
            modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight),
            horizontalArrangement = Arrangement.spacedBy(
                space = spacing.transactionTimePickerActionsGap,
                alignment = Alignment.End
            )
        ) {
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.picker_cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            TextButton(
                onClick = onApplyClick,
                modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.picker_apply),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
