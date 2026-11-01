package com.neval.anoba.photo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold

private val AnobaNavyLight = Color(0xFF1E293B)
private val TextWhiteSubtle = Color(0xFFCBD5E1)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SharedPhotoCard(
    photo: SharedPhotoModel,
    isPinned: Boolean = false,
    canDelete: Boolean = false,
    onPinClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onDoubleTap: () -> Unit = onLikeClick,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnobaNavyLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Başlık & Kullanıcı Barı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(
                        photoUrl = photo.userProfileImageUrl,
                        username = photo.username,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = photo.username,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AnobaBadge(
                                badgeType = photo.userBadgeType,
                                isOfficial = photo.isOfficial
                            )
                        }
                        if (photo.title.isNotBlank()) {
                            Text(
                                text = photo.title,
                                color = TextWhiteSubtle,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Eylemler: Pin ve Sil
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPinClick) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Sabitle",
                            tint = if (isPinned) AnobaGold else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Ana Görsel Alanı (Double Tap & Tıklama destekli)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
                    .combinedClickable(
                        onClick = onPhotoClick,
                        onDoubleClick = onDoubleTap
                    )
            ) {
                if (photo.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photo.photoUrl,
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Görsel Yüklenemedi", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            // Kişisel Not / Açıklama
            if (photo.personalNote.isNotBlank()) {
                Text(
                    text = photo.personalNote,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Alt Bar: Beğeni, Yorum, Görüntülenme Sayaçları
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(onClick = onLikeClick)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (photo.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (photo.isLikedByMe) Color(0xFFEF4444) else TextWhiteSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${photo.reactionCount}",
                            color = TextWhiteSubtle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(onClick = onCommentClick)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Yorumlar",
                            tint = TextWhiteSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${photo.commentsCount}",
                            color = TextWhiteSubtle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Görüntülenme Sayacı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Görüntülenme",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${photo.viewCount}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
