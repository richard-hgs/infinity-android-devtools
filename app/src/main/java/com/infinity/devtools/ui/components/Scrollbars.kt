@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.launch

/**
 * Scrollbar selection modes.
 */
enum class ScrollbarSelectionMode {
    /**
     * Enable selection in the whole scrollbar and thumb
     */
    Full,

    /**
     * Enable selection in the thumb
     */
    Thumb,

    /**
     * Disable selection
     */
    Disabled
}

/**
 * Scrollbar for Column composable contents
 *
 * @param modifier              Modifier attributes
 * @param state                 State of the content scroll
 * @param rightSide             Thumb position: true -> right, false -> left
 * @param thickness             Thickness of the scrollbar thumb
 * @param padding               Padding of the scrollbar
 * @param thumbModifier         Thumb modifier attributes
 * @param thumbMinHeight        Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 * @param thumbColor            Thumb color
 * @param thumbSelectedColor    Thumb selected color
 * @param thumbShape            Thumb shape form
 * @param enabled               True=Scrollbar thumb enabled, False=Scrollbar thumb disabled
 * @param selectionMode         [ScrollbarSelectionMode.Disabled] Disable thumb selection,
 * [ScrollbarSelectionMode.Full] Allow thumb and scrollbar selection, [ScrollbarSelectionMode.Disabled] Disables selection in thumb and scrollbar.
 * @param indicatorContent      Thumb indicator composable content
 * @param content               Scrollable composable content
 */
@Composable
fun ColumnScrollbar(
    modifier: Modifier = Modifier,
    state: ScrollState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 2.dp,
    thumbModifier: Modifier = Modifier,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    enabled: Boolean = true,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable (modifier: Modifier) -> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    val isInAction = state.isScrollInProgress || isSelected

    val displacement by animateFloatAsState(
            targetValue = if (isInAction) 0f else 14f, animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 100 else 500
        )
    )

    if (!enabled) content(Modifier.padding(end = max(0.dp, 14.dp - displacement.dp)))
    else BoxWithConstraints(modifier = modifier) {
        content(Modifier.padding(end = max(0.dp, 14.dp - displacement.dp)))
        ColumnScrollbar(
            state = state,
            rightSide = rightSide,
            thickness = thickness,
            padding = padding,
            thumbModifier = thumbModifier,
            thumbMinHeight = thumbMinHeight,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            visibleHeightDp = with(LocalDensity.current) { constraints.maxHeight.toDp() },
            indicatorContent = indicatorContent,
            selectionMode = selectionMode,
            isSelected = isSelected,
            setIsSelected = {
                isSelected = it
            },
            isInAction = isInAction,
            displacement = displacement
        )
    }
}

/**
 * Scrollbar for Column composable content
 *
 * @param state                 State of the content scroll
 * @param rightSide             true -> right,  false -> left
 * @param thickness             Thickness of the scrollbar thumb
 * @param padding               Padding of the scrollbar
 * @param thumbModifier         Thumb modifier attributes
 * @param thumbMinHeight        Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 * @param thumbColor            Thumb color
 * @param thumbSelectedColor    Thumb selected color
 * @param thumbShape            Thumb shape form
 * @param selectionMode         [ScrollbarSelectionMode.Disabled] Disable thumb selection,
 * [ScrollbarSelectionMode.Full] Allow thumb and scrollbar selection, [ScrollbarSelectionMode.Disabled] Disables selection in thumb and scrollbar.
 * @param indicatorContent      Thumb indicator composable content
 * @param visibleHeightDp       Visible height of column view
 * @param isSelected            True=Thumb or scrollbar is selected, False=Not selected
 * @param setIsSelected         Set Thumb or scrollbar selected state
 * @param isInAction            True=Scroll in progress, or thumb scroll in progress, False=No Scroll changes
 * @param displacement          Thumb right position displacement, usually animated value when scroll is in action
 */
