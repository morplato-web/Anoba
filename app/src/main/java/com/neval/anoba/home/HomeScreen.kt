@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.neval.anoba.home

import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.drawer.DrawerContent
import com.neval.anoba.drawer.GuideSelectionDialog
import com.neval.anoba.home.components.NotificationHubCard
import com.neval.anoba.letter.LetterCard
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.mosaic.MosaicSheet
import com.neval.anoba.mycontent.MyContentViewModel
import com.neval.anoba.photo.PhotoCard
import com.neval.anoba.ses.SesCard
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import com.neval.anoba.video.VideoCard
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.math.abs

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    myContentViewModel: MyContentViewModel = koinViewModel(),
    analyticsHelper: AnalyticsHelper = koinInject(),
    countryCode: String? = null
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val screenMaxWidth = configuration.screenWidthDp.dp
    val screenMaxHeight = configuration.screenHeightDp.dp

    LaunchedEffect(Unit) { analyticsHelper.logScreenView("Home Screen", "HomeScreen") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                myContentViewModel.onMediaSelected(uris, context)
                navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )

    var showMosaicSheet by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val showTutorial by authViewModel.showTutorial.collectAsState()
    val currentTutorialStep by authViewModel.currentTutorialStep.collectAsState()

    var menuIconRect by remember { mutableStateOf<Rect?>(null) }
    var flowerDotRect by remember { mutableStateOf<Rect?>(null) }
    var myContentsIconRect by remember { mutableStateOf<Rect?>(null) }
    var moreMenuIconRect by remember { mutableStateOf<Rect?>(null) }
    var searchIconRect by remember { mutableStateOf<Rect?>(null) }
    var pinnedIconRect by remember { mutableStateOf<Rect?>(null) }
    var followingIconRect by remember { mutableStateOf<Rect?>(null) }
    var globalMapIconRect by remember { mutableStateOf<Rect?>(null) }
    var mosaicCardRect by remember { mutableStateOf<Rect?>(null) }
    var notificationsIconRect by remember { mutableStateOf<Rect?>(null) }
    var tabVideosRect by remember { mutableStateOf<Rect?>(null) }
    var tabPhotosRect by remember { mutableStateOf<Rect?>(null) }
    var tabLettersRect by remember { mutableStateOf<Rect?>(null) }
    var tabChatsRect by remember { mutableStateOf<Rect?>(null) }

    var showQuarantineSheet by remember { mutableStateOf(false) }
    var showPopularSheet by remember { mutableStateOf(false) }
    val popularItems by homeViewModel.popularContentItems.collectAsState()

    val activeTargetRect: Rect? = when (currentTutorialStep) {
        TutorialStep.WELCOME -> null
        TutorialStep.DRAWER_MENU -> menuIconRect
        TutorialStep.FLOWER_DOT -> flowerDotRect
        TutorialStep.MORE_MENU -> moreMenuIconRect
        TutorialStep.SEARCH_FILTER -> searchIconRect
        TutorialStep.PINNED_FILTER -> pinnedIconRect
        TutorialStep.FOLLOWING_FILTER -> followingIconRect
        TutorialStep.GLOBAL_MAP -> globalMapIconRect
        TutorialStep.TAB_CHATS -> tabChatsRect
        TutorialStep.MOSAIC -> mosaicCardRect
        TutorialStep.NOTIFICATIONS -> notificationsIconRect
        TutorialStep.TAB_VIDEOS -> tabVideosRect
        TutorialStep.TAB_LETTERS -> tabLettersRect
        TutorialStep.TAB_PHOTOS -> tabPhotosRect
        TutorialStep.MY_CONTENTS -> myContentsIconRect
        else -> null
    }

    var showGuideDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val currentUserId = (authState as? AuthState.Authenticated)?.uid ?: ""
    val currentUserEmail = (authState as? AuthState.Authenticated)?.email ?: ""
    val isAdmin = userRole?.equals("ADMIN", ignoreCase = true) == true || currentUserEmail == "morplato@gmail.com"

    var adminUserFilterQuery by remember { mutableStateOf("") }
    var showAdminFilterDialog by remember { mutableStateOf(false) }

    val notificationList by homeViewModel.notifications.collectAsState()
    val hasUnreadNotifs by homeViewModel.hasUnreadNotifications.collectAsState()

    val allContentItemsState by homeViewModel.allContentItems.collectAsState()
    val allContentItems = (allContentItemsState as? UiState.Success<List<ContentItem>>)?.data ?: emptyList()

    val isPinnedFilterActive by homeViewModel.isPinnedFilterActive.collectAsState()
    val isFollowingFilterActive by homeViewModel.isFollowingFilterActive.collectAsState()
    val pinnedIds by homeViewModel.pinnedIds.collectAsState()

    val knownCountryCodes = remember {
        setOf(
            "TR", "US", "RU", "IT", "DE", "AZ", "SA", "MX", "KZ", "AE",
            "HU", "UZ", "EG", "ZA", "QA", "DZ", "KG", "MA", "PK", "NG",
            "KE", "TN", "OM", "JO", "CA", "AU", "BR", "AR", "IN", "JP",
            "KR", "ID", "GB", "FR", "ES", "NL", "CO", "MY", "TM"
        )
    }

    val filteredItems = remember(allContentItems, countryCode) {
        if (!countryCode.isNullOrBlank()) {
            val target = countryCode.uppercase()
            allContentItems.filter { item ->
                val code = when (item) {
                    is ContentItem.PhotoContent -> item.photo.countryCode
                    is ContentItem.LetterContent -> item.letter.countryCode
                    is ContentItem.VideoContent -> item.video.countryCode
                    is ContentItem.AudioContent -> item.audio.countryCode
                }?.trim()?.uppercase()

                if (target == "OTHER") {
                    !code.isNullOrBlank() && code !in knownCountryCodes
                } else if (code == null) {
                    target == "TR"
                } else {
                    code == target
                }
            }
        } else {
            allContentItems
        }
    }

    val finalFilteredItems = remember(filteredItems, adminUserFilterQuery) {
        if (adminUserFilterQuery.isBlank()) {
            filteredItems
        } else {
            val query = adminUserFilterQuery.trim().lowercase()
            filteredItems.filter { item ->
                when (item) {
                    is ContentItem.PhotoContent -> item.photo.username.lowercase().contains(query) || item.photo.ownerId.lowercase() == query
                    is ContentItem.LetterContent -> item.letter.username.lowercase().contains(query) || item.letter.ownerId.lowercase() == query
                    is ContentItem.VideoContent -> item.video.username.lowercase().contains(query) || item.video.ownerId.lowercase() == query
                    is ContentItem.AudioContent -> item.audio.username.lowercase().contains(query) || item.audio.ownerId.lowercase() == query
                }
            }
        }
    }

    val policeAssets by homeViewModel.policeAssets.collectAsState()
    val isPoliceAnimationActive by homeViewModel.isPoliceAnimationActive.collectAsState()
    val policeSoundUrl by homeViewModel.policeSoundUrl.collectAsState()

    val commandoAssets by homeViewModel.commandoAssets.collectAsState()
    val isCommandoAnimationActive by homeViewModel.isCommandoAnimationActive.collectAsState()
    val commandoSoundUrl by homeViewModel.commandoSoundUrl.collectAsState()

    val bekciAssets by homeViewModel.bekciAssets.collectAsState()
    val isBekciAnimationActive by homeViewModel.isBekciAnimationActive.collectAsState()
    val bekciSoundUrl by homeViewModel.bekciSoundUrl.collectAsState()

    var activeAnimationAssets by remember { mutableStateOf<List<AnimationAsset>>(emptyList()) }
    var activeSoundUrl by remember { mutableStateOf<String?>(null) }
    var isAnimationActive by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val exoPlayer = remember {
        MediaCacheManager.createPlayer(context).apply {
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    val activeItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf -1

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: -1
        }
    }

    var lastPlayedUrl by remember { mutableStateOf("") }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (activeItemIndex in filteredItems.indices && filteredItems[activeItemIndex] is ContentItem.VideoContent) {
                    exoPlayer.play()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(activeItemIndex, filteredItems) {
        if (activeItemIndex in filteredItems.indices) {
            val currentItem = filteredItems[activeItemIndex]
            if (currentItem is ContentItem.VideoContent && currentItem.video.videoUrl.isNotBlank()) {
                if (lastPlayedUrl != currentItem.video.videoUrl) {
                    lastPlayedUrl = currentItem.video.videoUrl
                    val mediaItem = MediaItem.fromUri(currentItem.video.videoUrl.toUri())
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                }
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.pause()
                lastPlayedUrl = ""
            }
        } else {
            exoPlayer.pause()
            lastPlayedUrl = ""
        }
    }

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    var containerWindowY by remember { mutableFloatStateOf(0f) }
    var dividerYOffset by remember { mutableStateOf(0.dp) }

    LaunchedEffect(isPoliceAnimationActive) {
        if (isPoliceAnimationActive && !policeSoundUrl.isNullOrBlank()) {
            val mp = MediaPlayer()
            try {
                mp.setDataSource(policeSoundUrl)
                mp.setOnPreparedListener { it.start() }
                mp.prepareAsync()
                awaitCancellation()
            } catch (_: Exception) {
            } finally {
                try {
                    if (mp.isPlaying) mp.stop()
                    mp.release()
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(isCommandoAnimationActive) {
        if (isCommandoAnimationActive && !commandoSoundUrl.isNullOrBlank()) {
            val mp = MediaPlayer()
            try {
                mp.setDataSource(commandoSoundUrl)
                mp.setOnPreparedListener { it.start() }
                mp.prepareAsync()
                awaitCancellation()
            } catch (_: Exception) {
            } finally {
                try {
                    if (mp.isPlaying) mp.stop()
                    mp.release()
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(isBekciAnimationActive) {
        if (isBekciAnimationActive && !bekciSoundUrl.isNullOrBlank()) {
            val mp = MediaPlayer()
            try {
                mp.setDataSource(bekciSoundUrl)
                mp.setOnPreparedListener { it.start() }
                mp.prepareAsync()
                awaitCancellation()
            } catch (_: Exception) {
            } finally {
                try {
                    if (mp.isPlaying) mp.stop()
                    mp.release()
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(isAnimationActive) {
        if (isAnimationActive && !activeSoundUrl.isNullOrBlank()) {
            val mp = MediaPlayer()
            try {
                mp.setDataSource(activeSoundUrl)
                mp.setOnPreparedListener { it.start() }
                mp.prepareAsync()
                awaitCancellation()
            } catch (_: Exception) {
            } finally {
                try {
                    if (mp.isPlaying) mp.stop()
                    mp.release()
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(homeViewModel.shareState) {
        homeViewModel.shareState.collectLatest { state ->
            when (state) {
                is com.neval.anoba.photo.ShareState.Success -> {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, state.uri)
                        type = context.contentResolver.getType(state.uri) ?: "image/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Paylaş"))
                    homeViewModel.resetShareState()
                }
                is com.neval.anoba.photo.ShareState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    homeViewModel.resetShareState()
                }
                else -> {}
            }
        }
    }

    DrawerContent(
        navController = navController,
        drawerState = drawerState,
        onInfoClick = { showGuideDialog = true }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .onGloballyPositioned { containerWindowY = it.positionInWindow().y }
            ) {
                androidx.compose.material3.Scaffold(
                    topBar = {
                        Column(modifier = Modifier.background(Color.White)) {
                            TopAppBar(
                                title = {
                                    if (!countryCode.isNullOrBlank()) {
                                        val flag = when (countryCode.uppercase()) {
                                            "TR" -> "🇹🇷"
                                            "SA" -> "🇸🇦"
                                            "AE" -> "🇦🇪"
                                            "DZ" -> "DZ"
                                            "MA" -> "🇲🇦"
                                            "ZA" -> "🇿🇦"
                                            "QA" -> "🇶🇦"
                                            "KE" -> "🇰🇪"
                                            "EG" -> "🇪🇬"
                                            "NG" -> "🇳🇬"
                                            "TN" -> "🇹🇳"
                                            "OM" -> "🇴🇲"
                                            "JO" -> "🇯🇴"
                                            "PK" -> "🇵🇰"
                                            "CA" -> "🇨🇦"
                                            "AU" -> "🇦🇺"
                                            "BR" -> "🇧🇷"
                                            "AR" -> "🇦🇷"
                                            else -> "🌍"
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AnobaNavy.copy(alpha = 0.08f),
                                            modifier = Modifier.clickable {
                                                navController.navigate(Constants.HOME_SCREEN) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        inclusive = true
                                                    }
                                                    launchSingleTop = true
                                                }
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(text = flag, fontSize = 16.sp)
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = countryCode.uppercase(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AnobaNavy,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear Filter",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = AnobaNavy.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                                navigationIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { scope.launch { drawerState.open() } }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Menu,
                                                contentDescription = stringResource(R.string.home_menu),
                                                modifier = Modifier.onGloballyPositioned { menuIconRect = it.boundsInWindow() }
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF06292))
                                                .onGloballyPositioned { flowerDotRect = it.boundsInWindow() }
                                                .clickable {
                                                    val pkg = homeViewModel.getRandomAnimationPackage()
                                                    if (pkg != null) {
                                                        activeAnimationAssets = pkg.assets
                                                        activeSoundUrl = pkg.soundUrl
                                                        isAnimationActive = true
                                                    }
                                                }
                                        )

                                        Spacer(modifier = Modifier.width(2.dp))

                                        // 🔮 3 NOKTA MENÜSÜ (1000+, Karantina ve Mete İçerir)
                                        Box {
                                            IconButton(
                                                modifier = Modifier.size(28.dp),
                                                onClick = { showMoreMenu = true }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Diğer Menü",
                                                    tint = AnobaNavy,
                                                    modifier = Modifier.size(20.dp).onGloballyPositioned { moreMenuIconRect = it.boundsInWindow() }
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showMoreMenu,
                                                onDismissRequest = { showMoreMenu = false }
                                            ) {
                                                // ⭐ 1000+ Popüler İçerikler
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(vertical = 2.dp)
                                                        ) {
                                                            Surface(
                                                                modifier = Modifier.size(20.dp),
                                                                shape = CircleShape,
                                                                color = AnobaGold,
                                                                shadowElevation = 1.dp
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Text(
                                                                        text = "1000+",
                                                                        color = Color.White,
                                                                        fontSize = 5.sp,
                                                                        fontWeight = FontWeight.Black,
                                                                        maxLines = 1,
                                                                        softWrap = false
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                text = "1000+ Popüler",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = AnobaNavy,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        showMoreMenu = false
                                                        showPopularSheet = true
                                                    }
                                                )

                                                // 🛡️ Karantina Havuzu
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(vertical = 2.dp)
                                                        ) {
                                                            Box(
                                                                contentAlignment = Alignment.Center,
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Shield,
                                                                    contentDescription = "Quarantine Pool",
                                                                    modifier = Modifier.size(20.dp),
                                                                    tint = Color(0xFF4CAF50).copy(alpha = 0.9f)
                                                                )
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(8.dp),
                                                                    tint = Color.White
                                                                )
                                                            }
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                text = "Karantina Havuzu",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = AnobaNavy,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        showMoreMenu = false
                                                        showQuarantineSheet = true
                                                    }
                                                )

                                                // 🇹🇷 Mete (Ay-Yıldız)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(vertical = 2.dp)
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.ay_yildiz),
                                                                contentDescription = "Mete",
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                text = "Mete",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = AnobaNavy,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        showMoreMenu = false
                                                        navController.navigate(Constants.METE_SCREEN)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 6.dp)) {
                                        // 1. ARAMA / FİLTRE
                                        IconButton(
                                            modifier = Modifier.size(32.dp),
                                            onClick = { showAdminFilterDialog = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Kullanıcı Filtresi",
                                                modifier = Modifier.size(24.dp).onGloballyPositioned { searchIconRect = it.boundsInWindow() },
                                                tint = if (adminUserFilterQuery.isNotBlank()) AnobaGold else Color(0xFF1E88E5)
                                            )
                                        }

                                        val hasPinnedItems = pinnedIds.isNotEmpty()

                                        val pinIcon = when {
                                            isPinnedFilterActive -> Icons.Filled.PushPin
                                            hasPinnedItems -> Icons.Filled.PushPin
                                            else -> Icons.Outlined.PushPin
                                        }

                                        val pinTint = when {
                                            isPinnedFilterActive -> Color(0xFF2196F3)
                                            hasPinnedItems -> AnobaGold
                                            else -> AnobaNavy
                                        }

                                        // 2. PİN
                                        IconButton(
                                            modifier = Modifier.size(32.dp),
                                            onClick = { homeViewModel.togglePinnedFilter() }
                                        ) {
                                            Icon(
                                                imageVector = pinIcon,
                                                contentDescription = "Pinned",
                                                modifier = Modifier.size(24.dp).onGloballyPositioned { pinnedIconRect = it.boundsInWindow() },
                                                tint = pinTint
                                            )
                                        }

                                        // 3. TAKİP ETTİKLERİM FİLTRESİ
                                        IconButton(
                                            modifier = Modifier.size(32.dp),
                                            onClick = { homeViewModel.toggleFollowingFilter() }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.People,
                                                contentDescription = "Following Filter",
                                                modifier = Modifier.size(25.dp).onGloballyPositioned { followingIconRect = it.boundsInWindow() },
                                                tint = if (isFollowingFilterActive) Color(0xFF4CAF50) else AnobaNavy
                                            )
                                        }

                                        // 4. HARİTA
                                        IconButton(
                                            modifier = Modifier.size(32.dp),
                                            onClick = {
                                                navController.navigate(Constants.GLOBAL_MAP_SCREEN) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Language, "Map", modifier = Modifier.size(24.dp).onGloballyPositioned { globalMapIconRect = it.boundsInWindow() }, tint = AnobaNavy)
                                        }

                                        // 5. SOHBET
                                        IconButton(
                                            modifier = Modifier.size(32.dp),
                                            onClick = {
                                                navController.navigate(Constants.CHAT_NAV_GRAPH) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                                contentDescription = "Chat",
                                                modifier = Modifier.size(24.dp).onGloballyPositioned { tabChatsRect = it.boundsInWindow() },
                                                tint = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                },                            )
                            HorizontalDivider(Modifier.fillMaxWidth().onGloballyPositioned { dividerYOffset = with(density) { (it.positionInWindow().y - containerWindowY).toDp() } }, color = Color.LightGray, thickness = 1.dp)

                            // --- MOZAİK VE SAĞ İKONLAR SATIRI ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(vertical = 10.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. MOZAİK KARTI
                                Card(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .weight(0.12f)
                                        .onGloballyPositioned { mosaicCardRect = it.boundsInWindow() }
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showMosaicSheet = true },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.mosaic_title_caps),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 8.sp
                                        )
                                        Text(
                                            text = stringResource(R.string.mosaic_ad_contains),
                                            color = Color.LightGray.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                val iconSize = 20.dp
                                val cardHeight = 30.dp
                                val cardWidth = 32.dp

                                val navOptions: androidx.navigation.NavOptionsBuilder.() -> Unit = {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }

                                // 2. SAĞ İKONLAR GRUBU (Sağa Yaslı & Sıralı)
                                Row(
                                    modifier = Modifier.weight(0.33f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // 1. ZİL (BİLDİRİM)
                                        BadgedBox(
                                            modifier = Modifier.size(width = cardWidth, height = cardHeight).onGloballyPositioned { notificationsIconRect = it.boundsInWindow() },
                                            badge = {
                                                if (hasUnreadNotifs) {
                                                    Badge(containerColor = Color.Red, modifier = Modifier.size(7.dp))
                                                }
                                            }
                                        ) {
                                            IconInCardButton(
                                                modifier = Modifier.fillMaxSize(),
                                                onClick = { showNotificationDialog = true },
                                                icon = Icons.Default.Notifications,
                                                cardColor = if (hasUnreadNotifs) Color(0xFFFFF3CD) else Color(0xFFECEFF1),
                                                iconColor = if (hasUnreadNotifs) Color(0xFFE65100) else Color(0xFF455A64),
                                                size = iconSize
                                            )
                                        }

                                        // 🎥 2. FOTOĞRAF MAKİNASI / KAMERA (ÇEK)
                                        IconInCardButton(
                                            modifier = Modifier.size(width = cardWidth, height = cardHeight).onGloballyPositioned { tabVideosRect = it.boundsInWindow() },
                                            onClick = { navController.navigate(Constants.VIDEO_CAMERA_SCREEN) },
                                            icon = Icons.Filled.CameraAlt,
                                            cardColor = Color(0xFFF3E5F5),
                                            iconColor = Color(0xFF8E24AA),
                                            size = iconSize
                                        )

                                        // ✍️ 3. KALEM (YAZ - Mektup & Sesli Mektup)
                                        IconInCardButton(
                                            modifier = Modifier.size(width = cardWidth, height = cardHeight).onGloballyPositioned { tabLettersRect = it.boundsInWindow() },
                                            onClick = { navController.navigate(Constants.LETTER_CREATE_SCREEN) },
                                            icon = Icons.Filled.Edit,
                                            cardColor = Color(0xFFE1F5FE),
                                            iconColor = Color(0xFF0288D1),
                                            size = iconSize
                                        )

                                        // 🖼️ 4. GALERİ (SEÇ - Galeri Medya)
                                        IconInCardButton(
                                            modifier = Modifier.size(width = cardWidth, height = cardHeight).onGloballyPositioned { tabPhotosRect = it.boundsInWindow() },
                                            onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                                            icon = Icons.Filled.PhotoLibrary,
                                            cardColor = Color(0xFFFFEBEE),
                                            iconColor = Color(0xFFE53935),
                                            size = iconSize
                                        )

                                        // 📑 5. ÜRETİM / İÇERİKLERİM (YÖNET / TASLAKLAR)
                                        IconInCardButton(
                                            modifier = Modifier.size(width = cardWidth, height = cardHeight).onGloballyPositioned { myContentsIconRect = it.boundsInWindow() },
                                            onClick = { navController.navigate(Constants.MY_CONTENT_NAV_GRAPH, navOptions) },
                                            icon = Icons.Filled.DynamicFeed,
                                            cardColor = Color(0xFFECEFF1),
                                            iconColor = Color(0xFF37474F),
                                            size = iconSize
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp)
                        }
                    },
                    content = { innerPadding ->
                        val isRefreshing = allContentItemsState is UiState.Loading && allContentItems.isNotEmpty()

                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = { homeViewModel.refreshFeed() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                                if (allContentItemsState is UiState.Loading && allContentItems.isEmpty()) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AnobaNavy)
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            top = innerPadding.calculateTopPadding() + 6.dp,
                                            bottom = innerPadding.calculateBottomPadding() + 80.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (adminUserFilterQuery.isNotBlank()) {
                                            item {
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    color = Color(0xFFE3F2FD),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Filtrelenen Kullanıcı: $adminUserFilterQuery (${finalFilteredItems.size} içerik)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1565C0)
                                                        )
                                                        IconButton(
                                                            modifier = Modifier.size(20.dp),
                                                            onClick = { adminUserFilterQuery = "" }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Temizle",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = Color(0xFF1565C0)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (finalFilteredItems.isEmpty()) {
                                            item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.home_no_content), color = Color.Gray) } }
                                        } else {
                                            itemsIndexed(finalFilteredItems, key = { index, item -> item.id.ifEmpty { "item-$index" } }) { index, contentItem ->
                                                val isPinned = pinnedIds.contains(contentItem.id)
                                                val onPinClick = { homeViewModel.togglePin(contentId = contentItem.id, contentType = contentItem.contentType) }

                                                val onUserCardClick: (String) -> Unit = { targetUid ->
                                                    if (targetUid.isNotBlank()) {
                                                        navController.navigate("my_content_screen?userId=$targetUid")
                                                    }
                                                }

                                                when (contentItem) {
                                                    is ContentItem.PhotoContent -> PhotoCard(
                                                        photo = contentItem.photo,
                                                        navController = navController,
                                                        isCommentSectionClickable = false,
                                                        isPinned = isPinned,
                                                        onPinClick = onPinClick,
                                                        canDelete = isAdmin || contentItem.photo.ownerId == currentUserId,
                                                        onDeleteClick = {
                                                            if (isAdmin && contentItem.photo.ownerId != currentUserId) {
                                                                homeViewModel.unpublishPhoto(contentItem.photo.id)
                                                            } else {
                                                                homeViewModel.deletePhoto(contentItem.photo.id)
                                                            }
                                                        },
                                                        onUserClick = onUserCardClick,
                                                        onShareClick = {
                                                            homeViewModel.sharePhoto(context, contentItem.photo.photoUrl, contentItem.photo.id)
                                                        },
                                                        onDoubleTap = { p -> homeViewModel.togglePhotoReaction(p.id, "❤️") }
                                                    )
                                                    is ContentItem.LetterContent -> LetterCard(
                                                        letter = contentItem.letter,
                                                        navController = navController,
                                                        currentUserId = currentUserId,
                                                        userRole = userRole ?: "MEMBER",
                                                        isCommentSectionClickable = false,
                                                        cardColor = Color(0xFFFFF3E0),
                                                        isPinned = isPinned,
                                                        onPinClick = onPinClick,
                                                        canDelete = isAdmin || contentItem.letter.ownerId == currentUserId,
                                                        onDeleteClick = {
                                                            if (isAdmin && contentItem.letter.ownerId != currentUserId) {
                                                                homeViewModel.unpublishLetter(contentItem.letter.id)
                                                            } else {
                                                                homeViewModel.deleteLetter(contentItem.letter.id)
                                                            }
                                                        },
                                                        onUserClick = onUserCardClick,
                                                        onShareClick = {
                                                            homeViewModel.incrementLetterShareCount(contentItem.letter.id)
                                                            val shareText = if (!contentItem.letter.audioUrl.isNullOrBlank()) {
                                                                "${contentItem.letter.content}\n\nSesli Mektubu Dinle: ${contentItem.letter.audioUrl}"
                                                            } else {
                                                                contentItem.letter.content
                                                            }
                                                            val sendIntent = Intent().apply {
                                                                action = Intent.ACTION_SEND
                                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                                type = "text/plain"
                                                            }
                                                            context.startActivity(Intent.createChooser(sendIntent, null))
                                                        }
                                                    )
                                                    is ContentItem.AudioContent -> SesCard(
                                                        ses = contentItem.audio,
                                                        navController = navController,
                                                        isCommentSectionClickable = false,
                                                        showPlayIcon = false,
                                                        cardColor = Color(0xFFFFD1DC),
                                                        isPinned = isPinned,
                                                        onPinClick = onPinClick,
                                                        canDelete = isAdmin || contentItem.audio.ownerId == currentUserId,
                                                        onDeleteClick = {
                                                            if (isAdmin && contentItem.audio.ownerId != currentUserId) {
                                                                homeViewModel.unpublishSes(contentItem.audio.id)
                                                            } else {
                                                                homeViewModel.deleteSes(contentItem.audio.id)
                                                            }
                                                        },
                                                        onUserClick = onUserCardClick,
                                                        onShareClick = {
                                                            homeViewModel.shareSes(context, contentItem.audio.audioUrl, contentItem.audio.id)
                                                        }
                                                    )
                                                    is ContentItem.VideoContent -> VideoCard(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 4.dp),
                                                        video = contentItem.video,
                                                        navController = navController,
                                                        exoPlayer = exoPlayer,
                                                        isPlaying = (index == activeItemIndex),
                                                        onClick = { navController.navigate(Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", contentItem.video.id)) },
                                                        isCommentSectionClickable = true,
                                                        isPinned = isPinned,
                                                        onPinClick = onPinClick,
                                                        canDelete = isAdmin || contentItem.video.ownerId == currentUserId,
                                                        onDeleteClick = {
                                                            if (isAdmin && contentItem.video.ownerId != currentUserId) {
                                                                homeViewModel.unpublishVideo(contentItem.video.id)
                                                            } else {
                                                                homeViewModel.deleteVideo(contentItem.video.id)
                                                            }
                                                        },
                                                        onUserClick = onUserCardClick,
                                                        onDoubleTap = { v ->
                                                            homeViewModel.toggleVideoReaction(v.id, "❤️")
                                                        },
                                                        onShareClick = {
                                                            homeViewModel.shareVideo(context, contentItem.video.videoUrl, contentItem.video.id)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                if (!showMosaicSheet) {
                    HomeAnimationLayer(
                        screenMaxWidth = screenMaxWidth,
                        screenMaxHeight = screenMaxHeight,
                        startYOffset = dividerYOffset,
                        activeAnimationAssets = activeAnimationAssets,
                        isAnimationActive = isAnimationActive,
                        onAnimationFinished = { isAnimationActive = false },
                        policeAssets = policeAssets,
                        isPoliceAnimationActive = isPoliceAnimationActive,
                        onPoliceAnimationFinished = { homeViewModel.togglePoliceAnimation(false) },
                        commandoAssets = commandoAssets,
                        isCommandoAnimationActive = isCommandoAnimationActive,
                        onCommandoAnimationFinished = { homeViewModel.toggleCommandoAnimation(false) },
                        bekciAssets = bekciAssets,
                        isBekciAnimationActive = isBekciAnimationActive,
                        onBekciAnimationFinished = { homeViewModel.toggleBekciAnimation(false) }
                    )
                }
            }

            MosaicSheet(isVisible = showMosaicSheet, onDismiss = { showMosaicSheet = false })

            QuarantineSheet(
                isVisible = showQuarantineSheet,
                onDismiss = { showQuarantineSheet = false },
                homeViewModel = homeViewModel,
                navController = navController
            )

            PopularSheet(
                isVisible = showPopularSheet,
                popularItems = popularItems,
                onDismiss = { showPopularSheet = false },
                navController = navController
            )

            // 🚀 BİLDİRİM DİALOGU (GRUP VE BİREBİR AYRIŞTIRILMIŞ NAVİGASYON)
            if (showNotificationDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationDialog = false },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showNotificationDialog = false }) {
                            Text(stringResource(id = R.string.notif_hub_close), color = AnobaNavy)
                        }
                    },
                    text = {
                        NotificationHubCard(
                            notifications = notificationList,
                            hasUnread = hasUnreadNotifs,
                            onMarkAllAsReadClick = {
                                homeViewModel.markAllNotificationsAsRead()
                            },
                            onDeleteNotificationClick = { notificationId ->
                                homeViewModel.deleteNotification(notificationId)
                            },
                            onDeleteAllNotificationsClick = {
                                homeViewModel.deleteAllNotifications()
                            },
                            onNotificationClick = { notif ->
                                showNotificationDialog = false
                                homeViewModel.markNotificationAsRead(notif.id)

                                val type = notif.type.uppercase()
                                val targetId = notif.targetId.trim()
                                val isGroupChat = notif.isGroup || !notif.groupId.isNullOrBlank()

                                Log.d("NotificationDebug", "ID=${notif.id}, Tip=$type, Target=$targetId, isGroup=$isGroupChat")

                                try {
                                    when {
                                        type == "QUARANTINE_ALERT" -> {
                                            // 🚀 Karantina bildirimi için özel yönlendirme (İçeriğin kendi detayına gider ama afişle karşılar)
                                            if (targetId.isNotBlank()) {
                                                val msg = notif.message.lowercase()
                                                val route = when {
                                                    msg.contains("foto") -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", targetId)
                                                    msg.contains("video") -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", targetId)
                                                    msg.contains("mektup") -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", targetId)
                                                    msg.contains("ses") -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", targetId)
                                                    else -> Constants.MY_CONTENT_NAV_GRAPH
                                                }
                                                navController.navigate(route)
                                            }
                                        }
                                        type == "REPORTER_QUARANTINE" || type == "REPORTER_UNFLAG" || type == "REPORTER_DELETED" -> {
                                            // 🛡️ İşaretleyenler için geri bildirim
                                            if (type == "REPORTER_DELETED") {
                                                Toast.makeText(context, "İçerik sistemden kaldırılmıştır.", Toast.LENGTH_LONG).show()
                                            } else if (type == "REPORTER_QUARANTINE") {
                                                Toast.makeText(context, "Bildirdiğiniz içerik inceleme havuzuna alınmıştır.", Toast.LENGTH_SHORT).show()
                                            } else if (targetId.isNotBlank()) {
                                                val msg = notif.message.lowercase()
                                                val route = when {
                                                    msg.contains("foto") -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", targetId)
                                                    msg.contains("video") -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", targetId)
                                                    msg.contains("mektup") -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", targetId)
                                                    msg.contains("ses") -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", targetId)
                                                    else -> Constants.HOME_SCREEN
                                                }
                                                navController.navigate(route)
                                            }
                                        }
                                        type == "VIDEO" || type.startsWith("VIDEO_") -> {
                                            if (targetId.isNotBlank()) navController.navigate(Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", targetId))
                                            else navController.navigate(Constants.VIDEO_NAV_GRAPH)
                                        }
                                        type == "PHOTO" || type.startsWith("PHOTO_") -> {
                                            if (targetId.isNotBlank()) navController.navigate(Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", targetId))
                                            else navController.navigate(Constants.PHOTO_NAV_GRAPH)
                                        }
                                        type == "LETTER" || type.startsWith("LETTER_") || type == "ANOBA_RESPONSE" || type == "SEALED_LETTER" -> {
                                            if (targetId.isNotBlank()) navController.navigate(Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", targetId))
                                            else navController.navigate(Constants.LETTER_NAV_GRAPH)
                                        }
                                        type == "SES" || type.startsWith("SES_") || type.startsWith("AUDIO_") -> {
                                            if (targetId.isNotBlank()) navController.navigate(Constants.SES_DETAIL_SCREEN.replace("{sesId}", targetId))
                                            else navController.navigate(Constants.SES_NAV_GRAPH)
                                        }
                                        type == "CHAT" || type.startsWith("CHAT_") -> {
                                            // 🚀 GRUP VE BİREBİR AYRIŞTIRMASI
                                            if (isGroupChat) {
                                                val finalGroupId = notif.groupId?.ifBlank { targetId } ?: targetId
                                                if (finalGroupId.isNotBlank()) {
                                                    navController.navigate(Constants.GROUP_CHAT_SCREEN.replace("{groupId}", finalGroupId))
                                                } else {
                                                    navController.navigate(Constants.CHAT_NAV_GRAPH)
                                                }
                                            } else {
                                                if (targetId.isNotBlank()) {
                                                    navController.navigate("${Constants.PRIVATE_CHAT_SCREEN}?userId=$targetId")
                                                } else {
                                                    navController.navigate(Constants.CHAT_NAV_GRAPH)
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("NotificationDebug", "Navigasyon Hatası: ${e.message}", e)
                                }
                            },
                            modifier = Modifier.padding(0.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }

            if (showTutorial && currentTutorialStep != TutorialStep.FINISHED) {
                val shouldShow = currentTutorialStep == TutorialStep.WELCOME || activeTargetRect != null

                if (shouldShow) {
                    HomeTutorialOverlay(
                        currentStep = currentTutorialStep,
                        targetRect = activeTargetRect,
                        onNext = { authViewModel.nextTutorialStep() },
                        onSkip = { authViewModel.completeTutorial() }
                    )
                }
            }

            // 🚀 ANOBA REHBER SEÇİM DİYALOĞU (Drawer'dan veya ilk girişte tetiklenir)
            if (showGuideDialog) {
                GuideSelectionDialog(
                    onDismiss = { showGuideDialog = false },
                    onStartTour = {
                        showGuideDialog = false
                        authViewModel.startTutorial()
                    }
                )
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
                        scope.launch {
                            if (listState.firstVisibleItemIndex > 10) {
                                listState.scrollToItem(5)
                            }
                            listState.animateScrollToItem(0)
                        }
                    },
                    shape = CircleShape,
                    containerColor = AnobaNavy,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Back to Top")
                }
            }

            if (showAdminFilterDialog) {
                var tempQuery by remember { mutableStateOf(adminUserFilterQuery) }
                AlertDialog(
                    onDismissRequest = { showAdminFilterDialog = false },
                    title = {
                        Text("Kullanıcı Filtresi", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            Text("Görmek istediğiniz kullanıcının adını veya ID'sini yazın:", fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = tempQuery,
                                onValueChange = { tempQuery = it },
                                placeholder = { Text("Örn: Misafir, Ahmet, Nanuq...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                adminUserFilterQuery = tempQuery
                                showAdminFilterDialog = false
                            }
                        ) {
                            Text("Filtrele", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                tempQuery = ""
                                adminUserFilterQuery = ""
                                showAdminFilterDialog = false
                            }
                        ) {
                            Text("Temizle", color = Color.Red)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IconInCardButton(
    onClick: () -> Unit,
    icon: ImageVector,
    cardColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                null,
                tint = iconColor,
                modifier = Modifier.size(size)
            )
        }
    }
}