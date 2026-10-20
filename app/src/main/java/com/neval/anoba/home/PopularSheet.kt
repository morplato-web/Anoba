package com.neval.anoba.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.ui.theme.AnobaGold

@Composable
fun PopularSheet(
    isVisible: Boolean,
    popularItems: List<ContentItem>,
    onDismiss: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterEnd),
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize().clickable(enabled = false) {}) {
                    Spacer(Modifier.height(48.dp)) // Mozaik sayfasıyla aynı boşluk
                    
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "1000+",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = AnobaGold
                            )
                            Text(
                                text = stringResource(R.string.popular_sheet_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    if (popularItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.popular_sheet_empty), color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(popularItems) { item ->
                                PopularGridItem(item = item, onClick = {
                                    onDismiss()
                                    val route = when (item) {
                                        is ContentItem.PhotoContent -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", item.photo.id)
                                        is ContentItem.VideoContent -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", item.video.id)
                                        is ContentItem.LetterContent -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", item.letter.id)
                                        is ContentItem.AudioContent -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", item.audio.id)
                                    }
                                    navController.navigate(route)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopularGridItem(item: ContentItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (item) {
                is ContentItem.PhotoContent -> {
                    AsyncImage(
                        model = item.photo.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                is ContentItem.VideoContent -> {
                    AsyncImage(
                        model = item.video.thumbnailUrl ?: item.video.videoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp),
                        tint = AnobaGold
                    )
                }
                is ContentItem.LetterContent -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF9C4)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = AnobaGold)
                    }
                }
                is ContentItem.AudioContent -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3E5F5)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color(0xFF8E24AA))
                    }
                }
            }
        }
    }
}
