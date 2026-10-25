package com.neval.anoba.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnobaBrandLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.75f)) {
            val w = this.size.width
            val h = this.size.height

            val greenColor = Color(0xFF2E7D32)
            val strokeWidth = w * 0.05f

            // Mountain 1 (Left Peak)
            val m1 = Path().apply {
                moveTo(w * 0.1f, h * 0.65f)
                lineTo(w * 0.4f, h * 0.25f)
                lineTo(w * 0.65f, h * 0.65f)
            }
            drawPath(m1, greenColor, style = Stroke(width = strokeWidth))

            // Mountain 2 (Right Peak)
            val m2 = Path().apply {
                moveTo(w * 0.45f, h * 0.65f)
                lineTo(w * 0.75f, h * 0.35f)
                lineTo(w * 0.95f, h * 0.65f)
            }
            drawPath(m2, greenColor, style = Stroke(width = strokeWidth))

            // River Line below
            val river = Path().apply {
                moveTo(w * 0.15f, h * 0.8f)
                cubicTo(w * 0.35f, h * 0.75f, w * 0.6f, h * 0.85f, w * 0.85f, h * 0.8f)
            }
            drawPath(river, greenColor, style = Stroke(width = strokeWidth))
        }
    }
}
