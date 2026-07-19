package com.aplikasi.asanekaldadipisne.odoopos.presentation.settings_screen

import android.content.Context
import androidx.core.content.edit
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext

actual fun savePsovSetting(enabled: Boolean) {
    val sharedPref = appContext.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putBoolean("psov_enabled", enabled) }
}

actual fun getSavedPsovSetting(): Boolean {
    val sharedPref = appContext.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("psov_enabled", false)
}
