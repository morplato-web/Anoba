package com.neval.anoba.chat.ui

import com.neval.anoba.chat.data.Messageable

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.neval.anoba.R
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onAudioRecorded: (File, Long) -> Unit,
    focusRequester: FocusRequester,
    bringIntoViewRequester: BringIntoViewRequester,
    replyingTo: Messageable? = null,
    onCancelReply: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    val recorder = remember { AudioRecorder(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = recorder.start()
            if (started) {
                isRecording = true
            } else {
                Toast.makeText(context, context.getString(R.string.chat_recording_failed_system), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.chat_microphone_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(recorder) {
        onDispose {
            recorder.stop()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0L
            while (isRecording) {
                delay(1000)
                recordingDuration += 1000
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Column {
            if (replyingTo != null) {
                Box(modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, end = 60.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(50.dp)
                                .background(AnobaNavy)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = AnobaNavy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).padding(vertical = 6.dp)) {
                            Text(
                                text = replyingTo.senderName ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AnobaNavy
                            )
                            Text(
                                text = replyingTo.content ?: stringResource(R.string.chat_media_message),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { onCancelReply?.invoke() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel), tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { if (!isRecording) onValueChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            if (isRecording) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${stringResource(R.string.chat_audio_recording)} ${recordingDuration / 1000}s",
                                        color = Color.Red,
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                Text(stringResource(R.string.chat_input_placeholder), color = Color.Gray, fontSize = 15.sp)
                            }
                        },
                        maxLines = 5,
                        readOnly = isRecording,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSendClick() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = AnobaNavy
                        ),
                        leadingIcon = { },
                        trailingIcon = {
                            if (!isRecording) {
                                IconButton(onClick = onAttachmentClick) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.chat_attachment_description), tint = Color.Gray, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                val canSend = value.isNotBlank()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.Red else AnobaNavy)
                        .clickable {
                            if (canSend) {
                                onSendClick()
                            } else {
                                if (isRecording) {
                                    val file = recorder.stop()
                                    if (file != null) onAudioRecorded(file, recordingDuration)
                                    isRecording = false
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        return@clickable
                                    }

                                    val started = recorder.start()
                                    if (started) {
                                        isRecording = true
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.chat_recording_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            canSend -> Icons.AutoMirrored.Filled.Send
                            isRecording -> Icons.Default.Stop
                            else -> Icons.Default.Mic
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = if (canSend) 4.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyQuote(
    replyToName: String?,
    replyToContent: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(AnobaNavy)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = replyToName ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AnobaNavy
            )
            Text(
                text = replyToContent ?: stringResource(R.string.chat_media),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun EditMessageIndicator(
    message: Messageable?,
    onCancel: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = AnobaNavy, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.chathome_edit_message),
                    fontWeight = FontWeight.Bold,
                    color = AnobaNavy,
                    fontSize = 13.sp
                )
                Text(
                    text = message?.content ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AudioMessagePlayer(
    audioUrl: String,
    duration: Long
) {
    val context = LocalContext.current
    val exoPlayer = remember(audioUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl.toUri()))
            prepare()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    val waveformAmplitudes = remember(audioUrl) {
        val random = Random(audioUrl.hashCode().toLong())
        List(24) { random.nextFloat() }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
            while (isPlaying) {
                currentPosition = exoPlayer.currentPosition
                delay(100L)
            }
        } else {
            exoPlayer.pause()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPosition = 0L
                    exoPlayer.seekTo(0)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .widthIn(min = 200.dp, max = 250.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .background(if (isPlaying) AnobaNavy.copy(alpha = 0.1f) else AnobaNavy, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isPlaying) AnobaNavy else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                val playedBars = (waveformAmplitudes.size * progress).toInt()
                waveformAmplitudes.forEachIndexed { index, amplitude ->
                    val barColor = if (index < playedBars) AnobaNavy else Color.Gray.copy(alpha = 0.3f)
                    Box(modifier = Modifier.width(3.dp).height((6 + 18 * amplitude).dp).background(barColor, RoundedCornerShape(2.dp)))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatDuration(duration), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.Gray)
            }
        }
    }
}

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    fun start(): Boolean {
        return try {
            outputFile = File.createTempFile("audio", ".m4a", context.cacheDir)
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "Failed to start recording", e)
            false
        }
    }

    fun stop(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "Failed to stop recording", e)
            null
        }
    }
}