package com.neval.anoba.letter

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun LetterHomeScreen(
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val firestore: FirebaseFirestore = koinInject()

    val lettersState by viewModel.lettersState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val pinnedIds by viewModel.pinnedIds.collectAsState()

    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val currentUserId = (authState as? AuthState.Authenticated)?.uid ?: ""
    val isAdmin = userRole?.equals("ADMIN", ignoreCase = true) == true

    var unreadLetterIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // CANLI DİNLEYİCİ: Okunmamış bildirimleri çeker
    DisposableEffect(currentUserId) {
        var listener: ListenerRegistration? = null
        if (currentUserId.isNotBlank()) {
            listener = firestore.collection("users").document(currentUserId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val ids = snapshot.documents.mapNotNull { it.getString("letterId") }.toSet()
                        unreadLetterIds = ids
                    }
                }
        }
        onDispose { listener?.remove() }
    }

    // Android LetterModel -> SharedLetterModel Dönüşümü (Projedeki mevcut mapper'ı kullanır)
    val rawLetters = (lettersState as? UiState.Success<List<LetterModel>>)?.data ?: emptyList()
    val sharedLetters: List<SharedLetterModel> = remember(rawLetters) {
        rawLetters.map { it.toSharedModel() }
    }

    val isLoading = isSending || (lettersState is UiState.Loading)
    val errorMessage = (lettersState as? UiState.Error)?.message

    SharedLetterHomeScreen(
        lettersList = sharedLetters,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        pinnedIds = pinnedIds,
        unreadLetterIds = unreadLetterIds,
        currentUserId = currentUserId,
        isAdmin = isAdmin,
        onRefresh = { viewModel.refresh() },
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onCreateLetterClick = {
            navController.navigate("letter_create_screen")
        },
        onLetterClick = { selected ->
            navController.navigate("letter_detail_screen/${selected.id}")
        },
        onCommentClick = { selected ->
            navController.navigate("letter_comments_screen/${selected.id}")
        },
        onPinClick = { letterId ->
            viewModel.togglePin(letterId)
        },
        onShareClick = { letter ->
            viewModel.incrementLetterShareCount(letter.id)
            val shareText = if (!letter.audioUrl.isNullOrBlank()) {
                "${letter.content}\n\nSesli Mektubu Dinle: ${letter.audioUrl}"
            } else {
                letter.content
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        },
        onDeleteClick = { letter ->
            viewModel.deleteLetter(letter.id) { success ->
                val msg = if (success) {
                    "${context.getString(R.string.mycontent_sheet_delete)} ${context.getString(R.string.auth_process_completed)}"
                } else {
                    context.getString(R.string.mycontent_error_delete)
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    )
}