package com.neval.anoba.ses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AnobaBackgroundDark = Color(0xFF0F172A)
private val AnobaTopBarNavy = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSesHomeScreen(
    sesler: List<SharedSesModel> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isRefreshing: Boolean = false,
    isAdmin: Boolean = false,
    pinnedIds: Set<String> = emptySet(),
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSesClick: (SharedSesModel) -> Unit = {},
    onCommentClick: (SharedSesModel) -> Unit = {},
    onPinClick: (String) -> Unit = {},
    onDeleteClick: (SharedSesModel) -> Unit = {},
    onShareClick: (SharedSesModel) -> Unit = {},
    onReactionClick: (SharedSesModel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    Scaffold(
        containerColor = AnobaBackgroundDark,
        topBar = {
            Surface(
                color = AnobaTopBarNavy,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "🎙️ Sesli Notlar Akışı",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Ana Sayfa",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && sesler.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Yükleniyor...",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    errorMessage != null && sesler.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                    }

                    sesler.isEmpty() && !isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz paylaşılan ses kaydı bulunmuyor.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(sesler, key = { it.id }) { ses ->
                                SharedSesCard(
                                    ses = ses,
                                    isPinned = pinnedIds.contains(ses.id),
                                    canDelete = isAdmin,
                                    onPinClick = { onPinClick(ses.id) },
                                    onDeleteClick = { onDeleteClick(ses) },
                                    onShareClick = { onShareClick(ses) },
                                    onSesClick = { onSesClick(ses) },
                                    onCommentClick = { onCommentClick(ses) },
                                    onLikeClick = { onReactionClick(ses) }
                                )
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
                        .padding(end = 16.dp, bottom = 24.dp)
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
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Başa Dön"
                        )
                    }
                }
            }
        }
    }
}
