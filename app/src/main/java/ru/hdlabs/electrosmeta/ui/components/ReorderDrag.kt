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
import kotlin.math.abs
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

@Stable
class HdReorderableState {
    var originIndex by mutableStateOf<Int?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set
    var itemCount by mutableIntStateOf(0)
        private set
    internal val itemHeights = mutableStateMapOf<Int, Int>()

    fun startDrag(index: Int, count: Int) {
        originIndex = index
        itemCount = count
        dragOffset = 0f
    }

    fun addDelta(deltaY: Float) {
        if (originIndex == null) return
        dragOffset += deltaY
    }

    fun endDrag() {
        originIndex = null
        dragOffset = 0f
    }

    fun targetIndex(): Int {
        val origin = originIndex ?: return 0
        val count = itemCount
        if (count <= 1) return origin
        var top = 0f
        for (i in 0 until origin) top += heightAt(i)
        val center = top + dragOffset + heightAt(origin) / 2f
        var cursor = 0f
        var nearest = 0
        var best = Float.MAX_VALUE
        for (i in 0 until count) {
            val h = heightAt(i)
            val itemCenter = cursor + h / 2f
            val dist = abs(center - itemCenter)
            if (dist < best) {
                best = dist
                nearest = i
            }
            cursor += h
        }
        return nearest
    }

    fun shiftFor(index: Int): Float {
        val origin = originIndex ?: return 0f
        val target = targetIndex()
        if (index == origin) return dragOffset
        val gap = heightAt(origin)
        return when {
            target > origin && index in (origin + 1)..target -> -gap
            target < origin && index in target until origin -> gap
            else -> 0f
        }
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
    val origin = state.originIndex
    val dragging = origin == index
    val shift = state.shiftFor(index)
    val animatedShift by animateFloatAsState(
        targetValue = shift,
        animationSpec = tween(durationMillis = if (dragging) 0 else 140),
        label = "reorderShift",
    )
    val scale by animateFloatAsState(
        targetValue = if (dragging) 1.03f else 1f,
        animationSpec = tween(120),
        label = "reorderScale",
    )
    this
        .zIndex(if (dragging) 2f else 0f)
        .onSizeChanged { state.itemHeights[index] = it.height }
        .graphicsLayer {
            translationY = if (dragging) state.dragOffset else animatedShift
            scaleX = scale
            scaleY = scale
            shadowElevation = if (dragging) 12f else 0f
            alpha = if (dragging) 0.96f else 1f
        }
}

@Composable
fun ReorderDragHandle(
    state: HdReorderableState,
    index: Int,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestIndex by rememberUpdatedState(index)
    val latestCount by rememberUpdatedState(itemCount)
    val latestOnMove by rememberUpdatedState(onMove)
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
                        val from = state.originIndex
                        val to = state.targetIndex()
                        dragging = false
                        if (from != null && from != to) latestOnMove(from, to)
                        state.endDrag()
                    },
                    onDragCancel = {
                        dragging = false
                        state.endDrag()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.addDelta(dragAmount.y)
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
