package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.ui.geometry.Offset

/**
 * Created by richard on 18/02/2023 13:45
 *
 */
class MaterialArcMotion : KeyframeBasedMotion() {

    override fun getKeyframes(start: Offset, end: Offset): Pair<FloatArray, LongArray> =
        QuadraticBezier.approximate(
            start,
            if (start.y > end.y) Offset(end.x, start.y) else Offset(start.x, end.y),
            end,
            0.5f
        )

}

val MaterialArcMotionFactory: PathMotionFactory = { MaterialArcMotion() }