@Composable
fun ColumnScrollbar(
    state: ScrollState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbModifier: Modifier = Modifier,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    visibleHeightDp: Dp,
    isSelected: Boolean,
    setIsSelected: (Boolean) -> Unit,
    isInAction: Boolean,
    displacement: Float
) {
    val coroutineScope = rememberCoroutineScope()

    var dragOffset by remember { mutableStateOf(0f) }

    val fullHeightDp = with(LocalDensity.current) { state.maxValue.toDp() + visibleHeightDp }

    val normalizedThumbSizeReal by remember(visibleHeightDp, state.maxValue) {
        derivedStateOf {
            if (fullHeightDp == 0.dp) 1f else {
                val normalizedDp = visibleHeightDp / fullHeightDp
                normalizedDp.coerceIn(0f, 1f)
            }
        }
    }

    val normalizedThumbSize by remember(normalizedThumbSizeReal) {
        derivedStateOf {
            normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight)
        }
    }

    val normalizedThumbSizeUpdated by rememberUpdatedState(newValue = normalizedThumbSize)

    fun offsetCorrection(top: Float): Float {
        val topRealMax = 1f
        val topMax = 1f - normalizedThumbSizeUpdated
        return top * topMax / topRealMax
    }

    fun offsetCorrectionInverse(top: Float): Float {
        val topRealMax = 1f
        val topMax = 1f - normalizedThumbSizeUpdated
        if (topMax == 0f) return top
        return (top * topRealMax / topMax).coerceAtLeast(0f)
    }

    val normalizedOffsetPosition by remember {
        derivedStateOf {
            if (state.maxValue == 0) return@derivedStateOf 0f
            val normalized = state.value.toFloat() / state.maxValue.toFloat()
            offsetCorrection(normalized)
        }
    }

    fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        dragOffset = value.coerceIn(0f, maxValue)
    }

    fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val exactIndex = offsetCorrectionInverse(state.maxValue * dragOffset).toInt()
        coroutineScope.launch {
            state.scrollTo(exactIndex)
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isInAction) 1f else 0f, animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 100 else 500
        )
    )

    BoxWithConstraints(
        Modifier
            .alpha(alpha)
            .fillMaxWidth()
    ) {
        if (indicatorContent != null) BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                    translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition
                }) {
            ConstraintLayout(
                Modifier.align(Alignment.TopEnd)
            ) {
                val (box, content) = createRefs()
                Box(
                    Modifier
                        .fillMaxHeight(normalizedThumbSize)
                        .padding(
                            start = if (rightSide) 0.dp else padding,
                            end = if (!rightSide) 0.dp else padding,
                        )
                        .width(thickness)
                        .constrainAs(box) {
                            if (rightSide) end.linkTo(parent.end)
                            else start.linkTo(parent.start)
                        }) {}

                Box(Modifier.constrainAs(content) {
                    top.linkTo(box.top)
                    bottom.linkTo(box.bottom)
                    if (rightSide) end.linkTo(box.start)
                    else start.linkTo(box.end)
                }) {
                    indicatorContent(
                        offsetCorrectionInverse(normalizedOffsetPosition),
                        isSelected
                    )
                }
            }
        }

        BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (isSelected) {
                            setScrollOffset(dragOffset + delta / constraints.maxHeight.toFloat())
                        }
                    },
                    orientation = Orientation.Vertical,
                    enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                    startDragImmediately = true,
                    onDragStarted = { offset ->
                        val newOffset = offset.y / constraints.maxHeight.toFloat()
                        val currentOffset = normalizedOffsetPosition
                        when (selectionMode) {
                            ScrollbarSelectionMode.Full -> {
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated))
                                    setDragOffset(currentOffset)
                                else
                                    setScrollOffset(newOffset)
                                setIsSelected(true)
                            }
                            ScrollbarSelectionMode.Thumb -> {
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated)) {
                                    setDragOffset(currentOffset)
                                    setIsSelected(true)
                                }
                            }
                            ScrollbarSelectionMode.Disabled -> Unit
                        }
                    },
                    onDragStopped = {
                        setIsSelected(false)
                    })
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                }) {
            Box(
                thumbModifier
                    .progressSemantics(displacement, -14f..14f)
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition
                    }
                    .padding(horizontal = padding)
                    .width(thickness)
                    .clip(thumbShape)
                    .background(if (isSelected) thumbSelectedColor else thumbColor)
                    .fillMaxHeight(normalizedThumbSize))
        }
    }
}