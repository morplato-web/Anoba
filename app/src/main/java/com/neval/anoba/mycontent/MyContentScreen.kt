package com.neval.anoba.mycontent

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.letter.LetterModel
import com.neval.anoba.ses.AudioRecordingSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyContentScreen(
    navController: NavController,
    viewModel: MyContentViewModel = koinViewModel()
) {
    val isOwner by viewModel.isOwner.collectAsState()
    val targetUsername by viewModel.targetUsername.collectAsState()
    val publishedContent by viewModel.publishedContent.collectAsState()
    val draftContent by viewModel.draftContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val isAnobaUser by viewModel.isAnobaUser.collectAsState()
    val errorMessageRes by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<MyContentItem?>(null) }
    val videoDownloadState by viewModel.videoDownloadState.collectAsState()
    val context = LocalContext.current
    val launchGallery by viewModel.launchGallery.collectAsState()
    var showAudioSheet by remember { mutableStateOf(false) }

    val backText = stringResource(id = R.string.mycontent_back)
    val homeText = stringResource(id = R.string.mycontent_home)
    val guideUpdatedText = stringResource(id = R.string.mycontent_guide_updated)
    val errorUnknownText = stringResource(id = R.string.error_unknown)
    val videoPreparingText = stringResource(id = R.string.mycontent_video_preparing)
    val contentLoadingText = stringResource(id = R.string.mycontent_content_loading)
    val newCameraText = stringResource(id = R.string.mycontent_new_photo)
    val newLetterText = stringResource(id = R.string.mycontent_new_letter)
    val fromGalleryText = stringResource(id = R.string.mycontent_from_gallery)

    val videoDownloadErrorMessage = (videoDownloadState as? VideoDownloadState.Error)?.let { state ->
        state.messageResId?.let { stringResource(it) } ?: state.message
    } ?: ""

    val uploadCancelledMessage = stringResource(id = R.string.mycontent_upload_cancelled)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onMediaSelected(uris, context) }
    )

    LaunchedEffect(launchGallery) {
        if (launchGallery) {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            viewModel.onGalleryLaunched()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedContentItem by remember { mutableStateOf<MyContentItem?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(1) }
    val tabs = listOf(stringResource(id = R.string.mycontent_published_tab), stringResource(id = R.string.mycontent_drafts_tab))

    LaunchedEffect(videoDownloadState) {
        when (val state = videoDownloadState) {
            is VideoDownloadState.Success -> {
                val encodedUri = URLEncoder.encode(state.localUri.toString(), StandardCharsets.UTF_8.toString())
                val route = Constants.VIDEO_EDIT_SCREEN
                    .replace("{videoId}", state.videoId)
                    .replace("{videoUri}", encodedUri)
                navController.navigate(route)
                viewModel.resetVideoDownloadState()
            }
            is VideoDownloadState.Error -> {
                val message = videoDownloadErrorMessage.ifBlank { errorUnknownText }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.resetVideoDownloadState()
            }
            else -> {}
        }
    }

    showDeleteDialog?.let {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteContent(it.id, it.type)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    if (isLoading) {
        BackHandler(enabled = true) {
            viewModel.cancelUpload()
            Toast.makeText(context, uploadCancelledMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (!isOwner) {
                                targetUsername?.let { "$it" } ?: stringResource(id = R.string.home_my_contents)
                            } else {
                                ""
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText)
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            if (isOwner) {
                                // 1. Kamera (ÇEK)
                                CreationIconButton(
                                    icon = Icons.Filled.CameraAlt,
                                    contentDescription = newCameraText,
                                    backgroundColor = Color(0xFFF3E5F5),
                                    iconColor = Color(0xFF8E24AA),
                                    onClick = { navController.navigate(Constants.VIDEO_CAMERA_SCREEN) }
                                )

                                // 2. Kalem (YAZ)
                                CreationIconButton(
                                    icon = Icons.Filled.Edit,
                                    contentDescription = newLetterText,
                                    backgroundColor = Color(0xFFE1F5FE),
                                    iconColor = Color(0xFF0288D1),
                                    onClick = { navController.navigate(Constants.LETTER_CREATE_SCREEN) }
                                )

                                // 3. Galeri (SEÇ)
                                CreationIconButton(
                                    icon = Icons.Filled.PhotoLibrary,
                                    contentDescription = fromGalleryText,
                                    backgroundColor = Color(0xFFFFEBEE),
                                    iconColor = Color(0xFFE53935),
                                    onClick = { viewModel.onGalleryIconClicked() }
                                )
                            }

                            // 4. Home Butonu
                            IconButton(
                                onClick = {
                                    navController.navigate(Constants.HOME_SCREEN) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Home, contentDescription = homeText)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                if (isOwner) {
                    PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(text = title) })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    if (errorMessageRes != null) {
                        Text(text = stringResource(id = errorMessageRes!!), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    } else {
                        val itemsToShow = if (isOwner && selectedTabIndex == 1) draftContent else publishedContent
                        if (itemsToShow.isEmpty() && !isLoading) {
                            Text(if (isOwner && selectedTabIndex == 1) stringResource(id = R.string.mycontent_no_draft_content) else stringResource(id = R.string.mycontent_no_published_content))
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalItemSpacing = 10.dp
                            ) {
                                items(itemsToShow, key = { it.id + it.type }) { item ->
                                    Box {
                                        ContentCard(
                                            item = item,
                                            onClick = {
                                                if (!isOwner) {
                                                    val route = when (item) {
                                                        is MyContentItem.Audio -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", item.audio.id)
                                                        is MyContentItem.Letter -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", item.letter.id)
                                                        is MyContentItem.Photo -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", item.photo.id)
                                                        is MyContentItem.Video -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", item.video.id)
                                                    }
                                                    navController.navigate(route)
                                                } else {
                                                    selectedContentItem = item
                                                }
                                            }
                                        )

                                        // 🛡️ KARANTİNA / İNCELEME ETİKETİ
                                        if (item.isFlagged) {
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp),
                                                color = Color(0xFF4CAF50).copy(alpha = 0.9f), // Yeşil
                                                shape = RoundedCornerShape(4.dp),
                                                shadowElevation = 4.dp
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Shield,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(10.dp),
                                                            tint = Color.White
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(5.dp),
                                                            tint = Color(0xFF4CAF50)
                                                        )
                                                    }
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "İncelemede",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading || videoDownloadState is VideoDownloadState.InProgress) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false, onClick = {}), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (videoDownloadState is VideoDownloadState.InProgress) {
                            videoPreparingText
                        } else {
                            uploadProgress?.let { "$contentLoadingText ($it)" } ?: contentLoadingText
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showAudioSheet) {
        AudioRecordingSheet(onDismiss = { showAudioSheet = false }, onRecordingComplete = { showAudioSheet = false })
    }

    selectedContentItem?.let { item ->
        ModalBottomSheet(onDismissRequest = { selectedContentItem = null }, sheetState = sheetState) {
            BottomSheetContent(
                item = item,
                onViewClick = {
                    val route = when (item) {
                        is MyContentItem.Audio -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", item.audio.id)
                        is MyContentItem.Letter -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", item.letter.id)
                        is MyContentItem.Photo -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", item.photo.id)
                        is MyContentItem.Video -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", item.video.id)
                    }
                    navController.navigate(route)
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onPublishClick = { message ->
                    viewModel.publishContent(item.id, item.type)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onEditClick = {
                    scope.launch { sheetState.hide(); viewModel.onEditClicked(item, context, navController); selectedContentItem = null }
                },
                onDeleteClick = {
                    showDeleteDialog = item
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onAnobaClick = {
                    viewModel.toggleAnobaGuideState(item.id, item.type)
                    Toast.makeText(context, guideUpdatedText, Toast.LENGTH_SHORT).show()
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                onShareClick = {
                    val shareText = when (item) {
                        is MyContentItem.Audio -> item.audio.audioUrl
                        is MyContentItem.Letter -> item.letter.content
                        is MyContentItem.Photo -> item.photo.photoUrl
                        is MyContentItem.Video -> item.video.videoUrl
                    }
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, null))
                    scope.launch { sheetState.hide(); selectedContentItem = null }
                },
                isAnobaUser = isAnobaUser
            )
        }
    }
}

@Composable
private fun CreationIconButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(34.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    item: MyContentItem,
    onViewClick: () -> Unit,
    onPublishClick: (message: String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAnobaClick: () -> Unit,
    onShareClick: () -> Unit,
    isAnobaUser: Boolean
) {
    val publishSuccessMessage = stringResource(id = R.string.mycontent_publish_success)
    val unpublishSuccessMessage = stringResource(id = R.string.mycontent_unpublish_success)
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text(text = if (item.isPublished) stringResource(id = R.string.mycontent_sheet_published) else stringResource(id = R.string.mycontent_sheet_draft), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (item.isPublished) Color(0xFF388E3C) else MaterialTheme.colorScheme.outline)
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = item.viewCount.toString(), style = MaterialTheme.typography.titleLarge); Text(text = stringResource(id = R.string.mycontent_sheet_views), style = MaterialTheme.typography.bodySmall) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = item.reactionsCount.toString(), style = MaterialTheme.typography.titleLarge); Text(text = stringResource(id = R.string.mycontent_sheet_reactions), style = MaterialTheme.typography.bodySmall) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = item.commentsCount.toString(), style = MaterialTheme.typography.titleLarge); Text(text = stringResource(id = R.string.mycontent_sheet_comments), style = MaterialTheme.typography.bodySmall) }
        }
        HorizontalDivider()

        ListItem(headlineContent = { Text(stringResource(id = R.string.mycontent_sheet_open)) }, leadingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF42A5F5)) }, modifier = Modifier.clickable(onClick = onViewClick))

        if (item.isPublished) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.home_share_content)) },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF4CAF50)) },
                modifier = Modifier.clickable(onClick = onShareClick)
            )
        }

        ListItem(headlineContent = { Text(if (item.isPublished) stringResource(id = R.string.mycontent_sheet_unpublish) else stringResource(id = R.string.mycontent_sheet_publish)) }, leadingContent = { Icon(Icons.Default.Publish, contentDescription = null, tint = Color(0xFF9E9E9E)) }, modifier = Modifier.clickable { val message = if (!item.isPublished) publishSuccessMessage else unpublishSuccessMessage; onPublishClick(message) })
        ListItem(headlineContent = { Text(stringResource(id = R.string.mycontent_sheet_edit)) }, leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFFA726)) }, modifier = Modifier.clickable(onClick = onEditClick))
        if (isAnobaUser) { ListItem(headlineContent = { Text(if (item.isGuideContent) stringResource(id = R.string.mycontent_guide_remove) else stringResource(id = R.string.mycontent_guide_add)) }, leadingContent = { Icon(imageVector = if (item.isGuideContent) Icons.Default.Star else Icons.Default.StarOutline, contentDescription = "Anoba", tint = AnobaGold) }, modifier = Modifier.clickable(onClick = onAnobaClick)) }
        ListItem(headlineContent = { Text(stringResource(id = R.string.mycontent_sheet_delete)) }, leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, modifier = Modifier.clickable(onClick = onDeleteClick))
    }
}

