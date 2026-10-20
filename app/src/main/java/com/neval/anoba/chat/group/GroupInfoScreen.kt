package com.neval.anoba.chat.group

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.general.GeneralChatUser
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.common.ui.OfficialBadge
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch

@Suppress("UnusedChangedValue", "UNUSED_VALUE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel,
    groupId: String,
    currentUserId: String?
) {
    val currentGroup by groupChatViewModel.activeGroup.collectAsStateWithLifecycle()
    val groupMembers by groupChatViewModel.groupMembers.collectAsStateWithLifecycle()
    val errorMessage by groupChatViewModel.errorMessage.collectAsStateWithLifecycle()
    val allUsers by groupChatViewModel.allUsers.collectAsStateWithLifecycle()
    val isMuted by groupChatViewModel.isMuted.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("") }
    var groupDescriptionInput by remember { mutableStateOf("") }
    var privacyStateSwitchEditable by remember { mutableStateOf(false) }
    var originalPrivacyState by remember { mutableStateOf(false) }

    var showDeleteGroupConfirmDialog by remember { mutableStateOf(false) }
    var showLeaveGroupConfirmDialog by remember { mutableStateOf(false) }
    var showInviteUserDialog by remember { mutableStateOf(false) }

    var showSelectMemberToRemoveDialog by remember { mutableStateOf(false) }
    var selectedUserToRemove by remember { mutableStateOf<GeneralChatUser?>(null) }

    var userSearchQuery by remember { mutableStateOf("") }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                groupChatViewModel.updateGroupImage(groupId, it)
            }
        }
    )

    LaunchedEffect(groupId) {
        if (groupId.isNotBlank()) {
            groupChatViewModel.setActiveGroupId(groupId)
        }
    }

    LaunchedEffect(currentGroup) {
        currentGroup?.let { group ->
            if (!isEditing) {
                groupNameInput = group.name
                groupDescriptionInput = group.description
                privacyStateSwitchEditable = group.isPrivate
            }
            originalPrivacyState = group.isPrivate
            groupChatViewModel.loadGroupMembers(group.id)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            groupChatViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.90f),
                shadowElevation = 2.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.groupinfo_title),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isEditing = false
                                currentGroup?.let {
                                    groupNameInput = it.name
                                    groupDescriptionInput = it.description
                                    privacyStateSwitchEditable = originalPrivacyState
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                if (isEditing) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isEditing) stringResource(id = R.string.groupinfo_cancel_edit) else stringResource(id = R.string.groupinfo_back),
                                tint = AnobaNavy
                            )
                        }
                    },
                    actions = {
                        currentGroup?.let { group ->
                            if (group.ownerId == currentUserId) {
                                if (isEditing) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            groupChatViewModel.updateGroupInfo(groupId, groupNameInput, groupDescriptionInput)
                                            groupChatViewModel.setGroupPrivacy(groupId, privacyStateSwitchEditable)
                                            isEditing = false
                                            originalPrivacyState = privacyStateSwitchEditable
                                        }
                                    }) {
                                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.groupinfo_save_changes), tint = AnobaNavy)
                                    }
                                } else {
                                    IconButton(onClick = {
                                        groupNameInput = group.name
                                        groupDescriptionInput = group.description
                                        privacyStateSwitchEditable = group.isPrivate
                                        originalPrivacyState = group.isPrivate
                                        isEditing = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.groupinfo_edit_group), tint = AnobaNavy)
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        val group = currentGroup

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.soft_gray1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (group == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AnobaNavy)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable(enabled = isEditing && group.ownerId == currentUserId) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!group.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = group.imageUrl,
                                contentDescription = stringResource(id = R.string.groupinfo_group_profile_image),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            InitialsAvatar(
                                name = group.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isEditing && group.ownerId == currentUserId) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = stringResource(id = R.string.groupinfo_change_image),
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isEditing && group.ownerId == currentUserId) {
                            OutlinedTextField(
                                value = groupNameInput,
                                onValueChange = { groupNameInput = it },
                                label = { Text(stringResource(id = R.string.groupinfo_group_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AnobaNavy,
                                    cursorColor = AnobaNavy,
                                    focusedLabelColor = AnobaNavy
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = groupDescriptionInput,
                                onValueChange = { groupDescriptionInput = it },
                                label = { Text(stringResource(id = R.string.groupinfo_group_description)) },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AnobaNavy,
                                    cursorColor = AnobaNavy,
                                    focusedLabelColor = AnobaNavy
                                )
                            )
                        } else {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AnobaNavy,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = group.description.ifBlank { "Bu grubun henüz bir açıklaması yok." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(id = R.string.groupinfo_mute_group), style = MaterialTheme.typography.bodyLarge, color = AnobaNavy)
                                }
                                Switch(
                                    checked = isMuted,
                                    onCheckedChange = { newState ->
                                        currentUserId?.let { uid ->
                                            if (newState) groupChatViewModel.muteGroup(groupId, uid)
                                            else groupChatViewModel.unmuteGroup(groupId, uid)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AnobaNavy)
                                )
                            }

                            if (group.ownerId == currentUserId) {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(id = R.string.groupinfo_is_private), style = MaterialTheme.typography.bodyLarge, color = AnobaNavy)
                                    }
                                    Switch(
                                        checked = if (isEditing) privacyStateSwitchEditable else group.isPrivate,
                                        onCheckedChange = { newState ->
                                            if (isEditing) {
                                                privacyStateSwitchEditable = newState
                                            } else {
                                                scope.launch { groupChatViewModel.setGroupPrivacy(groupId, newState) }
                                            }
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AnobaNavy)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Üyeler (${groupMembers.size})",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            if (group.ownerId == currentUserId) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showInviteUserDialog = true }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(AnobaNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Üye Ekle", style = MaterialTheme.typography.bodyLarge, color = AnobaNavy, fontWeight = FontWeight.Medium)
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }

                            groupMembers.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = group.ownerId == currentUserId && member.id != currentUserId) {
                                            selectedUserToRemove = member
                                            showSelectMemberToRemoveDialog = true
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape)) {
                                        if (!member.photoUrl.isNullOrBlank()) {
                                            AsyncImage(model = member.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            InitialsAvatar(name = member.safeDisplayName, modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = member.safeDisplayName, style = MaterialTheme.typography.bodyLarge, color = AnobaNavy, fontWeight = FontWeight.Medium)
                                        if (member.id == group.ownerId) {
                                            Text(text = "Grup Kurucusu", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    if (group.ownerId == currentUserId && member.id != currentUserId) {
                                        Icon(Icons.Default.PersonRemove, contentDescription = "Üyeyi Çıkar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                    }
                                }
                                if (member != groupMembers.last()) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(start = 68.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        if (group.ownerId == currentUserId) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showDeleteGroupConfirmDialog = true }.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(id = R.string.groupinfo_delete_group_permanently), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showLeaveGroupConfirmDialog = true }.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(id = R.string.groupinfo_leave_group), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showInviteUserDialog) {
        val memberIds = remember(groupMembers, currentGroup) {
            val fromMembers = groupMembers.flatMap { listOf(it.id, it.uid ?: "", it.userId ?: "").filter { id -> id.isNotBlank() } }
            val fromGroup = currentGroup?.members ?: emptyList()
            (fromMembers + fromGroup).toSet()
        }
        UserSelectionDialog(
            title = stringResource(id = R.string.groupinfo_dialog_invite_member_title),
            users = allUsers,
            currentMemberIds = memberIds,
            currentUserId = currentUserId,
            searchQuery = userSearchQuery,
            onSearchQueryChange = { userSearchQuery = it },
            onUserSelected = { user ->
                val targetId = user.uid?.takeIf { it.isNotBlank() } ?: user.id
                scope.launch { groupChatViewModel.inviteUserToGroup(groupId, targetId, user.safeDisplayName) }
                showInviteUserDialog = false
            },
            onDismiss = { showInviteUserDialog = false }
        )
    }

    if (showDeleteGroupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteGroupConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(id = R.string.groupinfo_dialog_delete_group_title), color = AnobaNavy) },
            text = { Text(stringResource(id = R.string.groupinfo_dialog_delete_group_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            groupChatViewModel.deleteGroup(groupId)
                            showDeleteGroupConfirmDialog = false
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(id = R.string.groupinfo_dialog_confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupConfirmDialog = false }) { Text(stringResource(id = R.string.groupinfo_dialog_cancel), color = Color.Gray) }
            }
        )
    }

    if (showLeaveGroupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveGroupConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(id = R.string.groupinfo_leave_group), color = AnobaNavy) },
            text = { Text(stringResource(id = R.string.groupinfo_leave_group_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        groupChatViewModel.leaveGroup(groupId) { success ->
                            if (success) {
                                showLeaveGroupConfirmDialog = false
                                navController.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(id = R.string.groupinfo_dialog_confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveGroupConfirmDialog = false }) { Text(stringResource(id = R.string.groupinfo_dialog_cancel), color = Color.Gray) }
            }
        )
    }

    if (showSelectMemberToRemoveDialog && selectedUserToRemove != null) {
        AlertDialog(
            onDismissRequest = { showSelectMemberToRemoveDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(id = R.string.groupinfo_dialog_remove_member_confirmation_title), color = AnobaNavy) },
            text = { Text(stringResource(id = R.string.groupinfo_dialog_remove_member_confirmation_text, selectedUserToRemove?.displayName ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUserToRemove?.let {
                            scope.launch {
                                groupChatViewModel.removeUserFromGroup(groupId, it.id, it.safeDisplayName)
                                selectedUserToRemove = null
                                showSelectMemberToRemoveDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(id = R.string.groupinfo_dialog_confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showSelectMemberToRemoveDialog = false }) { Text(stringResource(id = R.string.groupinfo_dialog_cancel), color = Color.Gray) }
            }
        )
    }
}

@Composable
private fun UserSelectionDialog(
    title: String,
    users: List<GeneralChatUser>,
    currentMemberIds: Set<String>,
    currentUserId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onUserSelected: (GeneralChatUser) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredUsers by remember(searchQuery, users, currentMemberIds) {
        derivedStateOf {
            val availableUsers = users.filter { user ->
                user.id !in currentMemberIds &&
                        (user.uid == null || user.uid !in currentMemberIds) &&
                        user.id != currentUserId &&
                        user.uid != currentUserId
            }

            if (searchQuery.isNotBlank()) {
                availableUsers.filter {
                    it.safeDisplayName.contains(searchQuery, ignoreCase = true) ||
                            (it.username?.contains(searchQuery, ignoreCase = true) == true)
                }
            } else {
                availableUsers
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, color = AnobaNavy, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(id = R.string.userselection_dialog_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.userselection_dialog_search_icon)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.userselection_dialog_clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnobaNavy,
                        cursorColor = AnobaNavy,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredUsers.isEmpty()) {
                    Text(stringResource(id = R.string.userselection_dialog_no_results), modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        itemsIndexed(
                            items = filteredUsers,
                            key = { index, user ->
                                user.id.takeIf { it.isNotBlank() } ?: "member_$index"
                            }
                        ) { _, user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                ) {
                                    if (!user.photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = user.photoUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        InitialsAvatar(
                                            name = user.safeDisplayName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.safeDisplayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.displayName?.equals("anoba", ignoreCase = true) == true) AnobaGold else AnobaNavy
                                        )
                                        if (user.displayName?.equals("anoba", ignoreCase = true) == true) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            OfficialBadge(size = 14.dp)
                                        }
                                    }
                                    val handle = user.username?.takeIf { it.isNotBlank() }
                                    if (!handle.isNullOrBlank()) {
                                        Text(
                                            text = "@$handle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(start = 60.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.userselection_dialog_close), color = Color.Gray)
            }
        }
    )
}