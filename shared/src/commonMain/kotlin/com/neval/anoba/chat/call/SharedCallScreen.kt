package com.neval.anoba.chat.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neval.anoba.common.ui.UserAvatar

@Composable
fun SharedCallScreen(
    partnerName: String = "Anoba",
    partnerPhotoUrl: String? = null,
    isVideoCall: Boolean = false,
    isMuted: Boolean = false,
    isCameraOff: Boolean = false,
    isSpeakerOn: Boolean = false,
    callEnded: Boolean = false,
    formattedDuration: String = "00:00",
    currentStatusText: String = "Arama Yapılıyor...",
    onToggleMute: () -> Unit = {},
    onToggleCamera: () -> Unit = {},
    onToggleSpeaker: () -> Unit = {},
    onSwitchCamera: () -> Unit = {},
    onEndCall: () -> Unit = {},
    remoteVideoSlot: @Composable () -> Unit = {},
    localVideoSlot: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // 1. ARKA PLAN VE ANA ALAN (Video veya Sesli Arama Avatarı)
        if (isVideoCall) {
            Box(modifier = Modifier.fillMaxSize()) {
                remoteVideoSlot()

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 16.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape
                ) {
                    Text(
                        text = formattedDuration,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    photoUrl = partnerPhotoUrl,
                    username = partnerName,
                    size = 140.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStatusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(42.dp))
            }
        }

        // 2. LOKAL KULLANICI VİDEOSU (Sağ üst küçük pencere)
        if (isVideoCall && !isCameraOff) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .size(120.dp, 160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.DarkGray)
            ) {
                localVideoSlot()
            }
        }

        // 3. ARAMA KONTROL BUTONLARI (Alt bar)
        if (!callEnded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Sessiz
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Sessiz",
                            tint = if (isMuted) Color.Black else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Aramayı Bitir (Kırmızı)
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Aramayı Bitir",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Kamera veya Hoparlör
                    if (isVideoCall) {
                        IconButton(
                            onClick = onToggleCamera,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isCameraOff) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = "Kamera",
                                tint = if (isCameraOff) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = "Hoparlör",
                                tint = if (isSpeakerOn) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Ön / Arka Kamera Değiştirme
                if (isVideoCall && !isCameraOff) {
                    Spacer(modifier = Modifier.height(20.dp))
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Kamerayı Çevir",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
