@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    modifierLength: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 16.sp,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onChange: (String) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyBoardActions: KeyboardActions = KeyboardActions(),
    isEnabled: Boolean = true,
    singleLine: Boolean = true,
    maxLength: Int? = null,
    isPassword: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    val labelFontSize by animateIntAsState(
        targetValue = if (isFocused) 12 else fontSize.value.toInt(), animationSpec = tween(
            durationMillis = 300, delayMillis = 0
        )
    )

    var passwordVisible by rememberSaveable { mutableStateOf(false) }



    CustomOutlinedTextField(
        modifier = modifier.onFocusChanged {
            isFocused = it.hasFocus
        },
        value = text,
        onValueChange = {
            if (maxLength != null) {
                onChange(it.take(maxLength))
            } else {
                onChange(it)
            }
        },
        leadingIcon = leadingIcon,
        textStyle = TextStyle(fontSize = fontSize),
        keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
        keyboardActions = keyBoardActions,
        enabled = isEnabled,
        singleLine = singleLine,
//        colors = TextFieldDefaults.outlinedTextFieldColors(
//            focusedBorderColor = Color.Black,
//            unfocusedBorderColor = Color.Gray,
//            disabledBorderColor = Color.Gray,
//            disabledTextColor = Color.Black
//        ),
        label = {
            Text(text = placeholder, style = TextStyle(fontSize = labelFontSize.sp, color = Color.Gray))
        },
        visualTransformation = if (!isPassword || passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (isPassword) {
            null
//            {
//                val image = if (passwordVisible)
//                    Icons.Default.
//                else Icons.Filled.VisibilityOff
//            }
        } else {
            null
        }
    )
    if (maxLength != null) {
        Text(
            text = "${text.length} / $maxLength",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.caption,
            modifier = modifierLength.fillMaxWidth().padding(end = 16.dp)
        )
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    contentPadding: PaddingValues = PaddingValues(12.dp, 12.dp, 12.dp, 12.dp)
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
        value = value,
        modifier = if (label != null) {
            modifier
                // Merge semantics at the beginning of the modifier chain to ensure padding is
                // considered part of the text field.
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp)
        } else {
            modifier
        }
        .background(colors.backgroundColor(enabled).value, shape)
        .defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = 20.dp
        ),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = contentPadding,
                border = {
                    TextFieldDefaults.BorderBox(
                        enabled,
                        isError,
                        interactionSource,
                        colors,
                        shape
                    )
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TextFieldPreview() {
    CustomOutlinedTextField(
        value = "",
        onValueChange = {

        },
        label = {
            Text(text = "Outlined Text Field", style = TextStyle(fontSize = 16.sp, color = Color.Gray))
        }
    )
}