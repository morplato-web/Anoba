@file:kotlin.jvm.JvmName("SharedMarketEditScreenKt")

package com.neval.anoba.market

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun MarketEditScreen(
    item: SharedMarketItemModel?,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: (SharedMarketItemModel) -> Unit = {}
) {
    var title by remember(item) { mutableStateOf(item?.title ?: "") }
    var priceText by remember(item) {
        val price = item?.price
        mutableStateOf(
            if (price != null) {
                if (price % 1.0 == 0.0) price.toLong().toString() else price.toString()
            } else ""
        )
    }
    var category by remember(item) { mutableStateOf(item?.category ?: "Elektronik") }
    var condition by remember(item) { mutableStateOf(item?.condition ?: "İkinci El") }
    var city by remember(item) { mutableStateOf(item?.city ?: "") }
    var description by remember(item) { mutableStateOf(item?.description ?: "") }

    var showLocationSheet by remember { mutableStateOf(false) }

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
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                    Text("İlanı Düzenle 📝", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("İlan bulunamadı", color = Color.White)
            }
        } else {
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
                // Fotoğraf Önizleme (Photo Preview)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.photoUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color(0xFFFFD700).copy(alpha = 0.7f),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("İlan Başlığı") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
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
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
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
                    label = { Text("Açıklama") },
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
                        val updated = item.copy(
                            title = title.trim(),
                            description = description.trim(),
                            price = priceText.toDoubleOrNull() ?: item.price,
                            category = category,
                            city = city.ifBlank { item.city },
                            condition = condition
                        )
                        onSaveSuccess(updated)
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color(0xFF334155)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Değişiklikleri Kaydet 🚀",
                        color = if (isFormValid) Color.Black else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
