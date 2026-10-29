package com.neval.anoba.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoogleLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height

        // Red Path
        val redPath = Path().apply {
            moveTo(width * 0.5f, height * 0.08f)
            cubicTo(width * 0.72f, height * 0.08f, width * 0.88f, height * 0.16f, width * 0.96f, height * 0.28f)
            lineTo(width * 0.80f, height * 0.42f)
            cubicTo(width * 0.74f, height * 0.35f, width * 0.63f, height * 0.30f, width * 0.50f, height * 0.30f)
            cubicTo(width * 0.34f, height * 0.30f, width * 0.20f, height * 0.40f, width * 0.15f, height * 0.55f)
            lineTo(width * 0.02f, height * 0.45f)
            cubicTo(width * 0.10f, height * 0.23f, width * 0.28f, height * 0.08f, width * 0.5f, height * 0.08f)
            close()
        }
        drawPath(redPath, Color(0xFFEA4335), style = Fill)

        // Green Path
        val greenPath = Path().apply {
            moveTo(width * 0.50f, height * 0.92f)
            cubicTo(width * 0.28f, height * 0.92f, width * 0.10f, height * 0.77f, width * 0.02f, height * 0.55f)
            lineTo(width * 0.15f, height * 0.45f)
            cubicTo(width * 0.20f, height * 0.60f, width * 0.34f, height * 0.70f, width * 0.50f, height * 0.70f)
            cubicTo(width * 0.63f, height * 0.70f, width * 0.74f, height * 0.64f, width * 0.81f, height * 0.55f)
            lineTo(width * 0.95f, height * 0.66f)
            cubicTo(width * 0.86f, height * 0.82f, width * 0.70f, height * 0.92f, width * 0.50f, height * 0.92f)
            close()
        }
        drawPath(greenPath, Color(0xFF34A853), style = Fill)

        // Blue Path
        val bluePath = Path().apply {
            moveTo(width * 0.96f, height * 0.50f)
            cubicTo(width * 0.96f, height * 0.46f, width * 0.95f, height * 0.42f, width * 0.94f, height * 0.38f)
            lineTo(width * 0.50f, height * 0.38f)
            lineTo(width * 0.50f, height * 0.58f)
            lineTo(width * 0.76f, height * 0.58f)
            cubicTo(width * 0.75f, height * 0.65f, width * 0.70f, height * 0.71f, width * 0.63f, height * 0.75f)
            lineTo(width * 0.77f, height * 0.86f)
            cubicTo(width * 0.88f, height * 0.76f, width * 0.96f, height * 0.64f, width * 0.96f, height * 0.50f)
            close()
        }
        drawPath(bluePath, Color(0xFF4285F4), style = Fill)

        // Yellow Path
        val yellowPath = Path().apply {
            moveTo(width * 0.15f, height * 0.45f)
            cubicTo(width * 0.13f, height * 0.50f, width * 0.13f, height * 0.55f, width * 0.15f, height * 0.60f)
            lineTo(width * 0.02f, height * 0.70f)
            cubicTo(width * 0.007f, height * 0.63f, width * 0.007f, height * 0.42f, width * 0.02f, height * 0.35f)
            lineTo(width * 0.15f, height * 0.45f)
            close()
        }
        drawPath(yellowPath, Color(0xFFFBBC05), style = Fill)
    }
}
