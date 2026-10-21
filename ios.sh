#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Bozuk konan önbelleği kontrolü ve otomatik temizleme
if [ -d "$HOME/.konan" ]; then
    if [ ! -f "$HOME/.konan/kotlin-native-prebuilt-macos-aarch64-2.1.0/tools/env_blacklist" ]; then
        echo "Bozuk/Eksik Kotlin-Native önbelleği tespit edildi. Temizleniyor..."
        rm -rf "$HOME/.konan"
    fi
fi

chmod +x gradlew
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
