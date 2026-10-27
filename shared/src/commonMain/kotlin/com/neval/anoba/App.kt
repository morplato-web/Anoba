package com.neval.anoba

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.neval.anoba.chat.group.SharedGroupChatListScreen
import com.neval.anoba.chat.ui.ChatHomeScreen
import com.neval.anoba.chat.ui.SharedPrivateChatScreen
import com.neval.anoba.drawer.DrawerSheetContent
import com.neval.anoba.guide.SharedGuideScreen
import com.neval.anoba.home.HomeScreen
import com.neval.anoba.login.AuthContainerView
import com.neval.anoba.market.MarketContainerView
import com.neval.anoba.mete.MeteHomeScreen
import com.neval.anoba.profile.ProfileScreen
import com.neval.anoba.ui.theme.AnobaTheme

@Composable
fun App() {
    AnobaTheme {
        var isAuthenticated by remember { mutableStateOf(false) }

        if (!isAuthenticated) {
            AuthContainerView(
                onLoginSuccess = { isAuthenticated = true }
            )
        } else {
            MainAppScreen(
                onLogout = { isAuthenticated = false }
            )
        }
    }
}

@Composable
private fun MainAppScreen(
    onLogout: () -> Unit = {}
) {
    var activeView by remember { mutableStateOf("HOME") }
    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            when (activeView) {
                "MARKET" -> MarketContainerView(
                    onNavigateHome = { activeView = "HOME" }
                )

                "CHAT" -> ChatHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onOpenGroupsClick = { activeView = "GROUP_CHAT" },
                    onOpenPrivateChatClick = { activeView = "PRIVATE_CHAT" }
                )

                "PRIVATE_CHAT" -> SharedPrivateChatScreen(
                    partner = null, // Başlangıçta kullanıcı arama listesini açar
                    onNavigateBack = { activeView = "CHAT" },
                    onHomeClick = { activeView = "HOME" }
                )

                "GROUP_CHAT" -> SharedGroupChatListScreen(
                    onNavigateBack = { activeView = "CHAT" },
                    onHomeClick = { activeView = "HOME" }
                )

                "PROFILE" -> ProfileScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onNavigateHome = { activeView = "HOME" },
                    onCloseApp = onLogout
                )

                "GUIDE" -> SharedGuideScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onOpenMarket = { activeView = "MARKET" },
                    onOpenMete = { activeView = "METE" }
                )

                "METE" -> MeteHomeScreen(
                    onNavigateBack = { activeView = "HOME" }
                )

                else -> HomeScreen(
                    onOpenDrawer = { isDrawerOpen = true },
                    onOpenMarket = { activeView = "MARKET" },
                    onOpenChat = { activeView = "CHAT" },
                    onOpenProfile = { activeView = "PROFILE" },
                    onOpenMete = { activeView = "METE" }
                )
            }
        }

        // Sol Yan Menü (Drawer)
        DrawerSheetContent(
            isOpen = isDrawerOpen,
            onDismiss = { isDrawerOpen = false },
            onNavigateProfile = {
                isDrawerOpen = false
                activeView = "PROFILE"
            },
            onNavigateGuide = {
                isDrawerOpen = false
                activeView = "GUIDE"
            },
            onLogout = {
                isDrawerOpen = false
                onLogout()
            }
        )
    }
}
