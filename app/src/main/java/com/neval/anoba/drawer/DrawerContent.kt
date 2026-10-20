package com.neval.anoba.drawer

import android.widget.Toast
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.home.HomeViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    onInfoClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    val isPoliceAnimationActive by homeViewModel.isPoliceAnimationActive.collectAsState()
    val isCommandoAnimationActive by homeViewModel.isCommandoAnimationActive.collectAsState()
    val isBekciAnimationActive by homeViewModel.isBekciAnimationActive.collectAsState()
    val isAnySpecialAnimActive = isPoliceAnimationActive || isCommandoAnimationActive || isBekciAnimationActive

    val (userName, profileImageUrl, isAdmin) = when (val state = authState) {
        is AuthState.Authenticated -> {
            val name = if (state.isAnonymous) {
                stringResource(id = R.string.guest)
            } else {
                state.displayName?.takeIf { it.isNotBlank() } ?: stringResource(id = R.string.state_loading)
            }
            val masterAdmin = state.isOfficial || 
                             state.displayName?.equals("anoba", ignoreCase = true) == true || 
                             state.email == "morplato@gmail.com" ||
                             userRole?.equals("ADMIN", ignoreCase = true) == true
            Triple(name, state.photoUrl ?: "", masterAdmin)
        }
        else -> Triple(stringResource(id = R.string.guest), "", false)
    }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showAdminPanelDialog by remember { mutableStateOf(false) }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text(stringResource(id = R.string.drawer_logout_dialog_title)) },
            text = { Text(stringResource(id = R.string.drawer_logout_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmDialog = false
                        authViewModel.logout()
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Constants.LOGIN_NAV_GRAPH) {
                                popToRoot(navController)
                            }
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.drawer_logout_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(stringResource(id = R.string.drawer_logout_dialog_dismiss))
                }
            }
        )
    }

    if (showAdminPanelDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPanelDialog = false },
            title = { Text(stringResource(id = R.string.admin_panel_title), fontWeight = FontWeight.Bold, color = AnobaNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminAnimButton(stringResource(id = R.string.admin_panel_police), Color(0xFF2196F3)) {
                        homeViewModel.togglePoliceAnimation(true)
                        showAdminPanelDialog = false
                    }
                    AdminAnimButton(stringResource(id = R.string.admin_panel_commando), Color(0xFF880E4F)) {
                        homeViewModel.toggleCommandoAnimation(true)
                        showAdminPanelDialog = false
                    }
                    AdminAnimButton(stringResource(id = R.string.admin_panel_bekci), Color(0xFF4E342E)) {
                        homeViewModel.toggleBekciAnimation(true)
                        showAdminPanelDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdminPanelDialog = false }) {
                    Text(stringResource(id = R.string.admin_panel_close), color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = profileImageUrl.ifBlank { R.drawable.ic_default_profile },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(50.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AnobaNavy)
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = AnobaNavy) },
                            label = { Text(stringResource(id = R.string.drawer_my_profile), color = AnobaNavy, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Constants.PROFILE_SCREEN) {
                                    launchSingleTop = true
                                }
                            }
                        )

                        // 🚀 YETKİLİLER (Admin Yıldızı)
                        if (isAdmin) {
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = if (isAnySpecialAnimActive) Color.Red else AnobaGold
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.admin_panel_title),
                                        color = if (isAnySpecialAnimActive) Color.Red else AnobaNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selected = false,
                                onClick = {
                                    showAdminPanelDialog = true
                                }
                            )
                        }

                        // 🚀 REHBER / INFO SATIRI
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = AnobaNavy) },
                            label = { Text(stringResource(id = R.string.guide_title), color = AnobaNavy, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onInfoClick()
                            }
                        )

                        // 🛡️ GİZLİLİK POLİTİKASI
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AnobaNavy) },
                            label = { Text(stringResource(id = R.string.drawer_privacy_policy), color = AnobaNavy, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, "https://anoba-cd9de.web.app/privacy.html".toUri())
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: Exception) {
                                    Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        label = { Text(stringResource(id = R.string.drawer_logout), color = MaterialTheme.colorScheme.error) },
                        selected = false,
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        content = content
    )
}

private fun NavOptionsBuilder.popToRoot(navController: NavHostController) {
    popUpTo(navController.graph.id) { inclusive = true }
}

@Composable
fun AdminAnimButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
