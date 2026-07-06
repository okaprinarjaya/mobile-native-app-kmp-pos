package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import java.util.UUID

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

private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@SuppressLint("MissingPermission")
actual fun checkPrinterConnection(address: String): Boolean {
    // 🚦 JIKA SEDANG MENCETAK STRUK, JANGAN GANGGU JALUR BLUETOOTH!
    if (PrinterLock.isPrinting.value) {
        return true
    }

    val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    if (adapter == null || !adapter.isEnabled) return false

    return try {
        val device = adapter.getRemoteDevice(address)

        // Cek 1: Apakah sistem Android mendeteksi socket aktif dari aplikasi kita?
        val isConnectedMethod = device.javaClass.getMethod("isConnected")
        val isSystemConnected = isConnectedMethod.invoke(device) as Boolean
        if (isSystemConnected) {
            return true
        }

        // Cek 2: Keamanan tingkat lanjut untuk aplikasi POS Mandiri
        // Selama status perangkat masih BOND_BONDED (sudah dipasangkan/paired) dan Bluetooth HP aktif,
        // kita asumsikan indikator printer di UI tetap "HIJAU" (Siap).
        if (device.bondState == android.bluetooth.BluetoothDevice.BOND_BONDED) {
            return true
        }

        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}