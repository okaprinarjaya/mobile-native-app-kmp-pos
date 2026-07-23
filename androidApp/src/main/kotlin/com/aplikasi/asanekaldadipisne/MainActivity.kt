package com.aplikasi.asanekaldadipisne

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext
import com.aplikasi.asanekaldadipisne.printer.AndroidPrinterController

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_USB_PERMISSION = "com.aplikasi.sarikembarpos.USB_PERMISSION"
        var instance: MainActivity? = null
            private set
    }

    private var onUsbPermissionResult: ((Boolean) -> Unit)? = null

    // 📩 Receiver untuk menangkap jawaban user dari Dialog Izin USB
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_USB_PERMISSION == intent?.action) {
                synchronized(this) {
                    val isGranted =
                        intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (isGranted) {
                        Log.d("OdooPrintDebug", "-> [USB-DIALOG] User MENGIZINKAN akses USB.")
                        Toast.makeText(
                            context,
                            "Izin printer USB berhasil disetujui!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("OdooPrintDebug", "-> [USB-DIALOG] User MENOLAK akses USB!")
                        Toast.makeText(context, "Izin printer USB ditolak.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    onUsbPermissionResult?.invoke(isGranted)
                    onUsbPermissionResult = null
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        if (!connectGranted) {
            Toast.makeText(
                this,
                "Izin Bluetooth diperlukan untuk mendeteksi printer thermal!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("WrongConstant", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        appContext = applicationContext

        // =========================================================================
        // SETUP IMMERSIVE STICKY FULLSCREEN (TOP & BOTTOM MOST)
        // =========================================================================
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            this,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasConnectPermission) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        }

        val printerController = AndroidPrinterController(this)

        setContent {
            App(odooUrl = BuildConfig.ODOO_URL, printerController = printerController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.e("OdooPrintDebug", "Gagal unregister receiver: ${e.message}")
        }
    }

    // ==========================================================
    // HANDLER TRIGGER USB SAAT TOMBOL "CONNECT" DITAP
    // ==========================================================
    fun requestUsbPermissionForDetectedPrinter(onResult: (Boolean) -> Unit) {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager

        // Cari printer USB (Class 7)
        val printerDevice = usbManager.deviceList.values.find { device ->
            isUsbPrinter(device)
        }

        if (printerDevice == null) {
            Toast.makeText(this, "Tidak ada printer USB yang terhubung!", Toast.LENGTH_SHORT).show()
            onResult(false)
            return
        }

        if (usbManager.hasPermission(printerDevice)) {
            Log.d("OdooPrintDebug", "-> [CONNECT-USB] Izin USB sudah ada.")
            onResult(true)
        } else {
            Log.d("OdooPrintDebug", "-> [CONNECT-USB] Meminta izin USB ke user...")
            onUsbPermissionResult = onResult

            val intent = Intent(ACTION_USB_PERMISSION).apply {
                setPackage(packageName)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val permissionIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
            usbManager.requestPermission(printerDevice, permissionIntent)
        }
    }

    private fun isUsbPrinter(device: UsbDevice): Boolean {
        if (device.deviceClass == UsbConstants.USB_CLASS_PRINTER) return true
        for (i in 0 until device.interfaceCount) {
            if (device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_PRINTER) {
                return true
            }
        }
        return false
    }
}
