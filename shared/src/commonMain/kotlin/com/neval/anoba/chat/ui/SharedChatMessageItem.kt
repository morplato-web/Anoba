package com.neval.anoba.chat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Messageable> ChatMessageItem(
    message: T,
    isSentByCurrentUser: Boolean,
    isSelected: Boolean,
    onLongPress: (T) -> Unit,
    onClick: (T) -> Unit,
    groupMemberCount: Int = 0
) {
    val isSystemMessage = (message.senderId == "SYSTEM") ||
            (message.senderName == "SYSTEM") ||
            (message.content?.contains("gruba ekledi") == true) ||
            (message.content?.contains("gruptan") == true)

    if (isSystemMessage) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.08f)
            ) {
                Text(
                    text = message.content ?: "",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isSentByCurrentUser -> Color(0xFFE7FFDB)
        else -> Color.White
    }
    val textColor = Color.Black
    val anonymousText = "Misafir"
    val isOfficialBadge = message.senderBadgeType == "OFFICIAL"
    val rawName = message.senderName.takeIf { !it.isNullOrBlank() } ?: anonymousText

    val displayName = if (isOfficialBadge && !rawName.contains("anoba", ignoreCase = true)) {
        "Anoba"
    } else {
        rawName
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isSentByCurrentUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                if (!message.senderPhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = message.senderPhotoUrl,
                        contentDescription = "Profil Resmi",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    InitialsAvatar(
                        name = displayName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isSentByCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = { onClick(message) },
                    onLongClick = { onLongPress(message) }
                )
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp)
                ) {
                    val isAnoba = displayName.contains("anoba", ignoreCase = true) || isOfficialBadge
                    val hasBadge = !message.senderBadgeType.isNullOrBlank() && message.senderBadgeType != "NONE"

                    if (!isSentByCurrentUser || isAnoba || hasBadge) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSentByCurrentUser && !isAnoba) "Ben" else displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isAnoba) AnobaGold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            AnobaBadge(
                                badgeType = message.senderBadgeType,
                                isOfficial = isAnoba
                            )
                        }
                    }

                    if (message.replyToId != null) {
                        ReplyQuote(
                            replyToName = message.replyToName,
                            replyToContent = message.replyToContent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    when {
                        !message.imageUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Görsel",
                                modifier = Modifier
                                    .width(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }

                    if (!message.content.isNullOrEmpty()) {
                        Text(
                            text = message.content ?: "",
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(
                                end = 44.dp,
                                bottom = 2.dp
                            )
                        )
                    }
                }

                val isMediaOnly = !message.imageUrl.isNullOrEmpty() && message.content.isNullOrEmpty()

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 6.dp)
                        .then(
                            if (isMediaOnly)
                                Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSentByCurrentUser) {
                        val isRead = when {
                            groupMemberCount > 1 && message.readBy != null -> {
                                (message.readBy?.size ?: 0) >= (groupMemberCount - 1)
                            }
                            message.status == "READ" -> true
                            else -> false
                        }

                        Icon(
                            imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (isRead) Color(0xFF34B7F1) else if (isMediaOnly) Color.White.copy(alpha = 0.7f) else textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyQuote(
    replyToName: String?,
    replyToContent: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(AnobaNavy)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = replyToName ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AnobaNavy
            )
            Text(
                text = replyToContent ?: "Medya",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
        }
    }
}
