package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Composable
actual fun WebViewForLoadedWebApp(
    url: String,
    modifier: Modifier,
    onUrlChanged: (String) -> Unit = {}
) {
    UIKitView(
        factory = {
            WKWebView().apply {
                val nsUrl = NSURL(string = url)
                loadRequest(NSURLRequest(uRL = nsUrl))
            }
        },
        modifier = modifier
    )
}