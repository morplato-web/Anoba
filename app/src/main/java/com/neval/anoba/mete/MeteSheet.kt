package com.neval.anoba.mete

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.MediaCacheManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun MeteScreen(navController: NavController) {
    val meteSesViewModel: MeteSesViewModel = koinViewModel()
    val sectionGroups by meteSesViewModel.sectionGroups.collectAsState()
    val storyVideos by meteSesViewModel.storyVideos.collectAsState()
    val selectedLang by meteSesViewModel.selectedLanguage.collectAsState()
    val unlockedSections by meteSesViewModel.unlockedSections.collectAsState()
    val isSystemAudioPlaying by meteSesViewModel.isSystemAudioPlaying.collectAsState()
    val context = LocalContext.current

    val kmpLanguage = SharedMeteLanguage.entries.find { it.name == selectedLang.name } ?: SharedMeteLanguage.TR

    SharedMeteHomeScreen(
        sectionGroups = sectionGroups.map { group ->
            SharedMeteSectionGroup(
                sectionName = group.sectionName,
                words = group.words.map {
                    SharedMeteWordItem(
                        id = it.id,
                        word = group.sectionName,
                        sectionName = it.sectionName,
                        boxColor = it.boxColor ?: Color(0xFF80DEEA),
                        imageUrl = it.imageUrl,
                        videoUrl = it.videoUrl,
                        audioUrl = it.audioUrl
                    )
                }
            )
        },
        storyVideos = storyVideos.map { story ->
            SharedMeteStoryItem(
                id = story.title,
                title = story.title,
                imageUrl = story.imageUrl,
                videoUrl = story.videoUrl,
                isUnlocked = unlockedSections.contains(story.title) || story.title.contains("1")
            )
        },
        selectedLanguage = kmpLanguage,
        isSystemAudioPlaying = isSystemAudioPlaying,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onSelectLanguage = { kmpLang ->
            MeteLanguage.entries.find { it.name == kmpLang.name }?.let { meteSesViewModel.selectLanguage(it) }
        },
        onPlayWordAudio = { item ->
            item.audioUrl?.let { meteSesViewModel.playSound(context, it) }
        },
        onStopAudio = { meteSesViewModel.stopSound() },
        onAddNewSection = { name -> meteSesViewModel.addNewSection(name) },
        onUnlockSectionStory = { section -> meteSesViewModel.unlockSection(context, section) }
    )
}

