package com.neval.anoba.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.R

enum class TutorialStep {
    WELCOME,
    DRAWER_MENU,
    FLOWER_DOT,
    MORE_MENU,
    MARKET,
    SEARCH_FILTER,
    PINNED_FILTER,
    FOLLOWING_FILTER,
    GLOBAL_MAP,
    TAB_CHATS,
    MOSAIC,
    NOTIFICATIONS,
    TAB_VIDEOS,
    TAB_LETTERS,
    TAB_PHOTOS,
    MY_CONTENTS,
    FINISHED
}

@Composable
fun HomeTutorialOverlay(
    currentStep: TutorialStep,
    targetRect: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (currentStep == TutorialStep.FINISHED) return

    val alpha by animateFloatAsState(targetValue = 0.7f, label = "alpha")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Tıklamayı engelle */ }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = alpha))

            targetRect?.let { rect ->
                val padding = 4.dp.toPx()
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(rect.left - padding, rect.top - padding),
                    size = Size(rect.width + padding * 2, rect.height + padding * 2),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            TutorialTooltip(
                step = currentStep,
                targetRect = targetRect,
                onNext = onNext,
                onSkip = onSkip,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun TutorialTooltip(
    step: TutorialStep,
    targetRect: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (targetRect != null) {
        val yOffset = if (targetRect.top < 500f) {
            targetRect.bottom + 50f
        } else {
            targetRect.top - 550f
        }
        Modifier.offset { IntOffset(0, yOffset.toInt()) }
    } else {
        modifier
    }

    Card(
        modifier = finalModifier
            .padding(24.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getTutorialTitle(step),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getTutorialDescription(step),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.tutorial_skip), color = Color.Gray)
                }
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
                ) {

                    val buttonText = if (step == TutorialStep.MY_CONTENTS) {
                        stringResource(R.string.tutorial_finish)
                    } else {
                        stringResource(R.string.tutorial_next)
                    }
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun getTutorialTitle(step: TutorialStep): String {
    return when (step) {
        TutorialStep.WELCOME -> stringResource(R.string.tutorial_title_welcome)
        TutorialStep.DRAWER_MENU -> stringResource(R.string.tutorial_title_drawer)
        TutorialStep.FLOWER_DOT -> stringResource(R.string.tutorial_title_flower)
        TutorialStep.MORE_MENU -> stringResource(R.string.tutorial_title_more_menu)
        TutorialStep.MARKET -> stringResource(R.string.market_title)
        TutorialStep.SEARCH_FILTER -> stringResource(R.string.tutorial_title_search)
        TutorialStep.PINNED_FILTER -> stringResource(R.string.tutorial_title_pin)
        TutorialStep.FOLLOWING_FILTER -> stringResource(R.string.tutorial_title_following)
        TutorialStep.GLOBAL_MAP -> stringResource(R.string.tutorial_title_map)
        TutorialStep.TAB_CHATS -> stringResource(R.string.tutorial_title_tab_chats)
        TutorialStep.MOSAIC -> stringResource(R.string.tutorial_title_mosaic)
        TutorialStep.NOTIFICATIONS -> stringResource(R.string.tutorial_title_notifications)
        TutorialStep.TAB_VIDEOS -> stringResource(R.string.tutorial_title_tab_videos)
        TutorialStep.TAB_LETTERS -> stringResource(R.string.tutorial_title_tab_letters)
        TutorialStep.TAB_PHOTOS -> stringResource(R.string.tutorial_title_tab_photos)
        TutorialStep.MY_CONTENTS -> stringResource(R.string.tutorial_title_my_contents)
        TutorialStep.FINISHED -> ""
    }
}

@Composable
fun getTutorialDescription(step: TutorialStep): String {
    return when (step) {
        TutorialStep.WELCOME -> stringResource(R.string.tutorial_desc_welcome)
        TutorialStep.DRAWER_MENU -> stringResource(R.string.tutorial_desc_drawer)
        TutorialStep.FLOWER_DOT -> stringResource(R.string.tutorial_desc_flower)
        TutorialStep.MORE_MENU -> stringResource(R.string.tutorial_desc_more_menu)
        TutorialStep.MARKET -> stringResource(R.string.market_guide_desc)
        TutorialStep.SEARCH_FILTER -> stringResource(R.string.tutorial_desc_search)
        TutorialStep.PINNED_FILTER -> stringResource(R.string.tutorial_desc_pin)
        TutorialStep.FOLLOWING_FILTER -> stringResource(R.string.tutorial_desc_following)
        TutorialStep.GLOBAL_MAP -> stringResource(R.string.tutorial_desc_map)
        TutorialStep.TAB_CHATS -> stringResource(R.string.tutorial_desc_tab_chats)
        TutorialStep.MOSAIC -> stringResource(R.string.tutorial_desc_mosaic)
        TutorialStep.NOTIFICATIONS -> stringResource(R.string.tutorial_desc_notifications)
        TutorialStep.TAB_VIDEOS -> stringResource(R.string.tutorial_desc_tab_videos)
        TutorialStep.TAB_LETTERS -> stringResource(R.string.tutorial_desc_tab_letters)
        TutorialStep.TAB_PHOTOS -> stringResource(R.string.tutorial_desc_tab_photos)
        TutorialStep.MY_CONTENTS -> stringResource(R.string.tutorial_desc_my_contents)
        TutorialStep.FINISHED -> ""
    }
}
