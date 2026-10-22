package com.neval.anoba

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.ui.theme.AnobaTheme

@Composable
fun App() {
    AnobaTheme {
        var currentTab by remember { mutableStateOf(0) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentTab) {
                        0 -> AnobaHomeView()
                        1 -> AnobaMarketView()
                        2 -> AnobaChatView()
                        3 -> AnobaProfileView()
                    }
                }

                // Custom Bulletproof Bottom Navigation Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1E293B),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavItem("🏠", "Ana Sayfa", currentTab == 0) { currentTab = 0 }
                        NavItem("🛒", "Pazar Yeri", currentTab == 1) { currentTab = 1 }
                        NavItem("💬", "Sohbet", currentTab == 2) { currentTab = 2 }
                        NavItem("👤", "Profil", currentTab == 3) { currentTab = 3 }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun AnobaHomeView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Upper Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Anoba iOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Connect, Chat & Time Capsule", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Global Feed Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚀", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "177 Ülkede Canlı Akış",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Anoba'nın tüm ortak mimarisi (Room Veritabanı, KMP Modülleri, Takip Sistemi ve Çoklu Dil Desteği) iOS üzerinde aktif edildi.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🌍 11 Dil & Local-First Mimarisi Aktif",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnobaMarketView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anoba Pazar Yeri",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ürün İlanları & Doğrudan Satıcı Mesajlaşması",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnobaChatView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💬", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anoba Sohbet & Gruplar",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Genel ve Özel Sohbet Odaları iOS Entegrasyonunda",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnobaProfileView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👤", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kullanıcı Profili",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Yayınlanan İçerikler, Taslaklar ve Ayarlar",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
