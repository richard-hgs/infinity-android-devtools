package com.infinity.devtools

import android.graphics.Point
import com.infinity.devtools.providers.UTLogProvider
import com.infinity.devtools.utils.MathUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * MathUtils test class
 */
class MathUtilsTest {

    private fun mockPoint(x: Int, y: Int) : Point {
        val point: Point = mock(Point::class.java)
        point.x = x
        point.y = y
        `when`(point.toString()).then {
            "Point(${point.x}, ${point.y})"
        }
        return point
    }

    @Test
    fun test_progress2D() {
        @Suppress("UNUSED_VARIABLE")
        val logProvider = UTLogProvider.mockInst

        val originPoint : Point = mockPoint(0, 0)
        val destPoint : Point = mockPoint(50, 100)
        val curPoint : Point = mockPoint(5, 10)

        var progress = MathUtils.progress2D(originPoint, destPoint, curPoint, 100)
        assertEquals(10, progress)

        progress = MathUtils.progress2D(originPoint, destPoint, curPoint, 1000)
        assertEquals(100, progress)
    }
}