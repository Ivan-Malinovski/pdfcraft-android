package io.github.artcraftandroid.pdfcraft.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onSizeChanged
import io.github.artcraftandroid.pdfcraft.ui.AppViewModel
import io.github.artcraftandroid.pdfcraft.ui.FloatSide
import io.github.artcraftandroid.pdfcraft.ui.LucideIcons
import io.github.artcraftandroid.pdfcraft.ui.PdfShapes
import io.github.artcraftandroid.pdfcraft.ui.floatingSurface
import io.github.artcraftandroid.pdfcraft.ui.pdfColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** The three main destinations, in nav order. Search is reached from Home. */
internal val NavTabs = listOf(HomeTab.Home, HomeTab.Files, HomeTab.Tools)

/** One destination of the floating nav: its icon over its label, washed with the accent when active. */
@Composable
internal fun NavTab(tab: HomeTab, selected: HomeTab, width: Dp, onSelect: (HomeTab) -> Unit) {
    val c = pdfColors
    val accent = homeAccent(c.dark)
    val icon = when (tab) {
        HomeTab.Home -> LucideIcons.House
        HomeTab.Files -> LucideIcons.Folder
        HomeTab.Tools -> LucideIcons.LayoutGrid
        HomeTab.Search -> LucideIcons.Search
    }
    val active = tab == selected || (tab == HomeTab.Home && selected == HomeTab.Search)
    val iconTint = if (active) accent else c.textSecondary
    Column(
        Modifier.width(width).height(52.dp).clip(PdfShapes.Pill)
            .background(if (active) tint(accent, c.dark) else Color.Transparent)
            .clickable(role = Role.Tab) { onSelect(tab) }.semantics { contentDescription = tab.label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(21.dp))
        Text(tab.label, color = iconTint, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val SideNavTabWidth = 66.dp
private val SideNavPadding = 6.dp

/** How much room the content leaves beside the side nav: the pill, and the margin on both its sides. */
val SideNavReserve = SideNavTabWidth + SideNavPadding * 2 + FloatSide * 2

/**
 * The nav as a vertical glass pill on a wide window's right or left edge. Press and hold it (it ticks
 * when it lets go of the page) and drag to move it: on letting go it settles against the nearer edge,
 * at the height it was left, and the spot is kept (see [AppViewModel.moveNav]).
 */
@Composable
internal fun SideNav(vm: AppViewModel, selected: HomeTab, modifier: Modifier = Modifier, onSelect: (HomeTab) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val margin = with(LocalDensity.current) { FloatSide.toPx() }
        var size by remember { mutableStateOf(IntSize.Zero) }
        fun rest(right: Boolean, y: Float) = Offset(
            if (right) w - size.width - margin else margin,
            y * (h - size.height).coerceAtLeast(0f),
        )
        val target = rest(vm.navOnRight, vm.navY)
        // while held it follows the finger; after letting go it glides from there to its resting place
        var held by remember { mutableStateOf<Offset?>(null) }
        var from by remember { mutableStateOf(Offset.Zero) }
        val glide = remember { Animatable(1f) }
        val pos = held ?: if (glide.value < 1f) from + (target - from) * glide.value else target
        val current by rememberUpdatedState(pos)

        fun grab() { held = current; haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
        fun move(by: Offset) {
            val p = (held ?: return) + by
            held = Offset(p.x.coerceIn(0f, (w - size.width).coerceAtLeast(0f)), p.y.coerceIn(0f, (h - size.height).coerceAtLeast(0f)))
        }
        fun drop() {
            val p = held ?: return
            val room = h - size.height
            val right = p.x + size.width / 2 > w / 2
            from = p
            held = null
            vm.moveNav(right, if (room > 0f) p.y / room else 0.5f)
            scope.launch { glide.snapTo(0f); glide.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 380f)) }
        }

        Column(
            Modifier.offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                .onSizeChanged { size = it }
                .systemGestureExclusion()
                .floatingSurface(PdfShapes.Pill)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(onDragStart = { grab() }, onDragEnd = { drop() }, onDragCancel = { drop() }) { change, amount ->
                        change.consume(); move(amount)
                    }
                }
                .padding(horizontal = SideNavPadding, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (t in NavTabs) NavTab(t, selected, SideNavTabWidth, onSelect)
        }
    }
}
