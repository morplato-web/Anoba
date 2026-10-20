package com.neval.anoba.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.neval.anoba.R
import com.neval.anoba.login.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel
) {
    val emailState = remember { mutableStateOf("") }
    val resetResult by viewModel.resetResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val primaryColor = Color(0xFF232066)

    val successMessage = stringResource(id = R.string.forgot_password_success)
    val unknownErrorMessage = stringResource(id = R.string.error_unknown)
    val dynamicErrorMessage = resetResult?.errorResId?.let { stringResource(it) } ?: resetResult?.errorMessage

    // Başarı veya Hata durumunda Snackbar gösterimi
    LaunchedEffect(resetResult) {
        resetResult?.let { state ->
            val message = if (state.isSuccess) {
                successMessage
            } else {
                dynamicErrorMessage ?: unknownErrorMessage
            }
            snackbarHostState.showSnackbar(message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.login_forgot_password),
                style = MaterialTheme.typography.headlineMedium,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(id = R.string.forgot_password_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text(stringResource(id = R.string.login_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (emailState.value.isNotBlank()) {
                            focusManager.clearFocus()
                            viewModel.resetPassword(emailState.value)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = primaryColor)
            } else {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.resetPassword(emailState.value)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = emailState.value.isNotBlank()
                ) {
                    Text(
                        text = stringResource(id = R.string.forgot_password_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = stringResource(id = R.string.common_back),
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
