package com.neval.anoba

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neval.anoba.chat.ui.chatNavGraph
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.drawer.drawerNavGraph
import com.neval.anoba.letter.letterNavGraph
import com.neval.anoba.login.ui.loginNavGraph
import com.neval.anoba.market.marketNavGraph
import com.neval.anoba.mycontent.myContentNavGraph
import com.neval.anoba.photo.photoNavGraph
import com.neval.anoba.ses.sesNavGraph
import com.neval.anoba.video.videoNavGraph
import com.neval.anoba.guide.GuideScreen
import com.neval.anoba.map.GlobalMapScreen

@UnstableApi
@OptIn(UnstableApi::class)
@Composable
fun MainAppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    // Scaffold kaldırıldı, padding çakışması önlendi.
    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = Constants.ROOT_GRAPH,
        modifier = Modifier.fillMaxSize()
    ) {
        loginNavGraph(navController)
        drawerNavGraph(navController)
        chatNavGraph(navController)
        letterNavGraph(navController)
        sesNavGraph(navController)
        photoNavGraph(navController)
        videoNavGraph(navController)
        myContentNavGraph(navController)
        marketNavGraph(navController)

        composable(Constants.GUIDE_NAV_GRAPH) { GuideScreen(navController) }
        composable(Constants.GLOBAL_MAP_SCREEN) { GlobalMapScreen(navController) }
        composable(Constants.METE_SCREEN) { com.neval.anoba.mete.MeteScreen(navController = navController) }
    }
}
