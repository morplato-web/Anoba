package com.neval.anoba.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.mycontent.MyContentScreen
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("Anoba") }
    var userEmail by remember { mutableStateOf("morplato@gmail.com") }
    var userBadgeType by remember { mutableStateOf("OFFICIAL") }
    var followerCount by remember { mutableStateOf(2) }
    var followingCount by remember { mutableStateOf(15) }

    val anobaLogoUrl = "https://raw.githubusercontent.com/morplato-web/Anoba/main/app/src/main/res/drawable/ay_yildiz.png"

    when (activeSubScreen) {
        "MY_CONTENT" -> MyContentScreen(onNavigateBack = { activeSubScreen = null })
        "EDIT" -> ProfileEditScreen(onNavigateBack = { activeSubScreen = null })
        "BLOCKED" -> BlockedUsersScreen(onNavigateBack = { activeSubScreen = null })
        else -> {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    Surface(color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                            }

                            Text(
                                text = "Profilim",
                                color = AnobaNavy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Sağda Home İkonu (Tıklandığında Ana Sayfaya Dönüş)
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
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
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    UserAvatar(
                        photoUrl = anobaLogoUrl,
                        username = username,
                        size = 90.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = username,
                            color = AnobaNavy,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AnobaBadge(
                            badgeType = userBadgeType,
                            isOfficial = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Takipçi ve Takip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$followerCount",
                                color = AnobaNavy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Takipçi",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(36.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$followingCount",
                                color = AnobaNavy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Takip",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // E-posta Bilgi Satırı
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "E-posta",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            color = AnobaNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. İÇERİKLERİM BUTONU (Dark Navy)
                    Button(
                        onClick = { activeSubScreen = "MY_CONTENT" },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.DynamicFeed, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("İçeriklerim", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. ANOBA ABLASI PANELİ
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AnobaGold)
                    ) {
                        Text("Anoba Ablası Paneli", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. PROFİLİ DÜZENLE
                    Button(
                        onClick = { activeSubScreen = "EDIT" },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
                    ) {
                        Text("Profili Düzenle", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. ENGELLENEN KULLANICILAR
                    OutlinedButton(
                        onClick = { activeSubScreen = "BLOCKED" },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AnobaNavy)
                        )
                    ) {
                        Text("Engellenen Kullanıcılar", color = AnobaNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. UYGULAMAYI KAPAT
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.LightGray)
                        )
                    ) {
                        Text("Uygulamayı Kapat", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
