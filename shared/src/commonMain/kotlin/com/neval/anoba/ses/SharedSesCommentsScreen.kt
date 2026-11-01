package com.neval.anoba.ses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSesCommentsScreen(
    sesId: String = "",
    comments: List<SharedSesCommentModel> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentUserId: String? = null,
    isAdmin: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSendComment: (commentText: String, replyToId: String?) -> Unit = { _, _ -> },
    onLikeComment: (commentId: String) -> Unit = {},
    onDeleteComment: (commentId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sampleComments = remember(comments) {
        comments.ifEmpty {
            listOf(
                SharedSesCommentModel(
                    id = "c1",
                    sesId = sesId,
                    userId = "u1",
                    username = "Ahmet K.",
                    content = "Harika bir ses kaydı olmuş! 🎙️",
                    timestampMillis = 1710000000000L,
                    likedBy = listOf("u2"),
                    userBadgeType = "PIONEER"
                ),
                SharedSesCommentModel(
                    id = "c2",
                    sesId = sesId,
                    userId = "u2",
                    username = "Zeynep S.",
                    content = "Çok beğendim, devamını bekliyoruz.",
                    timestampMillis = 1710000050000L,
                    likedBy = listOf("u1", "u3")
                )
            )
        }
    }

    var commentInput by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<SharedSesCommentModel?>(null) }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "💬 Yorumlar (${sampleComments.size})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Ana Sayfa",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                Column {
                    AnimatedVisibility(
                        visible = replyingToComment != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF334155))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yanıtlanan: @${replyingToComment?.username}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            IconButton(
                                onClick = { replyingToComment = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "İptal", tint = Color.LightGray)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Yorum yazın...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    onSendComment(commentInput.trim(), replyingToComment?.id)
                                    commentInput = ""
                                    replyingToComment = null
                                }
                            },
                            enabled = commentInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (commentInput.isNotBlank()) Color(0xFFE11D48) else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gönder",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && sampleComments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null && sampleComments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }

                sampleComments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz yorum yapılmamış. İlk yorumu sen yap!", color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sampleComments, key = { it.id }) { comment ->
                            val isLiked = currentUserId?.let { comment.likedBy.contains(it) } == true
                            val canDelete = currentUserId == comment.userId || isAdmin

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                UserAvatar(
                                    photoUrl = comment.userProfileUrl,
                                    username = comment.username,
                                    size = 36.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        AnobaBadge(
                                            badgeType = comment.userBadgeType,
                                            isOfficial = comment.isOfficial
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = comment.content,
                                        color = Color.LightGray,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Yanıtla",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.clickable { replyingToComment = comment }
                                        )

                                        if (canDelete) {
                                            Text(
                                                text = "Sil",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.clickable { onDeleteComment(comment.id) }
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onLikeComment(comment.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Beğen",
                                        tint = if (isLiked) Color(0xFFE11D48) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
