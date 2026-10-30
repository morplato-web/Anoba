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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.ui.theme.AnobaNavy

data class SharedCountryData(
    val code: String,
    val name: String,
    val flag: String,
    val contentCount: Int = 0,
)

val DEFAULT_SHARED_COUNTRIES = listOf(
    // Mevcut Üst Ülkeler (Sıra Korundu)
    SharedCountryData("TR", "Türkiye", "🇹🇷"),
    SharedCountryData("US", "Amerika Birleşik Devletleri", "🇺🇸"),
    SharedCountryData("RU", "Rusya", "🇷🇺"),
    SharedCountryData("IT", "İtalya", "🇮🇹"),
    SharedCountryData("DE", "Almanya", "🇩🇪"),
    SharedCountryData("AZ", "Azerbaycan", "🇦🇿"),
    SharedCountryData("SA", "Suudi Arabistan", "🇸🇦"),
    SharedCountryData("MX", "Meksika", "🇲🇽"),
    SharedCountryData("KZ", "Kazakistan", "🇰🇿"),
    SharedCountryData("AE", "Birleşik Arap Emirlikleri", "🇦🇪"),
    SharedCountryData("HU", "Macaristan", "🇭🇺"),
    SharedCountryData("UZ", "Özbekistan", "🇺🇿"),
    SharedCountryData("EG", "Mısır", "🇪🇬"),
    SharedCountryData("ZA", "Güney Afrika", "🇿🇦"),
    SharedCountryData("QA", "Katar", "🇶🇦"),
    SharedCountryData("DZ", "Cezayir", "🇩🇿"),
    SharedCountryData("KG", "Kırgızistan", "🇰🇬"),
    SharedCountryData("MA", "Fas", "🇲🇦"),
    SharedCountryData("PK", "Pakistan", "🇵🇰"),
    SharedCountryData("NG", "Nijerya", "🇳🇬"),
    SharedCountryData("KE", "Kenya", "🇰🇪"),
    SharedCountryData("TN", "Tunus", "🇹🇳"),
    SharedCountryData("OM", "Umman", "🇴🇲"),
    SharedCountryData("JO", "Ürdün", "🇯🇴"),
    SharedCountryData("CA", "Kanada", "🇨🇦"),
    SharedCountryData("AU", "Avustralya", "🇦🇺"),
    SharedCountryData("BR", "Brezilya", "🇧🇷"),
    SharedCountryData("AR", "Arjantin", "🇦🇷"),
    SharedCountryData("IN", "Hindistan", "🇮🇳"),
    SharedCountryData("JP", "Japonya", "🇯🇵"),
    SharedCountryData("KR", "Güney Kore", "🇰🇷"),
    SharedCountryData("ID", "Endonezya", "🇮🇩"),
    SharedCountryData("GB", "Birleşik Krallık", "🇬🇧"),
    SharedCountryData("FR", "Fransa", "🇫🇷"),
    SharedCountryData("ES", "İspanya", "🇪🇸"),
    SharedCountryData("NL", "Hollanda", "🇳🇱"),
    SharedCountryData("CO", "Kolombiya", "🇨🇴"),
    SharedCountryData("MY", "Malezya", "🇲🇾"),
    SharedCountryData("TM", "Türkmenistan", "🇹🇲"),

    // Eklenen Hedef Ülkeler
    SharedCountryData("AO", "Angola", "🇦🇴"),
    SharedCountryData("AG", "Antigua ve Barbuda", "🇦🇬"),
    SharedCountryData("AL", "Arnavutluk", "🇦🇱"),
    SharedCountryData("AW", "Aruba", "🇦🇼"),
    SharedCountryData("AT", "Avusturya", "🇦🇹"),
    SharedCountryData("BS", "Bahamalar", "🇧🇸"),
    SharedCountryData("BH", "Bahreyn", "🇧🇭"),
    SharedCountryData("BD", "Bangladeş", "🇧🇩"),
    SharedCountryData("BY", "Belarus", "🇧🇾"),
    SharedCountryData("BZ", "Belize", "🇧🇿"),
    SharedCountryData("BE", "Belçika", "🇧🇪"),
    SharedCountryData("BJ", "Benin", "🇧🇯"),
    SharedCountryData("BM", "Bermuda", "🇧🇲"),
    SharedCountryData("BO", "Bolivya", "🇧🇴"),
    SharedCountryData("BA", "Bosna-Hersek", "🇧🇦"),
    SharedCountryData("BW", "Botsvana", "🇧🇼"),
    SharedCountryData("VG", "Britanya Virjin Adaları", "🇻🇬"),
    SharedCountryData("BG", "Bulgaristan", "🇧🇬"),
    SharedCountryData("BF", "Burkina Faso", "🇧🇫"),
    SharedCountryData("CV", "Cabo Verde", "🇨🇻"),
    SharedCountryData("KY", "Cayman Adaları", "🇰🇾"),
    SharedCountryData("GI", "Cebelitarık", "🇬🇮"),
    SharedCountryData("DJ", "Cibuti", "🇩🇯"),
    SharedCountryData("CI", "Fildişi Sahili", "🇨🇮"),
    SharedCountryData("DK", "Danimarka", "🇩🇰"),
    SharedCountryData("DO", "Dominik Cumhuriyeti", "🇩🇴"),
    SharedCountryData("DM", "Dominika", "🇩🇲"),
    SharedCountryData("EC", "Ekvador", "🇪🇨"),
    SharedCountryData("SV", "El Salvador", "🇸🇻"),
    SharedCountryData("ER", "Eritre", "🇪🇷"),
    SharedCountryData("AM", "Ermenistan", "🇦🇲"),
    SharedCountryData("EE", "Estonya", "🇪🇪"),
    SharedCountryData("ET", "Etiyopya", "🇪🇹"),
    SharedCountryData("FJ", "Fiji", "🇫🇯"),
    SharedCountryData("PH", "Filipinler", "🇵🇭"),
    SharedCountryData("FI", "Finlandiya", "🇫🇮"),
    SharedCountryData("GA", "Gabon", "🇬🇦"),
    SharedCountryData("GM", "Gambiya", "🇬🇲"),
    SharedCountryData("GH", "Gana", "🇬🇭"),
    SharedCountryData("GN", "Gine", "🇬🇳"),
    SharedCountryData("GW", "Gine-Bissau", "🇬🇼"),
    SharedCountryData("GD", "Grenada", "🇬🇩"),
    SharedCountryData("GT", "Guatemala", "🇬🇹"),
    SharedCountryData("GE", "Gürcistan", "🇬🇪"),
    SharedCountryData("HT", "Haiti", "🇭🇹"),
    SharedCountryData("HN", "Honduras", "🇭🇳"),
    SharedCountryData("HK", "Hong Kong", "🇭🇰"),
    SharedCountryData("HR", "Hırvatistan", "🇭🇷"),
    SharedCountryData("IQ", "Irak", "🇮🇶"),
    SharedCountryData("JM", "Jamaika", "🇯🇲"),
    SharedCountryData("KH", "Kamboçya", "🇰🇭"),
    SharedCountryData("CM", "Kamerun", "🇨🇲"),
    SharedCountryData("KM", "Komorlar", "🇰🇲"),
    SharedCountryData("CG", "Kongo - Brazavil", "🇨🇬"),
    SharedCountryData("CD", "Kongo - Kinşasa", "🇨🇩"),
    SharedCountryData("CR", "Kosta Rika", "🇨🇷"),
    SharedCountryData("KW", "Kuveyt", "🇰🇼"),
    SharedCountryData("MK", "Kuzey Makedonya", "🇲🇰"),
    SharedCountryData("CU", "Küba", "🇨🇺"),
    SharedCountryData("CY", "Kıbrıs", "🇨🇾"),
    SharedCountryData("LA", "Laos", "🇱🇦"),
    SharedCountryData("LV", "Letonya", "🇱🇻"),
    SharedCountryData("LR", "Liberya", "🇱🇷"),
    SharedCountryData("LY", "Libya", "🇱🇾"),
    SharedCountryData("LI", "Liechtenstein", "🇱🇮"),
    SharedCountryData("LT", "Litvanya", "🇱🇹"),
    SharedCountryData("LB", "Lübnan", "🇱🇧"),
    SharedCountryData("LU", "Lüksemburg", "🇱🇺"),
    SharedCountryData("MO", "Makao", "🇲🇴"),
    SharedCountryData("MV", "Maldivler", "🇲🇻"),
    SharedCountryData("ML", "Mali", "🇲🇱"),
    SharedCountryData("MT", "Malta", "🇲🇹"),
    SharedCountryData("MU", "Mauritius", "🇲🇺"),
    SharedCountryData("FM", "Mikronezya", "🇫🇲"),
    SharedCountryData("MD", "Moldova", "🇲🇩"),
    SharedCountryData("MC", "Monako", "🇲🇨"),
    SharedCountryData("MZ", "Mozambik", "🇲🇿"),
    SharedCountryData("MN", "Moğolistan", "🇲🇳"),
    SharedCountryData("MM", "Myanmar", "🇲🇲"),
    SharedCountryData("NA", "Namibya", "🇳🇦"),
    SharedCountryData("NP", "Nepal", "🇳🇵"),
    SharedCountryData("NE", "Nijer", "🇳🇪"),
    SharedCountryData("NI", "Nikaragua", "🇳🇮"),
    SharedCountryData("NO", "Norveç", "🇳🇴"),
    SharedCountryData("PA", "Panama", "🇵🇦"),
    SharedCountryData("PG", "Papua Yeni Gine", "🇵🇬"),
    SharedCountryData("PY", "Paraguay", "🇵🇾"),
    SharedCountryData("PE", "Peru", "🇵🇪"),
    SharedCountryData("PL", "Polonya", "🇵🇱"),
    SharedCountryData("PT", "Portekiz", "🇵🇹"),
    SharedCountryData("RO", "Romanya", "🇷🇴"),
    SharedCountryData("RW", "Ruanda", "🇷🇼"),
    SharedCountryData("KN", "Saint Kitts ve Nevis", "🇰🇳"),
    SharedCountryData("LC", "Saint Lucia", "🇱🇨"),
    SharedCountryData("WS", "Samoa", "🇼🇸"),
    SharedCountryData("SM", "San Marino", "🇸🇲"),
    SharedCountryData("SN", "Senegal", "🇸🇳"),
    SharedCountryData("SC", "Seyşeller", "🇸🇨"),
    SharedCountryData("SL", "Sierra Leone", "🇸🇱"),
    SharedCountryData("SG", "Singapur", "🇸🇬"),
    SharedCountryData("SK", "Slovakya", "🇸🇰"),
    SharedCountryData("SI", "Slovenya", "🇸🇮"),
    SharedCountryData("SB", "Solomon Adaları", "🇸🇧"),
    SharedCountryData("SO", "Somali", "🇸🇴"),
    SharedCountryData("LK", "Sri Lanka", "🇱🇰"),
    SharedCountryData("SD", "Sudan", "🇸🇩"),
    SharedCountryData("SR", "Surinam", "🇸🇷"),
    SharedCountryData("RS", "Sırbistan", "🇷🇸"),
    SharedCountryData("TJ", "Tacikistan", "🇹🇯"),
    SharedCountryData("TZ", "Tanzanya", "🇹🇿"),
    SharedCountryData("TH", "Tayland", "🇹🇭"),
    SharedCountryData("TW", "Tayvan", "🇹🇼"),
    SharedCountryData("TG", "Togo", "🇹🇬"),
    SharedCountryData("TO", "Tonga", "🇹🇴"),
    SharedCountryData("TT", "Trinidad ve Tobago", "🇹🇹"),
    SharedCountryData("TC", "Turks ve Caicos Adaları", "🇹🇨"),
    SharedCountryData("UG", "Uganda", "🇺🇬"),
    SharedCountryData("UA", "Ukrayna", "🇺🇦"),
    SharedCountryData("UY", "Uruguay", "🇺🇾"),
    SharedCountryData("VU", "Vanuatu", "🇻🇺"),
    SharedCountryData("VA", "Vatikan", "🇻🇦"),
    SharedCountryData("VE", "Venezuela", "🇻🇪"),
    SharedCountryData("VN", "Vietnam", "🇻🇳"),
    SharedCountryData("YE", "Yemen", "🇾🇪"),
    SharedCountryData("NZ", "Yeni Zelanda", "🇳🇿"),
    SharedCountryData("GR", "Yunanistan", "🇬🇷"),
    SharedCountryData("ZM", "Zambiya", "🇿🇲"),
    SharedCountryData("ZW", "Zimbabve", "🇿🇼"),
    SharedCountryData("TD", "Çad", "🇹🇩"),
    SharedCountryData("CZ", "Çekya", "🇨🇿"),
    SharedCountryData("CN", "Çin", "🇨🇳"),
    SharedCountryData("IR", "İran", "🇮🇷"),
    SharedCountryData("IE", "İrlanda", "🇮🇪"),
    SharedCountryData("SE", "İsveç", "🇸🇪"),
    SharedCountryData("CH", "İsviçre", "🇨🇭"),
    SharedCountryData("IS", "İzlanda", "🇮🇸"),
    SharedCountryData("CL", "Şili", "🇨🇱"),

    // Diğer (Kapsama Dışı Tüm İçerikler)
    SharedCountryData("OTHER", "Diğer", "🌍"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedGlobalMapScreen(
    countryStats: List<SharedCountryData> = DEFAULT_SHARED_COUNTRIES,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onCountryClick: (SharedCountryData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌍 Küresel Harita",
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = AnobaNavy,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Ana Sayfa",
                            tint = AnobaNavy,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = AnobaNavy,
                    navigationIconContentColor = AnobaNavy,
                    actionIconContentColor = AnobaNavy,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF1976D2),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = countryStats,
                    key = { it.code },
                ) { country ->
                    SharedCountryCard(country = country) {
                        onCountryClick(country)
                    }
                }
            }
        }
    }
}

@Composable
fun SharedCountryCard(
    country: SharedCountryData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(country.flag, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AnobaNavy,
            )
            Text(
                text = "${country.contentCount} İçerik",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )
        }
    }
}
