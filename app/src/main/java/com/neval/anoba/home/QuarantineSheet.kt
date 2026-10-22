package com.neval.anoba.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.neval.anoba.common.utils.Constants

@Composable
fun QuarantineSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val quarantineItems by homeViewModel.quarantineContentItems.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = Modifier.zIndex(1000f)
    ) {
        BackHandler { onDismiss() }
        
        Box(
            modifier = Modifier
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
                    Spacer(Modifier.height(48.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Karantina Havuzu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = "İncelemedeki içerikler",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    if (quarantineItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Spacer(Modifier.height(12.dp))
                                Text("Şu an incelenecek içerik yok.", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(quarantineItems, key = { it.id }) { item ->
                                QuarantineGridItem(item) {
                                    val route = when (item) {
                                        is ContentItem.PhotoContent -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", item.id)
                                        is ContentItem.VideoContent -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", item.id)
                                        is ContentItem.LetterContent -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", item.id)
                                        is ContentItem.AudioContent -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", item.id)
                                        is ContentItem.MarketContent -> "market_detail_screen/${item.id}"
                                    }
                                    navController.navigate(route)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuarantineGridItem(item: ContentItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.video.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            Icons.Default.PlayCircle,
                            null,
                            modifier = Modifier.align(Alignment.Center).size(32.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                is ContentItem.LetterContent -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Description, null, tint = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.letter.content,
                            fontSize = 8.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp
                        )
                    }
                }
                is ContentItem.AudioContent -> {
                    Icon(Icons.Default.Mic, null, modifier = Modifier.size(32.dp), tint = Color.Gray)
                }
                is ContentItem.MarketContent -> {
                    AsyncImage(
                        model = item.marketItem.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Flag indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PriorityHigh, null, modifier = Modifier.size(12.dp), tint = Color.White)
            }
        }
    }
}
