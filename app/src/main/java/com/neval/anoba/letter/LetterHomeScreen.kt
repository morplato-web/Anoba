package com.neval.anoba.letter

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterHomeScreen(
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val lettersState by viewModel.lettersState.collectAsState()
    val letters = (lettersState as? UiState.Success<List<LetterModel>>)?.data ?: emptyList()
    val isSending by viewModel.isSending.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val pinnedIds by viewModel.pinnedIds.collectAsState()
    val currentUserId = (authState as? AuthState.Authenticated)?.uid ?: ""

    val firestore: FirebaseFirestore = koinInject()
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

    val isAdmin = userRole?.equals("ADMIN", ignoreCase = true) == true
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val showBackToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 4 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.letterhome_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.letterhome_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Home, stringResource(id = R.string.letterhome_home))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(id = R.string.letterhome_all_letters), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSending || (lettersState is UiState.Loading)) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (lettersState is UiState.Error) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = (lettersState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                        }
                    } else if (letters.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(id = R.string.state_no_data))
                        }
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(letters, key = { it.id.ifBlank { UUID.randomUUID().toString() } }) { letter ->
                                val hasUnreadNotification = unreadLetterIds.contains(letter.id)
                                val isMyLetter = currentUserId.isNotBlank() && letter.ownerId == currentUserId
                                val totalReactions = letter.reactions.values.sum()
                                val hasEmojiReaction = isMyLetter && totalReactions > 0

                                LetterCard(
                                    letter = letter,
                                    navController = navController,
                                    currentUserId = currentUserId,
                                    isCommentSectionClickable = true,
                                    cardColor = Color(0xFFFFF3E0),
                                    isPinned = pinnedIds.contains(letter.id),
                                    hasNotification = hasUnreadNotification || hasEmojiReaction, // 🚀 İÇERİYE AKTARILDI
                                    onPinClick = { viewModel.togglePin(letter.id) },
                                    onShareClick = {
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
                                    canDelete = isAdmin,
                                    onDeleteClick = {
                                        viewModel.deleteLetter(letter.id) { success ->
                                            if (success) {
                                                Toast.makeText(context, "${context.getString(R.string.mycontent_sheet_delete)} ${context.getString(R.string.auth_process_completed)}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.mycontent_error_delete), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(0) }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Back to Top")
                }
            }
        }
    }
}