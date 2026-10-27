@file:kotlin.jvm.JvmName("SharedGuideScreenKt")

package com.neval.anoba.guide

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.ui.theme.AnobaNavy

enum class GuideContentType {
    VIDEO, PHOTO, AUDIO, LETTER, MARKET
}

data class SharedGuideContentItem(
    val id: String,
    val type: GuideContentType,
    val mediaUrl: String = "",
    val textSnippet: String = "",
    val durationText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedGuideScreen(
    items: List<SharedGuideContentItem> = emptyList(),
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onOpenGlobalMap: () -> Unit = {},
    onOpenMete: () -> Unit = {},
    onOpenMosaic: () -> Unit = {},
    onOpenMarket: () -> Unit = {},
    onItemClick: (SharedGuideContentItem) -> Unit = {}
) {
    val brandNavy = AnobaNavy

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = "Rehber",
                            color = brandNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = brandNavy
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Ana Sayfa",
                                tint = brandNavy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 0. INFO (REHBER BİLGİ)
                item {
                    SpecialGuideCard(
                        title = "Rehber",
                        icon = Icons.Default.Info,
                        containerColor = Color(0xFFA5D6A7),
                        iconColor = Color.Black,
                        onClick = { /* Aktif ekran */ }
                    )
                }

                // 1. DÜNYA (HARİTA)
                item {
                    SpecialGuideCard(
                        title = "Dünya",
                        icon = Icons.Default.Language,
                        containerColor = Color(0xFFE3F2FD),
                        iconColor = Color(0xFF1976D2),
                        onClick = onOpenGlobalMap
                    )
                }

                // 2. METE
                item {
                    SpecialGuideCard(
                        title = "Mete",
                        icon = Icons.Default.Star,
                        containerColor = Color(0xFFF0F4C3),
                        iconColor = Color(0xFFC62828),
                        onClick = onOpenMete
                    )
                }

                // 3. MOZAİK
                item {
                    SpecialGuideCard(
                        title = "MOZAİK",
                        icon = Icons.Default.GridView,
                        containerColor = Color(0xFF37474F),
                        iconColor = Color.White,
                        onClick = onOpenMosaic
                    )
                }

                // 4. PAZAR YERİ 🛍️
                item {
                    SpecialGuideCard(
                        title = "Pazar Yeri 🛍️",
                        icon = Icons.Default.ShoppingBag,
                        containerColor = Color(0xFFFEF3C7),
                        iconColor = Color(0xFFD97706),
                        onClick = onOpenMarket
                    )
                }

                // NORMAL İÇERİKLER
                items(items, key = { it.id + it.type.name }) { item ->
                    GuideContentGridItem(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }

            if (isLoading && items.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = brandNavy
                )
            }
        }
    }
}

@Composable
fun SpecialGuideCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (iconColor == Color.White) Color.White else Color.Black.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GuideContentGridItem(
    item: SharedGuideContentItem,
    onClick: () -> Unit
) {
    val brandNavy = AnobaNavy
    val cardColor = when (item.type) {
        GuideContentType.VIDEO -> Color(0xFFF3E5F5)
        GuideContentType.PHOTO -> Color(0xFFFFEBEE)
        GuideContentType.AUDIO -> Color(0xFFFFD1DC)
        GuideContentType.LETTER -> Color(0xFFFFF3E0)
        GuideContentType.MARKET -> Color(0xFFFEF3C7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (item.type) {
                GuideContentType.VIDEO -> {
                    AsyncImage(
                        model = item.mediaUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayCircleOutline,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(36.dp)
                    )
                }
                GuideContentType.PHOTO, GuideContentType.MARKET -> {
                    AsyncImage(
                        model = item.mediaUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                GuideContentType.AUDIO -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = brandNavy
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.durationText.ifBlank { "0:30" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = brandNavy
                        )
                    }
                }
                GuideContentType.LETTER -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = brandNavy
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.textSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            color = brandNavy
                        )
                    }
                }
            }
        }
    }
}
