package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections

lateinit var appContext: Context

@SuppressLint("MissingPermission")
actual fun getPairedBluetoothPrintersList(): List<KmpPrinterDevice> {

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
                name = connection.device.name ?: "Unknown Bluetooth Printer",
                address = connection.device.address
            )
        } ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

actual fun getUSBPrinterList(): List<KmpPrinterDevice> {
    return try {
        val usbConnections = UsbPrintersConnections(appContext).list
        usbConnections?.map { connection ->
            val device = connection.device
            val printerName = device.productName ?: device.deviceName
            KmpPrinterDevice(
                name = printerName,
                address = device.deviceName
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

actual fun saveSelectedPrinterType(type: PrinterConnectionType) {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    sharedPref.edit { putString("selected_printer_type", type.toString()) }
}

actual fun getSavedPrinterType(): String? {
    val sharedPref = appContext.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_printer_type", null)
}
