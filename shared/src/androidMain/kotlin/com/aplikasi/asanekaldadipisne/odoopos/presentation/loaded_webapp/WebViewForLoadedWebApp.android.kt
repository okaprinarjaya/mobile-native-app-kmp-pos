package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebViewForLoadedWebApp(
    url: String,
    modifier: Modifier,
    isProvidePrinterBridge: Boolean,
    onUrlChanged: (String) -> Unit,
    onPageFinished: (url: String, bridge: WebViewBridge) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.userAgentString = "${settings.userAgentString} SariKembarPOSAndroidApp"

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = true

                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        if (url != null && view != null) {
                            view.evaluateJavascript(VOICE_TO_TEXT_JS_INJECTION, null)

                            val androidBridge = object : WebViewBridge {
                                override val url: String
                                    get() = view.url ?: ""

                                override fun evaluateJavascript(
                                    script: String,
                                    onResult: ((String) -> Unit)?
                                ) {
                                    view.evaluateJavascript(script, onResult)
                                }

                                override fun syncCookies() {
                                    CookieManager.getInstance().flush()
                                }
                            }

                            onPageFinished(url, androidBridge)
                        }
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean
                    ) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        url?.let { onUrlChanged(it) }
                    }
                }

                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                if (isProvidePrinterBridge) {
                    val receiptPrinterBridge = OdooReceiptPrinterBridge(context)
                    receiptPrinterBridge.setupWebViewBridge(this, coroutineScope)
                }

                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url && !url.isEmpty()) {
                webView.loadUrl(url)
            }
        },
        onRelease = { webView ->
            webView.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
        },
        modifier = modifier
    )
}

private const val VOICE_TO_TEXT_JS_INJECTION = """
    window.onVoiceToTextResult = function(text, isSpeaking) {
        if (!text) return;
        const query = '#psvv-dagang-aplikasi-kasir-search-input';
        const inputElement = document.querySelector(query);

        if (inputElement) {
            inputElement.value = text;
            inputElement.dispatchEvent(new Event('input', { bubbles: true }));
            inputElement.dispatchEvent(new Event('change', { bubbles: true }));

            if (!isSpeaking) {
                setTimeout(() => {
                    const enterEvent = new KeyboardEvent('keydown', {
                        key: 'Enter',
                        keyCode: 13,
                        code: 'Enter',
                        which: 13,
                        bubbles: true
                    });
                    inputElement.dispatchEvent(enterEvent);
                    inputElement.blur();
                }, 100);
            }
        }
    };
"""
