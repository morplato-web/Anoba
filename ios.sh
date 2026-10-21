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

# 1. Eski Gradle Daemon süreçlerini kapat
chmod +x gradlew
./gradlew --stop

# 2. Bozuk önbellekleri temizle
rm -rf "$HOME/.konan"
rm -rf "$HOME/.gradle/caches/modules-2/files-2.1/androidx.room"
rm -rf build shared/build app/build

# 3. Framework derlemesini yap
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# 4. SharedKit.framework dosyasını kopyala ve tüm izinleri 777 yap
mkdir -p iosApp/Frameworks
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/Frameworks/ 2>/dev/null || true
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/ 2>/dev/null || true
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

echo "İzinler tamamen açıldı ve kopyalama tamamlandı!"
