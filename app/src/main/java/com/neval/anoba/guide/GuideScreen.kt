package com.neval.anoba.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.neval.anoba.home.ContentItem
import com.neval.anoba.mete.MeteSheet
import com.neval.anoba.mosaic.MosaicSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    navController: NavController,
    viewModel: GuideViewModel = koinViewModel()
) {
    val guideContent by viewModel.guideContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val brandNavy = Color(0xFF232066)

    var showMeteSheet by remember { mutableStateOf(false) }
    var showMosaicSheet by remember { mutableStateOf(false) }

    if (showMeteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMeteSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            MeteSheet()
        }
    }

    MosaicSheet(isVisible = showMosaicSheet, onDismiss = { showMosaicSheet = false })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.guide_title),
                        color = brandNavy,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = brandNavy
                        )
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
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.mycontent_home),
                            tint = brandNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 0. INFO (REHBER BİLGİ)
                item {
                    SpecialGuideItem(
                        title = stringResource(id = R.string.guide_title),
                        icon = Icons.Default.Info,
                        containerColor = Color(0xFFA5D6A7),
                        iconColor = Color.Black,
                        onClick = { /* Zaten buradayız */ }
                    )
                }

                // 1. DÜNYA (HARİTA)
                item {
                    SpecialGuideItem(
                        title = stringResource(id = R.string.map_title),
                        icon = Icons.Default.Language,
                        containerColor = Color(0xFFE3F2FD),
                        iconColor = Color(0xFF1976D2),
                        onClick = { navController.navigate(Constants.GLOBAL_MAP_SCREEN) }
                    )
                }

                // 2. METE (AY-YILDIZ)
                item {
                    SpecialGuideItem(
                        title = "Mete",
                        painter = painterResource(id = R.drawable.ay_yildiz),
                        containerColor = Color(0xFFF0F4C3),
                        iconColor = Color.Unspecified,
                        onClick = { showMeteSheet = true }
                    )
                }

                // 3. MOZAİK
                item {
                    SpecialGuideItem(
                        title = stringResource(id = R.string.mosaic_title_caps),
                        icon = Icons.Default.GridView,
                        containerColor = Color(0xFF37474F),
                        iconColor = Color.White,
                        onClick = { showMosaicSheet = true }
                    )
                }

                // NORMAL İÇERİKLER
                items(guideContent, key = { it.id + it.contentType }) { item ->
                    GuideGridItem(
                        item = item,
                        onClick = {
                            val route = when (item) {
                                is ContentItem.LetterContent -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", item.letter.id)
                                is ContentItem.VideoContent -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", item.video.id)
                                is ContentItem.PhotoContent -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", item.photo.id)
                                is ContentItem.AudioContent -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", item.audio.id)
                            }
                            navController.navigate(route)
                        }
                    )
                }
            }

            if (isLoading && guideContent.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = brandNavy
                )
            }
        }
    }
}

@Composable
fun SpecialGuideItem(
    title: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(32.dp))
            } else if (painter != null) {
                Image(painter = painter, contentDescription = title, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (iconColor == Color.Unspecified) Color.DarkGray else iconColor,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GuideGridItem(
    item: ContentItem,
    onClick: () -> Unit
) {
    val brandNavy = Color(0xFF232066)
    val cardColor = when (item) {
        is ContentItem.VideoContent -> Color(0xFFF3E5F5)
        is ContentItem.PhotoContent -> Color(0xFFFFEBEE)
        is ContentItem.AudioContent -> Color(0xFFFFD1DC)
        is ContentItem.LetterContent -> Color(0xFFFFF3E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (item) {
                is ContentItem.VideoContent -> {
                    AsyncImage(
                        model = item.video.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(Icons.Filled.PlayCircleOutline, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
                }
                is ContentItem.PhotoContent -> {
                    AsyncImage(
                        model = item.photo.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                is ContentItem.AudioContent -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(32.dp), tint = brandNavy)
                        Spacer(Modifier.height(4.dp))
                        Text(text = formatDuration(item.audio.duration), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = brandNavy)
                    }
                }
                is ContentItem.LetterContent -> {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(22.dp), tint = brandNavy)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.letter.content,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            color = brandNavy
                        )
                    }
                }
            }
        }
    }
}
