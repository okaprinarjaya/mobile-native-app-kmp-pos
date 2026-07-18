package com.aplikasi.asanekaldadipisne.printer

import com.aplikasi.asanekaldadipisne.MainActivity
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.saveSelectedPrinterType

class AndroidPrinterController(
    private val activity: MainActivity
): PrinterController {
    override fun connectUSBPrinter(onSucess: () -> Unit, onError: () -> Unit) {
        activity.requestUsbPermissionForDetectedPrinter { isGranted ->
            if (isGranted) {
                saveSelectedPrinterType(PrinterConnectionType.USB)
                onSucess()
            } else {
                onError()
            }
        }
    }

}