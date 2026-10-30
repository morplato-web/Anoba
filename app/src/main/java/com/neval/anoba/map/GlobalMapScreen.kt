package com.neval.anoba.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.home.ContentItem
import com.neval.anoba.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GlobalMapScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    val items by homeViewModel.globalMapItems.collectAsState()
    val context = LocalContext.current
    val knownCodes = remember { DEFAULT_SHARED_COUNTRIES.asSequence().map { it.code }.toSet() - "OTHER" }

    val countryStats = remember(items) {
        if (items.isEmpty()) {
            DEFAULT_SHARED_COUNTRIES
        } else {
            val itemsByCountry = items.groupBy { item ->
                val rawCode = when (item) {
                    is ContentItem.PhotoContent -> item.photo.countryCode
                    is ContentItem.LetterContent -> item.letter.countryCode
                    is ContentItem.VideoContent -> item.video.countryCode
                    is ContentItem.AudioContent -> item.audio.countryCode
                    is ContentItem.MarketContent -> item.marketItem.countryCode
                }?.trim()?.uppercase()

                when {
                    rawCode.isNullOrBlank() -> "TR"
                    rawCode in knownCodes -> rawCode
                    else -> "OTHER"
                }
            }

            DEFAULT_SHARED_COUNTRIES.map { country ->
                val localizedName = when (country.code) {
                    "TR" -> context.getString(R.string.country_tr)
                    "US" -> context.getString(R.string.country_us)
                    "RU" -> context.getString(R.string.country_ru)
                    "IT" -> context.getString(R.string.country_it)
                    "DE" -> context.getString(R.string.country_de)
                    "AZ" -> context.getString(R.string.country_az)
                    "SA" -> context.getString(R.string.country_sa)
                    "MX" -> context.getString(R.string.country_mx)
                    "KZ" -> context.getString(R.string.country_kz)
                    "AE" -> context.getString(R.string.country_ae)
                    "HU" -> context.getString(R.string.country_hu)
                    "UZ" -> context.getString(R.string.country_uz)
                    "EG" -> context.getString(R.string.country_eg)
                    "ZA" -> context.getString(R.string.country_za)
                    "QA" -> context.getString(R.string.country_qa)
                    "DZ" -> context.getString(R.string.country_dz)
                    "KG" -> context.getString(R.string.country_kg)
                    "MA" -> context.getString(R.string.country_ma)
                    "PK" -> context.getString(R.string.country_pk)
                    "NG" -> context.getString(R.string.country_ng)
                    "KE" -> context.getString(R.string.country_ke)
                    "TN" -> context.getString(R.string.country_tn)
                    "OM" -> context.getString(R.string.country_om)
                    "JO" -> context.getString(R.string.country_jo)
                    "CA" -> context.getString(R.string.country_ca)
                    "AU" -> context.getString(R.string.country_au)
                    "BR" -> context.getString(R.string.country_br)
                    "AR" -> context.getString(R.string.country_ar)
                    "IN" -> context.getString(R.string.country_in)
                    "JP" -> context.getString(R.string.country_jp)
                    "KR" -> context.getString(R.string.country_kr)
                    "ID" -> context.getString(R.string.country_id)
                    "GB" -> context.getString(R.string.country_gb)
                    "FR" -> context.getString(R.string.country_fr)
                    "ES" -> context.getString(R.string.country_es)
                    "NL" -> context.getString(R.string.country_nl)
                    "CO" -> context.getString(R.string.country_co)
                    "MY" -> context.getString(R.string.country_my)
                    "TM" -> context.getString(R.string.country_tm)
                    else -> country.name
                }

                country.copy(
                    name = localizedName,
                    contentCount = itemsByCountry[country.code]?.size ?: 0,
                )
            }
        }
    }

    SharedGlobalMapScreen(
        countryStats = countryStats,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        },
        onCountryClick = { country ->
            navController.navigate("${Constants.HOME_SCREEN}?countryCode=${country.code}") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        },
    )
}
