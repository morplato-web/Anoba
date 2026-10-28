package com.neval.anoba.letter

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LetterEditScreen(
    navController: NavController,
    letterId: String,
    viewModel: LetterEditViewModel = koinViewModel { parametersOf(letterId) }
) {
    val letterState by viewModel.letterState.collectAsState()

    val currentLetter = (letterState as? LetterEditState.Success)?.letter
    val sharedLetter = currentLetter?.toSharedModel()
    val isLoading = letterState is LetterEditState.Loading

    SharedLetterEditScreen(
        letter = sharedLetter,
        isLoading = isLoading,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onSaveEdit = { newTitle, newContent ->
            viewModel.updateLetterDetails(
                title = newTitle,
                content = newContent,
                personalNote = currentLetter?.personalNote ?: "",
                pseudonym = currentLetter?.pseudonym ?: "",
                isAskAnoba = currentLetter?.isAskAnoba ?: false
            )
            navController.popBackStack()
        }
    )
}

sealed interface LetterEditState {
    data object Loading : LetterEditState
    data class Success(val letter: LetterModel) : LetterEditState
    data class Error(val message: String) : LetterEditState
}