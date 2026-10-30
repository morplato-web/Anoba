package com.neval.anoba.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.R
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun SplashScreen(navController: NavHostController) {
    // Koin ile repository'i alıyoruz
    val userRepository: IUserRepository = koinInject()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        delay(2000) // 2 saniye logonun görünmesi için bekleme

        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Giriş yapmış kullanıcı yok, Login'e git
            navController.navigate(Constants.LOGIN_SCREEN) {
                popUpTo(Constants.LOGIN_NAV_GRAPH) { inclusive = true }
            }
        } else {
            // Kullanıcı var, "Beni Hatırla" kontrolü yap
            val isRememberMe = withContext(Dispatchers.IO) { userRepository.isRememberMe() }

            if (currentUser.isAnonymous || isRememberMe) {
                // Misafir girişi veya Beni Hatırla seçiliyse Ana Sayfaya git
                navController.navigate(Constants.HOME_NAV_GRAPH) {
                    popUpTo(Constants.LOGIN_NAV_GRAPH) { inclusive = true }
                }
            } else {
                // Oturumu kapat ve Login'e yönlendir
                auth.signOut()
                navController.navigate(Constants.LOGIN_SCREEN) {
                    popUpTo(Constants.LOGIN_NAV_GRAPH) { inclusive = true }
                }
            }
        }
    }

    // GÖRSEL TASARIM
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo (R.drawable.app_logo şeffaf arka planlı olmalı)
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Anoba Logo",
                modifier = Modifier.size(120.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Yükleme Çubuğu
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
        }

        // Alt Kısımdaki Yazılar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Anoba",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.splash_made_in_turkiye),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}
