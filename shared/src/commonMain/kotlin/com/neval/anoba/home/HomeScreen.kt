@file:kotlin.jvm.JvmName("SharedHomeScreenKt")

package com.neval.anoba.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.zIndex
import com.neval.anoba.mosaic.MosaicSheet
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {},
    onOpenMarket: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenMete: () -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf<SharedFeedItem?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showQuarantineSheet by remember { mutableStateOf(false) }
    var showPopularSheet by remember { mutableStateOf(false) }
    var showMosaicSheet by remember { mutableStateOf(false) }
    var showHomeAnimation by remember { mutableStateOf(false) }

    val feedItems = remember {
        listOf(
            SharedFeedItem(
                id = "feed_1",
                type = ContentType.LETTER,
                title = "*Anoba'ya Hoş Geldiniz! 🌍✨",
                description = "Bu uygulama, sınırları ortadan kaldırarak iletişimi daha canlı kılan dijital bir köprü olması için tasarlandı.\n• 📜 İçeriklerinizi taslak olarak kaydedebilir; istediğiniz zaman...",
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
                mediaUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
                ownerId = "u_betty",
                username = "Betty",
                userBadgeType = "NONE",
                isOfficial = false,
                topEmoji = "❤️",
                reactionCount = 12,
                viewCount = 104,
                createdAtMillis = 1710000050000L
            ),
            SharedFeedItem(
                id = "feed_3",
                type = ContentType.PHOTO,
                title = "E",
                description = "Çiçeklerle dolu doğa manzarası.",
                mediaUrl = "https://images.unsplash.com/photo-1501004318641-b39e6451bec6",
                ownerId = "u_e",
                username = "E",
                userBadgeType = "PIONEER",
                isOfficial = false,
                topEmoji = "❤️",
                reactionCount = 45,
                viewCount = 312,
                createdAtMillis = 1710000100000L
            )
        )
    }

    if (showQuarantineSheet) {
        QuarantineSheet(
            isVisible = true,
            onDismiss = { showQuarantineSheet = false }
        )
    }

    if (showPopularSheet) {
        PopularSheet(
            isVisible = true,
            onDismiss = { showPopularSheet = false }
        )
    }

    if (showMosaicSheet) {
        MosaicSheet(
            isVisible = true,
            onDismiss = { showMosaicSheet = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedItem != null) {
            ContentDetailScreen(
                item = selectedItem,
                onNavigateBack = { selectedItem = null }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
                // Top Bar
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menü", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF06292))
                                        .clickable { showHomeAnimation = true }
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Box {
                                    IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "3 Nokta", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                    }

                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("⭐ 1000+ Popüler") },
                                            onClick = {
                                                showMoreMenu = false
                                                showPopularSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("🛡️ Karantina Havuzu") },
                                            onClick = {
                                                showMoreMenu = false
                                                showQuarantineSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("🇹🇷 Mete Ses & Metin") },
                                            onClick = {
                                                showMoreMenu = false
                                                onOpenMete()
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(onClick = onOpenMarket, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Outlined.ShoppingBag, contentDescription = "Anoba Pazar Yeri", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Search, contentDescription = "Arama", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Outlined.PushPin, contentDescription = "İğnelenenler", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.People, contentDescription = "Takip Edilenler", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Language, contentDescription = "Harita", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                                }
                                IconButton(onClick = onOpenChat, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color(0xFF22C55E), modifier = Modifier.size(22.dp))
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), color = Color(0xFFE2E8F0), thickness = 1.dp)
                        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), color = Color(0xFFE2E8F0), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquareActionButton(icon = Icons.Default.DynamicFeed, bgColor = Color(0xFFEBF5FF), iconColor = Color(0xFF2563EB), label = "Tümü")
                            SquareActionButton(icon = Icons.Default.PhotoLibrary, bgColor = Color(0xFFFDF2F8), iconColor = Color(0xFFDB2777), label = "Foto")
                            SquareActionButton(icon = Icons.Default.CameraAlt, bgColor = Color(0xFFF0FDF4), iconColor = Color(0xFF16A34A), label = "Video")
                            SquareActionButton(icon = Icons.Default.Edit, bgColor = Color(0xFFFFFBEB), iconColor = Color(0xFFD97706), label = "Mektup")

                            Surface(
                                modifier = Modifier.weight(1.2f).height(38.dp).clickable { showMosaicSheet = true },
                                color = Color(0xFF8B5CF6),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("MOZAİK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Reklam İçerir", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }

                // Ana Akış Listesi
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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

        HomeAnimationOverlay(
            isActive = showHomeAnimation,
            onFinished = { showHomeAnimation = false },
            modifier = Modifier.zIndex(999f)
        )
    }
}

@Composable
private fun SquareActionButton(
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    label: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.size(38.dp).clickable { onClick() },
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
        }
    }
}
