@file:kotlin.jvm.JvmName("SharedMarketCreateScreenKt")

package com.neval.anoba.market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val TURKEY_PROVINCES = listOf(
    "İstanbul", "Ankara", "İzmir", "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Antalya", "Artvin",
    "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı",
    "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun",
    "Gümüşhane", "Hakkari", "Hatay", "Isparta", "Mersin", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir",
    "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir",
    "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat",
    "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman",
    "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
)

private val GLOBAL_CITIES = listOf(
    "Baku, Azerbaycan 🇦🇿", "Berlin, Almanya 🇩🇪", "London, İngiltere 🇬🇧", "Paris, Fransa 🇫🇷",
    "Tashkent, Özbekistan 🇺🇿", "Jakarta, Endonezya 🇮🇩", "New York, ABD 🇺🇸", "Amsterdam, Hollanda 🇳🇱",
    "Tokyo, Japonya 🇯🇵", "Seoul, Güney Kore 🇰🇷", "Kuala Lumpur, Malezya 🇲🇾", "Tüm Dünya / Diğer 🌍"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCreateScreen(
    selectedImageUri: String? = null,
    onPickImage: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSuccess: (SharedMarketItemModel) -> Unit = {}
) {
    var showLocationSheet by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Elektronik") }
    var condition by remember { mutableStateOf("İkinci El") }
    var city by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("Elektronik", "Giyim & Moda", "Ev & Yaşam", "Araç & Vasıta", "Spor & Hobi", "Kitap & Oyun", "Diğer")
    }

    val isFormValid = title.trim().isNotBlank() && 
                      priceText.trim().isNotBlank() && 
                      (priceText.toDoubleOrNull() ?: 0.0) > 0.0

    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            containerColor = Color(0xFF1E293B)
        ) {
            var locationQuery by remember { mutableStateOf("") }
            val filteredProvinces = remember(locationQuery) {
                if (locationQuery.isBlank()) {
                    TURKEY_PROVINCES + GLOBAL_CITIES
                } else {
                    val q = locationQuery.trim().lowercase()
                    (TURKEY_PROVINCES + GLOBAL_CITIES).filter { it.lowercase().contains(q) }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Konum Seçin (Şehir / Ülke) 📍",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = { locationQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Şehir veya Ülke Ara...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD700)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProvinces) { loc ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    city = loc
                                    showLocationSheet = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (city == loc) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF0F172A)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = loc,
                                    color = if (city == loc) Color(0xFFFFD700) else Color.White,
                                    fontWeight = if (city == loc) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Yeni İlan Oluştur 🛍️",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Görsel Katmanı
            Text("1. Ürün Görseli 🖼️", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                if (!selectedImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Seçilen Ürün",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                        Text(
                            text = "Fotoğraf Yüklemek İçin Dokunun",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Form Alanları
            Text("2. İlan Detayları 📝", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("İlan Başlığı (Örn: iPhone 15 Pro Max)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                    singleLine = true,
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

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { showLocationSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = city.ifBlank { "Şehir / Konum" },
                            color = if (city.isNotBlank()) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Kategori Çipleri
            Text("Kategori", color = Color(0xFF94A3B8), fontSize = 14.sp)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
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
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = Color(0xFFFFD700),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Durum Çipleri
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
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF334155),
                        selectedBorderColor = Color(0xFFFFD700),
                        enabled = true,
                        selected = condition == "İkinci El"
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
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF334155),
                        selectedBorderColor = Color(0xFFFFD700),
                        enabled = true,
                        selected = condition == "Sıfır"
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
                    if (!isFormValid) return@Button
                    isSubmitting = true
                    val newItem = SharedMarketItemModel(
                        id = "item_${kotlin.random.Random.nextLong(100000, 999999)}",
                        title = title.trim(),
                        description = description.trim(),
                        price = priceText.toDoubleOrNull() ?: 0.0,
                        currency = "₺",
                        category = category,
                        condition = condition,
                        city = city.ifBlank { "Türkiye 🇹🇷" },
                        photoUrl = selectedImageUri ?: "",
                        ownerId = "me",
                        username = "Siz",
                        isSold = false
                    )
                    onSuccess(newItem)
                },
                enabled = !isSubmitting && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    disabledContainerColor = Color(0xFF334155)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İlanı Yayınla 🚀",
                        color = if (isFormValid) Color.Black else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
