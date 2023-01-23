@file:Suppress("FunctionName")

package com.infinity.devtools.ui.screens

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infinity.devtools.ui.theme.ArchitectureProjectTheme

@Composable
fun HomeScreen(navController: NavController) {
    // Text(text = "Home Screen")
    Button(content = {
        Text(text = "Settings")
    }, onClick = {
        navController.navigate("settings")
    })
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    ArchitectureProjectTheme {
        HomeScreen(navController = rememberNavController())
    }
}