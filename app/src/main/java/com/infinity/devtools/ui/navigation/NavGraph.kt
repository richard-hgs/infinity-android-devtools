@file:Suppress("FunctionName")

package com.infinity.devtools.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.infinity.devtools.constants.ConstantsDb.MYSQL_CONN_ID
import com.infinity.devtools.ui.presentation.MysqlConnsScreen
import com.infinity.devtools.ui.presentation.NewMysqlConnScreen
import com.infinity.devtools.ui.presentation.SplashScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
) {
    val configuration = LocalConfiguration.current
    val densityConf = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp
    val screenWidthPx = densityConf.run { screenWidthDp.dp.toPx()}

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
//        val springSpec = spring<IntOffset>(dampingRatio = Spring.DampingRatioMediumBouncy)
        val tweenSpec = tween<IntOffset>(durationMillis = 500)
        val offset = screenWidthPx.toInt()

        composable(
            route = Screen.SplashScreen.route,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
            }
        ) {
            SplashScreen(
                navigateToHomeScreen = {
                    navController.navigate(Screen.MysqlConnsScreen.route) {
                        popUpTo(Screen.SplashScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.MysqlConnsScreen.route,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
            }
        ) {
            MysqlConnsScreen(
                navigateToUpdateMysqlConnScreen = { connId ->
                    navController.navigate("${Screen.UpdateMysqlConnScreen.route}/${connId}")
                },
                navigateToInsertMysqlConnScreen = {
                    navController.navigate(Screen.NewMysqlConnScreen.route)
                },
                navigateToMysqlDbHomeScreen = {

                }
            )
        }

        composable(
            route = Screen.NewMysqlConnScreen.route,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
            }
        ) {
            NewMysqlConnScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Screen.UpdateMysqlConnScreen.route}/{$MYSQL_CONN_ID}",
            arguments = listOf(
                navArgument(MYSQL_CONN_ID) {
                    type = IntType
                }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
            }
        ) { backStackEntry ->
            val connId = backStackEntry.arguments?.getInt(MYSQL_CONN_ID) ?: 0
            NewMysqlConnScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                connId = connId
            )
        }
    }
}