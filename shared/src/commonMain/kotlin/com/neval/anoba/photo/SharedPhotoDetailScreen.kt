package com.neval.anoba.photo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AnobaGold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPhotoDetailScreen(
    photo: SharedPhotoDetailModel? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentUserId: String? = null,
    isAdminOrModerator: Boolean = false,
    userReactions: Map<String, String> = emptyMap(),
    reactionUsers: List<SharedReactionUser> = emptyList(),
    isFollowingAuthor: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onOpenComments: () -> Unit = {},
    onReactionClick: (String) -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onUnflagPhoto: () -> Unit = {},
    onRequestReactionUsers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showReactionsSheet by remember { mutableStateOf(false) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    if (showReactionsSheet) {
        SharedPhotoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (photo != null && (currentUserId == photo.ownerId || isAdminOrModerator)) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
                            tint = Color.White
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
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && photo == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                errorMessage != null && photo == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                    }
                }

                photo != null -> {
                    val topReaction = photo.reactions.maxByOrNull { it.value }

                    if (photo.isFlagged) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(10f)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFFDC2626).copy(alpha = 0.9f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = photo.flagReason ?: "Bu içerik inceleme altındadır.",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (isAdminOrModerator) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onUnflagPhoto,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Karantinadan Çıkar", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scope.launch {
                                        val newScale = (scale.value * zoom).coerceIn(1f, 4f)
                                        scale.snapTo(newScale)
                                        if (newScale > 1f) {
                                            offsetX.snapTo(offsetX.value + pan.x)
                                            offsetY.snapTo(offsetY.value + pan.y)
                                        }
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scope.launch {
                                            onReactionClick("❤️")
                                            heartScale.snapTo(0f)
                                            heartAlpha.snapTo(1f)
                                            launch { heartScale.animateTo(1.5f, spring(dampingRatio = 0.5f)) }
                                            launch {
                                                delay(600)
                                                heartAlpha.animateTo(0f, tween(300))
                                            }
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.all { !it.pressed }) {
                                            scope.launch {
                                                coroutineScope {
                                                    launch { scale.animateTo(1f) }
                                                    launch { offsetX.animateTo(0f) }
                                                    launch { offsetY.animateTo(0f) }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = photo.photoUrl,
                                contentDescription = photo.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer(
                                        scaleX = scale.value,
                                        scaleY = scale.value,
                                        translationX = offsetX.value,
                                        translationY = offsetY.value
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("🖼️ Fotoğraf", color = Color.Gray, fontSize = 16.sp)
                        }

                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer(
                                    scaleX = heartScale.value,
                                    scaleY = heartScale.value,
                                    alpha = heartAlpha.value
                                )
                                .zIndex(1f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!photo.userProfileImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = photo.userProfileImageUrl,
                                        contentDescription = photo.username,
                                        modifier = Modifier.matchParentSize()
                                    )
                                } else {
                                    Text(
                                        text = photo.username.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = photo.username.ifBlank { "Anonim" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (photo.isOfficial) AnobaGold else Color.White,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (photo.isPopular) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AnobaGold,
                                    shape = CircleShape,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("1k", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            if (currentUserId != null && currentUserId != photo.ownerId) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    color = if (isFollowingAuthor) Color.White.copy(alpha = 0.2f) else AnobaGold,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onToggleFollow() }
                                ) {
                                    Text(
                                        text = if (isFollowingAuthor) "Takipte" else "Takip Et",
                                        color = if (isFollowingAuthor) Color.White else Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (photo.personalNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = photo.personalNote,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Sol Küme: Göz + İzlenme ve Lider Emoji
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = photo.viewCount.toString(),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                if (topReaction != null && topReaction.value > 0) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onRequestReactionUsers()
                                                showReactionsSheet = true
                                            }
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(topReaction.key, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${topReaction.value}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Sağ Küme: Zaman Damgası ve Kutusuz Yorum İkonu
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (photo.timeAgoText.isNotBlank()) {
                                    Text(
                                        text = photo.timeAgoText,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clickable { onOpenComments() }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Comment,
                                        contentDescription = "Yorumlar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    if (photo.commentsCount > 0) {
                                        Text(
                                            text = photo.commentsCount.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SharedPhotoEmojiReactionRow(
                            reactions = photo.reactions,
                            selectedEmoji = userReactions[photo.id],
                            onReact = onReactionClick
                        )
                    }
                }
            }
        }
    }
}