package com.neval.anoba.mete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SharedMeteWord(
    val id: String,
    val title: String,
    val sectionName: String,
    val boxColor: Color = Color(0xFF80DEEA)
)

data class SharedMeteStory(
    val id: String,
    val title: String,
    val isUnlocked: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeteHomeScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedLanguage by remember { mutableStateOf("🇹🇷 Türkçe") }
    var isGameMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val languages = listOf("🇹🇷 Türkçe", "🇬🇧 English", "🇫🇷 Français", "🇩🇪 Deutsch", "🇸🇦 العربية", "🇪🇸 Español", "🇮🇹 Italiano", "🇯🇵 日本語", "🇷🇺 Русский", "🇵🇹 Português")

    val sampleWords = remember {
        listOf(
            SharedMeteWord("1", "Elma 🍎", "Bölüm 1: Meyveler", Color(0xFFFFCDD2)),
            SharedMeteWord("2", "Muz 🍌", "Bölüm 1: Meyveler", Color(0xFFFFF9C4)),
            SharedMeteWord("3", "Kedi 🐱", "Bölüm 2: Hayvanlar", Color(0xFFE1BEE7)),
            SharedMeteWord("4", "Köpek 🐶", "Bölüm 2: Hayvanlar", Color(0xFFC8E6C9))
        )
    }

    val sampleStories = remember {
        listOf(
            SharedMeteStory("s1", "Mete ve Ay-Yıldız Macerası 🚀", true),
            SharedMeteStory("s2", "Dünya Turu ve Diller 🌍", true),
            SharedMeteStory("s3", "Zaman Kapsülü Gizemi 🔒", false)
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(color = Color(0xFF1E293B), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Text("🌙 Mete Öğrenim & Ay-Yıldız", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { isGameMode = !isGameMode }) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Oyun Modu",
                            tint = if (isGameMode) Color(0xFFFFD700) else Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Dil Seçici
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { lang ->
                    val isSelected = lang == selectedLanguage
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGameMode) {
                // Interaktif Ses Dinle & Kart Bul Oyunu
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("🎮 Sesli Eğitici Oyun Modu", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Sesi dinle ve doğru görsel kartını eşleştir!", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            } else {
                Text("Öğrenim Kartları & Animasyonlar 🎬", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sampleStories) { story ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(story.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (story.isUnlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                                ) {
                                    Text(
                                        text = if (story.isUnlocked) "Açık ▶️" else "Kilitli 🔒",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
