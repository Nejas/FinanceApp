package com.example.financeapp.presentation.common.components.base

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinanceDeleteIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDeleteListItem(
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val density = LocalDensity.current
    val deleteWidthPx = with(density) { sizing.listItemHeight.toPx() }
    val decayAnimationSpec = remember { exponentialDecay<Float>() }
    val currentOnDeleteRequest by rememberUpdatedState(onDeleteRequest)
    val swipeState = remember {
        AnchoredDraggableState(
            initialValue = SwipeToDeleteValue.Closed,
            positionalThreshold = { distance -> distance * 0.98f },
            velocityThreshold = { Float.MAX_VALUE },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    LaunchedEffect(deleteWidthPx) {
        swipeState.updateAnchors(
            DraggableAnchors {
                SwipeToDeleteValue.Closed at 0f
                SwipeToDeleteValue.Open at -deleteWidthPx
            }
        )
    }

    LaunchedEffect(swipeState.settledValue) {
        if (swipeState.settledValue == SwipeToDeleteValue.Open) {
            currentOnDeleteRequest()
            swipeState.snapTo(SwipeToDeleteValue.Closed)
        }
    }

    val offsetX = if (swipeState.offset.isNaN()) {
        0
    } else {
        swipeState.requireOffset().roundToInt()
    }

    Box(
        modifier = modifier.anchoredDraggable(
            state = swipeState,
            orientation = Orientation.Horizontal
        )
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sizing.listItemHeight)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = spacing.lg),
                contentAlignment = Alignment.CenterEnd
            ) {
                FinanceDeleteIcon(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(sizing.icon)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX, 0) }
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

private enum class SwipeToDeleteValue {
    Closed,
    Open
}
