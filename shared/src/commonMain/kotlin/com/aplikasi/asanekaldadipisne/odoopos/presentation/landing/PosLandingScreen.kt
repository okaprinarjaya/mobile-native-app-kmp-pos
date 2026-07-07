package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.graphicsLayer
import com.aplikasi.asanekaldadipisne.odoopos.OdooTab
import com.aplikasi.asanekaldadipisne.odoopos.components.BluetoothPrinterHeader
import com.aplikasi.asanekaldadipisne.odoopos.components.OdooNavigationRail
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

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(OdooTab.POS) }

    // 🔄 AUTO LOAD PREFERENCES SAAT APLIKASI PERTAMA DI BUKA (Existing)
    LaunchedEffect(Unit) {
        val savedAddress = getSavedPrinterAddress()
        if (!savedAddress.isNullOrEmpty()) {
            selectedPrinter = KmpPrinterDevice(name = "Saved Printer", address = savedAddress)
        }
    }

    // 🔄 REFRESH DAFTAR PRINTER SETIAP KALI DIALOG AKAN DIBUKA (Existing)
    LaunchedEffect(showPrinterDialog) {
        if (showPrinterDialog) {
            printerList = getPairedPrintersList()
        }
    }

    // =========================================================================
    // 🎯 LAYOUT UTAMA: ROW HORIZONTAL (Memisahkan Side Bar dan Area Kerja)
    // =========================================================================
    Row(modifier = modifier.fillMaxSize()) {

        // 🟢 KIRI: Tampilkan Side Bar Navigation Rail HANYA jika Kasir sudah Logged-In
        if (isLoggedIn) {
            OdooNavigationRail(
                currentTab = currentTab,
                onTabSelected = { selectedTab -> currentTab = selectedTab }
            )
        }

        // 🔵 KANAN: Area Kerja Utama menggunakan Scaffold bawaan Anda
        Scaffold(
            modifier = Modifier.weight(1f), // Mengisi sisa ruang di kanan sidebar
            topBar = {
                // 🎯 1. KOMPONEN HEADER STATUS BAR (Existing)
                BluetoothPrinterHeader(
                    selectedPrinter = selectedPrinter,
                    onSetPrinterClick = { showPrinterDialog = true }
                )
            }
        ) { innerPadding ->
            var hasOpenedOrders by remember { mutableStateOf(false) }
            if (currentTab == OdooTab.ORDERS) {
                hasOpenedOrders = true
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {
                // -----------------------------------------------------------------
                // WEBVIEW INSTANCE 1: Kasir POS Utama
                // -----------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (currentTab == OdooTab.POS) 1f else 0f
                            translationX = if (currentTab == OdooTab.POS) 0f else 5000f
                        }
                ) {
                    WebViewForLoadedWebApp(
                        // Langsung tembak ke path Kasir POS Utama
                        url = "$odooUrl/odoo/point-of-sale",
                        onUrlChanged = { currentUrl ->
                            // Deteksi jika sudah berhasil masuk area POS utama
                            if (currentUrl.contains("/odoo/point-of-sale") && !currentUrl.contains("/web/login")) {
                                isLoggedIn = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // -----------------------------------------------------------------
                // WEBVIEW INSTANCE 2: Halaman Backend Orders Odoo (Lazy Loaded)
                // -----------------------------------------------------------------
                // WebView ini hanya akan diciptakan JIKA kasir sudah login DAN sudah pernah membuka tab Orders sekali
                if (isLoggedIn && hasOpenedOrders) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = if (currentTab == OdooTab.ORDERS) 1f else 0f
                                translationX = if (currentTab == OdooTab.ORDERS) 0f else 5000f
                            }
                    ) {
                        WebViewForLoadedWebApp(
                            url = "$odooUrl/odoo/pos-orders",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
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