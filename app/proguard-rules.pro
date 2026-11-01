# Firebase Firestore Modellerini Koru
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

# @Keep Anotasyonu Tanımı
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Veritabanı ile eşleşen tüm modelleri koru (Kritik: Market sürümü için şart)
-keep class com.neval.anoba.chat.general.GeneralChatMessage { *; }
-keep class com.neval.anoba.chat.general.GeneralChatUser { *; }
-keep class com.neval.anoba.chat.group.ChatGroup { *; }
-keep class com.neval.anoba.chat.group.GroupMessage { *; }
-keep class com.neval.anoba.chat.privatechat.PrivateMessage { *; }
-keep class com.neval.anoba.mosaic.MosaicItem { *; }
-keep class com.neval.anoba.chat.call.CallModel { *; }

# Ana içerik modellerini koru (Market sürümünde verilerin görünmesi için)
-keep class com.neval.anoba.video.VideoModel { *; }
-keep class com.neval.anoba.photo.PhotoModel { *; }
-keep class com.neval.anoba.letter.LetterModel { *; }
-keep class com.neval.anoba.ses.SesModel { *; }

# Shared KMP Modelleri (Room & Serialization Koruması)
-keep class com.neval.anoba.photo.SharedPhotoModel { *; }
-keep class com.neval.anoba.photo.SharedPhotoDetailModel { *; }
-keep class com.neval.anoba.video.SharedVideoModel { *; }
-keep class com.neval.anoba.video.SharedVideoDetailModel { *; }
-keep class com.neval.anoba.video.SharedVideoCommentModel { *; }
-keep class com.neval.anoba.ses.SharedSesModel { *; }
-keep class com.neval.anoba.ses.SharedSesDetailModel { *; }
-keep class com.neval.anoba.ses.SharedSesCommentModel { *; }
-keep class com.neval.anoba.common.database.** { *; }

# Yorum modellerini koru (Yorumların görünmeme sorunu için)
-keep class com.neval.anoba.photo.photocomment.PhotoComment { *; }
-keep class com.neval.anoba.video.videocomment.VideoComment { *; }
-keep class com.neval.anoba.ses.sescomment.SesComment { *; }
-keep class com.neval.anoba.letter.lettercomment.LetterComment { *; }

# Proje Modelleri için Alan Korumaları
-keepclassmembers class com.neval.anoba.models.** { *; }
-keepclassmembers class com.neval.anoba.mete.** { *; }

# Firebase, Koin, Coil ve Media3 Uyarı Bastırma (AAR Consumer Rules tarafından desteklenir)
-dontwarn com.google.firebase.**
-dontwarn org.koin.**
-dontwarn coil.**
-dontwarn androidx.media3.**

# Lottie (Animasyon) Kuralları
-keepclassmembers class com.airbnb.lottie.** { *; }

# Media3 Transformer, Effect & Common Reflection Korumaları
-keepclassmembers class androidx.media3.transformer.** { *; }
-keepclassmembers class androidx.media3.effect.** { *; }
-keepclassmembers class androidx.media3.common.** { *; }
