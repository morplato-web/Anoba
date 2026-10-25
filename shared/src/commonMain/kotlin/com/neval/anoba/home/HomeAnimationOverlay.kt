package com.neval.anoba.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class MultiplatformParticleData(
    val id: Int,
    val emoji: String,
    val startXRatio: Float,
    val sizeSp: Int,
    val durationMillis: Int,
    val delayMillis: Int
)

@Composable
fun HomeAnimationOverlay(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particleEmojis = remember { listOf("🌸", "❤️", "⭐", "🌺", "💖", "✨", "🌸", "🌹") }

    val particles = remember(isActive) {
        List(20) { index ->
            MultiplatformParticleData(
                id = index,
                emoji = particleEmojis[index % particleEmojis.size],
                startXRatio = Random.nextFloat(), // 0.0f to 1.0f
                sizeSp = Random.nextInt(24, 38),
                durationMillis = Random.nextInt(3000, 5000),
                delayMillis = Random.nextInt(0, 1500)
            )
        }
    }

    LaunchedEffect(isActive) {
        delay(6500)
        onFinished()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        val screenWidthPx = maxWidth
        val screenHeightPx = maxHeight

        particles.forEach { particle ->
            SingleFallingParticle(
                particle = particle,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx
            )
        }
    }
}

@Composable
private fun SingleFallingParticle(
    particle: MultiplatformParticleData,
    screenWidth: Dp,
    screenHeight: Dp
) {
    var hasStarted by remember { mutableStateOf(false) }

    LaunchedEffect(particle.id) {
        delay(particle.delayMillis.toLong())
        hasStarted = true
    }

    val startX = (particle.startXRatio * (screenWidth.value - 40)).dp

    val offsetY by animateDpAsState(
        targetValue = if (hasStarted) screenHeight + 60.dp else (-60).dp,
        animationSpec = tween(
            durationMillis = particle.durationMillis,
            easing = LinearEasing
        ),
        label = "yAnim"
    )

    val alphaVal by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alphaAnim"
    )

    Box(
        modifier = Modifier
            .offset(x = startX, y = offsetY)
            .alpha(alphaVal)
    ) {
        Text(
            text = particle.emoji,
            fontSize = particle.sizeSp.sp
        )
    }
}
