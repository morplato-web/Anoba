package com.neval.anoba.chat.privatechat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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
import com.neval.anoba.chat.ui.EditMessageIndicator
import com.neval.anoba.chat.ui.FullScreenImageViewer
import com.neval.anoba.chat.ui.FullScreenVideoViewer
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.chat.ui.SelectionModeTopAppBar
import com.neval.anoba.common.ui.OfficialBadge
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.getFormattedDate
import com.neval.anoba.common.utils.isSameDay
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivateChatScreen(
    navController: NavHostController,
    privateChatViewModel: PrivateChatViewModel,
    initialTargetUserId: String? = null,
    authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val pagingItems = privateChatViewModel.pagingMessages.collectAsLazyPagingItems()

    val selectedMessages by privateChatViewModel.selectedMessages.collectAsStateWithLifecycle()
    val replyingToMessage by privateChatViewModel.replyingToMessage.collectAsStateWithLifecycle()
    val isSelectionModeActive by privateChatViewModel.isSelectionModeActive.collectAsStateWithLifecycle()
    val currentUserId = privateChatViewModel.currentUserId
    val editingMessage by privateChatViewModel.editingMessage.collectAsStateWithLifecycle()
    val messageText by privateChatViewModel.messageText.collectAsStateWithLifecycle()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authenticatedUser = authState as? com.neval.anoba.login.state.AuthState.Authenticated
    val currentUserEmail = authenticatedUser?.email
    val isAdmin = currentUserEmail == "morplato@gmail.com" ||
            authenticatedUser?.displayName?.equals("anoba", ignoreCase = true) == true ||
            authenticatedUser?.displayName?.equals("Anoba", ignoreCase = true) == true

    val isUploading by privateChatViewModel.isUploading.collectAsStateWithLifecycle()
    val allUsers by privateChatViewModel.userList.collectAsStateWithLifecycle()
    val searchQueryByViewModel by privateChatViewModel.searchQuery.collectAsStateWithLifecycle()
    val chatPartnerProfile by privateChatViewModel.chatPartnerProfile.collectAsStateWithLifecycle()
    val isPartnerTyping by privateChatViewModel.isPartnerTyping.collectAsStateWithLifecycle()
    val chatRoomStatus by privateChatViewModel.chatRoomStatus.collectAsStateWithLifecycle()
    val requestedBy by privateChatViewModel.requestedBy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val undoText = stringResource(id = R.string.chathome_undo)

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val sharePreparingStr = stringResource(R.string.chathome_share_preparing)
    val shareWithStr = stringResource(R.string.chathome_share_with)
    val shareFailedStr = stringResource(R.string.chathome_share_failed)

    var selectedUserForChat by remember { mutableStateOf<com.neval.anoba.chat.general.GeneralChatUser?>(null) }

    LaunchedEffect(initialTargetUserId) {
        if (!initialTargetUserId.isNullOrBlank()) {
            privateChatViewModel.fetchUserAndSetAsPartner(initialTargetUserId) { user ->
                selectedUserForChat = user
            }
        }
    }

    LaunchedEffect(snackbarHostState) {
        privateChatViewModel.undoState.collectLatest { lastDeletedMessages ->
            if (lastDeletedMessages.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.chathome_messages_deleted_snackbar, lastDeletedMessages.size),
                    actionLabel = undoText,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    selectedUserForChat?.id?.let { privateChatViewModel.undoDelete(it) }
                } else {
                    privateChatViewModel.clearUndoState()
                }
            }
        }
    }

    var showBlockDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoToPlayFullScreen by remember { mutableStateOf<String?>(null) }
    var imageToShowFullScreen by remember { mutableStateOf<String?>(null) }
    var isSearchModeActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .filter { it.isNotEmpty() }
            .collect { visibleItems ->
                val itemCount = pagingItems.itemCount
                val unreadMessageIds = visibleItems
                    .filter { it.index in 0 until itemCount }
                    .mapNotNull { pagingItems.peek(it.index) }
                    .filter { it.senderId != currentUserId && it.status != "READ" }
                    .map { it.id }

                if (unreadMessageIds.isNotEmpty()) {
                    privateChatViewModel.markMessagesAsRead(unreadMessageIds)
                }
            }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            selectedUserForChat?.let { user ->
                val mimeType = context.contentResolver.getType(it)
                if (mimeType?.startsWith("video/") == true) {
                    privateChatViewModel.sendVideoMessage(user.id, it, messageText)
                } else {
                    privateChatViewModel.sendImageMessage(user.id, it, messageText)
                }
                privateChatViewModel.updateMessageText("")
                showBottomSheet = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                selectedUserForChat?.let { user ->
                    privateChatViewModel.sendImageMessage(user.id, it, messageText)
                    privateChatViewModel.updateMessageText("")
                    showBottomSheet = false
                }
            }
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            videoUri?.let {
                selectedUserForChat?.let { user ->
                    privateChatViewModel.sendVideoMessage(user.id, it, messageText)
                    privateChatViewModel.updateMessageText("")
                    showBottomSheet = false
                }
            }
        }
    }

    val editableMessage by remember(selectedMessages, currentUserId) {
        derivedStateOf {
            if (selectedMessages.size == 1 && selectedMessages.first().senderId == currentUserId &&
                selectedMessages.first().videoUrl == null && selectedMessages.first().imageUrl == null) {
                selectedMessages.first()
            } else null
        }
    }

    LaunchedEffect(selectedUserForChat?.id) {
        selectedUserForChat?.id?.let { privateChatViewModel.listenForMessages(it) }
    }

    LaunchedEffect(pagingItems.itemCount) {
        if (pagingItems.itemCount > 0) {
            val isAtBottom = listState.firstVisibleItemIndex <= 1
            if (isAtBottom) {
                listState.animateScrollToItem(0)
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            isSelectionModeActive -> privateChatViewModel.clearMessageSelection()
            selectedUserForChat != null -> {
                selectedUserForChat = null
                privateChatViewModel.resetChatState()
            }
            else -> navController.popBackStack()
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_gallery)) },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                modifier = Modifier.clickable {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_photo)) },
                leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                modifier = Modifier.clickable {
                    photoUri = createMediaUri(context, "jpg")
                    photoUri?.let { cameraLauncher.launch(it) }
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.chathome_video)) },
                leadingContent = { Icon(Icons.Default.Videocam, null) },
                modifier = Modifier.clickable {
                    videoUri = createMediaUri(context, "mp4")
                    videoUri?.let { videoLauncher.launch(it) }
                }
            )
        }
    }

    if (selectedUserForChat == null) {
        UserSelectionScreen(
            users = allUsers,
            onUserSelected = { selectedUserForChat = it },
            onDismiss = { navController.popBackStack() },
            onSearchQueryChanged = { privateChatViewModel.onSearchQueryChanged(it) },
            searchQuery = searchQueryByViewModel,
            navController = navController
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = R.drawable.soft_gray1), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (isSelectionModeActive) {
                        val selectedMessage = if (selectedMessages.size == 1) selectedMessages.first() else null
                        SelectionModeTopAppBar(
                            selectedCount = selectedMessages.size,
                            onCloseSelectionMode = { privateChatViewModel.clearMessageSelection() },
                            canDeleteAnySelected = isAdmin || selectedMessages.any { it.senderId == currentUserId },
                            onDeleteSelected = {
                                selectedUserForChat?.id?.let { privateChatViewModel.deleteSelectedMessages(it) }
                            },
                            canCopyAnySelected = selectedMessages.any { !it.content.isNullOrBlank() },
                            onCopySelected = { privateChatViewModel.copySelectedMessagesToClipboard(context, null) },
                            canEdit = editableMessage != null,
                            onEditSelected = {
                                editableMessage?.let { privateChatViewModel.startEditingMessage(it) }
                                privateChatViewModel.clearMessageSelection()
                            },
                            canShareAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                            onShareSelected = {
                                selectedMessage?.let { msg ->
                                    (msg.imageUrl ?: msg.videoUrl)?.let { url ->
                                        shareMedia(context, coroutineScope, url, sharePreparingStr, shareWithStr, shareFailedStr)
                                    }
                                }
                            },
                            canSaveAnySelected = selectedMessage?.let { it.imageUrl != null || it.videoUrl != null } ?: false,
                            onSaveSelected = { /* Kaydetme mantığı eklenecek */ },
                            canReply = selectedMessages.size == 1,
                            onReplySelected = {
                                selectedMessage?.let { privateChatViewModel.setReplyingToMessage(it) }
                                privateChatViewModel.clearMessageSelection()
                                focusRequester.requestFocus()
                            }
                        )
                    } else if (isSearchModeActive) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.95f), shadowElevation = 4.dp) {
                            TopAppBar(
                                title = {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text(stringResource(R.string.chat_search_placeholder)) },
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
                                            }) {
                                                Icon(Icons.Default.Close, null)
                                            }
                                        }
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isSearchModeActive = false
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                    }
                                }
                            )
                        }
                    } else {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.90f), shadowElevation = 2.dp) {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = chatPartnerProfile?.safeDisplayName
                                                ?: selectedUserForChat?.safeDisplayName
                                                ?: stringResource(id = R.string.privatechat_title),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AnobaNavy
                                        )
                                        if (isPartnerTyping) {
                                            Text(
                                                text = stringResource(id = R.string.chat_typing),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        selectedUserForChat = null
                                        privateChatViewModel.resetChatState()
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AnobaNavy)
                                    }
                                },
                                actions = {
                                    val currentId = privateChatViewModel.currentUserId ?: ""
                                    val partnerId = selectedUserForChat?.id ?: ""
                                    val isAccepted = chatRoomStatus == "ACCEPTED" || chatRoomStatus == null
                                    if (currentId.isNotEmpty() && partnerId.isNotEmpty()) {
                                        val channelName = if (currentId < partnerId) "${currentId}_${partnerId}" else "${partnerId}_${currentId}"
                                        IconButton(onClick = { if (isAccepted) navController.navigate("call_screen/$channelName/false/false") }, enabled = isAccepted) {
                                            Icon(Icons.Default.Call, null, modifier = Modifier.size(24.dp), tint = if (isAccepted) AnobaNavy else Color.Gray.copy(alpha = 0.4f))
                                        }
                                        IconButton(onClick = { if (isAccepted) navController.navigate("call_screen/$channelName/true/false") }, enabled = isAccepted) {
                                            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(26.dp), tint = if (isAccepted) AnobaNavy else Color.Gray.copy(alpha = 0.4f))
                                        }
                                    }
                                    var showMenu by remember { mutableStateOf(false) }
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, null, tint = AnobaNavy)
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.chat_block_user)) },
                                            onClick = {
                                                showMenu = false
                                                showBlockDialog = true
                                            },
                                            leadingIcon = { Icon(Icons.Default.Block, null) }
                                        )
                                    }
                                    IconButton(onClick = { navController.navigate(Constants.HOME_SCREEN) }) {
                                        Icon(Icons.Filled.Home, null, tint = AnobaNavy)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!isSelectionModeActive) {
                        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()) {
                            val isPending = chatRoomStatus == "PENDING"
                            val isReceiver = requestedBy != null && requestedBy != currentUserId
                            if (isPending && isReceiver) {
                                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.9f), tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = stringResource(id = R.string.chat_request_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AnobaNavy)
                                        Spacer(Modifier.height(8.dp))
                                        Text(text = stringResource(id = R.string.chat_request_desc, chatPartnerProfile?.safeDisplayName ?: selectedUserForChat?.safeDisplayName ?: ""), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color.Gray)
                                        Spacer(Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { showBlockDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))) {
                                                Text(stringResource(id = R.string.chat_block_user))
                                            }
                                            Button(onClick = { selectedUserForChat?.id?.let { privateChatViewModel.acceptChatRequest(it) } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy) ) {
                                                Text(stringResource(id = R.string.chat_request_accept), color = Color.White)
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (editingMessage != null) {
                                    EditMessageIndicator(message = editingMessage, onCancel = { privateChatViewModel.cancelEditing() })
                                }
                                ChatInput(
                                    value = messageText,
                                    onValueChange = { privateChatViewModel.updateMessageText(it) },
                                    onSendClick = { selectedUserForChat?.let { user -> privateChatViewModel.sendMessage(user.id, messageText) } },
                                    onAttachmentClick = { showBottomSheet = true },
                                    onAudioRecorded = { audioFile, duration -> selectedUserForChat?.id?.let { privateChatViewModel.sendAudioMessage(it, audioFile, duration) } },
                                    focusRequester = focusRequester,
                                    bringIntoViewRequester = bringIntoViewRequester,
                                    replyingTo = replyingToMessage,
                                    onCancelReply = { privateChatViewModel.clearReplyingToMessage() }
                                )
                            }
                        }
                    }
                },
                containerColor = Color.Transparent,
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState, reverseLayout = true, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (pagingItems.loadState.refresh is LoadState.Loading) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = Color(0xFFFEF5C3), // WhatsApp tarzı uçuk krem/sarı
                                        shape = RoundedCornerShape(10.dp), // Hafif yumuşatılmış köşeler
                                        shadowElevation = 1.dp, // Kağıt gibi durması için çok hafif gölge
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            // Güvenlik/Gizlilik hissi veren minik kilit ikonu
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Güvenli",
                                                tint = Color(0xFF7A6B3A), // Koyu altın/kahve tonu
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(id = R.string.privatechat_no_messages, selectedUserForChat?.safeDisplayName ?: ""),
                                                fontSize = 12.sp, // Standart sistem mesajı küçüklüğü
                                                lineHeight = 16.sp, // Satır arası boşluk (okunabilirlik için)
                                                color = Color(0xFF5A4B2A), // Simsiyah değil, göze batan kontrastı kıran koyu kahve/füme
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(count = pagingItems.itemCount, key = pagingItems.itemKey { it.id }, contentType = pagingItems.itemContentType { "private_message" }) { index ->
                            if (index >= pagingItems.itemCount) return@items
                            val message = pagingItems[index]
                            if (message != null) {
                                val nextMessage = if (index < pagingItems.itemCount - 1) pagingItems.peek(index + 1) else null
                                val isFirstMessageOfDay = nextMessage == null || !isSameDay(nextMessage.timestampMillis, message.timestampMillis)
                                Column {
                                    val dateHeader = getFormattedDate(message.timestampMillis)
                                    if (isFirstMessageOfDay && dateHeader != null) { DateHeader(dateHeader) }
                                    ChatMessageItem(
                                        message = message,
                                        isSentByCurrentUser = message.senderId == currentUserId,
                                        isSelected = selectedMessages.contains(message),
                                        onLongPress = { privateChatViewModel.onMessageLongClicked(it) },
                                        onClick = { msg ->
                                            if (isSelectionModeActive) {
                                                privateChatViewModel.onMessageClicked(msg)
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
                                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.state_loading_more), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

            if (isUploading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF232066))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.chathome_uploading_media), fontWeight = FontWeight.Bold, color = Color(0xFF232066))
                        }
                    }
                }
            }
            if (videoToPlayFullScreen != null) { FullScreenVideoViewer(videoUrl = videoToPlayFullScreen!!, onClose = { videoToPlayFullScreen = null }) }
            if (imageToShowFullScreen != null) { FullScreenImageViewer(imageUrl = imageToShowFullScreen!!, onClose = { imageToShowFullScreen = null }) }

            if (showBlockDialog) {
                AlertDialog(
                    onDismissRequest = { showBlockDialog = false },
                    title = { Text(stringResource(id = R.string.chat_block_dialog_title)) },
                    text = { Text(stringResource(id = R.string.chat_block_dialog_text, chatPartnerProfile?.safeDisplayName ?: selectedUserForChat?.safeDisplayName ?: "")) },
                    confirmButton = {
                        TextButton(onClick = {
                            val targetUid = selectedUserForChat?.id ?: ""
                            if (targetUid.isNotEmpty()) {
                                privateChatViewModel.blockUser(targetUid)
                                Toast.makeText(context, context.getString(R.string.chat_user_blocked_toast), Toast.LENGTH_SHORT).show()
                                showBlockDialog = false
                                selectedUserForChat = null
                                privateChatViewModel.resetChatState()
                            }
                        }) {
                            Text(stringResource(R.string.chat_block_user), color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionScreen(
    users: List<com.neval.anoba.chat.general.GeneralChatUser>,
    onUserSelected: (com.neval.anoba.chat.general.GeneralChatUser) -> Unit,
    onDismiss: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    searchQuery: String,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.soft_gray1), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.90f), shadowElevation = 2.dp) {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.userselection_title), fontWeight = FontWeight.Bold, color = AnobaNavy) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AnobaNavy) } },
                        actions = { IconButton(onClick = { navController.navigate(Constants.HOME_SCREEN) }) { Icon(Icons.Filled.Home, null, tint = AnobaNavy) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text(stringResource(id = R.string.userselection_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = AnobaNavy) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AnobaNavy, cursorColor = AnobaNavy, focusedLabelColor = AnobaNavy, unfocusedContainerColor = Color.White.copy(alpha = 0.5f))
                )

                // MANTIK DEĞİŞİKLİĞİ BURADA
                if (searchQuery.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.chat_search_user_prompt), color = Color.Gray)
                    }
                } else if (users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.chat_user_not_found), color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(count = users.size, key = { index -> users[index].id.takeIf { it.isNotBlank() } ?: "user_$index" }) { index ->
                            val user = users[index]
                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.safeDisplayName, fontWeight = FontWeight.SemiBold, color = if (user.displayName?.equals("anoba", ignoreCase = true) == true) AnobaGold else AnobaNavy)
                                        if (user.displayName?.equals("anoba", ignoreCase = true) == true) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            OfficialBadge(size = 14.dp)
                                        }
                                    }
                                },
                                supportingContent = { Text(stringResource(id = R.string.userselection_start_chat_prompt), style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
                                leadingContent = {
                                    if (!user.photoUrl.isNullOrBlank()) {
                                        AsyncImage(model = user.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(CircleShape))
                                    } else {
                                        InitialsAvatar(name = user.safeDisplayName, modifier = Modifier.size(48.dp).clip(CircleShape))
                                    }
                                },
                                modifier = Modifier.clickable { onUserSelected(user) }.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.wrapContentWidth(), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), tonalElevation = 0.5.dp) {
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), textAlign = TextAlign.Center)
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

internal fun shareMedia(context: Context, scope: CoroutineScope, mediaUrl: String, preparingMessage: String, shareWithTitle: String, failedMessage: String) {
    scope.launch {
        withContext(Dispatchers.Main) { Toast.makeText(context, preparingMessage, Toast.LENGTH_SHORT).show() }
        val localFileUri = downloadAndGetUri(context, mediaUrl)
        withContext(Dispatchers.Main) {
            if (localFileUri != null) {
                val mimeType = context.contentResolver.getType(localFileUri)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, localFileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, shareWithTitle))
            } else {
                Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
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