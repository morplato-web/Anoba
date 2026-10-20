package com.neval.anoba.common.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neval.anoba.ui.theme.AnobaGold

@Composable
fun OfficialBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = AnobaGold // Altın sarısı (Heyet rengi)
) {
    Icon(
        imageVector = Icons.Default.Verified,
        contentDescription = "Resmi Heyet",
        tint = tint,
        modifier = modifier
            .size(size)
            .padding(start = 4.dp)
    )
}
