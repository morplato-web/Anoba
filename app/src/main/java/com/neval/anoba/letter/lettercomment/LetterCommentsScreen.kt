package com.neval.anoba.letter.lettercomment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.letter.SharedLetterCommentItem
import com.neval.anoba.letter.SharedLetterCommentsScreen
import com.neval.anoba.login.state.AuthState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun LetterCommentsScreen(
    navController: NavController,
    letterId: String
) {
    val viewModel: LetterCommentViewModel = koinViewModel(parameters = { parametersOf(letterId) })
    val authService: AuthServiceInterface = koinInject()
    val authViewModel: AuthViewModel = koinViewModel()

    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by authService.authStateFlow.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val context = LocalContext.current

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAdmin = userRole?.equals("ADMIN", ignoreCase = true) == true

    val sharedComments = comments.map { c ->
        SharedLetterCommentItem(
            id = c.id,
            userId = c.userId,
            username = c.username,
            userProfileUrl = c.userProfileUrl,
            content = c.content,
            likesCount = c.likedBy.size,
            isLiked = currentUserId?.let { c.likedBy.contains(it) } == true,
            userBadgeType = c.userBadgeType,
            isOfficial = c.isOfficial,
            timeAgo = DateTimeUtils.formatRelativeTime(context, c.safeTimestamp),
            replyToId = c.replyToId
        )
    }

    SharedLetterCommentsScreen(
        letterId = letterId,
        comments = sharedComments,
        isLoading = isLoading,
        currentUserId = currentUserId,
        isAdmin = isAdmin,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onSendComment = { text, replyToId ->
            viewModel.onCommentTextChanged(text)
            if (replyToId != null) {
                val parent = comments.find { it.id == replyToId }
                if (parent != null) viewModel.onReplyClicked(parent)
            }
            viewModel.addComment()
        },
        onLikeComment = { commentId ->
            viewModel.likeComment(commentId)
        },
        onDeleteComment = { commentId ->
            comments.find { it.id == commentId }?.let { viewModel.deleteComment(it) }
        }
    )
}