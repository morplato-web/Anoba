package com.neval.anoba.letter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterEditScreen(
    navController: NavController,
    letterId: String,
    viewModel: LetterEditViewModel = koinViewModel { parametersOf(letterId) }
) {
    val letterState by viewModel.letterState.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var personalNote by remember { mutableStateOf("") }

    var pseudonym by remember { mutableStateOf("") }
    var isAskAnoba by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(letterState) {
        if (letterState is LetterEditState.Success) {
            val letter = (letterState as LetterEditState.Success).letter
            title = letter.title ?: ""
            content = letter.content
            personalNote = letter.personalNote
            pseudonym = letter.pseudonym ?: ""
            isAskAnoba = letter.isAskAnoba
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.letteredit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateLetterDetails(title, content, personalNote, pseudonym, isAskAnoba)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        when (val state = letterState) {
            is LetterEditState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LetterEditState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is LetterEditState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Yalnızca baştan Anoba Ablası mektubu olarak oluşturulmuşsa mahlas düzenlenebilir
                    AnimatedVisibility(visible = isAskAnoba) {
                        OutlinedTextField(
                            value = pseudonym,
                            onValueChange = { pseudonym = it },
                            label = { Text("Mahlas / Rumuz") },
                            placeholder = { Text("Kimliğini gizlemek için bir rumuz yaz...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(stringResource(id = R.string.letteredit_content_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(id = R.string.letteredit_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = personalNote,
                        onValueChange = { personalNote = it },
                        label = { Text(stringResource(id = R.string.letteredit_personal_note_label)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

sealed interface LetterEditState {
    data object Loading : LetterEditState
    data class Success(val letter: LetterModel) : LetterEditState
    data class Error(val message: String) : LetterEditState
}