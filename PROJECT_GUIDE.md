# Anoba Proje Özeti & Mimarî Protokol (Sürüm 9.2.2 - v142)

⚠️ **GELİŞTİRİCİ İÇİN KRİTİK KURALLAR (ASLA BOZMA)** ⚠️
1. **Listing Tasarımı:** Ana akıştaki kartlar (Listing) daima SADE kalmalıdır. Sadece "En Çok Beğenilen Emoji + Sayı" görünür. Emoji seçim barı ASLA listing kartlarına eklenmez, sadece Detay sayfalarında olur.
2. **Zoom Mantığı:** Listing kartlarında (HomeScreen) iki parmakla zoom ASLA olmayacak. Zoom sadece Detay sayfalarına özeldir.
3. **Veri Akışı (Local-First):** Uygulama her zaman önce Room'dan (Offline) okumalıdır. Reaksiyonlarda dahi ağ cevabı beklenmeden önce Room güncellenir (Optimistic Update).
4. **Zil Sesi Durdurma:** Zil sesini durdurmak için `startService` kullanılmaz, daima `stopService` kullanılır (Arka plan çökmelerini önlemek için).
5. **Görsel Yükleme:** Yeni yüklenen fotoğraflar mutlaka `compressImage` (1080px, %80 kalite) filtresinden geçerek Firebase'e gönderilmelidir.
6. **Chat Tasarımı:** Sohbet giriş kutuları (ChatInput) daima `Scaffold`'un `bottomBar` parametresine yazılmalıdır. `content` içine yazıldığında klavye ve sistem barı boşlukları (`innerPadding`) ile çakışma yaşanmaktadır.
7. **Reaksiyon Listeleri & Koleksiyon Grubu Kuralları:** `collectionGroup` sorgularının normal kullanıcılara engel oluşturmaması için `firestore.rules` içinde her zaman `match /{path=**}/[koleksiyon_adi]/{reactionId}` kuralı açık olmalı, `firestore.indexes.json` içinde `fieldOverrides` altında `COLLECTION_GROUP` tanımı bulunmalı ve veriler `DocumentSnapshot` üzerinden güvenli okunmalıdır.

---

## 1. Mimarî Yapı (v111 Güncellemeleri)
- **Generic UiState Safety:** `UiState<out T>` yapısı tüm projede standartlaştırıldı. Başarılı veri durumları artık `UiState.Success<T>` şeklinde açıkça tanımlanarak "type casting" hataları ve "always null" uyarıları giderildi.
- **Paging 3 Stabilization:** `GeneralChat` ve `PrivateChat` ekranlarındaki Paging 3 akışı, ViewModel başlatıldığında (`init`) otomatik tetiklenecek şekilde optimize edildi.

## 2. Görsel Standartlar & UX
- **HomeScreen Feed Fix:** Ana sayfadaki içerik akışının (Feed) yüksekliği, alt panel (Mete) kapalıyken tüm ekranı kaplayacak şekilde düzeltildi. `innerPadding` çakışmaları temizlendi.
- **Navigation Cleanup:** Yanlışlıkla eklenen global alt menü (Bottom Bar) kaldırılarak uygulamanın özgün ve sade tasarımı korundu.
- **TopAppBar Premium:** Sohbet ekranlarındaki üst barlar; gölge, yuvarlatılmış alt köşeler ve `AnobaNavy` renk uyumuyla modernize edildi.
- **Chat Balloons:** Mesaj balonlarına yönlü kavis (corner radius) ve hafif gölge eklenerek WhatsApp stili premium görünüm sağlandı.

## 3. Sohbet Modülü & Yetkili (Official) Mesaj Mimarisi (v128)
- **Official Message Masking:** Yetkili (`OFFICIAL` / `ADMIN`) hesapların attığı mesajlar, kullanıcının kişisel profil adı ne olursa olsun veritabanına ve ekrana her zaman **"Anoba"** adıyla ve altın rozetiyle yazılır.
- **Floating Pill Tasarımı:** Mesaj yazma alanı "havada süzülen hap" tasarımına geçirildi.
- **Real-time Sync Fix:** `GeneralChatViewModel` içinde mesajların gönderilmesine rağmen görünmemesi sorunu, `init` bloğunda `loadChatMessages` çağrısı yapılarak çözüldü.

## 4. Performans & Güvenlik
- **Privacy First (Kişi Arama):** Özel sohbet başlatırken kişi arama ekranı artık boşken kimseyi listelemiyor. Veritabanı yükünü azaltmak için kullanıcı arama yaptıkça sonuçlar geliyor.
- **CallScreen Optimization:** Sesli ve görüntülü aramalarda `FLAG_KEEP_SCREEN_ON` ile ekranın kapanması önlendi. Arka plana aranan kişinin profil fotoğrafının bulanık hali (blur) yerleştirildi.

---

## 5. Küresel Erişim & Kayıt Devrimi (v118 Güncellemeleri)
- **Google Sign-In (Credential Manager):** Uygulamaya "Tek Tıkla Giriş" özelliği eklendi. Android'in en güncel `Credential Manager` altyapısı kullanılarak `Login` ve `SignUp` ekranlarına marka kurallarına uygun, renkli logolu Google butonları yerleştirildi.
- **MainActivity Stability Fix:** Android 11, 12 ve 13 yüklü cihazlarda açılışta yaşanan `UnsupportedOperationException: Unknown windowLayoutInDisplayCutoutMode: 3` hatası, güvenli versiyon kontrolleri ve `try-catch` blokları ile giderildi.
- **Session-Based Hybrid Feed:** Ana sayfa (HomeScreen) akışı optimize edildi:
    - İlk 10 içerik daima en yeni (kronolojik) sırada kalır.
    - Kalan içerikler oturum başında bir kez karıştırılır (`_sessionShuffledOldMap`) ve kullanıcı detaydan geri döndüğünde liste sırası bozulmaz.
- **Real-time Stats Sync:** İzlenme sayıları (`viewCount`) artık hem Firebase hem de yerel Room veritabanına aynı anda yazılıyor.

---

