package com.neval.anoba.drawer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaNavy
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authState by authViewModel.authState.collectAsState()
    val deleteAccountState by authViewModel.deleteAccountState.collectAsState()

    val currentEmail = remember(authState) { (authState as? AuthState.Authenticated)?.email ?: "" }
    val currentDisplayName = remember(authState) { (authState as? AuthState.Authenticated)?.displayName ?: "" }
    val profileImageUrl = remember(authState) { (authState as? AuthState.Authenticated)?.photoUrl ?: "" }

    var newDisplayName by remember { mutableStateOf(currentDisplayName) }
    var isUpdating by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var isDeletePasswordVisible by remember { mutableStateOf(false) }

    val deleteSuccessText = stringResource(id = R.string.profileedit_delete_account_success_toast)
    val deleteErrorText = stringResource(id = R.string.profileedit_delete_account_error_password)
    val enterPasswordWarningText = stringResource(id = R.string.profileedit_enter_password_warning)
    val profileUpdatedText = stringResource(id = R.string.profileedit_profile_updated_toast)
    val photoUpdatedText = stringResource(id = R.string.profileedit_photo_updated_toast)
    val nameUpdatedText = stringResource(id = R.string.profileedit_name_updated_toast)
    val unknownErrorText = stringResource(id = R.string.unknown_error)

    LaunchedEffect(deleteAccountState) {
        when (val state = deleteAccountState) {
            is UiState.Success<*> -> {
                Toast.makeText(context, deleteSuccessText, Toast.LENGTH_LONG).show()
                navController.navigate(Constants.LOGIN_NAV_GRAPH) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                authViewModel.resetDeleteAccountState()
            }
            is UiState.Error -> {
                val msg = state.message
                Toast.makeText(context, msg.ifBlank { deleteErrorText }, Toast.LENGTH_SHORT).show()
                authViewModel.resetDeleteAccountState()
            }
            else -> {}
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> if (uri != null) selectedImageUri = uri }
    )

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (deleteAccountState !is UiState.Loading) showDeleteConfirmDialog = false },
            title = { Text(stringResource(id = R.string.profileedit_delete_account_confirm_title), color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text(stringResource(id = R.string.profileedit_delete_account_confirm_text))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text(stringResource(id = R.string.profileedit_delete_account_password_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = deleteAccountState !is UiState.Loading,
                        visualTransformation = if (isDeletePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (isDeletePasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { isDeletePasswordVisible = !isDeletePasswordVisible }) {
                                Icon(icon, contentDescription = null)
                            }
                        }
                    )
                    if (deleteAccountState is UiState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(id = R.string.profileedit_deleting_process))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = deleteAccountState !is UiState.Loading,
                    onClick = {
                        if (deletePassword.isNotBlank()) {
                            authViewModel.reauthenticateAndDelete(currentEmail, deletePassword)
                        } else {
                            Toast.makeText(context, enterPasswordWarningText, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.profileedit_delete_permanently))
                }
            },
            dismissButton = {
                TextButton(enabled = deleteAccountState !is UiState.Loading, onClick = {
                    showDeleteConfirmDialog = false
                    authViewModel.resetDeleteAccountState()
                }) {
                    Text(stringResource(id = R.string.profileedit_cancel))
                }
            }
        )
    }

    fun handleSave() {
        isUpdating = true
        if (selectedImageUri != null) {
            authViewModel.updateUserProfilePicture(selectedImageUri!!) { success: Boolean, msg: String? ->
                if (success) {
                    if (newDisplayName != currentDisplayName && newDisplayName.isNotBlank()) {
                        authViewModel.updateUserName(newDisplayName) { nameSuccess: Boolean, _: String? ->
                            isUpdating = false
                            if (nameSuccess) {
                                Toast.makeText(context, profileUpdatedText, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    } else {
                        isUpdating = false
                        Toast.makeText(context, photoUpdatedText, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } else {
                    isUpdating = false
                    Toast.makeText(context, msg ?: unknownErrorText, Toast.LENGTH_SHORT).show()
                }
            }
        } else if (newDisplayName != currentDisplayName && newDisplayName.isNotBlank()) {
            authViewModel.updateUserName(newDisplayName) { success, _ ->
                isUpdating = false
                if (success) {
                    Toast.makeText(context, nameUpdatedText, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profileedit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).clickable {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                contentAlignment = Alignment.BottomEnd
            ) {
                val imageModel = remember(selectedImageUri, profileImageUrl) {
                    selectedImageUri ?: profileImageUrl.ifBlank { R.drawable.ic_default_profile }
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).padding(6.dp)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = newDisplayName,
                onValueChange = { newDisplayName = it },
                label = { Text(stringResource(id = R.string.profileedit_display_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnobaNavy,
                    cursorColor = AnobaNavy,
                    focusedLabelColor = AnobaNavy
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (!isUpdating && (newDisplayName != currentDisplayName || selectedImageUri != null)) {
                        handleSave()
                    }
                })
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    handleSave()
                },
                enabled = !isUpdating && (newDisplayName != currentDisplayName || selectedImageUri != null),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
            ) {
                if (isUpdating) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text(stringResource(id = R.string.profileedit_save_changes), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.profileedit_delete_account_and_data), fontWeight = FontWeight.Bold)
            }
        }
    }
}