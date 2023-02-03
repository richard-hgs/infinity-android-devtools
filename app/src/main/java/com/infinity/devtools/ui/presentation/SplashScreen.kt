@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.infinity.devtools.R

@Composable
fun SplashScreen(
    navigateToHomeScreen: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colors.primary,
                    Color.White
                )
            )
        )
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_phone_flow))
        val progress = animateLottieCompositionAsState(
            composition = composition,
            speed = 2f,
            iterations = 1
        )
        LottieAnimation(
            modifier = Modifier.size(200.dp)
                .align(Alignment.Center),
            composition = composition,
            progress = { progress.progress },
        )

        if (progress.isAtEnd && progress.isPlaying) {
            navigateToHomeScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        navigateToHomeScreen = {}
    )
}