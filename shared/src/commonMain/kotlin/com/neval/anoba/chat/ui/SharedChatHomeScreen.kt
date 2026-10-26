package com.neval.anoba.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar

data class SamplePrivateChatItem(
    val id: String,
    val userId: String,
    val username: String,
    val userPhotoUrl: String?,
    val lastMessage: String,
    val badgeType: String?,
    val isOfficial: Boolean,
    val timeAgo: String,
    val unreadCount: Int = 0
)

data class SampleGroupChatItem(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val isPrivate: Boolean,
    val lastMessage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHomeScreen(
    onNavigateBack: () -> Unit = {},
    onOpenPrivateChat: (String, String) -> Unit = { _, _ -> },
    onOpenGroupChat: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    var inputMessageText by remember { mutableStateOf("") }

    val sampleGeneralMessages = remember {
        mutableStateListOf(
            GeneralChatMessageEntity(
                id = "gen_1",
                senderId = "admin",
                senderName = "Anoba",
                senderPhotoUrl = null,
                senderBadgeType = "OFFICIAL",
                content = "Anoba küresel topluluğuna hoş geldiniz! 🌍",
                timestampMillis = 1710000000000L
            ),
            GeneralChatMessageEntity(
                id = "gen_2",
                senderId = "u1",
                senderName = "Mehmet K.",
                senderPhotoUrl = null,
                senderBadgeType = "PIONEER",
                content = "Selamlar! iOS sürümü harika görünüyor 🚀",
                timestampMillis = 1710000050000L
            )
        )
    }

    val samplePrivateChats = remember {
        listOf(
            SamplePrivateChatItem(
                id = "p1",
                userId = "seller_1",
                username = "Ahmet Y. (Satıcı)",
                userPhotoUrl = null,
                lastMessage = "iPhone 15 Pro Max ilanı hakkında konuşalım.",
                badgeType = "PIONEER",
                isOfficial = false,
                timeAgo = "10 dk",
                unreadCount = 1
            ),
            SamplePrivateChatItem(
                id = "p2",
                userId = "anoba_official",
                username = "Anoba Destek",
                userPhotoUrl = null,
                lastMessage = "Talebiniz başarıyla alındı.",
                badgeType = "OFFICIAL",
                isOfficial = true,
                timeAgo = "1 sa"
            )
        )
    }

    val sampleGroups = remember {
        listOf(
            SampleGroupChatItem(
                id = "g1",
                name = "Pazar Yeri & Takas Kulübü 🛍️",
                description = "İkinci el alım, satım ve takas sohbet grubu.",
                memberCount = 124,
                isPrivate = false,
                lastMessage = "Zeynep: Yeni ilan ekledim arkadaşlar."
            ),
            SampleGroupChatItem(
                id = "g2",
                name = "Anoba Yazılım & Teknoloji 💻",
                description = "Yazılım, mobil ve KMP dünyasından gelişmeler.",
                memberCount = 89,
                isPrivate = false,
                lastMessage = "KMP iOS derlemesi tamamlandı!"
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(color = Color(0xFF1E293B), shadowElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💬 Sohbet Odaları",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Canlı 🟢",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFFD700),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFFD700)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Genel 🌍", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Özel 👤", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Gruplar 👥", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (selectedTab == 0) {
                Surface(
                    color = Color(0xFF1E293B),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessageText,
                            onValueChange = { inputMessageText = it },
                            placeholder = { Text("Genel sohbet odasına yaz...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (inputMessageText.isNotBlank()) {
                                    sampleGeneralMessages.add(
                                        GeneralChatMessageEntity(
                                            id = "gen_${sampleGeneralMessages.size + 1}",
                                            senderId = "me",
                                            senderName = "Siz",
                                            senderPhotoUrl = null,
                                            senderBadgeType = "NONE",
                                            content = inputMessageText,
                                            timestampMillis = 1710000100000L
                                        )
                                    )
                                    inputMessageText = ""
                                }
                            },
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier.size(20.dp))
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
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleGeneralMessages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                isSentByCurrentUser = msg.senderId == "me",
                                isSelected = false,
                                onLongPress = {},
                                onClick = {}
                            )
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(samplePrivateChats, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenPrivateChat(item.userId, item.username) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        photoUrl = item.userPhotoUrl,
                                        username = item.username,
                                        size = 48.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.username,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            AnobaBadge(
                                                badgeType = item.badgeType,
                                                isOfficial = item.isOfficial
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.lastMessage,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(item.timeAgo, color = Color(0xFF64748B), fontSize = 11.sp)
                                        if (item.unreadCount > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFFD700)
                                            ) {
                                                Text(
                                                    text = "${item.unreadCount}",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleGroups, key = { it.id }) { group ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenGroupChat(group.id, group.name) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Group, contentDescription = null, tint = Color.Black)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${group.memberCount} üye • ${group.description}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = group.lastMessage,
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
