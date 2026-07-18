package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType

lateinit var appContext: Context

@SuppressLint("MissingPermission")
actual fun getPairedBluetoothPrintersList(): List<KmpPrinterDevice> {
    // A. Cek Izin Android 12+ (API 31+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val isGranted =
            appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            Log.e(
                "OdooPrintDebug",
                "-> [BT-LIST] Izin BLUETOOTH_CONNECT belum diberikan oleh user!"
            )
            return emptyList()
        }
    }

    val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        Log.w("OdooPrintDebug", "-> [BT-LIST] Bluetooth mati atau tidak didukung pada device ini.")
        return emptyList()
    }

    return try {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices ?: emptySet()
        Log.d(
            "OdooPrintDebug",
            "-> [BT-LIST] Berhasil menemukan ${pairedDevices.size} paired bluetooth devices."
        )

        pairedDevices.map { device ->
            KmpPrinterDevice(
                name = device.name ?: "Unknown Bluetooth Printer",
                address = device.address
            )
        }
    } catch (e: Exception) {
        Log.e("OdooPrintDebug", "-> [BT-LIST] Error saat mengambil paired devices: ${e.message}")
        emptyList()
    }
}

actual fun getUSBPrinterList(): List<KmpPrinterDevice> {
    val usbManager = appContext.getSystemService(Context.USB_SERVICE) as? UsbManager
        ?: return emptyList()

    return try {
        val printerDevices = usbManager.deviceList.values.filter { device ->
            isUsbPrinterDevice(device)
        }

        Log.d(
            "OdooPrintDebug",
            "-> [USB-LIST] Berhasil menemukan ${printerDevices.size} USB Printer."
        )

        printerDevices.map { device ->
            val printerName = device.productName ?: device.deviceName

            KmpPrinterDevice(
                name = printerName,
                address = device.deviceName
            )
        }
    } catch (e: Exception) {
        Log.e("OdooPrintDebug", "-> [USB-LIST] Error scanning USB devices: ${e.message}")
        emptyList()
    }
}

// Mengecek apakah USB Device/Interface adalah Class 7 (Printer)
private fun isUsbPrinterDevice(device: UsbDevice): Boolean {
    if (device.deviceClass == UsbConstants.USB_CLASS_PRINTER) return true
    for (i in 0 until device.interfaceCount) {
        if (device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_PRINTER) {
            return true
        }
    }
    return false
}


actual fun saveSelectedPrinterAddress(address: String) {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putString("selected_printer_mac", address) }
}

actual fun getSavedPrinterAddress(): String? {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_printer_mac", null)
}

actual fun saveSelectedPrinterName(printerName: String) {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putString("selected_printer_name", printerName) }
}

actual fun getSavedPrinterName(): String? {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_printer_name", null)
}

actual fun saveSelectedPrinterType(type: PrinterConnectionType) {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putString("selected_printer_type", type.toString()) }
}

actual fun getSavedSelectedPrinterType(): String? {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_printer_type", null)
}
