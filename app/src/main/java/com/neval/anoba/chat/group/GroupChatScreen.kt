package com.neval.anoba.chat.group

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.ui.ChatInput
import com.neval.anoba.chat.ui.ChatMessageItem
import com.neval.anoba.chat.ui.FullScreenImageViewer
import com.neval.anoba.chat.ui.FullScreenVideoViewer
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.chat.ui.Messageable
import com.neval.anoba.chat.ui.SelectionModeTopAppBar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.getFormattedDate
import com.neval.anoba.common.utils.isSameDay
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLDecoder

private fun createImageUri(context: Context): Uri? {
    val errorStr = context.getString(R.string.groupchatscreen_image_uri_error)
    return try {
        val imageFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    } catch (e: Exception) {
        Log.e("createImageUri", errorStr, e)
        Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show()
        null
    }
}

private fun createVideoUri(context: Context): Uri? {
    val errorStr = context.getString(R.string.groupchatscreen_video_uri_error)
    return try {
        val videoFile = File.createTempFile(
            "MP4_${System.currentTimeMillis()}_",
            ".mp4",
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            videoFile
        )
    } catch (e: Exception) {
        Log.e("createVideoUri", errorStr, e)
        Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show()
        null
    }
}

private suspend fun downloadAndGetUri(context: Context, url: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val extension = if (url.contains("chat_videos")) ".mp4" else ".jpg"
            val file = File.createTempFile("shared_media_", extension, context.cacheDir)

            URL(url).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            Log.e("ShareHelper", "Error downloading file for sharing", e)
            null
        }
    }
}

