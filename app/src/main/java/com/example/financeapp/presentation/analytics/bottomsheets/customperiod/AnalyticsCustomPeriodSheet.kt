package com.example.financeapp.presentation.analytics.bottomsheets.customperiod

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.financeapp.R
import com.example.financeapp.core.theme.CustomPeriodActionTextStyle
import com.example.financeapp.core.theme.CustomPeriodDateTextStyle
import com.example.financeapp.core.theme.CustomPeriodTitleTextStyle
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinanceBackIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale as JavaLocale

@Composable
internal fun AnalyticsCustomPeriodSheet(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onCancelClick: () -> Unit,
    onApplyClick: (LocalDate, LocalDate) -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val locale = Locale.current.toJavaLocale()
    val maxSelectableDate = remember { LocalDate.now() }
    var firstVisibleMonth by remember(initialStartDate) {
        mutableStateOf(YearMonth.from(initialStartDate))
    }
    var startDate by remember(initialStartDate) { mutableStateOf(initialStartDate) }
    var endDate by remember(initialEndDate) { mutableStateOf(initialEndDate) }
    var isSelectingEndDate by remember(initialStartDate, initialEndDate) {
        mutableStateOf(false)
    }

    fun selectDate(date: LocalDate) {
        if (date.isAfter(maxSelectableDate)) return

        if (!isSelectingEndDate) {
            startDate = date
            endDate = date
            isSelectingEndDate = true
        } else if (date.isBefore(startDate)) {
            endDate = startDate
            startDate = date
            isSelectingEndDate = false
        } else {
            endDate = date
            isSelectingEndDate = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(sizing.analyticsCustomPeriodSheetMaxHeightFraction)
            .padding(bottom = spacing.xl)
    ) {
        Text(
            modifier = Modifier.padding(
                start = spacing.customPeriodTitleHorizontal,
                end = spacing.customPeriodTitleHorizontal,
                bottom = spacing.customPeriodTitleBottom
            ),
            text = stringResource(R.string.analytics_custom_period_title),
            style = CustomPeriodTitleTextStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(sizing.customPeriodDateRowHeight)
                .padding(horizontal = spacing.customPeriodDateHorizontal),
            horizontalArrangement = Arrangement.spacedBy(spacing.customPeriodDateGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateRangePill(
                text = startDate.formatAnalyticsDate(locale),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(sizing.dateRangeDividerWidth)
                    .height(sizing.dateRangeDividerHeight)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            DateRangePill(
                text = endDate.formatAnalyticsDate(locale),
                modifier = Modifier.weight(1f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.customPeriodCalendarGap)
        ) {
            CalendarMonthSection(
                month = firstVisibleMonth.formatAnalyticsMonth(locale),
                yearMonth = firstVisibleMonth,
                locale = locale,
                startDate = startDate,
                endDate = endDate,
                maxSelectableDate = maxSelectableDate,
                onPreviousClick = { firstVisibleMonth = firstVisibleMonth.minusMonths(1) },
                onNextClick = { firstVisibleMonth = firstVisibleMonth.plusMonths(1) },
                onDayClick = ::selectDate
            )
            CalendarMonthSection(
                month = firstVisibleMonth.plusMonths(1).formatAnalyticsMonth(locale),
                yearMonth = firstVisibleMonth.plusMonths(1),
                locale = locale,
                startDate = startDate,
                endDate = endDate,
                maxSelectableDate = maxSelectableDate,
                onDayClick = ::selectDate
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.customPeriodActionsHorizontal,
                    end = spacing.customPeriodActionsHorizontal,
                    top = spacing.customPeriodActionsTop
                ),
            horizontalArrangement = Arrangement.spacedBy(
                space = spacing.customPeriodActionsGap,
                alignment = Alignment.End
            ),
            verticalAlignment = Alignment.Top
        ) {
            CustomPeriodActionButton(
                text = stringResource(R.string.picker_cancel),
                onClick = onCancelClick,
                minWidth = sizing.customPeriodCancelButtonMinWidth,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                contentHorizontalPadding = spacing.md
            )
            CustomPeriodActionButton(
                text = stringResource(R.string.picker_apply),
                onClick = { onApplyClick(startDate, endDate) },
                minWidth = sizing.customPeriodApplyButtonMinWidth,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                contentHorizontalPadding = spacing.customPeriodActionsHorizontal
            )
        }
    }
}

@Composable
private fun CustomPeriodActionButton(
    text: String,
    onClick: () -> Unit,
    minWidth: Dp,
    containerColor: Color,
    contentColor: Color,
    contentHorizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current
    val shape = RoundedCornerShape(sizing.customPeriodApplyButtonCorner)

    Surface(
        modifier = modifier
            .widthIn(min = minWidth)
            .height(sizing.customPeriodActionButtonHeight)
            .clip(shape)
            .clickable(onClick = onClick),
        color = containerColor,
        shape = shape
    ) {
        Box(
            modifier = Modifier.padding(horizontal = contentHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = CustomPeriodActionTextStyle,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DateRangePill(
    text: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Surface(
        modifier = modifier.height(sizing.dateRangePillHeight),
        shape = RoundedCornerShape(sizing.dateRangePillCorner),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = CustomPeriodDateTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarMonthSection(
    month: String,
    yearMonth: YearMonth,
    locale: JavaLocale,
    startDate: LocalDate,
    endDate: LocalDate,
    maxSelectableDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.s)
    ) {
        CalendarMonthHeader(
            month = month,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick
        )
        CalendarMonthGrid(
            month = yearMonth,
            locale = locale,
            startDate = startDate,
            endDate = endDate,
            maxSelectableDate = maxSelectableDate,
            onDayClick = onDayClick
        )
    }
}

@Composable
private fun CalendarMonthHeader(
    month: String,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalSizing.current.customPeriodCalendarHeaderHeight)
            .padding(
                horizontal = LocalSpacing.current.customPeriodCalendarHeaderHorizontal,
                vertical = LocalSpacing.current.customPeriodCalendarHeaderVertical
            ),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = LocalSizing.current.selectionIndicator),
            text = month,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onPreviousClick != null) {
            CalendarArrowButton(
                onClick = onPreviousClick,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        if (onNextClick != null) {
            CalendarArrowButton(
                onClick = onNextClick,
                modifier = Modifier.align(Alignment.CenterEnd),
                iconModifier = Modifier.graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Composable
private fun CalendarArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(LocalSizing.current.selectionIndicator)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FinanceBackIcon(
            color = MaterialTheme.colorScheme.outline,
            modifier = iconModifier.size(LocalSizing.current.selectionIndicatorInner)
        )
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    locale: JavaLocale,
    startDate: LocalDate,
    endDate: LocalDate,
    maxSelectableDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        CalendarWeekDaysRow(locale = locale)
        CalendarWeeksGrid(
            weeks = month.buildCalendarCells().chunked(WeekDaysCount),
            startDate = startDate,
            endDate = endDate,
            maxSelectableDate = maxSelectableDate,
            onDayClick = onDayClick
        )
    }
}

@Composable
private fun CalendarWeekDaysRow(locale: JavaLocale) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.customPeriodWeekDaysHeight)
            .padding(horizontal = spacing.customPeriodCalendarGridHorizontal)
    ) {
        WeekDays.forEach { dayOfWeek ->
            Text(
                modifier = Modifier.weight(1f),
                text = dayOfWeek.localizedShortName(locale),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarWeeksGrid(
    weeks: List<List<LocalDate?>>,
    startDate: LocalDate,
    endDate: LocalDate,
    maxSelectableDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    weeks.forEach { week ->
        CalendarWeekRow(
            week = week,
            startDate = startDate,
            endDate = endDate,
            maxSelectableDate = maxSelectableDate,
            onDayClick = onDayClick
        )
    }
}

@Composable
private fun CalendarWeekRow(
    week: List<LocalDate?>,
    startDate: LocalDate,
    endDate: LocalDate,
    maxSelectableDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.customPeriodCalendarGridHorizontal)
    ) {
        week.forEach { date ->
            val isEnabled = date != null && !date.isAfter(maxSelectableDate)
            CalendarDayCell(
                modifier = Modifier.weight(1f),
                date = date,
                isInRange = date != null && !date.isBefore(startDate) && !date.isAfter(endDate),
                isStart = date == startDate,
                isEnd = date == endDate,
                isEnabled = isEnabled,
                onClick = { if (date != null) onDayClick(date) }
            )
        }
    }
}

private fun YearMonth.buildCalendarCells(): List<LocalDate?> {
    val leadingEmptyDays = atDay(1).dayOfWeek.value - 1
    val dates = (1..lengthOfMonth()).map { day -> atDay(day) }
    val trailingEmptyDays = (WeekDaysCount - (leadingEmptyDays + dates.size) % WeekDaysCount) % WeekDaysCount

    return List(leadingEmptyDays) { null } + dates + List(trailingEmptyDays) { null }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isInRange: Boolean,
    isStart: Boolean,
    isEnd: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current
    val rangeColor = MaterialTheme.colorScheme.secondaryContainer
    val selectedColor = MaterialTheme.colorScheme.primary
    val isEdge = isStart || isEnd

    Box(
        modifier = modifier
            .height(sizing.calendarDayHeight)
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            if (isInRange && !isEdge) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(rangeColor)
                )
            } else if (isStart && isEnd) {
                Unit
            } else if (isStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = sizing.calendarSelectedDay / 2)
                        .background(rangeColor)
                )
            } else if (isEnd) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = sizing.calendarSelectedDay / 2)
                        .background(rangeColor)
                )
            }
            Box(
                modifier = Modifier
                    .size(sizing.calendarSelectedDay)
                    .clip(CircleShape)
                    .background(
                        if (isEdge) {
                            selectedColor
                        } else {
                            Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = CustomPeriodDateTextStyle,
                    color = if (isEdge) {
                        MaterialTheme.colorScheme.onPrimary
                    } else if (!isEnabled) {
                        MaterialTheme.colorScheme.outline.copy(alpha = DisabledDateAlpha)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun LocalDate.formatAnalyticsDate(locale: JavaLocale): String {
    return format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
}

private fun YearMonth.formatAnalyticsMonth(locale: JavaLocale): String {
    return format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
        .replaceFirstChar { firstChar -> firstChar.titlecase(locale) }
}

private fun DayOfWeek.localizedShortName(locale: JavaLocale): String {
    return getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        .replaceFirstChar { firstChar -> firstChar.titlecase(locale) }
}

private fun Locale.toJavaLocale(): JavaLocale {
    return JavaLocale.forLanguageTag(toLanguageTag())
}

private val WeekDays = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)

private const val WeekDaysCount = 7
private const val DisabledDateAlpha = 0.38f
