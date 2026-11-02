@file:kotlin.jvm.JvmName("SharedPrivacyPolicyScreenKt")

package com.neval.anoba.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPrivacyPolicyScreen(
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack
) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = AnobaNavy
                        )
                    }

                    Text(
                        text = "Gizlilik Politikası",
                        color = AnobaNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Ana Sayfa",
                            tint = AnobaNavy
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AnobaNavy,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Anoba Gizlilik Politikası / Privacy Policy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Gizlilik Politikası (TR)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Anoba uygulaması olarak gizliliğinize önem veriyoruz. Bu metin, verilerinizin nasıl işlendiğini özetler:\n\n" +
                                "• Hesap Bilgileri: Kayıt olurken verdiğiniz e-posta, isim ve profil fotoğrafı sadece hesabınızı yönetmek için kullanılır.\n" +
                                "• Paylaşımlar: Paylaştığınız video, fotoğraf ve ses kayıtları uygulamanın doğası gereği diğer kullanıcılar tarafından görülebilir.\n" +
                                "• Mesajlaşma: Sohbetleriniz güvenli bir şekilde barındırılır ve üçüncü taraflarla paylaşılmaz.\n" +
                                "• İzinler: Kamera, mikrofon ve galeri izinleri sadece siz içerik üretmek istediğinizde kullanılır.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Privacy Policy (EN)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your privacy is important to us. This policy explains how Anoba handles your data:\n\n" +
                                "• Account Info: We collect information you provide directly to us when you create an account, such as your name, email, and profile picture. This information is used solely to provide and improve our services.\n" +
                                "• Content: Any media you upload (photos, videos, audio) will be accessible to other users as per the app's features.\n" +
                                "• Security: We implement industry-standard security measures to protect your personal information.\n" +
                                "• Contact: If you have any questions, please contact us through the app support.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}
