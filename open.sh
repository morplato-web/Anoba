#!/bin/bash
echo "🧹 Eski Xcode pencereleri temizleniyor..."
killall -9 Xcode 2>/dev/null || true
sleep 1

# MacInCloud eski şablon kilitlenmelerini (ExampleLibrary, FrameworkWrapper vb.) Xcode önbelleğinden tamamen sil
rm -rf "$HOME/Library/Saved Application State/com.apple.dt.Xcode.savedState" 2>/dev/null || true
rm -rf "$HOME/Library/Autosave Information/com.apple.dt.Xcode.plist" 2>/dev/null || true
rm -rf "$HOME/Library/Caches/com.apple.dt.Xcode" 2>/dev/null || true
defaults write com.apple.dt.Xcode NSQuitAlwaysKeepsWindows -bool false 2>/dev/null || true

# İzinleri güncelle
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true

echo "🚀 Anoba iOS projesi Xcode üzerinde temizce açılıyor..."
open "$HOME/Anoba/iosApp/iosApp.xcodeproj"

