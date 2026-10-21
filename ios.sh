#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "=========================================="
echo "🚀 ANOBA iOS TAM OTOMATİK BAŞLATICI"
echo "=========================================="

# 1. Ekranda çakışan tüm Xcode pencerelerini kapat
killall -9 Xcode 2>/dev/null || true
sleep 1

# 2. İzinleri kontrol et
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true
chmod -R 777 . 2>/dev/null || true

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
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator build

# 6. Derlenen .app paketini bul ve Simülatörde çalıştır
APP_PATH=$(find "$HOME/Library/Developer/Xcode/DerivedData" -name "iosApp.app" | head -n 1)

echo "📱 3/3: Simülatör açılıyor ve Anoba yükleniyor..."
open -a Simulator
sleep 2
xcrun simctl boot "iPhone 17 Pro" 2>/dev/null || xcrun simctl boot "iPhone 16" 2>/dev/null || true
sleep 2
if [ -n "$APP_PATH" ]; then
    xcrun simctl install booted "$APP_PATH"
    xcrun simctl launch booted com.neval.anoba.iosApp
fi

echo "=========================================="
echo "🎉 ANOBA İPHONE SİMÜLATÖRÜNDE BAŞARIYLA AÇILDI!"
echo "=========================================="
