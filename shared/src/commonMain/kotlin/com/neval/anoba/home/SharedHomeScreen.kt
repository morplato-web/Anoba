@file:kotlin.jvm.JvmName("SharedHomeScreenKt")

package com.neval.anoba.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.neval.anoba.mosaic.MosaicSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch

@Composable
fun SharedHomeScreen(
    countryCode: String? = null,
    onClearCountryFilter: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onOpenMarket: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onTogglePinFilter: () -> Unit = {},
    onToggleFollowingFilter: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenCamera: () -> Unit = {},
    onOpenLetter: () -> Unit = {},
    onOpenLetterCreate: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
    onOpenMyContents: () -> Unit = {},
    onOpenMete: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    isPinnedFilterActive: Boolean = false,
    hasPinnedItems: Boolean = false,
    isFollowingFilterActive: Boolean = false,
    feedItems: List<SharedFeedItem> = emptyList(),
    onItemClick: (SharedFeedItem) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showMoreMenu by remember { mutableStateOf(false) }
    var showQuarantineSheet by remember { mutableStateOf(false) }
    var showPopularSheet by remember { mutableStateOf(false) }
    var showMosaicSheet by remember { mutableStateOf(false) }
    var showHomeAnimation by remember { mutableStateOf(false) }

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. ÜST BAR ALANI ---
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // SOL KISIM: Menü, Çiçek Noktası, 3 Nokta, Pazar Yeri
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menü",
                                    tint = AnobaNavy
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF06292))
                                    .clickable { showHomeAnimation = true }
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            // 3 Nokta Menüsü
                            Box {
                                IconButton(
                                    modifier = Modifier.size(28.dp),
                                    onClick = { showMoreMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Diğer Menü",
                                        tint = AnobaNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    // 1000+ Popüler İçerikler
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(20.dp),
                                                    shape = CircleShape,
                                                    color = AnobaGold,
                                                    shadowElevation = 1.dp
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "1000+",
                                                            color = Color.White,
                                                            fontSize = 5.sp,
                                                            fontWeight = FontWeight.Black,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = "1000+ Popüler",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showPopularSheet = true
                                        }
                                    )

                                    // Karantina Havuzu
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = "Karantina Havuzu",
                                                        modifier = Modifier.size(20.dp),
                                                        tint = Color(0xFF4CAF50).copy(alpha = 0.9f)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(8.dp),
                                                        tint = Color.White
                                                    )
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = "Karantina Havuzu",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showQuarantineSheet = true
                                        }
                                    )

                                    // Mete
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Text(text = "🇹🇷", fontSize = 16.sp)
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = "Mete",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenMete()
                                        }
                                    )

                                    // Mektuplar (Zaman Kapsülü)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Text(text = "✉️", fontSize = 16.sp)
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = "Mektuplar",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenLetter()
                                        }
                                    )

                                    // Profilim
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Text(text = "👤", fontSize = 16.sp)
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = "Profilim",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenProfile()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Pazar Yeri Butonu
                            IconButton(
                                modifier = Modifier.size(28.dp),
                                onClick = onOpenMarket
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = "Pazar Yeri",
                                    tint = AnobaNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Varsa Ülke Filtresi Rozeti
                            if (!countryCode.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AnobaNavy.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable { onClearCountryFilter() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = countryCode.uppercase(), style = MaterialTheme.typography.bodySmall, color = AnobaNavy, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Filtreyi Kaldır", modifier = Modifier.size(12.dp), tint = AnobaNavy.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }

                        // SAĞ KISIM: Arama, Pin, Takip, Harita, Chat
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            // 1. Arama
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = onOpenSearch
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Kullanıcı Filtresi",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF1E88E5)
                                )
                            }

                            // 2. Pin
                            val pinIcon = if (isPinnedFilterActive || hasPinnedItems) Icons.Filled.PushPin else Icons.Outlined.PushPin
                            val pinTint = when {
                                isPinnedFilterActive -> Color(0xFF2196F3)
                                hasPinnedItems -> AnobaGold
                                else -> AnobaNavy
                            }
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = onTogglePinFilter
                            ) {
                                Icon(
                                    imageVector = pinIcon,
                                    contentDescription = "Pin Filtresi",
                                    modifier = Modifier.size(24.dp),
                                    tint = pinTint
                                )
                            }

                            // 3. Takip Edilenler
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = onToggleFollowingFilter
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = "Takip Filtresi",
                                    modifier = Modifier.size(25.dp),
                                    tint = if (isFollowingFilterActive) Color(0xFF4CAF50) else AnobaNavy
                                )
                            }

                            // 4. Harita
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = onOpenMap
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Harita",
                                    modifier = Modifier.size(24.dp),
                                    tint = AnobaNavy
                                )
                            }

                            // 5. Sohbet
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = onOpenChat
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Sohbet",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp)

                    // --- 2. MOZAİK VE SAĞ AKSİYON KARTLARI SATIRI ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Solda: Mozaik Kartı
                        Card(
                            modifier = Modifier
                                .height(38.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMosaicSheet = true },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "MOZAİK",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = "Reklam İçerir",
                                    color = Color.LightGray.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        val iconSize = 20.dp
                        val cardHeight = 30.dp
                        val cardWidth = 32.dp

                        // Sağda: 5 Adet Aksiyon Kartı
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Bildirim (Zil)
                                BadgedBox(
                                    modifier = Modifier.size(width = cardWidth, height = cardHeight),
                                    badge = {
                                        if (hasUnreadNotifications) {
                                            Badge(containerColor = Color.Red, modifier = Modifier.size(7.dp))
                                        }
                                    }
                                ) {
                                    IconInCardButton(
                                        modifier = Modifier.fillMaxSize(),
                                        onClick = onOpenNotifications,
                                        icon = Icons.Default.Notifications,
                                        cardColor = if (hasUnreadNotifications) Color(0xFFFFF3CD) else Color(0xFFECEFF1),
                                        iconColor = if (hasUnreadNotifications) Color(0xFFE65100) else Color(0xFF455A64),
                                        size = iconSize
                                    )
                                }

                                // 2. Video / Kamera
                                IconInCardButton(
                                    modifier = Modifier.size(width = cardWidth, height = cardHeight),
                                    onClick = onOpenCamera,
                                    icon = Icons.Filled.CameraAlt,
                                    cardColor = Color(0xFFF3E5F5),
                                    iconColor = Color(0xFF8E24AA),
                                    size = iconSize
                                )

                                // 3. Mektup / Kalem
                                IconInCardButton(
                                    modifier = Modifier.size(width = cardWidth, height = cardHeight),
                                    onClick = onOpenLetterCreate,
                                    icon = Icons.Filled.Edit,
                                    cardColor = Color(0xFFE1F5FE),
                                    iconColor = Color(0xFF0288D1),
                                    size = iconSize
                                )

                                // 4. Galeri
                                IconInCardButton(
                                    modifier = Modifier.size(width = cardWidth, height = cardHeight),
                                    onClick = onOpenGallery,
                                    icon = Icons.Filled.PhotoLibrary,
                                    cardColor = Color(0xFFFFEBEE),
                                    iconColor = Color(0xFFE53935),
                                    size = iconSize
                                )

                                // 5. İçeriklerim
                                IconInCardButton(
                                    modifier = Modifier.size(width = cardWidth, height = cardHeight),
                                    onClick = onOpenMyContents,
                                    icon = Icons.Filled.DynamicFeed,
                                    cardColor = Color(0xFFECEFF1),
                                    iconColor = Color(0xFF37474F),
                                    size = iconSize
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp)
                }
            }

            // --- 3. ANA AKIŞ LİSTESİ ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (feedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz içerik bulunmuyor", color = Color.Gray)
                        }
                    }
                } else {
                    items(feedItems, key = { it.id }) { item ->
                        SharedFeedCard(
                            item = item,
                            onItemClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // Yukarı Çık Butonu (Back To Top)
        AnimatedVisibility(
            visible = showBackToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        if (listState.firstVisibleItemIndex > 10) {
                            listState.scrollToItem(5)
                        }
                        listState.animateScrollToItem(0)
                    }
                },
                shape = CircleShape,
                containerColor = AnobaNavy,
                contentColor = Color.White,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı Çık")
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
fun IconInCardButton(
    onClick: () -> Unit,
    icon: ImageVector,
    cardColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(size)
            )
        }
    }
}
