#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# 1. Arka planda çalışan eski Gradle Daemon süreçlerini tamamen kapat
chmod +x gradlew
./gradlew --stop

# 2. Bozuk .konan ve eski build klasörlerini temizle
echo "Bozuk önbellek ve eski derleme dosyaları temizleniyor..."
rm -rf "$HOME/.konan"
rm -rf build shared/build app/build

# 3. Temiz ve yeni bir süreçle (no-daemon) derlemeyi başlat
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon
