@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.infinity.devtools.R
import kotlin.math.max

/**
 * Animated warning dialog
 *
 * @param open  True=Shows dialog, False=Hides dialog
 * @param msg   The message to be displayed
 * @param icon  Composable icon to be displayed
 * @param title Composable title to be displayed
 * @param text  Composable text that uses the msg as content to be displayed
 * @param buttons Composable buttons of dialog to be displayed
 */
@Composable
fun WarningDialog(
    open: MutableState<Boolean>,
    msg: String,
    icon: @Composable () -> Unit = {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_warning))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            clipSpec = LottieClipSpec.Frame(min = 10, max = 120),
            speed = .5f
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                modifier = Modifier.size(80.dp),
                composition = composition,
                progress = { progress },
            )
        }
    },
    title: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.err_dialog_warning)
            )
        }
    },
    text: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = msg
            )
        }
    },
    buttons: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    open.value = false
                }) {
                Text(stringResource(R.string.err_dialog_ok))
            }
            Spacer(modifier = Modifier.height(height = TitleBaselineDistanceFromTop.value.dp))
        }
    }
) {
    BaseDialog(
        open = open,
        msg = msg,
        icon = icon,
        title = title,
        text = text,
        buttons = buttons,
        surfaceShape = RoundedCornerShape(16.dp)
    )
}

/**
 * Base dialog composable used by all dialogs
 *
 * @param open              True=Shows dialog, False=Hides dialog
 * @param msg               The message to be displayed
 * @param backgroundColor   Background color of the dialog
 * @param contentColor      Content color of the dialog
 * @param surfaceShape      Shape of the surface of the dialog
 * @param icon              Composable icon of the dialog
 * @param title             Composable title of the dialog
 * @param text              Composable text of the dialog that uses msg as content
 * @param buttons           Composable buttons of the dialog
 */
@Composable
fun BaseDialog(
    open: MutableState<Boolean>,
    msg: String,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    surfaceShape: Shape = MaterialTheme.shapes.medium,
    icon: @Composable () -> Unit = {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_warning))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            clipSpec = LottieClipSpec.Frame(min = 10, max = 120),
            speed = .5f
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                modifier = Modifier.size(80.dp),
                composition = composition,
                progress = { progress },
            )
        }
    },
    title: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.err_dialog_warning)
            )
        }
    },
    text: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = msg
            )
        }
    },
    buttons: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    open.value = false
                }) {
                Text(stringResource(R.string.err_dialog_ok))
            }
            Spacer(modifier = Modifier.height(height = TitleBaselineDistanceFromTop.value.dp))
        }
    }
) {
    if (open.value) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties()
        ) {
            Surface(
                shape = surfaceShape,
                color = backgroundColor,
                contentColor = contentColor
            ) {
                Column {
                    AlertDialogBaselineLayout(
                        icon = icon,
                        title = title.let {
                            @Composable {
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                                    val textStyle = MaterialTheme.typography.subtitle1
                                    ProvideTextStyle(textStyle, title)
                                }
                            }
                        },
                        text = text.let {
                            @Composable {
                                CompositionLocalProvider(
                                    LocalContentAlpha provides ContentAlpha.medium
                                ) {
                                    val textStyle = MaterialTheme.typography.body2
                                    ProvideTextStyle(textStyle, text)
                                }
                            }
                        }
                    )
                    buttons()
                }
            }
        }
    }
}


/**
 * Layout that will add spacing between the top of the layout and [title]'s first baseline, and
 * [title]'s last baseline and [text]'s first baseline.
 *
 * If [title] and/or [text] do not have any baselines, the spacing will just be applied from the
 * edge of their layouts instead as a best effort implementation.
 */
