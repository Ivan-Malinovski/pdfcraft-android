package io.github.artcraftandroid.pdfcraft.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

/** Material window size classes: compact < 600dp, medium < 840dp, expanded beyond (height: 480 / 900). */
enum class SizeClass { Compact, Medium, Expanded }

/**
 * The size of the window the app is in, not the screen: it follows split-screen, freeform resizing and a
 * foldable folding or unfolding. Layouts branch on this instead of reading the configuration themselves.
 */
@Immutable
data class WindowSize(val widthDp: Int, val heightDp: Int) {
    val width = when { widthDp < 600 -> SizeClass.Compact; widthDp < 840 -> SizeClass.Medium; else -> SizeClass.Expanded }
    val height = when { heightDp < 480 -> SizeClass.Compact; heightDp < 900 -> SizeClass.Medium; else -> SizeClass.Expanded }

    /** Wider than tall. */
    val landscape get() = widthDp > heightDp

    /** A phone on its side: wide enough to want a sideways layout, too short to spend height freely. */
    val shortHeight get() = height == SizeClass.Compact

    /** Room for a second pane or a side panel (a tablet, an unfolded foldable, a wide window). */
    val twoPane get() = width != SizeClass.Compact
}

val LocalWindowSize = compositionLocalOf { WindowSize(widthDp = 411, heightDp = 891) }

@Composable
fun rememberWindowSize(): WindowSize {
    val c = LocalConfiguration.current
    return WindowSize(c.screenWidthDp, c.screenHeightDp)
}
