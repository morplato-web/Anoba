@file:kotlin.jvm.JvmName("SharedDrawerContentKt")

package com.neval.anoba.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerSheetContent(
    isOpen: Boolean,
    username: String = "Anoba",
    userProfileImageUrl: String? = null,
    isAdmin: Boolean = true,
    onDismiss: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateGuide: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    if (!isOpen) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterStart)
                .clickable(enabled = false) {},
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(18.dp)
            ) {
                // 1. ÜST BAŞLIK: Kullanıcı Avatarı + İsim + Kapat Butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                                onNavigateProfile()
                            }
                    ) {
                        UserAvatar(
                            photoUrl = userProfileImageUrl,
                            username = username.ifBlank { "Anoba" },
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = username.ifBlank { "Anoba" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AnobaNavy)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Menü Öğeleri
                Column(modifier = Modifier.weight(1f)) {
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

                    // Yalnızca Admin kullanıcılara görünür
                    if (isAdmin) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Stars, contentDescription = null, tint = AnobaGold) },
                            label = { Text("Animasyon Paneli", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                            selected = false,
                            onClick = {
                                onDismiss()
                                onOpenAdminPanel()
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AnobaNavy) },
                        label = { Text("Gizlilik Politikası", color = AnobaNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        selected = false,
                        onClick = {
                            onDismiss()
                            onOpenPrivacyPolicy()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

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