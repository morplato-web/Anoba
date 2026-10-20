package com.neval.anoba.letter

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterAdminScreen(
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel()
) {
    val pendingState by viewModel.pendingAnobaLetters.collectAsState()
    val answeredState by viewModel.answeredAnobaLetters.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPendingAnobaLetters()
        viewModel.loadAnsweredAnobaLetters()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.letteradmin_title), color = AnobaNavy, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AnobaNavy)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = AnobaNavy,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AnobaGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(stringResource(id = R.string.letteradmin_tab_pending), fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(stringResource(id = R.string.letteradmin_tab_answered), fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Color(0xFFFBFBFB))
        ) {
            if (selectedTabIndex == 0) {
                // BEKLEYENLER LİSTESİ
                when (val state = pendingState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AnobaGold)
                    is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyStateMessage(stringResource(id = R.string.letteradmin_empty_pending))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.data, key = { it.id }) { letter ->
                                    PendingLetterCard(
                                        letter = letter,
                                        onClick = {
                                            navController.navigate(Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", letter.id))
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteLetter(letter.id) { success ->
                                                val msg = if (success) "Mektup silindi." else "Mektup silinemedi."
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onSendResponse = { response, hidden ->
                                            viewModel.answerLetter(letter.id, response, hidden) { success ->
                                                val msg = if (success) "Cevap gönderildi!" else "Hata oluştu."
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (success) {
                                                    viewModel.loadAnsweredAnobaLetters()
                                                    viewModel.loadPendingAnobaLetters()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // CEVAPLANANLAR LİSTESİ
                when (val state = answeredState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AnobaGold)
                    is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyStateMessage("Henüz cevaplanmış bir mektup bulunmuyor.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.data, key = { it.id }) { letter ->
                                    AnsweredLetterCard(
                                        letter = letter,
                                        onClick = {
                                            navController.navigate(Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", letter.id))
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteLetter(letter.id) { success ->
                                                val msg = if (success) "Mektup silindi." else "Mektup silinemedi."
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (success) {
                                                    viewModel.loadAnsweredAnobaLetters()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✨", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingLetterCard(
    letter: LetterModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSendResponse: (String, Boolean) -> Unit
) {
    var responseText by remember { mutableStateOf("") }
    var isReplying by remember { mutableStateOf(false) }
    var isContentHidden by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.mycontent_delete_dialog_title)) },
            text = { Text(stringResource(R.string.mycontent_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .bringIntoViewRequester(bringIntoViewRequester)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF7)),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AnobaGold.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp, 24.dp).background(AnobaGold, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = letter.pseudonym ?: "Anonim Dert Ortağı",
                            style = MaterialTheme.typography.titleSmall,
                            color = AnobaNavy,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = DateTimeUtils.formatRelativeTime(context, letter.createdAt?.time ?: 0L),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = letter.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            if (letter.personalNote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Ek Not: ${letter.personalNote}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!isReplying) {
                Button(
                    onClick = {
                        isReplying = true
                        coroutineScope.launch {
                            delay(200)
                            bringIntoViewRequester.bringIntoView()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = AnobaGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cevap Yaz", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(250)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    placeholder = { Text("Yaz...", fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnobaGold,
                        unfocusedBorderColor = AnobaGold.copy(alpha = 0.3f),
                        cursorColor = AnobaGold
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isContentHidden,
                        onCheckedChange = { isContentHidden = it },
                        colors = CheckboxDefaults.colors(checkedColor = AnobaGold)
                    )
                    Text(
                        text = "Mektup içeriğini gizle (Sadece cevap görünsün)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.clickable { isContentHidden = !isContentHidden }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isReplying = false }) {
                        Text("Vazgeç", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (responseText.isNotBlank()) {
                                onSendResponse(responseText, isContentHidden)
                                isReplying = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Gönder")
                    }
                }
            }
        }
    }
}

@Composable
fun AnsweredLetterCard(
    letter: LetterModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.mycontent_delete_dialog_title)) },
            text = { Text(stringResource(R.string.mycontent_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp, 24.dp).background(Color(0xFF4CAF50), RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = letter.pseudonym ?: "Anonim Dert Ortağı",
                            style = MaterialTheme.typography.titleSmall,
                            color = AnobaNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DateTimeUtils.formatRelativeTime(context, letter.createdAt?.time ?: 0L),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = letter.content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.letteradmin_response_label),
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = AnobaGold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = letter.anobaResponse ?: "",
                        style = TextStyle(fontSize = 11.sp, lineHeight = 15.sp),
                        color = AnobaNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}