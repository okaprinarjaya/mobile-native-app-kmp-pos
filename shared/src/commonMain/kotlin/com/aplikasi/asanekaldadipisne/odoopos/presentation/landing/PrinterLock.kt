package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.runtime.mutableStateOf
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterState
import kotlinx.coroutines.flow.MutableStateFlow

object PrinterLock {
    val isPrinting = MutableStateFlow(false)
    val bluetoothLock = Any()

    // 🚦 State global yang bisa dibaca UI dan ditulis oleh fungsi cetak
    val printerState = mutableStateOf(PrinterState.NOT_SET)
}