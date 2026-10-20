package com.neval.anoba.common.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.neval.anoba.ui.theme.AnobaGold

enum class AnobaBadgeType(
    val icon: ImageVector,
    val color: Color,
) {
    OFFICIAL(Icons.Default.Verified, AnobaGold),
    ELITE(Icons.Default.Verified, Color(0xFF2979FF)),
    NONE(Icons.Default.Verified, Color.Transparent)
}

val ThickCheckIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ThickCheck",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 3.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(4.5f, 12.5f)
        lineTo(10f, 18f)
        lineTo(20f, 6.5f)
    }.build()

@Composable
fun AnobaBadge(
    badgeType: String?,
    modifier: Modifier = Modifier,
    isOfficial: Boolean = false
) {
    val finalBadgeType = when {
        (!badgeType.isNullOrBlank() && badgeType != "NONE") -> badgeType.uppercase()
        isOfficial -> "OFFICIAL"
        else -> null
    }

    if (finalBadgeType == "PIONEER" || finalBadgeType == "BUILDER") {
        Icon(
            imageVector = ThickCheckIcon,
            contentDescription = "Verified Pioneer",
            tint = Color(0xFF4CAF50),
            modifier = modifier
                .padding(start = 4.dp)
                .size(16.dp)
        )
        return
    }

    val type = when (finalBadgeType) {
        "OFFICIAL" -> AnobaBadgeType.OFFICIAL
        "ELITE" -> AnobaBadgeType.ELITE
        else -> null
    } ?: return

    Icon(
        imageVector = type.icon,
        contentDescription = null,
        tint = type.color,
        modifier = modifier
            .padding(start = 4.dp)
            .size(14.dp)
    )
}
