package com.neval.anoba.photo.photocomment

import android.content.Context
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaGold
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCommentsScreen(
    navController: NavController,
    photoId: String,
) {
    val viewModel: PhotoCommentViewModel = koinViewModel(parameters = { parametersOf(photoId) })
    val authViewModel: AuthViewModel = koinViewModel()
    val authService: AuthServiceInterface = koinInject()

    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val errorResId by viewModel.error.collectAsState()
    val replyingToComment by viewModel.replyingToComment.collectAsState()
    val listState = rememberLazyListState()
    val authState by authService.authStateFlow.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val layoutDirection = LocalLayoutDirection.current

    val commentTree = remember(comments) {
        val commentMap = comments.associateBy { it.id }
        val tree = mutableMapOf<PhotoComment, MutableList<PhotoComment>>()
        comments.forEach { comment ->
            if (comment.replyToId == null) {
                tree.getOrPut(comment) { mutableListOf() }
            } else {
                commentMap[comment.replyToId]?.let { parent ->
                    tree.getOrPut(parent) { mutableListOf() }.add(comment)
                }
            }
        }
        tree.mapValues { it.value.sortedBy { reply -> reply.timestamp } }
    }
    val mainComments = commentTree.keys.sortedBy { it.timestamp }

    LaunchedEffect(replyingToComment) {
        if (replyingToComment != null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.photocomments_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = stringResource(id = R.string.photodetail_home))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            CommentInputBar(
                commentText = commentText,
                onTextChanged = { viewModel.onCommentTextChanged(it) },
                onSendClicked = { viewModel.addComment() },
                replyingTo = replyingToComment,
                onCancelReply = { viewModel.cancelReply() },
                focusRequester = focusRequester
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    mainComments.forEach { mainComment ->
                        item(key = mainComment.id) {
                            PhotoCommentItem(
                                comment = mainComment,
                                currentUserId = (authState as? AuthState.Authenticated)?.uid,
                                canDelete = (authState as? AuthState.Authenticated)?.uid == mainComment.userId || userRole == "ADMIN" || userRole == "MUHTAR",
                                onLikeClicked = { viewModel.likeComment(mainComment.id) },
                                onReplyClicked = { viewModel.onReplyClicked(mainComment) },
                                onDeleteClicked = { viewModel.deleteComment(mainComment, userRole) },
                                context = context
                            )
                        }
                        items(commentTree[mainComment].orEmpty(), key = { "reply_${it.id}" }) { reply ->
                            Row {
                                Spacer(modifier = Modifier.width(if (layoutDirection == LayoutDirection.Ltr) 44.dp else 0.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    PhotoCommentItem(
                                        comment = reply,
                                        currentUserId = (authState as? AuthState.Authenticated)?.uid,
                                        canDelete = (authState as? AuthState.Authenticated)?.uid == reply.userId || userRole == "ADMIN" || userRole == "MUHTAR",
                                        onLikeClicked = { viewModel.likeComment(reply.id) },
                                        onReplyClicked = { viewModel.onReplyClicked(mainComment) },
                                        onDeleteClicked = { viewModel.deleteComment(reply, userRole) },
                                        context = context
                                    )
                                }
                                Spacer(modifier = Modifier.width(if (layoutDirection == LayoutDirection.Rtl) 44.dp else 0.dp))
                            }
                        }
                    }
                }
            }

            errorResId?.let {
                Text(
                    text = stringResource(id = it),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                )
            }
        }
    }
}

@Composable
fun CommentInputBar(
    commentText: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    replyingTo: PhotoComment?,
    onCancelReply: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            AnimatedVisibility(visible = replyingTo != null, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.photocomments_replying_to, replyingTo?.username ?: "..."),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }

            // Instagram Tarzı Kapsül Giriş Çubuğu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.photocomments_comment_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendClicked,
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = if (commentText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCommentItem(
    comment: PhotoComment,
    currentUserId: String?,
    canDelete: Boolean,
    onLikeClicked: () -> Unit,
    onReplyClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    context: Context
) {
    val isLiked = currentUserId?.let { comment.likedBy.contains(it) } ?: false

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = comment.userProfileUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                error = rememberVectorPainter(Icons.Default.AccountCircle)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (comment.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurface
                    )
                    com.neval.anoba.common.ui.AnobaBadge(badgeType = comment.userBadgeType)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Zarif Aksiyon Satırı: 3d • Yanıtla • Sil
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = DateTimeUtils.formatRelativeTime(context, comment.timestamp?.time ?: 0L),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = stringResource(R.string.photocomments_reply),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onReplyClicked() }
                    )
                    if (canDelete) {
                        Text(
                            text = stringResource(R.string.photocomments_delete),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
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
            IconButton(onClick = onLikeClicked, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                    contentDescription = null
                )
            }
            if (comment.likedBy.isNotEmpty()) {
                Text(
                    text = comment.likedBy.size.toString(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}