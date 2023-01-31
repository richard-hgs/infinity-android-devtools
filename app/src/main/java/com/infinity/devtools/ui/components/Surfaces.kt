@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AppSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    content: @Composable () -> Unit
) {
    val absoluteElevation = LocalAbsoluteElevation.current + elevation
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteElevation provides absoluteElevation
    ) {
        Box(
            modifier
                .minimumTouchTargetSize()
                .surface(
                    shape = shape,
                    backgroundColor = surfaceColorAtElevation(
                        color = color,
                        elevationOverlay = LocalElevationOverlay.current,
                        absoluteElevation = absoluteElevation
                    ),
                    border = border,
                    elevation = elevation
                )
                .then(
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = indication,
                        enabled = enabled,
                        onClickLabel = onClickLabel,
                        role = role,
                        onClick = onClick
                    )
                ),
            propagateMinConstraints = true
        ) {
            content()
        }
    }
}


internal fun Modifier.minimumTouchTargetSize(): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "minimumTouchTargetSize"
        // TTODO b/214589635 - surface this information through the layout inspector in a better way
        //  - for now just add some information to help developers debug what this size represents.
        properties["README"] = "Adds outer padding to measure at least 48.dp (default) in " +
                "size to disambiguate touch interactions if the element would measure smaller"
    }
) {
    if (LocalMinimumTouchTargetEnforcement.current) {
        // TTODO: consider using a hardcoded value of 48.dp instead to avoid inconsistent UI if the
        // LocalViewConfiguration changes across devices / during runtime.
        val size = LocalViewConfiguration.current.minimumTouchTargetSize
        MinimumTouchTargetModifier(size)
    } else {
        Modifier
    }
}

val LocalMinimumTouchTargetEnforcement: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { true }

private class MinimumTouchTargetModifier(val size: DpSize) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {

        val placeable = measurable.measure(constraints)

        // Be at least as big as the minimum dimension in both dimensions
        val width = maxOf(placeable.width, size.width.roundToPx())
        val height = maxOf(placeable.height, size.height.roundToPx())

        return layout(width, height) {
            val centerX = ((width - placeable.width) / 2f).roundToInt()
            val centerY = ((height - placeable.height) / 2f).roundToInt()
            placeable.place(centerX, centerY)
        }
    }

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? MinimumTouchTargetModifier ?: return false
        return size == otherModifier.size
    }

    override fun hashCode(): Int {
        return size.hashCode()
    }
}

@Composable
private fun surfaceColorAtElevation(
    color: Color,
    elevationOverlay: ElevationOverlay?,
    absoluteElevation: Dp
): Color {
    return if (color == MaterialTheme.colors.surface && elevationOverlay != null) {
        elevationOverlay.apply(color, absoluteElevation)
    } else {
        color
    }
}

private fun Modifier.surface(
    shape: Shape,
    backgroundColor: Color,
    border: BorderStroke?,
    elevation: Dp
) = this.shadow(elevation, shape, clip = false)
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)