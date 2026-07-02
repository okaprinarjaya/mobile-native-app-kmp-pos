package com.aplikasi.asanekaldadipisne

import android.app.Application
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}