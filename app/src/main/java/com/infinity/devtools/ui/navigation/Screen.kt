package com.infinity.devtools.ui.navigation

import com.infinity.devtools.constants.ConstantsScreen.MYSQL_CONNS_SCREEN
import com.infinity.devtools.constants.ConstantsScreen.NEW_MYSQL_CONN_SCREEN
import com.infinity.devtools.constants.ConstantsScreen.SERVER_CONN_HOME_SCREEN
import com.infinity.devtools.constants.ConstantsScreen.SPLASH_SCREEN
import com.infinity.devtools.constants.ConstantsScreen.UPDATE_MYSQL_CONN_SCREEN

sealed class Screen(val route: String) {
    object SplashScreen: Screen(SPLASH_SCREEN)
    object MysqlConnsScreen: Screen(MYSQL_CONNS_SCREEN)
    object NewMysqlConnScreen: Screen(NEW_MYSQL_CONN_SCREEN)
    object UpdateMysqlConnScreen: Screen(UPDATE_MYSQL_CONN_SCREEN)
    object ServerConnHomeScreen: Screen(SERVER_CONN_HOME_SCREEN)
}
