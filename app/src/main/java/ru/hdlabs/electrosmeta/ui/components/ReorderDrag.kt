package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

fun Modifier.reorderOnLongPressDrag(
    enabled: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
): Modifier = composed {
    if (!enabled) return@composed this
    val density = LocalDensity.current
    val thresholdPx = with(density) { 48.dp.toPx() }
    var accumulated by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val blockParentScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!dragging) return Offset.Zero
                return Offset(0f, available.y)
            }
        }
    }
    this
        .nestedScroll(blockParentScroll)
        .pointerInput(Unit) {
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

@Composable
fun ReorderDragHandle(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .reorderOnLongPressDrag(
                enabled = true,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
            )
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("⋮⋮", color = HdTextSecondary)
    }
}
