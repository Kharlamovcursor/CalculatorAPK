package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

@Stable
class HdReorderableState {
    var draggingIndex by mutableStateOf<Int?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set
    var itemCount by mutableIntStateOf(0)
        private set
    internal val itemHeights = mutableStateMapOf<Int, Int>()

    val isDragging: Boolean get() = draggingIndex != null

    fun startDrag(index: Int, count: Int) {
        draggingIndex = index
        itemCount = count
        dragOffset = 0f
    }

    fun dragBy(deltaY: Float, count: Int, onMove: (Int, Int) -> Unit) {
        if (draggingIndex == null) return
        itemCount = count
        dragOffset += deltaY
        while (true) {
            val index = draggingIndex ?: return
            if (index < count - 1 && dragOffset > heightAt(index + 1) / 2f) {
                val step = heightAt(index + 1)
                onMove(index, index + 1)
                swapHeights(index, index + 1)
                draggingIndex = index + 1
                dragOffset -= step
                continue
            }
            if (index > 0 && dragOffset < -heightAt(index - 1) / 2f) {
                val step = heightAt(index - 1)
                onMove(index, index - 1)
                swapHeights(index, index - 1)
                draggingIndex = index - 1
                dragOffset += step
                continue
            }
            break
        }
    }

    fun endDrag() {
        draggingIndex = null
        dragOffset = 0f
    }

    private fun swapHeights(from: Int, to: Int) {
        val fromHeight = itemHeights[from]
        val toHeight = itemHeights[to]
        if (toHeight != null) itemHeights[from] = toHeight else itemHeights.remove(from)
        if (fromHeight != null) itemHeights[to] = fromHeight else itemHeights.remove(to)
    }

    private fun heightAt(index: Int): Float {
        val measured = itemHeights[index]
        return if (measured != null && measured > 0) measured.toFloat() else 88f
    }
}

@Composable
fun rememberHdReorderableState(): HdReorderableState = remember { HdReorderableState() }

fun Modifier.hdReorderableItem(
    state: HdReorderableState,
    index: Int,
): Modifier = composed {
    val dragging = state.draggingIndex == index
    val translationTarget = if (dragging) state.dragOffset else 0f
    val translation by animateFloatAsState(
        targetValue = translationTarget,
        animationSpec = tween(durationMillis = if (dragging) 0 else 160),
        label = "reorderLock",
    )
    val scale by animateFloatAsState(
        targetValue = if (dragging) 1.03f else 1f,
        animationSpec = tween(if (dragging) 80 else 160),
        label = "reorderScale",
    )
    this
        .zIndex(if (dragging) 2f else 0f)
        .onSizeChanged { state.itemHeights[index] = it.height }
        .graphicsLayer {
            translationY = translation
            scaleX = scale
            scaleY = scale
            shadowElevation = if (dragging) 12f else 0f
            alpha = if (dragging) 0.98f else 1f
        }
}

@Composable
fun ReorderDragHandle(
    state: HdReorderableState,
    index: Int,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val latestIndex by rememberUpdatedState(index)
    val latestCount by rememberUpdatedState(itemCount)
    val latestOnMove by rememberUpdatedState(onMove)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
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
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        state.startDrag(latestIndex, latestCount)
                    },
                    onDragEnd = {
                        dragging = false
                        state.endDrag()
                        latestOnDragEnd()
                    },
                    onDragCancel = {
                        dragging = false
                        state.endDrag()
                        latestOnDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragBy(dragAmount.y, latestCount, latestOnMove)
                    },
                )
            }
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("⋮⋮", color = HdTextSecondary, fontSize = 28.sp)
    }
}
