package com.aplikasi.asanekaldadipisne.odoopos

interface PrinterController {
    fun connectUSBPrinter(onSucess: () -> Unit, onError: () -> Unit)
}