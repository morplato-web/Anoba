package com.neval.anoba.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.androidx.compose.koinViewModel

private fun parseHexColor(hexString: String?): Color {
    if (hexString.isNullOrBlank()) return Color.Gray
    return try {
        val cleanHex = hexString.removePrefix("#")
        val colorLong = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorLong or 0xFF000000)
        } else {
            Color(colorLong)
        }
    } catch (_: Exception) {
        Color.Gray
    }
}

@Composable
fun MosaicSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MosaicViewModel = koinViewModel(),
) {
    val items by viewModel.mosaicItems.collectAsState()

    val sharedItems = remember(items) {
        items.map { item ->
            SharedMosaicItem(
                id = item.id,
                title = item.textContent ?: "",
                isAd = item.isAd,
                color = parseHexColor(item.colorHex),
            )
        }
    }

    SharedMosaicSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        items = sharedItems,
        onItemClick = { sharedItem ->
            viewModel.trackClick(sharedItem.id)
        },
        modifier = modifier,
    )
}
