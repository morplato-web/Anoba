#!/bin/bash
# Her zaman ana proje dizinine geç
cd "$HOME/Anoba" || exit 1

if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "=========================================="
echo "🚀 ANOBA iOS TAM OTOMATİK BAŞLATICI"
echo "=========================================="

# 0. GitHub'dan en güncel kodları otomatik çek
echo "📥 Güncellemeler otomatik çekiliyor..."
git stash 2>/dev/null || true
git pull 2>/dev/null || true

# 1. Ekranda çakışan tüm Xcode pencerelerini ve eski MacInCloud şablon önbelleklerini temizle
killall -9 Xcode 2>/dev/null || true
rm -rf "$HOME/Library/Saved Application State/com.apple.dt.Xcode.savedState" 2>/dev/null || true
rm -rf "$HOME/Library/Autosave Information/com.apple.dt.Xcode.plist" 2>/dev/null || true
rm -rf "$HOME/Library/Caches/com.apple.dt.Xcode" 2>/dev/null || true
defaults write com.apple.dt.Xcode NSQuitAlwaysKeepsWindows -bool false 2>/dev/null || true
sleep 1

# 2. İzinleri kontrol et
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

# 3. SharedKit.framework kütüphanesini derle
chmod +x gradlew
echo "📦 1/3: SharedKit kütüphanesi derleniyor..."
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# 4. Kütüphaneyi kopyala
mkdir -p iosApp/Frameworks
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/Frameworks/ 2>/dev/null || true
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

# 5. Xcode GUI olmadan doğrudan komut satırından iOS uygulamasını derle
echo "🔨 2/3: iOS Uygulaması derleniyor (xcodebuild)..."
cd "$HOME/Anoba/iosApp" || exit 1
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator build
cd "$HOME/Anoba" || exit 1

# 6. Derlenen .app paketini bul ve TEK BİR Simülatörde çalıştır
APP_PATH=$(find "$HOME/Library/Developer/Xcode/DerivedData" -name "iosApp.app" 2>/dev/null | head -n 1)

echo "📱 3/3: Tüm ekstra simülatörler kapatılıyor, tek bir temiz iPhone açılıyor..."
killall -9 Simulator 2>/dev/null || true
xcrun simctl shutdown all 2>/dev/null || true
sleep 1

# Sadece 1 adet varsayılan iPhone simülatörünü seç ve başlat
SIM_ID=$(xcrun simctl list devices | grep "iPhone 16" | grep -v "unavailable" | head -n 1 | sed -E 's/.*\(([-A-F0-9]+)\).*/\1/')
if [ -z "$SIM_ID" ]; then
    SIM_ID=$(xcrun simctl list devices | grep "iPhone" | grep -v "unavailable" | head -n 1 | sed -E 's/.*\(([-A-F0-9]+)\).*/\1/')
fi

if [ -n "$SIM_ID" ]; then
    echo "📱 Başlatılan Tek Simülatör UDID: $SIM_ID"
    xcrun simctl boot "$SIM_ID" 2>/dev/null || true
    sleep 2
fi

open -a Simulator
sleep 2

if [ -n "$APP_PATH" ]; then
    echo "📦 Yüklenen App Yolu: $APP_PATH"
    xcrun simctl install booted "$APP_PATH" 2>/dev/null || xcrun simctl install "$SIM_ID" "$APP_PATH" 2>/dev/null || true
    xcrun simctl launch booted com.neval.anoba.iosApp 2>/dev/null || xcrun simctl launch "$SIM_ID" com.neval.anoba.iosApp 2>/dev/null || true
fi

echo "=========================================="
echo "🎉 ANOBA İPHONE SİMÜLATÖRÜNDE BAŞARIYLA AÇILDI!"
echo "=========================================="
