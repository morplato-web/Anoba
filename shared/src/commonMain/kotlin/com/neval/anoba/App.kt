package com.neval.anoba

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.chat.ui.ChatHomeScreen
import com.neval.anoba.home.ContentType
import com.neval.anoba.home.SharedFeedCard
import com.neval.anoba.home.SharedFeedItem
import com.neval.anoba.login.LoginScreen
import com.neval.anoba.market.MarketContainerView
import com.neval.anoba.profile.ProfileScreen
import com.neval.anoba.ui.theme.AnobaTheme

@Composable
fun App() {
    AnobaTheme {
        var isAuthenticated by remember { mutableStateOf(false) }

        if (!isAuthenticated) {
            LoginScreen(
                onLoginSuccess = { isAuthenticated = true },
                onNavigateSignUp = { isAuthenticated = true }
            )
        } else {
            MainAppScreen()
        }
    }
}

@Composable
private fun MainAppScreen() {
    var currentTab by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Content View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    0 -> AnobaHomeView()
                    1 -> AnobaMarketView()
                    2 -> AnobaChatView()
                    3 -> AnobaProfileView()
                }
            }

            // Custom Bulletproof Bottom Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem("🏠", "Ana Sayfa", currentTab == 0) { currentTab = 0 }
                    NavItem("🛒", "Pazar Yeri", currentTab == 1) { currentTab = 1 }
                    NavItem("💬", "Sohbet", currentTab == 2) { currentTab = 2 }
                    NavItem("👤", "Profil", currentTab == 3) { currentTab = 3 }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun AnobaHomeView() {
    val feedItems = remember {
        listOf(
            SharedFeedItem(
                id = "feed_1",
                type = ContentType.PHOTO,
                title = "İstanbul Boğazında Gün Batımı 🌅",
                description = "Zaman kapsülüne kaydedilen eşsiz bir manzara karesi.",
                mediaUrl = null,
                ownerId = "u1",
                username = "Ahmet Y.",
                userBadgeType = "PIONEER",
                isOfficial = false,
                topEmoji = "❤️",
                reactionCount = 84,
                createdAtMillis = 1710000000000L
            ),
            SharedFeedItem(
                id = "feed_2",
                type = ContentType.LETTER,
                title = "Geleceğe Mektup: 2026 Hedefleri ✉️",
                description = "Zaman kapsülü kilitlendi, 1 yıl sonra açılacak.",
                mediaUrl = null,
                ownerId = "anoba_official",
                username = "Anoba",
                userBadgeType = "OFFICIAL",
                isOfficial = true,
                topEmoji = "🔥",
                reactionCount = 156,
                createdAtMillis = 1710000050000L
            ),
            SharedFeedItem(
                id = "feed_3",
                type = ContentType.AUDIO,
                title = "Sesli Not: Doğa Sesi & Yağmur 🎧",
                description = "2 dakikalık huzur veren doğa ses kaydı.",
                mediaUrl = null,
                ownerId = "u2",
                username = "Zeynep K.",
                userBadgeType = "NONE",
                isOfficial = false,
                topEmoji = "👏",
                reactionCount = 42,
                createdAtMillis = 1710000100000L
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Anoba iOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Connect, Chat & Time Capsule", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }

        items(feedItems, key = { it.id }) { item ->
            SharedFeedCard(item = item)
        }
    }
}

@Composable
private fun AnobaMarketView() {
    MarketContainerView()
}

@Composable
private fun AnobaChatView() {
    ChatHomeScreen()
}

@Composable
private fun AnobaProfileView() {
    ProfileScreen()
}
