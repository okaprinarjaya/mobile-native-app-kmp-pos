package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.aplikasi.asanekaldadipisne.odoopos.components.PrinterState
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.PrinterLock
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getSavedPrinterAddress
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class OdooReceiptPrinterBridge(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebViewBridge(webView: WebView) {
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
                "WARN" -> Log.w("OdooPrintDebug", "[JS-WARN]  $message")
                else -> Log.d("OdooPrintDebug", "[JS-INFO]  $message")
            }
        }
    }

    private fun executeBluetoothPrint(jsonString: String) {
        // 1. Pindahkan log entri ke paling atas agar pelacakan debug mutlak akurat
        Log.d(
            "OdooPrintDebug",
            "-> [KOTLIN] executeBluetoothPrint() dipanggil dengan jsonString: $jsonString"
        )

        PrinterLock.isPrinting.value = true

        val printerConnection = getSavedPrinterConnection()
        if (printerConnection == null) {
            PrinterLock.isPrinting.value = false
            return
        }

        // 2. 🔥 OPTIMISASI UTAMA: Siapkan teks struk sebelum menyentuh hardware & antrean gembok.
        // Operasi parsing JSON & manipulasi string cukup dilakukan SEKALI saja.
        val receiptContent = try {
            constructReceiptContent(jsonString)
        } catch (e: Exception) {
            Log.e(
                "OdooPrintDebug",
                "-> [ERR] Gagal menyusun konten struk (JSON Error): ${e.message}",
                e
            )
            PrinterLock.isPrinting.value = false
            return
        }

        var isPrintSuccess = false
        var attempts = 0
        val maxAttempts = 3

        try {
            synchronized(PrinterLock.bluetoothLock) {
                Log.d("OdooPrintDebug", "-> [HARDWARE] Mengunci token Bluetooth, siap mencetak...")

                while (attempts < maxAttempts && !isPrintSuccess) {
                    attempts++

                    try {
                        Log.d(
                            "OdooPrintDebug",
                            "-> [HARDWARE] Mencoba koneksi ke printer (Percobaan $attempts/$maxAttempts)..."
                        )

                        // Menginisialisasi koneksi socket hardware
                        val printer = EscPosPrinter(printerConnection, 203, 48f, 32)

                        Log.d("OdooPrintDebug", "-> [HARDWARE] Mengirim teks ke printer thermal...")

                        // Tips: Ubah ke printFormattedTextAndCut() jika ingin auto-cut aktif
                        printer.printFormattedText(receiptContent)

                        isPrintSuccess = true

                        // 🔥 UPDATE UI SECARA INSTAN: Cetak sukses berarti printer 100% HIJAU!
                        PrinterLock.printerState.value = PrinterState.CONNECTED

                        Log.d(
                            "OdooPrintDebug",
                            "-> [HARDWARE] Selesai cetak sukses pada percobaan ke-$attempts!"
                        )
                    } catch (connectionException: EscPosConnectionException) {
                        Log.w(
                            "OdooPrintDebug",
                            "-> [WARN] Percobaan ke-$attempts gagal karena printer sibuk: ${connectionException.message}"
                        )

                        if (attempts < maxAttempts) {
                            Log.d(
                                "OdooPrintDebug",
                                "-> [HARDWARE] Bluetooth sibuk. Tidur 500ms sebelum mencoba kembali..."
                            )
                            android.os.SystemClock.sleep(500) // Istirahat sejenak demi kestabilan stack Bluetooth OS
                        } else {
                            throw connectionException
                        }
                    }
                }

                Log.d("OdooPrintDebug", "-> [HARDWARE] Selesai cetak, melepas gembok...")
            }
        } catch (e: Exception) {
            Log.e(
                "OdooPrintDebug",
                "-> [CRASH] Gagal total setelah $maxAttempts percobaan. Error: ${e.message}",
                e
            )

            // 🔥 UPDATE UI SECARA INSTAN: Jika benar-benar gagal setelah 3 kali coba, set KUNING
            PrinterLock.printerState.value = PrinterState.OFFLINE
        } finally {
            PrinterLock.isPrinting.value = false
        }
    }

    private fun constructReceiptContent(jsonString: String): String {
        val data = JSONObject(jsonString)
        val sb = StringBuilder()

        // Formatter internal untuk angka desimal murni (Top-level Totals)
        val idrFormatter =
            NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }
        val formatIdr =
            { value: Double -> idrFormatter.format(value).replace("Rp", "Rp ") }

        // ==========================================
        // 1. HEADER LAYOUT
        // ==========================================
        sb.append("[C]<b>Sari Kembar - Gianyar</b>\n")
        sb.append("[C]Majapahit\n")
        sb.append("[C]--------------------------------\n")

        val cashier = data.optString("cashier")
        if (cashier.isNotEmpty()) {
            sb.append("[L]Served by $cashier\n")
        }
        sb.append("[L]Tgl: ${data.optString("date")}\n")
        sb.append("[C]--------------------------------\n")

        // ==========================================
        // 2. INTENTIONAL TRACKING NUMBER (ANTREAN)
        // ==========================================
        val headerData = data.optJSONObject("headerData")
        val trackingNo = headerData?.optString("trackingNumber") ?: ""
        if (trackingNo.isNotEmpty()) {
            sb.append("[C]<font size='big'><b>$trackingNo</b></font>\n")
            sb.append("[C]--------------------------------\n")
        }

        // ==========================================
        // 3. DYNAMIC ORDER LINES (DAFTAR ITEM)
        // ==========================================
        val lines = data.optJSONArray("orderlines")
        if (lines != null) {
            for (i in 0 until lines.length()) {
                val item = lines.getJSONObject(i)

                val productName = item.optString("productName", "Item")
                val rawQty =
                    item.optString(
                        "qty",
                        "1"
                    ) // Mengambil string mentah (e.g., "1.000")
                val unit = item.optString("unit", "")
                val price = item.optString("price", "")
                val discount = item.optString("discount", "")
                val customerNote = item.optString("customerNote", "")

                val qty = cleanOdooQty(rawQty)

                sb.append("[L]<b>$productName</b>\n")

                val qtyLabel = if (unit.isNotEmpty()) "$qty $unit" else qty
                sb.append("[L]  $qtyLabel[R]$price\n")

                if (discount.isNotEmpty() && discount != "0" && discount != "0.0") {
                    sb.append("[L]  <font size='small'>* Disc: $discount</font>\n")
                }

                if (customerNote.isNotEmpty()) {
                    sb.append("[L]  <font size='small'>* Note: $customerNote</font>\n")
                }
            }
        }
        sb.append("[C]--------------------------------\n")

        // ==========================================
        // 4. DYNAMIC TOTALS & TAXATION BLOCK
        // ==========================================
        val amountTotal = data.optDouble("amount_total", 0.0)
        val amountTax = data.optDouble("amount_tax", 0.0)
        val totalWithoutTax =
            data.optDouble("total_without_tax", amountTotal - amountTax)
        val totalDiscount = data.optDouble("total_discount", 0.0)

        if (amountTax > 0.0) {
            sb.append("[R]Untaxed Amount: ${formatIdr(totalWithoutTax)}\n")
            sb.append("[R]Non-luxury Good Taxes: ${formatIdr(amountTax)}\n")
            sb.append("[C]--------------------------------\n")
        }

        if (totalDiscount > 0.0) {
            val labelDiscount = data.optString("label_discounts", "Discounts")
            sb.append("[R]$labelDiscount: -${formatIdr(totalDiscount)}\n")
        }

        val labelTotal = data.optString("label_total", "TOTAL")
        sb.append("[R]<font size='big'><b>$labelTotal: ${formatIdr(amountTotal)}</b></font>\n")

        // ==========================================
        // 5. DYNAMIC PAYMENT LINES & CHANGE
        // ==========================================
        val paymentLines = data.optJSONArray("paymentlines")
        if (paymentLines != null && paymentLines.length() > 0) {
            for (k in 0 until paymentLines.length()) {
                val payment = paymentLines.getJSONObject(k)
                val payName = payment.optString("name", "Paid")
                val payAmount = payment.optDouble("amount", 0.0)
                sb.append("[R]$payName: ${formatIdr(payAmount)}\n")
            }
        }

        val showRounding = data.optBoolean("show_rounding", false)
        val orderRounding = data.optDouble("order_rounding", 0.0)
        if (showRounding && orderRounding != 0.0) {
            val labelRounding = data.optString("label_rounding", "Rounding")
            sb.append("[R]$labelRounding: ${formatIdr(orderRounding)}\n")
        }

        val showChange = data.optBoolean("show_change", true)
        val change = data.optDouble("change", 0.0)
        if (showChange && change > 0.0) {
            val labelChange = data.optString("label_change", "CHANGE")
            sb.append("[R]<b>$labelChange: ${formatIdr(change)}</b>\n")
        }

        // ==========================================
        // 6. FOOTER NOTES & METADATA
        // ==========================================
        val generalNote = data.optString("generalNote")
        if (generalNote.isNotEmpty()) {
            sb.append("[C]--------------------------------\n")
            sb.append("[L]Note: $generalNote\n")
        }

        sb.append("[C]--------------------------------\n")
        sb.append("[C]<font size='small'>Powered by Odoo</font>\n")
        sb.append("[C]<font size='small'>${data.optString("name")}</font>\n")
        sb.append("[L]\n\n\n") // Spasi kosong umpan kertas (feed paper)

        return sb.toString()
    }

    private fun getSavedPrinterConnection(): DeviceConnection? {
        // 1. Ambil MAC Address dengan aman dari SharedPreferences
        val savedMac = try {
            getSavedPrinterAddress()
        } catch (e: Exception) {
            Log.e("OdooPrintDebug", "-> [ERR] Gagal membaca SharedPreferences: ${e.message}")
            return null // ✅ Berikan return null agar tidak compile-error
        }

        Log.d("OdooPrintDebug", "-> [HARDWARE] Mac address terpilih: $savedMac")

        if (savedMac.isNullOrEmpty()) {
            Log.e(
                "OdooPrintDebug",
                "-> [HARDWARE] Gagal: Mac address belum di-set di pengaturan aplikasi!"
            )
            return null // ✅ Aman dari compile-error
        }

        // 2. Ambil daftar paired devices dari OS Android
        return try {
            val pairedList = BluetoothPrintersConnections().list
            val connection = pairedList?.find { it.device.address == savedMac }

            if (connection == null) {
                Log.e(
                    "OdooPrintDebug",
                    "-> [HARDWARE] Gagal: MAC [$savedMac] tidak ditemukan di daftar Paired Devices!"
                )
            }

            connection // ✅ Mengembalikan DeviceConnection atau null jika tidak ketemu
        } catch (e: SecurityException) {
            Log.e(
                "OdooPrintDebug",
                "-> [CRASH] Permission BLUETOOTH_CONNECT tidak diizinkan oleh pengguna!",
                e
            )
            null
        } catch (e: Exception) {
            Log.e(
                "OdooPrintDebug",
                "-> [CRASH] Gagal memuat daftar bluetooth terikat: ${e.message}",
                e
            )
            null
        }
    }

    private fun cleanOdooQty(rawQty: String): String {
        if (rawQty.endsWith(".000") || rawQty.endsWith(",000")) {
            return rawQty.substring(0, rawQty.length - 4)
        }

        if (rawQty.contains(".") || rawQty.contains(",")) {
            var cleaned = rawQty.replace("0+$".toRegex(), "")
            if (cleaned.endsWith(".") || cleaned.endsWith(",")) {
                cleaned = cleaned.substring(0, cleaned.length - 1)
            }

            return cleaned.replace(".", ",")
        }

        return rawQty
    }
}
