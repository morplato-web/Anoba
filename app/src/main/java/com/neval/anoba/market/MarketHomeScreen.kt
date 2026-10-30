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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHomeScreen(
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onCreateItemClick: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: MarketViewModel = koinViewModel(),
) {
    val items by viewModel.items.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = Color(0xFF131826),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White,
                                )
                            }
                            Text(
                                text = stringResource(R.string.market_screen_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onCreateItemClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.market_create_button),
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            IconButton(onClick = onHomeClick) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Ana Sayfa",
                                    tint = Color.White,
                                )
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text(stringResource(R.string.market_search_placeholder), color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF131826),
                            unfocusedContainerColor = Color(0xFF131826),
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF2E384D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                        singleLine = true,
                    )

                    // Categories LazyRow
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(viewModel.categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(category) },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF131826),
                                    labelColor = Color(0xFFCBD5E1),
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color(0xFF2E384D),
                                    selectedBorderColor = Color(0xFFFFD700),
                                    enabled = true,
                                    selected = isSelected,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFFFD700).copy(alpha = 0.85f),
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.market_no_items),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedCategory != "Tümü" || searchQuery.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.selectCategory("Tümü")
                                viewModel.updateSearchQuery("")
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(Color(0xFFFFD700)),
                            ),
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.market_clear_filters), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onCreateItemClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("➕ İlan Oluştur", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // 🚀 2'li Izgara Yapısı (2-Column Grid Layout)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        MarketCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                        )
                    }
                }
            }
        }
    }
}
