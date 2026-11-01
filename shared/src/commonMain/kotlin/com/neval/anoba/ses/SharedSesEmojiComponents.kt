package com.neval.anoba.ses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.common.utils.SharedStandardEmojis
import com.neval.anoba.photo.SharedReactionUser

@Suppress("unused", "UnusedSymbol")
@Composable
fun SharedSesEmojiReactionRow(
    reactions: Map<String, Long>,
    selectedEmoji: String?,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allEmojis = remember(reactions) {
        (reactions.keys + SharedStandardEmojis.list).distinct().filter { it.isNotBlank() }
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = allEmojis,
            key = { emoji -> emoji }
        ) { emoji ->
            val count = reactions[emoji] ?: 0L
            val isSelected = selectedEmoji == emoji
            val baseBackgroundColor = SharedStandardEmojis.emojiColors[emoji] ?: Color.White.copy(alpha = 0.1f)

            EmojiColumnItem(
                emoji = emoji,
                count = count,
                isSelected = isSelected,
                baseBackgroundColor = baseBackgroundColor,
                onReact = onReact
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSesReactionUsersBottomSheet(
    reactionUsers: List<SharedReactionUser>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tepki Verenler (${reactionUsers.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (reactionUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz tepki veren kimse yok.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reactionUsers, key = { it.userId }) { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF334155)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user.avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = user.avatarUrl,
                                            contentDescription = user.username,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    } else {
                                        Text(user.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(user.username, color = Color.White, fontSize = 14.sp)
                            }
                            Text(user.emoji, fontSize = 18.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmojiColumnItem(
    emoji: String,
    count: Long,
    isSelected: Boolean,
    baseBackgroundColor: Color,
    onReact: (String) -> Unit
) {
    val selectedBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val selectedBorderColor = MaterialTheme.colorScheme.primary
    val defaultBorderColor = Color.White.copy(alpha = 0.2f)

    Column(
        modifier = Modifier.width(44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isSelected) selectedBgColor else baseBackgroundColor)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) selectedBorderColor else defaultBorderColor,
                    shape = CircleShape
                )
                .clickable { onReact(emoji) },
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (count > 0) count.toString() else "·",
            fontSize = 11.sp,
            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
        )
    }
}
