package com.infinity.devtools.ui.components.sharedelement.navigation

import androidx.compose.runtime.Composable

/**
 * Created by richard on 19/02/2023 15:02
 *
 */
data class SharedNavDestination(
    val route: String,
    val content: @Composable () -> Unit
) {
    companion object {
        val EMPTY = SharedNavDestination("") {}
    }
}
