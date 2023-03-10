@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 *
 */
@Composable
fun SharedElRoot(
    screenKey: String,
    content: @Composable () -> Unit
) {
    val rootState = remember { SharedElRootState() }

    if (rootState.curScreen == null) {
        rootState.prevScreen = screenKey
    }

    rootState.curScreen = screenKey

    if (rootState.prevScreen == null) {
        rootState.prevScreen = screenKey
    }

    if (rootState.curScreen != rootState.prevScreen) {
        rootState.transitionRunning = true
    }

    CompositionLocalProvider(
        LocalSharedElsRootState provides rootState
    ) {
        // Wrap screen content on a box to fill max size to allow transition of shared elements
        // start in any position of entire screen
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        rootState.getTransitionElements(rootState.prevScreen).forEach {
            SharedElTransition(it.id)
        }
//        rootState.transitionElements.forEach {
//            SharedElTransition(it.id)
//        }
    }
}

val LocalSharedElsRootState = staticCompositionLocalOf<SharedElRootState> {
    error("SharedElementsRoot not found. SharedElement must be hosted in SharedElementsRoot.")
}


class SharedElRootState {
    var prevScreen: String? by mutableStateOf(null)
    var curScreen: String? by mutableStateOf(null)
    var transitionRunning: Boolean by mutableStateOf(false)
    var transitionElements = mutableStateListOf<com.infinity.devtools.ui.components.sharedelement.SharedElInfo>()

    /**
     * Register all information of a shared element.
     * The information is needed to animate the shared element
     * Don't forget to unregister the shared element will not be used anymore
     *
     * @param sharedElInfo  Shared element info. Can be a start or a end element information.
     */
    fun registerSharedElement(sharedElInfo : com.infinity.devtools.ui.components.sharedelement.SharedElInfo) {
        // NotNull=Shared element is already registered, Null=New shared element being registered
        var elExists : com.infinity.devtools.ui.components.sharedelement.SharedElInfo? = null
        var elExistsPos = 0
        // Null=Shared element start, NotNull=Parent of the Shared element end
//        var startEl : SharedElInfo? = null
//        var startElPos = 0
        transitionElements.forEachIndexed { index, it ->
            if (sharedElInfo.key == it.key && sharedElInfo.screenKey == it.screenKey) {
                // Element is already registered
                elExists = it
                elExistsPos = index
            }
//            if (sharedElInfo.key == it.key && sharedElInfo.screenKey != it.screenKey) {
//                // Start element of the current end element found
//                startEl = it
//                startElPos = index
//            }
        }

//        if (startEl == null) {
        // No start element found, register current element as the start element
        if (elExists == null) {
            // Element for key doesn't exists add for the first time as a start element
            transitionElements.add(sharedElInfo)
        } else {
            // Start element already exists update it's information
            transitionElements[elExistsPos] = sharedElInfo
        }
//        } else {
//            // Start element found, register current element inside the start element as a end element
//            startEl!!.endElement = sharedElInfo
//            transitionElements[startElPos] = startEl!!
//        }
    }
//
//    /**
//     * Search for a shared element information using it's identifier [id]
//     *
//     * @param id    Identifier of the shared element information
//     * @return      Element information or NULL if not found
//     */
//    fun getSharedElement(id: SharedElId) : SharedElInfo? {
//        for(i in transitionElements.indices) {
//            val sharedElAt = transitionElements[i]
//            if (sharedElAt.id == id) {
//                // Check if element being searched is the start element
//                return sharedElAt
//            } else if (sharedElAt.endElement != null && sharedElAt.endElement!!.id == id) {
//                // If not start element, checks if it's the end element
//                return sharedElAt.endElement!!
//            }
//        }
//        // Element not found return null
//        return null
//    }

    fun getSharedElement(id: SharedElId) : SharedElInfo? {
        for(i in transitionElements.indices) {
            val sharedElAt = transitionElements[i]
            if (sharedElAt.id == id) {
                // Check if element being searched is the start element
                return sharedElAt
            }
        }
        // Element not found return null
        return null
    }

    fun getSharedElements(key: String) : List<SharedElInfo> {
        return transitionElements.filter { it.key == key }
    }

    fun getTransitionElements(screenKey: String?) : List<SharedElInfo> {
        return transitionElements.filter { it.screenKey == screenKey }
    }
}
