@file:kotlin.jvm.JvmName("SharedMarketHomeScreenKt")

package com.neval.anoba.market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarketContainerView(
    onNavigateHome: () -> Unit = {}
) {
    var activeView by remember { mutableStateOf("LIST") }
    var selectedItem by remember { mutableStateOf<SharedMarketItemModel?>(null) }

    val sampleItems = remember {
        mutableStateListOf(
            SharedMarketItemModel(
                id = "1",
                title = "iPhone 15 Pro Max 256GB Gold",
                description = "Kutusunda sıfır gibi, garantisi devam ediyor.",
                price = 62500.0,
                currency = "₺",
                category = "Elektronik",
                condition = "İkinci El",
                city = "İstanbul",
                photoUrl = "",
                ownerId = "user1",
                username = "Ahmet Y.",
                isSold = false
            ),
            SharedMarketItemModel(
                id = "2",
                title = "Sony PlayStation 5 Slim 1TB",
                description = "Çift kol + 3 orijinal oyun dahil.",
                price = 19500.0,
                currency = "₺",
                category = "Elektronik",
                condition = "İkinci El",
                city = "Ankara",
                photoUrl = "",
                ownerId = "user2",
                username = "Zeynep K.",
                isSold = false
            ),
            SharedMarketItemModel(
                id = "3",
                title = "Ahşap Çalışma Masası ve Sandalye Seti",
                description = "Masif çam ağacından el yapımı özel tasarım.",
                price = 4200.0,
                currency = "₺",
                category = "Ev & Yaşam",
                condition = "Sıfır",
                city = "İzmir",
                photoUrl = "",
                ownerId = "user3",
                username = "Anoba İmalat",
                isSold = false,
                isOfficial = true
            ),
            SharedMarketItemModel(
                id = "4",
                title = "Deri Ceket (L Beden Black)",
                description = "Orijinal hakiki deri mont, hiç giyilmedi.",
                price = 2800.0,
                currency = "₺",
                category = "Giyim & Moda",
                condition = "Sıfır",
                city = "Bursa",
                photoUrl = "",
                ownerId = "user4",
                username = "Mehmet T.",
                isSold = true
            )
        )
    }

    when (activeView) {
        "CREATE" -> {
            MarketCreateScreen(
                onNavigateBack = { activeView = "LIST" },
                onSuccess = { newItem ->
                    sampleItems.add(0, newItem)
                    activeView = "LIST"
                }
            )
        }
        "DETAIL" -> {
            MarketDetailScreen(
                item = selectedItem,
                onNavigateBack = { activeView = "LIST" }
            )
        }
        else -> {
            MarketHomeScreen(
                itemsList = sampleItems,
                onNavigateBack = onNavigateHome,
                onHomeClick = onNavigateHome,
                onCreateItemClick = { activeView = "CREATE" },
                onItemClick = { item ->
                    selectedItem = item
                    activeView = "DETAIL"
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHomeScreen(
    itemsList: List<SharedMarketItemModel> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onCreateItemClick: () -> Unit = {},
    onItemClick: (SharedMarketItemModel) -> Unit = {}
) {
    val categories = remember {
        listOf("Tümü", "Elektronik", "Giyim & Moda", "Ev & Yaşam", "Araç & Vasıta", "Spor & Hobi", "Kitap & Oyun", "Diğer")
    }

    var selectedCategory by remember { mutableStateOf("Tümü") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(selectedCategory, searchQuery, itemsList) {
        itemsList.filter { item ->
            val matchesCategory = (selectedCategory == "Tümü" || item.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.city.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Pazar Yeri",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onCreateItemClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "İlan Oluştur",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(onClick = onHomeClick) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Ana Sayfa",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Arama Kutusu
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = {
                            Text("Ürün, şehir veya açıklama ara...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8))
                        },
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

                    // Kategori Çipleri
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFFCBD5E1)
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
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFFFD700).copy(alpha = 0.85f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz İlan Bulunmuyor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedCategory != "Tümü" || searchQuery.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                selectedCategory = "Tümü"
                                searchQuery = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(Color(0xFFFFD700))
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Filtreleri Temizle", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onCreateItemClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("➕ İlan Oluştur", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MarketCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}
