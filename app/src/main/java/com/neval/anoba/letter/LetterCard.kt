package com.neval.anoba.letter

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import com.neval.anoba.R
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRepository
import com.neval.anoba.letter.letteremoji.LetterReactionUsersBottomSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import com.neval.anoba.video.videoemoji.VideoEmojis
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.koinInject

@SuppressLint("ContextCast")
@Composable
fun LetterCard(
    letter: LetterModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    currentUserId: String = "",
    userRole: String = "MEMBER",
    isCommentSectionClickable: Boolean = false,
    cardColor: Color = Color(0xFFFFF3E0),
    isPinned: Boolean = false,
    hasNotification: Boolean = false,
    onPinClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onUserClick: ((String) -> Unit)? = null,
    imageLoader: ImageLoader = koinInject(),
    reactionRepository: LetterEmojiReactionRepository = koinInject()
) {
    val topReaction = letter.reactions.maxByOrNull { it.value }
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReactionsSheet by remember { mutableStateOf(false) }

    val reactionUsers by remember(letter.id, showReactionsSheet) {
        if (showReactionsSheet) {
            reactionRepository.getLetterReactionUsers(letter.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    if (showReactionsSheet) {
        LetterReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.mycontent_delete_dialog_title)) },
            text = { Text(stringResource(R.string.mycontent_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_cancel))
                }
            }
        )
    }

    val isAudio = !letter.audioUrl.isNullOrBlank()
    val isSealed = letter.privacy == "SEALED"
    val isTimeCapsule = letter.unlockTimestamp != null && letter.unlockTimestamp!! > System.currentTimeMillis()
    val isLocked = isSealed || isTimeCapsule

    val sealedErrorMessage = stringResource(R.string.lettercard_sealed_error)
    val timeCapsuleErrorMessage = stringResource(R.string.lettercard_time_capsule_msg)

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // 1. ÜST BAŞLIK: Profil Resmi + İsim + Pin + Tarih + Kırmızı Bildirim Noktası
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayName = if (letter.isAskAnoba && !letter.pseudonym.isNullOrBlank()) {
                letter.pseudonym
            } else {
                letter.username.ifBlank { stringResource(id = R.string.sescard_anonymous) }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(
                        if (onUserClick != null) Modifier.clickable { onUserClick.invoke(letter.ownerId) } else Modifier
                    )
                    .weight(1f)
            ) {
                UserAvatar(
                    photoUrl = letter.userProfileImageUrl,
                    username = displayName,
                    size = 28.dp,
                    imageLoader = imageLoader
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp)
                ) {

                    Text(
                        text = displayName ?: "",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = if (letter.isOfficial || !letter.pseudonym.isNullOrBlank()) AnobaGold else AnobaNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    com.neval.anoba.common.ui.AnobaBadge(
                        badgeType = letter.userBadgeType,
                        isOfficial = letter.isOfficial
                    )
                }
            }

            val displayDate = letter.publishTimestamp ?: letter.createdAt

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(onClick = onPinClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin",
                        tint = if (isPinned) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // 🚀 KIRMIZI BİLDİRİM NOKTASI
                if (hasNotification) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                }

                Text(
                    text = DateTimeUtils.formatRelativeTime(context, displayDate?.time),
                    style = TextStyle(fontSize = 9.sp),
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        // 2. MEKTUP ZARFI KARTI (160dp Sabit Yükseklik)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable {
                    val canOpen = (!isSealed && !isTimeCapsule) || currentUserId == letter.ownerId ||
                            currentUserId == letter.recipientId || userRole == "ADMIN"
                    if (canOpen) {
                        navController.navigate(Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", letter.id))
                    } else {
                        val msg = if (isTimeCapsule) timeCapsuleErrorMessage else sealedErrorMessage
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isTimeCapsule -> Color(0xFFF5F5F5)
                    isSealed -> Color(0xFFFFF9C4)
                    else -> cardColor
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val showRecipientBadge = isSealed || letter.isAskAnoba || !letter.recipientUsername.isNullOrBlank()

                if (showRecipientBadge) {
                    Surface(
                        color = AnobaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val labelText = if (letter.isAskAnoba) {
                                stringResource(id = R.string.letter_ask_anoba_label)
                            } else {
                                stringResource(id = R.string.lettercard_recipient, letter.recipientUsername ?: "..")
                            }

                            Text(
                                text = labelText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7B5E00)
                            )
                        }
                    }
                } else {
                    // Sağ Üstte Açık Mektup / Ses Rozeti
                    Icon(
                        imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 10.dp)
                            .size(16.dp),
                        tint = AnobaNavy.copy(alpha = 0.25f)
                    )
                }

                if (isLocked) {
                    // Kilitli / Mühürlü / Zaman Kapsülü Ortadaki İkonlar
                    val iconSize = if (isTimeCapsule && isSealed) 28.dp else 34.dp
                    val iconSpacing = if (isTimeCapsule && isSealed) 8.dp else 10.dp

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isTimeCapsule) {
                                Icon(
                                    imageVector = Icons.Default.WatchLater,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(iconSpacing))
                            }
                            if (isSealed) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(iconSpacing))
                            }
                            Icon(
                                imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else if (letter.isAskAnoba && letter.isContentHidden && !letter.anobaResponse.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.letter_ask_anoba_hidden_content),
                            style = TextStyle(fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else if (isAudio) {
                    // Sesli Mektup Kart İçi Görünümü
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(AnobaGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AnobaGold
                            )
                        }

                        if (letter.audioDuration != null && letter.audioDuration!! > 0L) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = formatDuration(letter.audioDuration!!),
                                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            )
                        }
                    }
                } else {
                    // Yazılı Mektup Kart İçi (4 Satır + Devamı)
                    var isLetterExpanded by remember { mutableStateOf(false) }
                    var isLetterOverflowing by remember { mutableStateOf(false) }
                    val letterCardScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .padding(top = if (showRecipientBadge) 18.dp else 0.dp)
                            .animateContentSize()
                            .then(if (isLetterExpanded) Modifier.verticalScroll(letterCardScrollState) else Modifier)
                    ) {
                        Text(
                            text = letter.content,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
                            color = Color.Black.copy(alpha = 0.8f),
                            maxLines = if (isLetterExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { textLayoutResult ->
                                isLetterOverflowing = textLayoutResult.hasVisualOverflow
                            }
                        )
                        if (!isLetterExpanded && isLetterOverflowing) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "… devamı",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { isLetterExpanded = true }
                            )
                        }
                    }
                }
            }
        }

        // ANOBA ABLASI CEVABI
        if (!letter.anobaResponse.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp)
                    .clickable {
                        navController.navigate(Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", letter.id))
                    },
                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, AnobaGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(id = R.string.letter_anoba_response_title),
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Black),
                        color = AnobaGold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = letter.anobaResponse ?: "",
                        style = TextStyle(fontSize = 11.sp, lineHeight = 16.sp),
                        color = AnobaNavy.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. ALT BİLGİ: Göz, Emoji, Paylaş, Yorum
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (letter.viewCount > 0) {
                    Icon(Icons.Default.RemoveRedEye, null, Modifier.size(14.dp), Color.Gray)
                    Text("${letter.viewCount}", Modifier.padding(start = 2.dp, end = 8.dp), Color.Gray, 10.sp)
                }

                if (topReaction != null && topReaction.value > 0) {
                    val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showReactionsSheet = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(displayEmoji, fontSize = 14.sp)
                        Text(
                            text = "${topReaction.value}", // 🚀 LİDER SKOR
                            modifier = Modifier.padding(start = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isSealed && !isTimeCapsule) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onShareClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(16.dp), AnobaNavy.copy(alpha = 0.5f))
                        if (letter.shareCount > 0) {
                            Text("${letter.shareCount}", Modifier.padding(start = 2.dp), fontWeight = FontWeight.Bold, color = AnobaNavy, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = isCommentSectionClickable) {
                            navController.navigate(Constants.LETTER_COMMENTS_ROUTE.replace("{letterId}", letter.id))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Comment, null, Modifier.size(16.dp), AnobaNavy)
                    if (letter.commentsCount > 0) {
                        Text("${letter.commentsCount}", Modifier.padding(start = 2.dp), fontWeight = FontWeight.Bold, color = AnobaNavy, fontSize = 11.sp)
                    }
                }
            }
        }

        // KİŞİSEL NOT
        if (letter.personalNote.isNotBlank()) {
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflowing by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = letter.personalNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        isOverflowing = textLayoutResult.hasVisualOverflow
                    },
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
                if (!isExpanded && isOverflowing) {
                    Text(
                        text = "… devamı",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { isExpanded = true }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}