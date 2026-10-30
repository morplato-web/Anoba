package com.neval.anoba.mycontent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMyContentScreen(
    isOwner: Boolean = true,
    targetUsername: String? = null,
    publishedContent: List<SharedMyContentItem> = emptyList(),
    draftContent: List<SharedMyContentItem> = emptyList(),
    marketContent: List<SharedMyContentItem> = emptyList(),
    isLoading: Boolean = false,
    uploadProgress: String? = null,
    isAnobaUser: Boolean = false,
    errorMessage: String? = null,
    videoPreparing: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onCameraClick: () -> Unit = {},
    onLetterClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onItemClick: (SharedMyContentItem) -> Unit = {},
    onPublishContent: (id: String, type: SharedMyContentType) -> Unit = { _, _ -> },
    onEditContent: (item: SharedMyContentItem) -> Unit = {},
    onDeleteContent: (id: String, type: SharedMyContentType) -> Unit = { _, _ -> },
    onToggleAnobaGuide: (id: String, type: SharedMyContentType) -> Unit = { _, _ -> },
    onShareContent: (shareText: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var deleteItemTarget by remember { mutableStateOf<SharedMyContentItem?>(null) }
    var selectedContentItem by remember { mutableStateOf<SharedMyContentItem?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val tabs = if (isOwner) {
        listOf("Yayınlananlar", "Taslaklar", "Pazar Yeri 🛍️")
    } else {
        listOf("Yayınlananlar", "Pazar Yeri 🛍️")
    }

    deleteItemTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItemTarget = null },
            title = { Text("İçerik Silinsin mi?") },
            text = { Text("Bu içeriği silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteContent(item.id, item.type)
                        deleteItemTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteItemTarget = null }) {
                    Text("İptal")
                }
            },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(color = Color(0xFF1E293B), shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                            }
                            Text(
                                text = if (!isOwner) {
                                    targetUsername ?: "İçerikler"
                                } else {
                                    "İçeriklerim 🎬"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (isOwner) {
                                CreationIconButton(
                                    icon = Icons.Filled.CameraAlt,
                                    contentDescription = "Yeni Foto/Video",
                                    backgroundColor = Color(0xFFF3E5F5),
                                    iconColor = Color(0xFF8E24AA),
                                    onClick = onCameraClick,
                                )

                                CreationIconButton(
                                    icon = Icons.Filled.Edit,
                                    contentDescription = "Yeni Mektup",
                                    backgroundColor = Color(0xFFE1F5FE),
                                    iconColor = Color(0xFF0288D1),
                                    onClick = onLetterClick,
                                )

                                CreationIconButton(
                                    icon = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Galeriden Seç",
                                    backgroundColor = Color(0xFFFFEBEE),
                                    iconColor = Color(0xFFE53935),
                                    onClick = onGalleryClick,
                                )
                            }

                            IconButton(onClick = onHomeClick) {
                                Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = Color.White)
                            }
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        val itemsToShow: List<SharedMyContentItem> = if (isOwner) {
                            when (selectedTabIndex) {
                                0 -> publishedContent
                                1 -> draftContent
                                2 -> marketContent
                                else -> publishedContent
                            }
                        } else {
                            when (selectedTabIndex) {
                                0 -> publishedContent
                                1 -> marketContent
                                else -> publishedContent
                            }
                        }

                        val emptyMessage = when {
                            (isOwner && selectedTabIndex == 1) -> "Henüz Taslak Bulunmuyor 📝"
                            selectedTabIndex == (if (isOwner) 2 else 1) -> "Henüz İlan Bulunmuyor 🛍️"
                            else -> "Henüz Yayınlanan İçerik Bulunmuyor 🎬"
                        }

                        if (itemsToShow.isEmpty() && !isLoading) {
                            Text(text = emptyMessage, color = Color.Gray)
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalItemSpacing = 10.dp,
                            ) {
                                items(itemsToShow, key = { it.id + it.type.name }) { item ->
                                    Box {
                                        SharedContentCard(
                                            item = item,
                                            onClick = {
                                                if (!isOwner) {
                                                    onItemClick(item)
                                                } else {
                                                    selectedContentItem = item
                                                }
                                            },
                                        )

                                        if (item.isFlagged) {
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp),
                                                color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                                                shape = RoundedCornerShape(4.dp),
                                                shadowElevation = 4.dp,
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(10.dp),
                                                        tint = Color.White,
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "İncelemede",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
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
        }

        if (isLoading || videoPreparing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (videoPreparing) {
                            "Video Hazırlanıyor..."
                        } else {
                            uploadProgress?.let { "Yükleniyor ($it)" } ?: "İçerikler Yükleniyor..."
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }

    selectedContentItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedContentItem = null },
            sheetState = sheetState,
        ) {
            SharedBottomSheetContent(
                item = item,
                onViewClick = {
                    onItemClick(item)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onPublishClick = {
                    onPublishContent(item.id, item.type)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onEditClick = {
                    onEditContent(item)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onDeleteClick = {
                    deleteItemTarget = item
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onAnobaClick = {
                    onToggleAnobaGuide(item.id, item.type)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onShareClick = {
                    val shareText = when (item) {
                        is SharedMyContentItem.Audio -> item.audioUrl
                        is SharedMyContentItem.Letter -> item.content
                        is SharedMyContentItem.Photo -> item.photoUrl
                        is SharedMyContentItem.Video -> item.videoUrl
                        is SharedMyContentItem.Market -> item.photoUrl
                    }
                    onShareContent(shareText)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                isAnobaUser = isAnobaUser,
            )
        }
    }
}

@Composable
private fun CreationIconButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .size(34.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SharedBottomSheetContent(
    item: SharedMyContentItem,
    onViewClick: () -> Unit,
    onPublishClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAnobaClick: () -> Unit,
    onShareClick: () -> Unit,
    isAnobaUser: Boolean,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (item.isPublished) "Yayınlandı" else "Taslak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isPublished) Color(0xFF388E3C) else MaterialTheme.colorScheme.outline,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.viewCount.toString(), style = MaterialTheme.typography.titleLarge)
                Text(text = "Görüntülenme", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.reactionsCount.toString(), style = MaterialTheme.typography.titleLarge)
                Text(text = "Tepkiler", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.commentsCount.toString(), style = MaterialTheme.typography.titleLarge)
                Text(text = "Yorumlar", style = MaterialTheme.typography.bodySmall)
            }
        }
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Aç / Görüntüle") },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF42A5F5)) },
            modifier = Modifier.clickable(onClick = onViewClick),
        )

        if (item.isPublished) {
            ListItem(
                headlineContent = { Text("İçeriği Paylaş") },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF4CAF50)) },
                modifier = Modifier.clickable(onClick = onShareClick),
            )
        }

        ListItem(
            headlineContent = { Text(if (item.isPublished) "Yayından Kaldır" else "Yayınla") },
            leadingContent = { Icon(Icons.Default.Publish, contentDescription = null, tint = Color(0xFF9E9E9E)) },
            modifier = Modifier.clickable(onClick = onPublishClick),
        )

        ListItem(
            headlineContent = { Text("Düzenle") },
            leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFFA726)) },
            modifier = Modifier.clickable(onClick = onEditClick),
        )

        if (isAnobaUser) {
            ListItem(
                headlineContent = { Text(if (item.isGuideContent) "Rehberden Çıkar" else "Rehbere Ekle") },
                leadingContent = {
                    Icon(
                        imageVector = if (item.isGuideContent) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Anoba",
                        tint = AnobaGold,
                    )
                },
                modifier = Modifier.clickable(onClick = onAnobaClick),
            )
        }

        ListItem(
            headlineContent = { Text("Sil") },
            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            modifier = Modifier.clickable(onClick = onDeleteClick),
        )
    }
}

