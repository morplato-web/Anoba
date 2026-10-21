#!/bin/bash
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif command -v /usr/libexec/java_home &> /dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Proje izinlerini tam aç
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true
chmod -R 777 . 2>/dev/null || true

# 1. Eski süreçleri kapat
chmod +x gradlew
./gradlew --stop
killall -9 Xcode 2>/dev/null || true

# 2. Önbellekleri temizle
rm -rf "$HOME/Library/Developer/Xcode/DerivedData"
rm -rf "$HOME/Library/Saved Application State/com.apple.dt.Xcode.savedState"
rm -rf "$HOME/.swiftpm/cache"
rm -rf "$HOME/.konan"
rm -rf "$HOME/.gradle/caches/modules-2/files-2.1/androidx.room"
rm -rf build shared/build app/build

# 3. SharedKit.framework derlemesini yap
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# 4. Framework kopyala
mkdir -p iosApp/Frameworks
mkdir -p shared/build/xcode-frameworks/Debug/iphonesimulator
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/Frameworks/ 2>/dev/null || true
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework iosApp/ 2>/dev/null || true
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework shared/build/xcode-frameworks/Debug/iphonesimulator/ 2>/dev/null || true

chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

# 5. Terminal üzerinden xcodebuild ile tam derleme ve teşhis yap
echo "iOS Uygulaması xcodebuild ile derleniyor..."
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build
