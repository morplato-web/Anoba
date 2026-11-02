@file:kotlin.jvm.JvmName("SharedLoginScreenKt")

package com.neval.anoba.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.GoogleLogoIcon

private val LoginPrimaryColor = Color(0xFF232066)
private val AnobaGoldColor = Color(0xFFFFD700)
private const val ANOBA_OFFICIAL_AVATAR = "https://firebasestorage.googleapis.com/v0/b/anoba-cd9de.appspot.com/o/profile_photos%2Fanoba_official.jpg?alt=media"

@Composable
fun AuthContainerView(
    onLoginSuccess: (email: String, username: String, photoUrl: String?) -> Unit = { _, _, _ -> },
) {
    var authMode by remember { mutableStateOf("LOGIN") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    var forgotEmail by remember { mutableStateOf("") }

    when (authMode) {
        "SIGN_UP" -> {
            SharedSignUpScreen(
                username = regUsername,
                onUsernameChange = { regUsername = it },
                email = regEmail,
                onEmailChange = { regEmail = it },
                password = regPassword,
                onPasswordChange = { regPassword = it },
                confirmPassword = regConfirmPassword,
                onConfirmPasswordChange = { regConfirmPassword = it },
                onNavigateBack = { authMode = "LOGIN" },
                onSignUpClick = {
                    val derivedName = regUsername.ifBlank { regEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }
                    val photoUrl = if (regEmail.trim().equals("morplato@gmail.com", ignoreCase = true)) ANOBA_OFFICIAL_AVATAR else null
                    onLoginSuccess(regEmail.trim(), derivedName, photoUrl)
                },
                onGoogleSignUpClick = {
                    val actualEmail = regEmail.trim().ifBlank { "morplato@gmail.com" }
                    val isOfficial = actualEmail.equals("morplato@gmail.com", ignoreCase = true)
                    val photoUrl = if (isOfficial) ANOBA_OFFICIAL_AVATAR else null
                    val name = if (isOfficial) "Anoba" else regUsername.ifBlank { actualEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }
                    onLoginSuccess(actualEmail, name, photoUrl)
                },
            )
        }
        "FORGOT_PASSWORD" -> {
            SharedForgotPasswordScreen(
                email = forgotEmail,
                onEmailChange = { forgotEmail = it },
                onNavigateBack = { authMode = "LOGIN" },
                onResetPasswordClick = {
                    authMode = "LOGIN"
                },
            )
        }
        else -> {
            SharedLoginScreen(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
                onLoginClick = {
                    val trimmedEmail = email.trim()
                    val isOfficial = trimmedEmail.equals("morplato@gmail.com", ignoreCase = true)
                    val derivedName = if (isOfficial) {
                        "Anoba"
                    } else if (trimmedEmail.contains("@")) {
                        trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                    } else {
                        "Kullanıcı"
                    }
                    val photoUrl = if (isOfficial) ANOBA_OFFICIAL_AVATAR else null
                    onLoginSuccess(trimmedEmail, derivedName, photoUrl)
                },
                onGoogleLoginClick = {
                    val actualEmail = email.trim().ifBlank { "morplato@gmail.com" }
                    val isOfficial = actualEmail.equals("morplato@gmail.com", ignoreCase = true)
                    val derivedName = if (isOfficial) "Anoba" else actualEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                    val photoUrl = if (isOfficial) ANOBA_OFFICIAL_AVATAR else null
                    onLoginSuccess(actualEmail, derivedName, photoUrl)
                },
                onGuestLoginClick = {
                    onLoginSuccess("misafir@anoba.com", "Misafir", null)
                },
                onForgotPasswordClick = { authMode = "FORGOT_PASSWORD" },
                onSignUpClick = { authMode = "SIGN_UP" },
            )
        }
    }
}

@Composable
fun SharedLoginScreen(
    email: String = "",
    onEmailChange: (String) -> Unit = {},
    password: String = "",
    onPasswordChange: (String) -> Unit = {},
    rememberMe: Boolean = false,
    onRememberMeChange: (Boolean) -> Unit = {},
    isLoading: Boolean = false,
    onLoginClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onGuestLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Anoba'ya Hoş Geldiniz",
                style = MaterialTheme.typography.displaySmall.copy(letterSpacing = 1.sp),
                color = LoginPrimaryColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onGoogleLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleLogoIcon(size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Google ile Devam Et",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.6f),
                )
                Text(
                    text = "  veya  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-posta") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Şifre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                            tint = LoginPrimaryColor,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(checkedColor = LoginPrimaryColor),
                    )
                    Text(
                        text = "Beni Hatırla",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        text = "Şifremi Unuttum?",
                        color = LoginPrimaryColor,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginPrimaryColor,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF888888),
                ),
            ) {
                Text(
                    text = "Giriş",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGuestLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, LoginPrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AnobaGoldColor),
            ) {
                Text(
                    text = "Misafir Girişi",
                    color = LoginPrimaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onSignUpClick) {
                Text(
                    text = "Kayıt Ol",
                    color = LoginPrimaryColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}