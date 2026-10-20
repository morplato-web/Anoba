package com.neval.anoba.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.home.ContentItem
import com.neval.anoba.home.HomeViewModel
import com.neval.anoba.ui.theme.AnobaNavy
import org.koin.androidx.compose.koinViewModel

// 🌍 Statik ülke listesi Composable dışına taşındı (Performans için)
private val DEFAULT_COUNTRIES = listOf(
    CountryData("TR", R.string.country_tr, "🇹🇷"),
    CountryData("US", R.string.country_us, "🇺🇸"),
    CountryData("RU", R.string.country_ru, "🇷🇺"),
    CountryData("IT", R.string.country_it, "🇮🇹"),
    CountryData("DE", R.string.country_de, "🇩🇪"),
    CountryData("AZ", R.string.country_az, "🇦🇿"),
    CountryData("SA", R.string.country_sa, "🇸🇦"),
    CountryData("MX", R.string.country_mx, "🇲🇽"),
    CountryData("KZ", R.string.country_kz, "🇰🇿"),
    CountryData("AE", R.string.country_ae, "🇦🇪"),
    CountryData("HU", R.string.country_hu, "🇭🇺"),
    CountryData("UZ", R.string.country_uz, "🇺🇿"),
    CountryData("EG", R.string.country_eg, "🇪🇬"),
    CountryData("ZA", R.string.country_za, "🇿🇦"),
    CountryData("QA", R.string.country_qa, "🇶🇦"),
    CountryData("DZ", R.string.country_dz, "🇩🇿"),
    CountryData("KG", R.string.country_kg, "🇰🇬"),
    CountryData("MA", R.string.country_ma, "🇲🇦"),
    CountryData("PK", R.string.country_pk, "🇵🇰"),
    CountryData("NG", R.string.country_ng, "🇳🇬"),
    CountryData("KE", R.string.country_ke, "🇰🇪"),
    CountryData("TN", R.string.country_tn, "🇹🇳"),
    CountryData("OM", R.string.country_om, "🇴🇲"),
    CountryData("JO", R.string.country_jo, "🇯🇴"),
    CountryData("CA", R.string.country_ca, "🇨🇦"),
    CountryData("AU", R.string.country_au, "🇦🇺"),
    CountryData("BR", R.string.country_br, "🇧🇷"),
    CountryData("AR", R.string.country_ar, "🇦🇷"),
    CountryData("IN", R.string.country_in, "🇮🇳"),
    CountryData("JP", R.string.country_jp, "🇯🇵"),
    CountryData("KR", R.string.country_kr, "🇰🇷"),
    CountryData("ID", R.string.country_id, "🇮🇩"),
    CountryData("GB", R.string.country_gb, "🇬🇧"),
    CountryData("FR", R.string.country_fr, "🇫🇷"),
    CountryData("ES", R.string.country_es, "🇪🇸"),
    CountryData("NL", R.string.country_nl, "🇳🇱"),
    CountryData("CO", R.string.country_co, "🇨🇴"),
    CountryData("MY", R.string.country_my, "🇲🇾"),
    CountryData("TM", R.string.country_tm, "🇹🇲"),
    CountryData("OTHER", R.string.country_other, "🌍")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalMapScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val items by homeViewModel.globalMapItems.collectAsState()
    val knownCodes = remember { DEFAULT_COUNTRIES.map { it.code }.toSet() - "OTHER" }

    val countryStats = remember(items) {
        if (items.isEmpty()) {
            // 🚀 İçerik henüz gelmediyse veya boşsa listeyi 0 içerikle anında göster
            DEFAULT_COUNTRIES
        } else {
            val itemsByCountry = items.groupBy { item ->
                val code = when (item) {
                    is ContentItem.PhotoContent -> item.photo.countryCode
                    is ContentItem.LetterContent -> item.letter.countryCode
                    is ContentItem.VideoContent -> item.video.countryCode
                    is ContentItem.AudioContent -> item.audio.countryCode
                }?.trim()?.uppercase()

                if (code.isNullOrBlank()) "TR"
                else if (code in knownCodes) code
                else "OTHER"
            }

            DEFAULT_COUNTRIES.map { country ->
                country.copy(contentCount = itemsByCountry[country.code]?.size ?: 0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.map_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = stringResource(R.string.mycontent_home)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = AnobaNavy,
                    navigationIconContentColor = AnobaNavy,
                    actionIconContentColor = AnobaNavy
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF1976D2)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = countryStats,
                    key = { it.code } // 🚀 Performans için key eklendi
                ) { country ->
                    CountryCard(country) {
                        navController.navigate("${Constants.HOME_SCREEN}?countryCode=${country.code}") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryCard(country: CountryData, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(country.flag, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(country.nameRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AnobaNavy
            )
            Text(
                text = stringResource(R.string.map_content_count, country.contentCount),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

data class CountryData(
    val code: String,
    val nameRes: Int,
    val flag: String,
    val contentCount: Int = 0
)
