#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Bozuk Kotlin-Native önbelleğini kesin olarak temizle
echo "Kotlin-Native önbelleği saptanıyor ve sıfırlanıyor..."
rm -rf "$HOME/.konan"

chmod +x gradlew
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
