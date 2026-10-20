package com.neval.anoba.ses

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.photo.ShareState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesHomeScreen(
    navController: NavController,
    viewModel: SesViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val seslerState by viewModel.seslerState.collectAsState()
    val pinnedIds by viewModel.pinnedIds.collectAsState()
    val shareState by viewModel.shareState.collectAsState()

    val userRole by authViewModel.userRole.collectAsStateWithLifecycle()
    val isAdmin = userRole.equals("ADMIN", ignoreCase = true)

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val shareChooserTitle = stringResource(id = R.string.sesdetail_share_chooser_title)
    val sharePreparingText = stringResource(id = R.string.sesdetail_share_preparing)

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    LaunchedEffect(shareState) {
        when (val state = shareState) {
            is ShareState.Success -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, state.uri)
                    type = "audio/m4a"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, shareChooserTitle)
                shareLauncher.launch(chooser)
                viewModel.resetShareState()
            }
            is ShareState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetShareState()
            }
            is ShareState.Processing -> {
                Toast.makeText(context, sharePreparingText, Toast.LENGTH_SHORT).show()
            }
            ShareState.Idle -> { }
        }
    }

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val backText = stringResource(id = R.string.seshome_back)
    val homeText = stringResource(id = R.string.seshome_home)
    val loadingText = stringResource(id = R.string.state_loading)
    val noDataText = stringResource(id = R.string.state_no_data)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.seshome_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, backText)
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
                        Icon(Icons.Default.Home, homeText)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.refresh()
                        delay(600)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = seslerState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Text(
                                        text = loadingText,
                                        modifier = Modifier.padding(top = 8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is UiState.Success<List<SesModel>> -> {
                            val sesler = state.data
                            if (sesler.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(noDataText, style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(items = sesler, key = { it.id }) { ses ->
                                        SesCard(
                                            ses = ses,
                                            navController = navController,
                                            isCommentSectionClickable = true,
                                            cardColor = Color(0xFFFFD1DC),
                                            isPinned = pinnedIds.contains(ses.id),
                                            onPinClick = { viewModel.togglePin(ses.id) },
                                            onShareClick = {
                                                if (shareState !is ShareState.Processing) {
                                                    viewModel.shareSes(context, ses.audioUrl, ses.id)
                                                }
                                            },
                                            canDelete = isAdmin,
                                            onDeleteClick = {
                                                viewModel.deleteAudio(ses.id) { success ->
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
                        else -> { }
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
                        scope.launch { listState.animateScrollToItem(0) }
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