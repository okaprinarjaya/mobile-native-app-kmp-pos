package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType

data class KmpPrinterDevice(val name: String, val address: String)

expect fun getPairedBluetoothPrintersList(): List<KmpPrinterDevice>
expect fun getUSBPrinterList(): List<KmpPrinterDevice>

expect fun saveSelectedPrinterAddress(address: String)
expect fun getSavedPrinterAddress(): String?

expect fun saveSelectedPrinterType(type: PrinterConnectionType)
expect fun getSavedPrinterType(): String?
