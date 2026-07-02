package com.aplikasi.asanekaldadipisne

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.LandingScreen
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp

@Composable
fun App() {
    var confirmedUrl by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
        ) {
            if (confirmedUrl == null) {
                LandingScreen(
                    onNavigateToWebView = { url ->
                        confirmedUrl = url
                    }
                )
            } else {
                WebViewForLoadedWebApp(
                    url = confirmedUrl!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}