package com.aplikasi.asanekaldadipisne.odoopos

import androidx.compose.runtime.Stable

@Stable
interface PrinterController {
    fun connectUSBPrinter(onSucess: () -> Unit, onError: () -> Unit)
}