#!/bin/bash
killall -9 Xcode 2>/dev/null || true
sleep 1
chmod -R 777 "$HOME/Anoba" 2>/dev/null || true
echo "Anoba iOS projesi Xcode üzerinde açılıyor..."
open "$HOME/Anoba/iosApp/iosApp.xcodeproj"
