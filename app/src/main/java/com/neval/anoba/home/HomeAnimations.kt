package com.neval.anoba.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import kotlin.random.Random

enum class AnimationType { FALL, FLOAT, FLY, POP, DRIVE, WALK }

data class AnimationAsset(val imageUrl: String, val type: AnimationType, val hasSiren: Boolean = false)

@Composable
fun HomeAnimationLayer(
    screenMaxWidth: Dp, screenMaxHeight: Dp, startYOffset: Dp,
    activeAnimationAssets: List<AnimationAsset>, isAnimationActive: Boolean, onAnimationFinished: () -> Unit,
    policeAssets: List<AnimationAsset>, isPoliceAnimationActive: Boolean, onPoliceAnimationFinished: () -> Unit,
    commandoAssets: List<AnimationAsset>, isCommandoAnimationActive: Boolean, onCommandoAnimationFinished: () -> Unit,
    bekciAssets: List<AnimationAsset>, isBekciAnimationActive: Boolean, onBekciAnimationFinished: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isBekciAnimationActive && bekciAssets.isNotEmpty()) {
            HomeEntityAnimation(
                maxWidth = screenMaxWidth, maxHeight = screenMaxHeight, startYOffset = startYOffset,
                assets = bekciAssets, particleCount = 1, description = "Bekci",
                minDuration = 14000, maxDuration = 17000, minSize = 125.dp, maxSize = 145.dp,
                onFinished = onBekciAnimationFinished, maxDelay = 1
            )
        }

        if (isAnimationActive && activeAnimationAssets.isNotEmpty()) {
            HomeEntityAnimation(maxWidth = screenMaxWidth, maxHeight = screenMaxHeight, startYOffset = startYOffset, assets = activeAnimationAssets, particleCount = 20, description = "ForYou", onFinished = onAnimationFinished)
        }
        if (isPoliceAnimationActive && policeAssets.isNotEmpty()) {
            val dynamicParticleCount = remember { Random.nextInt(8, 13) }
            HomeEntityAnimation(
                maxWidth = screenMaxWidth, maxHeight = screenMaxHeight, startYOffset = startYOffset,
                assets = policeAssets, particleCount = dynamicParticleCount, description = "Polis",
                minSize = 80.dp, maxSize = 110.dp,
                minDuration = 8000, maxDuration = 12000,
                maxDelay = 7000,
                onFinished = onPoliceAnimationFinished
            )
        }
        if (isCommandoAnimationActive && commandoAssets.isNotEmpty()) {
            val dynamicParticleCount = remember { Random.nextInt(8, 13) }
            HomeEntityAnimation(
                maxWidth = screenMaxWidth, maxHeight = screenMaxHeight, startYOffset = startYOffset,
                assets = commandoAssets, particleCount = dynamicParticleCount, description = "Komando",
                minSize = 80.dp, maxSize = 110.dp,
                minDuration = 8000, maxDuration = 12000,
                maxDelay = 7000,
                onFinished = onCommandoAnimationFinished
            )
        }
    }
}

@Composable
private fun HomeEntityAnimation(
    maxWidth: Dp, maxHeight: Dp, startYOffset: Dp,
    assets: List<AnimationAsset>, description: String,
    particleCount: Int = 20, startDelayMillis: Long = 0,
    minDuration: Long = 6000, maxDuration: Long = 10000,
    minSize: Dp = 45.dp, maxSize: Dp = 65.dp,
    maxDelay: Long = 3000,
    onFinished: (() -> Unit)? = null
) {
    val entities = remember(description, particleCount, maxWidth, maxHeight, startYOffset, minSize, maxSize, minDuration, maxDuration, maxDelay) {
        List(particleCount) { index ->
            val asset = assets[index % assets.size]
            val size = Random.nextInt(minSize.value.toInt().coerceAtLeast(1), maxSize.value.toInt().coerceAtLeast(2)).dp

            val startX: Dp
            val targetX: Dp
            val startY: Dp
            val targetY: Dp

            if (asset.type == AnimationType.FALL) {
                // ÇİÇEK ANİMASYONU DÜZELTMESİ:
                // Başlangıç noktasını -400.dp yaparak üst barın arkasında takılı kalmasını önledik.
                // Çiçekler artık telefonun en üstünden süzülerek girmeye başlayacak.
                startX = (Random.nextFloat() * maxWidth.value).dp
                targetX = startX + (Random.nextInt(-30, 30)).dp
                startY = (-400).dp
                targetY = maxHeight + size + 100.dp
            } else {
                startX = -size - 120.dp
                targetX = maxWidth + size + 120.dp

                if (description == "Polis" || description == "Komando" || description == "Bekci") {
                    val entryTop = startYOffset.value
                    val entryBottom = maxHeight.value * 0.4f
                    startY = (entryTop + Random.nextFloat() * (entryBottom - entryTop)).dp

                    val exitTop = maxHeight.value * 0.5f
                    targetY = (exitTop + Random.nextFloat() * (maxHeight.value - exitTop)).dp
                } else {
                    startY = startYOffset + (Random.nextInt(20, 220)).dp
                    targetY = startY + (Random.nextInt(-60, 160)).dp
                }
            }

            HomeEntityData(
                id = index, asset = asset,
                startX = startX, targetX = targetX,
                startY = startY, targetY = targetY,
                size = size,
                animationDuration = Random.nextLong(minDuration, maxDuration),
                startDelay = startDelayMillis + Random.nextLong(0, maxDelay + 1)
            )
        }
    }

    entities.forEach { entity ->
        key(description + entity.id) {
            HomeEntityParticle(entity = entity, allAssets = assets)
        }
    }

    LaunchedEffect(entities) {
        val maxTime = entities.maxOfOrNull { it.startDelay + it.animationDuration } ?: 0L
        delay(maxTime + 1000)
        onFinished?.invoke()
    }
}

