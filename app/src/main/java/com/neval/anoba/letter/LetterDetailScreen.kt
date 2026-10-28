package com.neval.anoba.letter

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.letter.lettercomment.LetterCommentViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LetterDetailScreen(
    letterId: String,
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    commentViewModel: LetterCommentViewModel = koinViewModel { parametersOf(letterId) }
) {
    val context = LocalContext.current
    val letterState by viewModel.selectedLetter.collectAsState()
    val comments by commentViewModel.comments.collectAsState()

    LaunchedEffect(letterId) {
        if (letterId.isNotBlank()) {
            viewModel.getLetterDetails(letterId)
            viewModel.incrementLetterViewCount(letterId)
        }
    }

    val currentLetter = (letterState as? UiState.Success<LetterModel>)?.data
    val sharedLetter = remember(currentLetter, comments.size) {
        currentLetter?.toSharedModel()?.copy(
            commentsCount = comments.size.toLong()
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
        }
    )
}