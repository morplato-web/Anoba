package com.neval.anoba

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.neval.anoba.chat.call.SharedCallScreen
import com.neval.anoba.chat.general.SharedChatHomeScreen
import com.neval.anoba.chat.group.SharedChatGroup
import com.neval.anoba.chat.group.SharedGroupChatListScreen
import com.neval.anoba.chat.group.SharedGroupChatScreen
import com.neval.anoba.chat.group.SharedGroupInfoScreen
import com.neval.anoba.chat.privatechat.SharedChatPartner
import com.neval.anoba.chat.privatechat.SharedPrivateChatScreen
import com.neval.anoba.drawer.DrawerSheetContent
import com.neval.anoba.guide.SharedGuideScreen
import com.neval.anoba.home.SharedHomeScreen
import com.neval.anoba.home.SharedHomeViewModel
import com.neval.anoba.letter.SharedLetterCommentsScreen
import com.neval.anoba.letter.SharedLetterCreateScreen
import com.neval.anoba.letter.SharedLetterDetailScreen
import com.neval.anoba.letter.SharedLetterEditScreen
import com.neval.anoba.letter.SharedLetterHomeScreen
import com.neval.anoba.letter.SharedLetterModel
import com.neval.anoba.login.AuthContainerView
import com.neval.anoba.map.SharedGlobalMapScreen
import com.neval.anoba.market.MarketContainerView
import com.neval.anoba.mete.SharedMeteHomeScreen
import com.neval.anoba.mycontent.SharedMyContentScreen
import com.neval.anoba.mycontent.SharedMyContentType
import com.neval.anoba.photo.SharedPhotoCommentsScreen
import com.neval.anoba.photo.SharedPhotoDetailModel
import com.neval.anoba.photo.SharedPhotoDetailScreen
import com.neval.anoba.photo.SharedPhotoEditScreen
import com.neval.anoba.photo.SharedPhotoHomeScreen
import com.neval.anoba.photo.SharedPhotoModel
import com.neval.anoba.drawer.profile.SharedProfileScreen
import com.neval.anoba.ses.SharedSesCommentsScreen
import com.neval.anoba.ses.SharedSesDetailModel
import com.neval.anoba.ses.SharedSesDetailScreen
import com.neval.anoba.ses.SharedSesEditScreen
import com.neval.anoba.ses.SharedSesHomeScreen
import com.neval.anoba.ses.SharedSesModel
import com.neval.anoba.ui.theme.AnobaTheme
import com.neval.anoba.video.SharedCameraScreen
import com.neval.anoba.video.SharedVideoCommentsScreen
import com.neval.anoba.video.SharedVideoDetailModel
import com.neval.anoba.video.SharedVideoDetailScreen
import com.neval.anoba.video.SharedVideoEditScreen
import com.neval.anoba.video.SharedVideoHomeScreen
import com.neval.anoba.video.SharedVideoModel

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
    val homeViewModel = remember { SharedHomeViewModel() }
    val homeFeedItems by homeViewModel.feedItems.collectAsState()

    var activeView by remember { mutableStateOf("HOME") }
    var selectedLetter by remember { mutableStateOf<SharedLetterModel?>(null) }
    var selectedPhoto by remember { mutableStateOf<SharedPhotoModel?>(null) }
    var selectedVideo by remember { mutableStateOf<SharedVideoModel?>(null) }
    var selectedSes by remember { mutableStateOf<SharedSesModel?>(null) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    // Sohbet ve Arama State'leri
    var activeGroupId by remember { mutableStateOf<String?>(null) }
    var activePartnerId by remember { mutableStateOf<String?>(null) }
    var activeCallChannelName by remember { mutableStateOf("") }
    var activeCallIsVideo by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            when (activeView) {
                "MARKET" -> MarketContainerView(
                    onNavigateHome = { activeView = "HOME" }
                )

                "CHAT" -> SharedChatHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onOpenGroupsClick = { activeView = "GROUP_CHAT" },
                    onOpenPrivateChatClick = { 
                        activePartnerId = null
                        activeView = "PRIVATE_CHAT" 
                    }
                )

                "PRIVATE_CHAT" -> SharedPrivateChatScreen(
                    partner = activePartnerId?.let { id -> SharedChatPartner(id = id, username = "Kullanıcı") },
                    onNavigateBack = { activeView = "CHAT" },
                    onHomeClick = { activeView = "HOME" },
                    onStartVoiceCall = {
                        activeCallChannelName = "Sesli Arama"
                        activeCallIsVideo = false
                        activeView = "CALL"
                    },
                    onStartVideoCall = {
                        activeCallChannelName = "Görüntülü Arama"
                        activeCallIsVideo = true
                        activeView = "CALL"
                    }
                )

                "GROUP_CHAT" -> SharedGroupChatListScreen(
                    onNavigateBack = { activeView = "CHAT" },
                    onHomeClick = { activeView = "HOME" },
                    onGroupClick = { group ->
                        activeGroupId = group.id
                        activeView = "GROUP_CHAT_DETAIL"
                    }
                )

                "GROUP_CHAT_DETAIL" -> SharedGroupChatScreen(
                    group = activeGroupId?.let { id -> SharedChatGroup(id = id, name = "Grup Sohbeti") },
                    onNavigateBack = { activeView = "GROUP_CHAT" },
                    onHomeClick = { activeView = "HOME" },
                    onGroupInfoClick = { id ->
                        activeGroupId = id
                        activeView = "GROUP_INFO"
                    }
                )

                "GROUP_INFO" -> SharedGroupInfoScreen(
                    group = activeGroupId?.let { id -> SharedChatGroup(id = id, name = "Grup Bilgisi") },
                    onNavigateBack = { activeView = "GROUP_CHAT_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "CALL" -> SharedCallScreen(
                    partnerName = activeCallChannelName.ifBlank { "Anoba Kullanıcısı" },
                    isVideoCall = activeCallIsVideo,
                    onEndCall = { activeView = "HOME" }
                )

                "PROFILE" -> SharedProfileScreen(
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

                "METE" -> SharedMeteHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                )

                "LETTER" -> SharedLetterHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onCreateLetterClick = { activeView = "LETTER_CREATE" },
                    onLetterClick = { letter ->
                        selectedLetter = letter
                        activeView = "LETTER_DETAIL"
                    },
                    onCommentClick = { letter ->
                        selectedLetter = letter
                        activeView = "LETTER_COMMENTS"
                    }
                )

                "LETTER_CREATE" -> SharedLetterCreateScreen(
                    onNavigateBack = { activeView = "LETTER" },
                    onHomeClick = { activeView = "HOME" }
                )

                "LETTER_DETAIL" -> SharedLetterDetailScreen(
                    letter = selectedLetter,
                    onNavigateBack = { activeView = "LETTER" },
                    onHomeClick = { activeView = "HOME" },
                    onOpenComments = { activeView = "LETTER_COMMENTS" }
                )

                "LETTER_EDIT" -> SharedLetterEditScreen(
                    letter = selectedLetter,
                    onNavigateBack = { activeView = "LETTER_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "LETTER_COMMENTS" -> SharedLetterCommentsScreen(
                    letterId = selectedLetter?.id ?: "",
                    onNavigateBack = { activeView = "LETTER_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "PHOTO" -> SharedPhotoHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onPhotoClick = { photo ->
                        selectedPhoto = photo
                        activeView = "PHOTO_DETAIL"
                    },
                    onCommentClick = { photo ->
                        selectedPhoto = photo
                        activeView = "PHOTO_COMMENTS"
                    }
                )

                "PHOTO_DETAIL" -> SharedPhotoDetailScreen(
                    photo = selectedPhoto?.let { p ->
                        SharedPhotoDetailModel(
                            id = p.id,
                            ownerId = p.ownerId,
                            username = p.username,
                            userProfileImageUrl = p.userProfileImageUrl,
                            photoUrl = p.photoUrl,
                            title = p.title,
                            personalNote = p.personalNote,
                            viewCount = p.viewCount,
                            commentsCount = p.commentsCount,
                            isOfficial = p.isOfficial,
                            userBadgeType = p.userBadgeType,
                            timeAgoText = "az önce"
                        )
                    },
                    onNavigateBack = { activeView = "PHOTO" },
                    onHomeClick = { activeView = "HOME" },
                    onEditClick = { activeView = "PHOTO_EDIT" },
                    onOpenComments = { activeView = "PHOTO_COMMENTS" }
                )

                "PHOTO_EDIT" -> SharedPhotoEditScreen(
                    photo = selectedPhoto,
                    onNavigateBack = { activeView = "PHOTO_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "PHOTO_COMMENTS" -> SharedPhotoCommentsScreen(
                    photoId = selectedPhoto?.id ?: "",
                    onNavigateBack = { activeView = "PHOTO_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "VIDEO" -> SharedVideoHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onVideoClick = { video ->
                        selectedVideo = video
                        activeView = "VIDEO_DETAIL"
                    },
                    onCommentClick = { video ->
                        selectedVideo = video
                        activeView = "VIDEO_COMMENTS"
                    }
                )

                "VIDEO_DETAIL" -> SharedVideoDetailScreen(
                    video = selectedVideo?.let { v ->
                        SharedVideoDetailModel(
                            id = v.id,
                            ownerId = v.ownerId,
                            username = v.username,
                            userProfileImageUrl = v.userProfileImageUrl,
                            videoUrl = v.videoUrl,
                            thumbnailUrl = v.thumbnailUrl,
                            title = v.title,
                            personalNote = v.personalNote,
                            viewCount = v.viewCount,
                            commentsCount = v.commentsCount,
                            isOfficial = v.isOfficial,
                            userBadgeType = v.userBadgeType,
                            timeAgoText = "az önce"
                        )
                    },
                    onNavigateBack = { activeView = "VIDEO" },
                    onHomeClick = { activeView = "HOME" },
                    onEditClick = { activeView = "VIDEO_EDIT" },
                    onOpenComments = { activeView = "VIDEO_COMMENTS" }
                )

                "VIDEO_EDIT" -> SharedVideoEditScreen(
                    video = selectedVideo,
                    onNavigateBack = { activeView = "VIDEO_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "VIDEO_COMMENTS" -> SharedVideoCommentsScreen(
                    videoId = selectedVideo?.id ?: "",
                    onNavigateBack = { activeView = "VIDEO_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "CAMERA" -> SharedCameraScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onSaveMedia = { activeView = "VIDEO" }
                )

                "SES" -> SharedSesHomeScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onSesClick = { ses ->
                        selectedSes = ses
                        activeView = "SES_DETAIL"
                    },
                    onCommentClick = { ses ->
                        selectedSes = ses
                        activeView = "SES_COMMENTS"
                    }
                )

                "SES_DETAIL" -> SharedSesDetailScreen(
                    ses = selectedSes?.let { s ->
                        SharedSesDetailModel(
                            id = s.id,
                            ownerId = s.ownerId,
                            username = s.username,
                            userProfileImageUrl = s.userProfileImageUrl,
                            audioUrl = s.audioUrl,
                            title = s.title,
                            personalNote = s.personalNote,
                            viewCount = s.viewCount,
                            commentsCount = s.commentsCount,
                            durationText = s.durationText,
                            durationMillis = s.durationMillis,
                            isOfficial = s.isOfficial,
                            userBadgeType = s.userBadgeType,
                            timeAgoText = "az önce"
                        )
                    },
                    onNavigateBack = { activeView = "SES" },
                    onHomeClick = { activeView = "HOME" },
                    onEditClick = { activeView = "SES_EDIT" },
                    onOpenComments = { activeView = "SES_COMMENTS" }
                )

                "SES_EDIT" -> SharedSesEditScreen(
                    ses = selectedSes,
                    onNavigateBack = { activeView = "SES_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "SES_COMMENTS" -> SharedSesCommentsScreen(
                    sesId = selectedSes?.id ?: "",
                    onNavigateBack = { activeView = "SES_DETAIL" },
                    onHomeClick = { activeView = "HOME" }
                )

                "MY_CONTENT" -> SharedMyContentScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onCameraClick = { activeView = "CAMERA" },
                    onLetterClick = { activeView = "LETTER_CREATE" },
                    onGalleryClick = { activeView = "PHOTO" },
                    onItemClick = { item ->
                        activeView = when (item.type) {
                            SharedMyContentType.LETTER -> "LETTER"
                            SharedMyContentType.PHOTO -> "PHOTO"
                            SharedMyContentType.VIDEO -> "VIDEO"
                            SharedMyContentType.AUDIO -> "SES"
                            SharedMyContentType.MARKET -> "MARKET"
                        }
                    }
                )

                "GLOBAL_MAP" -> SharedGlobalMapScreen(
                    onNavigateBack = { activeView = "HOME" },
                    onHomeClick = { activeView = "HOME" },
                    onCountryClick = { activeView = "HOME" }
                )

                else -> SharedHomeScreen(
                    feedItems = homeFeedItems,
                    onOpenDrawer = { isDrawerOpen = true },
                    onOpenMarket = { activeView = "MARKET" },
                    onOpenChat = { activeView = "CHAT" },
                    onOpenMap = { activeView = "GLOBAL_MAP" },
                    onOpenMyContents = { activeView = "MY_CONTENT" },
                    onOpenProfile = { activeView = "PROFILE" },
                    onOpenMete = { activeView = "METE" },
                    onOpenLetter = { activeView = "LETTER" },
                    onOpenLetterCreate = { activeView = "LETTER_CREATE" },
                    onOpenGallery = { activeView = "PHOTO" },
                    onOpenCamera = { activeView = "CAMERA" }
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
