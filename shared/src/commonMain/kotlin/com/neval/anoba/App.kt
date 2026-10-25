package com.neval.anoba

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
import com.neval.anoba.chat.ui.ChatHomeScreen
import com.neval.anoba.drawer.DrawerSheetContent
import com.neval.anoba.guide.GuideScreen
import com.neval.anoba.home.ContentDetailScreen
import com.neval.anoba.home.ContentType
import com.neval.anoba.home.HomeAnimationOverlay
import com.neval.anoba.home.PopularSheet
import com.neval.anoba.home.QuarantineSheet
import com.neval.anoba.home.SharedFeedCard
import com.neval.anoba.home.SharedFeedItem
import com.neval.anoba.login.AuthContainerView
import com.neval.anoba.market.MarketContainerView
import com.neval.anoba.mete.MeteHomeScreen
import com.neval.anoba.mosaic.MosaicSheet
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
            MainAppScreen(
                onLogout = { isAuthenticated = false }
            )
        }
    }
}

@Composable
private fun MainAppScreen(
    onLogout: () -> Unit = {}
) {
    var activeView by remember { mutableStateOf("HOME") }
    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            when (activeView) {
                "MARKET" -> MarketContainerView(onNavigateHome = { activeView = "HOME" })
                "CHAT" -> ChatHomeScreen()
                "PROFILE" -> ProfileScreen(onNavigateBack = { activeView = "HOME" })
                "GUIDE" -> GuideScreen(onNavigateBack = { activeView = "HOME" })
                "METE" -> MeteHomeScreen(onNavigateBack = { activeView = "HOME" })
                else -> AnobaHomeView(
                    onOpenDrawer = { isDrawerOpen = true },
                    onOpenMarket = { activeView = "MARKET" },
                    onOpenChat = { activeView = "CHAT" },
                    onOpenProfile = { activeView = "PROFILE" },
                    onOpenMete = { activeView = "METE" }
                )
            }
        }

        // 🚀 Orijinal Android Sol Yan Menü (Drawer)
        DrawerSheetContent(
            isOpen = isDrawerOpen,
            onDismiss = { isDrawerOpen = false },
            onNavigateProfile = { activeView = "PROFILE" },
            onNavigateGuide = { activeView = "GUIDE" },
            onLogout = onLogout
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnobaHomeView(
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

    // 🌸 Pembe Çiçek Animasyon Efekti Overlay (Android Birebir Süzülen Efekt)
    HomeAnimationOverlay(
        isActive = showHomeAnimation,
        onFinished = { showHomeAnimation = false }
    )

    if (selectedItem != null) {
        ContentDetailScreen(
            item = selectedItem,
            onNavigateBack = { selectedItem = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            // 🚀 ANDROID İLE %100 BİREBİR TOP ACTION BAR
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 2.dp)) {
                    // 1. Satır: SOLDA Menü + Çiçek + 3 Nokta + İçi Boş Mağaza, SAĞDA Arama + İçi Boş İğne + Takip + Dünya + Yeşil Chat
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

                            // Pembe Çiçek Yuvarlağı (Tıklanabilir Animasyon)
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
                                        text = { Text("🇹🇷 Mete") },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenMete()
                                        }
                                    )
                                }
                            }

                            IconButton(onClick = onOpenMarket, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.ShoppingBag, contentDescription = "Pazar Yeri", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Search, contentDescription = "Arama", tint = Color(0xFF0288D1), modifier = Modifier.size(22.dp))
                            }

                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.PushPin, contentDescription = "Sabitlenenler", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                            }

                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.People, contentDescription = "Takip Edilenler", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                            }

                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Language, contentDescription = "Dünya Haritası", tint = AnobaNavy, modifier = Modifier.size(22.dp))
                            }

                            IconButton(onClick = onOpenChat, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Sohbet", tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )

                    // 2. Satır: MOZAİK / Reklam İçerir
                    Row(
                        modifier = Modifier.fillMaxWidth().height(42.dp).padding(top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.width(82.dp).fillMaxHeight().clickable { showMosaicSheet = true }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("MOZAİK", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, lineHeight = 11.sp, letterSpacing = 0.5.sp)
                                Text("Reklam İçerir", color = Color(0xFFCFD8DC), fontSize = 8.sp, lineHeight = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
        modifier = Modifier.size(32.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(17.dp))
        }
    }
}
