package com.infinity.devtools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.imeAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinity.devtools.ui.components.AppTextField
import com.infinity.devtools.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTextField() {
        val textFieldTag = "TEXT_FIELD_TAG"
        val textFieldLenTag = "TEXT_FIELD_LEN_TAG"
        val textFieldTag2 = "TEXT_FIELD_TAG2"
        val textFieldLenTag2 = "TEXT_FIELD_LEN_TAG2"
        val textFieldPlaceHolder = "Text Field"
        val textField2PlaceHolder = "Text Field 2"
        val textState = mutableStateOf("")
        val textState2 = mutableStateOf("")

        val smallTestText = "Some small test text to field"
        val largeTestText = "Some large test text to field that surpass its max length of 45 characters"

        composeTestRule.setContent {
            AppTheme {
                val focusManager = LocalFocusManager.current
                var text by remember { textState }
                var text2 by remember { textState2 }

                Column {
                    AppTextField(
                        modifier = Modifier.testTag(textFieldTag).semantics { imeAction = ImeAction.Next },
                        modifierLength = Modifier.testTag(textFieldLenTag),
                        text = text,
                        onChange = {
                            text = it
                        },
                        placeholder = textFieldPlaceHolder,
                        maxLength = 45,
                    )

                    AppTextField(
                        modifier = Modifier.testTag(textFieldTag2).semantics { imeAction = ImeAction.Done },
                        modifierLength = Modifier.testTag(textFieldLenTag2),
                        text = text2,
                        onChange = {
                            text2 = it
                        },
                        placeholder = textField2PlaceHolder,
                        maxLength = 45,
                        imeAction = ImeAction.Done,
                        keyBoardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )
                }
            }
        }

        // Check if TextField is empty
        composeTestRule.onNodeWithTag(textFieldTag).assert(hasText(""))
        composeTestRule.onNodeWithTag(textFieldLenTag).assert(hasText("0 / 45"))

        // Input some text in TextField
        composeTestRule.onNodeWithTag(textFieldTag).performTextInput(smallTestText)
        composeTestRule.onNodeWithTag(textFieldLenTag).assert(hasText("29 / 45"))

        // Check if TextField has given text
        composeTestRule.onNodeWithTag(textFieldTag).assert(hasText(smallTestText))
        assertEquals(smallTestText, textState.value)

        // Check if TextField clear its text contents
        composeTestRule.onNodeWithTag(textFieldTag).performTextClearance()
        composeTestRule.onNodeWithTag(textFieldLenTag).assert(hasText("0 / 45"))
        assertEquals("", textState.value)

        // Check if TextField surpass its max length
        composeTestRule.onNodeWithTag(textFieldTag).performTextInput(largeTestText)
        composeTestRule.onNodeWithTag(textFieldLenTag).assert(hasText("45 / 45"))
        composeTestRule.onNodeWithTag(textFieldTag).assert(hasText("Some large test text to field that surpass it"))
        assertEquals("Some large test text to field that surpass it", textState.value)

        // Check if TextField ImeAction.Next is working
        composeTestRule.onNodeWithTag(textFieldTag).performImeAction()

        // Check if TextField2 has focus
        composeTestRule.onNodeWithTag(textFieldTag2).assertIsFocused()

        // Check if TextField ImeAction.Done is working
        composeTestRule.onNodeWithTag(textFieldTag2).performImeAction()
        composeTestRule.onNodeWithTag(textFieldTag).assertIsNotFocused()
        composeTestRule.onNodeWithTag(textFieldTag2).assertIsNotFocused()
    }

    fun ComposeContentTestRule.waitUntilNodeCount(
        matcher: SemanticsMatcher,
        count: Int,
        timeoutMillis: Long = 1_000L
    ) {
        this.waitUntil(timeoutMillis) {
            this.onAllNodes(matcher).fetchSemanticsNodes().size == count
        }
    }
}