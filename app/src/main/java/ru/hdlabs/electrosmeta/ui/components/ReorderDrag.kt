package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.max
import kotlin.math.min
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

@Stable
class HdReorderableState {
    var draggingIndex by mutableStateOf<Int?>(null)
        internal set
    var dragOffset by mutableFloatStateOf(0f)
        internal set
    internal val itemHeights = mutableStateMapOf<Int, Int>()

    internal fun startDrag(index: Int) {
        draggingIndex = index
        dragOffset = 0f
    }

    internal fun endDrag() {
        draggingIndex = null
        dragOffset = 0f
    }

    internal fun drag(index: Int, itemCount: Int, deltaY: Float, onMove: (Int, Int) -> Unit) {
        if (draggingIndex != index) return
        dragOffset += deltaY
        val height = itemHeights[index]?.toFloat() ?: return
        if (height <= 0f) return
        val half = height / 2f
        while (dragOffset > half && draggingIndex!! < itemCount - 1) {
            val from = draggingIndex!!
            onMove(from, from + 1)
            dragOffset -= height
            draggingIndex = from + 1
        }
        while (dragOffset < -half && draggingIndex!! > 0) {
            val from = draggingIndex!!
            val prevHeight = itemHeights[from - 1]?.toFloat() ?: height
            onMove(from, from - 1)
            dragOffset += prevHeight
            draggingIndex = from - 1
        }
    }
}

@Composable
fun rememberHdReorderableState(): HdReorderableState = remember { HdReorderableState() }

fun Modifier.hdReorderableItem(
    state: HdReorderableState,
    index: Int,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
): Modifier = composed {
    val dragging = state.draggingIndex == index
    val dragIdx = state.draggingIndex
    val dragOffset = state.dragOffset
    val selfHeight = state.itemHeights[index]?.toFloat() ?: 0f
    val neighborHeight = when {
        dragIdx != null && index == dragIdx + 1 -> state.itemHeights[dragIdx]?.toFloat() ?: selfHeight
        dragIdx != null && index == dragIdx - 1 -> state.itemHeights[dragIdx]?.toFloat() ?: selfHeight
        else -> selfHeight
    }
    val displacement = when {
        dragIdx == null -> 0f
        index == dragIdx -> dragOffset
        index == dragIdx + 1 && dragOffset > 0f -> -min(dragOffset, neighborHeight)
        index == dragIdx - 1 && dragOffset < 0f -> -max(dragOffset, -neighborHeight)
        else -> 0f
    }
    val animatedDisplacement by animateFloatAsState(
        targetValue = displacement,
        animationSpec = tween(durationMillis = if (dragging) 0 else 120),
        label = "reorderShift",
    )
    val scale by animateFloatAsState(
        targetValue = if (dragging) 1.02f else 1f,
        animationSpec = tween(100),
        label = "reorderScale",
    )
    this
        .zIndex(if (dragging) 2f else 0f)
        .onSizeChanged { state.itemHeights[index] = it.height }
        .graphicsLayer {
            translationY = if (dragging) dragOffset else animatedDisplacement
            scaleX = scale
            scaleY = scale
            shadowElevation = if (dragging) 10f else 0f
            if (dragging) {
                alpha = 0.98f
            }
        }
        .then(
            if (dragging) {
                Modifier.nestedScroll(
                    remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                                Offset(0f, available.y)
                        }
                    },
                )
            } else {
                Modifier
            },
        )
}

@Composable
fun ReorderDragHandle(
    state: HdReorderableState,
    index: Int,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    val blockParentScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!dragging) return Offset.Zero
                return Offset(0f, available.y)
            }
        }
    }
    Box(
        modifier
            .nestedScroll(blockParentScroll)
            .pointerInput(index, itemCount) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        state.startDrag(index)
                    },
                    onDragEnd = {
                        dragging = false
                        state.endDrag()
                    },
                    onDragCancel = {
                        dragging = false
                        state.endDrag()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.drag(index, itemCount, dragAmount.y, onMove)
                    },
                )
            }
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("⋮⋮", color = HdTextSecondary)
    }
}

/** @deprecated Используйте [ReorderDragHandle] с [HdReorderableState]. */
fun Modifier.reorderOnLongPressDrag(
    enabled: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
): Modifier = composed {
    if (!enabled) return@composed this
    var accumulated by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val thresholdPx = 48f
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
