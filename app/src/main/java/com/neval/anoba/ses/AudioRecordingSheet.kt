package com.neval.anoba.ses

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ContextCast")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AudioRecordingSheet(
    viewModel: SesViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onRecordingComplete: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var hasFinishedRecording by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (_: Exception) {}
            mediaPlayer.release()
        }
    }

    val permissionNeededText = stringResource(id = R.string.audiorecording_permission_needed)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startRecording(context)
                isRecording = true
                hasFinishedRecording = false
            } else {
                Toast.makeText(context, permissionNeededText, Toast.LENGTH_SHORT).show()
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = when {
                    isLoading -> stringResource(id = R.string.audiorecording_status_loading)
                    isRecording -> stringResource(id = R.string.audiorecording_status_recording)
                    hasFinishedRecording -> stringResource(id = R.string.audiorecording_status_completed)
                    else -> stringResource(id = R.string.audiorecording_status_create)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                CircularProgressIndicator()
            } else if (!isRecording && !hasFinishedRecording) {
                RecordButton(isRecording = false, onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) })
                Text(
                    stringResource(id = R.string.audiorecording_start_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isRecording) {
                RecordButton(isRecording = true, onClick = {
                    viewModel.stopRecording()
                    isRecording = false
                    hasFinishedRecording = true
                })
                Text(
                    stringResource(id = R.string.audiorecording_stop_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // --- KAYIT BİTTİ: SEÇENEK EKRANI ---
                var showDatePicker by remember { mutableStateOf(false) }
                var unlockTimestamp by remember { mutableStateOf<Long?>(null) }
                var showRecipientDialog by remember { mutableStateOf(false) }
                val allUsers by viewModel.allUsers.collectAsState()

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()

                    Dialog(
                        onDismissRequest = { showDatePicker = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    title = null,
                                    headline = null,
                                    showModeToggle = false
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = Color.LightGray
                                )

                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // HERKESE AÇIK (Takvim İçi)
                                    com.neval.anoba.letter.OptionRow(
                                        text = "Herkese Açık",
                                        icon = Icons.Default.Public,
                                        onClick = {
                                            val date = datePickerState.selectedDateMillis
                                            if (date != null) {
                                                viewModel.audioFile?.let { file ->
                                                    isLoading = true
                                                    viewModel.uploadAudioToFirebase(file, "PUBLIC", date) { id ->
                                                        if (id != null) onRecordingComplete()
                                                        isLoading = false
                                                    }
                                                }
                                                showDatePicker = false
                                            } else {
                                                Toast.makeText(context, "Lütfen bir tarih seçin", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )

                                    // MÜHÜRLÜ (Takvim İçi)
                                    com.neval.anoba.letter.OptionRow(
                                        text = "Mühürlü (Seçilen Kişi)",
                                        icon = Icons.Default.Lock,
                                        onClick = {
                                            val date = datePickerState.selectedDateMillis
                                            if (date != null) {
                                                unlockTimestamp = date
                                                showDatePicker = false
                                                showRecipientDialog = true
                                            } else {
                                                Toast.makeText(context, "Lütfen bir tarih seçin", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )

                                    // İptal Butonu
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showDatePicker = false }) {
                                            Text("İptal", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (showRecipientDialog) {
                    com.neval.anoba.letter.LetterRecipientSelectionDialog(
                        allUsers = allUsers.filter { it.uid != (viewModel.authService.authStateFlow.value as? com.neval.anoba.login.state.AuthState.Authenticated)?.uid },
                        searchQuery = "",
                        onSearchQueryChange = {},
                        onUserSelected = { selectedUser ->
                            viewModel.audioFile?.let { file ->
                                isLoading = true
                                viewModel.uploadAudioToFirebase(file, "SEALED", unlockTimestamp) { id ->
                                    if (id != null) {
                                        viewModel.viewModelScope.launch {
                                            viewModel.sesRepository.updateSes(id, mapOf("recipientId" to selectedUser.uid, "recipientUsername" to selectedUser.displayName))
                                            onRecordingComplete()
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                            showRecipientDialog = false
                        },
                        onDismiss = { showRecipientDialog = false }
                    )
                }

                // --- OYNAT BUTONU (Çemberli) ---
                IconButton(
                    onClick = {
                        if (isPlayingPreview) {
                            mediaPlayer.pause()
                            isPlayingPreview = false
                        } else {
                            viewModel.audioFile?.let { file ->
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(file.absolutePath)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlayingPreview = true
                                mediaPlayer.setOnCompletionListener { isPlayingPreview = false }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.dp, AnobaNavy.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                ) {
                    Icon(
                        imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = AnobaNavy
                    )
                }

                // --- ŞIKLAR SIRALAMASI: 1. Herkese Açık -> 2. Mühürlü -> 3. Zaman Kapsülü ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. HERKESE AÇIK
                    com.neval.anoba.letter.OptionRow(
                        text = if (unlockTimestamp == null) "Herkese Açık" else "Açık Zaman Kapsülü",
                        icon = Icons.Default.Public,
                        onClick = {
                            viewModel.audioFile?.let {
                                isLoading = true
                                viewModel.uploadAudioToFirebase(it, "PUBLIC", unlockTimestamp) { id ->
                                    if (id != null) onRecordingComplete()
                                    isLoading = false
                                }
                            }
                        }
                    )

                    // 2. MÜHÜRLÜ (KİŞİYE ÖZEL)
                    com.neval.anoba.letter.OptionRow(
                        text = if (unlockTimestamp == null) "Mühürlü (Kişiye Özel)" else "Özel Zaman Kapsülü",
                        icon = Icons.Default.Lock,
                        onClick = { showRecipientDialog = true }
                    )

                    // 3. ZAMAN KAPSÜLÜ MODU
                    com.neval.anoba.letter.OptionRow(
                        text = if (unlockTimestamp == null) "Zaman Kapsülü" else "Zaman Kapsülü: " + SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(unlockTimestamp!!)),
                        icon = Icons.Default.WatchLater,
                        onClick = { showDatePicker = true },
                        isActive = unlockTimestamp != null,
                        showClose = unlockTimestamp != null,
                        onCloseClick = { unlockTimestamp = null }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        viewModel.cancelRecording()
                        onDismiss()
                    }) {
                        Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecordButton(isRecording: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .border(2.dp, color, RoundedCornerShape(50.dp)),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isRecording) 36.dp else 24.dp)
                .background(color, shape = RoundedCornerShape(if (isRecording) 8.dp else 50.dp))
        )
    }
}