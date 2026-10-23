#!/bin/bash
cd "$HOME/Anoba" || exit 1
echo "🧹 Xcode kilitlenmeleri temizleniyor..."
killall -9 Xcode 2>/dev/null || true
rm -rf "$HOME/Library/Developer/Xcode/DerivedData"
open iosApp/iosApp.xcodeproj
echo "✅ iosApp projesi Xcode ile açıldı!"
