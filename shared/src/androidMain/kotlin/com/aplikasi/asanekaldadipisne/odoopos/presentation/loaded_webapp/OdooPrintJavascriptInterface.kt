package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class OdooPrintJavascriptInterface(private val context: Context) {
    private val printer = HiddenWebViewForRenderReceipt(context)

    @JavascriptInterface
    fun sendHtmlToNativePrinter(htmlContent: String) {
        Handler(Looper.getMainLooper()).post {
            printer.processAndPrint(htmlContent)
        }
    }
}