@Composable
fun ContentCard(item: MyContentItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        when (item) {
            is MyContentItem.Letter -> {
                MyContentLetterItem(letter = item.letter, onClick = onClick)
            }
            is MyContentItem.Video, is MyContentItem.Photo, is MyContentItem.Audio -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (item) {
                            is MyContentItem.Video -> Color(0xFFB3E5FC)
                            is MyContentItem.Photo -> Color(0xFFD1C4E9)
                            is MyContentItem.Audio -> Color(0xFFFFD1DC)
                            else -> Color(0xFFB3E5FC)
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (item is MyContentItem.Audio) 1f else 0.75f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (item) {
                            is MyContentItem.Video -> {
                                AsyncImage(
                                    model = item.video.thumbnailUrl,
                                    contentDescription = stringResource(id = R.string.mycontent_card_video_thumb),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_default_profile)
                                )
                                Icon(
                                    imageVector = Icons.Filled.PlayCircleOutline,
                                    contentDescription = stringResource(id = R.string.mycontent_card_play_video),
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            is MyContentItem.Photo -> {
                                AsyncImage(
                                    model = item.photo.photoUrl,
                                    contentDescription = stringResource(id = R.string.mycontent_card_user_photo),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_default_profile)
                                )
                            }
                            is MyContentItem.Audio -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.GraphicEq,
                                        contentDescription = stringResource(id = R.string.mycontent_card_audio_waveform),
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formatDuration(item.audio.duration),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        val personalNote = when(item) {
            is MyContentItem.Audio -> item.audio.personalNote
            is MyContentItem.Letter -> item.letter.personalNote
            is MyContentItem.Photo -> item.photo.personalNote
            is MyContentItem.Video -> item.video.personalNote
        }

        if (personalNote.isNotBlank()) {
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflowing by remember { mutableStateOf(false) }
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .animateContentSize()) {
                Text(
                    text = personalNote,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult -> isOverflowing = textLayoutResult.hasVisualOverflow },
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
                if (!isExpanded && isOverflowing) {
                    Text(
                        text = "… devamı",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { isExpanded = true }
                    )
                }
            }
        }
    }
}

@Composable
fun MyContentLetterItem(
    letter: LetterModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAudio = !letter.audioUrl.isNullOrBlank()
    val isSealed = letter.privacy == "SEALED"
    val isTimeCapsule = letter.unlockTimestamp != null && letter.unlockTimestamp!! > System.currentTimeMillis()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECB3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sağ Üst Rozet / İkon
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                if (isSealed && !letter.recipientUsername.isNullOrBlank()) {
                    Surface(
                        color = AnobaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp),
                                tint = Color(0xFF7B5E00)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = letter.recipientUsername ?: "..",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7B5E00),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (isAudio) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = AnobaNavy.copy(alpha = 0.35f)
                    )
                }
            }

            if (isTimeCapsule || isSealed) {
                // Kilitli / Mühürlü Kombinasyonlar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTimeCapsule) {
                        Icon(
                            imageVector = Icons.Default.WatchLater,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    if (isSealed) {
                        if (isTimeCapsule) Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    if (isAudio) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else if (isAudio) {
                // Sesli Mektup İkonu ve Süresi
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AnobaGold
                    )
                    if (letter.audioDuration != null && letter.audioDuration!! > 0L) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formatDuration(letter.audioDuration!!, leadingZero = false),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Yazılı Mektup 4 Satır + Devamı
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 14.dp, bottom = 4.dp)) {
                    if (isExpanded) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())) {
                            Text(
                                text = letter.content,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    } else {
                        Text(
                            text = letter.content,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (letter.content.length > 50) {
                            Text(
                                text = "… devamı",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFFFFECB3).copy(alpha = 0.9f))
                                    .clickable { isExpanded = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.mycontent_delete_dialog_title)) },
        text = { Text(stringResource(id = R.string.mycontent_delete_dialog_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(id = R.string.mycontent_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.mycontent_delete_dialog_cancel))
            }
        }
    )
}