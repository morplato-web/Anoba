package com.neval.anoba.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.R

@Composable
fun QuarantineBanner(
    isFlagged: Boolean,
    flagReason: String?
) {
    if (!isFlagged) return

    val emoji = flagReason?.substringAfterLast("_") ?: "🚩"
    val explanation = when (emoji) {
        "🚫" -> stringResource(R.string.quarantine_explanation_forbidden)
        "❌" -> stringResource(R.string.quarantine_explanation_delete)
        else -> stringResource(R.string.quarantine_explanation_generic)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xFFC62828), // Dark Red
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Büyük ve belirgin emoji
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 34.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = explanation,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.quarantine_footer_note),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
            }
        }
    }
}
