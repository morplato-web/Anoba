package com.neval.anoba.letter

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import coil3.ImageLoader
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.ui.FollowButton
import com.neval.anoba.common.ui.OfficialBadge
import com.neval.anoba.common.ui.QuarantineBanner
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.letter.lettercomment.LetterCommentViewModel
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRepository
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRow
import com.neval.anoba.letter.letteremoji.LetterReactionUsersBottomSheet
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDetailScreen(
    letterId: String,
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    commentViewModel: LetterCommentViewModel = koinViewModel { parametersOf(letterId) },
    imageLoader: ImageLoader = koinInject(),
    letterRepository: LetterRepository = koinInject(),
    reactionRepository: LetterEmojiReactionRepository = koinInject()
) {
    val letterState by viewModel.selectedLetter.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRoleState by authViewModel.userRole.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid ?: ""
    val isAnoba = userRoleState?.equals("ADMIN", ignoreCase = true) == true ||
                  (authState as? AuthState.Authenticated)?.email == "morplato@gmail.com"

    var showReactionUsersSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val anonymousSender = stringResource(id = R.string.letterdetail_anonymous_sender)
    val commentText = stringResource(id = R.string.letterdetail_comment)
    val closeButtonText = stringResource(id = R.string.letterdetail_close_button)
    val backText = stringResource(id = R.string.photodetail_back)

    val reactionUsers by remember(letterId, showReactionUsersSheet) {
        if (showReactionUsersSheet) {
            reactionRepository.getLetterReactionUsers(letterId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(letterId) {
        if (letterId.isNotBlank()) {
            viewModel.getLetterDetails(letterId)
            viewModel.incrementLetterViewCount(letterId)
            if (currentUserId.isNotBlank()) {
                // Her iki bildirim havuzunu da temizle (Master Admin ise)
                letterRepository.markLetterNotificationsAsRead(currentUserId, letterId)
                if (isAnoba) {
                    letterRepository.markAdminNotificationAsRead(letterId)
                }
            }
        }
    }

    when (val state = letterState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AnobaGold)
                }
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success<LetterModel> -> {
            val currentLetter = state.data
            val isAudio = !currentLetter.audioUrl.isNullOrBlank()

            if (showReactionUsersSheet) {
                LetterReactionUsersBottomSheet(
                    reactionUsers = reactionUsers,
                    onDismiss = { showReactionUsersSheet = false }
                )
            }

            // ExoPlayer (Sesli Mektup İçin)
            var isPlaying by remember { mutableStateOf(false) }
            var currentPosition by remember { mutableLongStateOf(0L) }
            var totalDuration by remember { mutableLongStateOf(currentLetter.audioDuration ?: 0L) }

            val exoPlayer = remember(currentLetter.audioUrl) {
                if (isAudio && !currentLetter.audioUrl.isNullOrBlank()) {
                    MediaCacheManager.createPlayer(context.applicationContext).apply {
                        val mediaItem = MediaItem.fromUri(currentLetter.audioUrl!!)
                        setMediaItem(mediaItem)
                        prepare()
                    }
                } else null
            }

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            if (totalDuration <= 0L && (exoPlayer?.duration ?: 0L) > 0L) {
                                totalDuration = exoPlayer!!.duration
                            }
                        } else if (playbackState == Player.STATE_ENDED) {
                            exoPlayer?.seekTo(0)
                            exoPlayer?.pause()
                        }
                    }
                }
                exoPlayer?.addListener(listener)

                onDispose {
                    exoPlayer?.removeListener(listener)
                    exoPlayer?.stop()
                    exoPlayer?.release()
                }
            }

            LaunchedEffect(isPlaying) {
                while (isPlaying && exoPlayer != null) {
                    currentPosition = exoPlayer.currentPosition
                    delay(500L)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.letterdetail_title)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                val shareContent = if (!currentLetter.audioUrl.isNullOrBlank()) {
                                    "${currentLetter.content}\n\nSesli Mektup: ${currentLetter.audioUrl}"
                                } else {
                                    currentLetter.content
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareContent)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                            IconButton(onClick = {
                                navController.navigate(Constants.HOME_SCREEN) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }) {
                                Icon(Icons.Default.Home, contentDescription = null)
                            }
                        }
                    )
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QuarantineBanner(isFlagged = currentLetter.isFlagged, flagReason = currentLetter.flagReason)

                        if (currentLetter.isFlagged) {
                            Button(
                                onClick = { 
                                    if (isAnoba) {
                                        viewModel.unflagLetter(currentLetter.id) 
                                    } else {
                                        Toast.makeText(context, "Sadece yetkililer karantinadan çıkarabilir.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAnoba) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isAnoba) "KARANTİNADAN ÇIKAR" else "İNCELEMEDE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFCFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val displaySender = if (currentLetter.isAskAnoba && !currentLetter.pseudonym.isNullOrBlank()) {
                                            currentLetter.pseudonym ?: ""
                                        } else {
                                            currentLetter.username.ifBlank { anonymousSender }
                                        }

                                        UserAvatar(
                                            photoUrl = currentLetter.userProfileImageUrl,
                                            username = displaySender,
                                            size = 32.dp,
                                            imageLoader = imageLoader
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(id = R.string.letterdetail_sender, displaySender),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (currentLetter.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (currentLetter.isOfficial) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            OfficialBadge(size = 14.dp)
                                        }

                                        if (currentLetter.isPopular) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = AnobaGold,
                                                shape = CircleShape,
                                                modifier = Modifier.size(24.dp),
                                                shadowElevation = 4.dp
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("1k", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }

                                        FollowButton(
                                            targetUserId = currentLetter.ownerId,
                                            currentUserId = currentUserId,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }

                                    if (currentLetter.privacy == "SEALED" && !currentLetter.recipientUsername.isNullOrBlank()) {
                                        Surface(
                                            color = AnobaGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(10.dp),
                                                    tint = Color(0xFF7B5E00)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(id = R.string.lettercard_recipient, currentLetter.recipientUsername ?: ".."),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF7B5E00)
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ses Oynatıcı Paneli
                                if (isAudio && exoPlayer != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                                },
                                                modifier = Modifier.size(52.dp).background(Color.White, CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(32.dp),
                                                    tint = AnobaGold
                                                )
                                            }

                                            Slider(
                                                value = currentPosition.toFloat(),
                                                onValueChange = { newPos ->
                                                    exoPlayer.seekTo(newPos.toLong())
                                                    currentPosition = newPos.toLong()
                                                },
                                                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                                                modifier = Modifier.fillMaxWidth().graphicsLayer(scaleY = 0.6f)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(formatDuration(currentPosition), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                Text(formatDuration(totalDuration), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    }
                                }

                                if (currentLetter.content.isNotBlank()) {
                                    Text(
                                        text = currentLetter.content,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            lineHeight = 24.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val displayDate = currentLetter.publishTimestamp ?: currentLetter.createdAt
                                displayDate?.let {
                                    Text(
                                        text = DateTimeUtils.formatRelativeTime(context, it.time),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }

                        // 🌙 ANOBA ABLASI CEVAP PANELİ (Eğer cevap varsa)
                        if (!currentLetter.anobaResponse.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, AnobaGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(id = R.string.letter_anoba_response_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            color = AnobaGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = currentLetter.anobaResponse ?: "",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            lineHeight = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val userReactions by viewModel.userReactions.collectAsState()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentLetter.viewCount > 0) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentLetter.viewCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            val topReaction = currentLetter.reactions.maxByOrNull { it.value }
                            if (topReaction != null && topReaction.value > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showReactionUsersSheet = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = VideoEmojis.getDisplayEmoji(topReaction.key),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${topReaction.value}", // 🚀 TEKİL LİDER SKOR
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LetterEmojiReactionRow(
                            reactions = currentLetter.reactions,
                            selectedEmoji = userReactions[letterId],
                            onReact = { emoji -> viewModel.toggleReaction(letterId, emoji) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val route = Constants.LETTER_COMMENTS_ROUTE.replace("{letterId}", letterId)
                                    navController.navigate(route)
                                }
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Comment,
                                    contentDescription = commentText,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = commentText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (comments.isNotEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.letterdetail_comments_count, comments.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(closeButtonText)
                        }

                        Spacer(modifier = Modifier.height(34.dp))
                    }
                }
            )
        }
        else -> { }
    }
}
