package com.neval.anoba.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.login.viewmodel.NewPasswordUiState
import com.neval.anoba.login.viewmodel.NewPasswordViewModel

@Composable
fun NewPasswordScreen(
    navController: NavHostController,
    viewModel: NewPasswordViewModel
) {
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF232066)
    val snackbarHostState = remember { SnackbarHostState() }
    val defaultErrorMessage = stringResource(id = R.string.error_unknown)

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is NewPasswordUiState.Success -> {
                val successMessage = state.message ?: state.messageRes?.let { context.getString(it) } ?: "Success"
                snackbarHostState.showSnackbar(successMessage)
                viewModel.resetState()
                navController.navigate(Constants.LOGIN_SCREEN) {
                    popUpTo(Constants.LOGIN_NAV_GRAPH) { inclusive = true }
                }
            }
            is NewPasswordUiState.Error -> {
                val errorMessage = state.message ?: state.messageRes?.let { context.getString(it) } ?: defaultErrorMessage
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.new_password_title),
                style = MaterialTheme.typography.headlineMedium,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // YENİ ŞİFRE ALANI
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text(stringResource(id = R.string.new_password_label)) },
                isError = passwordError != null,
                supportingText = {
                    Text(text = passwordError?.let { stringResource(id = it) } ?: "")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ŞİFRE TEKRAR ALANI
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                label = { Text(stringResource(id = R.string.signup_confirm_password)) },
                isError = confirmPasswordError != null,
                supportingText = {
                    Text(text = confirmPasswordError?.let { stringResource(id = it) } ?: "")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isFormValid) {
                            focusManager.clearFocus()
                            viewModel.updatePassword()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // GÜNCELLE BUTONU
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.updatePassword() 
                },
                enabled = isFormValid && uiState !is NewPasswordUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (uiState is NewPasswordUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = stringResource(id = R.string.new_password_button),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
