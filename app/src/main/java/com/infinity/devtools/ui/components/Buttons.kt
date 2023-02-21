@file:Suppress("FunctionName", "UnresolvedReferences")

package com.infinity.devtools.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.infinity.devtools.constants.ConstantsUi.DEF_BTN_HEIGHT
import com.infinity.devtools.utils.MultipleEventsCutter
import com.infinity.devtools.utils.get
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSCustomLoadingEffect
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import com.simform.ssjetpackcomposeprogressbuttonlibrary.utils.ZERO
import com.simform.ssjetpackcomposeprogressbuttonlibrary.utils.thousand
import com.simform.ssjetpackcomposeprogressbuttonlibrary.utils.twenty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple ripple effect button
 *
 * @param onClick   Callback that handle clicks events
 * @param modifier  Modifier attributes
 * @param color     The color of the button
 * @param content   The content of the button
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: ButtonColors,
    content : @Composable RowScope.() -> Unit,
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }
    Button(
        onClick = { multipleEventsCutter.processEvent { onClick() }},
        modifier = modifier,
        content = content,
        colors = color,
    )
}

/**
 * A simple transparent button with ripple effect
 *
 * @param onClick   Callback that handle click events
 * @param modifier  Modifier attributes
 * @param content   Content of the transparent button
 */
@Composable
fun TransparentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content : @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        content = content,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
        ),
    )
}

/**
 * A custom button with a progress, error and success states
 *
 * @param modifier                  Modifier attributes
 * @param type                      Button type, one of [SSButtonType.CIRCLE], [SSButtonType.WHEEL] and so on...
 * @param width                     (Optional) Width of the button. If not informed fill max width
 * @param height                    (Optional) Height of the button. If not informed uses the [DEF_BTN_HEIGHT]
 * @param onClick                   Callback that will handle click events
 * @param assetColor                Color of the icons of the button
 * @param buttonState               State of this button, one of [SSButtonState.FAILURE], [SSButtonState.IDLE], [SSButtonState.LOADING], [SSButtonState.SUCCESS]
 * @param setButtonState            Change the button state
 * @param buttonBorderStroke        Border stroke of the button
 * @param blinkingIcon              The blink effect to be applied to [SSButtonState] icons
 * @param cornerRadius              Corner radius of the button.
 * @param speedMillis               The animation speed time of the button
 * @param enabled                   Enables or Disables the button
 * @param elevation                 Elevation that also adds shadow box effect
 * @param colors                    Colors of the button
 * @param padding                   Padding applied to the button
 * @param alphaValue                Transparency of the button
 * @param leftImagePainter          Changes left image icon
 * @param rightImagePainter         Changes right image icon
 * @param successIconPainter        Changes success icon
 * @param failureIconPainter        Changes failure icon
 * @param successIconColor          Success icon color
 * @param failureIconColor          Failure icon color
 * @param text                      Button text
 * @param textModifier              Modifier applied to button text
 * @param fontSize                  Font size applied to button text
 * @param fontStyle                 Font style applied to button text
 * @param fontFamily                Font family applied to button text
 * @param fontWeight                Font weight applied to button text
 * @param hourHandColor             ???
 * @param customLoadingIconPainter  Changes loading icon
 * @param customLoadingEffect       Changes loading effect
 * @param customLoadingPadding      Changes loading padding
 */
@Composable
fun ProgressButton(
    modifier: Modifier = Modifier,
    type: SSButtonType,
    width: Dp = (-1).dp,
    height: Dp = (-1).dp,
    onClick: () -> Unit,
    assetColor: Color,
    buttonState: SSButtonState,
    setButtonState: (SSButtonState) -> Unit,
    buttonBorderStroke: BorderStroke? = null,
    blinkingIcon: Boolean = false,
    cornerRadius: Int = twenty,
    speedMillis: Int = thousand,
    enabled: Boolean = true,
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    padding: PaddingValues = PaddingValues(0.dp),
    alphaValue: Float = 1f,
    leftImagePainter: Painter? = null,
    rightImagePainter: Painter? = null,
    successIconPainter: Painter = rememberVectorPainter(image = Icons.Default.Done),
    failureIconPainter: Painter = rememberVectorPainter(image = Icons.Outlined.Info),
    successIconColor: Color = assetColor,
    failureIconColor: Color = assetColor,
    text: String? = null,
    textModifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    hourHandColor: Color = Color.Black,
    customLoadingIconPainter: Painter = painterResource(id = com.simform.ssjetpackcomposeprogressbuttonlibrary.R.drawable.simform_logo),
    customLoadingEffect: SSCustomLoadingEffect = SSCustomLoadingEffect(
        rotation = false,
        zoomInOut = false,
        colorChanger = false
    ),
    customLoadingPadding: Int = ZERO
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        // verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints {
            val mWidth = if (width == (-1).dp) {
                maxWidth.value.dp
            } else {
                width
            }
            val mHeight = if (height == (-1).dp) {
                DEF_BTN_HEIGHT.dp
            } else {
                height
            }
            val multipleEventsCutter = remember { MultipleEventsCutter.get() }
            val coroutineScope = rememberCoroutineScope()

            // Get the maximum width value
            SSJetPackComposeProgressButton(
                type = type,
                width = mWidth,
                height = mHeight,
                onClick = {
                    multipleEventsCutter.processEvent {
                        // Check if click allowed
                        if (buttonState == SSButtonState.IDLE) {
                            // Notify click
                            onClick()

                            coroutineScope.launch {
                                // Wait success and idle animations end then allow click again
                                delay((speedMillis * 4).toLong())
                                setButtonState(SSButtonState.IDLE)
                            }
                        }
                    }
                },
                assetColor = assetColor,
                buttonState = buttonState,
                buttonBorderStroke = buttonBorderStroke,
                blinkingIcon = blinkingIcon,
                cornerRadius = cornerRadius,
                speedMillis = speedMillis,
                enabled = enabled,
                elevation = elevation,
                colors = colors,
                padding = padding,
                alphaValue = alphaValue,
                leftImagePainter = leftImagePainter,
                rightImagePainter = rightImagePainter,
                successIconPainter = successIconPainter,
                failureIconPainter = failureIconPainter,
                successIconColor = successIconColor,
                failureIconColor = failureIconColor,
                text = text,
                textModifier = textModifier,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                hourHandColor = hourHandColor,
                customLoadingIconPainter = customLoadingIconPainter,
                customLoadingEffect = customLoadingEffect,
                customLoadingPadding = customLoadingPadding
            )
        }
    }
}