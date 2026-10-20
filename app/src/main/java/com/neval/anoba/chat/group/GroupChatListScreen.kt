package com.neval.anoba.chat.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatListScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel,
) {
    val groups by groupChatViewModel.groups.collectAsStateWithLifecycle()
    val currentUserId = groupChatViewModel.currentUserId
    val scope = rememberCoroutineScope()

    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }
    var isPrivate by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val errorNameEmpty = stringResource(id = R.string.groupchatlist_error_name_empty)
    val errorNoUser = stringResource(id = R.string.groupchatlist_error_no_user)
    val defaultDescription = stringResource(id = R.string.groupchatlist_default_description)
    val successGroupCreated = stringResource(id = R.string.groupchatlist_success_group_created)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.soft_gray1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.90f),
                    shadowElevation = 4.dp
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.groupchatlist_title),
                                fontWeight = FontWeight.Bold,
                                color = AnobaNavy
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.groupchatlist_back), tint = AnobaNavy)
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Constants.HOME_SCREEN) }) {
                                Icon(Icons.Filled.Home, contentDescription = stringResource(id = R.string.groupchatlist_home), tint = AnobaNavy)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = AnobaNavy,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.groupchatlist_add_group))
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = R.string.groupchatlist_no_groups),
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(items = groups, key = { it.id.ifBlank { UUID.randomUUID().toString() } }) { group ->
                            GroupItem(
                                group = group,
                                navController = navController,
                                groupChatViewModel = groupChatViewModel
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = stringResource(id = R.string.groupchatlist_dialog_create_new_group),
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text(stringResource(id = R.string.groupchatlist_dialog_group_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AnobaNavy,
                                focusedLabelColor = AnobaNavy,
                                cursorColor = AnobaNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = groupDescription,
                            onValueChange = { groupDescription = it },
                            label = { Text(stringResource(id = R.string.groupchatlist_dialog_group_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AnobaNavy,
                                focusedLabelColor = AnobaNavy,
                                cursorColor = AnobaNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.groupchatlist_dialog_private_group),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AnobaNavy
                            )
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                enabled = !isLoading,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AnobaNavy,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            )
                        }
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        if (successMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = successMessage!!, color = AnobaNavy, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmedName = groupName.trim()
                            when {
                                trimmedName.isBlank() -> errorMessage = errorNameEmpty
                                currentUserId.isNullOrBlank() -> errorMessage = errorNoUser
                                else -> {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch {
                                        val createdGroupId = groupChatViewModel.createGroup(
                                            trimmedName,
                                            isPrivate,
                                            groupDescription.ifBlank { defaultDescription }
                                        )
                                        isLoading = false
                                        if (createdGroupId != null) {
                                            successMessage = successGroupCreated
                                            delay(1500)
                                            successMessage = null
                                            groupName = ""
                                            groupDescription = ""
                                            isPrivate = false
                                            showDialog = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(stringResource(id = R.string.groupchatlist_dialog_create), color = AnobaNavy, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(id = R.string.groupchatlist_dialog_cancel), color = Color.Gray)
                    }
                }
            )
        }
    }
}