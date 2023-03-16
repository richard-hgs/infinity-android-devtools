package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot

/**
 * Composable that wraps a content that will be animated to it's target in a second screen
 * @param key           A unique identifier of this content in it's [screenKey]
 * @param screenKey     The screen where this content is present
 * @param content       The content that will be animated to a target [SharedEl] in a second screen
 */
@Composable
fun SharedEl(
    key: String,
    screenKey: String,
    transitionContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val rootState = LocalSharedElsRootState.current
    val alpha = if(!rootState.transitionRunning && rootState.curScreen == screenKey) 1f else 0f
    val id = SharedElId(key = key, screenKey = screenKey)
    val elInfo = rootState.getSharedElement(id)

    if (elInfo == null) {
        rootState.registerSharedElement(SharedElInfo(key = key, screenKey = screenKey, content = transitionContent ?: content))
    }

//    LaunchedEffect(Unit) {
//        val mElInfo = rootState.getSharedElement(id)
//        if (mElInfo != null) {
//            rootState.registerSharedElement(mElInfo.copy(content = content))
//        }
//    }

    Box(modifier = Modifier.alpha(alpha).onPlaced { coordinates ->
        val mElInfo = rootState.getSharedElement(id)
        if (mElInfo != null) {
            rootState.registerSharedElement(mElInfo.copy(offset = coordinates.positionInRoot()))
        }
    }) {
        content()
    }
}