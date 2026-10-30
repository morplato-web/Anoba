@file:kotlin.jvm.JvmName("SharedMosaicSheetKt")

package com.neval.anoba.mosaic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("Unused", "unused")
data class SharedMosaicItem(
    val id: String = "",
    val title: String = "",
    val isAd: Boolean = false,
    val color: Color = Color.Gray,
)

@Suppress("Unused", "unused")
@Composable
fun SharedMosaicSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    items: List<SharedMosaicItem> = emptyList(),
    onItemClick: (SharedMosaicItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val mosaicColors = remember {
        listOf(
            Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
            Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
            Color(0xFF29B6F6), Color(0xFF26C6DA), Color(0xFF26A69A),
            Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
            Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726),
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.92f)
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = false) {}, // İçeriğe tıklanınca arka planın kapanmasını engeller
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = Color(0xFFECEFF1),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "MOZAİK 🧩",
                                color = Color(0xFF232066),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = "Reklam & İçerik Havuzu",
                                color = Color.Gray,
                                fontSize = 12.sp,
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 6 Sütun Izgara
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (items.isNotEmpty()) {
                            items(items, key = { it.id }) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable { onItemClick(item) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = item.color.copy(alpha = 0.85f),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        if (item.isAd) {
                                            Text("Reklam", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(120) { index ->
                                val color = mosaicColors[index % mosaicColors.size]
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable { },
                                    shape = RoundedCornerShape(6.dp),
                                    color = color.copy(alpha = 0.85f),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        if (index % 17 == 0) {
                                            Text("Reklam", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
