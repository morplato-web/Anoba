package com.neval.anoba.letter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

enum class SharedLetterMode { WRITE, SPEAK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedLetterCreateScreen(
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onStartRecord: () -> Unit = {},
    onStopRecord: () -> Unit = {},
    onCancelRecord: () -> Unit = {},
    onPlayPreview: () -> Unit = {},
    onStopPreview: () -> Unit = {},
    onSaveLetter: (
        content: String,
        privacy: String,
        pseudonym: String?,
        isAskAnoba: Boolean,
        hasAudio: Boolean
    ) -> Unit = { _, _, _, _, _ -> }
) {
    var newLetterContent by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(SharedLetterMode.WRITE) }

    var isRecording by remember { mutableStateOf(false) }
    var hasFinishedRecording by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    var pseudonym by remember { mutableStateOf("") }
    var isAskAnoba by remember { mutableStateOf(false) }
    var answerPrivacy by remember { mutableStateOf("PUBLIC") }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✉️ Mektup Yaz", fontWeight = FontWeight.Bold, color = AnobaNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MOD SEÇİCİ (Yaz / Konuş)
            TabRow(
                selectedTabIndex = if (selectedMode == SharedLetterMode.WRITE) 0 else 1,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedMode == SharedLetterMode.WRITE) 0 else 1]),
                        color = AnobaGold
                    )
                }
            ) {
                Tab(
                    selected = selectedMode == SharedLetterMode.WRITE,
                    onClick = { selectedMode = SharedLetterMode.WRITE },
                    text = { Text("Mektup Yaz", fontWeight = if (selectedMode == SharedLetterMode.WRITE) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedMode == SharedLetterMode.SPEAK,
                    onClick = { selectedMode = SharedLetterMode.SPEAK },
                    text = { Text("Sesli Konuş", fontWeight = if (selectedMode == SharedLetterMode.SPEAK) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (selectedMode == SharedLetterMode.WRITE) {
                OutlinedTextField(
                    value = newLetterContent,
                    onValueChange = { newLetterContent = it },
                    placeholder = { Text("Geleceğe ya da birine duygu ve düşüncelerini yaz...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                // SESLİ MEKTUP ARAYÜZÜ
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!isRecording && !hasFinishedRecording) {
                            IconButton(
                                onClick = {
                                    isRecording = true
                                    hasFinishedRecording = false
                                    onStartRecord()
                                },
                                modifier = Modifier.size(64.dp).background(Color(0xFFFFD1DC), CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Kayıt başlatmak için mikrofon simgesine dokunun", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else if (isRecording) {
                            IconButton(
                                onClick = {
                                    isRecording = false
                                    hasFinishedRecording = true
                                    onStopRecord()
                                },
                                modifier = Modifier.size(64.dp).background(Color.Red.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Box(modifier = Modifier.size(24.dp).background(Color.Red, RoundedCornerShape(4.dp)))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Kaydediliyor... Durdurmak için dokunun", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlayingPreview) {
                                            onStopPreview()
                                            isPlayingPreview = false
                                        } else {
                                            onPlayPreview()
                                            isPlayingPreview = true
                                        }
                                    },
                                    modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape)
                                ) {
                                    Icon(if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color(0xFF388E3C))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text("Ses Kaydedildi 🎙️", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = {
                                    if (isPlayingPreview) onStopPreview()
                                    hasFinishedRecording = false
                                    isPlayingPreview = false
                                    onCancelRecord()
                                }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // ANOBA ABLASI KARTI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🌙 Anoba Ablası'na Sor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sorunu Rumuz ile sorabilirsin, Anoba Ablası cevaplar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAskAnoba,
                            onCheckedChange = { isAskAnoba = it }
                        )
                    }

                    AnimatedVisibility(visible = isAskAnoba) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pseudonym,
                                onValueChange = { pseudonym = it },
                                label = { Text("Mahlas / Rumuz") },
                                placeholder = { Text("Gizlenmek için bir rumuz yaz...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = answerPrivacy == "PUBLIC",
                                    onClick = { answerPrivacy = "PUBLIC" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AnobaGold)
                                )
                                Text("Herkese Açık", style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { answerPrivacy = "PUBLIC" })

                                Spacer(Modifier.width(16.dp))

                                RadioButton(
                                    selected = answerPrivacy == "SEALED",
                                    onClick = { answerPrivacy = "SEALED" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AnobaGold)
                                )
                                Text("Mühürlü (Gizli)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { answerPrivacy = "SEALED" })
                            }
                        }
                    }
                }
            }

            // SEÇENEKLER
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canSubmit = newLetterContent.isNotBlank() || hasFinishedRecording

                if (isAskAnoba) {
                    SharedOptionRow(
                        text = "Anoba Ablası'na Gönder ✉️",
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = {
                            if (canSubmit) {
                                onSaveLetter(newLetterContent, answerPrivacy, pseudonym, true, hasFinishedRecording)
                            }
                        },
                        isEnabled = canSubmit,
                        isActive = true
                    )
                } else {
                    SharedOptionRow(
                        text = "Herkese Açık Paylaş 🌐",
                        icon = Icons.Default.Public,
                        onClick = {
                            selectedOption = "PUBLIC"
                            if (canSubmit) {
                                onSaveLetter(newLetterContent, "PUBLIC", null, false, hasFinishedRecording)
                            }
                        },
                        isEnabled = canSubmit,
                        isActive = selectedOption == "PUBLIC"
                    )

                    SharedOptionRow(
                        text = "Mühürlü (Kişiye Özel Gönder) 🔒",
                        icon = Icons.Default.Lock,
                        onClick = {
                            selectedOption = "SEALED"
                            if (canSubmit) {
                                onSaveLetter(newLetterContent, "SEALED", null, false, hasFinishedRecording)
                            }
                        },
                        isEnabled = canSubmit,
                        isActive = selectedOption == "SEALED"
                    )

                    SharedOptionRow(
                        text = "Zaman Kapsülü Yap ⏳",
                        icon = Icons.Default.WatchLater,
                        onClick = {
                            selectedOption = "TIMECAPSULE"
                            if (canSubmit) {
                                onSaveLetter(newLetterContent, "PUBLIC", null, false, hasFinishedRecording)
                            }
                        },
                        isEnabled = canSubmit,
                        isActive = selectedOption == "TIMECAPSULE"
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onNavigateBack) {
                    Text("Vazgeç", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SharedOptionRow(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isActive) Color(0xFFBBDEFB) else Color(0xFFECEFF1)
    val contentColor = if (isActive) Color(0xFF0D47A1) else Color(0xFF455A64)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(enabled = isEnabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
