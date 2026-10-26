package com.neval.anoba

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.neval.anoba.chat.call.CallModel
import com.neval.anoba.chat.call.CallStatus
import com.neval.anoba.common.services.RingtoneService
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.ui.theme.AnobaTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {

    private var isReady by mutableStateOf(value = false)

    private lateinit var appUpdateManager: AppUpdateManager

    private var pendingNavigationTarget = mutableStateOf<String?>(null)

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.e("MainActivity", "Güncelleme akışı başarısız veya iptal edildi. Kod: ${result.resultCode}")
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateCompleteToast()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("MainActivity", "Bildirim izni reddedildi.")
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isReady }

        super.onCreate(savedInstanceState)

        // 🚀 Android 11-13 cihazlardaki çökme hatasını (UnsupportedOperationException) önleyen güvenli Edge-to-Edge
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.w("MainActivity", "EdgeToEdge güvenli başlatma atlandı: ${e.message}")
        }

        handleIntent(intent)

        try {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager.registerListener(installStateUpdatedListener)
        } catch (e: Exception) {
            Log.e("MainActivity", "AppUpdateManager başlatılamadı: ${e.message}")
        }

        askNotificationPermission()
        isReady = true

        setContent {
            if (isReady) {
                KoinContext {
                    AnobaTheme {
                        val firestore: FirebaseFirestore = org.koin.compose.koinInject()
                        val navController = rememberNavController()

                        val authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = koinViewModel()
                        val followViewModel: com.neval.anoba.follow.FollowViewModel = koinViewModel()
                        val authState by authViewModel.authState.collectAsState()
                        val isAuthenticated = authState is com.neval.anoba.login.state.AuthState.Authenticated

                        var incomingCall by remember { mutableStateOf<CallModel?>(null) }

                        LaunchedEffect(isAuthenticated) {
                            if (isAuthenticated) {
                                followViewModel.syncFollows()
                            }
                        }

                        LaunchedEffect(pendingNavigationTarget.value, isAuthenticated) {
                            if (isAuthenticated && (pendingNavigationTarget.value != null)) {
                                val targetRoute = pendingNavigationTarget.value
                                delay(300)
                                try {
                                    navController.navigate(targetRoute!!) {
                                        launchSingleTop = true
                                    }
                                    pendingNavigationTarget.value = null
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Navigasyon denemesi başarısız: ${e.message}")
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            try {
                                delay(3000)
                                checkForUpdates()
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Update check skipped: ${e.message}")
                            }
                        }

                        val currentUid = (authState as? com.neval.anoba.login.state.AuthState.Authenticated)?.uid ?: ""
                        val isAnonymous = (authState as? com.neval.anoba.login.state.AuthState.Authenticated)?.isAnonymous ?: true

                        DisposableEffect(currentUid) {
                            var listenerRegistration: ListenerRegistration? = null

                            if (currentUid.isNotBlank() && !isAnonymous) {
                                try {
                                    listenerRegistration = firestore.collection("calls")
                                        .whereEqualTo("receiverId", currentUid)
                                        .whereEqualTo("status", "RINGING")
                                        .addSnapshotListener { snapshot, error ->
                                            if (error != null) return@addSnapshotListener

                                            val doc = snapshot?.documents?.firstOrNull()
                                            if (doc != null && doc.exists()) {
                                                try {
                                                    val callerId: String? = doc.getString("callerId")
                                                    val channelName: String? = doc.getString("channelName")
                                                    val ts: Timestamp? = doc.getTimestamp("timestamp")
                                                    val isVideo: Boolean = doc.getBoolean("isVideoCall") ?: true

                                                    if (!callerId.isNullOrEmpty() && callerId != currentUid) {
                                                        val now = Timestamp.now().seconds
                                                        if (now - (ts?.seconds ?: 0) < 60) {
                                                            incomingCall = CallModel(
                                                                id = doc.id,
                                                                callerId = callerId,
                                                                receiverId = currentUid,
                                                                channelName = channelName ?: "",
                                                                isVideoCall = isVideo,
                                                                status = "RINGING",
                                                                timestamp = ts
                                                            )
                                                        }
                                                    }
                                                } catch (_: Exception) {}
                                            } else {
                                                incomingCall = null
                                            }
                                        }
                                } catch (_: Exception) { }
                            } else {
                                incomingCall = null
                            }

                            onDispose { listenerRegistration?.remove() }
                        }

                        LaunchedEffect(incomingCall) {
                            val intent = Intent(this@MainActivity, RingtoneService::class.java)
                            if (incomingCall != null) {
                                intent.action = RingtoneService.ACTION_START
                                try {
                                    ContextCompat.startForegroundService(this@MainActivity, intent)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Zil sesi başlatılamadı: ${e.message}")
                                }
                            } else {
                                stopService(intent)
                            }
                        }

                        incomingCall?.let { call ->
                            AlertDialog(
                                onDismissRequest = { },
                                title = { Text(stringResource(R.string.call_incoming)) },
                                text = { Text(if(call.isVideoCall) stringResource(R.string.call_video_incoming) else stringResource(R.string.call_voice_incoming)) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val route = "call_screen/${call.channelName}/${call.isVideoCall}/true"
                                            firestore.collection("calls").document(call.id).update("status", "ACCEPTED")
                                            incomingCall = null
                                            navController.navigate(route)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                                    ) { Text(stringResource(R.string.call_accept), color = Color.White) }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            val docId = call.id.ifEmpty { call.channelName }
                                            firestore.collection("calls").document(docId).update("status", CallStatus.REJECTED.name)
                                            incomingCall = null
                                        }
                                    ) { Text(stringResource(R.string.call_reject), color = Color.Red) }
                                }
                            )
                        }

                        val isBanned = (authState as? com.neval.anoba.login.state.AuthState.Authenticated)?.isBanned == true

                        if (isBanned) {
                            com.neval.anoba.common.ui.BannedUserScreen(
                                onLogoutClick = { authViewModel.logout() }
                            )
                        } else {
                            MainAppNavHost(navController, Constants.LOGIN_NAV_GRAPH)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val senderId: String? = it.getStringExtra("senderId")
            val groupId: String? = it.getStringExtra("groupId")
            val type: String? = it.getStringExtra("type")

            when {
                type == "CALL" -> {
                    // FCM'den gelen çağrı bilgisini SnapshotListener otomatik yakalar.
                }
                !groupId.isNullOrBlank() -> {
                    pendingNavigationTarget.value = "${Constants.GROUP_CHAT_SCREEN}/$groupId"
                }
                !senderId.isNullOrBlank() -> {
                    pendingNavigationTarget.value = "${Constants.PRIVATE_CHAT_SCREEN}?userId=$senderId"
                }
                type == "GENERAL" -> {
                    pendingNavigationTarget.value = Constants.CHAT_HOME_SCREEN
                }
                type == "ANOBA_RESPONSE" || type == "SEALED_LETTER" || type == "LETTER_REACTION" || type == "LETTER_LIKE" -> {
                    val letterId: String? = it.getStringExtra("letterId") ?: it.getStringExtra("targetId")
                    if (!letterId.isNullOrBlank()) {
                        pendingNavigationTarget.value = Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", letterId)
                    }
                }
                type == "LETTER_COMMENT" -> {
                    val letterId: String? = it.getStringExtra("letterId")
                    if (!letterId.isNullOrBlank()) {
                        pendingNavigationTarget.value = Constants.LETTER_COMMENTS_ROUTE.replace("{letterId}", letterId)
                    }
                }
            }
        }
    }

    private fun checkForUpdates() {
        if (!::appUpdateManager.isInitialized) return
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Güncelleme akışı hatası", e)
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("MainActivity", "GMS Güncelleme kontrolü yapılamadı: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "AppUpdateManager erişim hatası", e)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showUpdateCompleteToast() {
        Toast.makeText(applicationContext, getString(R.string.update_downloaded), Toast.LENGTH_LONG).show()
        if (::appUpdateManager.isInitialized) appUpdateManager.completeUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appUpdateManager.isInitialized) appUpdateManager.unregisterListener(installStateUpdatedListener)

        val intent = Intent(this, RingtoneService::class.java).apply {
            action = RingtoneService.ACTION_STOP
        }
        stopService(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) showUpdateCompleteToast()
            }
        }
    }
}