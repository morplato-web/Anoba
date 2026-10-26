@file:kotlin.jvm.JvmName("SharedPrivateChatScreenKt")

package com.neval.anoba.chat.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedChatPartner(
    val id: String,
    val username: String,
    val photoUrl: String? = null,
    val badgeType: String? = null,
    val isOfficial: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPrivateChatScreen(
    partner: SharedChatPartner?,
    messages: List<SharedChatMessage> = emptyList(),
    userSearchResults: List<SharedChatPartner> = emptyList(),
    currentUserId: String = "me",
    isLoading: Boolean = false,
    isPartnerTyping: Boolean = false,
    isPendingRequest: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSelectPartner: (SharedChatPartner) -> Unit = {},
    onSearchUser: (String) -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onSendMediaClick: (type: String) -> Unit = {},
    onRecordAudioClick: () -> Unit = {},
    onAcceptRequest: () -> Unit = {},
    onBlockUser: (String) -> Unit = {},
    onStartVoiceCall: () -> Unit = {},
    onStartVideoCall: () -> Unit = {},
    onMessageClick: (SharedChatMessage) -> Unit = {},
    onMessageLongClick: (SharedChatMessage) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var inputMessage by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (showBlockDialog && partner != null) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Kullanıcıyı Engelle", fontWeight = FontWeight.Bold, color = AnobaNavy) },
            text = { Text("${partner.username} adlı kullanıcıyı engellemek istediğinizden emin misiniz?", color = Color.DarkGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockDialog = false
                        onBlockUser(partner.id)
                    }
                ) {
                    Text("Engelle", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("İptal", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // 1. KULLANICI SEÇİMİ / ARAMA EKRANI
    if (partner == null) {
        Scaffold(
            containerColor = Color(0xFFF1F5F9),
            topBar = {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        modifier = Modifier.statusBarsPadding(),
                        title = {
                            Text(
                                text = "Özel Sohbet Başlat",
                                fontWeight = FontWeight.Bold,
                                color = AnobaNavy,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                            }
                        },
                        actions = {
                            IconButton(onClick = onHomeClick) {
                                Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchUser(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Kullanıcı adı veya numara ara...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AnobaNavy) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AnobaNavy,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        cursorColor = AnobaNavy
                    ),
                    singleLine = true
                )

                if (searchQuery.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sohbet etmek için bir kullanıcı arayın 🔍", color = Color.Gray, fontSize = 14.sp)
                    }
                } else if (userSearchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Kullanıcı bulunamadı.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(userSearchResults, key = { it.id }) { user ->
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.username,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (user.isOfficial) AnobaGold else AnobaNavy
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        AnobaBadge(badgeType = user.badgeType, isOfficial = user.isOfficial)
                                    }
                                },
                                supportingContent = {
                                    Text("Sohbeti başlatmak için dokunun", fontSize = 12.sp, color = Color.Gray)
                                },
                                leadingContent = {
                                    UserAvatar(photoUrl = user.photoUrl, username = user.username, size = 44.dp)
                                },
                                modifier = Modifier
                                    .clickable { onSelectPartner(user) }
                                    .padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    } else {
        // 2. BİREBİR SOHBET ODASI
        Scaffold(
            containerColor = Color(0xFFF1F5F9),
            topBar = {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        modifier = Modifier.statusBarsPadding(),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(photoUrl = partner.photoUrl, username = partner.username, size = 38.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = partner.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = AnobaNavy,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        AnobaBadge(badgeType = partner.badgeType, isOfficial = partner.isOfficial)
                                    }
                                    if (isPartnerTyping) {
                                        Text(
                                            text = "yazıyor...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold
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
                            IconButton(onClick = onStartVoiceCall, enabled = !isPendingRequest) {
                                Icon(Icons.Default.Call, contentDescription = "Sesli Ara", tint = if (!isPendingRequest) AnobaNavy else Color.Gray)
                            }
                            IconButton(onClick = onStartVideoCall, enabled = !isPendingRequest) {
                                Icon(Icons.Default.Videocam, contentDescription = "Görüntülü Ara", tint = if (!isPendingRequest) AnobaNavy else Color.Gray)
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menü", tint = AnobaNavy)
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Kullanıcıyı Engelle", color = Color.Red) },
                                        onClick = {
                                            showMenu = false
                                            showBlockDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) }
                                    )
                                }
                            }
                            IconButton(onClick = onHomeClick) {
                                Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = {
                if (isPendingRequest) {
                    // Sohbet İsteği Onay Barı
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sohbet İsteği Alındı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AnobaNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${partner.username} sizinle sohbet başlatmak istiyor.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showBlockDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("Engelle")
                                }
                                Button(
                                    onClick = onAcceptRequest,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
                                ) {
                                    Text("Kabul Et", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Mesaj Yazma Giriş Barı
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
                            IconButton(onClick = { showAttachmentSheet = true }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Ekle", tint = AnobaNavy)
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
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLoading && messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AnobaNavy)
                    }
                } else if (messages.isEmpty()) {
                    // WhatsApp Tarzı Güvenlik Kartı
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color(0xFFFEF5C3),
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF7A6B3A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${partner.username} ile sohbetiniz burada başlayacak. Mesajlarınız güvenle korunur.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF5A4B2A),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(messages, key = { _, item -> item.id }) { _, msg ->
                            PrivateChatBubble(
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
                    PrivateAttachmentOption(icon = Icons.Default.PhotoLibrary, title = "Galeri", color = Color(0xFFE11D48)) {
                        showAttachmentSheet = false
                        onSendMediaClick("GALLERY")
                    }
                    PrivateAttachmentOption(icon = Icons.Default.CameraAlt, title = "Fotoğraf Çek", color = Color(0xFF2563EB)) {
                        showAttachmentSheet = false
                        onSendMediaClick("CAMERA")
                    }
                    PrivateAttachmentOption(icon = Icons.Default.Videocam, title = "Video Çek", color = Color(0xFF059669)) {
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
private fun PrivateAttachmentOption(
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
private fun PrivateChatBubble(
    message: SharedChatMessage,
    isSentByMe: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSentByMe) 16.dp else 4.dp,
                        bottomEnd = if (isSentByMe) 4.dp else 16.dp
                    )
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
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
