package com.neval.anoba.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NewPasswordPrimaryColor = Color(0xFF232066)

@Composable
fun NewPasswordScreen(
    password: String = "",
    onPasswordChange: (String) -> Unit = {},
    confirmPassword: String = "",
    onConfirmPasswordChange: (String) -> Unit = {},
    passwordError: String? = null,
    confirmPasswordError: String? = null,
    isLoading: Boolean = false,
    onUpdatePasswordClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isFormValid = password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            password == confirmPassword &&
            passwordError == null &&
            confirmPasswordError == null

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NewPasswordPrimaryColor,
        focusedLabelColor = NewPasswordPrimaryColor,
        cursorColor = NewPasswordPrimaryColor
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yeni Şifre Oluştur",
                style = MaterialTheme.typography.headlineMedium,
                color = NewPasswordPrimaryColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // YENİ ŞİFRE ALANI
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Yeni Şifre") },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(text = it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = textFieldColors,
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
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                            tint = NewPasswordPrimaryColor
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ŞİFRE TEKRAR ALANI
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Şifreyi Onayla") },
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(text = it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = textFieldColors,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isFormValid && !isLoading) {
                            focusManager.clearFocus()
                            onUpdatePasswordClick()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isConfirmPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                            tint = NewPasswordPrimaryColor
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // GÜNCELLE BUTONU
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onUpdatePasswordClick()
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NewPasswordPrimaryColor,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF888888)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Şifreyi Güncelle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
