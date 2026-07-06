package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aplikasi.asanekaldadipisne.odoopos.components.BluetoothPrinterHeader
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterSelectionDialog
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp

@Composable
fun PosLandingScreen(
    modifier: Modifier = Modifier,
    odooUrl: String = "http://192.168.1.6:8069"
) {
    var selectedPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerList by remember { mutableStateOf<List<KmpPrinterDevice>>(emptyList()) }

    // 🔄 AUTO LOAD PREFERENCES SAAT APLIKASI PERTAMA DI BUKA
    LaunchedEffect(Unit) {
        val savedAddress = getSavedPrinterAddress()
        if (!savedAddress.isNullOrEmpty()) {
            selectedPrinter = KmpPrinterDevice(name = "Saved Printer", address = savedAddress)
        }
    }

    // 🔄 REFRESH DAFTAR PRINTER SETIAP KALI DIALOG AKAN DIBUKA
    LaunchedEffect(showPrinterDialog) {
        if (showPrinterDialog) {
            printerList = getPairedPrintersList()
        }
    }

    Scaffold(
        topBar = {
            // 🎯 1. KOMPONEN HEADER STATUS BAR
            BluetoothPrinterHeader(
                selectedPrinter = selectedPrinter,
                onSetPrinterClick = { showPrinterDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            WebViewForLoadedWebApp(
                url = odooUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // =========================================================================
    // 🎯 2. KOMPONEN DIALOG SELEKSI PRINTER (HASIL REFACTOR)
    // =========================================================================
    if (showPrinterDialog) {
        PrinterSelectionDialog(
            printerList = printerList,
            currentSelectedPrinter = selectedPrinter,
            onDismissRequest = { showPrinterDialog = false },
            onConfirmConnect = { printer ->
                selectedPrinter = printer
                saveSelectedPrinterAddress(printer.address) // Simpan ke SharedPreferences
                showPrinterDialog = false
            }
        )
    }
}