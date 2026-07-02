package com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.graphics.createBitmap
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getSavedPrinterAddress

class HiddenWebViewForRenderReceipt(private val context: Context) {
    @SuppressLint("SetJavaScriptEnabled")
    fun processAndPrint(htmlContent: String) {
        // Eksekusi wajib di Main Thread untuk urusan WebView
        Handler(Looper.getMainLooper()).post {
            val webView = WebView(context)

            // KUNCI UTAMA 1: Paksa WebView menggunakan Software Layer
            // Tanpa ini, webView.draw(canvas) pada komponen tersembunyi PASTI KOSONG/PUTIH!
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            val styledHtml = """
                <html>
                <head>
                    <style>
                        body { 
                            width: 384px; 
                            margin: 0; 
                            padding: 0; 
                            background-color: white;
                        }
                        .pos-receipt { 
                            width: 100% !important; 
                            box-shadow: none !important; 
                            padding: 0 !important;
                        }
                    </style>
                </head>
                <body>
                    $htmlContent
                </body>
                </html>
            """.trimIndent()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Beri jeda 500ms agar engine webview selesai merender text & layout CSS
                    webView.postDelayed({

                        // KUNCI UTAMA 2: Jalankan Measure & Layout manual karena WebView ini tidak menempel di UI
                        // Lebar dikunci kaku 384px (lebar kertas 58mm), tinggi fleksibel mengikuti konten
                        webView.measure(
                            View.MeasureSpec.makeMeasureSpec(384, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

                        val width = webView.measuredWidth
                        val height = webView.measuredHeight

                        if (width <= 0 || height <= 0) return@postDelayed

                        // Buat canvas bitmap dengan ukuran presisi hasil ukur manual di atas
                        val bitmap = createBitmap(width, height)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)

                        // KUNCI UTAMA 3: Lempar bitmap yang sudah terisi konten ini ke fungsi printer Bluetooth Anda
                        sendBitmapToBluetoothPrinter(bitmap)

                    }, 500)
                }
            }

            // KUNCI UTAMA 4: Masukkan IP Server Odoo Anda sebagai Base URL (Bukan null)
            // Agar relative path link gambar logo perusahaan bisa terunduh sempurna oleh WebView
            val odooBaseUrl = "http://100.107.185.4:8069"
            webView.loadDataWithBaseURL(odooBaseUrl, styledHtml, "text/html", "utf-8", null)
        }
    }

    private fun sendBitmapToBluetoothPrinter(bitmap: Bitmap) {
        Thread {
            try {
                val macAddress = getSavedPrinterAddress() ?: return@Thread

                // 1. Ambil objek koneksi Bluetooth
                val connection =
                    com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections()
                        .list?.firstOrNull {
                            it.device.address.equals(
                                macAddress,
                                ignoreCase = true
                            )
                        }

                if (connection != null) {
                    // 2. Bungkus koneksi ke dalam printer layout engine
                    val printer = com.dantsu.escposprinter.EscPosPrinter(connection, 203, 48f, 32)

                    val hexImage =
                        com.dantsu.escposprinter.textparser.PrinterTextParserImg.bitmapToHexadecimalString(
                            printer,
                            bitmap
                        )

                    printer.printFormattedTextAndCut("[C]<img>$hexImage</img>\n\n\n")

                    // KOREKSI TEGAS: Panggil disconnect() pada objek CONNECTION, bukan printer
                    connection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}