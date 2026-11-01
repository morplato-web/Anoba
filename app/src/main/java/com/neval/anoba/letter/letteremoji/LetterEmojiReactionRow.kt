@file:Suppress("unused")

package com.neval.anoba.letter.letteremoji

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LetterEmojiReactionRow(
    reactions: Map<String, Long>,
    selectedEmoji: String?,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 13 Standart emojiyi temel alır
    val allEmojis = remember(reactions) {
        (LetterEmojis.list + reactions.keys).distinct().filter { it.isNotBlank() }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(
            items = allEmojis,
            key = { emoji -> emoji },
        ) { emoji ->
            val count = reactions[emoji] ?: 0L
            val isSelected = selectedEmoji == emoji
            val baseBackgroundColor = LetterEmojis.emojiColors[emoji] ?: Color(0xFFF1F5F9)

            EmojiColumnItem(
                emoji = emoji,
                count = count,
                isSelected = isSelected,
                baseBackgroundColor = baseBackgroundColor,
                onReact = onReact,
            )
        }
    }
}

@Composable
private fun EmojiColumnItem(
    emoji: String,
    count: Long,
    isSelected: Boolean,
    baseBackgroundColor: Color,
    onReact: (String) -> Unit,
) {
    val selectedBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val selectedBorderColor = Color(0xFF232066)
    val defaultBorderColor = Color.LightGray.copy(alpha = 0.4f)

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.width(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) selectedBgColor else baseBackgroundColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) selectedBorderColor else defaultBorderColor,
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 20.dp),
                    role = Role.Button,
                    onClick = { onReact(emoji) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (count > 0) count.toString() else "·",
            fontSize = 11.sp,
            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) selectedBorderColor else Color.Gray,
        )
    }
}