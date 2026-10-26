package com.neval.anoba.mycontent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.home.ContentType
import com.neval.anoba.home.SharedFeedCard
import com.neval.anoba.home.SharedFeedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContentScreen(
    onNavigateBack: () -> Unit = {},
    onItemClick: (SharedFeedItem) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Yayınlananlar, 1: Taslaklar, 2: Pazar Yeri
    val tabs = listOf("Yayınlananlar", "Taslaklar", "Pazar Yeri 🛍️")

    val sampleMyPublished = remember {
        listOf(
            SharedFeedItem(
                id = "my_pub_1",
                type = ContentType.PHOTO,
                title = "Manzara Çekimi 📸",
                description = "Kendi profilimde yayınladığım fotoğrafım.",
                ownerId = "me",
                username = "Siz",
                userBadgeType = "PIONEER",
                isOfficial = false,
                topEmoji = "❤️",
                reactionCount = 142
            )
        )
    }

    val sampleMyDrafts = remember {
        listOf(
            SharedFeedItem(
                id = "my_draft_1",
                type = ContentType.LETTER,
                title = "Geleceğe Not (Taslak) 📝",
                description = "Henüz kilitlenmemiş taslak mektup.",
                ownerId = "me",
                username = "Siz",
                userBadgeType = "PIONEER",
                isOfficial = false,
                topEmoji = "✉️",
                reactionCount = 0
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
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Text("İçeriklerim & Taslaklarım 🎬", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFFD700)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                            )
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
                .padding(12.dp)
        ) {
            val currentList = when (selectedTabIndex) {
                0 -> sampleMyPublished
                1 -> sampleMyDrafts
                else -> emptyList<SharedFeedItem>()
            }

            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bu sekmede içerik bulunmuyor ✨", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList) { item ->
                        SharedFeedCard(
                            item = item,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}
