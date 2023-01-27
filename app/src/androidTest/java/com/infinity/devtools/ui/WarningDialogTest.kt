package com.infinity.devtools.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinity.devtools.R
import com.infinity.devtools.ui.components.WarningDialog
import com.infinity.devtools.ui.theme.AppTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WarningDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWarningDialog() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dialogOpen = mutableStateOf(true)
        val dialogMsg = "Dialog message"
        val dialogBtnOk = context.getString(R.string.err_dialog_ok)

        composeTestRule.setContent {
            AppTheme {
                WarningDialog(open = dialogOpen, msg = dialogMsg)
            }
        }

        // Check if dialog is open
        composeTestRule.onNodeWithText(dialogMsg).assertIsDisplayed()
        composeTestRule.onNodeWithText(dialogBtnOk).assertIsDisplayed()

        // Click dialog OK Button
        composeTestRule.onNodeWithText(dialogBtnOk).performClick()

        // Check if dialog is closed
        composeTestRule.onNodeWithText(dialogMsg).assertDoesNotExist()
        composeTestRule.onNodeWithText(dialogBtnOk).assertDoesNotExist()
        assertFalse(dialogOpen.value)
    }
}