package com.aplikasi.asanekaldadipisne.odoopos.components

import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.KmpPrinterDevice

enum class PrinterConnectionType {
    NONE,
    BLUETOOTH,
    USB
}

data class SelectedPrinterConnectionTypeState(
    val connectionType: PrinterConnectionType = PrinterConnectionType.NONE,
    val bluetoothDevice: KmpPrinterDevice? = null,
    val usbDevice: KmpPrinterDevice? = null
)