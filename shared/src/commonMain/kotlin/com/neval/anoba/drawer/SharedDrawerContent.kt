@file:kotlin.jvm.JvmName("SharedDrawerContentKt")

package com.neval.anoba.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerSheetContent(
    isOpen: Boolean,
    onDismiss: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateGuide: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    if (!isOpen) return

    val anobaPhotoUrl: String? = null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterStart)
                .clickable(enabled = false) {},
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                // Header: Logo / Avatar + "Anoba" + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            photoUrl = anobaPhotoUrl,
                            username = "Anoba",
                            size = 44.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Anoba",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AnobaNavy)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(16.dp))

                // Menu Items
                Column(modifier = Modifier.weight(1f)) {
                    // 1. Profilim 👤
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = AnobaNavy) },
                        label = { Text("Profilim", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        selected = false,
                        onClick = {
                            onDismiss()
                            onNavigateProfile()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // 2. Animasyon Paneli ⭐
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFD700)) },
                        label = { Text("Animasyon Paneli", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        selected = false,
                        onClick = { onDismiss() },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // 3. Anoba Rehberi ℹ️
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = AnobaNavy) },
                        label = { Text("Anoba Rehberi", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        selected = false,
                        onClick = {
                            onDismiss()
                            onNavigateGuide()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // 4. Gizlilik Politikası 🔒
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AnobaNavy) },
                        label = { Text("Gizlilik Politikası", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        selected = false,
                        onClick = { onDismiss() },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))

                // 5. Çıkış Yap 🚪
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFEF4444)) },
                    label = { Text("Çıkış Yap", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = false,
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
    }
}
