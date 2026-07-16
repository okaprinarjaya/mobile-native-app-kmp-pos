package com.aplikasi.asanekaldadipisne

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.PosLandingScreen

@Composable
fun App(
    odooUrl: String,
    printerController: PrinterController
) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PosLandingScreen(
                odooUrl = odooUrl,
                printerController = printerController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}