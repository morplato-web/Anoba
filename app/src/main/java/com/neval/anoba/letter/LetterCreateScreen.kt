package com.neval.anoba.letter

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel

@Composable
fun LetterCreateScreen(
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
            viewModel.cancelRecording()
        }
    }

    SharedLetterCreateScreen(
        onNavigateBack = {
            viewModel.cancelRecording()
            navController.popBackStack()
        },
        onHomeClick = {
            viewModel.cancelRecording()
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onStartRecord = {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
            viewModel.startRecording(context)
        },
        onStopRecord = {
            viewModel.stopRecording()
        },
        onCancelRecord = {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
            viewModel.cancelRecording()
        },
        onPlayPreview = {
            viewModel.audioFile?.let { file ->
                try {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        start()
                    }
                } catch (_: Exception) {}
            }
        },
        onStopPreview = {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
        },
        onSaveLetter = { content, privacyStr, pseudonym, isAskAnoba, hasAudio ->
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}

            val privacy = if (privacyStr == "SEALED") LetterPrivacy.SEALED else LetterPrivacy.PUBLIC
            val file = if (hasAudio) viewModel.audioFile else null
            val duration = if (hasAudio && file != null) {
                viewModel.getAudioDuration(file.absolutePath)
            } else {
                null
            }

            viewModel.saveLetter(
                content = content,
                privacy = privacy,
                pseudonym = pseudonym ?: "",
                isAskAnoba = isAskAnoba,
                audioFile = file,
                audioDuration = duration,
                onResult = { id ->
                    if (!id.isNullOrBlank()) {
                        Toast.makeText(context, context.getString(R.string.auth_process_completed), Toast.LENGTH_SHORT).show()
                        navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) {
                            popUpTo(Constants.HOME_SCREEN) { inclusive = false }
                        }
                    } else {
                        Toast.makeText(context, "Mektup kaydedilemedi, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    )
}
