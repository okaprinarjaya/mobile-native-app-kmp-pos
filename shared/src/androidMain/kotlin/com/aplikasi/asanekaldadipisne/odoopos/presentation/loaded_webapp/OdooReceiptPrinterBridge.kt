package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getSavedPrinterAddress
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class OdooReceiptPrinterBridge(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebViewBridge(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Sangat penting untuk keandalan PWA Odoo

        // Menggunakan kelas eksplisit agar binding Javascipt murni & aman
        webView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")
        Log.d("OdooPrintDebug", "-> [STARTUP] AndroidBridge berhasil didaftarkan ke WebView")
    }

    // Class Interface resmi untuk menjembatani JS -> Kotlin
    inner class WebAppInterface {
        @JavascriptInterface
        fun printReceipt(jsonString: String) {
            Log.d("OdooPrintDebug", "-> [KOTLIN] printReceipt() dipanggil oleh web.")
            executeBluetoothPrint(jsonString)
        }

        // FUNGSI BARU: Terowongan log dari JS ke Android Logcat
        @JavascriptInterface
        fun logFromWeb(level: String, message: String) {
            when (level.uppercase()) {
                "ERROR" -> Log.e("OdooPrintDebug", "[JS-ERROR] $message")
                "WARN"  -> Log.w("OdooPrintDebug", "[JS-WARN]  $message")
                else    -> Log.d("OdooPrintDebug", "[JS-INFO]  $message")
            }
        }
    }

    private fun executeBluetoothPrint(jsonString: String) {
        Log.d("OdooPrintDebug", "-> [KOTLIN] executeBluetoothPrint() dipanggil dengan jsonString: $jsonString")
        try {
            val data = JSONObject(jsonString)
            val savedMac = try { getSavedPrinterAddress() } catch (e: Exception) { "" }

            Log.d("OdooPrintDebug", "-> [HARDWARE] Mac address: $savedMac")

            var bluetoothConnection: DeviceConnection? = null
            if (!savedMac.isNullOrEmpty()) {
                val pairedList = BluetoothPrintersConnections().list
                bluetoothConnection = pairedList?.find { it.device.address == savedMac }
            }

            if (bluetoothConnection == null) {
                bluetoothConnection = BluetoothPrintersConnections.selectFirstPaired()
            }

            if (bluetoothConnection == null) {
                Log.e("OdooPrintDebug", "-> [HARDWARE] Gagal: Tidak ada printer terikat (paired)!")
                return
            }

            val printer = EscPosPrinter(bluetoothConnection, 203, 48f, 32)
            val sb = StringBuilder()
            val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }

            // 1. HEADER
            sb.append("[C]<b>Sari Kembar - Gianyar</b>\n")
            sb.append("[C]Majapahit\n")
            sb.append("[C]--------------------------------\n")
            sb.append("[L]Kasir: ${data.optString("cashier")}\n")
            sb.append("[L]Tgl  : ${data.optString("date")}\n")
            sb.append("[C]--------------------------------\n")

            // 2. ANTREAN
            val trackingNo = data.optString("trackingNumber")
            if (trackingNo.isNotEmpty()) {
                sb.append("[C]<font size='big'>$trackingNo</font>\n")
                sb.append("[C]--------------------------------\n")
            }

            // 3. LINES
            val lines = data.optJSONArray("orderlines")
            if (lines != null) {
                for (i in 0 until lines.length()) {
                    val item = lines.getJSONObject(i)
                    sb.append("[L]<b>${item.optString("productName")}</b>\n")
                    sb.append("[L]  ${item.optString("qty")} x ${item.optString("unitPrice")}[R]}${item.optString("price")}\n")
                }
            }
            sb.append("[C]--------------------------------\n")

            // 4. FOOTER
            val amountTotal = data.optDouble("amount_total", 0.0)
            val amountTax = data.optDouble("amount_tax", 0.0)
            val cashPaid = data.optDouble("cashPaid", 0.0)
            val change = data.optDouble("change", 0.0)

            sb.append("[R]Sebelum Pajak: ${idrFormatter.format(amountTotal - amountTax).replace("Rp", "Rp ")}\n")
            sb.append("[R]Pajak Barang: ${idrFormatter.format(amountTax).replace("Rp", "Rp ")}\n")
            sb.append("[C]--------------------------------\n")
            sb.append("[R]<font size='big'>TOTAL: ${idrFormatter.format(amountTotal).replace("Rp", "Rp ")}</font>\n")
            sb.append("[R]Cash: ${idrFormatter.format(cashPaid).replace("Rp", "Rp ")}\n")
            sb.append("[R]KEMBALIAN: ${idrFormatter.format(change).replace("Rp", "Rp ")}\n")
            sb.append("[C]--------------------------------\n")
            sb.append("[C]<font size='small'>Powered by Odoo</font>\n")
            sb.append("[C]<font size='small'>${data.optString("name")}</font>\n")
            sb.append("[L]\n\n\n")

            Log.d("OdooPrintDebug", "-> [HARDWARE] Mengirim teks ke printer thermal...")
            printer.printFormattedText(sb.toString())
            Log.d("OdooPrintDebug", "-> [HARDWARE] Selesai cetak!")

        } catch (e: Exception) {
            Log.e("OdooPrintDebug", "-> [CRASH] Error executeBluetoothPrint: ${e.message}", e)
        }
    }
}
