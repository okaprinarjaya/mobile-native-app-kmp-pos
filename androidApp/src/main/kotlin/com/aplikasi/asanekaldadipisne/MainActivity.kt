package com.aplikasi.asanekaldadipisne

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getSavedPrinterType

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_USB_PERMISSION = "com.aplikasi.sarikembarpos.USB_PERMISSION"
    }

    // 📩 Receiver untuk menangkap konfirmasi izin dari dialog pop-up USB
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_USB_PERMISSION == intent?.action) {
                synchronized(this) {
                    val device: UsbDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                UsbManager.EXTRA_DEVICE,
                                UsbDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            Log.d(
                                "OdooPrintDebug",
                                "-> [MAIN-USB] User MENGIZINKAN akses USB: ${it.deviceName}"
                            )
                            Toast.makeText(
                                context,
                                "Izin printer USB berhasil disetujui!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("OdooPrintDebug", "-> [MAIN-USB] User MENOLAK izin akses USB!")
                    }
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
                "Aplikasi butuh izin Bluetooth Connect untuk mendeteksi printer thermal!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContext = applicationContext

        // 1. Register Receiver USB dengan ContextCompat agar kompatibel dari Android 8 - 14+
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            this,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // 2. Minta Izin Bluetooth (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }

        setContent {
            App(
                odooUrl = BuildConfig.ODOO_URL
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Cek dan minta izin USB saat aplikasi aktif/dibuka
        checkAndRequestUsbPermissionOnStartup()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.e("OdooPrintDebug", "Gagal unregister usbReceiver: ${e.message}")
        }
    }

    private fun checkAndRequestUsbPermissionOnStartup() {
        val savedType = try {
            getSavedPrinterType()
        } catch (_: Exception) {
            "BLUETOOTH"
        }
        if (savedType != "USB") return

        val usbManager = getSystemService(USB_SERVICE) as UsbManager

        // 1. Cari peranti USB yang merupakan PRINTER (Class 7)
        // Ini otomatis mengabaikan USB Hub, Display Adapter, Keyboard, dll.
        val printerDevice: UsbDevice? = usbManager.deviceList.values.find { device ->
            isUsbPrinter(device)
        }

        if (printerDevice != null) {
            if (!usbManager.hasPermission(printerDevice)) {
                Log.d(
                    "OdooPrintDebug",
                    "-> [STARTUP-USB] Printer terdeteksi (${printerDevice.deviceName}, VendorID=${printerDevice.vendorId}). Meminta izin..."
                )

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
            } else {
                Log.d(
                    "OdooPrintDebug",
                    "-> [STARTUP-USB] Izin USB Printer sudah aktif & siap digunakan."
                )
            }
        } else {
            Log.w("OdooPrintDebug", "-> [STARTUP-USB] Tidak ada printer USB terhubung.")
        }
    }

    // Mengecek apakah device/interface memiliki USB Class 7 (Printer)
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
