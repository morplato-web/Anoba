package com.neval.anoba.chat.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.neval.anoba.R
import com.neval.anoba.chat.ui.ChatInput
import com.neval.anoba.chat.ui.ChatMessageItem
import com.neval.anoba.chat.ui.EditMessageIndicator
import com.neval.anoba.chat.ui.FullScreenImageViewer
import com.neval.anoba.chat.ui.FullScreenVideoViewer
import com.neval.anoba.chat.ui.SelectionModeTopAppBar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.getFormattedDate
import com.neval.anoba.common.utils.isSameDay
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ChatHomeScreen(
    navController: NavHostController,
    chatViewModel: GeneralChatViewModel,
    authViewModel: AuthViewModel
) {
    val pagingItems = chatViewModel.pagingMessages.collectAsLazyPagingItems()
    val selectedMessages by chatViewModel.selectedMessages.collectAsState()
    val isSelectionModeActive by chatViewModel.isSelectionModeActive.collectAsState()
    val currentUserId = chatViewModel.currentUserId
    val isUploading by chatViewModel.isUploading.collectAsState()

    val authState by authViewModel.authState.collectAsState()
    val authenticatedUser = authState as? AuthState.Authenticated
    val userPhotoUrl = authenticatedUser?.photoUrl
    val userBadgeType = authenticatedUser?.badgeType
    val currentUserEmail = authenticatedUser?.email
    val isAdmin = currentUserEmail == "morplato@gmail.com" ||
            authenticatedUser?.displayName?.equals("anoba", ignoreCase = true) == true ||
            authenticatedUser?.displayName?.equals("Anoba", ignoreCase = true) == true

    var inputMessage by remember { mutableStateOf("") }
    var messageToEdit by remember { mutableStateOf<GeneralChatMessage?>(null) }
    val isEditMode = messageToEdit != null

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val backText = stringResource(id = R.string.chathome_back)
    val homeText = stringResource(id = R.string.chathome_home)
    val galleryText = stringResource(id = R.string.chathome_gallery)
    val cameraText = stringResource(id = R.string.chathome_photo)
    val videoText = stringResource(id = R.string.chathome_video)
    val uploadMediaText = stringResource(id = R.string.chathome_uploading_media)

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    var videoToPlayFullScreen by remember { mutableStateOf<String?>(null) }
    var imageToShowFullScreen by remember { mutableStateOf<String?>(null) }
    var isSearchModeActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            if (mimeType?.startsWith("video/") == true) {
                chatViewModel.sendVideoMessage("GeneralChat", it, inputMessage, userPhotoUrl, userBadgeType)
            } else {
                chatViewModel.sendImageMessage("GeneralChat", it, inputMessage, userPhotoUrl, userBadgeType)
            }
            inputMessage = ""
            showBottomSheet = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let {
                chatViewModel.sendImageMessage("GeneralChat", it, inputMessage, userPhotoUrl, userBadgeType)
                inputMessage = ""
                showBottomSheet = false
            }
        }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            videoUri?.let {
                chatViewModel.sendVideoMessage("GeneralChat", it, inputMessage, userPhotoUrl, userBadgeType)
                inputMessage = ""
                showBottomSheet = false
            }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.soft_gray1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = {
                if (isSelectionModeActive) {
                    val selectedMessage = if (selectedMessages.size == 1) selectedMessages.first() else null
                    SelectionModeTopAppBar(
                        selectedCount = selectedMessages.size,
                        onCloseSelectionMode = { chatViewModel.clearMessageSelection() },
                        canDeleteAnySelected = isAdmin || selectedMessages.any { it.senderId == currentUserId },
                        onDeleteSelected = { chatViewModel.deleteSelectedMessages("GeneralChat") },
                        canCopyAnySelected = selectedMessages.any { !it.content.isNullOrBlank() },
                        onCopySelected = { chatViewModel.copySelectedMessagesToClipboard(context) },
                        canEdit = selectedMessages.size == 1 && selectedMessages.first().senderId == currentUserId && !selectedMessages.first().content.isNullOrBlank(),
                        onEditSelected = {
                            val msg = selectedMessages.firstOrNull()
                            if (msg != null) {
                                messageToEdit = msg
                                inputMessage = msg.content ?: ""
                                chatViewModel.clearMessageSelection()
                            }
                        },
                        canShareAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                        onShareSelected = {
                            selectedMessage?.let { msg ->
                                val mediaUrl = msg.imageUrl ?: msg.videoUrl
                                mediaUrl?.let { url ->
                                    shareMedia(context, coroutineScope, url)
                                }
                            }
                        },
                        canSaveAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                        onSaveSelected = { }
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
                                    onValueChange = {
                                        searchQuery = it
                                        chatViewModel.updateSearchQuery(it)
                                    },
                                    placeholder = { Text(stringResource(R.string.chat_search_placeholder), color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = AnobaNavy
                                    ),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            isSearchModeActive = false
                                            chatViewModel.updateSearchQuery("")
                                        }) { Icon(Icons.Default.Close, contentDescription = "Aramayı Kapat", tint = AnobaNavy) }
                                    }
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchModeActive = false
                                    chatViewModel.updateSearchQuery("")
                                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy) }
                            },
                            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.90f),
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ) {
                        TopAppBar(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            title = {
                                Surface(
                                    onClick = { },
                                    color = Color(0xFF1565C0),
                                    shape = RoundedCornerShape(10.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Polis\nKarakolu",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            lineHeight = 11.sp,
                                            textAlign = TextAlign.Center,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (isEditMode) {
                                        messageToEdit = null
                                        inputMessage = ""
                                    } else {
                                        navController.popBackStack()
                                    }
                                }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = backText,
                                        tint = AnobaNavy
                                    )
                                }
                            },
                            actions = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 2.dp)
                                ) {
                                    IconButton(onClick = { isSearchModeActive = true }, modifier = Modifier.size(40.dp)) {
                                        Icon(Icons.Default.Search, contentDescription = "Ara", modifier = Modifier.size(26.dp), tint = AnobaNavy)
                                    }
                                    IconButton(onClick = { navController.navigate(Constants.GROUP_CHAT_LIST_SCREEN) }, modifier = Modifier.size(40.dp)) {
                                        Icon(Icons.Filled.Groups, contentDescription = "Gruplar", modifier = Modifier.size(26.dp), tint = AnobaNavy)
                                    }
                                    IconButton(onClick = { navController.navigate(Constants.PRIVATE_CHAT_SCREEN) }, modifier = Modifier.size(40.dp)) {
                                        Icon(Icons.Default.Person, contentDescription = "Özel", modifier = Modifier.size(26.dp), tint = AnobaNavy)
                                    }
                                    IconButton(onClick = { navController.navigate(Constants.HOME_SCREEN) }, modifier = Modifier.size(40.dp)) {
                                        Icon(Icons.Filled.Home, contentDescription = homeText, modifier = Modifier.size(26.dp), tint = AnobaNavy)
                                    }
                                }
                            },
                            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            bottomBar = {
                if (!isSelectionModeActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        if (isEditMode) {
                            EditMessageIndicator(
                                message = messageToEdit,
                                onCancel = {
                                    messageToEdit = null
                                    inputMessage = ""
                                }
                            )
                        }
                        ChatInput(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            onSendClick = {
                                if (isEditMode) {
                                    messageToEdit?.let { chatViewModel.editMessage("GeneralChat", it.id, inputMessage) }
                                    messageToEdit = null
                                } else {
                                    chatViewModel.sendMessage("GeneralChat", inputMessage, userPhotoUrl, userBadgeType)
                                }
                                inputMessage = ""
                            },
                            onAttachmentClick = { showBottomSheet = true },
                            onAudioRecorded = { file, duration ->
                                chatViewModel.sendAudioMessage("GeneralChat", file, duration, userPhotoUrl, userBadgeType)
                            },
                            focusRequester = focusRequester,
                            bringIntoViewRequester = bringIntoViewRequester
                        )
                    }
                }
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.state_no_data), color = Color.Gray)
                            }
                        }
                    }

                    if (pagingItems.loadState.refresh is LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { it.id },
                        contentType = pagingItems.itemContentType { "chat_message" }
                    ) { index ->
                        if (index >= pagingItems.itemCount) return@items

                        val message = pagingItems[index]
                        if (message != null) {
                            val nextMessage = if (index < pagingItems.itemCount - 1) pagingItems.peek(index + 1) else null
                            val isFirstMessageOfDay = nextMessage == null || !isSameDay(nextMessage.timestampMillis, message.timestampMillis)

                            Column {
                                if (isFirstMessageOfDay) {
                                    getFormattedDate(message.timestampMillis)?.let { GeneralDateHeader(dateText = it) }
                                }

                                ChatMessageItem(
                                    message = message,
                                    isSentByCurrentUser = message.senderId == currentUserId,
                                    isSelected = selectedMessages.contains(message),
                                    onLongPress = { if (!isEditMode) chatViewModel.onMessageLongClicked(it) },
                                    onClick = { msg ->
                                        if (isSelectionModeActive) {
                                            chatViewModel.onMessageClicked(msg)
                                        } else {
                                            if (!msg.videoUrl.isNullOrEmpty()) {
                                                videoToPlayFullScreen = msg.videoUrl
                                            } else if (!msg.imageUrl.isNullOrEmpty()) {
                                                imageToShowFullScreen = msg.imageUrl
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    when (pagingItems.loadState.append) {
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
                        else -> {}
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                ListItem(headlineContent = { Text(galleryText) }, leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) })
                ListItem(headlineContent = { Text(cameraText) }, leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable { photoUri = createMediaUri(context, "jpg"); photoUri?.let { cameraLauncher.launch(it) } })
                ListItem(headlineContent = { Text(videoText) }, leadingContent = { Icon(Icons.Default.Videocam, null) },
                    modifier = Modifier.clickable { videoUri = createMediaUri(context, "mp4"); videoUri?.let { videoLauncher.launch(it) } })
            }
        }

        if (isUploading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AnobaNavy)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uploadMediaText, fontWeight = FontWeight.Bold, color = AnobaNavy)
                    }
                }
            }
        }

        if (videoToPlayFullScreen != null) { FullScreenVideoViewer(videoUrl = videoToPlayFullScreen!!, onClose = { videoToPlayFullScreen = null }) }
        if (imageToShowFullScreen != null) { FullScreenImageViewer(imageUrl = imageToShowFullScreen!!, onClose = { imageToShowFullScreen = null }) }
    }
}

@Composable
fun GeneralDateHeader(dateText: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp)) {
            Text(text = dateText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

internal suspend fun downloadAndGetUri(context: Context, url: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val extension = if (url.contains("chat_videos")) ".mp4" else ".jpg"
            val file = File.createTempFile("shared_media_", extension, context.cacheDir)
            URL(url).openStream().use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (_: Exception) { null }
    }
}

internal fun shareMedia(context: Context, scope: CoroutineScope, mediaUrl: String) {
    scope.launch {
        val localFileUri = downloadAndGetUri(context, mediaUrl)
        withContext(Dispatchers.Main) {
            if (localFileUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(localFileUri)
                    putExtra(Intent.EXTRA_STREAM, localFileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Paylaş"))
            }
        }
    }
}

internal fun createMediaUri(context: Context, extension: String): Uri? {
    return try {
        val file = File.createTempFile("media_", ".$extension", context.externalCacheDir)
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (_: Exception) { null }
}