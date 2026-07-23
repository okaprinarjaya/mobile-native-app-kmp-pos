package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.lifecycle.ViewModel
import com.aplikasi.asanekaldadipisne.odoopos.OdooTab
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterConnectionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PosLandingState(
    val activePrinterType: PrinterConnectionType = PrinterConnectionType.NONE,
    val selectedBluetoothPrinter: KmpPrinterDevice? = null,
    val selectedUsbPrinter: KmpPrinterDevice? = null,
    val isLoggedIn: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isOrdersLoaded: Boolean = false,
    val currentTab: OdooTab = OdooTab.POS,
    val webViewPOSUrl: String = ""
)

class PosLandingViewModel(private val odooUrl: String) : ViewModel() {

    private val _state =
        MutableStateFlow(PosLandingState(webViewPOSUrl = "$odooUrl/odoo/point-of-sale"))
    val state: StateFlow<PosLandingState> = _state.asStateFlow()

    init {
        loadPrinterSettings()
    }

    private fun loadPrinterSettings() {
        val savedType = getSavedSelectedPrinterType()
        val savedAddress = getSavedPrinterAddress()
        val savedName = getSavedPrinterName()

        if (!savedAddress.isNullOrEmpty() && !savedName.isNullOrEmpty()) {
            _state.update {
                if (savedType == "USB") {
                    it.copy(
                        activePrinterType = PrinterConnectionType.USB,
                        selectedUsbPrinter = KmpPrinterDevice(
                            name = savedName,
                            address = savedAddress
                        )
                    )
                } else {
                    it.copy(
                        activePrinterType = PrinterConnectionType.BLUETOOTH,
                        selectedBluetoothPrinter = KmpPrinterDevice(
                            name = savedName,
                            address = savedAddress
                        )
                    )
                }
            }
        }
    }

    fun onTabSelected(tab: OdooTab) {
        if (tab == OdooTab.LOGOUT) {
            _state.update { it.copy(isLoggingOut = true, isOrdersLoaded = false) }
        } else {
            _state.update { it.copy(currentTab = tab) }
        }
    }

    fun onLoginStatusChanged(isLoggedIn: Boolean) {
        _state.update { it.copy(isLoggedIn = isLoggedIn) }
    }

    fun onOrdersLoadedChanged(isLoaded: Boolean) {
        _state.update { it.copy(isOrdersLoaded = isLoaded) }
    }

    fun onPOSUrlChanged(url: String) {
        _state.update { it.copy(webViewPOSUrl = url) }
    }

    fun onLoggingOutChanged(isLoggingOut: Boolean) {
        _state.update { it.copy(isLoggingOut = isLoggingOut) }
    }

    fun onBluetoothPrinterSelected(device: KmpPrinterDevice) {
        saveSelectedPrinterType(PrinterConnectionType.BLUETOOTH)
        saveSelectedPrinterAddress(device.address)
        saveSelectedPrinterName(device.name)
        _state.update {
            it.copy(
                activePrinterType = PrinterConnectionType.BLUETOOTH,
                selectedBluetoothPrinter = device
            )
        }
    }

    fun onUSBPrinterSelected(device: KmpPrinterDevice) {
        saveSelectedPrinterType(PrinterConnectionType.USB)
        saveSelectedPrinterAddress(device.address)
        saveSelectedPrinterName(device.name)
        _state.update {
            it.copy(
                activePrinterType = PrinterConnectionType.USB,
                selectedUsbPrinter = device
            )
        }
    }

    fun onDeviceUnAvailable(
        action: String,
        connectionTarget: PrinterConnectionType,
        btDev: KmpPrinterDevice?,
        usbDev: KmpPrinterDevice?
    ) {
        when (Pair(action, connectionTarget)) {
            Pair("DELETE", PrinterConnectionType.BLUETOOTH) -> {
                _state.update { it.copy(selectedBluetoothPrinter = null) }
            }

            Pair("DELETE", PrinterConnectionType.USB) -> {
                _state.update { it.copy(selectedUsbPrinter = null) }
            }

            Pair("SWITCH", PrinterConnectionType.NONE) -> {
                saveSelectedPrinterType(PrinterConnectionType.NONE)
                saveSelectedPrinterAddress("")
                saveSelectedPrinterName("")
                _state.update { it.copy(activePrinterType = PrinterConnectionType.NONE) }
            }

            Pair("SWITCH", PrinterConnectionType.BLUETOOTH) -> {
                saveSelectedPrinterType(PrinterConnectionType.BLUETOOTH)
                saveSelectedPrinterAddress(btDev?.address ?: "")
                saveSelectedPrinterName(btDev?.name ?: "")
                _state.update { it.copy(activePrinterType = PrinterConnectionType.BLUETOOTH) }
            }

            Pair("SWITCH", PrinterConnectionType.USB) -> {
                saveSelectedPrinterType(PrinterConnectionType.USB)
                saveSelectedPrinterAddress(usbDev?.address ?: "")
                saveSelectedPrinterName(usbDev?.name ?: "")
                _state.update { it.copy(activePrinterType = PrinterConnectionType.USB) }
            }
        }
    }
}
