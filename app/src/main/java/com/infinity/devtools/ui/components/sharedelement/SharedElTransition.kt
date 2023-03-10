@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

/**
 * Composable that performs the transition in [SharedElRoot] composable of a child [SharedEl]
 * This composable is called by [SharedElRoot] once a [SharedEl] is registered.
 * A [SharedElInfo] is registered when it's screen opens for the first time by the [SharedEl] present on that screen.
 *
 * @param id Unique identifier of a [SharedElInfo] to allow getting updated [SharedElInfo] about the [SharedEl] of the transition
 * that will be performed to a target [SharedEl] on a target screenKey. This information will contain the offsets and any other
 * animation parameters that will be used to transition this element.
 */
@Composable
fun SharedElTransition(
    id: SharedElId
) {
    val rootState = LocalSharedElsRootState.current

    val transAlpha = if(rootState.transitionRunning) 1f else 0f
    var offsetAnim: Animatable<Offset, AnimationVector2D>? by remember { mutableStateOf(null) }

    /**
     * Listen for [SharedElRootState.transitionElements] changes to set the origin offset of the
     * screenKey where the transition will start from. This will fix a blink effect when the
     * [SharedEl] content offset is positioned right before the transition starts. So we fix this
     * by positioning the transition content of the [SharedEl] when it is composed for the first time
     * and not when the transition is in progress.
     */
    LaunchedEffect(rootState.transitionElements) {
        // Get updated information about the [SharedEl] of this transition
        val elInfo = rootState.getSharedElement(id)
        // If the offset is != null the [SharedEl] is placed and if the screenKey equals to curScreen
        // this means this element is from the start screen, also the screen check prevents the offset to be
        // reset when a transition is in progress, since the curScreen will be the target screen instead
        // of the origin screen where this offset should point to.
        if (elInfo?.offset != null && elInfo.screenKey == rootState.curScreen) {
            // Set the start offset of the transition
            offsetAnim = Animatable(
                Offset(
                elInfo.offset?.x ?: 0f,
                elInfo.offset?.y ?: 0f,
            ), Offset.VectorConverter)
        }
    }

    /**
     * Listen when a [SharedElRootState.transitionRunning] also when the [SharedElRootState.transitionElements] changes.
     * [SharedElRootState.transitionRunning] = true means that [SharedElRootState.curScreen] is different from [SharedElRootState.prevScreen]
     * which means that a transition from one screen to another is in progress, but we still need to know
     * if the [SharedElRootState.transitionElements] offsets and contents are valid to begin the element transition.
     */
    LaunchedEffect(
        rootState.transitionRunning,
        rootState.transitionElements
    ) {
        // If offsetAnim != null the start offset of the transition is set, and if offsetAnim.isRunning == false means that offset animation is not in progress
        // But we only can start an offset animation when transitionRunning == true which means that a new screen is open
        if (offsetAnim != null && !offsetAnim!!.isRunning && rootState.transitionRunning) {
            // Get origin [elInfo] of the animation and
            // Get target [elInfoTarget] of the animation these will hold information about offset required to animate the offsets
            // When a transition is in progress the rootState.curScreen will point to target screen instead of the origin screen.
            val elInfo = rootState.getSharedElement(id)
            val elInfoTarget = rootState.getSharedElement(SharedElId(id.key, rootState.curScreen ?: ""))
            // If origin and target elements info found
            if (elInfo != null && elInfoTarget != null) {
                // Get the target and destination offset positions of the current transition ScreenA -> ScreenB
                var targetOffset = elInfo.offset
                var destOffset = elInfoTarget.offset
                // Here we perform a transition direction check.
                if (rootState.curScreen == elInfo.screenKey) {
                    // When rootState.curScreen == elInfo.screenKey this means that we are transitioning from ScreenB -> ScreenA
                    targetOffset = elInfoTarget.offset
                    destOffset = elInfo.offset
                }

                // If the targetOffset and destOffset and content of the shared element are set we can start the transition
                if (targetOffset != null && destOffset != null && elInfo.content != null) {
                    // Begin the transition
                    offsetAnim!!.animateTo(
                        targetValue = Offset(
                            destOffset.x,
                            destOffset.y
                        ),
                        animationSpec = tween(1000),
                    )
                    // Transition finished with success, so we can notify transition end by setting rootState.prevScreen equals to rootState.curScreen
                    // which means that no transition is in progress, said that we should set rootState.transitionRunning to false.
                    rootState.prevScreen = rootState.curScreen
                    rootState.transitionRunning = false
                }
            }
        }
    }

    // We use a Box to apply modifiers to the transition content,
    // One of these modifiers are a transparency to only show the transition element when this element transition is in progress
    // Also apply the offset when a animation is running to move element to the offset of the target shared element
    Box(modifier = Modifier.alpha(transAlpha).offset {
        IntOffset(offsetAnim?.value?.x?.toInt() ?: 0, offsetAnim?.value?.y?.toInt() ?: 0)
    }) {
        // Get the updated shared element info content and composes it to display a copy of the
        // element on the screen with a transition animation to the target screen
        val elInfo = rootState.getSharedElement(id)
        if (elInfo != null) {
            elInfo.content?.let { it() }
        }
    }
}