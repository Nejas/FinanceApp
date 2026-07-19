package com.example.financeapp.presentation.common.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.JoinInner
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun FinanceCalendarIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.DateRange,
        contentDescription = "Ночной режим",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceBarChartIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.4.dp.toPx()
        drawLine(color, Offset(size.width * 0.18f, size.height * 0.82f), Offset(size.width * 0.86f, size.height * 0.82f), strokeWidth, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.28f, size.height * 0.82f), Offset(size.width * 0.28f, size.height * 0.58f), strokeWidth, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.46f, size.height * 0.82f), Offset(size.width * 0.46f, size.height * 0.42f), strokeWidth, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.82f), Offset(size.width * 0.64f, size.height * 0.24f), strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
fun FinanceSlidersIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Tune,
        contentDescription = "Настройки",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceListTypeIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
        contentDescription = "Тип",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceBackChevronIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.ArrowBackIosNew,
        contentDescription = "Ночной режим",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceReceiptIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = ExpensesNavIconPath,
            viewportWidth = 18f,
            viewportHeight = 22.01f,
            color = color,
            strokeWidth = 2f
        )
    }
}

@Composable
fun FinanceTrendingUpIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
        contentDescription = "Доходы",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinancePersonIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = AccountsNavIconPath,
            viewportWidth = 16f,
            viewportHeight = 20f,
            color = color,
            strokeWidth = 2f
        )
    }
}

@Composable
fun FinancePlusIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Add,
        contentDescription = "Добавить",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceCheckIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.52f)
            lineTo(size.width * 0.43f, size.height * 0.7f)
            lineTo(size.width * 0.78f, size.height * 0.32f)
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun FinanceBackspaceIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.28f)
            lineTo(size.width * 0.16f, size.height * 0.5f)
            lineTo(size.width * 0.28f, size.height * 0.72f)
            lineTo(size.width * 0.78f, size.height * 0.72f)
            quadraticTo(size.width * 0.88f, size.height * 0.72f, size.width * 0.88f, size.height * 0.62f)
            lineTo(size.width * 0.88f, size.height * 0.38f)
            quadraticTo(size.width * 0.88f, size.height * 0.28f, size.width * 0.78f, size.height * 0.28f)
            close()
        }
        drawPath(path, color, style = stroke)
        drawLine(color, Offset(size.width * 0.48f, size.height * 0.4f), Offset(size.width * 0.66f, size.height * 0.6f), stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.66f, size.height * 0.4f), Offset(size.width * 0.48f, size.height * 0.6f), stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
fun FinanceArticleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = ArticleIconPath,
            viewportWidth = 18f,
            viewportHeight = 18f,
            color = color,
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun FinanceTagIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = TagIconPath,
            viewportWidth = 17f,
            viewportHeight = 17f,
            color = color,
            strokeWidth = 2f
        )
    }
}
@Composable
fun FinanceAccountCardIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = AccountCardIconPath,
            viewportWidth = 18f,
            viewportHeight = 13f,
            color = color,
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun FinanceCurrencyIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSvgStrokePath(
            pathData = CurrencyIconPath,
            viewportWidth = 18f,
            viewportHeight = 13f,
            color = color,
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun FinanceSettingsCurrencyIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.JoinInner,
        contentDescription = "Валюта",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceSettingsArticleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Receipt,
        contentDescription = "Статьи",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceSettingsMoonIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Bedtime,
        contentDescription = "Ночной режим",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceThemeLightIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.WbSunny,
        contentDescription = "Светлая тема",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceThemeSystemIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.DesktopWindows,
        contentDescription = "Системная тема",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceSettingsGlobeIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Language,
        contentDescription = "Язык",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceSettingsLockIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Lock,
        contentDescription = "Пин-код",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceSettingsBiometryIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Outlined.Fingerprint,
        contentDescription = "Биометрия",
        tint = color,
        modifier = modifier
    )
}