## 6. UI & Detay İyileştirmeleri (v118)
- **VideoCard Header:** Kullanıcı adı ve zaman damgasının (`9.sp`) üst üste binme/kırılma sorunu `weight(1f)` ve `maxLines=1` ile çözüldü.
- **VideoDetail Navigation:** Zaman damgası alt istatistiklerden alınarak üst başlığa (Kullanıcı adının yanına) taşındı.
- **PhotoDetail Layout:** Yatay fotoğrafların çok tepede kalmaması için üst boşluk `100.dp`'ye çıkarılarak daha dengeli bir görünüm sağlandı.

---

## 7. Merkezi Bildirim Sistemi & UX Devrimi (v128 Güncellemeleri)
- **Notification Hub (Bildirim Merkezi):** Uygulamadaki tüm sosyal etkileşimler ana ekrandaki **`NotificationHubCard`** içinde toplanır.
- **Sadeleştirilmiş Dil (11 Dil):** "Etkileşim" kelimesi çıkarıldı. Başlık mektup ve bildirim metinleri 11 dilde yerelleştirildi.
- **Temiz Avatar Gösterimi:** Reaksiyon/Tepki bildirimlerinde avatarın köşesindeki çelişkili kırmızı kalp rozeti kaldırıldı. Sadece temiz profil fotoğrafı gösterilmekte, verilen asıl emoji (❤️, 🚫, ❌ vb.) metinde vurgulanmaktadır.
- **Esnek Okuma (maxLines = 3):** Uzun yorumlar ve bildirim mesajları artık yarım kalmadan 3 satıra kadar rahatça esneyebilmektedir.
- **Sessiz Bildirim Filtresi (FCM Logic):** Sadece `CHAT` ve `CALL` tipleri kilit ekranında sesli bildirim gösterir, diğerleri sessizce hub'a aktarılır.

---

## 8. Gelişmiş Medya, Reaksiyon Mimarisi & Emoji Kullanıcı Listesi Çözümü (v128_emojisonhali)
- **Salise Hızında Tepki Güncellemesi (Local-First):** Reaksiyon verildiğinde (`toggleReaction`) ağ yanıtı beklenmeden önce yerel Room veritabanı (`videoDao.insertAll`) güncellenir. Ana sayfa akışı anında güncel skorları ekrana basar.
- **Şeffaf Video Animasyon Desteği (Alpha WebM):** `Mete` ve `Mozaik` sayfalarında arka plansız video oynatma altyapısı (`TextureView`) kuruldu.
- **Emoji Sayaç Standardizasyonu:** Tüm içerik türlerinde kartlar ve detay sayfaları **"Lider Tekil Emoji + Skor"** gösterecek şekilde eşitlendi.
- **🔥 Emoji Kullanıcı Listesi Çözümü (ÇOK KRİTİK):**
    1. **Firestore Kuralları (`firestore.rules`):** `collectionGroup` sorgularının tüm kullanıcılar için çalışabilmesi amacıyla `match /{path=**}/video_reactions/{reactionId}` vb. koleksiyon grubu wildcard kuralları eklendi.
    2. **Firestore İndeksleri (`firestore.indexes.json`):** `video_reactions`, `photo_reactions`, `ses_reactions` ve `letter_reactions` koleksiyon grupları için `fieldOverrides` altında `COLLECTION_GROUP` indeks tanımları eklendi ve `firebase deploy` ile buluta yayınlandı.
    3. **Crash-Proof Veri Okuma:** Repository katmanlarında (`VideoEmojiReactionRepository`, `PhotoEmojiReactionRepository`, `SesEmojiReactionRepository`, `LetterEmojiReactionRepository`) veriler `toObject()` bağımlılığı olmadan doğrudan `DocumentSnapshot` üzerinden okundu (`isOfficial` / `official` anahtar çakışmaları ve `timestamp` null durumları izole edildi).

---

## 9. Takip Sistemi & Akış Kişiselleştirme (v123 Güncellemeleri)
- **Hafif Takip Mekanizması (Following):** Takip butonu sadece detay sayfalarında ve profil ekranında yer alır.
- **Hibrit Senkronizasyon (SQLite Optimization):** Room (VideoDao) seviyesinde `SELECT * FROM video_table WHERE ownerId IN (SELECT followingId FROM followed_users)` sorgusu kullanılır.
- **Takipçi Listeleri:** `UserListSheet` listeleri 30'arlı paketler (chunking) halinde çeker.

---

## 10. Kamera ve Medya Optimizasyonu (v126 Güncellemeleri)
- **Kristal Netlik (Photo Quality):** `CameraScreen` çekim modu `CAPTURE_MODE_MAXIMIZE_QUALITY` olarak güncellendi.
- **Hafıza Koruması (OOM):** `PhotoRepository` içinde `inSampleSize` ve `RGB_565` ile hafıza sızıntıları önlendi.
- **Yön ve EXIF Desteği:** `AndroidX ExifInterface` ile yan çıkan fotoğraflar Matrix ile otomatik döndürülüyor.

---

## 11. Küresel Genişleme & 6 Kıta Kapsayıcılığı (v128 Güncellemeleri)
- **39+ Ülke Desteği & Tüm Dünya Kapsaması (OTHER / Diğer 🌍):** Haritaya Japonya (🇯🇵), Güney Kore (🇰🇷), Endonezya (🇮🇩), İngiltere (🇬🇧), Fransa (🇫🇷), İspanya (🇪🇸), Hollanda (🇳🇱), Kolombiya (🇨🇴), Malezya (🇲🇾), Türkmenistan (🇹🇲) ve **"Diğer 🌍"** kategorisi eklendi. Listede adı geçmeyen dünyadaki tüm ülkelerden gelen içerikler otomatik olarak **"Diğer 🌍"** filtresinde toplanır.
- **11 Dünya Dili:** Türkçe, İngilizce, Almanca, Fransızca, İspanyolca, İtalyanca, Rusça, Arapça, Svahili, Afrikaans ve **Portekizce (`values-pt`)** ile tüm arayüz yerelleştirildi.

---

## 12. Instagram & Kurumsal Web Yönlendirme (GitHub Pages)
- **Sınırsız Akıllı Yönlendirme:** Onelink limitlerini ve Instagram WebView engelini aşmak için **`https://anobaapp.github.io/-indir/`** adresi kuruldu.
- **Android Intent Protokolü:** Instagram içi tarayıcıdan tıklandığı an Android `intent://` protokolü tetiklenir ve doğrudan cihazdaki **Google Play Store uygulaması** ayağa kaldırılır.