@Composable
fun SharedContentCard(item: SharedMyContentItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        when (item) {
            is SharedMyContentItem.Letter -> {
                SharedMyContentLetterItem(letter = item, onClick = onClick)
            }

            is SharedMyContentItem.Video, is SharedMyContentItem.Photo, is SharedMyContentItem.Audio, is SharedMyContentItem.Market -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (item) {
                            is SharedMyContentItem.Video -> Color(0xFFB3E5FC)
                            is SharedMyContentItem.Photo -> Color(0xFFD1C4E9)
                            is SharedMyContentItem.Audio -> Color(0xFFFFD1DC)
                            is SharedMyContentItem.Market -> Color(0xFFFFF9C4)
                            else -> Color(0xFFB3E5FC)
                        },
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (item is SharedMyContentItem.Audio) 1f else 0.75f),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (item) {
                            is SharedMyContentItem.Market -> {
                                AsyncImage(
                                    model = item.photoUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD700),
                                ) {
                                    Text(
                                        text = "🛍️ İlan",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            is SharedMyContentItem.Video -> {
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = "Video kapak",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                Icon(
                                    imageVector = Icons.Filled.PlayCircleOutline,
                                    contentDescription = "Oynat",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(48.dp),
                                )
                            }

                            is SharedMyContentItem.Photo -> {
                                AsyncImage(
                                    model = item.photoUrl,
                                    contentDescription = "Fotoğraf",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }

                            is SharedMyContentItem.Audio -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.GraphicEq,
                                        contentDescription = "Ses Dalgası",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                    val formattedDuration = remember(item.durationMillis) {
                                        val totalSec = item.durationMillis / 1000
                                        val min = totalSec / 60
                                        val sec = totalSec % 60
                                        "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
                                    }
                                    Text(
                                        text = formattedDuration,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }

        val personalNote = item.personalNote

        if (personalNote.isNotBlank()) {
            var isExpanded by remember { mutableStateOf(value = false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .animateContentSize(),
            ) {
                Text(
                    text = personalNote,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                )
            }
        }
    }
}

@Composable
fun SharedMyContentLetterItem(
    letter: SharedMyContentItem.Letter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAudio = !letter.audioUrl.isNullOrBlank()
    val isSealed = letter.privacy == "SEALED"
    val isTimeCapsule = letter.isTimeCapsule
    var isExpanded by remember { mutableStateOf(value = false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECB3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd,
            ) {
                if (isSealed && !letter.recipientUsername.isNullOrBlank()) {
                    Surface(
                        color = AnobaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp),
                                tint = Color(0xFF7B5E00),
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = letter.recipientUsername,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7B5E00),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else if (isAudio) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = AnobaNavy.copy(alpha = 0.35f),
                    )
                }
            }

            if (isTimeCapsule || isSealed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isTimeCapsule) {
                        Icon(
                            imageVector = Icons.Default.WatchLater,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    if (isSealed) {
                        if (isTimeCapsule) Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    if (isAudio) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            } else if (isAudio) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AnobaGold,
                    )
                    if (letter.audioDuration != null && letter.audioDuration > 0L) {
                        Spacer(Modifier.height(2.dp))
                        val formattedDuration = remember(letter.audioDuration) {
                            val totalSec = letter.audioDuration / 1000
                            val min = totalSec / 60
                            val sec = totalSec % 60
                            "$min:${sec.toString().padStart(2, '0')}"
                        }
                        Text(
                            text = formattedDuration,
                            fontSize = 10.sp,
                            color = Color.Gray,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 14.dp, bottom = 4.dp),
                ) {
                    if (isExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = letter.content,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            )
                        }
                    } else {
                        Text(
                            text = letter.content,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (letter.content.length > 50) {
                            Text(
                                text = "… devamı",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFFFFECB3).copy(alpha = 0.9f))
                                    .clickable { isExpanded = true },
                            )
                        }
                    }
                }
            }
        }
    }
}
