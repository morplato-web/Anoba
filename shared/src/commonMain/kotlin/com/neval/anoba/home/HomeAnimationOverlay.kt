package com.neval.anoba.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun HomeAnimationOverlay(
    isActive: Boolean,
    onFinished: () -> Unit
) {
    if (!isActive) return

    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(isActive) {
        transitionState.targetState = true
        delay(3000)
        transitionState.targetState = false
        onFinished()
    }

    val offsetY by animateDpAsState(
        targetValue = if (transitionState.targetState) 600.dp else 0.dp,
        animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
    )

    val alphaBy by animateFloatAsState(
        targetValue = if (transitionState.targetState) 1f else 0f,
        animationSpec = tween(durationMillis = 2800)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .alpha(alphaBy),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("🌸", fontSize = 28.sp)
            Text("❤️", fontSize = 28.sp)
            Text("✨", fontSize = 28.sp)
            Text("🌸", fontSize = 28.sp)
            Text("💖", fontSize = 28.sp)
        }
    }
}
