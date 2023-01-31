@file:Suppress("FunctionName")

package com.infinity.devtools.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.infinity.devtools.constants.ConstantsDb.MYSQL_CONN_ID
import com.infinity.devtools.ui.presentation.MysqlConnsScreen
import com.infinity.devtools.ui.presentation.NewMysqlConnScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MysqlConnsScreen.route
    ) {
        composable(
            route = Screen.MysqlConnsScreen.route
        ) {
            MysqlConnsScreen(
                navigateToUpdateMysqlConnScreen = { connId ->
                    navController.navigate("${Screen.UpdateMysqlConnScreen.route}/${connId}")
                },
                navigateToInsertMysqlConnScreen = {
                    navController.navigate(Screen.NewMysqlConnScreen.route)
                }
            )
        }

        composable(
            route = Screen.NewMysqlConnScreen.route
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
            )
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