@Composable
fun MeteSheet() {
    val meteSesViewModel: MeteSesViewModel = koinViewModel()

    val sectionGroups by meteSesViewModel.sectionGroups.collectAsState()
    val storyVideos by meteSesViewModel.storyVideos.collectAsState()
    val allPuzzlesForGame by meteSesViewModel.allPuzzlesForGame.collectAsState()
    val selectedLang by meteSesViewModel.selectedLanguage.collectAsState()
    val unlockedSections by meteSesViewModel.unlockedSections.collectAsState()
    val context = LocalContext.current

    var isGameMode by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedWordItem by remember { mutableStateOf<MeteWordItem?>(null) }
    var selectedStoryItem by remember { mutableStateOf<MeteStoryItem?>(null) }
    var showCreateSectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        meteSesViewModel.startLoading()
        meteSesViewModel.loadUnlockedSections(context)
    }

    LaunchedEffect(key1 = selectedLang) {
        isGameMode = false
        searchQuery = ""
        isSearchActive = false
    }

    val filteredSectionGroups = remember(sectionGroups, searchQuery) {
        if (searchQuery.isBlank()) sectionGroups
        else sectionGroups.filter { it.sectionName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredStoryVideos = remember(storyVideos, searchQuery) {
        if (searchQuery.isBlank()) storyVideos
        else storyVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    selectedWordItem?.let { item ->
        PuzzleDetailDialog(item = item, onDismiss = { selectedWordItem = null }, viewModel = meteSesViewModel)
    }

    selectedStoryItem?.let { story ->
        StoryVideoPlayerDialog(storyItem = story, onDismiss = { selectedStoryItem = null })
    }

    if (showCreateSectionDialog) {
        CreateSectionDialog(
            onDismiss = { showCreateSectionDialog = false },
            onConfirm = { sectionName ->
                showCreateSectionDialog = false
                meteSesViewModel.addNewSection(sectionName)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F8FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.mete_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) searchQuery = ""
                }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = if (isSearchActive) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { isGameMode = !isGameMode }) {
                    Icon(
                        imageVector = if (isGameMode) Icons.Default.Close else Icons.Default.SportsEsports,
                        contentDescription = "Oyun Modu",
                        tint = if (isGameMode) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (isSearchActive) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.mete_search_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LanguageSelector(
            currentLang = selectedLang,
            onLangSelected = { meteSesViewModel.selectLanguage(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

        if (isGameMode) {
            MeteGameMode(allPuzzlesForGame, meteSesViewModel)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                // İKİ SÜTUNLU LİSTE ALANI (Kalan boşluğu doldurur)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SOL SÜTUN (%58): Kelime Bölümleri
                    LazyColumn(
                        modifier = Modifier.weight(1.2f).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredSectionGroups) { group ->
                            SectionCardModule(
                                group = group,
                                onWordClick = { selectedWordItem = it }
                            )
                        }
                    }

                    // SAĞ SÜTUN (%42): Çizgi Film / Animasyon Kartları
                    LazyColumn(
                        modifier = Modifier.weight(0.8f).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredStoryVideos) { story ->
                            val isUnlocked = unlockedSections.contains(story.title) || story.title == "1" || story.title.contains("1")
                            MeteStoryVideoCard(
                                item = story,
                                isUnlocked = isUnlocked,
                                onClick = {
                                    if (isUnlocked) {
                                        selectedStoryItem = story
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Bölüm videosunu izlemek için oyun testini tamamlayın! 🔓",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }

                // SAYFANIN EN ALTINDAKİ ZARİF BUTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = { showCreateSectionDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.mete_add_new_section),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCardModule(
    group: MeteSectionGroup,
    onWordClick: (MeteWordItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.sectionName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD),
                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
            )

            group.words.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            PuzzleCard(item = item, onClick = { onWordClick(item) })
                        }
                    }
                    val emptySlots = 3 - rowItems.size
                    if (emptySlots > 0) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateSectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (sectionName: String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(id = R.string.mete_add_new_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(id = R.string.mete_section_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.common_cancel))
                    }
                    Button(
                        onClick = { if (text.isNotBlank()) onConfirm(text) },
                        enabled = text.isNotBlank()
                    ) {
                        Text(stringResource(id = R.string.mete_create))
                    }
                }
            }
        }
    }
}

@Composable
fun MeteStoryVideoCard(
    item: MeteStoryItem,
    isUnlocked: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFFE0F2F1) else Color(0xFFECEFF1))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isUnlocked) Color(0xFF80CBC4) else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (isUnlocked) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isUnlocked) item.title else "🔒 ${item.title}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color(0xFF004D40) else Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun StoryVideoPlayerDialog(
    storyItem: MeteStoryItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val videoSource = storyItem.videoUrl
        ?: if (storyItem.videoResId != null) "android.resource://${context.packageName}/${storyItem.videoResId}" else null

    var isBuffering by remember { mutableStateOf(false) }

    val exoPlayer = remember(context, videoSource) {
        if (!videoSource.isNullOrBlank()) {
            MediaCacheManager.createPlayer(context).apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                    }
                })
                setMediaItem(MediaItem.fromUri(videoSource.toUri()))
                prepare()
                playWhenReady = true
            }
        } else null
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.stop()
            exoPlayer?.release()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = storyItem.title.ifBlank { stringResource(id = R.string.mete_story_fallback_title) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Transparent, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                // 🚀 Lint uyarısını önlemek için FrameLayout kullanıyoruz
                                val parent = android.widget.FrameLayout(ctx)
                                val view = android.view.LayoutInflater.from(ctx).inflate(R.layout.transparent_player_view, parent, false) as PlayerView
                                view.apply {
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    this.player = exoPlayer
                                    useController = true
                                    setKeepContentOnPlayerReset(true)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (isBuffering) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(stringResource(id = R.string.common_close))
                }
            }
        }
    }
}

