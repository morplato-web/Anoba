package com.neval.anoba.ses

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.formatDuration
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesEditScreen(
    navController: NavController,
    sesId: String,
    viewModel: SesEditViewModel = koinViewModel { parametersOf(sesId) }
) {
    val sesState by viewModel.sesState.collectAsState()
    var title by remember { mutableStateOf("") }
    var personalNote by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var trimRange by remember { mutableStateOf(0f..1f) }
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }
    var totalDuration by remember { mutableLongStateOf(0L) }

    val playerErrorTemplate = stringResource(id = R.string.sesedit_player_error, "")
    val updateSuccessText = stringResource(id = R.string.sesedit_success)

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (mediaPlayer.isPlaying) {
                if (mediaPlayer.currentPosition >= trimRange.endInclusive.toInt()) {
                    mediaPlayer.pause()
                    isPlaying = false
                    mediaPlayer.seekTo(trimRange.start.toInt())
                }
                delay(100)
            }
        }
    }

    LaunchedEffect(sesState) {
        when (val state = sesState) {
            is SesEditState.Success -> {
                val ses = state.ses
                title = ses.title ?: ""
                personalNote = ses.personalNote
                if (totalDuration == 0L) {
                    totalDuration = ses.duration
                    val start = ses.trimStartMs?.toFloat() ?: 0f
                    val end = ses.trimEndMs?.toFloat() ?: ses.duration.toFloat()
                    if (ses.duration > 0) {
                        trimRange = start..end
                    }
                }
                if (!mediaPlayer.isPlaying && mediaPlayer.currentPosition == 0) {
                    try {
                        if (mediaPlayer.isPlaying) mediaPlayer.stop()
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(ses.audioUrl)
                        mediaPlayer.prepareAsync()
                    } catch (e: IOException) {
                        Toast.makeText(context, playerErrorTemplate.format(e.message ?: "Unknown"), Toast.LENGTH_LONG).show()
                    }
                }
            }
            SesEditState.UpdateSuccess -> {
                Toast.makeText(context, updateSuccessText, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is SesEditState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        mediaPlayer.setOnErrorListener { _, what, extra ->
            Toast.makeText(context, playerErrorTemplate.format("What: $what, Extra: $extra"), Toast.LENGTH_SHORT).show()
            true
        }
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            mediaPlayer.seekTo(trimRange.start.toInt())
        }
        onDispose { mediaPlayer.release() }
    }

    fun togglePlayback() {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            if (mediaPlayer.duration > trimRange.start.toInt()) {
                mediaPlayer.seekTo(trimRange.start.toInt())
                mediaPlayer.start()
            }
        }
        isPlaying = !isPlaying
    }

    fun onSliderValueChange(newRange: ClosedFloatingPointRange<Float>) {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
        trimRange = newRange
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.sesedit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val finalStart = if (trimRange.start.toLong() == 0L) null else trimRange.start.toLong()
                        val finalEnd = if (trimRange.endInclusive.toLong() == totalDuration) null else trimRange.endInclusive.toLong()
                        viewModel.updateSesDetails(title, personalNote, finalStart, finalEnd)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        when (sesState) {
            is SesEditState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(id = R.string.sesedit_trim_label), style = MaterialTheme.typography.titleMedium)
                    RangeSlider(
                        value = trimRange,
                        onValueChange = { onSliderValueChange(it) },
                        valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(1f)),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = totalDuration > 0
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatDuration(trimRange.start.toLong()))
                        Text(formatDuration(trimRange.endInclusive.toLong()))
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { togglePlayback() }) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(id = R.string.sesedit_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = personalNote,
                        onValueChange = { personalNote = it },
                        label = { Text(stringResource(id = R.string.sesedit_note_label)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
