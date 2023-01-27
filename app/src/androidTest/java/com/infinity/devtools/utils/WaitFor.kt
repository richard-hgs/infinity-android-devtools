@file:Suppress("unused")

package com.infinity.devtools.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals

object WaitFor {

    /**
     * Sleep for a period of time
     * @param millis Millis to sleep
     */
    fun sleep(millis: Long) : ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription(): String = "wait for $millis milliseconds"
            override fun perform(uiController: UiController, v: View?) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    /**
     * Run current thread until value matches or timeout reached
     * @param checkEvery Amount of millis to check against value
     * @param millisTimeout Max millis timeout to check against value
     * @param expectedValue Value expected
     * @param currentValue A callback that returns current value being checked
     */
    fun <T> valueChecker(checkEvery: Int = 100, millisTimeout: Long = 5000, expectedValue: T, currentValue: () -> T) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + millisTimeout
        var previousCheckTime = 0L
        while(System.currentTimeMillis() < endTime) {
            if (System.currentTimeMillis() - previousCheckTime >= checkEvery) {
                if (currentValue() == expectedValue) {
                    break
                }

                previousCheckTime = System.currentTimeMillis()
            }
        }

        // Throw assertion error
        assertEquals(expectedValue, currentValue())
    }
}