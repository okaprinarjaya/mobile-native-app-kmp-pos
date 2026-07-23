package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aplikasi.asanekaldadipisne.odoopos.OdooTab
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.components.OdooNavigationRail
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionHeader
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType
import com.aplikasi.asanekaldadipisne.odoopos.components.SelectedPrinterConnectionTypeState
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewBridge
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp
import com.aplikasi.asanekaldadipisne.odoopos.presentation.settings_screen.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PosLandingScreen(
    modifier: Modifier = Modifier,
    odooUrl: String,
    printerController: PrinterController,
    viewModel: PosLandingViewModel = viewModel { PosLandingViewModel(odooUrl) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var webViewOrdersBridge by remember { mutableStateOf<WebViewBridge?>(null) }
    var webViewPOSBridge by remember { mutableStateOf<WebViewBridge?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isLoadingOrders by remember {
        derivedStateOf { state.currentTab == OdooTab.ORDERS && state.isLoggedIn && !state.isOrdersLoaded }
    }

    val showLoadingOverlay by remember {
        derivedStateOf { isLoadingOrders || state.isLoggingOut }
    }

    val showMenuTrigger by remember {
        derivedStateOf { state.isLoggedIn && drawerState.isClosed }
    }

    val onBluetoothPrinterSelected = remember(viewModel) {
        { device: KmpPrinterDevice -> viewModel.onBluetoothPrinterSelected(device) }
    }
    val onUSBPrinterSelected = remember(viewModel) {
        { device: KmpPrinterDevice -> viewModel.onUSBPrinterSelected(device) }
    }
    val onDeviceUnAvailable = remember(viewModel) {
        { action: String, target: PrinterConnectionType, bt: KmpPrinterDevice?, usb: KmpPrinterDevice? ->
            viewModel.onDeviceUnAvailable(action, target, bt, usb)
        }
    }
    val onTabSelected: (OdooTab) -> Unit =
        remember(viewModel, scope, drawerState, odooUrl, webViewPOSBridge) {
            { tab: OdooTab ->
                scope.launch { drawerState.close() }

                if (tab == OdooTab.LOGOUT) {
                    viewModel.onTabSelected(OdooTab.LOGOUT)

                    val logoutUrl = "$odooUrl/web/session/logout"
                    webViewPOSBridge?.evaluateJavascript(
                        "window.location.href = '$logoutUrl';"
                    ) {}
                } else {
                    viewModel.onTabSelected(tab)
                }
            }
        }

    val drawerContent = remember(state.currentTab, onTabSelected) {
        @Composable {
            ModalDrawerSheet(
                modifier = Modifier.width(80.dp),
                drawerContainerColor = Color(0xFF1E1E2C)
            ) {
                OdooNavigationRail(
                    currentTab = state.currentTab,
                    onTabSelected = onTabSelected
                )
            }
        }
    }

    val topBar = remember(
        state.activePrinterType,
        state.selectedBluetoothPrinter,
        state.selectedUsbPrinter,
        onBluetoothPrinterSelected,
        onUSBPrinterSelected,
        onDeviceUnAvailable
    ) {
        @Composable {
            PrinterConnectionHeader(
                printerController = printerController,
                selectedPrinterConnectionType = SelectedPrinterConnectionTypeState(
                    connectionType = state.activePrinterType,
                    bluetoothDevice = state.selectedBluetoothPrinter,
                    usbDevice = state.selectedUsbPrinter
                ),
                onBluetoothPrinterSelected = onBluetoothPrinterSelected,
                onUSBPrinterSelected = onUSBPrinterSelected,
                onDeviceUnAvailable = onDeviceUnAvailable
            )
        }
    }

    LaunchedEffect(state.currentTab) {
        if (state.currentTab == OdooTab.ORDERS && state.isLoggedIn) {
            viewModel.onOrdersLoadedChanged(false)
            webViewOrdersBridge?.syncCookies()
            webViewOrdersBridge?.evaluateJavascript(
                "window.location.href = '$odooUrl/odoo/pos-orders';"
            ) {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = drawerContent
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = topBar
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Black)
                ) {
                    // WEBVIEW INSTANCE 1: Kasir POS Utama
                    WebViewTab(
                        isVisible = state.currentTab == OdooTab.POS,
                        url = state.webViewPOSUrl,
                        onUrlChanged = { currentUrl ->
                            viewModel.onPOSUrlChanged(currentUrl)
                            if (currentUrl.contains("/web/login")) {
                                viewModel.onLoginStatusChanged(false)
                                viewModel.onTabSelected(OdooTab.POS)
                            }
                        },
                        onPageFinished = { finalUrl, webView ->
                            webViewPOSBridge = webView
                            viewModel.onPOSUrlChanged(finalUrl)

                            if (
                                (finalUrl.contains("/odoo") || finalUrl.contains("/odoo/point-of-sale")) &&
                                !finalUrl.contains("/web/login")
                            ) {
                                webView.evaluateJavascript("(function() { return document.querySelector('.oe_login_form') === null; })();") { result ->
                                    val isLoginFormAbsent = result.toBoolean()
                                    if (isLoginFormAbsent) {
                                        webView.syncCookies()
                                        viewModel.onLoginStatusChanged(true)
                                    } else {
                                        viewModel.onLoginStatusChanged(false)
                                    }
                                }
                            } else if (finalUrl.contains("/web/login")) {
                                viewModel.onLoginStatusChanged(false)
                                viewModel.onOrdersLoadedChanged(false)
                                viewModel.onLoggingOutChanged(false)

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
                        isVisible = state.currentTab == OdooTab.ORDERS && state.isLoggedIn && state.isOrdersLoaded,
                        isTabActive = state.currentTab == OdooTab.ORDERS && state.isLoggedIn,
                        url = "$odooUrl/odoo/pos-orders",
                        onUrlChanged = { currentUrl ->
                            if (currentUrl.contains("/web/login") && state.currentTab == OdooTab.ORDERS && state.isLoggedIn) {
                                viewModel.onLoginStatusChanged(false)
                                viewModel.onOrdersLoadedChanged(false)
                                viewModel.onTabSelected(OdooTab.POS)
                                viewModel.onPOSUrlChanged("$odooUrl/web/login")
                            }
                        },
                        onPageFinished = { finalUrl, webView ->
                            webViewOrdersBridge = webView

                            if (finalUrl.contains("/odoo/pos-orders")) {
                                scope.launch {
                                    delay(500.milliseconds)
                                    viewModel.onOrdersLoadedChanged(true)
                                }
                            }

                            if (finalUrl.contains("/web/login") && state.isLoggedIn) {
                                viewModel.onOrdersLoadedChanged(false)
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

                    // SETTINGS SCREEN
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = if (state.currentTab == OdooTab.SETTINGS) 1f else 0f
                                translationX =
                                    if (state.currentTab == OdooTab.SETTINGS) 0f else 5000f
                            }
                    ) {
                        SettingsScreen(modifier = Modifier.fillMaxSize())
                    }

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
                                    text = if (state.isLoggingOut) "Mengeluarkan sesi" else "Memuat daftar order...",
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
            isActive = isTabActive,
            onUrlChanged = onUrlChanged,
            onPageFinished = onPageFinished,
            modifier = Modifier.fillMaxSize(),
            isProvidePrinterBridge = isProvidePrinterBridge
        )
    }
}