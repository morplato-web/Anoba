package com.neval.anoba.letter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedLetterEditScreen(
    letter: SharedLetterModel? = null,
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSaveEdit: (title: String, content: String) -> Unit = { _, _ -> }
) {
    var title by remember(letter?.id) { mutableStateOf(letter?.title ?: "") }
    var content by remember(letter?.id) { mutableStateOf(letter?.content ?: "") }

    // Veri asenkron geldiğinde metin kutularını güvenle doldur
    LaunchedEffect(letter) {
        if (letter != null) {
            title = letter.title ?: ""
            content = letter.content
        }
    }

    val canSave = content.isNotBlank() && !isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✏️ Mektubu Düzenle", fontWeight = FontWeight.Bold, color = AnobaNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (canSave) {
                                onSaveEdit(title.trim(), content.trim())
                            }
                        },
                        enabled = canSave
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kaydet",
                            tint = if (canSave) AnobaNavy else AnobaNavy.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(onClick = onHomeClick) {
                        Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && letter == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mektup Başlığı") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Mektup İçeriği") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 8,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}