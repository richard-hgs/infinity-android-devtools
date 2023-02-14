package com.infinity.devtools.ui.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.infinity.devtools.ui.vm.ServerConnVm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ServerConnHomeScreen(
    serverConnVm: ServerConnVm = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        coroutineScope.launch(Dispatchers.IO) {
            serverConnVm.getTables()
        }

    }
}