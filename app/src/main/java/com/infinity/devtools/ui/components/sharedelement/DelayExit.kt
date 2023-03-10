@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.runtime.*


@Composable
fun DelayExit(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val rootState = LocalSharedElsRootState.current

    var state by remember { mutableStateOf(DelayExitState.Invisible) }

    when (state) {
        DelayExitState.Invisible -> {
            if (visible) state = DelayExitState.Visible
        }
        DelayExitState.Visible -> {
            if (!visible) {
                state = if (rootState.transitionRunning) DelayExitState.ExitDelayed else DelayExitState.Invisible
            }
        }
        DelayExitState.ExitDelayed -> {
            if (!rootState.transitionRunning) state = DelayExitState.Invisible
        }
    }

    if (state != DelayExitState.Invisible) content()
}

/**
 * State used by [DelayExit] to keep both screens opens when a transition is in progress and when
 * the screen transition finishes closes dispose the previous screen composable content
 */
private enum class DelayExitState {
    Invisible, Visible, ExitDelayed
}
