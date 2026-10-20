package com.neval.anoba.photo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditScreen(
    navController: NavController,
    photoId: String,
    viewModel: PhotoEditViewModel = koinViewModel { parametersOf(photoId) }
) {
    val photoState by viewModel.photoState
    var title by remember { mutableStateOf("") }
    var personalNote by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(photoState) {
        if (photoState is PhotoEditState.Success) {
            val photo = (photoState as PhotoEditState.Success).photo
            title = photo.title ?: ""
            personalNote = photo.personalNote
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(), // Klavyeyi algılar
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.photoedit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updatePhotoDetails(title, personalNote)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState) // Sayfayı kaydırılabilir yapar
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = photoState) {
                is PhotoEditState.Success -> {
                    AsyncImage(
                        model = state.photo.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(id = R.string.photoedit_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = personalNote,
                        onValueChange = { personalNote = it },
                        label = { Text(stringResource(id = R.string.photoedit_note_label)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                is PhotoEditState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PhotoEditState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
