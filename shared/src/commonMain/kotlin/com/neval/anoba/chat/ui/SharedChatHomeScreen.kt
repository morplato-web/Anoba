@file:kotlin.jvm.JvmName("SharedChatHomeScreenKt")

package com.neval.anoba.chat.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String? = null,
    val senderBadgeType: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val timestampMillis: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHomeScreen(
    messages: List<SharedChatMessage> = emptyList(),
    currentUserId: String = "me",
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onOpenGroupsClick: () -> Unit = {},
    onOpenPrivateChatClick: () -> Unit = {},
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
                                placeholder = { Text("Mesajlarda ara...", color = Color.Gray, fontSize = 14.sp) },
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
                    shadowElevation = 6.dp,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    TopAppBar(
                        modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp),
                        title = {
                            Surface(
                                color = Color(0xFF1565C0),
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = AnobaNavy
                                )
                            }
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                IconButton(onClick = { isSearchActive = true }, modifier = Modifier.size(38.dp)) {
                                    Icon(Icons.Default.Search, contentDescription = "Ara", modifier = Modifier.size(24.dp), tint = AnobaNavy)
                                }
                                IconButton(onClick = onOpenGroupsClick, modifier = Modifier.size(38.dp)) {
                                    Icon(Icons.Filled.Groups, contentDescription = "Gruplar", modifier = Modifier.size(24.dp), tint = AnobaNavy)
                                }
                                IconButton(onClick = onOpenPrivateChatClick, modifier = Modifier.size(38.dp)) {
                                    Icon(Icons.Default.Person, contentDescription = "Özel", modifier = Modifier.size(24.dp), tint = AnobaNavy)
                                }
                                IconButton(onClick = onHomeClick, modifier = Modifier.size(38.dp)) {
                                    Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", modifier = Modifier.size(24.dp), tint = AnobaNavy)
                                }
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
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Medya Ekle",
                            tint = AnobaNavy
                        )
                    }

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Bir mesaj yazın...", color = Color.Gray, fontSize = 14.sp) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && filteredMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AnobaNavy)
                }
            } else if (filteredMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz mesaj bulunmuyor. İlk mesajı siz yazın! 🕊️", color = Color.Gray, fontSize = 14.sp)
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
                        ChatBubbleItem(
                            message = msg,
                            isSentByMe = msg.senderId == currentUserId,
                            onClick = { onMessageClick(msg) },
                            onLongClick = { onMessageLongClick(msg) }
                        )
                    }
                }
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
                Text(
                    text = "Medya Paylaş 📎",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AnobaNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOptionItem(icon = Icons.Default.PhotoLibrary, title = "Galeri", color = Color(0xFFE11D48)) {
                        showAttachmentSheet = false
                        onSendMediaClick("GALLERY")
                    }
                    AttachmentOptionItem(icon = Icons.Default.CameraAlt, title = "Fotoğraf Çek", color = Color(0xFF2563EB)) {
                        showAttachmentSheet = false
                        onSendMediaClick("CAMERA")
                    }
                    AttachmentOptionItem(icon = Icons.Default.Videocam, title = "Video Çek", color = Color(0xFF059669)) {
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
private fun AttachmentOptionItem(
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
private fun ChatBubbleItem(
    message: SharedChatMessage,
    isSentByMe: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            color = if (isSentByMe) AnobaNavy else Color.White,
            shadowElevation = 1.5.dp,
            modifier = Modifier.fillMaxWidth(0.78f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isSentByMe) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                message.content?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = if (isSentByMe) Color.White else Color(0xFF1E293B),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
