package com.neval.anoba.ses

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SesEditScreen(
    navController: NavController,
    sesId: String,
    viewModel: SesEditViewModel = koinViewModel { parametersOf(sesId) }
) {
    val sesState by viewModel.sesState.collectAsState()
    val context = LocalContext.current
    val updateSuccessText = stringResource(id = R.string.sesedit_success)

    LaunchedEffect(sesState) {
        when (val state = sesState) {
            SesEditState.UpdateSuccess -> {
                Toast.makeText(context, updateSuccessText, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is SesEditState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val currentSes = (sesState as? SesEditState.Success)?.ses
    val sharedSes = currentSes?.let { ses ->
        SharedSesModel(
            id = ses.id,
            title = ses.title ?: "",
            personalNote = ses.personalNote,
            durationMillis = ses.duration
        )
    }

    SharedSesEditScreen(
        ses = sharedSes,
        isLoading = sesState is SesEditState.Loading,
        errorMessage = (sesState as? SesEditState.Error)?.message,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onSaveClick = { title, personalNote, startMs, endMs ->
            val totalDuration = currentSes?.duration ?: 0L
            val finalStart = if (startMs == 0L || startMs == null) null else startMs
            val finalEnd = if (endMs == totalDuration || endMs == null) null else endMs
            viewModel.updateSesDetails(title, personalNote, finalStart, finalEnd)
        }
    )
}
