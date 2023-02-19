@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

/**
 * Created by richard on 19/02/2023 14:20
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
    navController.graph = graph

    Crossfade(navController.currentDestination.value) {
        it.content()
    }
}

fun SharedNavGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    addDestination(SharedNavDestination(route, content))
}

@Composable
fun TestSharedNavHost() {
    SharedElementsNavHost(
        navController = rememberSharedNavController(),
        startDestination = "Screen1"
    ) {
        composable(
            "Screen1",
        ) {
            Text(text = "Screen 1")
        }

        composable(
            "Screen2",
        ) {
            Text(text = "Screen 2")
        }
    }
}