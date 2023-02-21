package com.infinity.devtools.ui.components.sharedelement.navigation

import androidx.compose.runtime.Composable


data class SharedNavDestination(
    val route: String,
    val content: @Composable (SharedNavStackDest) -> Unit
)
