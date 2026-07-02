package com.aplikasi.asanekaldadipisne

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.appContext

class MainActivity : ComponentActivity() {
   // Launcher untuk meminta izin Bluetooth secara interaktif
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi appContext untuk kebutuhan PrinterManager Anda
        appContext = applicationContext

        // 2. Periksa dan minta izin secara runtime jika berjalan di Android 12 (API 31) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }

        setContent {
            // Panggil root Composable Anda (misal: App() yang mengarah ke LandingScreen)
            App()
        }
    }
}
