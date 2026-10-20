package com.neval.anoba.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.ui.theme.AnobaNavy
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val blockedUsers by authViewModel.blockedUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profilescreen_blocked_users)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.chat_no_blocked_users),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(blockedUsers, key = { it.id }) { user ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = user.safeDisplayName,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingContent = {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                InitialsAvatar(
                                    name = user.safeDisplayName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            }
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { authViewModel.unblockUser(user.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = AnobaNavy)
                            ) {
                                Text(stringResource(id = R.string.chat_unblock_user))
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}
