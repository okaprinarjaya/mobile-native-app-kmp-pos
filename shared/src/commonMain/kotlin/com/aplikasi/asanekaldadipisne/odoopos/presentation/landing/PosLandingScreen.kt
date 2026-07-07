package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
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
        gesturesEnabled = isLoggedIn,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(80.dp),
                drawerContainerColor = Color(0xFF1E1E2C)
            ) {
                OdooNavigationRail(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        currentTab = selectedTab
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // =========================================================================
        // 📦 CONTAINER UTAMA: Membungkus Kerja & Gagang Pintu (Pull-Tab)
        // =========================================================================
        Box(modifier = modifier.fillMaxSize()) {

            // 🔵 AREA SCAFOLD (KONTEN UTAMA ODOO)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
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
                    // WEBVIEW INSTANCE 1: Kasir POS Utama
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
                                if (currentUrl.contains("/odoo/point-of-sale") && !currentUrl.contains(
                                        "/web/login"
                                    )
                                ) {
                                    isLoggedIn = true
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // WEBVIEW INSTANCE 2: Backend Orders Odoo
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

            // =========================================================================
            // 🔥 INDIKATOR GAGANG PINTU (PULL-TAB HANDLE) - STATIC & SLEEK
            // =========================================================================
            // Hanya muncul jika kasir sudah login DAN posisi pintu sedang tertutup (Closed)
            if (isLoggedIn && drawerState.isClosed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart) // Posisikan tepat di tengah-tengah pinggiran kiri layar
                        .width(14.dp) // Sangat tipis dan elegan agar tidak mengganggu pandangan webview
                        .height(80.dp) // Tinggi gagang yang pas untuk target sentuhan jari
                        .background(
                            color = Color(0xFF1E1E2C).copy(alpha = 0.85f), // Warna matching dengan sidebar + sedikit transparan
                            shape = RoundedCornerShape(
                                topEnd = 12.dp,
                                bottomEnd = 12.dp
                            ) // Melengkung mulus di sisi kanan
                        )
                        .clickable {
                            // 🚀 BONUS: Sekali tap di gagang ini, menu langsung meluncur terbuka!
                            scope.launch { drawerState.open() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Icon panah kecil penunjuk arah kanan bawaan material design core
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "Slide or Tap to open menu",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
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