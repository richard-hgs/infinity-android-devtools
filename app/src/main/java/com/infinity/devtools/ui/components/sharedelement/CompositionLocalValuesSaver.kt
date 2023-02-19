package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*

/**
 * Created by richard on 18/02/2023 13:42
 *
 */
@Suppress("UNCHECKED_CAST")
private val compositionLocalList = listOf(
    LocalAbsoluteElevation,
    LocalContentColor,
    LocalContentAlpha,
    LocalIndication,
    LocalTextSelectionColors,
    LocalTextStyle
) as List<ProvidableCompositionLocal<Any>>

@JvmInline
@Immutable
internal value class CompositionLocalValues(private val values: Array<ProvidedValue<*>>) {

    @Composable
    @NonRestartableComposable
    fun Provider(content: @Composable () -> Unit) {
        CompositionLocalProvider(*values, content = content)
    }

}

internal val compositionLocalValues: CompositionLocalValues
    @Composable get() = CompositionLocalValues(
        compositionLocalList.map { it provides it.current }.toTypedArray()
    )