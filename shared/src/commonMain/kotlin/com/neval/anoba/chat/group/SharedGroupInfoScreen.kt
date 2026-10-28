@file:kotlin.jvm.JvmName("SharedGroupInfoScreenKt")

package com.neval.anoba.chat.group

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedGroupMember(
    val id: String,
    val username: String,
    val photoUrl: String? = null,
    val isOwner: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedGroupInfoScreen(
    group: SharedGroupItem?,
    members: List<SharedGroupMember> = emptyList(),
    currentUserId: String = "me",
    isMuted: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onUpdateGroupInfo: (name: String, description: String) -> Unit = { _, _ -> },
    onToggleMute: (Boolean) -> Unit = {},
    onTogglePrivacy: (Boolean) -> Unit = {},
    onAddMember: () -> Unit = {},
    onRemoveMember: (String) -> Unit = {},
    onLeaveGroup: () -> Unit = {},
    onDeleteGroup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var groupNameInput by remember(group) { mutableStateOf(group?.name ?: "") }
    var groupDescriptionInput by remember(group) { mutableStateOf(group?.description ?: "") }
    var isPrivateState by remember(group) { mutableStateOf(group?.isPrivate ?: false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }

    // Gerçek grup kurucusu kontrolü
    val isGroupOwner = group != null && group.ownerId.isNotBlank() && group.ownerId == currentUserId

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = Color.White,
            title = { Text("Grubu Sil", fontWeight = FontWeight.Bold, color = AnobaNavy) },
            text = { Text("Bu grubu ve tüm sohbet geçmişini kalıcı olarak silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGroup()
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("İptal", color = Color.Gray)
                }
            }
        )
    }

    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            containerColor = Color.White,
            title = { Text("Gruptan Ayrıl", fontWeight = FontWeight.Bold, color = AnobaNavy) },
            text = { Text("Bu gruptan ayrılmak istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmDialog = false
                        onLeaveGroup()
                    }
                ) {
                    Text("Ayrıl", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("İptal", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Grup Bilgisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isEditing = false
                                groupNameInput = group?.name ?: ""
                                groupDescriptionInput = group?.description ?: ""
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = AnobaNavy
                            )
                        }
                    },
                    actions = {
                        if (isGroupOwner && !isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Düzenle",
                                    tint = AnobaNavy
                                )
                            }
                        }
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Ana Sayfa",
                                tint = AnobaNavy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Grup Fotoğrafı (Coil 3 AsyncImage + Fallback)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(AnobaGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!group?.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = group?.imageUrl,
                        contentDescription = "Grup Resmi",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = AnobaNavy,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Grup Adı & Açıklama
            if (isEditing) {
                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = { groupNameInput = it },
                    label = { Text("Grup Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = groupDescriptionInput,
                    onValueChange = { groupDescriptionInput = it },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val trimmedName = groupNameInput.trim()
                        if (trimmedName.isNotBlank()) {
                            onUpdateGroupInfo(trimmedName, groupDescriptionInput.trim())
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
                ) {
                    Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = group?.name ?: "Grup Adı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AnobaNavy,
                    textAlign = TextAlign.Center
                )
                if (!group?.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Bildirim & Gizlilik Ayarları
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Bildirimleri Sessize Al", color = AnobaNavy)
                        }
                        Switch(
                            checked = isMuted,
                            onCheckedChange = onToggleMute,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AnobaNavy)
                        )
                    }

                    if (isGroupOwner) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Gizli Grup", color = AnobaNavy)
                            }
                            Switch(
                                checked = isPrivateState,
                                onCheckedChange = {
                                    isPrivateState = it
                                    onTogglePrivacy(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AnobaNavy)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Üye Listesi
            Text(
                text = "Üyeler (${members.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AnobaNavy,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddMember() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AnobaNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Üye Ekle", color = AnobaNavy, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    members.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(photoUrl = member.photoUrl, username = member.username, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.username, fontWeight = FontWeight.SemiBold, color = AnobaNavy)
                                if (member.isOwner) {
                                    Text("Grup Kurucusu", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            if (isGroupOwner && !member.isOwner) {
                                IconButton(onClick = { onRemoveMember(member.id) }) {
                                    Icon(Icons.Default.PersonRemove, contentDescription = "Çıkar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Grup Çıkış / Silme Butonları (Onay Tetikleyicili)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (isGroupOwner) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteConfirmDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Grubu Kalıcı Olarak Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLeaveConfirmDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gruptan Ayrıl", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}