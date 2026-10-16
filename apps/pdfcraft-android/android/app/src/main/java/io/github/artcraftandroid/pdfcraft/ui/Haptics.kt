package io.github.artcraftandroid.pdfcraft.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** The few light haptics the app uses: picking a tool, flipping a switch, landing on a page. */
@Immutable
class Haptics(private val feedback: HapticFeedback) {
    /** A light tick: a tool was picked, or a page was reached. */
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    /** A switch went on or off. */
    fun toggle(on: Boolean) = feedback.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
}

@Composable
fun rememberHaptics(): Haptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { Haptics(feedback) }
}

/** [onClick] preceded by a light tick, for buttons that pick a tool. */
@Composable
fun tickOnClick(onClick: () -> Unit): () -> Unit {
    val haptics = rememberHaptics()
    return { haptics.tick(); onClick() }
}
