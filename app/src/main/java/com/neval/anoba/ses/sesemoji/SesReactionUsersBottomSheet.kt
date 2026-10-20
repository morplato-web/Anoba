package com.neval.anoba.ses.sesemoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.ui.theme.AnobaGold
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

data class ReactionUserData(
    val uid: String = "",
    val displayName: String = "Anonim",
    val photoUrl: String? = null,
    val badgeType: String? = "NONE",
    val isOfficial: Boolean = false,
    val emoji: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesReactionUsersBottomSheet(
    sesId: String,
    reactions: Map<String, Long>,
    onDismiss: () -> Unit
) {
    val repository: SesEmojiReactionRepository = koinInject()
    var selectedFilterEmoji by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var reactionUsers by remember { mutableStateOf<List<ReactionUserData>>(emptyList()) }

    LaunchedEffect(sesId) {
        if (sesId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        repository.getSesReactionUsers(sesId).collectLatest { users ->
            reactionUsers = users
            isLoading = false
        }
    }

    val filteredList = remember(reactionUsers, selectedFilterEmoji) {
        if (selectedFilterEmoji == null) reactionUsers
        else reactionUsers.filter { it.emoji == selectedFilterEmoji }
    }

    val totalCount = remember(reactions, reactionUsers) {
        val countFromMap = reactions.values.sum()
        if (countFromMap > 0) countFromMap else reactionUsers.size.toLong()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.reactions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // FİLTRE ÇİPLERİ
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                item {
                    val isAllSelected = selectedFilterEmoji == null
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSelected) AnobaGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAllSelected) AnobaGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.clickable { selectedFilterEmoji = null }
                    ) {
                        Text(
                            text = "${stringResource(R.string.reactions_all)} ($totalCount)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                items(reactions.entries.filter { it.value > 0 }.toList(), key = { it.key }) { entry ->
                    val isSelected = selectedFilterEmoji == entry.key
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AnobaGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AnobaGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.clickable { selectedFilterEmoji = if (isSelected) null else entry.key }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = entry.key, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = entry.value.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // LİSTE ALANI
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AnobaGold)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.reactions_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredList, key = { "${it.uid}_${it.emoji}" }) { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                                    error = rememberVectorPainter(Icons.Default.AccountCircle)
                                )
                                Spacer(Modifier.width(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    AnobaBadge(
                                        badgeType = user.badgeType,
                                        isOfficial = user.isOfficial
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.emoji, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
