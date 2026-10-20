package com.neval.anoba.letter

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.User
import com.neval.anoba.ui.theme.AnobaGold
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LetterMode { WRITE, SPEAK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterCreateScreen(
    navController: NavController,
    viewModel: LetterViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val isSending by viewModel.isSending.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var newLetterContent by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(LetterMode.WRITE) }
    var isRecording by remember { mutableStateOf(false) }
    var hasFinishedRecording by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    val mediaPlayer = remember { android.media.MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try { if (mediaPlayer.isPlaying) mediaPlayer.stop() } catch (_: Exception) {}
            mediaPlayer.release()
            if (!hasFinishedRecording) {
                viewModel.audioFile?.delete()
            }
        }
    }

    var showRecipientDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var unlockTimestamp by remember { mutableStateOf<Long?>(null) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var selectedDatePickerOption by remember { mutableStateOf<String?>(null) }

    var userSearchQuery by remember { mutableStateOf("") }

    var pseudonym by remember { mutableStateOf("") }
    var isAskAnoba by remember { mutableStateOf(false) }
    var answerPrivacy by remember { mutableStateOf("PUBLIC") }

    val context = LocalContext.current

    androidx.activity.compose.BackHandler {
        viewModel.cancelRecording()
        navController.popBackStack()
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startRecording(context)
                isRecording = true
                hasFinishedRecording = false
            } else {
                Toast.makeText(context, context.getString(R.string.audiorecording_permission_needed), Toast.LENGTH_SHORT).show()
            }
        }
    )

    val draftFailedMsg = stringResource(id = R.string.lettercreate_draft_failed_toast)
    val letterTitle = stringResource(id = R.string.lettercreate_title)
    val backText = stringResource(id = R.string.lettercreate_back)
    val homeText = stringResource(id = R.string.lettercreate_home)
    val textfieldPlaceholder = stringResource(id = R.string.lettercreate_textfield_placeholder)
    val discardText = stringResource(id = R.string.lettercreate_discard)
    val selectDateError = stringResource(id = R.string.lettercreate_select_date_error)
    val timeCapsuleLabel = stringResource(id = R.string.lettercreate_time_capsule_label)
    val timeCapsuleMake = stringResource(id = R.string.lettercreate_time_capsule_make)
    val sharePublicText = stringResource(id = R.string.lettercreate_share_public)
    val sendSealedText = stringResource(id = R.string.lettercreate_send_sealed)
    val datePickerPublic = stringResource(id = R.string.lettercreate_datepicker_public)
    val datePickerSealed = stringResource(id = R.string.lettercreate_datepicker_sealed)
    val cancelAction = stringResource(id = R.string.lettercreate_cancel_action)

    val anobaTitle = stringResource(id = R.string.lettercreate_anoba_title)
    val anobaDesc = stringResource(id = R.string.lettercreate_anoba_desc)
    val pseudonymLabel = stringResource(id = R.string.lettercreate_pseudonym_label)
    val pseudonymPlaceholder = stringResource(id = R.string.lettercreate_pseudonym_placeholder)
    val privacyPublicText = stringResource(id = R.string.lettercreate_anoba_privacy_public)
    val privacyPrivateText = stringResource(id = R.string.lettercreate_anoba_privacy_private)
    val anobaSendText = stringResource(id = R.string.lettercreate_anoba_send)
    val pseudonymError = stringResource(id = R.string.lettercreate_pseudonym_error)

    val modeWriteText = stringResource(id = R.string.lettercreate_mode_write)
    val modeSpeakText = stringResource(id = R.string.lettercreate_mode_speak)
    val audioRecordedText = stringResource(id = R.string.lettercreate_audio_recorded)
    val audioRecordHint = stringResource(id = R.string.lettercreate_audio_record_hint)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                selectedDatePickerOption = null
            },
            confirmButton = { },
            dismissButton = { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Zaman Kapsülü: 1. Herkese Açık
                    OptionRow(
                        text = datePickerPublic,
                        icon = Icons.Default.Public,
                        isActive = selectedDatePickerOption == "DP_PUBLIC",
                        onClick = {
                            selectedDatePickerOption = "DP_PUBLIC"
                            val date = datePickerState.selectedDateMillis
                            if (date != null && (newLetterContent.isNotBlank() || hasFinishedRecording)) {
                                viewModel.saveLetter(
                                    content = newLetterContent,
                                    privacy = LetterPrivacy.PUBLIC,
                                    unlockTimestamp = date,
                                    pseudonym = pseudonym,
                                    isAskAnoba = isAskAnoba,
                                    audioFile = viewModel.audioFile,
                                    audioDuration = if (hasFinishedRecording) viewModel.getAudioDuration(viewModel.audioFile?.absolutePath ?: "") else null,
                                    onResult = { id ->
                                        if (!id.isNullOrBlank()) {
                                            newLetterContent = ""
                                            navController.navigate(Constants.MY_CONTENT_NAV_GRAPH)
                                        }
                                    }
                                )
                                showDatePicker = false
                                selectedDatePickerOption = null
                            } else {
                                Toast.makeText(context, selectDateError, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // Zaman Kapsülü: 2. Mühürlü (Kişiye Özel)
                    OptionRow(
                        text = datePickerSealed,
                        icon = Icons.Default.Lock,
                        isActive = selectedDatePickerOption == "DP_SEALED",
                        onClick = {
                            selectedDatePickerOption = "DP_SEALED"
                            val date = datePickerState.selectedDateMillis
                            if (date != null) {
                                unlockTimestamp = date
                                showDatePicker = false
                                selectedDatePickerOption = null
                                showRecipientDialog = true
                            } else {
                                Toast.makeText(context, selectDateError, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            showDatePicker = false
                            selectedDatePickerOption = null
                        }) {
                            Text(cancelAction, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showRecipientDialog) {
        val currentUserId = (authState as? AuthState.Authenticated)?.uid ?: ""

        LetterRecipientSelectionDialog(
            allUsers = allUsers.filter { it.uid != currentUserId },
            searchQuery = userSearchQuery,
            onSearchQueryChange = { userSearchQuery = it },
            onUserSelected = { selectedUser ->
                viewModel.saveLetter(
                    content = newLetterContent,
                    privacy = LetterPrivacy.SEALED,
                    recipientId = selectedUser.uid,
                    recipientUsername = selectedUser.displayName,
                    unlockTimestamp = unlockTimestamp,
                    pseudonym = pseudonym,
                    isAskAnoba = isAskAnoba,
                    audioFile = viewModel.audioFile,
                    audioDuration = if (hasFinishedRecording) viewModel.getAudioDuration(viewModel.audioFile?.absolutePath ?: "") else null,
                    onResult = { newLetterId ->
                        if (!newLetterId.isNullOrBlank()) {
                            newLetterContent = ""
                            userSearchQuery = ""
                            val message = context.getString(R.string.lettercreate_sealed_draft_saved_toast, selectedUser.displayName)
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            navController.navigate(Constants.MY_CONTENT_NAV_GRAPH)
                        } else {
                            Toast.makeText(context, draftFailedMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                showRecipientDialog = false
            },
            onDismiss = {
                showRecipientDialog = false
                userSearchQuery = ""
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(letterTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelRecording()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Home, contentDescription = homeText)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- MOD SEÇİCİ (Yaz / Konuş) ---
            TabRow(
                selectedTabIndex = if (selectedMode == LetterMode.WRITE) 0 else 1,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedMode == LetterMode.WRITE) 0 else 1]),
                        color = AnobaGold
                    )
                }
            ) {
                Tab(
                    selected = selectedMode == LetterMode.WRITE,
                    onClick = { selectedMode = LetterMode.WRITE },
                    text = { Text(modeWriteText, fontWeight = if (selectedMode == LetterMode.WRITE) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedMode == LetterMode.SPEAK,
                    onClick = { selectedMode = LetterMode.SPEAK },
                    text = { Text(modeSpeakText, fontWeight = if (selectedMode == LetterMode.SPEAK) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (selectedMode == LetterMode.WRITE) {
                OutlinedTextField(
                    value = newLetterContent,
                    onValueChange = { newLetterContent = it },
                    placeholder = { Text(textfieldPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                // --- SESLİ MEKTUP ARAYÜZÜ ---
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!isRecording && !hasFinishedRecording) {
                            IconButton(
                                onClick = { permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                                modifier = Modifier.size(64.dp).background(Color(0xFFFFD1DC), CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(audioRecordHint, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else if (isRecording) {
                            IconButton(
                                onClick = {
                                    viewModel.stopRecording()
                                    isRecording = false
                                    hasFinishedRecording = true
                                },
                                modifier = Modifier.size(64.dp).background(Color.Red.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Box(modifier = Modifier.size(24.dp).background(Color.Red, RoundedCornerShape(4.dp)))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Kaydediliyor...", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlayingPreview) {
                                            mediaPlayer.pause()
                                            isPlayingPreview = false
                                        } else {
                                            viewModel.audioFile?.let { file ->
                                                mediaPlayer.reset()
                                                mediaPlayer.setDataSource(file.absolutePath)
                                                mediaPlayer.prepare()
                                                mediaPlayer.start()
                                                isPlayingPreview = true
                                                mediaPlayer.setOnCompletionListener { isPlayingPreview = false }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape)
                                ) {
                                    Icon(if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color(0xFF388E3C))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(audioRecordedText, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = {
                                    viewModel.cancelRecording()
                                    hasFinishedRecording = false
                                }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = anobaTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = anobaDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAskAnoba,
                            onCheckedChange = { isAskAnoba = it }
                        )
                    }

                    AnimatedVisibility(visible = isAskAnoba) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pseudonym,
                                onValueChange = { pseudonym = it },
                                label = { Text(pseudonymLabel) },
                                placeholder = { Text(pseudonymPlaceholder) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = answerPrivacy == "PUBLIC",
                                    onClick = { answerPrivacy = "PUBLIC" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AnobaGold)
                                )
                                Text(privacyPublicText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { answerPrivacy = "PUBLIC" })

                                Spacer(Modifier.width(16.dp))

                                RadioButton(
                                    selected = answerPrivacy == "SEALED",
                                    onClick = { answerPrivacy = "SEALED" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AnobaGold)
                                )
                                Text(privacyPrivateText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { answerPrivacy = "SEALED" })
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAskAnoba) {
                    OptionRow(
                        text = anobaSendText,
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = {
                            if ((newLetterContent.isNotBlank() || hasFinishedRecording) && pseudonym.isNotBlank()) {
                                viewModel.saveLetter(
                                    content = newLetterContent,
                                    privacy = if (answerPrivacy == "PUBLIC") LetterPrivacy.PUBLIC else LetterPrivacy.SEALED,
                                    unlockTimestamp = null,
                                    pseudonym = pseudonym,
                                    isAskAnoba = true,
                                    audioFile = viewModel.audioFile,
                                    audioDuration = if (hasFinishedRecording) viewModel.getAudioDuration(viewModel.audioFile?.absolutePath ?: "") else null,
                                    onResult = { id ->
                                        if (!id.isNullOrBlank()) {
                                            newLetterContent = ""
                                            navController.navigate(Constants.MY_CONTENT_NAV_GRAPH)
                                        }
                                    }
                                )
                            } else if (pseudonym.isBlank()) {
                                Toast.makeText(context, pseudonymError, Toast.LENGTH_SHORT).show()
                            }
                        },
                        isEnabled = !isSending && (newLetterContent.isNotBlank() || hasFinishedRecording) && pseudonym.isNotBlank(),
                        isActive = true
                    )
                } else {
                    // 1. Herkese Açık
                    OptionRow(
                        text = sharePublicText,
                        icon = Icons.Default.Public,
                        onClick = {
                            selectedOption = "PUBLIC"
                            if (newLetterContent.isNotBlank() || hasFinishedRecording) {
                                viewModel.saveLetter(
                                    content = newLetterContent,
                                    privacy = LetterPrivacy.PUBLIC,
                                    unlockTimestamp = unlockTimestamp,
                                    pseudonym = pseudonym,
                                    isAskAnoba = isAskAnoba,
                                    audioFile = viewModel.audioFile,
                                    audioDuration = if (hasFinishedRecording) viewModel.getAudioDuration(viewModel.audioFile?.absolutePath ?: "") else null,
                                    onResult = { id -> if (!id.isNullOrBlank()) { newLetterContent = ""; navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) } }
                                )
                            }
                        },
                        isEnabled = !isSending && (newLetterContent.isNotBlank() || hasFinishedRecording),
                        isActive = selectedOption == "PUBLIC"
                    )

                    // 2. Mühürlü (Kişiye Özel)
                    OptionRow(
                        text = sendSealedText,
                        icon = Icons.Default.Lock,
                        onClick = {
                            selectedOption = "SEALED"
                            if (newLetterContent.isNotBlank() || hasFinishedRecording) showRecipientDialog = true
                        },
                        isEnabled = !isSending && (newLetterContent.isNotBlank() || hasFinishedRecording),
                        isActive = selectedOption == "SEALED"
                    )

                    // 3. Zaman Kapsülü
                    OptionRow(
                        text = if (unlockTimestamp == null) timeCapsuleMake else "$timeCapsuleLabel: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(unlockTimestamp!!))}",
                        icon = if (unlockTimestamp != null) Icons.Default.WatchLater else Icons.Default.DateRange,
                        onClick = {
                            selectedOption = "TIMECAPSULE"
                            showDatePicker = true
                        },
                        isActive = selectedOption == "TIMECAPSULE" || unlockTimestamp != null,
                        showClose = unlockTimestamp != null,
                        onCloseClick = {
                            unlockTimestamp = null
                            if (selectedOption == "TIMECAPSULE") selectedOption = null
                        }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        newLetterContent = ""
                        viewModel.cancelRecording()
                        navController.popBackStack()
                    },
                    enabled = !isSending
                ) {
                    Text(discardText, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun OptionRow(
    text: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    isActive: Boolean = false,
    showClose: Boolean = false,
    onCloseClick: () -> Unit = {},
    iconSize: androidx.compose.ui.unit.Dp = 18.dp
) {
    val backgroundColor = if (isActive) Color(0xFFBBDEFB) else Color(0xFFECEFF1)
    val contentColor = if (isActive) Color(0xFF0D47A1) else Color(0xFF455A64)

    Surface(
        modifier = Modifier
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
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
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