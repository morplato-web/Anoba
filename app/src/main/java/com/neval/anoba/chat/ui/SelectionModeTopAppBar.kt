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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neval.anoba.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeTopAppBar(
    selectedCount: Int,
    onCloseSelectionMode: () -> Unit,
    canDeleteAnySelected: Boolean,
    onDeleteSelected: () -> Unit,
    canCopyAnySelected: Boolean,
    onCopySelected: () -> Unit,
    canEdit: Boolean,
    onEditSelected: () -> Unit,
    canShareAnySelected: Boolean,
    onShareSelected: () -> Unit,
    canSaveAnySelected: Boolean,
    onSaveSelected: () -> Unit,
    canReply: Boolean = false,
    onReplySelected: () -> Unit = {},
    canTranscribe: Boolean = false,
    onTranscribeSelected: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.chat_selection_title, selectedCount)) },
        navigationIcon = {
            IconButton(onClick = onCloseSelectionMode) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.chat_selection_close))
            }
        },
        actions = {
            // "Yanıtla" butonu
            if (canReply) {
                IconButton(onClick = onReplySelected) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = stringResource(id = R.string.chat_selection_reply))
                }
            }
            // "Kaydet" butonu
            if (canSaveAnySelected) {
                IconButton(onClick = onSaveSelected) {
                    Icon(Icons.Filled.Download, contentDescription = stringResource(id = R.string.chat_selection_save_to_gallery))
                }
            }
            //  "Paylaş" butonu
            if (canShareAnySelected) {
                IconButton(onClick = onShareSelected) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(id = R.string.chat_selection_share))
                }
            }
            // "Kopyala" butonu
            if (canCopyAnySelected) {
                IconButton(onClick = onCopySelected) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.chat_selection_copy))
                }
            }
            // "Düzenle" butonu
            if (canEdit) {
                IconButton(onClick = onEditSelected) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.chat_selection_edit))
                }
            }
            // "Yazıya Dök" butonu
            if (canTranscribe) {
                IconButton(onClick = onTranscribeSelected) {
                    Icon(
                        Icons.Filled.Hearing,
                        contentDescription = stringResource(id = R.string.chat_selection_transcribe)
                    )
                }
            }
            // "Sil" butonu (kırmızı ve en sonda)
            if (canDeleteAnySelected) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.chat_selection_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.height(56.dp)
    )
}