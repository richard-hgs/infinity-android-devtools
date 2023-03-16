package com.infinity.devtools.utils

import android.graphics.Point
import android.graphics.PointF
import kotlin.math.roundToInt

/**
 * Math utilities to perform some difficult calculations
 */
object MathUtils {

    /**
     * Calculates a progression of a (x,y) point between two other (x,y) points
     * by using a median value of the progress in both axis {(xProgress + yProgress) / 2}
     *
     * @param originPoint 2D Point of the origin
     * @param destPoint 2D Point of the destination
     * @param accuracy Accuracy of the progress measurement defaults to 100 which means that the progress range is 0-100
     */
    fun progress2D(originPoint: Point, destPoint: Point, curPoint: Point, accuracy: Int = 100) : Int {
        return progress2D(PointF(originPoint), PointF(destPoint), PointF(curPoint), accuracy.toFloat()).roundToInt()
    }

    /**
     * Calculates a progression of a (x,y) point between two other (x,y) points
     * by using a median value of the progress in both axis {(xProgress + yProgress) / 2}
     *
     * @param originPoint 2D Point of the origin
     * @param destPoint 2D Point of the destination
     * @param accuracy Accuracy of the progress measurement defaults to 100 which means that the progress range is 0-100
     */
    fun progress2D(originPoint: PointF, destPoint: PointF, curPoint: PointF, accuracy: Float = 100f) : Float {
        var progressX = 0f
        var progressY = 0f

        if (
            curPoint.x >= originPoint.x &&
            curPoint.y >= originPoint.y &&
            curPoint.x <= destPoint.x &&
            curPoint.y <= destPoint.y
        ) {
            // Point is in region
            // Calc progress in both axis
            // OBS: Subtract the origin offsets allow progress to starts at 0 and goes up to accuracy max value
            if (destPoint.x > 0) {
                progressX = ((curPoint.x - originPoint.x) * accuracy) / (destPoint.x - originPoint.x)
            }
            if (destPoint.y > 0) {
                progressY = ((curPoint.y - originPoint.y) * accuracy) / (destPoint.y - originPoint.y)
            }


        }
        // The progress is a Median of the two axis progress
        return (progressX + progressY) / 2
    }
}