@Composable
fun FinanceEditIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 48f
        val scaleY = size.height / 48f

        val path = Path().apply {
            moveTo(15f * scaleX, 33f * scaleY)
            lineTo(15f * scaleX, 28.75f * scaleY)
            lineTo(28.2f * scaleX, 15.575f * scaleY)
            cubicTo(
                28.4f * scaleX,
                15.3917f * scaleY,
                28.6208f * scaleX,
                15.25f * scaleY,
                28.8625f * scaleX,
                15.15f * scaleY
            )
            cubicTo(
                29.1042f * scaleX,
                15.05f * scaleY,
                29.3583f * scaleX,
                15f * scaleY,
                29.625f * scaleX,
                15f * scaleY
            )
            cubicTo(
                29.8917f * scaleX,
                15f * scaleY,
                30.15f * scaleX,
                15.05f * scaleY,
                30.4f * scaleX,
                15.15f * scaleY
            )
            cubicTo(
                30.65f * scaleX,
                15.25f * scaleY,
                30.8667f * scaleX,
                15.4f * scaleY,
                31.05f * scaleX,
                15.6f * scaleY
            )
            lineTo(32.425f * scaleX, 17f * scaleY)
            cubicTo(
                32.625f * scaleX,
                17.1833f * scaleY,
                32.7708f * scaleX,
                17.4f * scaleY,
                32.8625f * scaleX,
                17.65f * scaleY
            )
            cubicTo(
                32.9542f * scaleX,
                17.9f * scaleY,
                33f * scaleX,
                18.15f * scaleY,
                33f * scaleX,
                18.4f * scaleY
            )
            cubicTo(
                33f * scaleX,
                18.6667f * scaleY,
                32.9542f * scaleX,
                18.9208f * scaleY,
                32.8625f * scaleX,
                19.1625f * scaleY
            )
            cubicTo(
                32.7708f * scaleX,
                19.4042f * scaleY,
                32.625f * scaleX,
                19.625f * scaleY,
                32.425f * scaleX,
                19.825f * scaleY
            )
            lineTo(19.25f * scaleX, 33f * scaleY)
            lineTo(15f * scaleX, 33f * scaleY)
            close()

            moveTo(29.6f * scaleX, 19.8f * scaleY)
            lineTo(31f * scaleX, 18.4f * scaleY)
            lineTo(29.6f * scaleX, 17f * scaleY)
            lineTo(28.2f * scaleX, 18.4f * scaleY)
            lineTo(29.6f * scaleX, 19.8f * scaleY)
            close()
        }

        drawPath(path = path, color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun FinanceIconsPreview() {
    FinanceAppTheme(dynamicColor = false) {
        val spacing = LocalSpacing.current
        val color = MaterialTheme.colorScheme.primary
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                FinanceCalendarIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceBarChartIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceSlidersIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceListTypeIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceBackChevronIcon(color = color, modifier = Modifier.size(spacing.xl))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                FinanceReceiptIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceTrendingUpIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinancePersonIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinancePlusIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceCheckIcon(color = color, modifier = Modifier.size(spacing.xl))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                FinanceBackspaceIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceArticleIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceTagIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceAccountCardIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceCurrencyIcon(color = color, modifier = Modifier.size(spacing.xl))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                FinanceSettingsCurrencyIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceSettingsArticleIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceSettingsMoonIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceThemeLightIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceThemeSystemIcon(color = color, modifier = Modifier.size(spacing.xl))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                FinanceSettingsGlobeIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceSettingsLockIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceSettingsBiometryIcon(color = color, modifier = Modifier.size(spacing.xl))
                FinanceEditIcon(color = color, modifier = Modifier.size(spacing.xl))
            }
        }
    }
}

