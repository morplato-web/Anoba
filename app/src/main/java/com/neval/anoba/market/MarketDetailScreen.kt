package com.neval.anoba.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.common.ui.UserAvatar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketDetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onSendMessageClick: (String, String) -> Unit, // ownerId, username
    viewModel: MarketViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id == itemId }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(color = Color(0xFF1E293B), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Text("İlan Detayı 🛒", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    if (item != null && item.ownerId == currentUserId) {
                        IconButton(onClick = {
                            viewModel.deleteItem(item.id)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("İlan bulunamadı", color = Color.White, fontSize = 16.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFF0F172A))
                ) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            text = "${item.price.toInt()} ${item.currency}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Info Content
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = item.category,
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = item.condition,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.city.ifBlank { "Tüm Türkiye" },
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Açıklama", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.description.ifBlank { "Açıklama eklenmemiş." },
                        color = Color(0xFFCBD5E1),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Seller Card
                    Text("Satıcı Bilgileri", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                photoUrl = item.userProfileImageUrl,
                                username = item.username,
                                size = 48.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = item.username,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Anoba Üyesi",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Owner or Buyer Action Buttons
                    if (item.ownerId == currentUserId) {
                        if (!item.isSold) {
                            Button(
                                onClick = { viewModel.markAsSold(item.id) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Satıldı Olarak İşaretle 🛑", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.2f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Bu Ürün Satılmıştır 🛑", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { onSendMessageClick(item.ownerId, item.username) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Satıcı İle Sohbet Et 💬", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
