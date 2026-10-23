package com.neval.anoba

import androidx.compose.ui.window.ComposeUIViewController

@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    App()
}
