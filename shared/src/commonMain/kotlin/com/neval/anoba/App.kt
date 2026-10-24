package com.neval.anoba

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.chat.ui.ChatHomeScreen
import com.neval.anoba.home.ContentDetailScreen
import com.neval.anoba.home.ContentType
import com.neval.anoba.home.SharedFeedCard
import com.neval.anoba.home.SharedFeedItem
import com.neval.anoba.login.AuthContainerView
import com.neval.anoba.market.MarketContainerView
import com.neval.anoba.profile.ProfileScreen
import com.neval.anoba.ui.theme.AnobaNavy
import com.neval.anoba.ui.theme.AnobaTheme

@Composable
fun App() {
    AnobaTheme {
        var isAuthenticated by remember { mutableStateOf(false) }

        if (!isAuthenticated) {
            AuthContainerView(
                onLoginSuccess = { isAuthenticated = true }
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
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
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
            color = if (isSelected) AnobaNavy else Color(0xFF94A3B8)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnobaHomeView() {
    var selectedItem by remember { mutableStateOf<SharedFeedItem?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val feedItems = remember {
        listOf(
            SharedFeedItem(
                id = "feed_1",
                type = ContentType.LETTER,
                title = "*Anoba'ya Hoş Geldiniz! 🌍✨",
                description = "Bu uygulama, sınırları ortadan kaldıran ve iletişimi daha canlı kılan dijital bir köprü olması için tasarlandı.\n• 📜 İçeriklerinizi taslak olarak kaydedebilir; istediğiniz zaman...",
                mediaUrl = null,
                ownerId = "anoba_official",
                username = "Anoba",
                userBadgeType = "OFFICIAL",
                isOfficial = true,
                topEmoji = "❤️",
                reactionCount = 1,
                viewCount = 27,
                createdAtMillis = 1710000000000L
            ),
            SharedFeedItem(
                id = "feed_2",
                type = ContentType.PHOTO,
                title = "Betty",
                description = "Bahçede güneşli bir gün karesi.",
                mediaUrl = null,
                ownerId = "u_betty",
                username = "Betty",
                userBadgeType = "NONE",
                isOfficial = false,
                topEmoji = "❤️",
                reactionCount = 12,
                viewCount = 104,
                createdAtMillis = 1710000050000L
            )
        )
    }

    if (selectedItem != null) {
        ContentDetailScreen(
            item = selectedItem,
            onNavigateBack = { selectedItem = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            // 🚀 BİREBİR ANDROID TOP ACTION BAR (Açık Beyaz Tema)
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp)) {
                    // 1. Satır İkonlar (Üst Bitişik)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Menu, contentDescription = "Menü", tint = AnobaNavy)
                            }

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF06292))
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "3 Nokta", tint = AnobaNavy)
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("⭐ 1000+ Popüler") },
                                        onClick = { showMoreMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🛡️ Karantina Havuzu") },
                                        onClick = { showMoreMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🇹🇷 Mete Ay-Yıldız") },
                                        onClick = { showMoreMenu = false }
                                    )
                                }
                            }

                            IconButton(onClick = {}) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = "Pazar Yeri", tint = AnobaNavy)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Search, contentDescription = "Arama", tint = Color(0xFF1E88E5))
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.PushPin, contentDescription = "Sabitlenenler", tint = AnobaNavy)
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.People, contentDescription = "Takip Edilenler", tint = AnobaNavy)
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Language, contentDescription = "Dünya Haritası", tint = AnobaNavy)
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = Color(0xFF4CAF50))
                            }
                        }
                    }

                    // 2. Satır: SOLDA Lacivert MOZAİK Butonu, SAĞDA 5 Renkli Kare Kart
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SOLDA: MOZAİK Butonu (Koyu Lacivert)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AnobaNavy,
                            modifier = Modifier.height(38.dp).clickable { }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("MOZAİK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                Text("Reklam İçerir", color = Color.LightGray, fontSize = 8.sp)
                            }
                        }

                        // SAĞDA: 5 Renkli Kare Kart (Zil, Kamera, Kalem, Galeri, İçeriklerim)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquareActionCard(
                                icon = Icons.Default.Notifications,
                                bgColor = Color(0xFFFFF8E1),
                                iconColor = Color(0xFFE53935)
                            )
                            SquareActionCard(
                                icon = Icons.Default.CameraAlt,
                                bgColor = Color(0xFFF3E5F5),
                                iconColor = Color(0xFF8E24AA)
                            )
                            SquareActionCard(
                                icon = Icons.Default.Edit,
                                bgColor = Color(0xFFE1F5FE),
                                iconColor = Color(0xFF0288D1)
                            )
                            SquareActionCard(
                                icon = Icons.Default.PhotoLibrary,
                                bgColor = Color(0xFFFFEBEE),
                                iconColor = Color(0xFFE53935)
                            )
                            SquareActionCard(
                                icon = Icons.Default.DynamicFeed,
                                bgColor = Color(0xFFECEFF1),
                                iconColor = Color(0xFF455A64)
                            )
                        }
                    }
                }
            }

            // Ana Akış Listesi
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(feedItems, key = { it.id }) { item ->
                    SharedFeedCard(
                        item = item,
                        onItemClick = { selectedItem = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareActionCard(
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.size(34.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
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
