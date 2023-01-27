@file:Suppress("FunctionName")

package com.infinity.devtools.ui.screens

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infinity.devtools.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    navController: NavController,
) {
    Button(content = {
        Text(text = "Home")
    }, onClick = {
        navController.navigateUp()
    })
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    AppTheme {
        SettingsScreen(
            navController = rememberNavController()
        )
    }
}