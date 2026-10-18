package io.github.artcraftandroid.pdfcraft.ui.viewer

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Where the viewer's quick toolbar floats: along the bottom as it comes, or down the left or right edge. */
enum class QuickDock {
    Bottom, Left, Right;

    val side: Boolean get() = this != Bottom

    companion object {
        fun of(name: String?): QuickDock = entries.firstOrNull { it.name == name } ?: Bottom
    }
}

/**
 * The quick toolbar being moved. Press and hold the pill (it ticks as it lets go of the page) and drag it:
 * on letting go it settles against the edge the finger is nearest, the bottom or a side, and the spot is kept.
 * Both placements (in the bottom stack, or on a side) share this: the pill is laid out where it rests and
 * this only shifts it, by the finger's travel while held and by what is left of its glide back afterwards.
 */
@Stable
class QuickDrag(private val scope: CoroutineScope, private val onGrab: () -> Unit, private val onDrop: (QuickDock, Float) -> Unit) {
    /** The window, and the room the pill must keep clear of at the top and at the bottom of it, in pixels. */
    var screen by mutableStateOf(IntSize.Zero)
    var topGap = 0f
    var bottomGap = 0f
    /** How tall the pill would be down a side, for working out its place from a drag that began along the bottom. */
    var sideHeight = 0f
    var margin = 0f

    /** The pill's size and where it rests (without the shift), as last laid out. */
    var size by mutableStateOf(IntSize.Zero)
        private set
    var rest by mutableStateOf(Offset.Zero)
        private set

    private var held by mutableStateOf<Offset?>(null)
    private var from = Offset.Zero
    private var progress by mutableFloatStateOf(1f)
    /** 0 to 1 as the pill appears in its new shape (a pill moved between the bottom and a side is laid out afresh, so it fades in rather than slides). */
    var appear by mutableFloatStateOf(1f)
        private set
    private var finger = Offset.Zero
    private var glide: Job? = null
    private var onSide = false

    val shift: Offset
        get() = held?.let { it - rest } ?: if (progress < 1f) (from - rest) * (1f - progress) else Offset.Zero

    val dragging: Boolean get() = held != null

    fun placed(at: Offset, size: IntSize, side: Boolean) { rest = at; this.size = size; onSide = side }

    fun grab(at: Offset) {
        glide?.cancel()
        progress = 1f
        appear = 1f
        val start = rest + shift
        held = start
        finger = start + at
        onGrab()
    }

    fun move(by: Offset, at: Offset) {
        val p = (held ?: return) + by
        held = Offset(
            p.x.coerceIn(0f, (screen.width - size.width).coerceAtLeast(0).toFloat()),
            p.y.coerceIn(0f, (screen.height - size.height).coerceAtLeast(0).toFloat()),
        )
        finger = held!! + at
    }

    fun drop() {
        val p = held ?: return
        val w = screen.width.toFloat().coerceAtLeast(1f)
        val h = screen.height.toFloat().coerceAtLeast(1f)
        val left = finger.x / w
        val right = (w - finger.x) / w
        val bottom = (h - finger.y) / h
        val dock = when {
            bottom <= left && bottom <= right -> QuickDock.Bottom
            left <= right -> QuickDock.Left
            else -> QuickDock.Right
        }
        // down a side, the pill's top: where it was held if it was already there, else centred on the finger
        val tall = if (onSide) size.height.toFloat() else sideHeight
        val top = if (onSide) p.y else finger.y - tall / 2
        val room = h - bottomGap - topGap - tall
        val y = if (room > 0f) ((top - topGap) / room).coerceIn(0f, 1f) else 0.5f
        val reshaped = dock.side != onSide
        held = null
        if (reshaped) {
            // the other layout isn't placed yet (and is a different shape): no glide, a short fade-in once it is
            progress = 1f
            appear = 0f
            glide = scope.launch { delay(50); animate(0f, 1f, animationSpec = tween(200)) { v, _ -> appear = v } }
        } else {
            from = p
            progress = 0f
            // already known, so the glide doesn't aim for the old spot for the frame before the pill is placed anew
            if (dock.side) rest = sideRest(dock, y, margin)
            glide = scope.launch { animate(0f, 1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) { v, _ -> progress = v } }
        }
        onDrop(dock, y)
    }

    /** Where the pill rests down a side: against the left or right edge, [y] of the way down the room it has. */
    fun sideRest(dock: QuickDock, y: Float, margin: Float): Offset {
        val x = if (dock == QuickDock.Left) margin else screen.width - size.width - margin
        val room = (screen.height - bottomGap - topGap - size.height).coerceAtLeast(0f)
        return Offset(x, topGap + y * room)
    }
}

/** Lays the pill out under [drag]'s control: moved by a long press and a drag, ticking as it is picked up. */
@Composable
fun Modifier.quickMovable(drag: QuickDrag, side: Boolean): Modifier {
    return this
        .onGloballyPositioned { drag.placed(it.positionInRoot(), it.size, side) }
        .graphicsLayer {
            translationX = drag.shift.x; translationY = drag.shift.y
            alpha = drag.appear
            scaleX = 0.92f + 0.08f * drag.appear; scaleY = scaleX
        }
        .then(if (side) Modifier.systemGestureExclusion() else Modifier)
        .pointerInput(drag) {
            detectDragGesturesAfterLongPress(
                onDragStart = { drag.grab(it) },
                onDragEnd = { drag.drop() },
                onDragCancel = { drag.drop() },
            ) { change, amount ->
                change.consume()
                drag.move(amount, change.position)
            }
        }
}

/**
 * Places the pill against a side edge, [y] of the way down the room between [top] and [bottom] (pixels kept clear),
 * from its own measured size: it is right in the very frame the pill first appears (going by the size of the last
 * layout, the bottom one's, it sat in the middle of the screen for a moment).
 */
fun Modifier.sideDocked(dock: QuickDock, y: Float, top: Float, bottom: Float, margin: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(constraints.maxWidth, constraints.maxHeight) {
        val x = if (dock == QuickDock.Left) margin else constraints.maxWidth - placeable.width - margin
        val room = (constraints.maxHeight - bottom - top - placeable.height).coerceAtLeast(0f)
        placeable.place(x.roundToInt(), (top + y * room).roundToInt())
    }
}
