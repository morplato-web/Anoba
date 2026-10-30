@file:kotlin.jvm.JvmName("SharedGroupChatScreenKt")

package com.neval.anoba.chat.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.chat.general.SharedChatMessage
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedGroupChatScreen(
    group: SharedGroupItem?,
    messages: List<SharedChatMessage> = emptyList(),
    currentUserId: String = "me",
    isLoading: Boolean = false,
    typingUsers: List<String> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onGroupInfoClick: (String) -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onSendMediaClick: (mediaType: String) -> Unit = {},
    onRecordAudioClick: () -> Unit = {},
    onMessageClick: (SharedChatMessage) -> Unit = {},
    onMessageLongClick: (SharedChatMessage) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var inputMessage by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            val q = searchQuery.trim().lowercase()
            messages.filter {
                it.content?.lowercase()?.contains(q) == true || it.senderName.lowercase().contains(q)
            }
        }
    }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            if (isSearchActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Grup mesajlarında ara...", color = Color.Gray, fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = AnobaNavy
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isSearchActive = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AnobaNavy)
                                    }
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { group?.id?.let { onGroupInfoClick(it) } }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!group?.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = group.imageUrl,
                                        contentDescription = group.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(AnobaGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = AnobaNavy,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = group?.name ?: "Grup Sohbeti",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AnobaNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (group != null && group.memberCount > 0) {
                                        Text(
                                            text = "${group.memberCount} üye",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Ara", tint = AnobaNavy)
                            }
                            IconButton(onClick = onHomeClick) {
                                Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Medya Ekle", tint = AnobaNavy)
                    }

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Gruba mesaj yazın...", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = AnobaNavy,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = AnobaNavy,
                            unfocusedTextColor = AnobaNavy
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (inputMessage.isNotBlank()) {
                        FloatingActionButton(
                            onClick = {
                                onSendMessage(inputMessage.trim())
                                inputMessage = ""
                            },
                            containerColor = AnobaNavy,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier.size(18.dp))
                        }
                    } else {
                        IconButton(
                            onClick = onRecordAudioClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Ses Kaydı", tint = AnobaNavy)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading && filteredMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AnobaNavy)
                    }
                } else if (filteredMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu grupta henüz mesaj bulunmuyor. İlk mesajı siz yazın! 💬", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredMessages, key = { _, item -> item.id }) { _, msg ->
                            GroupChatBubbleItem(
                                message = msg,
                                isSentByMe = msg.senderId == currentUserId,
                                onClick = { onMessageClick(msg) },
                                onLongClick = { onMessageLongClick(msg) }
                            )
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
                    1 -> "${typingUsers.first()} yazıyor..."
                    2 -> "${typingUsers[0]} ve ${typingUsers[1]} yazıyor..."
                    else -> "${typingUsers.take(2).joinToString(", ")} ve diğerleri yazıyor..."
                }
                Text(
                    text = typingText,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }

    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Medya Paylaş 📎", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AnobaNavy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GroupAttachmentOption(icon = Icons.Default.PhotoLibrary, title = "Galeri", color = Color(0xFFE11D48)) {
                        showAttachmentSheet = false
                        onSendMediaClick("GALLERY")
                    }
                    GroupAttachmentOption(icon = Icons.Default.CameraAlt, title = "Fotoğraf Çek", color = Color(0xFF2563EB)) {
                        showAttachmentSheet = false
                        onSendMediaClick("CAMERA")
                    }
                    GroupAttachmentOption(icon = Icons.Default.Videocam, title = "Video Çek", color = Color(0xFF059669)) {
                        showAttachmentSheet = false
                        onSendMediaClick("VIDEO")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GroupAttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 12.sp, color = AnobaNavy, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupChatBubbleItem(
    message: SharedChatMessage,
    isSentByMe: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isSentByMe) {
            UserAvatar(
                photoUrl = message.senderPhotoUrl,
                username = message.senderName,
                size = 32.dp,
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp,
            ),
            color = if (isSentByMe) AnobaNavy else Color.White,
            shadowElevation = 1.5.dp,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSentByMe) 16.dp else 4.dp,
                        bottomEnd = if (isSentByMe) 4.dp else 16.dp,
                    ),
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (!isSentByMe) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (!message.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Fotoğraf",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (!message.videoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (!message.audioUrl.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSentByMe) Color.White.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = if (isSentByMe) Color.White else AnobaNavy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sesli Mesaj", fontSize = 13.sp, color = if (isSentByMe) Color.White else AnobaNavy, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (!message.content.isNullOrBlank()) {
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = if (isSentByMe) Color.White else Color(0xFF1E293B),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
