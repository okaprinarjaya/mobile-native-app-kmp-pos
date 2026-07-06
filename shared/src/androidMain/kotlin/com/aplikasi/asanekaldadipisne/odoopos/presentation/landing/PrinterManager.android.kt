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
    // 1. Jika aplikasi sedang dalam proses mencetak struk, JANGAN diganggu!
    // Otomatis asumsikan terhubung karena hardware sedang aktif mentransfer data.
    if (PrinterLock.isPrinting.value) return true

    val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    if (adapter == null || !adapter.isEnabled) return false

    // 2. 🔒 Gunakan gembok yang sama dengan proses cetak agar tidak terjadi tabrakan jalur data
    return synchronized(PrinterLock.bluetoothLock) {
        // Cek sekali lagi setelah berhasil mendapatkan gembok
        if (PrinterLock.isPrinting.value) return true

        try {
            val device = adapter.getRemoteDevice(address)
            val sppUuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

            // ⚡ Lakukan hardware probing singkat (Ping Fisik)
            val socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect() // Jika printer mati, baris ini akan melempar Exception setelah beberapa detik
            socket.close()   // Langsung tutup kembali agar jalur bersih

            true // Berhasil connect-close tanpa error = Printer NYALA (Hijau)
        } catch (e: Exception) {
            false // Gagal / Timeout / Printer mati = Printer OFFLINE (Kuning)
        }
    }
}