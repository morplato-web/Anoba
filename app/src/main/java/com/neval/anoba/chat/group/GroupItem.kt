package com.neval.anoba.chat.group

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.ui.theme.AnobaNavy

@Composable
fun GroupItem(
    group: ChatGroup,
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel
) {
    val groupId = group.id.ifBlank { "defaultGroup" }
    val groupName = group.name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                groupChatViewModel.setActiveGroupId(groupId)
                navController.navigate("${Constants.GROUP_CHAT_SCREEN}/$groupId")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!group.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = group.imageUrl,
                        contentDescription = "Grup Resmi: $groupName",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    InitialsAvatar(
                        name = group.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AnobaNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (group.isPrivate) stringResource(id = R.string.groupitem_private_group) else stringResource(id = R.string.groupitem_public_group),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 84.dp, end = 16.dp),
            color = Color.LightGray.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )
    }
}