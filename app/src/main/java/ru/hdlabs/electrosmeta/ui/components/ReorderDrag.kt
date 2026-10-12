package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

fun Modifier.reorderOnLongPressDrag(
    enabled: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
): Modifier = composed {
    if (!enabled) return@composed this
    val density = LocalDensity.current
    val thresholdPx = with(density) { 56.dp.toPx() }
    var accumulated by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                dragging = true
                accumulated = 0f
            },
            onDragEnd = { dragging = false },
            onDragCancel = { dragging = false },
            onDrag = { change, dragAmount ->
                change.consume()
                accumulated += dragAmount.y
                while (accumulated > thresholdPx) {
                    onMoveDown()
                    accumulated -= thresholdPx
                }
                while (accumulated < -thresholdPx) {
                    onMoveUp()
                    accumulated += thresholdPx
                }
            },
        )
    }
}
