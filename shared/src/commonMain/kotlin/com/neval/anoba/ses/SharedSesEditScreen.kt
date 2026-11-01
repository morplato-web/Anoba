package com.neval.anoba.ses

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSesEditScreen(
    ses: SharedSesModel? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSaveClick: (title: String, note: String, startMs: Long?, endMs: Long?) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val sampleSes = remember(ses) {
        ses ?: SharedSesModel(
            id = "ses_edit_1",
            title = "Gece Sesli Notu 🎙️",
            personalNote = "Düşüncelerimi sesli olarak paylaşmak istedim.",
            durationMillis = 105000L
        )
    }

    var title by remember(sampleSes) { mutableStateOf(sampleSes.title) }
    var personalNote by remember(sampleSes) { mutableStateOf(sampleSes.personalNote) }

    val totalDurationMs = sampleSes.durationMillis.toFloat().coerceAtLeast(1000f)
    var trimRange by remember(sampleSes) { mutableStateOf(0f..totalDurationMs) }
    var isPlaying by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            delay((trimRange.endInclusive - trimRange.start).toLong().coerceAtLeast(500L))
            isPlaying = false
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "✏️ Ses Kaydını Düzenle",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            onSaveClick(
                                title,
                                personalNote,
                                trimRange.start.toLong(),
                                trimRange.endInclusive.toLong()
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Kaydet",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Ana Sayfa",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "✂️ Ses Kırpma Aralığı",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        RangeSlider(
                            value = trimRange,
                            onValueChange = { trimRange = it },
                            valueRange = 0f..totalDurationMs,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(trimRange.start / 1000).toInt()}s",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(trimRange.endInclusive / 1000).toInt()}s",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48))
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Dinle",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Ses Başlığı", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = personalNote,
                            onValueChange = { personalNote = it },
                            label = { Text("Kişisel Not", color = Color.LightGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    }
}
