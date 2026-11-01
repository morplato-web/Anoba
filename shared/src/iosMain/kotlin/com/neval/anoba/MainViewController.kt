package com.neval.anoba

import androidx.compose.ui.window.ComposeUIViewController
import com.neval.anoba.di.appModules
import org.koin.core.context.startKoin

@Suppress("unused")
fun initKoinIos() {
    try {
        startKoin {
            modules(appModules())
        }
    } catch (_: Exception) {
        // Koin zaten başlatıldıysa yut
    }
}

@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    App()
}
