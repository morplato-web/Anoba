package com.neval.anoba.letter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedLetterHomeScreen(
    lettersList: List<SharedLetterModel> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onCreateLetterClick: () -> Unit = {},
    onLetterClick: (SharedLetterModel) -> Unit = {},
    onCommentClick: (SharedLetterModel) -> Unit = {}
) {
    val sampleLetters = remember(lettersList) {
        lettersList.ifEmpty {
            listOf(
                SharedLetterModel(
                    id = "let_1",
                    ownerId = "u1",
                    username = "Ahmet Y.",
                    title = "2026 Yılına Mektup ✉️",
                    content = "Sevgili geleceğim, bu mektup Anoba Zaman Kapsülü ile yazıldı...",
                    privacy = "PUBLIC",
                    createdAtMillis = 1710000000000L,
                    isPublished = true,
                    userBadgeType = "PIONEER",
                    reactionCount = 128,
                    commentsCount = 12
                ),
                SharedLetterModel(
                    id = "let_2",
                    ownerId = "anoba_official",
                    username = "Anoba",
                    title = "Geleceğin İnsanına Mesaj 🌟",
                    content = "Geçmişten gelen bir ses tonu ve zaman kapsülü mesajı.",
                    privacy = "PUBLIC",
                    audioUrl = "sample_voice.mp3",
                    audioDuration = 120L,
                    createdAtMillis = 1710000050000L,
                    isPublished = true,
                    isOfficial = true,
                    userBadgeType = "OFFICIAL",
                    reactionCount = 312,
                    commentsCount = 28
                )
            )
        }
    }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var pinnedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val showBackToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 3 }
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
                        Text("✉️ Zaman Kapsülü Mektupları", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onHomeClick) {
                            Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = onCreateLetterClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mektup Yaz", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tüm Mektuplar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sampleLetters, key = { it.id }) { letter ->
                        val isPinned = pinnedIds.contains(letter.id)
                        SharedLetterCard(
                            letter = letter,
                            cardColor = Color(0xFFFFF3E0),
                            isPinned = isPinned,
                            onPinClick = {
                                pinnedIds = if (isPinned) pinnedIds - letter.id else pinnedIds + letter.id
                            },
                            onLetterClick = { onLetterClick(letter) },
                            onCommentClick = { onCommentClick(letter) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(0) }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı Çık")
                }
            }
        }
    }
}
