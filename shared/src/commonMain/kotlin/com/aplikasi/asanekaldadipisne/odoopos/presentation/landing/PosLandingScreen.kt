package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.aplikasi.asanekaldadipisne.odoopos.OdooTab
import com.aplikasi.asanekaldadipisne.odoopos.components.BluetoothPrinterHeader
import com.aplikasi.asanekaldadipisne.odoopos.components.OdooNavigationRail
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterSelectionDialog
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp
import kotlinx.coroutines.launch

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

    // 🎯 STATE & SCOPE UNTUK MANAGEMENT SLIDING MENU DRAWER (MATERIAL 3)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
    // 🎯 BUILT-IN MATERIAL 3 SLIDING DRAWER
    // =========================================================================
    ModalNavigationDrawer(
        drawerState = drawerState,
        // 🔥 GESTURE AKTIF HANYA JIKA KASIR SUDAH LOGIN
        gesturesEnabled = isLoggedIn,
        drawerContent = {
            // Bungkus OdooNavigationRail di dalam ModalDrawerSheet dengan lebar pas setebal rail (Compact & Elegan)
            ModalDrawerSheet(
                modifier = Modifier.width(80.dp), // Menyesuaikan dengan ukuran default Rail Anda
                drawerContainerColor = Color(0xFF1E1E2C) // Menyamakan warna background sidebar Anda
            ) {
                OdooNavigationRail(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        currentTab = selectedTab
                        // 🔄 OTOMATIS SLIDE SHUT (TUTUP PINTU) SETIAP KALI TAB DIKLIK
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // =========================================================================
        // 🔵 AREA KONTEN UTAMA: Otomatis Full Screen saat Drawer tertutup
        // =========================================================================
        Scaffold(
            modifier = modifier.fillMaxSize(),
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
                        url = "$odooUrl/odoo/point-of-sale",
                        onUrlChanged = { currentUrl ->
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
    // 🎯 2. KOMPONEN DIALOG SELEKSI PRINTER (Existing)
    // =========================================================================
    if (showPrinterDialog) {
        PrinterSelectionDialog(
            printerList = printerList,
            currentSelectedPrinter = selectedPrinter,
            onDismissRequest = { showPrinterDialog = false },
            onConfirmConnect = { printer ->
                selectedPrinter = printer
                saveSelectedPrinterAddress(printer.address)
                showPrinterDialog = false
            }
        )
    }
}