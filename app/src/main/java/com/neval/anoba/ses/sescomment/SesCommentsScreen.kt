package com.neval.anoba.ses.sescomment

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ses.SharedSesCommentModel
import com.neval.anoba.ses.SharedSesCommentsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesCommentsScreen(
    navController: NavController,
    sesId: String
) {
    val viewModel: SesCommentViewModel = koinViewModel(parameters = { parametersOf(sesId) })
    val authService: AuthServiceInterface = koinInject()

    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by authService.authStateFlow.collectAsState()
    val userRole by koinViewModel<AuthViewModel>().userRole.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAdmin = userRole == "ADMIN"

    val sharedComments = comments.map { c ->
        SharedSesCommentModel(
            id = c.id,
            sesId = c.sesId,
            userId = c.userId,
            username = c.username,
            userProfileUrl = c.userProfileUrl,
            content = c.content,
            timestampMillis = c.timestamp?.time ?: 0L,
            likedBy = c.likedBy,
            replyToId = c.replyToId,
            isOfficial = c.isOfficial,
            userBadgeType = c.userBadgeType
        )
    }

    SharedSesCommentsScreen(
        sesId = sesId,
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
