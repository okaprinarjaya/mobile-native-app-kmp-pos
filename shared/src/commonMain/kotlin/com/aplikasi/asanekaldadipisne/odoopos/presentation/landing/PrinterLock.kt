package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import kotlinx.coroutines.flow.MutableStateFlow

object PrinterLock {
    val isPrinting = MutableStateFlow(false)
    val bluetoothLock = Any()
}