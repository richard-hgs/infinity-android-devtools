@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.infinity.devtools.ui.components.sharedelementold.SharedElement
import com.infinity.devtools.ui.components.sharedelementold.SharedElementsTransitionSpec

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
    sharedElTransitionKey: String? = null,
    sharedElTransitionScreenKey: String? = null,
    sharedElTransitionSpec: SharedElementsTransitionSpec? = null,
    sharedElTransitionEnd: Boolean = false
) {
    // Fix caret position at first time TextFieldValue is filled text selection is set to zero.
    var lastFixCaretValue by remember { mutableStateOf(text) }
    // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
    // of the composition.
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = text)) }
    // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
    // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
    // composition.
    val textFieldValue = textFieldValueState.copy(text = text)
    // Last String value that either text field was recomposed with or updated in the onValueChange
    // callback. We keep track of it to prevent calling onValueChange(String) for same String when
    // CoreTextField's onValueChange is called multiple times without recomposition in between.
    var lastTextValue by remember(text) { mutableStateOf(text) }

    // Each time Text changes
    LaunchedEffect(text) {
        // Check if caret position should be fixed
        if (text.length > lastFixCaretValue.length) {
            textFieldValueState = textFieldValueState.copy(text = text, selection = TextRange(text.length))
        }
        lastFixCaretValue = text
    }

    AppTextField(
        modifier = modifier,
        modifierLength = modifierLength,
        text = textFieldValue,
        fontSize = fontSize,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        onChange = { newTextFieldValueState ->
            textFieldValueState = newTextFieldValueState

            val stringChangedSinceLastInvocation = lastTextValue != newTextFieldValueState.text
            lastTextValue = newTextFieldValueState.text

            if (stringChangedSinceLastInvocation) {
                onChange(newTextFieldValueState.text)
            }
        },
        imeAction = imeAction,
        keyboardType = keyboardType,
        keyBoardActions = keyBoardActions,
        isEnabled = isEnabled,
        singleLine = singleLine,
        maxLength = maxLength,
        isPassword = isPassword,
        sharedElTransitionKey = sharedElTransitionKey,
        sharedElTransitionScreenKey = sharedElTransitionScreenKey,
        sharedElTransitionSpec = sharedElTransitionSpec,
        sharedElTransitionEnd = sharedElTransitionEnd
    )
}

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    modifierLength: Modifier = Modifier,
    text: TextFieldValue,
    fontSize: TextUnit = 16.sp,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onChange: (TextFieldValue) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyBoardActions: KeyboardActions = KeyboardActions(),
    isEnabled: Boolean = true,
    singleLine: Boolean = true,
    maxLength: Int? = null,
    isPassword: Boolean = false,
    sharedElTransitionKey: String? = null,
    sharedElTransitionScreenKey: String? = null,
    sharedElTransitionSpec: SharedElementsTransitionSpec? = null,
    sharedElTransitionEnd: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    val labelFontSize by animateIntAsState(
        targetValue = if (isFocused) 12 else fontSize.value.toInt(), animationSpec = tween(
            durationMillis = 300, delayMillis = 0
        )
    )

    val passwordVisible by rememberSaveable { mutableStateOf(false) }

    var colors = TextFieldDefaults.outlinedTextFieldColors()

    if (sharedElTransitionKey != null && sharedElTransitionScreenKey != null && sharedElTransitionSpec != null && !sharedElTransitionEnd) {
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.Transparent
        )
    }

    ConstraintLayout {
        val (textFieldRef, textLengthRef, sharedElText) = createRefs()
        if (sharedElTransitionKey != null && sharedElTransitionScreenKey != null && sharedElTransitionSpec != null && !sharedElTransitionEnd) {
            Column(modifier = Modifier.constrainAs(sharedElText) {
                top.linkTo(textFieldRef.top, 8.dp)
                start.linkTo(textFieldRef.start, 12.dp)
                bottom.linkTo(textFieldRef.bottom)
                height = Dimension.fillToConstraints
            },
                verticalArrangement = Arrangement.Center
            ) {
                SharedElement(
                    key = sharedElTransitionKey,
                    screenKey = sharedElTransitionScreenKey,
                    transitionSpec = sharedElTransitionSpec
                ) {
                    Text(text = text.text, style = TextStyle(fontSize = fontSize))
                }
            }

        }
        CustomOutlinedTextField(
            modifier = modifier.onFocusChanged {
                isFocused = it.hasFocus
            }.constrainAs(textFieldRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            value = text,
            onValueChange = {
                if (maxLength != null) {
                    onChange(
                        it.copy(text = it.text.take(maxLength))
                    )
                } else {
                    onChange(it)
                }
            },
            leadingIcon = leadingIcon,
            textStyle = TextStyle(fontSize = fontSize),
            colors = colors,
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
                text = "${text.text.length} / $maxLength",
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.caption,
                modifier = modifierLength.fillMaxWidth()
                    .padding(end = 16.dp)
                    .constrainAs(textLengthRef) {
                        top.linkTo(textFieldRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
                value = value.text,
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
        value = TextFieldValue(text = ""),
        onValueChange = {

        },
        label = {
            Text(text = "Outlined Text Field", style = TextStyle(fontSize = 16.sp, color = Color.Gray))
        }
    )
}