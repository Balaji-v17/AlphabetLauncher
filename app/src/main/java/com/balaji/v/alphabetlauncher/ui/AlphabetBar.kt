package com.balaji.v.alphabetlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

// Unified list so the Star and Dot participate in the curve animation perfectly
private val ITEMS = listOf('\u2606') + ('A'..'Z').toList() + listOf('\u25CB')
private const val INFLUENCE = 5f
private const val MAX_OFFSET_DP = 46f

@Composable
fun AlphabetBar(
    onLetterChanged: (Char?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val fingerIndex = remember { Animatable(-1f) }
    var barHeightPx by remember { mutableStateOf(0f) }
    var lastReportedItem by remember { mutableStateOf<Char?>(null) }

    fun itemAt(y: Float): Float {
        if (barHeightPx <= 0f) return 0f
        val clamped = y.coerceIn(0f, barHeightPx)
        return (clamped / barHeightPx) * (ITEMS.size - 1)
    }

    fun reportIfChanged(idx: Float) {
        val item = ITEMS[idx.roundToInt().coerceIn(0, ITEMS.lastIndex)]
        if (item != lastReportedItem) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastReportedItem = item
        }

        if (item == '\u2606' || item == '\u25CB') {
            onLetterChanged(null)
        } else {
            onLetterChanged(item)
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight() // Takes up the full height of the screen
                .padding(vertical = 48.dp) // Keeps the top and bottom safe from status/nav bars
                .onGloballyPositioned { barHeightPx = it.size.height.toFloat() }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val idx = itemAt(offset.y)
                            scope.launch { fingerIndex.snapTo(idx) }
                            reportIfChanged(idx)
                        },
                        onVerticalDrag = { change, _ ->
                            val idx = itemAt(change.position.y)
                            scope.launch { fingerIndex.snapTo(idx) }
                            reportIfChanged(idx)
                        },
                        onDragEnd = {
                            scope.launch {
                                fingerIndex.animateTo(
                                    targetValue = -1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                                )
                            }
                            lastReportedItem = null
                            onLetterChanged(null)
                        },
                    )
                },
            verticalArrangement = Arrangement.SpaceBetween, // Dynamically spaces letters to fit your screen
            horizontalAlignment = Alignment.End,
        ) {
            ITEMS.forEachIndexed { i, item ->
                val offsetDp = curveOffsetDp(i, fingerIndex.value)
                Text(
                    text = item.toString(),
                    color = Color.Gray,
                    style = if (item == '\u25CB') MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .offset { IntOffset(-offsetDp.dp.roundToPx(), 0) }
                        .padding(horizontal = 12.dp),
                )
            }
        }

        if (fingerIndex.value >= 0f && barHeightPx > 0f) {
            val bubbleYPx = (fingerIndex.value / (ITEMS.size - 1)) * barHeightPx
            val bubbleItem = ITEMS[fingerIndex.value.roundToInt().coerceIn(0, ITEMS.lastIndex)]
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        IntOffset(
                            x = -(MAX_OFFSET_DP.dp.roundToPx() + 64.dp.roundToPx()),
                            y = bubbleYPx.roundToInt() - 28.dp.roundToPx(),
                        )
                    }
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bubbleItem.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
            }
        }
    }
}

private fun curveOffsetDp(itemIndex: Int, fingerIndex: Float): Float {
    if (fingerIndex < 0f) return 0f
    val distance = abs(itemIndex - fingerIndex)
    if (distance > INFLUENCE) return 0f
    val t = (distance / INFLUENCE).coerceIn(0f, 1f)
    val falloff = cos(t * (PI / 2)).toFloat()
    return MAX_OFFSET_DP * falloff
}