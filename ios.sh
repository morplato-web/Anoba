#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Projedeki tüm dosyaların yazma/düzenleme izinlerini tam aç
echo "Proje yazma izinleri açılıyor (Unlock permissions)..."
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true
chmod -R 777 . 2>/dev/null || true

# 1. Eski Gradle Daemon süreçlerini ve Xcode önbelleklerini temizle
chmod +x gradlew
./gradlew --stop
killall Xcode 2>/dev/null || true

echo "Xcode türev verileri ve eski önbellekler temizleniyor..."
rm -rf "$HOME/Library/Developer/Xcode/DerivedData"
rm -rf "$HOME/Library/Saved Application State/com.apple.dt.Xcode.savedState"
rm -rf "$HOME/.konan"
rm -rf "$HOME/.gradle/caches/modules-2/files-2.1/androidx.room"
rm -rf build shared/build app/build

# 2. Framework derlemesini yap
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# 3. SharedKit.framework dosyasını Xcode dizinlerine kopyala
echo "SharedKit.framework kütüphanesi tüm Xcode dizinlerine kopyalanıyor..."
mkdir -p iosApp/Frameworks
mkdir -p shared/build/xcode-frameworks/Debug/iphonesimulator
mkdir -p shared/build/xcode-frameworks/Release/iphoneos

cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/Frameworks/ 2>/dev/null || true
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/ 2>/dev/null || true
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework shared/build/xcode-frameworks/Debug/iphonesimulator/ 2>/dev/null || true

chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

echo "Kopyalama ve temizlik tamamlandı!"

# 4. Gerçek Anoba projesini Xcode üzerinde temiz sıfır pencere ile aç
echo "Gerçek Anoba iOS projesi açılıyor..."
open "$HOME/Anoba/iosApp/iosApp.xcodeproj" 2>/dev/null || open "$HOME/Anoba/iosApp" 2>/dev/null || true
