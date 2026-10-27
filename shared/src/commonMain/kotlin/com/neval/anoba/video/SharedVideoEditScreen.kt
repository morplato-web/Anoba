package com.neval.anoba.video

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVideoEditScreen(
    video: SharedVideoModel? = null,
    videoDurationMillis: Long = 0L,
    isSaving: Boolean = false,
    isMuted: Boolean = false,
    rotationDegrees: Int = 0,
    sliderRange: ClosedFloatingPointRange<Float> = 0f..(videoDurationMillis.toFloat().coerceAtLeast(1f)),
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onToggleMute: () -> Unit = {},
    onRotateVideo: () -> Unit = {},
    onSliderRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit = {},
    onSaveQuickText: (title: String, personalNote: String) -> Unit = { _, _ -> },
    onApplyTrimAndSave: (title: String, personalNote: String, startMs: Long, endMs: Long) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier,
    videoPlayerContent: (@Composable () -> Unit)? = null
) {
    var title by remember(video) { mutableStateOf(video?.title ?: "") }
    var personalNote by remember(video) { mutableStateOf(video?.personalNote ?: "") }
    var localSliderRange by remember(sliderRange) { mutableStateOf(sliderRange) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✏️ Videoyu Düzenle", fontWeight = FontWeight.Bold, color = AnobaNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSaveQuickText(title, personalNote) },
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Metinleri Kaydet", tint = AnobaNavy)
                    }
                    IconButton(onClick = onHomeClick) {
                        Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Video Önizleme Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = rotationDegrees.toFloat() },
                    contentAlignment = Alignment.Center
                ) {
                    if (videoPlayerContent != null) {
                        videoPlayerContent()
                    } else if (video != null && video.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = video.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("🎬 Video Önizleme", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kırpma (Trim) Aralığı
            if (videoDurationMillis > 0L) {
                RangeSlider(
                    value = localSliderRange,
                    onValueChange = { range -> localSliderRange = range },
                    onValueChangeFinished = { onSliderRangeChanged(localSliderRange) },
                    valueRange = 0f..videoDurationMillis.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDurationMillis(localSliderRange.start.toLong()), fontSize = 12.sp, color = Color.Gray)
                    Text(formatDurationMillis(localSliderRange.endInclusive.toLong()), fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Döndürme ve Sessiz Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ses Aç/Kapat"
                    )
                }
                IconButton(onClick = onRotateVideo) {
                    Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = "Döndür")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Başlık & Not Form Alanları
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Video Başlığı") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = personalNote,
                onValueChange = { personalNote = it },
                label = { Text("Açıklama / Kişisel Not") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Uygula & Kaydet (Kırpma + Güncelleme)
            Button(
                onClick = {
                    onApplyTrimAndSave(
                        title,
                        personalNote,
                        localSliderRange.start.toLong(),
                        localSliderRange.endInclusive.toLong()
                    )
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Uygula ve Kaydet", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}