@Composable
private fun HomeEntityParticle(entity: HomeEntityData, allAssets: List<AnimationAsset>) {
    val imageLoader: ImageLoader = koinInject()
    var hasStarted by remember { mutableStateOf(false) }
    var currentWalkIndex by remember { mutableIntStateOf(0) }

    val isLottie = entity.asset.imageUrl.contains(".json", ignoreCase = true)

    val composition by rememberLottieComposition(LottieCompositionSpec.Url(entity.asset.imageUrl))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = hasStarted,
        restartOnPlay = true
    )

    LaunchedEffect(Unit) {
        delay(entity.startDelay)
        hasStarted = true
    }

    LaunchedEffect(hasStarted) {
        if (hasStarted) {
            while (true) {
                if (entity.asset.type == AnimationType.WALK) {
                    delay(320)
                } else if (entity.asset.type == AnimationType.FLY) {
                    delay(800)
                } else {
                    break
                }
                currentWalkIndex = (currentWalkIndex + 1) % 100
            }
        }
    }

    val isBouncingType = entity.asset.type == AnimationType.WALK || entity.asset.type == AnimationType.FLY
    val yBobbingOffset = if (isBouncingType) {
        if (entity.asset.type == AnimationType.FLY) {
            if (currentWalkIndex % 2 != 0) (-12).dp else 0.dp
        } else {
            if (currentWalkIndex % 2 != 0) (-4).dp else 0.dp
        }
    } else 0.dp

    var sirenVisible by remember { mutableStateOf(false) }
    if (entity.asset.hasSiren && !isLottie) {
        LaunchedEffect(hasStarted) {
            if (hasStarted) {
                while (true) {
                    sirenVisible = !sirenVisible
                    delay(250)
                }
            }
        }
    }

    val xPos by animateDpAsState(targetValue = if (hasStarted) entity.targetX else entity.startX, animationSpec = tween(entity.animationDuration.toInt(), easing = LinearEasing), label = "x")
    val yPos by animateDpAsState(targetValue = if (hasStarted) entity.targetY + yBobbingOffset else entity.startY, animationSpec = tween(entity.animationDuration.toInt(), easing = LinearEasing), label = "y")
    val rotationAngle by animateFloatAsState(targetValue = if (hasStarted && entity.asset.type == AnimationType.FALL) 180f else 0f, animationSpec = tween(entity.animationDuration.toInt(), easing = LinearEasing), label = "rotation")

    Box(modifier = Modifier.offset(x = xPos, y = yPos).size(entity.size).rotate(if (entity.asset.type == AnimationType.FALL) rotationAngle else 0f)) {
        if (isLottie) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                renderMode = RenderMode.AUTOMATIC
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (entity.asset.type == AnimationType.WALK && allAssets.size > 1) {
                    allAssets.forEachIndexed { index, asset ->
                        AsyncImage(
                            model = asset.imageUrl,
                            contentDescription = null,
                            imageLoader = imageLoader,
                            modifier = Modifier.fillMaxSize().alpha(if (index == currentWalkIndex % allAssets.size) 1f else 0f)
                        )
                    }
                } else {
                    AsyncImage(
                        model = entity.asset.imageUrl,
                        contentDescription = null,
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (entity.asset.hasSiren && sirenVisible) {
                    Box(
                        modifier = Modifier
                            .size(entity.size / 4)
                            .align(Alignment.TopCenter)
                            .background(if (Random.nextBoolean()) Color.Red.copy(alpha = 0.6f) else Color.Blue.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

private data class HomeEntityData(val id: Int, val asset: AnimationAsset, val startX: Dp, val targetX: Dp, val startY: Dp, val targetY: Dp, val size: Dp, val animationDuration: Long, val startDelay: Long)