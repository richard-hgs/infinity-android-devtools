package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 * Holds all information needed by [SharedEl] and [SharedElTransition] to save and perform transitions
 * to the shared elements.
 *
 * @param key       Unique key identifier of a [SharedEl]
 * @param screenKey Screen key where the [SharedEl] is present
 * @param content   The content of the [SharedEl] that will be transitioned to a target [SharedEl]
 * @param offset    The actual offset of the [SharedEl] relative to root
 * @param id        Unique identifier of a [SharedElInfo] that is a junction of [key] and [screenKey]
 */
data class SharedElInfo(
    var key: String,
    var screenKey: String,
    var content: (@Composable () -> Unit)?,
    var offset: Offset? = null,
    var id: SharedElId = SharedElId(key, screenKey)
)
