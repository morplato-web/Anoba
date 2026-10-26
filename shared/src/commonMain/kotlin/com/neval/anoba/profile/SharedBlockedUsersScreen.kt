package com.neval.anoba.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedBlockedUser(
    val id: String,
    val username: String,
    val photoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    onNavigateBack: () -> Unit = {}
) {
    val sampleBlockedUsers = remember {
        mutableStateListOf<SharedBlockedUser>()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                    Text("Engellenen Kullanıcılar 🚫", color = AnobaNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        if (sampleBlockedUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Engellenen kullanıcı bulunmuyor ✨",
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
                items(sampleBlockedUsers, key = { it.id }) { user ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = user.username,
                                fontWeight = FontWeight.SemiBold,
                                color = AnobaNavy
                            )
                        },
                        leadingContent = {
                            UserAvatar(
                                photoUrl = user.photoUrl,
                                username = user.username,
                                size = 40.dp
                            )
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { sampleBlockedUsers.remove(user) },
                                colors = ButtonDefaults.textButtonColors(contentColor = AnobaNavy)
                            ) {
                                Text("Engeli Kaldır")
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}
