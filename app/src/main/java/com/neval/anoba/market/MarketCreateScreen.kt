package com.neval.anoba.market

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCreateScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: MarketViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val createUiState by viewModel.createUiState.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Genel") }
    var condition by remember { mutableStateOf("İkinci El") }
    var city by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(createUiState) {
        when (val state = createUiState) {
            is MarketCreateUiState.Success -> {
                Toast.makeText(context, "İlanınız başarıyla yayınlandı! 🚀", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateState()
                onSuccess()
            }
            is MarketCreateUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateState()
            }
            else -> {}
        }
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                    Text("Yeni İlan Oluştur 🛒", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker Section
            Text("1. Ürün Fotoğrafı 🖼️", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, if (selectedImageUri != null) Color(0xFFFFD700) else Color(0xFF334155), RoundedCornerShape(16.dp))
                    .clickable {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Ürün Fotoğrafı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fotoğraf Seçmek İçin Dokunun", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
            }

            // Info Fields
            Text("2. İlan Detayları 📝", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("İlan Başlığı (Örn: iPhone 13 Pro Max)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Fiyat (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Şehir (Örn: İstanbul)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Category Selection Dropdown/Chips
            Text("Kategori", color = Color(0xFF94A3B8), fontSize = 14.sp)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Elektronik", "Giyim", "Ev & Yaşam", "Kitap & Hobi", "El Yapımı", "Diğer")) { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 12.sp, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            // Condition Selection
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = condition == "İkinci El",
                    onClick = { condition = "İkinci El" },
                    label = { Text("İkinci El") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFD700),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.White
                    )
                )
                FilterChip(
                    selected = condition == "Sıfır",
                    onClick = { condition = "Sıfır" },
                    label = { Text("Sıfır / Ambalajında") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFD700),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.White
                    )
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama (Ürün durumu, detaylar vb.)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Lütfen ürün fotoğrafı seçiniz", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createMarketItem(
                        context = context,
                        imageUri = selectedImageUri!!,
                        title = title,
                        description = description,
                        priceText = priceText,
                        category = category,
                        city = city,
                        condition = condition
                    )
                },
                enabled = createUiState !is MarketCreateUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                if (createUiState is MarketCreateUiState.Loading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("İlanı Yayınla 🚀", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
