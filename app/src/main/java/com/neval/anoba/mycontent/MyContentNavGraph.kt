package com.neval.anoba.mycontent

import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.letter.LetterEditScreen
import com.neval.anoba.letter.LetterEditViewModel
import com.neval.anoba.photo.PhotoEditScreen
import com.neval.anoba.photo.PhotoEditViewModel
import com.neval.anoba.ses.SesEditScreen
import com.neval.anoba.ses.SesEditViewModel
import com.neval.anoba.video.VideoEditScreen
import com.neval.anoba.video.VideoEditViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MY_CONTENT_SCREEN = "my_content_screen?userId={userId}"

@UnstableApi
fun NavGraphBuilder.myContentNavGraph(navController: NavController) {
    navigation(
        startDestination = MY_CONTENT_SCREEN,
        route = Constants.MY_CONTENT_NAV_GRAPH,
    ) {
        composable(
            route = MY_CONTENT_SCREEN,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            MyContentScreen(navController = navController)
        }

        composable(
            route = Constants.SES_EDIT_SCREEN,
            arguments = listOf(navArgument("sesId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val sesId = backStackEntry.arguments?.getString("sesId")
            if (sesId != null) {
                val viewModel: SesEditViewModel = koinViewModel { parametersOf(sesId) }
                SesEditScreen(navController = navController, sesId = sesId, viewModel = viewModel)
            }
        }

        composable(
            route = Constants.LETTER_EDIT_SCREEN,
            arguments = listOf(navArgument("letterId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val letterId = backStackEntry.arguments?.getString("letterId")
            if (letterId != null) {
                val viewModel: LetterEditViewModel = koinViewModel { parametersOf(letterId) }
                LetterEditScreen(navController = navController, letterId = letterId, viewModel = viewModel)
            }
        }

        composable(
            route = Constants.PHOTO_EDIT_SCREEN,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId")
            if (photoId != null) {
                val viewModel: PhotoEditViewModel = koinViewModel { parametersOf(photoId) }
                PhotoEditScreen(navController = navController, photoId = photoId, viewModel = viewModel)
            }
        }

        composable(
            route = Constants.VIDEO_EDIT_SCREEN,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("videoUri") { type = NavType.StringType; nullable = false },
            ),
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")
            val videoUriString = backStackEntry.arguments?.getString("videoUri")
            if ((videoId != null) && (videoUriString != null)) {
                val viewModel: VideoEditViewModel = koinViewModel()
                VideoEditScreen(navController = navController, videoId = videoId, videoUri = videoUriString.toUri(), viewModel = viewModel)
            }
        }
    }
}
