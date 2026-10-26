package com.neval.anoba.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class FlowerParticleData(
    val id: Int,
    val imageUrl: String,
    val startXRatio: Float,
    val sizeDp: Dp,
    val durationMillis: Int,
    val delayMillis: Int,
    val targetRotation: Float
)

@Composable
fun HomeAnimationOverlay(
    isActive: Boolean,
    onFinished: () -> Unit,
    flowerImageUrls: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particles = remember(isActive, flowerImageUrls) {
        if (flowerImageUrls.isEmpty()) {
            emptyList()
        } else {
            List(20) { index ->
                FlowerParticleData(
                    id = index,
                    imageUrl = flowerImageUrls[index % flowerImageUrls.size],
                    startXRatio = Random.nextFloat(),
                    sizeDp = Random.nextInt(45, 65).dp,
                    durationMillis = Random.nextInt(5000, 8000),
                    delayMillis = Random.nextInt(0, 1500),
                    targetRotation = Random.nextInt(-180, 180).toFloat()
                )
            }
        }
    }

    LaunchedEffect(isActive) {
        delay(8500)
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
            SingleFallingFlowerParticle(
                particle = particle,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx
            )
        }
    }
}

@Composable
private fun SingleFallingFlowerParticle(
    particle: FlowerParticleData,
    screenWidth: Dp,
    screenHeight: Dp
) {
    var hasStarted by remember { mutableStateOf(false) }

    LaunchedEffect(particle.id) {
        delay(particle.delayMillis.toLong())
        hasStarted = true
    }

    val startX = (particle.startXRatio * (screenWidth.value - 60).coerceAtLeast(0f)).dp

    val offsetY by animateDpAsState(
        targetValue = if (hasStarted) screenHeight + 100.dp else (-100).dp,
        animationSpec = tween(
            durationMillis = particle.durationMillis,
            easing = LinearEasing
        ),
        label = "yAnim"
    )

    val rotation by animateFloatAsState(
        targetValue = if (hasStarted) particle.targetRotation else 0f,
        animationSpec = tween(
            durationMillis = particle.durationMillis,
            easing = LinearEasing
        ),
        label = "rotationAnim"
    )

    val alphaVal by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alphaAnim"
    )

    Box(
        modifier = Modifier
            .offset(x = startX, y = offsetY)
            .size(particle.sizeDp)
            .alpha(alphaVal)
            .rotate(rotation)
    ) {
        AsyncImage(
            model = particle.imageUrl,
            contentDescription = "Çiçek",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
