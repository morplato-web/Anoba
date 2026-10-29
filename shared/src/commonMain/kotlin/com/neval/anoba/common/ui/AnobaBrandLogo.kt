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
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val greenColor = Color(0xFF2E7D32)
            val lightGreen = Color(0xFF4CAF50)
            val goldColor = Color(0xFFFFC107)
            val strokeWidth = w * 0.06f

            // Sun / Gold Circle
            drawCircle(
                color = goldColor,
                radius = w * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.32f)
            )

            // Mountain 1 (Left Main Peak)
            val m1 = Path().apply {
                moveTo(w * 0.12f, h * 0.68f)
                lineTo(w * 0.42f, h * 0.28f)
                lineTo(w * 0.68f, h * 0.68f)
            }
            drawPath(m1, greenColor, style = Stroke(width = strokeWidth))

            // Mountain 2 (Right Peak)
            val m2 = Path().apply {
                moveTo(w * 0.45f, h * 0.68f)
                lineTo(w * 0.76f, h * 0.38f)
                lineTo(w * 0.92f, h * 0.68f)
            }
            drawPath(m2, lightGreen, style = Stroke(width = strokeWidth))

            // River Wavy Path below
            val river = Path().apply {
                moveTo(w * 0.12f, h * 0.82f)
                cubicTo(w * 0.35f, h * 0.76f, w * 0.65f, h * 0.88f, w * 0.88f, h * 0.82f)
            }
            drawPath(river, greenColor, style = Stroke(width = strokeWidth))
        }
    }
}
