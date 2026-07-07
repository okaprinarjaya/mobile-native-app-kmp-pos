package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebViewForLoadedWebApp(
    url: String,
    modifier: Modifier,
    onUrlChanged: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // 2. 🔥 WAJIB: Paksa hardware acceleration langsung di level komponen WebView
                // Menangani bug painting pada div/article yang menggunakan CSS modern di Android
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                // Konfigurasi Web Engine Lengkap
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = true

                // Optimasi viewport tablet
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                // 🔥 2. SUNTIKKAN WEBCHROMECLIENT DI SINI
                // Ini yang bertugas menjembatani rendering frame JS & layout engine modern milik OWL Odoo 18
                webChromeClient = WebChromeClient()

                // Sinkronisasi Cookie
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                WebView.setWebContentsDebuggingEnabled(true)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let { onUrlChanged(it) }
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        url?.let { onUrlChanged(it) }
                    }
                }

                loadUrl(url)
            }
        },
        update = {
            // Tetap kosongkan untuk menghindari interupsi recompose
        },
        modifier = modifier
    )
}