package com.neval.anoba.drawer

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.follow.FollowViewModel
import com.neval.anoba.follow.UserListSheet
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val followViewModel: FollowViewModel = koinViewModel()

    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val sonNumara by authViewModel.sonNumara.collectAsState()
    val context = LocalContext.current

    var showUserListSheet by remember { mutableStateOf(false) }
    var userListTitle by remember { mutableStateOf("") }

    if (showUserListSheet) {
        UserListSheet(
            title = userListTitle,
            onDismiss = {
                showUserListSheet = false
                followViewModel.clearUserList()
            },
            followViewModel = followViewModel
        )
    }

    // Sayfa açıldığında bir kez tazelemeyi dene
    LaunchedEffect(Unit) {
        authViewModel.refreshProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.drawer_my_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Constants.HOME_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = stringResource(id = R.string.profilescreen_home))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    ProfileContent(
                        navController = navController,
                        userEmail = state.email ?: "",
                        userName = if (state.isAnonymous) stringResource(id = R.string.guest) else (state.displayName ?: stringResource(id = R.string.state_loading)),
                        profileImageUrl = state.photoUrl ?: "",
                        sonNumara = sonNumara,
                        isAnonymous = state.isAnonymous,
                        isOfficial = state.isOfficial,
                        userRole = userRole,
                        badgeType = state.badgeType,
                        followerCount = state.followerCount,
                        followingCount = state.followingCount,
                        onFollowersClick = {
                            userListTitle = context.getString(R.string.followers)
                            followViewModel.fetchFollowers(state.uid)
                            showUserListSheet = true
                        },
                        onFollowingClick = {
                            userListTitle = context.getString(R.string.following_label)
                            followViewModel.fetchFollowing()
                            showUserListSheet = true
                        }
                    )
                }
                is AuthState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AnobaNavy)
                    }
                }
                else -> {
                    // Hata veya Unauthenticated durumu
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(id = R.string.profilescreen_info_not_available))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { authViewModel.refreshProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
                        ) {
                            Text(stringResource(id = R.string.profilescreen_try_again))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    navController: NavController,
    userEmail: String,
    userName: String,
    profileImageUrl: String,
    sonNumara: Int?,
    isAnonymous: Boolean,
    isOfficial: Boolean,
    userRole: String?,
    badgeType: String?,
    followerCount: Int = 0,
    followingCount: Int = 0,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageModel: Any = remember(profileImageUrl) {
            profileImageUrl.ifBlank { R.drawable.ic_default_profile }
        }

        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) },
            contentScale = ContentScale.Crop,
            onError = { error ->
                Log.e("ProfileScreen", "Resim yükleme hatası: ${error.result.throwable.message}")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isAnonymous) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) }
            ) {
                Text(
                    text = userName, 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.Bold,
                    color = AnobaNavy
                )
                com.neval.anoba.common.ui.AnobaBadge(
                    badgeType = badgeType,
                    isOfficial = isOfficial
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🚀 TAKİPÇİ VE TAKİP EDİLEN SAYILARI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFollowersClick() }
                ) {
                    Text(text = followerCount.toString(), fontWeight = FontWeight.Bold, color = AnobaNavy, fontSize = 18.sp)
                    Text(text = stringResource(id = R.string.followers), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFollowingClick() }
                ) {
                    Text(text = followingCount.toString(), fontWeight = FontWeight.Bold, color = AnobaNavy, fontSize = 18.sp)
                    Text(text = stringResource(id = R.string.following_label), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            UserInfoRow(
                label = stringResource(id = R.string.profilescreen_email), 
                value = userEmail.ifBlank { stringResource(id = R.string.profilescreen_email_not_specified) }
            )
            
            sonNumara?.let { 
                UserInfoRow(label = stringResource(id = R.string.profilescreen_user_number), value = it.toString()) 
            }

            // 🚀 YAYINLANAN İÇERİKLERİM BUTONU
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
            ) {
                Icon(Icons.Default.DynamicFeed, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.home_my_contents), color = Color.White, fontWeight = FontWeight.Bold)
            }

            // 🚀 ANOBA ADMİN PANEL GİRİŞİ (Master Admin Koruması)
            val isMasterAdmin = isOfficial || 
                               userName.equals("anoba", ignoreCase = true) || 
                               userEmail == "morplato@gmail.com" ||
                               userRole?.equals("ADMIN", ignoreCase = true) == true

            if (isMasterAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate(Constants.LETTER_ADMIN_SCREEN) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnobaGold)
                ) {
                    Text(stringResource(id = R.string.admin_anoba_panel_button), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(Constants.PROFILE_EDIT_SCREEN) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AnobaNavy)
            ) {
                Text(stringResource(id = R.string.profilescreen_edit_profile))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate(Constants.BLOCKED_USERS_SCREEN) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AnobaNavy),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AnobaNavy)
            ) {
                Text(stringResource(id = R.string.profilescreen_blocked_users))
            }
        } else {
            Text(stringResource(id = R.string.guest), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { 
                (context as? Activity)?.finishAffinity()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(id = R.string.profilescreen_close_app))
        }
    }
}

@Composable
fun UserInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}