@Composable
internal fun ColumnScope.AlertDialogBaselineLayout(
    icon: @Composable (() -> Unit)?,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?
) {
    Layout(
        {
            icon?.let { icon ->
                Box(TitlePadding.layoutId("icon").align(Alignment.Start)) {
                    icon()
                }
            }
            title?.let { title ->
                Box(TitlePadding.layoutId("title").align(Alignment.Start)) {
                    title()
                }
            }
            text?.let { text ->
                Box(TextPadding.layoutId("text").align(Alignment.Start)) {
                    text()
                }
            }
        },
        Modifier.weight(1f, false)
    ) { measurables, constraints ->
        // Measure with loose constraints for height as we don't want the text to take up more
        // space than it needs
        val iconPlaceable = measurables.firstOrNull { it.layoutId == "icon" }?.measure(
            constraints.copy(minHeight = 0)
        )
        val titlePlaceable = measurables.firstOrNull { it.layoutId == "title" }?.measure(
            constraints.copy(minHeight = 0)
        )
        val textPlaceable = measurables.firstOrNull { it.layoutId == "text" }?.measure(
            constraints.copy(minHeight = 0)
        )

        val layoutWidth = max(
            max(titlePlaceable?.width ?: 0, textPlaceable?.width ?: 0),
            iconPlaceable?.width ?: 0
        )

        val firstTitleBaseline = titlePlaceable?.get(FirstBaseline)?.let { baseline ->
            if (baseline == AlignmentLine.Unspecified) null else baseline
        } ?: 0
        val lastTitleBaseline = titlePlaceable?.get(LastBaseline)?.let { baseline ->
            if (baseline == AlignmentLine.Unspecified) null else baseline
        } ?: 0

        val iconOffset = TitleBaselineDistanceFromTop.roundToPx()

        val iconPositionY = iconOffset

        val titleOffset = TitleBaselineDistanceFromTop.roundToPx()

        // Place the title so that its first baseline is titleOffset from the top
        val titlePositionY = if (iconPlaceable == null) {
            titleOffset - firstTitleBaseline
        } else {
            titleOffset + (iconPlaceable.height + iconPositionY) - firstTitleBaseline
        }

        val firstTextBaseline = textPlaceable?.get(FirstBaseline)?.let { baseline ->
            if (baseline == AlignmentLine.Unspecified) null else baseline
        } ?: 0

        val textOffset = if (titlePlaceable == null) {
            TextBaselineDistanceFromTop.roundToPx()
        } else {
            TextBaselineDistanceFromTitle.roundToPx()
        }

        // Combined height of title and spacing above
        val titleHeightWithSpacing = titlePlaceable?.let { it.height + titlePositionY } ?: 0

        // Align the bottom baseline of the text with the bottom baseline of the title, and then
        // add the offset
        val textPositionY = if (titlePlaceable == null) {
            // If there is no title, just place the text offset from the top of the dialog
            textOffset - firstTextBaseline
        } else {
            if (lastTitleBaseline == 0) {
                // If `title` has no baseline, just place the text's baseline textOffset from the
                // bottom of the title
                titleHeightWithSpacing - firstTextBaseline + textOffset
            } else {
                // Otherwise place the text's baseline textOffset from the title's last baseline
                (titlePositionY + lastTitleBaseline) - firstTextBaseline + textOffset
            }
        }

        // Combined height of text and spacing above
        val textHeightWithSpacing = textPlaceable?.let {
            if (lastTitleBaseline == 0) {
                textPlaceable.height + textOffset - firstTextBaseline
            } else {
                textPlaceable.height + textOffset - firstTextBaseline -
                    ((titlePlaceable?.height ?: 0) - lastTitleBaseline)
            }
        } ?: 0

        val layoutHeight = titleHeightWithSpacing + textHeightWithSpacing

        layout(layoutWidth, layoutHeight) {
            iconPlaceable?.place(0, iconPositionY)
            titlePlaceable?.place(0, titlePositionY)
            textPlaceable?.place(0, textPositionY)
        }
    }
}

/**
 * Padding used by title composable of the [BaseDialog]
 */
private val TitlePadding = Modifier.padding(start = 24.dp, end = 24.dp)
/**
 * Padding used by text message composable of the [BaseDialog]
 */
private val TextPadding = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)

/**
 * Baseline distance from the first line of the title to the top of the dialog
 */
private val TitleBaselineDistanceFromTop = 20.sp

/**
 * Baseline distance from the first line of the text to the last line of the title
 */
private val TextBaselineDistanceFromTitle = 30.sp

/**
 * For dialogs with no title, baseline distance from the first line of the text to the top of the dialog
 */
private val TextBaselineDistanceFromTop = 38.sp

/**
 * Composable preview of the [WarningDialog]
 */
@Preview(showBackground = true)
@Composable
fun PreviewDialogWarning() {
    val open = remember { mutableStateOf(true) }
    WarningDialog(
        open = open,
        msg = "A Test Warning"
    )
    Button(
        onClick = {
            open.value = true
        }) {
        Text("Click to open dialog")
    }
}
