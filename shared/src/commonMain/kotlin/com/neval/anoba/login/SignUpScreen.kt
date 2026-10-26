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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.GoogleLogoIcon

private val SignUpPrimaryColor = Color(0xFF232066)

@Composable
fun SignUpScreen(
    username: String = "",
    onUsernameChange: (String) -> Unit = {},
    email: String = "",
    onEmailChange: (String) -> Unit = {},
    password: String = "",
    onPasswordChange: (String) -> Unit = {},
    confirmPassword: String = "",
    onConfirmPasswordChange: (String) -> Unit = {},
    usernameError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    confirmPasswordError: String? = null,
    isLoading: Boolean = false,
    onSignUpClick: () -> Unit = {},
    onGoogleSignUpClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isFormValid = username.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            password == confirmPassword &&
            usernameError == null &&
            emailError == null &&
            passwordError == null &&
            confirmPasswordError == null

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SignUpPrimaryColor,
        focusedLabelColor = SignUpPrimaryColor,
        cursorColor = SignUpPrimaryColor
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kayıt Ol",
                style = MaterialTheme.typography.headlineMedium,
                color = SignUpPrimaryColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Google ile Hızlı Kayıt Butonu
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    onGoogleSignUpClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleLogoIcon(size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Google ile Hızlı Kayıt Ol",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ayraç
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
                Text(
                    text = "  veya e-posta ile  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kullanıcı Adı
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Kullanıcı Adı") },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(text = it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )

            Spacer(modifier = Modifier.height(8.dp))

            // E-Posta
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-Posta Adresi") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(text = it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Şifre
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Şifre") },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(text = it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = textFieldColors,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                            tint = SignUpPrimaryColor
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Şifre Onay
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
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                            tint = SignUpPrimaryColor
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isFormValid && !isLoading) {
                        focusManager.clearFocus()
                        onSignUpClick()
                    }
                })
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kayıt Ol Butonu
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSignUpClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignUpPrimaryColor,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF888888)
                ),
                enabled = isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Kayıt Ol",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zaten Hesabınız Var mı?
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Zaten bir hesabınız var mı? Giriş Yapın",
                    color = SignUpPrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
