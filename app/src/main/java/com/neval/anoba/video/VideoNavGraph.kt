package com.neval.anoba.video

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.video.videocomment.VideoCommentsScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
fun NavGraphBuilder.videoNavGraph(navController: NavController) {
    navigation(
        route = Constants.VIDEO_NAV_GRAPH,
        startDestination = Constants.VIDEO_HOME_SCREEN
    ) {
        composable(Constants.VIDEO_HOME_SCREEN) {
            VideoHomeScreen(navController = navController)
        }

        composable(Constants.VIDEO_CAMERA_SCREEN) {
            CameraScreen(navController = navController)
        }

        composable(
            route = Constants.VIDEO_DETAIL_SCREEN, 
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")
            if (videoId != null) {
                VideoDetailScreen(
                    navController = navController,
                    videoId = videoId
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = Constants.VIDEO_COMMENTS_ROUTE,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")
            if (videoId != null) {
                VideoCommentsScreen(
                    navController = navController,
                    videoId = videoId
                )
            } else {
                navController.popBackStack()
            }
        }
        
        composable(
            route = Constants.VIDEO_EDIT_SCREEN,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("videoUri") { 
                    type = NavType.StringType
                    nullable = false // videoUri is required
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")
            val videoUriString = backStackEntry.arguments?.getString("videoUri")

            if (videoId != null && videoUriString != null) {
                val viewModel: VideoEditViewModel = koinViewModel()
                VideoEditScreen(navController = navController, videoId = videoId, videoUri = videoUriString.toUri(), viewModel = viewModel)
            }
        }
    }
}
