package com.neval.anoba.ses

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.photo.SharedReactionUser
import com.neval.anoba.video.SharedVideoEmojiReactionRow
import com.neval.anoba.video.SharedVideoReactionUsersBottomSheet
import com.neval.anoba.video.formatDurationMillis

private val AnobaGold = Color(0xFFFFD700)
private val AnobaBackgroundDark = Color(0xFF0F172A)
private val AnobaTopBarNavy = Color(0xFF1E293B)
private val SesPinkAccent = Color(0xFFE11D48)

@Composable
fun SoundWaveAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "sound_wave_transition")
    val waveHeights = List(5) {
        val duration = remember { (900..1500).random() }
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 1f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(vertical = 8.dp)
    ) {
        val barCount = 42
        val barWidth = size.width / (barCount * 2f)
        val maxBarHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            val waveIndex = i % waveHeights.size
            val currentHeight = maxBarHeight * (if (isPlaying) waveHeights[waveIndex].value else 0.15f)
            val yOffset = (size.height - currentHeight) / 2f

            drawRoundRect(
                color = if (isPlaying) SesPinkAccent else Color.LightGray.copy(alpha = 0.6f),
                topLeft = Offset(x = i * barWidth * 2f + barWidth / 2f, y = yOffset),
                size = Size(width = barWidth, height = currentHeight),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSesDetailScreen(
    ses: SharedSesDetailModel? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentUserId: String? = null,
    isAdminOrModerator: Boolean = false,
    userReactions: Map<String, String> = emptyMap(),
    reactionUsers: List<SharedReactionUser> = emptyList(),
    isFollowingAuthor: Boolean = false,
    isPlaying: Boolean = false,
    currentPositionMillis: Long = 0L,
    totalDurationMillis: Long = 0L,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onOpenComments: () -> Unit = {},
    onReactionClick: (String) -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onUnflagSes: () -> Unit = {},
    onRequestReactionUsers: () -> Unit = {},
    onPlayPauseToggle: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    audioPlayerContent: (@Composable () -> Unit)? = null
) {
    var showReactionsSheet by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentPositionMillis, isSeeking) {
        if (!isSeeking) {
            sliderPosition = currentPositionMillis.toFloat()
        }
    }

    if (showReactionsSheet) {
        SharedVideoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    Scaffold(
        containerColor = AnobaBackgroundDark,
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
                        text = "🎙️ Sesli Not",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (ses != null && (currentUserId == ses.ownerId || isAdminOrModerator)) {
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
                isLoading && ses == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null && ses == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                    }
                }

                ses != null -> {
                    val topReaction = ses.reactions.maxByOrNull { it.value }
                    val activeDurationMs = if (totalDurationMillis > 0L) totalDurationMillis else ses.durationMillis

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (ses.isFlagged) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDC2626).copy(alpha = 0.9f))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ses.flagReason ?: "Bu ses kaydı inceleme altındadır.",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (isAdminOrModerator) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = onUnflagSes,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Karantinadan Çıkar", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF334155)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!ses.userProfileImageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = ses.userProfileImageUrl,
                                                    contentDescription = ses.username,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            } else {
                                                Text(
                                                    text = ses.username.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = ses.username,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = if (ses.isOfficial) AnobaGold else Color.White
                                                )
                                                if (ses.isOfficial) {
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
                                            if (ses.title.isNotBlank()) {
                                                Text(
                                                    text = ses.title,
                                                    fontSize = 13.sp,
                                                    color = Color.LightGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    if (currentUserId != null && currentUserId != ses.ownerId) {
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

                                if (ses.personalNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = ses.personalNote,
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SoundWaveAnimation(isPlaying = isPlaying)

                                Spacer(modifier = Modifier.height(12.dp))

                                if (audioPlayerContent != null) {
                                    audioPlayerContent()
                                } else {
                                    IconButton(
                                        onClick = onPlayPauseToggle,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(SesPinkAccent)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) "Durdur" else "Oynat",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Slider(
                                        value = sliderPosition,
                                        onValueChange = {
                                            isSeeking = true
                                            sliderPosition = it
                                        },
                                        onValueChangeFinished = {
                                            onSeekTo(sliderPosition.toLong())
                                            isSeeking = false
                                        },
                                        valueRange = 0f..activeDurationMs.toFloat().coerceAtLeast(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = SesPinkAccent,
                                            activeTrackColor = SesPinkAccent,
                                            inactiveTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatDurationMillis(sliderPosition.toLong()),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = formatDurationMillis(activeDurationMs),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${ses.viewCount} · ${ses.timeAgoText}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                if (topReaction != null && topReaction.value > 0) {
                                    Spacer(modifier = Modifier.width(12.dp))
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

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { onOpenComments() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Comment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                if (ses.commentsCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = ses.commentsCount.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SharedVideoEmojiReactionRow(
                            reactions = ses.reactions,
                            selectedEmoji = userReactions[ses.id],
                            onReact = onReactionClick
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ses kaydı bulunamadı.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}