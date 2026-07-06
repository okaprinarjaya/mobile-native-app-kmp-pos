package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

data class KmpPrinterDevice(val name: String, val address: String)

// Kontrak fungsi yang akan diisi oleh sisi Android
expect fun getPairedPrintersList(): List<KmpPrinterDevice>
expect fun saveSelectedPrinterAddress(address: String)
expect fun getSavedPrinterAddress(): String?
