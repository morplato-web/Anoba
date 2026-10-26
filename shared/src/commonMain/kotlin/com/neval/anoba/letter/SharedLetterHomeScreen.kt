package com.neval.anoba.letter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterHomeScreen(
    onNavigateBack: () -> Unit = {},
    onCreateLetterClick: () -> Unit = {},
    onLetterClick: (SharedLetterModel) -> Unit = {}
) {
    val sampleLetters = remember {
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
                reactionCount = 128
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
                reactionCount = 312
            )
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
                        Text("✉️ Zaman Kapsülü Mektupları", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleLetters, key = { it.id }) { letter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLetterClick(letter) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(
                                    photoUrl = letter.userProfileImageUrl,
                                    username = letter.username,
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = letter.username,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        AnobaBadge(
                                            badgeType = letter.userBadgeType,
                                            isOfficial = letter.isOfficial
                                        )
                                    }
                                    val timeStr = getRelativeTimeString(letter.createdAtMillis)
                                    if (timeStr.isNotBlank()) {
                                        Text(timeStr, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            }

                            if (!letter.audioUrl.isNullOrBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF57C00).copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sesli Mektup", color = Color(0xFFF57C00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!letter.title.isNullOrBlank()) {
                            Text(
                                text = letter.title!!,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = letter.content,
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF0F172A)
                            ) {
                                Text(
                                    text = "❤️ ${letter.reactionCount}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Zaman Kapsülü Kilitli 🔒",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
