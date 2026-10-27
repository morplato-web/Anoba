package com.neval.anoba.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedPhotoCommentItem(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileUrl: String? = null,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val userBadgeType: String? = "NONE",
    val isOfficial: Boolean = false,
    val timeAgo: String = "Şimdi"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPhotoCommentsScreen(
    photoId: String = "",
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack
) {
    var commentText by remember { mutableStateOf("") }
    val commentsList = remember {
        mutableStateListOf(
            SharedPhotoCommentItem(
                id = "c1",
                userId = "u1",
                username = "Merve K.",
                content = "Harika bir kare! Renkler çok güzel 📸✨",
                likesCount = 8,
                isLiked = true,
                timeAgo = "1s"
            ),
            SharedPhotoCommentItem(
                id = "c2",
                userId = "u2",
                username = "Anoba Official",
                content = "Fotoğrafınız günün seçkilerine eklendi 🌟",
                likesCount = 24,
                userBadgeType = "OFFICIAL",
                isOfficial = true,
                timeAgo = "3s"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 Fotoğraf Yorumları", fontWeight = FontWeight.Bold, color = AnobaNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Bir yorum yaz...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                commentsList.add(
                                    SharedPhotoCommentItem(
                                        id = "c_${commentsList.size + 1}",
                                        userId = "me",
                                        username = "Ben",
                                        content = commentText.trim(),
                                        timeAgo = "Şimdi"
                                    )
                                )
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (commentText.isNotBlank()) AnobaNavy else Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gönder",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(commentsList, key = { it.id }) { comment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                        UserAvatar(
                            photoUrl = comment.userProfileUrl,
                            username = comment.username,
                            size = 36.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = comment.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (comment.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurface
                                )
                                AnobaBadge(
                                    badgeType = comment.userBadgeType,
                                    isOfficial = comment.isOfficial
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = comment.timeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val idx = commentsList.indexOfFirst { it.id == comment.id }
                                if (idx != -1) {
                                    val item = commentsList[idx]
                                    val newLiked = !item.isLiked
                                    val newCount = if (newLiked) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0)
                                    commentsList[idx] = item.copy(isLiked = newLiked, likesCount = newCount)
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Beğen",
                                tint = if (comment.isLiked) Color(0xFFE91E63) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (comment.likesCount > 0) {
                            Text(text = "${comment.likesCount}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
