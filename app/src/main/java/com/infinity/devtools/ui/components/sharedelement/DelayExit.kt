package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.runtime.*
import com.infinity.devtools.ui.components.sharedelement.DelayExitState.Invisible
import com.infinity.devtools.ui.components.sharedelement.DelayExitState.Visible

/**
 * Created by richard on 18/02/2023 13:23
 *
 */
/**
 * When [visible] becomes false, if transition is running, delay the exit of the content until
 * transition finishes. Note that you may need to call [SharedElementsRootScope.prepareTransition]
 * before [visible] becomes false to start transition immediately.
 */
@Composable
fun SharedElementsRootScope.DelayExit(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    var state by remember { mutableStateOf(Invisible) }

    when (state) {
        Invisible -> {
            if (visible) state = Visible
        }
        Visible -> {
            if (!visible) {
                state = if (isRunningTransition) DelayExitState.ExitDelayed else Invisible
            }
        }
        DelayExitState.ExitDelayed -> {
            if (!isRunningTransition) state = Invisible
        }
    }

    if (state != Invisible) content()
}

private enum class DelayExitState {
    Invisible, Visible, ExitDelayed
}