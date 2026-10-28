package com.neval.anoba.letter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.R
import com.neval.anoba.models.User

@Composable
fun OptionRow(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    isEnabled: Boolean = true,
    isActive: Boolean = false,
    showClose: Boolean = false,
    onCloseClick: () -> Unit = {},
    iconSize: Dp = 18.dp,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isActive) Color(0xFFBBDEFB) else Color(0xFFECEFF1)
    val contentColor = if (isActive) Color(0xFF0D47A1) else Color(0xFF455A64)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(enabled = isEnabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                color = contentColor,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            if (showClose) {
                IconButton(onClick = onCloseClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = contentColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterRecipientSelectionDialog(
    allUsers: List<User>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredUsers by remember(searchQuery, allUsers) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                allUsers
            } else {
                allUsers.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.lettercreate_recipient_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text(stringResource(id = R.string.lettercreate_recipient_dialog_search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    itemsIndexed(
                        items = filteredUsers,
                        key = { index, user -> "recipient_${user.uid}_$index" }
                    ) { _, user: User ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(user.displayName.ifBlank { user.email })
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.lettercreate_recipient_dialog_cancel))
            }
        }
    )
}
