#!/bin/bash
# Her zaman ana proje dizinine geç
cd "$HOME/Anoba" || exit 1

chmod +x "$HOME/Anoba/ios.sh" 2>/dev/null || true
chmod +x "$HOME/Anoba/Baslat.command" 2>/dev/null || true

if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

# Masaüstüne Çift Tıklanabilir Başlatıcı Kısayolu Oluştur
if [ -d "$HOME/Desktop" ]; then
    cat << 'EOF' > "$HOME/Desktop/Baslat.command"
#!/bin/bash
cd "$HOME/Anoba" || exit 1
sh ios.sh
EOF
    chmod +x "$HOME/Desktop/Baslat.command" 2>/dev/null || true
    chmod 777 "$HOME/Desktop/Baslat.command" 2>/dev/null || true
fi

echo "=========================================="
echo "🚀 ANOBA iOS TAM OTOMATİK BAŞLATICI"
echo "=========================================="

# 0. GitHub'dan en güncel kodları otomatik çek
echo "📥 1/5: Güncellemeler otomatik çekiliyor..."
git stash 2>/dev/null || true
git pull origin main

# 1. Eski önbellekleri ve DerivedData'yı TAMAMEN TEMİZLE
echo "🧹 2/5: Eski derleme önbellekleri temizleniyor..."
killall -9 Xcode 2>/dev/null || true
killall -9 Simulator 2>/dev/null || true
rm -rf "$HOME/Library/Developer/Xcode/DerivedData"
rm -rf "$HOME/Anoba/iosApp/build"
rm -rf "$HOME/Anoba/iosApp/Frameworks"
mkdir -p "$HOME/Anoba/iosApp/Frameworks"

# 2. İşlemci mimarisini tespit et (Intel x86_64 vs Apple Silicon arm64)
ARCH=$(uname -m)
echo "🖥️ İşlemci Mimarisi: $ARCH"

chmod +x gradlew
if [ "$ARCH" = "x86_64" ]; then
    echo "📦 3/5: Intel Mac için SharedKit (IosX64) derleniyor..."
    ./gradlew :shared:linkDebugFrameworkIosX64 --no-daemon
    FRAMEWORK_SRC="shared/build/bin/iosX64/debugFramework/SharedKit.framework"
else
    echo "📦 3/5: Apple Silicon Mac için SharedKit (IosSimulatorArm64) derleniyor..."
    ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon
    FRAMEWORK_SRC="shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework"
fi

if [ ! -d "$FRAMEWORK_SRC" ]; then
    echo "❌ HATA: SharedKit.framework derlenemedi ($FRAMEWORK_SRC bulunamadı)!"
    exit 1
fi

# 3. Kütüphaneyi iosApp/Frameworks dizinine kopyala
cp -R "$FRAMEWORK_SRC" iosApp/Frameworks/
echo "✅ SharedKit.framework kopyalandı."

# 4. Xcode projesini derle
echo "🔨 4/5: iOS Uygulaması derleniyor (xcodebuild)..."
cd "$HOME/Anoba/iosApp" || exit 1
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator ONLY_ACTIVE_ARCH=YES ARCHS="$ARCH" build
cd "$HOME/Anoba" || exit 1

# 5. Derlenen .app paketini bul
APP_PATH=$(find "$HOME/Library/Developer/Xcode/DerivedData" -path "*/Build/Products/Debug-iphonesimulator/*.app" | head -n 1)

if [ -z "$APP_PATH" ]; then
    echo "❌ HATA: Derlenen .app paketi bulunamadı!"
    exit 1
fi

echo "📦 Bulunan App Paketi: $APP_PATH"

# 6. Simülatörü temizle ve başlat
echo "📱 5/5: Simülatör hazırlanıyor ve uygulama yükleniyor..."
xcrun simctl shutdown all 2>/dev/null || true

SIM_ID=$(xcrun simctl list devices | grep "iPhone" | grep -v "unavailable" | head -n 1 | sed -E 's/.*\(([-A-F0-9]+)\).*/\1/')

if [ -n "$SIM_ID" ]; then
    echo "📱 Simülatör Başlatılıyor (UDID: $SIM_ID)..."
    xcrun simctl boot "$SIM_ID" 2>/dev/null || true
fi

open -a Simulator
xcrun simctl bootstatus "$SIM_ID" -b 2>/dev/null || sleep 6

# Eski uygulamayı kaldır
xcrun simctl uninstall "$SIM_ID" com.neval.anoba 2>/dev/null || true
xcrun simctl uninstall "$SIM_ID" com.neval.anoba.iosApp 2>/dev/null || true

# Uygulamayı yükle
echo "🚀 Uygulama simülatöre yükleniyor..."
xcrun simctl install "$SIM_ID" "$APP_PATH"

echo "📱 Uygulama canlı başlatılıyor (Log Akışı)..."
xcrun simctl launch --console-pty "$SIM_ID" com.neval.anoba || xcrun simctl launch --console-pty "$SIM_ID" com.neval.anoba.iosApp

echo "=========================================="
echo "🎉 İŞLEM TAMAMLANDI!"
echo "=========================================="
