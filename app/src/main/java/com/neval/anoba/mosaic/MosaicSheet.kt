package com.neval.anoba.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun MosaicSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") viewModel: MosaicViewModel = koinViewModel(),
) {
    SharedMosaicSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
