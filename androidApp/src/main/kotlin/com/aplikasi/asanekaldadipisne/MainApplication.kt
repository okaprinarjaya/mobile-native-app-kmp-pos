package com.aplikasi.asanekaldadipisne

import android.app.Application
import android.webkit.WebView
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Global WebView Configuration
        val isDebuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        WebView.setWebContentsDebuggingEnabled(isDebuggable)
    }
}