@Composable
fun PuzzleCard(item: MeteWordItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = item.boxColor ?: Color(0xFF80DEEA))
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun MeteGameMode(puzzles: List<MeteWordItem>, viewModel: MeteSesViewModel) {
    val context = LocalContext.current

    val allItems = remember(puzzles) {
        puzzles.filter { (it.audioUrl != null) || (it.soundResName != null) }
    }

    var options by remember { mutableStateOf<List<MeteWordItem>>(emptyList()) }
    var targetItem by remember { mutableStateOf<MeteWordItem?>(null) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var isAnsweredCorrectly by remember { mutableStateOf(false) }
    var hasListened by remember { mutableStateOf(false) }

    val isSystemAudioPlaying by viewModel.isSystemAudioPlaying.collectAsState()

    fun startNewRound() {
        if (allItems.size < 4) return
        val shuffled = allItems.shuffled()
        options = shuffled.take(4)
        targetItem = options.random()
        selectedItemId = null
        isAnsweredCorrectly = false
        hasListened = false
    }

    LaunchedEffect(allItems.size) {
        if (options.isEmpty() && allItems.isNotEmpty()) {
            startNewRound()
        }
    }

    if (allItems.size < 4) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.mete_game_insufficient_content),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (isSystemAudioPlaying) Color.Red.copy(alpha = 0.1f) else Color.White)
                .border(
                    width = 2.dp,
                    color = if (isSystemAudioPlaying) Color.Red else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable {
                    targetItem?.let { item ->
                        (item.audioUrl ?: item.soundResName)?.let { s ->
                            viewModel.playSound(context, s)
                            hasListened = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSystemAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Dinle",
                modifier = Modifier.size(40.dp),
                tint = if (isSystemAudioPlaying) Color.Red else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when {
                isAnsweredCorrectly -> stringResource(id = R.string.mete_game_correct)
                !hasListened -> stringResource(id = R.string.mete_game_listen)
                else -> stringResource(id = R.string.mete_game_find_card)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isAnsweredCorrectly) Color(0xFF4CAF50) else Color(0xFF232066)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        val isSelected = selectedItemId == item.id
                        val isCorrect = item.imageUrl == targetItem?.imageUrl && item.imageResId == targetItem?.imageResId

                        val borderColor = when {
                            isSelected && isCorrect -> Color(0xFF4CAF50)
                            isSelected && !isCorrect -> Color.Red
                            else -> Color.Transparent
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !isAnsweredCorrectly && hasListened) {
                                    selectedItemId = item.id
                                    if (isCorrect) {
                                        isAnsweredCorrectly = true
                                        val targetSection = targetItem?.sectionName ?: "1"
                                        viewModel.unlockSection(context, targetSection)
                                        android.widget.Toast.makeText(
                                            context,
                                            "Tebrikler! Bölüm $targetSection Videosunun Kilidi Açıldı! 🎉🔓",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(if (isSelected) 10.dp else 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasListened) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GamePuzzleImage(item)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (isAnsweredCorrectly) {
            Button(
                onClick = { startNewRound() },
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    text = stringResource(id = R.string.mete_game_continue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
fun GamePuzzleImage(item: MeteWordItem) {
    when {
        !item.imageUrl.isNullOrBlank() -> {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        item.imageResId != null -> {
            Image(
                painter = painterResource(id = item.imageResId!!),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        else -> {
            Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
        }
    }
}

@Composable
fun LanguageSelector(currentLang: MeteLanguage, onLangSelected: (MeteLanguage) -> Unit) {
    val languages = listOf(
        Pair("🇹🇷", MeteLanguage.TR),
        Pair("🇬🇧", MeteLanguage.EN),
        Pair("🇫🇷", MeteLanguage.FR),
        Pair("🇩🇪", MeteLanguage.DE),
        Pair("🇸🇦", MeteLanguage.AR),
        Pair("🇪🇸", MeteLanguage.ES),
        Pair("🇮🇹", MeteLanguage.IT),
        Pair("🇯🇵", MeteLanguage.JP),
        Pair("🇷🇺", MeteLanguage.RU),
        Pair("🇨🇳", MeteLanguage.ZH),
        Pair("🇵🇹", MeteLanguage.PT),
        Pair("🇰🇷", MeteLanguage.KO),
        Pair("🇮🇳", MeteLanguage.HI),
        Pair("🇰🇪", MeteLanguage.SW),
        Pair("🇿🇦", MeteLanguage.AF)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(languages.size) { index ->
            val langInfo = languages[index]
            val isSelected = langInfo.second == currentLang

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 52.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.LightGray.copy(alpha = 0.4f)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onLangSelected(langInfo.second) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = langInfo.first,
                    fontSize = if (isSelected) 24.sp else 20.sp,
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PuzzleDetailDialog(item: MeteWordItem, onDismiss: () -> Unit, viewModel: MeteSesViewModel) {
    val context = LocalContext.current
    val videoSource = item.videoUrl
        ?: if (item.videoResId != null) "android.resource://${context.packageName}/${item.videoResId}" else null
    var isBuffering by remember { mutableStateOf(false) }

    val exoPlayer = remember(context, videoSource) {
        if (!videoSource.isNullOrBlank()) {
            MediaCacheManager.createPlayer(context).apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                    }
                })
                setMediaItem(MediaItem.fromUri(videoSource.toUri()))
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
            }
        } else null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    var isRecording by remember { mutableStateOf(false) }
    var isPlayingUserAudio by remember { mutableStateOf(false) }
    var userAudioFile by remember { mutableStateOf<File?>(null) }

    val isSystemAudioPlaying by viewModel.isSystemAudioPlaying.collectAsState()

    val audioRecorder = remember { MeteAudioRecorder(context) }
    val audioPlayer = remember { MeteAudioPlayer(context) }
    val recordingFile = remember(item.id) { File(context.cacheDir, "record_${item.id}.m4a") }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.stop()
            exoPlayer?.release()
            audioPlayer.stop()
            audioRecorder.stop()
            viewModel.stopSound()
            if (recordingFile.exists()) {
                recordingFile.delete()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !videoSource.isNullOrBlank() && exoPlayer != null -> {
                            Box(contentAlignment = Alignment.Center) {
                                AndroidView(
                                    factory = { ctx ->
                                        // 🚀 Lint uyarısını önlemek için FrameLayout kullanıyoruz
                                        val parent = android.widget.FrameLayout(ctx)
                                        val view = android.view.LayoutInflater.from(ctx).inflate(R.layout.transparent_player_view, parent, false) as PlayerView
                                        view.apply {
                                            layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            this.player = exoPlayer
                                            useController = false
                                            setKeepContentOnPlayerReset(true)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isBuffering) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }
                        !item.animationUrl.isNullOrBlank() -> {
                            val composition by rememberLottieComposition(LottieCompositionSpec.Url(item.animationUrl!!))
                            LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(150.dp))
                        }
                        item.animationResId != null -> {
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(item.animationResId!!))
                            LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(150.dp))
                        }
                        !item.imageUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = item.imageResId ?: R.drawable.ay_yildiz),
                                contentDescription = null,
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isSystemAudioPlaying) {
                            viewModel.stopSound()
                        } else {
                            val s = item.audioUrl ?: item.soundResName
                            if (s != null) viewModel.playSound(context, s)
                        }
                    }) {
                        Icon(
                            imageVector = if (isSystemAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = stringResource(id = if (isSystemAudioPlaying) R.string.common_stop else R.string.common_listen),
                            modifier = Modifier.size(40.dp),
                            tint = if (isSystemAudioPlaying) Color.Red else Color(0xFF4CAF50)
                        )
                    }

                    if (userAudioFile == null) {
                        val scope = rememberCoroutineScope()

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color.Red else MaterialTheme.colorScheme.secondaryContainer)
                                .clickable {
                                    if (isRecording) {
                                        scope.launch {
                                            delay(300)
                                            audioRecorder.stop()
                                            userAudioFile = if (recordingFile.exists() && recordingFile.length() > 10) recordingFile else null
                                            isRecording = false
                                        }
                                    } else {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            viewModel.stopSound()
                                            audioRecorder.start(recordingFile)
                                            isRecording = true
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = stringResource(id = R.string.common_record),
                                modifier = Modifier.size(32.dp),
                                tint = if (isRecording) Color.White else Color(0xFF2196F3)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = {
                                if (isPlayingUserAudio) {
                                    audioPlayer.stop()
                                    isPlayingUserAudio = false
                                } else {
                                    audioPlayer.play(userAudioFile!!) { isPlayingUserAudio = false }
                                    isPlayingUserAudio = true
                                }
                            }) {
                                Icon(
                                    imageVector = if (isPlayingUserAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                audioPlayer.stop()
                                isPlayingUserAudio = false
                                userAudioFile?.delete()
                                userAudioFile = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sil",
                                    modifier = Modifier.size(36.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(stringResource(id = R.string.common_close))
                }
            }
        }
    }
}

class MeteAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var isRecording = false

    fun start(file: File) {
        if (isRecording) return
        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
                isRecording = true
            }
        } catch (e: Exception) {
            Log.e("MeteAudioRecorder", "start failed", e)
            isRecording = false
            recorder?.release()
            recorder = null
        }
    }

    fun stop() {
        if (!isRecording) return
        try {
            recorder?.stop()
            Log.d("MeteAudioRecorder", "Ses kaydı başarıyla durduruldu.")
        } catch (e: Exception) {
            Log.w("MeteAudioRecorder", "Kayıt durdurma hatası: ${e.message}")
        } finally {
            try {
                recorder?.reset()
                recorder?.release()
            } catch (_: Exception) {}
            recorder = null
            isRecording = false
        }
    }
}

class MeteAudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(file: File, onCompletion: () -> Unit) {
        player = MediaPlayer.create(context, Uri.fromFile(file))?.apply {
            setOnCompletionListener {
                onCompletion()
                it.release()
                player = null
            }
            try {
                start()
            } catch (_: Exception) {
                onCompletion()
            }
        }
    }

    fun stop() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}