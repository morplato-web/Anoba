package com.neval.anoba.market

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.R
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketDetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onSellerClick: (String) -> Unit = {},
    onSendMessageClick: (String, String) -> Unit, // ownerId, username
    viewModel: MarketViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id == itemId }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Silme Onay Penceresi (Evet / Hayır)
    if (showDeleteConfirmDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.market_delete_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text(stringResource(R.string.market_delete_confirm), color = Color(0xFFCBD5E1), fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteItem(item.id)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.market_yes_delete), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.market_cancel), color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Şikayet Etme Diyaloğu
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.market_report_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val reportSuccessText = stringResource(R.string.market_report_success)
                    val reasons = listOf("Yanıltıcı / Sahte İlan", "Yasaklı / Uygunsuz Ürün", "Dolandırıcı / Hatalı Fiyat", "Diğer")
                    reasons.forEach { reason ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showReportDialog = false
                                    Toast.makeText(context, reportSuccessText, Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = reason,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(stringResource(R.string.market_cancel), color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

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
                        Text(stringResource(R.string.market_detail_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item != null) {
                            if (item.ownerId == currentUserId) {
                                IconButton(onClick = { onEditClick(item.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFFFFD700))
                                }
                                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444))
                                }
                            } else {
                                IconButton(onClick = {
                                    val shareIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Anoba Pazar Yeri'nde ilana göz atın: ${item.title} - ${item.price.toInt()} TL")
                                        type = "text/plain"
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, null))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = Color.White)
                                }
                                IconButton(onClick = { showReportDialog = true }) {
                                    Icon(Icons.Default.Flag, contentDescription = "Şikayet Et", tint = Color(0xFFFFD700))
                                }
                            }
                        }

                        IconButton(onClick = onHomeClick) {
                            Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = Color.White)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (item != null) {
                Surface(
                    color = Color(0xFF1E293B),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
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
                                    Text(stringResource(R.string.market_mark_as_sold), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stringResource(R.string.market_item_is_sold), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
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
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.market_chat_seller), color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
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

                    // Row 1: Başlık (Sol) + Zaman Damgası (Sağ)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        val timeStr = getRelativeTimeString(item.createdAt)
                        if (timeStr.isNotBlank()) {
                            Text(
                                text = timeStr,
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Şehir Bilgisi (Sağa Yatkın)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                    Text(stringResource(R.string.market_seller_info), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSellerClick(item.ownerId) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        text = stringResource(R.string.market_seller_label),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
