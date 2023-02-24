@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement.navigation

import android.os.Bundle
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.infinity.devtools.ui.components.sharedelement.*

/**
 * Shared elements navigation controller component
 */
@Composable
fun SharedElementsNavHost(
    navController: SharedNavController,
    startDestination : String,
    builder: SharedNavGraphBuilder.() -> Unit
) {
    SharedElementsNavHost(
        navController,
        SharedNavGraphBuilder(startDestination)
            .apply(builder)
            .build()
    )
//
//    val navController = rememberNavController()
//    NavHost(
//        navController = navController,
//        startDestination = "Abc"
//    ) {
//        composable(
//            "Abc"
//        ) {
//            navController.navigate("")
//        }
//    }
//    SharedElementsRoot {
//
//    }
}

@Composable
fun SharedElementsNavHost(
    navController: SharedNavController,
    graph: SharedNavGraph
) {
    // Get current lifecycleOwner
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get the onBackPressed dispatcher and set in the navController
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val onBackPressedDispatcher = onBackPressedDispatcherOwner?.onBackPressedDispatcher

    // Set lifecycle owner to the nav controller
    navController.setLifecycleOwner(lifecycleOwner)

    // Set the onBackPressed dispatcher callback handler to the nav controller
    if (onBackPressedDispatcher != null) {
        navController.setOnBackPressedDispatcher(onBackPressedDispatcher)
    }

    // Ensure that the NavController only receives back events while
    // the SharedElementsNavHost is in composition
    DisposableEffect(navController) {
        navController.enableOnBackPressed(true)
        onDispose {
            navController.enableOnBackPressed(false)
        }
    }

    // Set navigation graph to the nav controller. Must be the last navController setup, because
    // after this the first layout is composed.
    navController.graph = graph

    // Shared elements root
    SharedElementsRoot {
        // Save the shared elements root scope
        navController.setSharedElementsRootScope(this)

        // Compose the current destination content on screen
        Crossfade(
            targetState = navController.currentDestination.value,
            animationSpec = tween(500)
        ) {
            val content = navController.getContentForDestination(it.route)
            content(it)
        }
    }
}

fun SharedNavGraphBuilder.composable(
    route: String,
    content: @Composable (SharedNavStackDest) -> Unit
) {
    addDestination(SharedNavDestination(route, content))
}

@Composable
fun TestSharedNavHost() {
    val navController = rememberSharedNavController()
    SharedElementsNavHost(
        navController = navController,
        startDestination = "Screen1"
    ) {
        composable(
            "Screen1",
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                Text(
                    modifier = Modifier.clickable {
                        val arguments = Bundle()
                        arguments.putString("text_share", "Shared Text")
                        navController.navigate("Screen2", arguments)
                    },
                    text = "Screen 1"
                )
                SharedElement(
                    key = "txt_1",
                    screenKey = "Screen1",
                    transitionSpec = SharedElementsTransitionSpec(
                        durationMillis = 1000,
                        fadeMode = FadeMode.Out
                    )
                ) {
                    Text(text = "Shared Text")
                }
            }
        }

        composable(
            "Screen2",
        ) { sharedNavStackDest ->
            Column {
                Text(
                    modifier = Modifier.clickable { navController.popBackStack() },
                    text = "Screen 2"
                )
                Spacer(modifier = Modifier.weight(1f))
                SharedElement(
                    key = "txt_1",
                    screenKey = "Screen2",
                    transitionSpec = SharedElementsTransitionSpec(
                        durationMillis = 1000,
                        fadeMode = FadeMode.Out
                    )
                ) {
                    Text(text = sharedNavStackDest.arguments!!.getString("text_share")!!)
                }
            }

        }
    }
}