---

## 13. Servis Sağlayıcılar & Kritik Hesap Bilgileri

### 📞 Agora (Sesli & Görüntülü Arama)
- **Servis Sağlayıcı:** [Agora.io](https://console.agora.io/)
- **Yönetim Hesabı:** `morplato@gmail.com` (Google Login).
- **Maliyet & Kota Yönetimi:** Aylık 10.000 Dakika ücretsiz. Güvenlik freni olarak tüm aramalar **45. dakikada** otomatik kapatılır.

### 🌐 Diğer Servisler & Web Köprüsü
- **Firebase Console:** `Anoba` Projesi.
- **Google Play Console:** Geliştirici Hesabı (`tarikdeveloper25@gmail.com`).
- **GitHub Pages Yönlendirme:** `https://anobaapp.github.io/-indir/` (Instagram Biyografi ve Reklam Linki).
- **ManyChat (Instagram Otomasyonu):** Essential Paketi ($17/ay - Sınırsız Yorum/DM Otomasyonu, 250 Contact Kapasiteli).

---

## 14. Bulut Yedekleri & Yayın Anahtarı (Google Drive)
Projenin kritik yedekleri ve markete gönderim anahtarı `morplato@gmail.com` hesabına bağlı Google Drive'da saklanmaktadır.

### 💾 Önemli Yedek Dosyaları
- **`Anoba_v8.9.8_Global_v139_Media3_GroupSync.zip`:** (Yerel Dosya Adresi: `D:\anobadepo\Anoba_v8.9.8_Global_v139_Media3_GroupSync.zip`) Media3 Transformer 1080p + 3.5 Mbps otomatik video sıkıştırma, AuthService canlı yetki/rozet (PIONEER/Yeşil Tik) senkronizasyonu, Herkese Açık Grup Canlı Akışı (`combine`), R8/ProGuard güvenliği ve yarış durumu (race condition) koruması içeren en güncel ve eksiksiz sürüm yedeği.
- **`Anoba_v8.9.8_Global_v128_emojisonhali.zip`:** (Yerel Dosya Adresi: `D:\anobadepo\Anoba_v8.9.8_Global_v128_emojisonhali.zip`) Emojiye basınca reaksiyon veren kullanıcılar listesinin tüm kullanıcılara açıldığı (Firestore koleksiyon grubu kuralları, buluta deploy edilmiş indeksler ve çökmez DocumentSnapshot parsing), 11 Dil Desteği, Local-First Reaksiyon Hızı ve Instagram Akıllı Linkini içeren sürüm yedeği.
- **`Anoba_v8.9.8_Global_v128_Final.zip`:** (Yerel Dosya Adresi: `D:\anobadepo\Anoba_v8.9.8_Global_v128_Final.zip`) Önceki final yedek dosyası.

### 🔑 Uygulama İmza Anahtarı (Keystore)
- **Dosya Adı:** `anoba_key.jks`
- **Önemi:** Google Play Store'a güncelleme göndermek için şarttır.
- **Drive Konumu:** Ana dizin (Drive'ım).

---

## 15. Anoba Proje Hesap Listesi (Yedek Hesaplar)

| E-Posta | Kullanıcı Adı / Unvan | Şifre | Ülke / Not |
| :--- | :--- | :--- | :--- |
| `morplato@gmail.com` | Anoba (Admin) | `59123456` | Yönetici Hesabı |
| `anobaimalat@gmail.com` | Anoba İmalat | `70123456` | Kurumsal |
| `anobaheyet@gmail.com` | anoba_heyet | `70123456` | Kurumsal |
| `anobamuhtar@gmail.com` | anoba_muhtar | `70123456` | Kurumsal |
| `anobaguide@gmail.com` | anoba_guide | `70123456` | Rehber |
| `anobapolis@gmail.com` | Anoba Polis | `70123456` | Polis |
| `anobaaslan@gmail.com` | Global Aslan | `70123456` | Öncü Hesap |
| `nevalcicek@hotmail.com` | esma | `70123456` | Öncü Hesap |
| `nevalcicek2026@gmail.com` | beyaz | `70123456` | Öncü Hesap |
| `nevalcicek2025@gmail.com` | emine | `70123456` | Öncü Hesap |
| `oneturkdeveloper@gmail.com` | emre | `70123456` | Öncü Hesap |
| `oneturkdeveloper25@gmail.com` | demir | `70123456` | Öncü Hesap |
| `ozgurcicek250@hotmail.com` | ozgur | `ozgur250` | Öncü Hesap |
| `gm.sibel.un@gmail.com` | Sblnntly | `sibel70` | Öncü Hesap |
| `cerencicek497@gmail.com` | cico | `ceren497` | Öncü Hesap |
| `cylncck20@gmail.com` | cylncck | `ceylan20` | Öncü Hesap |
| `anoba_ethanus@gmail.com` | ethan_m | `70123456` | ABD 🇺🇸 |
| `anoba_amira@gmail.com` | amira | `70123456` | Mısır 🇪🇬 |
| `anoba_kamel@gmail.com` | kamel_mansouri | `70123456` | Tunus 🇹🇳 |

---

## 16. Instagram Büyüme, Sosyal Medya Otomasyonu & Endonezya Pazarı (v135 Güncellemeleri)
- **12. Dünya Dili - Endonezce (`values-in` & `values-id`):** Dünyanın en büyük mobil pazarlarından Endonezya için tüm arayüz metinleri Bahasa Indonesia diline çevrildi ve Android uyumluluğu için hem `values-in` hem de `values-id` klasörlerine eklendi.
- **ManyChat Yorum Otomasyonu (Meta API 24/7 - Essential Paket $17/ay):**
  - **Tetikleyici Kelimeler:** `ANOBA`, `INDIR`, `LINK`, `DOWNLOAD`
  - **Çalışma Mantığı:** Kullanıcı herhangi bir Reels veya gönderinin altına bu kelimeleri yazdığında sistem salisesinde kullanıcının DM kutusuna indirme bağlantısını (`https://anobaapp.github.io/-indir`) ve butonunu otomatik gönderir.
  - **Keşfet Etkisi:** Otomatik yorumlar sayesinde Reels videolarının etkileşimi artarak Instagram Keşfet (Explore) algoritmasında öne çıkar.
- **Instagram Profil Standartları (`@anobaapp`):**
  - **Resmi Logo:** Arka planı düz beyaz zeminli net Anoba amblemi.
  - **Kurumsal Bio:**  
    🌐 *Anoba: Connect, Chat & Time Capsule*  
    ✉️ *Send letters to the future & chat freely!*  
    📲 *Available on Google Play ⬇️*  
    🔗 `anobaapp.github.io/-indir`
  - **Öne Çıkanlar (Highlights):** `ANOBA` (Tanıtım), `METE` (Çocuk/Öğrenim), `LETTERS` (Zaman Kapsülü), `WORLD` (Dünya Haritası ve Ülkeler).
- **GÜVENLİ MANUEL DM KURALI (SPAM ENGELİ KORUMASI):**
  - Günde maksimum **20-30 kişiye**, mesajlar arasında **5-10 dakika ara vererek**, kullanıcının adıyla ve kendi ana diliyle samimi davet mesajı atılır.
  - Asla toplu/bütün gün peş peşe mesaj atılmaz.
- **Instagram Usulü Profil & Yayınlanan İçerik Yönlendirmesi:**  
  - Ana akış kartlarında (Video, Fotoğraf, Mektup, Ses) kullanıcının profil resmine veya adına tıklandığında o kullanıcının yayınlanan tüm içeriklerinin listelendiği sayfaya yönlendirilir.
  - Profil ekranında fotoğraf/isim üzerine veya "Yayınlanan İçeriklerim 🎬" butonuna tıklandığında doğrudan yayınlanan içerikler sayfası açılır.

---

## 17. Reaksiyon Sayaç Senkronizasyonu & Sekme Durum Saklama (v136 Güncellemeleri)
- **4 Modülde Akıllı Emoji Sayacı (Toggle OFF/ON):**
  - `Video`, `Photo`, `Letter` ve `Ses` repository sınıflarında yerel Room güncellemesinden önce kullanıcının mevcut emojisi kontrol edilir.
  - Aynı emojiye tekrar basıldığında sayaç **`-1` düşürülür (Geri alma / Toggle OFF)**.
  - Başka emojiye geçilirse eski emoji **`-1` düşürülüp** yeni emoji **`+1` artırılır**.
  - Firestore işleminde hata olursa Room verisi `backupEntity` ile orijinal haline **Rollback** edilir.
- **Firestore Kuralları Tam Eşleme (`firestore.rules`):**
  - Normal kullanıcıların `is_popular`, `is_flagged`, `flag_reason`, `flagged_at` alanlarını güncelleyebilmesi için izinler eklendi ve buluta deploy edildi.
- **MyContentScreen Sekme Düzeni & Varsayılan Açılış (`Taslaklar`):**
  - Sekme dizilimi **Yayınlananlar / Taslaklar** olarak düzenlendi. Sayfa açıldığında varsayılan olarak **Taslaklar** sekmesi (`selectedTabIndex = 1`) aktif ve seçili olarak gelir. Sekme koruması `rememberSaveable` ile güvence atındadır.

---

## 18. Grup Sohbeti Gizlilik & Sistem Mesajları İyileştirmeleri (v137 Güncellemeleri)
- **Grup Üye Ekleme Gizliliği (E-Posta Gizleme):** Grup üye ekleme diyaloğunda kişisel e-posta adreslerinin görünmesi engellendi, yerini profesyonel kullanıcı adı (`@kullaniciadi`) aldı.
- **Mükerrer Üye Ekleme Engeli:** Bir kullanıcı gruba eklendiğinde hem yerel grupta hem de davet listesinde filtresi anında güncellenerek "Üye Ekle" listesinden düşürülür. Çift tıklama veya listede tekrar çıkma sorunu giderildi.
- **Ortalanmış Sistem Rozetleri (Center Capsule Badge):** *"x kişiyi gruba ekledi"*, *"gruptan çıktı"* gibi sistem bildirimleri kişisel mesaj balonu yerine ekranın tam ortasında zarif ve ortalanmış gri bir kapsül içinde gösterilecek şekilde modernize edildi.
- **Herkese Açık Grupların Listelenmesi (`GroupChatRepository`):** Grup listesi akışı (`getGroupsStreamForUser`), kullanıcının üye olduğu gruplar ile herkese açık grupları (`isPrivate == false`) `combine` operasyonuyla birleştirerek tüm kullanıcılara canlı göstermektedir. Ayrıca mesaj gönderildiğinde kullanıcı üyeliği otomatik güncellenmektedir.

---

## 19. Otomatik Video Sıkıştırma Pipeline (v138 Güncellemeleri)
- **Media3 Transformer İstemci Tarafı Optimizasyonu:** `VideoRepository.uploadVideo()` ve `VideoEditViewModel.trimVideo()` akışlarına `AndroidX Media3 Transformer` sıkıştırma katmanı entegre edildi.
- **Video Standartları & Bitrate Koruması:** Tüm videolar Firebase Storage'a yüklenmeden önce istemci tarafında **1080p Dikey (1080x1920)**, **H.264 video codec**, **~3.5 Mbps Hedef Bitrate (`DefaultEncoderFactory` + `VideoEncoderSettings`)**, **128 kbps AAC ses** ve **Faststart (moov atom başta)** formatına otomatik dönüştürülür.
- **Release (R8/ProGuard) Güvenliği:** `proguard-rules.pro` dosyasına `androidx.media3.transformer.**`, `androidx.media3.effect.**` ve `androidx.media3.common.**` keep kuralları eklenerek Release APK/AAB derlemelerinde çökme engellendi.
- **Senkronize Çağrılar:** `VideoViewModel`, `MyContentViewModel` ve `CameraScreen` sınıflarındaki video yükleme çağrılarına `context` parametresi eklendi ve `media3-muxer:1.4.1` modülü Gradle'a eklenerek derleme senkronizasyonu tamamlandı.

---

## 20. Canlı Yetki & Herkese Açık Grup Senkronizasyonu (v139 Güncellemeleri)
- **Canlı Yetki & Rozet Senkronizasyonu (`AuthService`):** `firestore.collection("users").document(uid).addSnapshotListener` entegre edilerek Firebase Console üzerinden kullanıcının `badgeType` alanı (`PIONEER`, `OFFICIAL`, `ELITE`) değiştirildiği salise uygulamanın canlı hafızada güncellenmesi sağlandı.
- **Herkese Açık Grup Canlı Akışı (`GroupChatRepository`):** `getGroupsStreamForUser` metodu `combine` operasyonuyla kullanıcının kendi grupları ile herkese açık (`isPrivate == false`) grupları canlı birleştirip listelemektedir.
- **Race Condition (Yarış Durumu) Güvenliği:** `addMessageToGroup` metodunda kullanıcı önce `members` dizisine `FieldValue.arrayUnion` ile eklenmekte, ardından mesaj subcollection'a yazılmaktadır. Firestore güvenlik kuralları uyumluluğu %100 sağlandı.

---

## 21. Hızlı Üretim & Paylaşım Üst Bar UX Dönüşümü (v140 Güncellemeleri)
- **Ana Sayfa Hızlı Üretim Butonları (`HomeScreen`):** Eski kafa karışıklığı yaratan liste filtre ikonları üst bardan kaldırılarak yerlerini doğrudan aksiyon alan **Hızlı Üretim / Paylaşım Butonları** aldı:
  - 🎥 **Kamera (`CameraAlt`):** Doğrudan `Constants.VIDEO_CAMERA_SCREEN` (`CameraScreen`) tam ekranını açar (Fotoğraf çekme + Video kaydetme).
  - 🖼️ **Galeri (`PhotoLibrary`):** `PickMultipleVisualMedia` sistem galeri seçicisini açarak seçilen fotoğrafları/videoları `MyContentScreen` taslaklarına doğrudan aktarır.
  - ✍️ **Kalem (`Edit`):** Doğrudan `Constants.LETTER_CREATE_SCREEN` (`LetterCreateScreen`) ekranını açarak yazılı ve sesli mektup oluşturmayı sağlar.
  - 📑 **İçeriklerim (`DynamicFeed`):** 2. sıradaki 4. kart olarak doğrudan `MyContentScreen` (Taslaklar/İçeriklerim) ekranını açar.
- **Üst Bar İkon Sıralaması & Boyut Hizalaması (`HomeScreen` 2. Sıra):** 2. sıradaki aksiyon barı **1. Zil (Bildirim) ➔ 2. Kamera (Çek) ➔ 3. Kalem (Yaz) ➔ 4. Galeri (Seç) ➔ 5. Üretim / İçeriklerim (Yönet)** olarak sıralandı. Kart boyutları bir tık küçültülüp (`cardWidth = 32.dp`, `cardHeight = 30.dp`, `iconSize = 18.dp`) sağa doğru tam hizalandı (`Arrangement.End` + `spacedBy(6.dp)`).
- **Mete Ay-Yıldız Tam Ekran & 3 Nokta Menü Entegrasyonu (`MeteScreen`):**
  - Ay-Yıldız (Mete) ikonu 1. sıradan kaldırılarak sol üstteki 3 Nokta (`MoreVert`) açılır menüsünün içine (Karantina Havuzu'ndan sonra) `Mete` ismiyle taşındı.
  - HomeScreen üzerindeki alttan açılır sheet (`BottomSheetScaffold`) kaldırılarak `HomeScreen` hafıza ve performans açısından hafifletildi.
  - Tıklandığında bağımsız tam ekran **`MeteScreen`** (`Constants.METE_SCREEN`) açılmaktadır.
  - Üst barı standartlaştırıldı: Solda **Geri Dön Oku**, ortada sade **"Mete"** başlığı, sağda uygulamanın tüm sayfalarında bulunan **Home İkonu** yer almaktadır. Dil değişimlerinde üst bar sade yapısını korur.
- **Uygulama Turu Rehberi İnce Ayarları (`HomeTutorial`):**
  - **3 Nokta Işık Odağı Sınırlandırıldı:** `moreMenuIconRect` ışık odağı doğrudan 3 Nokta ikonuna çekildi, yanındaki pembe çiçek yuvarlağının ışık çemberine girmesi engellendi.
  - **Menü İçi Özellik Açıklamaları:** 3 Nokta adımı açıklaması alt alta 3 maddenin orijinal metinleriyle zenginleştirildi (`1000+ Popüler`, `Karantina Havuzu`, `Mete`).
  - **Kullanıcı Filtresi (Arama İkonu) Tura Eklendi:** `SEARCH_FILTER` adımı tura dahil edilerek Büyüteç ikonuna ışık odağı bağlandı.
  - **"Bitti" Butonu Düzeltmesi:** Sohbetler adımında erken çıkan "Bitti" yazısı kaldırıldı. "Bitti" butonu artık turun tam son adımı olan `MY_CONTENTS` (İçeriklerim & Taslaklar) üzerinde görünmektedir.

---

## 22. Kotlin Multiplatform (KMP) & iOS Dönüşümü (v141 Güncellemeleri)
- **Ortak Modül Yapısı (`:shared`):** Android ve iOS ortak mantığı için `:shared` modülü kuruldu. Veri modelleri, Room Veritabanı (`AppDatabase`), Koin DI ve KMP mimarisi entegre edildi.
- **Compose Multiplatform UI:** Arayüz bileşenleri hem Android hem iOS üzerinde çalışacak şekilde uyumlu hale getirildi.
- **Room KMP Native Uyumlaştırması:** `ChatDao`, `FollowedUserDao`, `PinnedItemDao` ve `Converters` sınıfları KMP Native (iOS) derleme standartlarına taşındı. Android'e özel `PagingSource` bağımlılıkları Native uyumlu `Flow` akışına çevrildi.
- **Bulut Mac (MacInCloud) Entegrasyonu & GitHub CI/CD:**
  - Proje GitHub reposuna (`https://github.com/morplato-web/Anoba.git`) taşındı.
  - Bulut Mac portal hesabı: `nevalcicek@hotmail.com`
  - Bulut Mac sunucusu (`FF376.macincloud.com`, kullanıcı: `user299223`, şifre: `pkp45927chd`) kuruldu ve bilgileri güvenceye alındı.
  - Kotlin `2.1.0`, KSP `2.1.0-1.0.29` ve Room KMP `2.7.0-alpha13` sürümleri hizalanarak iOS KLIB derleme motoru tam uyumlu hale getirildi (`BUILD SUCCESSFUL`).
  - Kilitli Xcode şablonlarının yerine bağımsız **`iosApp/iosApp.xcodeproj`** projesi oluşturuldu.
  - Mac üzerinde tek komutla güncellemeleri çeken, önbellekleri temizleyen, kütüphaneyi kopyalayıp iPhone Simülatöründe Anoba'yı başlatan **`ios.sh`** ve **`open.sh`** betikleri eklendi.

---

## 23. Anoba Pazar Yeri (C2C Marketplace) Modülü & 13 Dil Yerelleştirmesi (v142 Güncellemeleri)
- **Komisyonsuz C2C İlan Altyapısı (`MarketItemModel` & `MarketDao`):** Kullanıcıların ikinci el, sıfır veya el yapımı ürünlerini ücretsiz sergilediği, komisyonsuz ve doğrudan uygulama içi sohbetle (`PrivateChatScreen`) buluştuğu Pazar Yeri modülü kuruldu.
- **Local-First Veritabanı & Firestore Sync (Room v20):** `MarketItemModel` entity'si Room v20 veritabanına eklendi. İlan görselleri istemci tarafında 1080px %80 kalitede otomatik sıkıştırılarak Firebase Storage `"market_photos"` dizinine, ilan verileri Firestore `"market_items"` koleksiyonuna ve yerel SQLite veritabanına kaydolmaktadır.
- **2'li Şık Izgara (Grid) Arayüzü (`MarketHomeScreen`):** İlanlar Letgo/Sahibinden tarzında 2 sütunlu izgara (`LazyVerticalGrid`) olarak listelenir. Ürün başlığı, göreceli zaman damgası, şehir, satıcı profil fotoğrafı ve kullanıcı adı en az yer kaplayacak şekilde kompakt tasarlanmıştır.
- **Akıllı Konum Seçimi (`LocationBottomSheet`):** İlan eklerken ve düzenlerken 81 Türkiye İli + Uluslararası şehir arama motoru entegre edildi.
- **İlan Detay & Sabit Eylem Butonu (`MarketDetailScreen`):** *"Satıcı İle Sohbet Et"* veya *"Satıldı Olarak İşaretle"* butonu ekranın en altına sabitlendi (`Scaffold.bottomBar`).
- **İlanı Düzenle (`MarketEditScreen`) & Gerekçeli Şikayet (⚠️):** İlan sahibine özel ilan güncelleme ekranı ve normal kullanıcılara özel gerekçeli şikayet etme penceresi eklendi. Şikayet edilen ilanlar otomatik Karantina Havuzuna aktarılır.
- **"İçeriklerim" ➔ "Pazar Yeri 🛍️" Sekmesi (`MyContentScreen`):** Kullanıcının yayınladığı tüm ilanları tek yerden yönetebileceği 3. sekme açıldı. Satıcı profiline tıklandığında doğrudan o satıcının Pazar Yeri sekmesine yönlendirilir.
- **Uygulama Turu (Anoba Rehberi) Entegrasyonu (`HomeTutorial`):** Step-by-step Uygulama Turu rehberine Pazar Yeri ışık odağı ve onaylı tanımı entegre edildi.
- **13 Dünya Dili Yerelleştirmesi (`strings.xml` & `build.gradle.kts`):** Tüm Pazar Yeri metinleri 13 farklı dil klasörüne (`values`, `values-en`, `values-de`, `values-fr`, `values-es`, `values-it`, `values-ru`, `values-ar`, `values-sw`, `values-af`, `values-in`, `values-id`, `values-pt`) eklendi ve `build.gradle.kts` içinde `resourceConfigurations` listesine `id` ve `in` kodları dahil edildi.
- **DEX & Release AAB Çakışma Temizliği:** `:app` ve `:shared` modülleri arasında çift tanımlanan sınıflar (`InitialsAvatar`, `AnobaBadge`, `OfficialBadge`, `UserRole`, `Theme`, `Converters`) temizlenerek Release AAB paketi (`bundleRelease`) %100 sorunsuz üretilip Google Play Console'da başarıyla yayınlandı.

---

## 24. Apple Developer Hesabı Bilgileri 🍎
- **Giriş URL:** `https://developer.apple.com/account` veya `https://appstoreconnect.apple.com`
- **Apple ID (Gmail):** `morplato@gmail.com`
- **Şifre:** `fabrikalaR3019`
- **Apple Destek Takip Kodu (Case ID):** `103017117489` *(Enrollment İnceleme Desteği Gönderildi)*

---

## 25. Paket Refactoring, KMP Standartlaşması & Oturum Notları (v143 Güncellemeleri)

### 📌 Tamamlanan Paket Çalışmaları
1. **`login` Paketi:** `:app` ve `:shared` tarafında ViewModel, AuthState ve sealed `AuthResult` sınıfları KMP standartlarında eşitlendi. Tekrarlanan ölü kodlar temizlendi.
2. **`map` Paketi:** `DEFAULT_SHARED_COUNTRIES` listesi Google Play Console verileriyle 177 hedef ülkeye genişletildi. Bayrak emojileri ve ISO kodları eklendi, ilk 39 ülkenin sıralaması korundu.
3. **`market` Paketi:** Dolap/Vinted tarzı 4 fotoğraflı galeri (`photoUrls: List<String>`), Room `@TypeConverters(StringListTypeConverter::class)` dönüşümü ve 2'li `LazyVerticalStaggeredGrid` akışı KMP katmanlarına taşındı.
4. **`mete` Paketi:** Kod satır kırılmaları, loglama hataları ve `emptySlots = 3 - rowItems.size` döngü hesaplamaları düzeltildi.
5. **`models` Paketi:** `SharedAnobaNotification` modeli `com.neval.anoba.models` paketine taşındı, `User` model linter uyarıları temizlendi.
6. **`mosaic` Paketi:** `MosaicViewModel` üzerindeki canlı Firestore akışı (`mosaicItems`) `:shared` katmanındaki `SharedMosaicSheet` bileşenine bağlandı.
7. **`mycontent` Paketi:**
   - `:shared` katmanındaki `SharedMyContentScreen` ekranı `:app` ile tam görsel uyum için `LazyVerticalStaggeredGrid` akışına geçirildi.
   - Pazar Yeri sekmesi ve kart rozetlerindeki ikon, `HomeScreen.kt` üst barındaki kurumsal `Icons.Outlined.ShoppingBag` ikonu ile eşitlendi.
   - Karantina rozetindeki çift ikon çakışması temizlendi.
8. **Room Veritabanı Şema Güncellemesi (v21):** `MarketItemModel` üzerindeki `photoUrls` sütun değişikliği nedeniyle `AppDatabase.kt` sürümü `21` yapılarak `IllegalStateException` çökmeleri önlendi.
9. **Grup Sohbet Profil Resimleri:** `SharedGroupChatListScreen` ve `SharedGroupChatScreen` üst barlarında grup kapak görselleri (`group.imageUrl`) ve mesaj balonlarında gönderen üyelerin profil fotoğrafları (`UserAvatar`) dinamik hale getirildi.
10. **Akış Önbellek Koruması (Feed Persistence Fix):** `getUserLetters` ve `getUserPhotos` çağrıldığında Room veritabanındaki diğer yayınlanmış içerikleri temizleyen `resetPublishedLetters()` / `resetPublishedPhotos()` kaldırıldı; böylece kullanıcı kendi içeriklerine bakıp Ana Sayfaya döndüğünde diğer içerikler kaybolmamaktadır.
11. **Mektup Detay Emoji Satırı & Hizalaması:** `SharedLetterDetailScreen` ve `LetterDetailScreen` ekranlarında Göz İkonu (`👁️`) ve Lider Emoji + Skor (`❤️ 128`) sol tarafa yan yana hizalandı; altına sağa/sola kaydırılabilir 12 emojilik tam standart emoji satırı (`❤️`, `🔥`, `👏`, `👍`, `😂`, `😍`, `😮`, `😢`, `🥰`...) eklendi.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna push edildi (`working tree clean`).

---

## 26. Mektup Detay Emoji Düzeltmeleri & Tip Güvenliği (v144 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **Mektup Reaksiyon Repository Tip Düzeltmesi:** `LetterEmojiReactionRepository.kt` dosyasındaki `getUserLetterReactions` fonksiyonunun dönüş tipi jenerik eksikliği düzeltilerek `Flow<Map<String, String>>` haline getirildi.
2. **13 Standart Emoji Yapısı:** `SharedLetterDetailScreen.kt` içerisindeki `ANOBA_13_EMOJIS` (13 standart emoji listesi) düzenlendi, mektup detay ekranlarındaki lider emoji + sayaç yerleşimi ve reaksiyon satırları stabilize edildi.
3. **Detay Ekranları Uyarı Temizliği:** `PhotoDetailScreen.kt` ve ilişkili detay ekranlarındaki gereksiz durum kontrolleri (`UiState.Success` uyarıları) temizlendi ve kodlar sadeleştirildi.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna yüklenmiştir.

---

## 27. Mete & Photo Paketi Kontrolleri & İyileştirmeler (v145 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **Mete Modülü Yapboz Mantığı & Veri Akışı:**
   - `MeteRepository.kt` içerisindeki Firestore `.orderBy("id")` kısıtlaması kaldırılarak tüm kartların eksiksiz çekilmesi ve bellek tarafında sıralanması sağlandı.
   - `MeteScreen` ekranı orijinal zengin `MeteSheet()` bileşenine bağlandı. Dışarıdan sadece kapalı renkli yapboz kutuları görünür hale getirildi; tıklanınca Lottie animasyonu, video, ses çalma ve mikrofon kaydı bulunan `PuzzleDetailDialog` diyalogunun açılması sağlandı.
2. **Photo Paketi İncelemesi & Temizlik:**
   - `:app` ve `:shared` tarafındaki tüm Fotoğraf sayfaları (`PhotoHomeScreen`, `PhotoDetailScreen`, `PhotoEditScreen`, `PhotoCommentsScreen`, `PhotoCard`) kontrol edildi.
   - `SharedPhotoCommentsScreen.kt` içerisindeki `photoId` parametre uyarısı `@Suppress("UNUSED_PARAMETER")` ile giderildi.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna push edildi (`working tree clean`).

---

## 28. Profile & Drawer Paketi Eşitlemesi (v146 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **Paket Yapısı Eşitlemesi:**
   - Profil sayfaları hem `:app` hem de `:shared` katmanlarında `drawer` paketinin altına `profile` alt paketi (`com.neval.anoba.drawer.profile`) olarak taşındı.
   - `:app`: `ProfileScreen.kt`, `ProfileEditScreen.kt`, `BlockedUsersScreen.kt` ➔ `com.neval.anoba.drawer.profile`
   - `:shared`: `SharedProfileScreen.kt`, `SharedProfileEditScreen.kt`, `SharedBlockedUsersScreen.kt` ➔ `com.neval.anoba.drawer.profile`
2. **Navigasyon ve Import Güncellemeleri:**
   - `DrawerNavGraph.kt` ve `App.kt` içerisindeki tüm importlar yeni pakete göre güncellendi.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna yüklenmiştir (`working tree clean`).

---

## 29. Drawer Menü Callback & Status Bar İyileştirmeleri (v147 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **Drawer Menü İşlevselliği (`SharedDrawerContent.kt`):**
   - `DrawerSheetContent` bileşenine `onOpenAdminPanel` ve `onOpenPrivacyPolicy` callback parametreleri eklendi.
   - Animasyon Paneli ve Gizlilik Politikası tıklamalarında ilgili aksiyonların tetiklenmesi sağlandı.
2. **iOS Safe Area & Status Bar Yerleşimi:**
   - `SharedProfileScreen.kt`, `SharedProfileEditScreen.kt` ve `SharedBlockedUsersScreen.kt` ekranlarında `statusBarsPadding()` modifiyeri iç `Row` üzerinden alınıp üst `Surface` bileşenine taşındı. Böylece status bar arka plan rengi tüm alana yayıldı ve üst bar içeriği 56dp içine sıkışmaktan kurtarıldı.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna yüklenmiştir (`working tree clean`).

---

## 30. Tam Proje Denetimi, Ses/Video Paket İyileştirmeleri & Build Optimizasyonu (v148 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **`ses` ve `video` Paketi İncelemeleri:**
   - `:shared` katmanındaki ses detay modülüne bağımsız `SharedSesReactionUsersBottomSheet` bileşeni eklendi.
   - `VideoPlayer.kt` ve `SharedVideoCommentsScreen.kt` üzerindeki linter uyarıları giderildi.
   - `:shared` katmanındaki `SharedSesHomeScreen.kt`, `SharedSesDetailScreen.kt`, `SharedSesEditScreen.kt`, `SharedSesCommentsScreen.kt` ve `SharedVideoHomeScreen.kt` ekranlarında `statusBarsPadding()` Surface seviyesine taşınarak iOS Safe Area uyumu yükseltildi.
2. **Kök ve Modül Yapılandırma Optimizasyonları:**
   - **`shared/build.gradle.kts`:** `:shared` modülünün `compileSdk` seviyesi `:app` ile eşitlenerek `36` yapıldı.
   - **`settings.gradle.kts`:** JetBrains Compose dev Maven deposu (`https://maven.pkg.jetbrains.space/public/p/compose/dev`) eklendi.
   - **`app/proguard-rules.pro`:** Release/R8 sürümlerinde Room ve Serializer bozulmalarını önlemek için `:shared` altındaki KMP modellerine ve veritabanı paketine `-keep` kuralları eklendi.
   - **`gradle.properties`:** Gradle Daemon bellek limiti `-Xmx4096m -XX:MaxMetaspaceSize=1024m` seviyesine yükseltildi.

### 🧪 Derleme, Yedekleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **Proje Zip Yedeği:** **`D:\anobadepo\Anoba_v9.2.4_v148_Final.zip`** adresi üzerine tam yedek alındı. 📦
- Tüm proje kodları ve rehber GitHub `origin/main` deposuna yüklenmiştir.

---

## 31. KMP iOS Canlı Akış, Birebir Android Kart Mimarisi & Profil Hizalaması (v163 Güncellemeleri)

### 📌 Tamamlanan Çalışmalar
1. **KMP Firestore Canlı Veri Akışı (`SharedHomeRepository.kt` & `HttpFetcher`):**
   - Firebase Firestore REST API (`anoba-cd9de`) entegrasyonu sağlandı. Mektup (`content`), fotoğraf (`photoUrl`), video (`videoUrl`) ve ses (`audioUrl`) koleksiyon alanları eksiksiz eşlendi.
   - iOS tarafında CocoaPods çakışmalarını önlemek için `HttpFetcher` native motoru, `coil-network-ktor3` ve `ktor-client-core` bağlandı.
2. **Birebir Android 3 Katmanlı Kart Mimarisi (`SharedFeedCard.kt`):**
   - **Üst Bant (Kart Dışında):** Profil resmi, kullanıcı adı, rozet, iğne (pin), kırmızı çöp kutusu (delete) ikonu ve zaman damgası.
   - **Orta Bölüm (Kart İçinde):** Krem zeminli mektup zarfı (`#FFFDE7`) veya fotoğraf görseli.
   - **Alt Bant (Kart Dışında):** İzlenme sayısı (`👁️`), reaksiyon lideri ve skoru, paylaşım ve yorum ikonları.
3. **Kombine Karışık Akış & Tasarım Detayları:**
   - Fotoğraf, mektup, video ve ses içeriklerini sırayla harmanlayan Round-Robin algoritması bağlandı.
   - Kartların zeminle bütünleşmesini önlemek için akış arka planı açık griye (`#F6F7F9`) çekildi ve 12.dp kart aralığı verildi.
   - "Yeni Anı" ve jenerik başlıklar gizlendi.
   - Mozaik butonu `width(105.dp)` ve `height(36.dp)` boyutuna çekildi.
   - Bildirim ikonu sarı zemin (`#FFF3CD`) ve canlı turuncu çan ikonu (`#E65100`) olarak güncellendi.
4. **Profil & Çekmece (Drawer) Hizalaması:**
   - `SharedDrawerContent.kt` ve `SharedProfileScreen.kt` içinde kullanıcının gerçek avatar resmi (`UserAvatar`), e-posta adresi (`morplato@gmail.com`) ve sarı **Anoba Panel** butonu aktif edildi.
   - Sol çekmecedeki gereksiz "Profili Görüntüle" yazısı temizlendi.
5. **Xcode & Derleme Otomasyonu (`ios.sh` & `project.pbxproj`):**
   - `SharedKit.framework` kütüphanesi hem `iosApp/Frameworks` hem de `iosApp/build` klasörüne kopyalanacak şekilde ayarlandı.
   - Xcode'un içindeki mükerrer Gradle çalıştırma adımı pasifize edildi.
6. **Apple Developer Hesabı Desteği:**
   - Apple Destek Takip Kodu (Case ID): `103017117489` oluşturularak inceleme talebi gönderildi.

### 🧪 Derleme & GitHub Durumu
- **`:app:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- **`:shared:assembleDebug`** ➔ **`BUILD SUCCESSFUL`** (0 Hata) ✅
- Tüm değişiklikler GitHub `origin/main` deposuna başarıyla push edilmiştir (`working tree clean`).

---

## 32. MacInCloud Aylık Plan & Kimlik / Hesap Bilgileri (v185 Güncellemeleri)

### 🖥️ MacInCloud Sunucu & Üyelik Bilgileri (YENİ AYLIK PLAN)
- **E-posta (Portal Üyelik):** `nevalcicek@hotmail.com`
- **Sunucu Adı (Hostname):** `FF775.macincloud.com`
- **Sunucu IP Adresi:** `195.82.45.175`
- **Giriş / Sunucu Kullanıcı Adı:** `user306378`
- **Mac VNC / Terminal Şifresi:** `kst56487ejt`
- **Aktif Plan:** **Aylık Plan (Managed Server Monthly - Europe Germany)** *(Yeni aylık Mac sunucusu).*

### 🍎 Apple Developer Geliştirici Hesabı
- **Apple ID (E-posta):** `morplato@gmail.com`
- **Giriş Şifresi:** `fabrikalaR3019`
- **Apple Destek İnceleme Kodu (Case ID):** `103017117489` *(Hatırlatma mesajı gönderildi, ödeme onayı bekleniyor).*








