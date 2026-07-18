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
import androidx.compose.runtime.derivedStateOf
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
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.components.OdooNavigationRail
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionHeader
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType
import com.aplikasi.asanekaldadipisne.odoopos.components.SelectedPrinterConnectionTypeState
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewBridge
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class SwitchPrinterConnection(
    val switchTo: PrinterConnectionType = PrinterConnectionType.NONE
)

@Composable
fun PosLandingScreen(
    modifier: Modifier = Modifier,
    odooUrl: String,
    printerController: PrinterController
) {
    var activePrinterType by remember { mutableStateOf(PrinterConnectionType.NONE) }
    var selectedBluetoothPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }
    var selectedUsbPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }
    var switchPrinterConnection by remember { mutableStateOf(SwitchPrinterConnection()) }
    var doSwitchPrinterConnection by remember { mutableStateOf(false) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var isOrdersLoaded by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(OdooTab.POS) }
    var webViewPOSUrl by remember { mutableStateOf("$odooUrl/odoo/point-of-sale") }

    var webViewOrdersBridge by remember { mutableStateOf<WebViewBridge?>(null) }
    var webViewPOSBridge by remember { mutableStateOf<WebViewBridge?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isLoadingOrders by remember {
        derivedStateOf { currentTab == OdooTab.ORDERS && isLoggedIn && !isOrdersLoaded }
    }

    val showLoadingOverlay by remember {
        derivedStateOf { isLoadingOrders || isLoggingOut }
    }

    val showMenuTrigger by remember {
        derivedStateOf { isLoggedIn && drawerState.isClosed }
    }

    LaunchedEffect(Unit) {
        val savedType = getSavedSelectedPrinterType()
        val savedAddress = getSavedPrinterAddress()
        val savedName = getSavedPrinterName()

        if (!savedAddress.isNullOrEmpty() && !savedName.isNullOrEmpty()) {
            if (savedType == "USB") {
                activePrinterType = PrinterConnectionType.USB
                selectedUsbPrinter =
                    KmpPrinterDevice(name = savedName, address = savedAddress)
            } else {
                activePrinterType = PrinterConnectionType.BLUETOOTH
                selectedBluetoothPrinter =
                    KmpPrinterDevice(name = savedName, address = savedAddress)
            }
        }
    }

    LaunchedEffect(doSwitchPrinterConnection) {
        if (doSwitchPrinterConnection) {
            activePrinterType = switchPrinterConnection.switchTo
            doSwitchPrinterConnection = false
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
                            isLoggingOut = true
                            isOrdersLoaded = false

                            val logoutUrl = "$odooUrl/web/session/logout"
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
                    PrinterConnectionHeader(
                        printerController = printerController,
                        selectedPrinterConnectionType = SelectedPrinterConnectionTypeState(
                            connectionType = activePrinterType,
                            bluetoothDevice = selectedBluetoothPrinter,
                            usbDevice = selectedUsbPrinter
                        ),
                        onBluetoothPrinterSelected = { device ->
                            selectedBluetoothPrinter = device
                            activePrinterType = PrinterConnectionType.BLUETOOTH
                            saveSelectedPrinterType(PrinterConnectionType.BLUETOOTH)
                            saveSelectedPrinterAddress(device.address)
                            saveSelectedPrinterName(device.name)
                        },
                        onUSBPrinterSelected = { device ->
                            selectedUsbPrinter = device
                            activePrinterType = PrinterConnectionType.USB
                            saveSelectedPrinterType(PrinterConnectionType.USB)
                            saveSelectedPrinterAddress(device.address)
                            saveSelectedPrinterName(device.name)
                        },
                        onDeviceUnAvailable = { action, connectionTarget, btDev, usbDev ->
                            when (Pair(action, connectionTarget)) {
                                Pair("DELETE", PrinterConnectionType.BLUETOOTH) -> {
                                    selectedBluetoothPrinter = null
                                }

                                Pair("DELETE", PrinterConnectionType.USB) -> {
                                    selectedUsbPrinter = null
                                }

                                Pair("SWITCH", PrinterConnectionType.NONE) -> {
                                    saveSelectedPrinterType(PrinterConnectionType.NONE)
                                    saveSelectedPrinterAddress("")
                                    saveSelectedPrinterName("")

                                    switchPrinterConnection = SwitchPrinterConnection(
                                        switchTo = PrinterConnectionType.NONE
                                    )
                                    doSwitchPrinterConnection = true
                                }

                                Pair("SWITCH", PrinterConnectionType.BLUETOOTH) -> {
                                    saveSelectedPrinterType(PrinterConnectionType.BLUETOOTH)
                                    saveSelectedPrinterAddress(btDev?.address ?: "")
                                    saveSelectedPrinterName(btDev?.name ?: "")

                                    switchPrinterConnection = SwitchPrinterConnection(
                                        switchTo = PrinterConnectionType.BLUETOOTH
                                    )
                                    doSwitchPrinterConnection = true
                                }

                                Pair("SWITCH", PrinterConnectionType.USB) -> {
                                    saveSelectedPrinterType(PrinterConnectionType.USB)
                                    saveSelectedPrinterAddress(usbDev?.address ?: "")
                                    saveSelectedPrinterName(usbDev?.name ?: "")

                                    switchPrinterConnection = SwitchPrinterConnection(
                                        switchTo = PrinterConnectionType.USB
                                    )
                                    doSwitchPrinterConnection = true
                                }
                            }
                        }
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
                    WebViewTab(
                        isVisible = currentTab == OdooTab.POS,
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
                                    } else {
                                        isLoggedIn = false
                                    }
                                }
                            } else if (finalUrl.contains("/web/login")) {
                                isLoggedIn = false
                                isOrdersLoaded = false
                                isLoggingOut = false

                                if (webViewOrdersBridge?.url?.isNotEmpty() == true && webViewOrdersBridge?.url == "$odooUrl/odoo/pos-orders") {
                                    webViewOrdersBridge?.syncCookies()
                                    webViewOrdersBridge?.evaluateJavascript("window.location.href = '$odooUrl/web/login';") {}
                                }
                            }
                        },
                        isProvidePrinterBridge = true
                    )

                    // WEBVIEW INSTANCE 2: Odoo Backend - Order list
                    WebViewTab(
                        isVisible = currentTab == OdooTab.ORDERS && isLoggedIn && isOrdersLoaded,
                        isTabActive = currentTab == OdooTab.ORDERS && isLoggedIn,
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
                                    webViewPOSBridge?.syncCookies()
                                    webViewPOSBridge?.evaluateJavascript("window.location.href = '$odooUrl/web/login';") {}
                                }
                            }
                        },
                        isProvidePrinterBridge = false
                    )

                    if (showLoadingOverlay) {
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
                                    text = if (isLoggingOut) "Mengeluarkan sesi" else "Memuat daftar order...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            if (showMenuTrigger) {
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
}

@Composable
private fun WebViewTab(
    isVisible: Boolean,
    isTabActive: Boolean = isVisible,
    url: String,
    onUrlChanged: (String) -> Unit,
    onPageFinished: (String, WebViewBridge) -> Unit,
    isProvidePrinterBridge: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (isVisible) 1f else 0f
                translationX = if (isTabActive) 0f else 5000f
            }
    ) {
        WebViewForLoadedWebApp(
            url = url,
            onUrlChanged = onUrlChanged,
            onPageFinished = onPageFinished,
            modifier = Modifier.fillMaxSize(),
            isProvidePrinterBridge = isProvidePrinterBridge
        )
    }
}