private fun DrawScope.drawSvgStrokePath(
    pathData: String,
    viewportWidth: Float,
    viewportHeight: Float,
    color: Color,
    strokeWidth: Float
) {
    val scale = minOf(size.width / viewportWidth, size.height / viewportHeight)
    val offsetX = (size.width - viewportWidth * scale) / 2f
    val offsetY = (size.height - viewportHeight * scale) / 2f
    val path = PathParser().parsePathString(pathData).toPath()
    val stroke = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    withTransform({
        translate(left = offsetX, top = offsetY)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        drawPath(path = path, color = color, style = stroke)
    }
}

private const val ExpensesNavIconPath =
    "M8.9992 15.9988V5.9989M12.9988 6.9989H6.9994C6.469 6.9989 5.9604 7.2096 5.5853 7.5847C5.2103 7.9598 4.9996 8.4685 4.9996 8.9989C4.9996 9.5293 5.2103 10.038 5.5853 10.4131C5.9604 10.7882 6.469 10.9989 6.9994 10.9989H10.999C11.5294 10.9989 12.038 11.2096 12.4131 11.5846C12.7881 11.9597 12.9988 12.4684 12.9988 12.9988C12.9988 13.5293 12.7881 14.038 12.4131 14.413C12.038 14.7881 11.5294 14.9988 10.999 14.9988H4.9996M1 1.9987C1 1.7335 1.1053 1.4791 1.2929 1.2916C1.4804 1.1041 1.7347 0.9987 1.9999 0.9987C2.2475 0.9973 2.4903 1.0667 2.6998 1.1987L3.6327 1.7987C3.8417 1.9323 4.0846 2.0032 4.3327 2.0032C4.5807 2.0032 4.8236 1.9323 5.0326 1.7987L5.9665 1.1987C6.1755 1.0651 6.4184 0.9941 6.6664 0.9941C6.9145 0.9941 7.1573 1.0651 7.3663 1.1987L8.2993 1.7987C8.5083 1.9323 8.7511 2.0032 8.9992 2.0032C9.2472 2.0032 9.4901 1.9323 9.6991 1.7987L10.632 1.1987C10.841 1.0651 11.0839 0.9941 11.332 0.9941C11.58 0.9941 11.8229 1.0651 12.0319 1.1987L12.9658 1.7987C13.1748 1.9323 13.4177 2.0032 13.6657 2.0032C13.9138 2.0032 14.1566 1.9323 14.3657 1.7987L15.2986 1.1987C15.5081 1.0667 15.7509 0.9973 15.9985 0.9987C16.2637 0.9987 16.518 1.1041 16.7055 1.2916C16.893 1.4791 16.9984 1.7335 16.9984 1.9987V19.9984C16.9984 20.2636 16.893 20.518 16.7055 20.7055C16.518 20.893 16.2637 20.9984 15.9985 20.9984C15.7509 20.9997 15.5081 20.9304 15.2986 20.7984L14.3657 20.1984C14.1566 20.0648 13.9138 19.9938 13.6657 19.9938C13.4177 19.9938 13.1748 20.0648 12.9658 20.1984L12.0319 20.7984C11.8229 20.932 11.58 21.0029 11.332 21.0029C11.0839 21.0029 10.841 20.932 10.632 20.7984L9.6991 20.1984C9.4901 20.0648 9.2472 19.9938 8.9992 19.9938C8.7511 19.9938 8.5083 20.0648 8.2993 20.1984L7.3663 20.7984C7.1573 20.932 6.9145 21.0029 6.6664 21.0029C6.4184 21.0029 6.1755 20.932 5.9665 20.7984L5.0326 20.1984C4.8236 20.0648 4.5807 19.9938 4.3327 19.9938C4.0846 19.9938 3.8417 20.0648 3.6327 20.1984L2.6998 20.7984C2.4903 20.9304 2.2475 20.9997 1.9999 20.9984C1.7347 20.9984 1.4804 20.893 1.2929 20.7055C1.1053 20.518 1 20.2636 1 19.9984V1.9987Z"

private const val AccountsNavIconPath =
    "M15.0016 19V17C15.0016 15.9391 14.5801 14.9217 13.8299 14.1716C13.0796 13.4214 12.0621 13 11.0011 13H5.0004C3.9395 13 2.9219 13.4214 2.1717 14.1716C1.4215 14.9217 1 15.9391 1 17V19M12.0012 5C12.0012 7.2091 10.2102 9 8.0008 9C5.7914 9 4.0003 7.2091 4.0003 5C4.0003 2.7909 5.7914 1 8.0008 1C10.2102 1 12.0012 2.7909 12.0012 5Z"

private const val ArticleIconPath =
    "M11.75 0.75H0.75V16.75H11.75L16.75 8.75L11.75 0.75Z"

private const val TagIconPath =
    "M7.87957 1C8.2774 1.00008 8.6589 1.15819 8.94016 1.43954L15.4687 7.96808C15.8075 8.30899 15.9976 8.77008 15.9976 9.25069C15.9976 9.73129 15.8075 10.1924 15.4687 10.5333L10.5333 15.4687C10.1924 15.8075 9.73129 15.9976 9.25069 15.9976C8.77008 15.9976 8.30899 15.8075 7.96808 15.4687L1.43954 8.94016C1.15819 8.6589 1.00008 8.2774 1 7.87957V2.50012C1 2.10227 1.15805 1.7207 1.43938 1.43938C1.7207 1.15805 2.10227 1 2.50012 1H7.87957Z"

private const val AccountCardIconPath =
    "M0.75 4.75H16.75M2.75 8.75H5.75M7.75 8.75H9.75M0.75 0.75H16.75V11.75H0.75V0.75Z"

private const val CurrencyIconPath =
    "M0.75 2.75H16.75M0.75 0.75H16.75V11.75H0.75V0.75ZM10.75 7.25C10.75 8.078 11.422 8.75 12.25 8.75C13.078 8.75 13.75 8.078 13.75 7.25C13.75 6.422 13.078 5.75 12.25 5.75C11.422 5.75 10.75 6.422 10.75 7.25Z"