private fun shareMedia(context: Context, scope: CoroutineScope, mediaUrl: String, sharePreparingText: String, shareWithText: String, shareFailedText: String) {
    scope.launch {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, sharePreparingText, Toast.LENGTH_SHORT).show()
        }
        val localFileUri = downloadAndGetUri(context, mediaUrl)

        withContext(Dispatchers.Main) {
            if (localFileUri != null) {
                val mimeType = context.contentResolver.getType(localFileUri)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, localFileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, shareWithText))
            } else {
                Toast.makeText(context, shareFailedText, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupChatScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel,
    groupId: String,
    authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authenticatedUser = authState as? com.neval.anoba.login.state.AuthState.Authenticated
    val currentUserEmail = authenticatedUser?.email
    val isAdmin = currentUserEmail == "morplato@gmail.com" ||
            authenticatedUser?.displayName?.equals("anoba", ignoreCase = true) == true ||
            authenticatedUser?.displayName?.equals("Anoba", ignoreCase = true) == true

    val currentUserId = groupChatViewModel.currentUserId
    val context = LocalContext.current

    if (currentUserId == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.groupchatscreen_not_logged_in))
        }
        return
    }

    val messageText by groupChatViewModel.messageText.collectAsStateWithLifecycle()
    val pagingItems = groupChatViewModel.pagingMessages.collectAsLazyPagingItems()
    val activeGroup by groupChatViewModel.activeGroup.collectAsStateWithLifecycle()
    val selectedMessages by groupChatViewModel.selectedMessages.collectAsStateWithLifecycle()
    val replyingToMessage by groupChatViewModel.replyingToMessage.collectAsStateWithLifecycle()
    val isSelectionModeActive by groupChatViewModel.isSelectionModeActive.collectAsStateWithLifecycle()
    val isUploading by groupChatViewModel.isUploading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val undoText = stringResource(id = R.string.chathome_undo)

    LaunchedEffect(snackbarHostState) {
        groupChatViewModel.undoState.collectLatest { lastDeletedMessages ->
            if (lastDeletedMessages.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.chathome_messages_deleted_snackbar, lastDeletedMessages.size),
                    actionLabel = undoText,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    groupChatViewModel.undoDelete(groupId)
                } else {
                    groupChatViewModel.clearUndoState()
                }
            }
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var messageToEdit by remember { mutableStateOf<GroupMessage?>(null) }
    val isEditMode by remember { derivedStateOf { messageToEdit != null } }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    var videoToPlayFullScreen by remember { mutableStateOf<String?>(null) }
    var imageToShowFullScreen by remember { mutableStateOf<String?>(null) }

    var isSearchModeActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val cameraPermissionDeniedText = stringResource(id = R.string.camera_permission_denied)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, cameraPermissionDeniedText, Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            if (mimeType?.startsWith("video/") == true) {
                groupChatViewModel.sendVideoMessage(groupId, it, null)
            } else {
                groupChatViewModel.sendImageMessage(groupId, it, null)
            }
            showBottomSheet = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                groupChatViewModel.sendImageMessage(groupId, it, null)
                showBottomSheet = false
            }
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            videoUri?.let {
                groupChatViewModel.sendVideoMessage(groupId, it, null)
                showBottomSheet = false
            }
        }
    }

    val typingUsers by remember(activeGroup) {
        derivedStateOf {
            activeGroup?.typingUsers?.filterKeys { it != currentUserId }?.values?.toList() ?: emptyList()
        }
    }

    val downloadingFileText = stringResource(id = R.string.groupchatscreen_downloading_file)
    val downloadStartedText = stringResource(id = R.string.groupchatscreen_download_started)
    val downloadFailedText = stringResource(id = R.string.groupchatscreen_download_failed)
    val sharePreparingText = stringResource(id = R.string.groupchatscreen_share_preparing)
    val shareWithText = stringResource(id = R.string.groupchatscreen_share_with)
    val shareFailedText = stringResource(id = R.string.groupchatscreen_share_failed)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .filter { it.isNotEmpty() }
            .collect { visibleItems ->
                val itemCount = pagingItems.itemCount
                val unreadMessageIds = visibleItems
                    .filter { it.index in 0 until itemCount }
                    .mapNotNull { pagingItems.peek(it.index) }
                    .filter { it.senderId != currentUserId && !it.readBy.contains(currentUserId) }
                    .map { it.id }

                if (unreadMessageIds.isNotEmpty()) {
                    groupChatViewModel.markMessagesAsRead(unreadMessageIds)
                }
            }
    }

    LaunchedEffect(groupId) {
        if (groupId.isNotBlank()) {
            groupChatViewModel.setActiveGroupId(groupId)
        }
    }

    LaunchedEffect(pagingItems.itemCount) {
        if (pagingItems.itemCount > 0) {
            val isAtBottom = listState.firstVisibleItemIndex <= 1
            if (isAtBottom) {
                listState.animateScrollToItem(0)
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_gallery)) },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = stringResource(id = R.string.chathome_select_from_gallery)) },
                modifier = Modifier.clickable { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_photo)) },
                leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = stringResource(id = R.string.chathome_open_camera)) },
                modifier = Modifier.clickable {
                    val cameraPermission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                        val uri = createImageUri(context)
                        photoUri = uri
                        uri?.let { cameraLauncher.launch(it) }
                    } else {
                        cameraPermissionLauncher.launch(cameraPermission)
                    }
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_video)) },
                leadingContent = { Icon(Icons.Default.Videocam, contentDescription = stringResource(id = R.string.chathome_open_video)) },
                modifier = Modifier.clickable {
                    val cameraPermission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                        val uri = createVideoUri(context)
                        videoUri = uri
                        uri?.let { videoLauncher.launch(it) }
                    } else {
                        cameraPermissionLauncher.launch(cameraPermission)
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.soft_gray1),
            contentDescription = stringResource(id = R.string.chathome_background_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isSelectionModeActive) {
                    val selectedMessage = if (selectedMessages.size == 1) selectedMessages.first() else null

                    SelectionModeTopAppBar(
                        selectedCount = selectedMessages.size,
                        onCloseSelectionMode = { groupChatViewModel.clearMessageSelection() },
                        canDeleteAnySelected = isAdmin || selectedMessages.any {
                            it.senderId == currentUserId || activeGroup?.ownerId == currentUserId
                        },
                        onDeleteSelected = {
                            groupChatViewModel.deleteSelectedMessages(groupId)
                        },

                        canCopyAnySelected = selectedMessages.any { !it.content.isNullOrBlank() },
                        onCopySelected = {
                            val textToCopy = selectedMessages
                                .sortedBy { it.timestampMillis }
                                .joinToString("\n") { it.content ?: "" }
                            groupChatViewModel.copySelectedMessagesToClipboard(context, textToCopy)
                            groupChatViewModel.clearMessageSelection()
                        },

                        canEdit = selectedMessage?.let {
                            it.senderId == currentUserId && it.imageUrl == null && it.videoUrl == null && it.audioUrl == null
                        } ?: false,
                        onEditSelected = {
                            selectedMessage?.let { message ->
                                messageToEdit = message
                                groupChatViewModel.updateMessageText(message.content ?: "")
                                groupChatViewModel.clearMessageSelection()
                                coroutineScope.launch {
                                    delay(100)
                                    focusRequester.requestFocus()
                                }
                            }
                        },
                        canShareAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                        onShareSelected = {
                            selectedMessage?.let { msg ->
                                val mediaUrl = msg.imageUrl ?: msg.videoUrl
                                mediaUrl?.let { url ->
                                    shareMedia(context, coroutineScope, url, sharePreparingText, shareWithText, shareFailedText)
                                }
                            }
                            groupChatViewModel.clearMessageSelection()
                        },
                        canSaveAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                        onSaveSelected = {
                            selectedMessage?.let { msg ->
                                val mediaUrl = msg.imageUrl ?: msg.videoUrl
                                mediaUrl?.let { url ->
                                    try {
                                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        val cleanFileName = try {
                                            URLDecoder.decode(url.substringBefore("?").substringAfterLast("/"), "UTF-8")
                                        } catch (_: Exception) {
                                            "anoba_group_media_${System.currentTimeMillis()}"
                                        }

                                        val request =
                                            DownloadManager.Request(url.toUri())
                                                .setTitle(downloadingFileText)
                                                .setDescription(cleanFileName)
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName)
                                        downloadManager.enqueue(request)
                                        Toast.makeText(context, downloadStartedText, Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, downloadFailedText, Toast.LENGTH_SHORT).show()
                                        Log.e("GroupChatScreen", "Download error", e)
                                    }
                                }
                            }
                            groupChatViewModel.clearMessageSelection()
                        },
                        canReply = selectedMessages.size == 1,
                        onReplySelected = {
                            selectedMessage?.let { groupChatViewModel.setReplyingToMessage(it) }
                            groupChatViewModel.clearMessageSelection()
                            focusRequester.requestFocus()
                        }
                    )
                } else if (isSearchModeActive) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        TopAppBar(
                            title = {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(stringResource(R.string.chat_search_group_placeholder)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            isSearchModeActive = false
                                        }) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close)) }
                                    }
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchModeActive = false
                                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back)) }
                            }
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.90f),
                        shadowElevation = 4.dp
                    ) {
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("${Constants.GROUP_INFO_SCREEN}/$groupId") }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!activeGroup?.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = activeGroup?.imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            InitialsAvatar(
                                                name = activeGroup?.name ?: "",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = activeGroup?.name ?: stringResource(id = R.string.groupchatscreen_title_default),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AnobaNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    groupChatViewModel.setActiveGroupId(null)
                                    navController.popBackStack()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.groupchatscreen_back), tint = AnobaNavy)
                                }
                            },
                            actions = {
                                IconButton(onClick = { isSearchModeActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.userselection_search_icon), tint = AnobaNavy)
                                }
                                IconButton(onClick = {
                                    groupChatViewModel.setActiveGroupId(null)
                                    navController.navigate(Constants.HOME_SCREEN) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }) {
                                    Icon(Icons.Filled.Home, contentDescription = stringResource(id = R.string.groupchatscreen_home), tint = AnobaNavy)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                }
            },
            bottomBar = {
                if (!isSelectionModeActive) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Column {
                            if (isEditMode) {
                                EditMessageIndicator(message = messageToEdit, onCancel = {
                                    messageToEdit = null
                                    groupChatViewModel.updateMessageText("")
                                })
                            }

                            ChatInput(
                                value = messageText,
                                onValueChange = { groupChatViewModel.updateMessageText(it) },
                                onSendClick = {
                                    val textToSend = messageText.trim()
                                    if (textToSend.isNotEmpty()) {
                                        val msgToEdit = messageToEdit
                                        if (msgToEdit != null) {
                                            groupChatViewModel.editMessage(msgToEdit.id, textToSend)
                                            messageToEdit = null
                                        } else {
                                            groupChatViewModel.onMessageSent()
                                            groupChatViewModel.addMessageToCurrentGroup(textToSend, null)
                                        }
                                    }
                                },
                                onAttachmentClick = { showBottomSheet = true },
                                onAudioRecorded = { audioFile, duration ->
                                    groupChatViewModel.sendAudioMessage(groupId, audioFile, duration, null)
                                },
                                focusRequester = focusRequester,
                                bringIntoViewRequester = bringIntoViewRequester,
                                replyingTo = replyingToMessage,
                                onCancelReply = { groupChatViewModel.clearReplyingToMessage() }
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (pagingItems.loadState.refresh is LoadState.Loading) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.state_no_data), color = Color.Gray)
                                }
                            }
                        }

                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.id },
                            contentType = pagingItems.itemContentType { "group_message" }
                        ) { index ->
                            if (index >= pagingItems.itemCount) return@items

                            val message = pagingItems[index]
                            if (message != null) {
                                val nextMessage = if (index < pagingItems.itemCount - 1) pagingItems.peek(index + 1) else null
                                val isFirstMessageOfDay = nextMessage == null || !isSameDay(nextMessage.timestampMillis, message.timestampMillis)

                                Column {
                                    val dateHeader = getFormattedDate(message.timestampMillis)
                                    if (isFirstMessageOfDay && dateHeader != null) {
                                        GroupDateHeader(dateHeader)
                                    }
                                    ChatMessageItem(
                                        message = message,
                                        isSentByCurrentUser = message.senderId == currentUserId,
                                        isSelected = selectedMessages.contains(message),
                                        onLongPress = { if (!isEditMode) groupChatViewModel.onMessageLongClicked(it) },
                                        onClick = { msg ->
                                            if (isSelectionModeActive) {
                                                groupChatViewModel.onMessageClicked(msg)
                                            } else {
                                                if (!msg.videoUrl.isNullOrEmpty()) {
                                                    videoToPlayFullScreen = msg.videoUrl
                                                } else if (!msg.imageUrl.isNullOrEmpty()) {
                                                    imageToShowFullScreen = msg.imageUrl
                                                }
                                            }
                                        },
                                        groupMemberCount = activeGroup?.members?.size ?: 0
                                    )
                                }
                            }
                        }

                        when (val appendState = pagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.state_loading_more), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(stringResource(R.string.state_load_error), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                        TextButton(onClick = { pagingItems.retry() }) {
                                            Text(stringResource(R.string.state_retry), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            is LoadState.NotLoading -> {
                                if (appendState.endOfPaginationReached.not()) {
                                    item {
                                        LaunchedEffect(pagingItems.itemCount) {
                                            val lastItem = if (pagingItems.itemCount > 0) pagingItems.peek(pagingItems.itemCount - 1) else null
                                            groupChatViewModel.loadMoreHistory(lastItem?.timestampMillis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = typingUsers.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val typingText = when (typingUsers.size) {
                        1 -> stringResource(id = R.string.groupchatscreen_typing_one, typingUsers.first())
                        2 -> stringResource(id = R.string.groupchatscreen_typing_two, typingUsers[0], typingUsers[1])
                        else -> stringResource(id = R.string.groupchatscreen_typing_many, typingUsers.take(2).joinToString())
                    }
                    Text(
                        text = typingText,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF232066))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.chathome_uploading_media),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF232066)
                        )
                    }
                }
            }
        }

        if (videoToPlayFullScreen != null) {
            FullScreenVideoViewer(
                videoUrl = videoToPlayFullScreen!!,
                onClose = { videoToPlayFullScreen = null }
            )
        }
        if (imageToShowFullScreen != null) {
            FullScreenImageViewer(
                imageUrl = imageToShowFullScreen!!,
                onClose = { imageToShowFullScreen = null }
            )
        }
    }
}

@Composable
fun EditMessageIndicator(
    message: Messageable?,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = stringResource(id = R.string.groupchatscreen_edit_mode),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.groupchatscreen_edit_message_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = message?.content ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.groupchatscreen_cancel_edit))
        }
    }
}

@Composable
fun GroupDateHeader(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            tonalElevation = 0.5.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}