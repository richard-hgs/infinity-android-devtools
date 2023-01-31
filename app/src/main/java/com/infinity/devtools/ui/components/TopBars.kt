@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.infinity.devtools.R

@Composable
fun AppTopBar(
    title: String = stringResource(R.string.app_name),
    homeIcon: ImageVector? = null,
    onHomeClick: (() -> Unit)? = null,
    onOptionsItemClick: ((menuItemId: Int) -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = homeIcon?.let {
            {
                Button(
                    shape = CircleShape,
                    content = {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ic_back)
                        )
                    },
                    onClick = { onHomeClick?.let { it() } },
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
        }
    )
}