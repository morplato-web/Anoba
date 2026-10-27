package com.neval.anoba.video

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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val AnobaGold = Color(0xFFFFD700)
private val AnobaNavyDark = Color(0xFF0F172A)
private val AnobaTopBarNavy = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVideoCommentsScreen(
    videoId: String = "",
    comments: List<SharedVideoCommentModel> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentUserId: String? = null,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSendComment: (content: String, replyToId: String?) -> Unit = { _, _ -> },
    onLikeComment: (commentId: String) -> Unit = {},
    onDeleteComment: (commentId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<SharedVideoCommentModel?>(null) }

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val layoutDirection = LocalLayoutDirection.current

    // İç İçe Yorum Ağacı (Comment -> Replies)
    val commentTree = remember(comments) {
        val commentMap = comments.associateBy { it.id }
        val tree = mutableMapOf<SharedVideoCommentModel, MutableList<SharedVideoCommentModel>>()
        comments.forEach { comment ->
            if (comment.replyToId == null) {
                tree.getOrPut(comment) { mutableListOf() }
            } else {
                commentMap[comment.replyToId]?.let { parent ->
                    tree.getOrPut(parent) { mutableListOf() }.add(comment)
                }
            }
        }
        tree.mapValues { entry -> entry.value.sortedBy { it.timestampMillis } }
    }
    val mainComments = remember(commentTree) {
        commentTree.keys.sortedBy { it.timestampMillis }
    }

    LaunchedEffect(replyingToComment) {
        if (replyingToComment != null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = AnobaNavyDark,
        modifier = modifier.imePadding(),
        topBar = {
            Surface(
                color = AnobaTopBarNavy,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Yorumlar (${comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )

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
                shadowElevation = 8.dp,
                color = AnobaTopBarNavy
            ) {
                Column {
                    // Yanıt Veriliyor Bildirim Çubuğu
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
                                text = "${replyingToComment?.username} kullanıcısına yanıt veriliyor...",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            IconButton(
                                onClick = { replyingToComment = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "İptal",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Kapsül Giriş Çubuğu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    text = if (replyingToComment != null) "Yanıtınızı yazın..." else "Bir yorum ekleyin...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onSendComment(commentText.trim(), replyingToComment?.id)
                                    commentText = ""
                                    replyingToComment = null
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                                    else Color(0xFF334155)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gönder",
                                tint = if (commentText.isNotBlank()) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && comments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                errorMessage != null && comments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                    }
                }

                comments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Henüz yorum yapılmamış. İlk yorumu sen yap!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        mainComments.forEach { mainComment ->
                            item(key = mainComment.id) {
                                SharedVideoCommentItem(
                                    comment = mainComment,
                                    currentUserId = currentUserId,
                                    onLikeClicked = { onLikeComment(mainComment.id) },
                                    onReplyClicked = { replyingToComment = mainComment },
                                    onDeleteClicked = { onDeleteComment(mainComment.id) }
                                )
                            }
                            items(commentTree[mainComment].orEmpty(), key = { "reply_${it.id}" }) { reply ->
                                Row {
                                    Spacer(modifier = Modifier.width(if (layoutDirection == LayoutDirection.Ltr) 44.dp else 0.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        SharedVideoCommentItem(
                                            comment = reply,
                                            currentUserId = currentUserId,
                                            onLikeClicked = { onLikeComment(reply.id) },
                                            onReplyClicked = { replyingToComment = mainComment },
                                            onDeleteClicked = { onDeleteComment(reply.id) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(if (layoutDirection == LayoutDirection.Rtl) 44.dp else 0.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedVideoCommentItem(
    comment: SharedVideoCommentModel,
    currentUserId: String?,
    onLikeClicked: () -> Unit,
    onReplyClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val isLiked = currentUserId?.let { comment.likedBy.contains(it) } ?: false

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (!comment.userProfileUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = comment.userProfileUrl,
                        contentDescription = comment.username,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = comment.username.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (comment.isOfficial) AnobaGold else Color.White
                    )
                    if (comment.isOfficial) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = AnobaGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "RESMİ",
                                color = AnobaGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (comment.timeAgoText.isNotBlank()) {
                        Text(
                            text = comment.timeAgoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "Yanıtla",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.LightGray,
                        modifier = Modifier.clickable { onReplyClicked() }
                    )
                    if (currentUserId == comment.userId) {
                        Text(
                            text = "Sil",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            modifier = Modifier.clickable { onDeleteClicked() }
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(onClick = onLikeClicked, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Beğen",
                    tint = if (isLiked) Color(0xFFE91E63) else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (comment.likedBy.isNotEmpty()) {
                Text(
                    text = comment.likedBy.size.toString(),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
