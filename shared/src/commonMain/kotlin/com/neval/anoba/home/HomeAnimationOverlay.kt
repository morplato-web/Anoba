package com.neval.anoba.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@Composable
fun HomeAnimationOverlay(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(isActive) {
        transitionState.targetState = true
        delay(2500)
        transitionState.targetState = false
        onFinished()
    }

    val offsetY by animateDpAsState(
        targetValue = if (transitionState.targetState) 700.dp else (-50).dp,
        animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
    )

    val alphaBy by animateFloatAsState(
        targetValue = if (transitionState.targetState) 1f else 0f,
        animationSpec = tween(durationMillis = 2400)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .alpha(alphaBy),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("🌸", fontSize = 32.sp)
            Text("❤️", fontSize = 32.sp)
            Text("⭐", fontSize = 32.sp)
            Text("🌸", fontSize = 32.sp)
            Text("💖", fontSize = 32.sp)
        }
    }
}
