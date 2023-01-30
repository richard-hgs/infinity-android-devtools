package com.infinity.devtools.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinity.devtools.R
import com.infinity.devtools.ui.components.ColumnScrollbar
import com.infinity.devtools.ui.theme.AppTheme
import com.infinity.devtools.utils.WaitFor
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class ScrollBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScrollBarThumb() {
        val thumbTAG = "THUMB_TAG"
        val contentTAG = "CONTENT_TAG"

        // Start the app
        composeTestRule.setContent {
            AppTheme {
                val coroutineScope = rememberCoroutineScope()
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier.padding(all = 8.dp)
                ) {
                    ColumnScrollbar(
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f),
                        state = scrollState,
                        thumbModifier = Modifier.testTag(thumbTAG)
                    ) { modifier ->
                        Column(
                            modifier = modifier
                                .testTag(contentTAG)
                                .semantics {
                                    scrollToIndex {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            sleep(1000)
                                            scrollState.animateScrollTo(
                                                value = scrollState.maxValue,
                                            )
                                        }
                                        true
                                    }
                                }
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_mysql),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                                    .align(alignment = Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }

        // Check if in IDLE state THUMB is not displayed
        val semProgressVal = composeTestRule
            .onNodeWithTag(thumbTAG)
            .fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
            .current
        assertEquals(14f, semProgressVal)

        // Performs scroll to last item and check if THUMB is displayed
        composeTestRule.onNodeWithTag(contentTAG).performScrollToIndex(6)

        // Check if in SCROLL state THUMB is displayed. This method blocks until condition is satisfied or timeout is thrown
        WaitFor.valueCheckerBlocking(
            composeTestRule = composeTestRule,
            expectedValue = 0f,
            currentValue =  {
                val current = composeTestRule
                    .onNodeWithTag(thumbTAG)
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current
                current
            }
        )

        // Advance animation to make value checker easy
        composeTestRule.mainClock.advanceTimeBy(5000)

        // Check if in IDLE state THUMB is not displayed. This method blocks until condition is satisfied or timeout is thrown
        WaitFor.valueCheckerBlocking(
            composeTestRule = composeTestRule,
            expectedValue = 14f,
            currentValue =  {
                val current = composeTestRule
                    .onNodeWithTag(thumbTAG)
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current

                current
            }
        )
    }
}