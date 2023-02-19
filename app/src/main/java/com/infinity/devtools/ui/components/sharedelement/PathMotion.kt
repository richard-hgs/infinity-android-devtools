package com.infinity.devtools.ui.components.sharedelement

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp

/**
 * Created by richard on 18/02/2023 13:31
 *
 */
typealias PathMotion = (start: Offset, end: Offset, fraction: Float) -> Offset

typealias PathMotionFactory = () -> PathMotion

val LinearMotion: PathMotion = ::lerp

val LinearMotionFactory: PathMotionFactory = { LinearMotion }