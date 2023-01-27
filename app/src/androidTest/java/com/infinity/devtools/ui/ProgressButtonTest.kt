package com.infinity.devtools.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinity.devtools.ui.components.ProgressButton
import com.infinity.devtools.ui.theme.AppTheme
import com.infinity.devtools.utils.WaitFor
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @Test
    fun testButtonIdleLoadingSuccessState() {
        val btnText = "Some Text"
        var btnClicked = false
        var btnState : SSButtonState = SSButtonState.IDLE

        // Start the app
        composeTestRule.setContent {
            AppTheme {
                val coroutineScope = rememberCoroutineScope()
                var submitButtonState by remember { mutableStateOf(SSButtonState.IDLE) }

                btnState = submitButtonState

                ProgressButton(
                    type = SSButtonType.CIRCLE,
                    onClick = {
                        coroutineScope.launch {
                            // Save click handled
                            btnClicked = true

                            // Test loading state
                            submitButtonState = SSButtonState.LOADING

                            // Wait for loading progress show
                            delay(1000)

                            // Show a success result
                            submitButtonState = SSButtonState.SUCCESS
                        }
                    },
                    assetColor = Color.White,
                    buttonState = submitButtonState,
                    setButtonState = { submitButtonState = it },
                    text = btnText
                )
            }
        }

        // Wait for button visibility
        onView(isRoot()).perform(WaitFor.sleep(500))

        // Check if idle button is in idle state
        assertEquals(SSButtonState.IDLE, btnState)

        // Check if click callback received
        composeTestRule.onNodeWithText(btnText).performClick()
        assertTrue(btnClicked)

        // Advance animation time until button state matches LOADING state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil(
            condition = { btnState == SSButtonState.LOADING },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.LOADING, btnState)

        // Advance animation time until button state matches SUCCESS state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil (
            condition = { btnState == SSButtonState.SUCCESS },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.SUCCESS, btnState)

        // Advance animation time until button state matches IDLE state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil (
            condition = { btnState == SSButtonState.IDLE },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.IDLE, btnState)
    }

    @Test
    fun testButtonIdleLoadingFailureState() {
        val btnText = "Some Text"
        var btnClicked = false
        var btnState : SSButtonState = SSButtonState.IDLE

        // Start the app
        composeTestRule.setContent {
            AppTheme {
                val coroutineScope = rememberCoroutineScope()
                var submitButtonState by remember { mutableStateOf(SSButtonState.IDLE) }

                btnState = submitButtonState

                ProgressButton(
                    type = SSButtonType.CIRCLE,
                    onClick = {
                        coroutineScope.launch {
                            // Save click handled
                            btnClicked = true

                            // Test loading state
                            submitButtonState = SSButtonState.LOADING

                            // Wait for loading progress show
                            delay(1000)

                            // Show a success result
                            submitButtonState = SSButtonState.FAILURE
                        }
                    },
                    assetColor = Color.White,
                    buttonState = submitButtonState,
                    setButtonState = { submitButtonState = it },
                    text = btnText
                )
            }
        }

        // Wait for button visibility
        onView(isRoot()).perform(WaitFor.sleep(500))

        // Check if idle button is in idle state
        assertEquals(SSButtonState.IDLE, btnState)

        // Check if click callback received
        composeTestRule.onNodeWithText(btnText).performClick()
        assertTrue(btnClicked)

        // Advance animation time until button state matches LOADING state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil(
            condition = { btnState == SSButtonState.LOADING },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.LOADING, btnState)

        // Advance animation time until button state matches FAILURE state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil (
            condition = { btnState == SSButtonState.FAILURE },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.FAILURE, btnState)

        // Advance animation time until button state matches IDLE state with a max TIMEOUT millis
        composeTestRule.mainClock.advanceTimeUntil (
            condition = { btnState == SSButtonState.IDLE },
            timeoutMillis = 5000
        )
        assertEquals(SSButtonState.IDLE, btnState)
    }
}