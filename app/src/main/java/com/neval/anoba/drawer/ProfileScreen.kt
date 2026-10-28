package com.neval.anoba.drawer

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.follow.FollowViewModel
import com.neval.anoba.follow.UserListSheet
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.profile.SharedProfileScreen
import org.koin.androidx.compose.koinViewModel

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

    LaunchedEffect(Unit) {
        authViewModel.refreshProfile()
    }

    val state = authState
    val (email, userName, photoUrl, sonNum, isAnon, isOfficial, badgeType, followers, following, uid) = when (state) {
        is AuthState.Authenticated -> Tuple10(
            state.email ?: "",
            if (state.isAnonymous) stringResource(id = R.string.guest) else (state.displayName ?: stringResource(id = R.string.state_loading)),
            state.photoUrl ?: "",
            sonNumara,
            state.isAnonymous,
            state.isOfficial,
            state.badgeType,
            state.followerCount,
            state.followingCount,
            state.uid
        )
        else -> Tuple10("", stringResource(id = R.string.guest), "", null, true, false, "NONE", 0, 0, "")
    }

    val isMasterAdmin = isOfficial ||
            userName.equals("anoba", ignoreCase = true) ||
            email == "morplato@gmail.com" ||
            userRole?.equals("ADMIN", ignoreCase = true) == true

    SharedProfileScreen(
        userEmail = email,
        userName = userName,
        profileImageUrl = photoUrl,
        sonNumara = sonNum,
        isAnonymous = isAnon,
        isOfficial = isOfficial,
        badgeType = badgeType,
        isAdmin = isMasterAdmin,
        followerCount = followers,
        followingCount = following,
        onNavigateBack = { navController.popBackStack() },
        onNavigateHome = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        },
        onOpenMyContents = { navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) },
        onOpenAdminPanel = { navController.navigate(Constants.LETTER_ADMIN_SCREEN) },
        onOpenEditProfile = { navController.navigate(Constants.PROFILE_EDIT_SCREEN) },
        onOpenBlockedUsers = { navController.navigate(Constants.BLOCKED_USERS_SCREEN) },
        onFollowersClick = {
            userListTitle = context.getString(R.string.followers)
            if (uid.isNotBlank()) followViewModel.fetchFollowers(uid)
            showUserListSheet = true
        },
        onFollowingClick = {
            userListTitle = context.getString(R.string.following_label)
            followViewModel.fetchFollowing()
            showUserListSheet = true
        },
        onCloseApp = {
            (context as? Activity)?.finishAffinity()
        }
    )
}

private data class Tuple10<A, B, C, D, E, F, G, H, I, J>(
    val a: A, val b: B, val c: C, val d: D, val e: E,
    val f: F, val g: G, val h: H, val i: I, val j: J
)
