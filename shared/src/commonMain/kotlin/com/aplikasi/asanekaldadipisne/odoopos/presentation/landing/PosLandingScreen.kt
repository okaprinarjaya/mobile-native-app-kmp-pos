package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewBridge
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PosLandingScreen(
    modifier: Modifier = Modifier,
    odooUrl: String = "http://192.168.1.6:8069"
) {
    var selectedPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerList by remember { mutableStateOf<List<KmpPrinterDevice>>(emptyList()) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var isOrdersLoaded by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(OdooTab.POS) }
    var webViewPOSUrl by remember { mutableStateOf("$odooUrl/odoo/point-of-sale") }

    var webViewOrdersBridge by remember { mutableStateOf<WebViewBridge?>(null) }
    var webViewPOSBridge by remember { mutableStateOf<WebViewBridge?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedAddress = getSavedPrinterAddress()
        if (!savedAddress.isNullOrEmpty()) {
            selectedPrinter = KmpPrinterDevice(name = "Saved Printer", address = savedAddress)
        }
    }

    LaunchedEffect(showPrinterDialog) {
        if (showPrinterDialog) {
            printerList = getPairedPrintersList()
        }
    }

    LaunchedEffect(currentTab) {
        if (currentTab == OdooTab.ORDERS && isLoggedIn) {
            isOrdersLoaded = false
            webViewOrdersBridge?.syncCookies()
            webViewOrdersBridge?.evaluateJavascript(
                "window.location.href = '$odooUrl/odoo/pos-orders';"
            ) {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(80.dp),
                drawerContainerColor = Color(0xFF1E1E2C)
            ) {
                OdooNavigationRail(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        scope.launch { drawerState.close() }

                        if (selectedTab == OdooTab.LOGOUT) {
                            val logoutUrl = "$odooUrl/web/session/logout"

                            isOrdersLoaded = false
                            webViewPOSBridge?.evaluateJavascript(
                                "window.location.href = '$logoutUrl';"
                            ) {}
                        } else {
                            currentTab = selectedTab
                        }
                    }
                )
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
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
                            url = webViewPOSUrl,
                            onUrlChanged = { currentUrl ->
                                webViewPOSUrl = currentUrl
                                if (currentUrl.contains("/web/login")) {
                                    isLoggedIn = false
                                    currentTab = OdooTab.POS
                                }
                            },
                            onPageFinished = { finalUrl, webView ->
                                webViewPOSBridge = webView
                                webViewPOSUrl = finalUrl

                                if (
                                    (finalUrl.contains("/odoo") || finalUrl.contains("/odoo/point-of-sale")) &&
                                    !finalUrl.contains("/web/login")
                                ) {
                                    webView.evaluateJavascript("(function() { return document.querySelector('.oe_login_form') === null; })();") { result ->
                                        val isLoginFormAbsent = result.toBoolean()
                                        if (isLoginFormAbsent) {
                                            webView.syncCookies()
                                            isLoggedIn = true
                                        }
                                    }
                                } else if (finalUrl.contains("/web/login")) {
                                    isLoggedIn = false
                                    if (webViewOrdersBridge?.url?.isNotEmpty() == true && webViewOrdersBridge?.url == "$odooUrl/odoo/pos-orders") {
                                        webViewOrdersBridge?.evaluateJavascript("window.location.href = '$odooUrl/web/login';") {}
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            isProvidePrinterBridge = true
                        )
                    }

                    // WEBVIEW INSTANCE 2: Odoo Backend - Order list
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha =
                                    if (currentTab == OdooTab.ORDERS && isLoggedIn && isOrdersLoaded) 1f else 0f
                                translationX =
                                    if (currentTab == OdooTab.ORDERS && isLoggedIn) 0f else 5000f
                            }
                    ) {
                        WebViewForLoadedWebApp(
                            url = "$odooUrl/odoo/pos-orders",
                            onUrlChanged = { currentUrl ->
                                if (currentUrl.contains("/web/login") && currentTab == OdooTab.ORDERS && isLoggedIn) {
                                    isLoggedIn = false
                                    isOrdersLoaded = false
                                    currentTab = OdooTab.POS
                                    webViewPOSUrl = "$odooUrl/web/login"
                                }
                            },
                            onPageFinished = { finalUrl, webView ->
                                webViewOrdersBridge = webView

                                if (finalUrl.contains("/odoo/pos-orders")) {
                                    scope.launch {
                                        delay(500.milliseconds)
                                        isOrdersLoaded = true
                                    }
                                }

                                if (finalUrl.contains("/web/login") && isLoggedIn) {
                                    isOrdersLoaded = false
                                    webView.syncCookies()
                                    webView.evaluateJavascript(
                                        "window.location.href = '$odooUrl/odoo/pos-orders';"
                                    ) {}

                                    if (webViewPOSBridge?.url?.isNotEmpty() == true && webViewPOSBridge?.url == "$odooUrl/odoo/point-of-sale") {
                                        webViewPOSBridge?.evaluateJavascript("window.location.href = '$odooUrl/web/login';") {}
                                    }
                                }
                            },
                            isProvidePrinterBridge = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // ⏳ LOADING OVERLAY ELEGAN (Muncul saat pindah ke Tab Orders tapi halaman belum selesai memuat)
                    if (currentTab == OdooTab.ORDERS && isLoggedIn && !isOrdersLoaded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Memuat Daftar Order...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
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
                        .align(Alignment.CenterStart)
                        .width(14.dp)
                        .height(80.dp)
                        .background(
                            color = Color(0xFF1E1E2C).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(
                                topEnd = 12.dp,
                                bottomEnd = 12.dp
                            )
                        )
                        .clickable {
                            scope.launch { drawerState.open() }
                        },
                    contentAlignment = Alignment.Center
                ) {
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