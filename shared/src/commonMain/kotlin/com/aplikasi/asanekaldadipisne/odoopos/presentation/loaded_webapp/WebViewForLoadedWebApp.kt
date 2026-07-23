package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface WebViewBridge {
    val url: String
    fun evaluateJavascript(script: String, onResult: ((String) -> Unit)?)
    fun syncCookies()
}

@Composable
expect fun WebViewForLoadedWebApp(
    url: String,
    modifier: Modifier,
    isActive: Boolean = true,
    isProvidePrinterBridge: Boolean = true,
    onUrlChanged: (String) -> Unit = {},
    onPageFinished: (url: String, bridge: WebViewBridge) -> Unit = { _, _ -> }
)