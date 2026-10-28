package com.neval.anoba.drawer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.profile.SharedProfileEditScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEditScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val deleteAccountState by authViewModel.deleteAccountState.collectAsState()

    val currentEmail = remember(authState) { (authState as? AuthState.Authenticated)?.email ?: "" }
    val currentDisplayName = remember(authState) { (authState as? AuthState.Authenticated)?.displayName ?: "" }
    val profileImageUrl = remember(authState) { (authState as? AuthState.Authenticated)?.photoUrl ?: "" }

    var isUpdating by remember { mutableStateOf(false) }

    val deleteSuccessText = stringResource(id = R.string.profileedit_delete_account_success_toast)
    val deleteErrorText = stringResource(id = R.string.profileedit_delete_account_error_password)
    val nameUpdatedText = stringResource(id = R.string.profileedit_name_updated_toast)
    val photoUpdatedText = stringResource(id = R.string.profileedit_photo_updated_toast)
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
        onResult = { uri: Uri? ->
            if (uri != null) {
                isUpdating = true
                authViewModel.updateUserProfilePicture(uri) { success, msg ->
                    isUpdating = false
                    if (success) {
                        Toast.makeText(context, photoUpdatedText, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, msg ?: unknownErrorText, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    SharedProfileEditScreen(
        currentDisplayName = currentDisplayName,
        currentEmail = currentEmail,
        profileImageUrl = profileImageUrl,
        isLoading = isUpdating,
        isDeletingAccount = deleteAccountState is UiState.Loading,
        onNavigateBack = { navController.popBackStack() },
        onPickImage = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onSaveProfile = { newDisplayName ->
            if (newDisplayName.isNotBlank() && newDisplayName != currentDisplayName) {
                isUpdating = true
                authViewModel.updateUserName(newDisplayName) { success, msg ->
                    isUpdating = false
                    if (success) {
                        Toast.makeText(context, nameUpdatedText, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, msg ?: unknownErrorText, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onDeleteAccount = { password ->
            if (password.isNotBlank()) {
                authViewModel.reauthenticateAndDelete(currentEmail, password)
            }
        }
    )
}
