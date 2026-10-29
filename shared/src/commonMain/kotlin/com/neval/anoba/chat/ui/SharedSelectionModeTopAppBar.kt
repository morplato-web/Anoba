@file:Suppress("unused")

package com.neval.anoba.chat.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSelectionModeTopAppBar(
    selectedCount: Int,
    onCloseSelectionMode: () -> Unit,
    canDeleteAnySelected: Boolean = false,
    onDeleteSelected: () -> Unit = {},
    canCopyAnySelected: Boolean = false,
    onCopySelected: () -> Unit = {},
    canEdit: Boolean = false,
    onEditSelected: () -> Unit = {},
    canShareAnySelected: Boolean = false,
    onShareSelected: () -> Unit = {},
    canSaveAnySelected: Boolean = false,
    onSaveSelected: () -> Unit = {},
    canReply: Boolean = false,
    onReplySelected: () -> Unit = {},
    canTranscribe: Boolean = false,
    onTranscribeSelected: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text("$selectedCount seçildi") },
        navigationIcon = {
            IconButton(onClick = onCloseSelectionMode) {
                Icon(Icons.Filled.Close, contentDescription = "Kapat")
            }
        },
        actions = {
            if (canReply) {
                IconButton(onClick = onReplySelected) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Yanıtla")
                }
            }
            if (canSaveAnySelected) {
                IconButton(onClick = onSaveSelected) {
                    Icon(Icons.Filled.Download, contentDescription = "Kaydet")
                }
            }
            if (canShareAnySelected) {
                IconButton(onClick = onShareSelected) {
                    Icon(Icons.Filled.Share, contentDescription = "Paylaş")
                }
            }
            if (canCopyAnySelected) {
                IconButton(onClick = onCopySelected) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Kopyala")
                }
            }
            if (canEdit) {
                IconButton(onClick = onEditSelected) {
                    Icon(Icons.Filled.Edit, contentDescription = "Düzenle")
                }
            }
            if (canTranscribe) {
                IconButton(onClick = onTranscribeSelected) {
                    Icon(Icons.Filled.Hearing, contentDescription = "Yazıya Dök")
                }
            }
            if (canDeleteAnySelected) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier.height(56.dp)
    )
}
