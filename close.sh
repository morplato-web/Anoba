#!/bin/bash
cd "$HOME/Anoba" || exit 1
echo "🧹 Fazla simülatörler kapatılıyor..."
xcrun simctl shutdown "iPhone 16" 2>/dev/null || true
xcrun simctl shutdown "iPhone 17" 2>/dev/null || true
echo "✅ İşlem tamamlandı!"
