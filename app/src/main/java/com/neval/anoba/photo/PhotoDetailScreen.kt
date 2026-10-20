package com.neval.anoba.photo

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.FollowButton
import com.neval.anoba.common.ui.QuarantineBanner
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRepository
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRow
import com.neval.anoba.photo.photoemoji.PhotoReactionUsersBottomSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojis
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@SuppressLint("ContextCast", "ComposeParameterOrder")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photoId: String,
    navController: NavController,
    photoViewModel: PhotoViewModel = koinViewModel(),
    authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = koinViewModel(),
    imageLoader: ImageLoader = koinInject(),
    reactionRepository: PhotoEmojiReactionRepository = koinInject()
) {
    val photoState by photoViewModel.photoState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRoleState by authViewModel.userRole.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAnoba = userRoleState?.equals("ADMIN", ignoreCase = true) == true ||
                  (authState as? AuthState.Authenticated)?.email == "morplato@gmail.com"

    val shareState by photoViewModel.shareState.collectAsState()
    val userReactions by photoViewModel.userReactions.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showReactionsSheet by remember { mutableStateOf(false) }

    val reactionUsers by remember(photoId, showReactionsSheet) {
        if (showReactionsSheet) {
            reactionRepository.getPhotoReactionUsers(photoId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    if (showReactionsSheet) {
        PhotoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    val shareChooserTitle = stringResource(id = R.string.photodetail_share_chooser_title)
    val backText = stringResource(id = R.string.photodetail_back)
    val shareText = stringResource(id = R.string.photodetail_share)
    val homeText = stringResource(id = R.string.photodetail_home)

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { }

    LaunchedEffect(key1 = photoViewModel.eventFlow) {
        photoViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is PhotoUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(shareState) {
        if (shareState is ShareState.Success) {
            val state = shareState as ShareState.Success
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, state.uri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            shareLauncher.launch(Intent.createChooser(shareIntent, shareChooserTitle))
            photoViewModel.resetShareState()
        }
    }

    LaunchedEffect(photoId) {
        photoViewModel.loadPhotoDetails(photoId)
        photoViewModel.incrementViewCount(photoId)
        photoViewModel.loadComments(photoId)
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText, tint = Color.White)
                    }
                },
                actions = {
                    val currentPhotoUrl = (photoState as? UiState.Success)?.data?.photoUrl ?: ""
                    IconButton(onClick = { photoViewModel.sharePhoto(context, currentPhotoUrl, photoId) }) {
                        Icon(Icons.Default.Share, contentDescription = shareText, tint = Color.White)
                    }
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = homeText, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        when (val state = photoState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is UiState.Success -> {
                val currentPhoto = state.data
                val topReaction = currentPhoto.reactions.maxByOrNull { it.value }

                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    Column(modifier = Modifier.zIndex(10f).align(Alignment.TopCenter)) {
                        QuarantineBanner(isFlagged = currentPhoto.isFlagged, flagReason = currentPhoto.flagReason)
                        
                        if (currentPhoto.isFlagged) {
                            Button(
                                onClick = { 
                                    if (isAnoba) {
                                        photoViewModel.unflagPhoto(currentPhoto.id) 
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
                                            photoViewModel.toggleReaction(currentPhoto.id, "❤️")
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
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AsyncImage(
                            model = currentPhoto.photoUrl,
                            contentDescription = null,
                            imageLoader = imageLoader,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp)
                                .graphicsLayer(
                                    scaleX = scale.value,
                                    scaleY = scale.value,
                                    translationX = offsetX.value,
                                    translationY = offsetY.value
                                ),
                            contentScale = ContentScale.FillWidth
                        )

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
                                .align(Alignment.Center)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                photoUrl = currentPhoto.userProfileImageUrl,
                                username = currentPhoto.username,
                                size = 36.dp,
                                imageLoader = imageLoader
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = currentPhoto.username.ifBlank { "Anonim" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentPhoto.isOfficial) AnobaGold else Color.White,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            AnobaBadge(badgeType = currentPhoto.userBadgeType, isOfficial = currentPhoto.isOfficial)

                            if (currentPhoto.isPopular) {
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
                                targetUserId = currentPhoto.ownerId,
                                currentUserId = currentUserId,
                                isDarkBackground = true,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RemoveRedEye, null, Modifier.size(16.dp), Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = " ${currentPhoto.viewCount}  •  ${DateTimeUtils.formatRelativeTime(context, currentPhoto.publishTimestamp?.time ?: currentPhoto.createdAt?.time)}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                if (topReaction != null && topReaction.value > 0) {
                                    Spacer(Modifier.width(12.dp))
                                    val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { showReactionsSheet = true }
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(displayEmoji, fontSize = 12.sp)
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "${topReaction.value}", // 🚀 TOPLAM YERİNE TEKİL LİDER SKOR
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
                                    .clickable {
                                        val route = Constants.PHOTO_COMMENTS_ROUTE.replace("{photoId}", currentPhoto.id)
                                        navController.navigate(route)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Comment, null, Modifier.size(14.dp), Color.White)
                                if (currentPhoto.commentsCount > 0) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(currentPhoto.commentsCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        PhotoEmojiReactionRow(
                            reactions = currentPhoto.reactions,
                            selectedEmoji = userReactions[currentPhoto.id],
                            onReact = { emoji -> photoViewModel.toggleReaction(currentPhoto.id, emoji) }
                        )

                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red)
                }
            }
            else -> {}
        }
    }
}
