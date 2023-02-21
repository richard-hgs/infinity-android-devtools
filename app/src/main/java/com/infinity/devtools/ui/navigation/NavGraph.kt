@file:Suppress("FunctionName")

package com.infinity.devtools.ui.navigation

import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.infinity.devtools.model.sqlite.MysqlConn
import com.infinity.devtools.ui.components.sharedelement.navigation.SharedElementsNavHost
import com.infinity.devtools.ui.components.sharedelement.navigation.composable
import com.infinity.devtools.ui.components.sharedelement.navigation.rememberSharedNavController
import com.infinity.devtools.ui.presentation.EditMysqlConnScreen
import com.infinity.devtools.ui.presentation.MysqlConnScreen
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
            // States shared between multiple compositions - START
            val listState = rememberLazyListState()
            // States shared between multiple compositions - END

            val sharedNavController = rememberSharedNavController()
            SharedElementsNavHost(
                navController = sharedNavController,
                startDestination = Screen.MysqlConnsScreen.route
            ) {
                composable(
                    route = Screen.MysqlConnsScreen.route
                ) {
                    MysqlConnScreen(
                        listState,
                        navigateToEditScreen = {
                            val arguments = Bundle()
                            arguments.putParcelable("conn", it)
                            sharedNavController.navigate(
                                Screen.NewMysqlConnScreen.route,
                                arguments,
                                "img_${it?.id}", "name_${it?.id}", "host_${it?.id}", "port_${it?.id}"
                            )
                        },
                        navigateToNewScreen = { sharedNavController.navigate(Screen.NewMysqlConnScreen.route) }
                    )
                }

                composable(
                    route = Screen.NewMysqlConnScreen.route
                ) {
                    val arguments = it.arguments
                    @Suppress("DEPRECATION")
                    val conn : MysqlConn? = arguments?.getParcelable("conn")

                    EditMysqlConnScreen(
                        sharedNavController.getSharedElementsRootScope(),
                        conn,
                        navigateBack = {
                            sharedNavController.popBackStack()
                        }
                    )
                }
            }

//            Screen.MysqlConnsScreen(
//                navigateToUpdateMysqlConnScreen = { connId ->
//                    navController.navigate("${Screen.UpdateMysqlConnScreen.route}/${connId}")
//                },
//                navigateToInsertMysqlConnScreen = {
//                    navController.navigate(Screen.NewMysqlConnScreen.route)
//                },
//                navigateToMysqlDbHomeScreen = {
//                    navController.navigate(Screen.ServerConnHomeScreen.route)
//                }
//            )
        }

//        composable(
//            route = Screen.MysqlConnsScreen.route,
//            enterTransition = {
//                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
//            },
//            exitTransition = {
//                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popEnterTransition = {
//                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popExitTransition = {
//                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
//            }
//        ) {
//            MysqlConnsScreen(
//                navigateToUpdateMysqlConnScreen = { connId ->
//                    navController.navigate("${Screen.UpdateMysqlConnScreen.route}/${connId}")
//                },
//                navigateToInsertMysqlConnScreen = {
//                    navController.navigate(Screen.NewMysqlConnScreen.route)
//                },
//                navigateToMysqlDbHomeScreen = {
//                    navController.navigate(Screen.ServerConnHomeScreen.route)
//                }
//            )
//        }
//
//        composable(
//            route = Screen.NewMysqlConnScreen.route,
//            enterTransition = {
//                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
//            },
//            exitTransition = {
//                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popEnterTransition = {
//                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popExitTransition = {
//                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
//            }
//        ) {
//            NewMysqlConnScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        composable(
//            route = "${Screen.UpdateMysqlConnScreen.route}/{$MYSQL_CONN_ID}",
//            arguments = listOf(
//                navArgument(MYSQL_CONN_ID) {
//                    type = IntType
//                }
//            ),
//            enterTransition = {
//                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
//            },
//            exitTransition = {
//                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popEnterTransition = {
//                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popExitTransition = {
//                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
//            }
//        ) { backStackEntry ->
//            val connId = backStackEntry.arguments?.getInt(MYSQL_CONN_ID) ?: 0
//            NewMysqlConnScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                },
//                connId = connId
//            )
//        }
//
//        composable(
//            route = Screen.ServerConnHomeScreen.route,
//            enterTransition = {
//                slideInHorizontally(initialOffsetX = { offset }, animationSpec = tweenSpec)
//            },
//            exitTransition = {
//                slideOutHorizontally(targetOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popEnterTransition = {
//                slideInHorizontally(initialOffsetX = { -offset }, animationSpec = tweenSpec)
//            },
//            popExitTransition = {
//                slideOutHorizontally(targetOffsetX = { offset }, animationSpec = tweenSpec)
//            }
//        ) {
//            ServerConnHomeScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
    }
}