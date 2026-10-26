package com.neval.anoba.login

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBrandLogoIcon
import kotlinx.coroutines.delay

private val SplashPrimaryColor = Color(0xFF232066)
private val SplashFooterColor = Color.Gray

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {},
    footerText: String = "Türkiye'de Üretildi 🇹🇷",
    delayMillis: Long = 2000L
) {
    LaunchedEffect(Unit) {
        delay(delayMillis)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Anoba Logo (120dp)
            AnobaBrandLogoIcon(
                size = 120.dp,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Yükleme Göstergesi
            CircularProgressIndicator(
                color = SplashPrimaryColor,
                strokeWidth = 3.dp
            )
        }

        // Alt Kısımdaki Marka & Menşei Metni
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Anoba",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SplashPrimaryColor,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = footerText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = SplashFooterColor
                )
            }
        }
    }
}
