package com.neval.anoba.drawer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.neval.anoba.R

@Composable
fun GuideSelectionDialog(
    onDismiss: () -> Unit,
    onStartTour: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_guide_dialog_title)) },
        text = { Text(stringResource(R.string.home_guide_dialog_text)) },
        confirmButton = {
            Button(
                onClick = onStartTour,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
            ) {
                Text(stringResource(R.string.home_guide_dialog_tour))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}
