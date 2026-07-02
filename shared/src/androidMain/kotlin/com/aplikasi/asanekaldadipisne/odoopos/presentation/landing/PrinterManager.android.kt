package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import androidx.core.content.edit

// Butuh context Android untuk SharedPreferences, asumsi diinisialisasi dari Android App/Activity
lateinit var appContext: Context

@SuppressLint("MissingPermission")
actual fun getPairedPrintersList(): List<KmpPrinterDevice> {

    // 2. Tambahkan defensive check: Jika berjalan di Android 12+ dan user belum mengizinkan, langsung return kosong
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val isGranted =
            appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            return emptyList()
        }
    }

    return try {
        BluetoothPrintersConnections().list?.map { connection ->
            KmpPrinterDevice(
                // Sekarang baris di bawah ini aman dari error/garis merah garis Lint
                name = connection.device.name ?: "Unknown Printer",
                address = connection.device.address
            )
        } ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

actual fun saveSelectedPrinterAddress(address: String) {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putString("selected_printer_mac", address) }
}

actual fun getSavedPrinterAddress(): String? {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_printer_mac", null)
}