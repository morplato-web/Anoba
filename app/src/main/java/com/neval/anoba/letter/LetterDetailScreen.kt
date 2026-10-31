package com.neval.anoba.letter

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.letter.lettercomment.LetterCommentViewModel
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRepository
import com.neval.anoba.letter.letteremoji.LetterReactionUsersBottomSheet
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun LetterDetailScreen(
    letterId: String,
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    commentViewModel: LetterCommentViewModel = koinViewModel { parametersOf(letterId) },
) {
    val context = LocalContext.current
    val letterState by viewModel.selectedLetter.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    var showEmojiUsersSheet by remember { mutableStateOf(value = false) }

    val reactionRepository: LetterEmojiReactionRepository = koinInject()
    val reactionUsers by remember(letterId, showEmojiUsersSheet) {
        if (showEmojiUsersSheet && letterId.isNotBlank()) {
            reactionRepository.getLetterReactionUsers(letterId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(letterId) {
        if (letterId.isNotBlank()) {
            viewModel.getLetterDetails(letterId)
            viewModel.incrementLetterViewCount(letterId)
        }
    }

    val currentLetter = (letterState as? UiState.Success<LetterModel>)?.data
    val sharedLetter = remember(currentLetter, comments.size) {
        currentLetter?.toSharedModel()?.copy(
            commentsCount = comments.size.toLong(),
        )
    }

    SharedLetterDetailScreen(
        letter = sharedLetter,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onOpenComments = {
            navController.navigate("letter_comments_screen/$letterId")
        },
        onShareClick = { letterToShare ->
            viewModel.incrementLetterShareCount(letterToShare.id)
            val shareText = if (!letterToShare.audioUrl.isNullOrBlank()) {
                "${letterToShare.content}\n\nSesli Mektubu Dinle: ${letterToShare.audioUrl}"
            } else {
                letterToShare.content
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        },
        onEmojiReaction = { emoji ->
            if (letterId.isNotBlank()) {
                viewModel.toggleReaction(letterId, emoji)
            }
        },
        onOpenEmojiUsersList = {
            showEmojiUsersSheet = true
        },
    )

    if (showEmojiUsersSheet) {
        LetterReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showEmojiUsersSheet = false },
        )
    }
}
