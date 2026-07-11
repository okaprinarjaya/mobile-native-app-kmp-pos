package com.aplikasi.asanekaldadipisne

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.PosLandingScreen

@Composable
fun App(odooUrl: String) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PosLandingScreen(
                odooUrl = odooUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}