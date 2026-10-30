@file:kotlin.jvm.JvmName("SharedMeteHomeScreenKt")

package com.neval.anoba.mete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.ui.theme.AnobaNavy

enum class SharedMeteLanguage(val flag: String, val label: String) {
    TR("🇹🇷", "Türkçe"),
    EN("🇬🇧", "English"),
    FR("🇫🇷", "Français"),
    DE("🇩🇪", "Deutsch"),
    AR("🇸🇦", "العربية"),
    ES("🇪🇸", "Español"),
    IT("🇮🇹", "Italiano"),
    JP("🇯🇵", "日本語"),
    RU("🇷🇺", "Русский"),
    ZH("🇨🇳", "中文"),
    PT("🇵🇹", "Português"),
    KO("🇰🇷", "한국어"),
    HI("🇮🇳", "हिन्दी"),
    SW("🇰🇪", "Kiswahili"),
    AF("🇿🇦", "Afrikaans"),
}

data class SharedMeteWordItem(
    val id: Long,
    val word: String,
    val sectionName: String = "1",
    val boxColor: Color = Color(0xFF80DEEA),
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
)

data class SharedMeteSectionGroup(
    val sectionName: String,
    val words: List<SharedMeteWordItem> = emptyList(),
)

data class SharedMeteStoryItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val isUnlocked: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMeteHomeScreen(
    sectionGroups: List<SharedMeteSectionGroup> = emptyList(),
    storyVideos: List<SharedMeteStoryItem> = emptyList(),
    selectedLanguage: SharedMeteLanguage = SharedMeteLanguage.TR,
    isSystemAudioPlaying: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSelectLanguage: (SharedMeteLanguage) -> Unit = {},
    onPlayWordAudio: (SharedMeteWordItem) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onAddNewSection: (String) -> Unit = {},
    onUnlockSectionStory: (String) -> Unit = {},
    onPlayStoryVideo: (SharedMeteStoryItem) -> Unit = {},
) {
    var isGameMode by remember { mutableStateOf(value = false) }
    var isSearchActive by remember { mutableStateOf(value = false) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedWordItem by remember { mutableStateOf<SharedMeteWordItem?>(null) }
    var showCreateSectionDialog by remember { mutableStateOf(value = false) }

    val filteredSectionGroups = remember(sectionGroups, searchQuery) {
        if (searchQuery.isBlank()) sectionGroups
        else sectionGroups.filter { it.sectionName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredStoryVideos = remember(storyVideos, searchQuery) {
        if (searchQuery.isBlank()) storyVideos
        else storyVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    // Kelime Detay & Telaffuz Diyaloğu
    selectedWordItem?.let { wordItem ->
        SharedWordDetailDialog(
            item = wordItem,
            isAudioPlaying = isSystemAudioPlaying,
            onPlayAudio = { onPlayWordAudio(wordItem) },
            onStopAudio = onStopAudio,
        ) {
            onStopAudio()
            selectedWordItem = null
        }
    }

    // Yeni Bölüm Ekleme Diyaloğu
    if (showCreateSectionDialog) {
        var newSectionName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateSectionDialog = false },
            containerColor = Color.White,
            title = {
                Text("Yeni Bölüm Ekle", fontWeight = FontWeight.Bold, color = AnobaNavy)
            },
            text = {
                OutlinedTextField(
                    value = newSectionName,
                    onValueChange = { newSectionName = it },
                    label = { Text("Bölüm Adı") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newSectionName.trim()
                        if (trimmed.isNotBlank()) {
                            onAddNewSection(trimmed)
                            showCreateSectionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy),
                ) {
                    Text("Oluştur", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSectionDialog = false }) {
                    Text("İptal", color = Color.Gray)
                }
            },
        )
    }

    Scaffold(
        containerColor = Color(0xFFF0F8FF),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = "Mete",
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy,
                            fontSize = 18.sp,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = AnobaNavy,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) searchQuery = ""
                            },
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Ara",
                                tint = if (isSearchActive) Color.Red else AnobaNavy,
                            )
                        }
                        IconButton(
                            onClick = { isGameMode = !isGameMode },
                        ) {
                            Icon(
                                imageVector = if (isGameMode) Icons.Default.Close else Icons.Default.SportsEsports,
                                contentDescription = "Oyun Modu",
                                tint = if (isGameMode) Color.Red else AnobaNavy,
                            )
                        }
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Ana Sayfa",
                                tint = AnobaNavy,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Arama Çubuğu
            if (isSearchActive) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Bölüm veya video ara...", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Dil Seçici
            SharedLanguageSelector(
                selectedLanguage = selectedLanguage,
                onSelectLanguage = onSelectLanguage,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

            if (isGameMode) {
                val allPuzzles = remember(sectionGroups) { sectionGroups.flatMap { it.words } }
                SharedMeteGameView(
                    puzzles = allPuzzles,
                    isAudioPlaying = isSystemAudioPlaying,
                    onPlayAudio = onPlayWordAudio,
                    onCorrectAnswer = onUnlockSectionStory,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                ) {
                    // İKİ SÜTUNLU LİSTE ALANI
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // SOL SÜTUN (%58): Kelime Bölümleri
                        LazyColumn(
                            modifier = Modifier.weight(1.2f).fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            items(filteredSectionGroups) { group ->
                                SharedSectionCardModule(
                                    group = group,
                                    onWordClick = { selectedWordItem = it },
                                )
                            }
                        }

                        // SAĞ SÜTUN (%42): Çizgi Film / Animasyon Kartları
                        LazyColumn(
                            modifier = Modifier.weight(0.8f).fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            items(filteredStoryVideos) { story ->
                                SharedMeteStoryCard(
                                    item = story,
                                    onClick = { onPlayStoryVideo(story) },
                                )
                            }
                        }
                    }

                    // ALT BÖLÜM EKLE BUTONU
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        OutlinedButton(
                            onClick = { showCreateSectionDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yeni Bölüm Ekle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedLanguageSelector(
    selectedLanguage: SharedMeteLanguage,
    onSelectLanguage: (SharedMeteLanguage) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        items(SharedMeteLanguage.entries) { lang ->
            val isSelected = lang == selectedLanguage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 50.dp else 42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AnobaNavy
                        else Color.LightGray.copy(alpha = 0.35f),
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) AnobaNavy else Color.Transparent,
                        shape = CircleShape,
                    )
                    .clickable { onSelectLanguage(lang) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = lang.flag,
                    fontSize = if (isSelected) 22.sp else 18.sp,
                )
            }
        }
    }
}

@Composable
fun SharedSectionCardModule(
    group: SharedMeteSectionGroup,
    onWordClick: (SharedMeteWordItem) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = group.sectionName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD),
                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp),
            )

            group.words.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable { onWordClick(item) },
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(containerColor = item.boxColor),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!item.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = item.word,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit,
                                        )
                                    } else {
                                        Text(
                                            text = item.word,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    val emptySlots = 3 - rowItems.size
                    if (emptySlots > 0) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedMeteStoryCard(
    item: SharedMeteStoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) Color(0xFFE0F2F1) else Color(0xFFECEFF1),
        ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.isUnlocked) Color(0xFF80CBC4) else Color.Gray),
                contentAlignment = Alignment.Center,
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (item.isUnlocked) 0.5f else 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.isUnlocked) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text("🔒", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (item.isUnlocked) item.title else "🔒 ${item.title}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isUnlocked) Color(0xFF004D40) else Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SharedMeteGameView(
    puzzles: List<SharedMeteWordItem>,
    isAudioPlaying: Boolean,
    onPlayAudio: (SharedMeteWordItem) -> Unit,
    onCorrectAnswer: (String) -> Unit,
) {
    var options by remember { mutableStateOf<List<SharedMeteWordItem>>(emptyList()) }
    var targetItem by remember { mutableStateOf<SharedMeteWordItem?>(null) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var isAnsweredCorrectly by remember { mutableStateOf(value = false) }
    var hasListened by remember { mutableStateOf(value = false) }

    fun startNewRound() {
        if (puzzles.size < 4) return
        val shuffled = puzzles.shuffled()
        options = shuffled.take(4)
        targetItem = options.random()
        selectedItemId = null
        isAnsweredCorrectly = false
        hasListened = false
    }

    LaunchedEffect(puzzles.size) {
        if (options.isEmpty() && puzzles.isNotEmpty()) {
            startNewRound()
        }
    }

    if (puzzles.size < 4) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Oyun için yeterli kelime içeriği bulunamadı.", color = Color.Gray)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (isAudioPlaying) Color.Red.copy(alpha = 0.1f) else Color.White)
                .border(2.dp, if (isAudioPlaying) Color.Red else AnobaNavy, CircleShape)
                .clickable {
                    targetItem?.let {
                        onPlayAudio(it)
                        hasListened = true
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Dinle",
                modifier = Modifier.size(38.dp),
                tint = if (isAudioPlaying) Color.Red else AnobaNavy,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when {
                isAnsweredCorrectly -> "Tebrikler! Doğru Kartı Buldunuz! 🎉"
                !hasListened -> "Önce Sesi Dinleyin"
                else -> "Duyduğunuz kelimenin kartını seçin"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAnsweredCorrectly) Color(0xFF4CAF50) else AnobaNavy,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        val isSelected = selectedItemId == item.id
                        val isCorrect = item.id == targetItem?.id

                        val borderColor = when {
                            isSelected && isCorrect -> Color(0xFF4CAF50)
                            isSelected && !isCorrect -> Color.Red
                            else -> Color.Transparent
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(width = if (isSelected) 4.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
                                .clickable(enabled = !isAnsweredCorrectly && hasListened) {
                                    selectedItemId = item.id
                                    if (isCorrect) {
                                        isAnsweredCorrectly = true
                                        onCorrectAnswer(targetItem?.sectionName ?: "1")
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasListened) Color.White else Color.White.copy(alpha = 0.5f),
                            ),
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                                if (!item.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.word,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                } else {
                                    Text(text = item.word, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (isAnsweredCorrectly) {
            Button(
                onClick = { startNewRound() },
                modifier = Modifier.fillMaxWidth(0.6f).height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            ) {
                Text("Sonraki Soru ➡️", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SharedWordDetailDialog(
    item: SharedMeteWordItem,
    isAudioPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(text = item.word, fontWeight = FontWeight.Bold, color = AnobaNavy, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.boxColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.word,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text(text = item.word, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AnobaNavy)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { if (isAudioPlaying) onStopAudio() else onPlayAudio() },
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Dinle",
                            tint = if (isAudioPlaying) Color.Red else Color(0xFF4CAF50),
                            modifier = Modifier.size(44.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tekrar Et",
                            tint = AnobaNavy,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = AnobaNavy, fontWeight = FontWeight.Bold)
            }
        },
    )
}
