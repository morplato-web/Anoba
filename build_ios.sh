#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"
chmod +x gradlew

ARCH=$(uname -m)
if [ "$ARCH" = "x86_64" ]; then
    ./gradlew :shared:linkDebugFrameworkIosX64 --stacktrace
else
    ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
fi
