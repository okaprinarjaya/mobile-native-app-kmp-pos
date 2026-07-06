package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aplikasi.asanekaldadipisne.odoopos.presentation.loaded_webapp.WebViewForLoadedWebApp

@Composable
fun PosLandingScreen(
    modifier: Modifier = Modifier, odooUrl: String = "http://192.168.1.6:8069"
) {
    var isConnected by remember { mutableStateOf(false) }
    var selectedPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }
    var showPrinterDialog by remember { mutableStateOf(false) }

    // State untuk menampung list printer Bluetooth yang REAL
    var printerList by remember { mutableStateOf<List<KmpPrinterDevice>>(emptyList()) }
    var tempSelectedPrinter by remember { mutableStateOf<KmpPrinterDevice?>(null) }

    // 🔄 1. AUTOMATIC AUTO-LOAD SAAT APLIKASI DIKLIKA/DIBUKA
    LaunchedEffect(Unit) {
        val savedAddress = getSavedPrinterAddress()
        if (!savedAddress.isNullOrEmpty()) {
            // Ambil daftar printer untuk mencari kecocokan nama device-nya
            val pairedDevices = getPairedPrintersList()
            val matchedPrinter = pairedDevices.find { it.address == savedAddress }

            if (matchedPrinter != null) {
                selectedPrinter = matchedPrinter
                tempSelectedPrinter = matchedPrinter
                isConnected = true
            } else {
                // Jika devicenya tidak ketemu di daftar paired tapi mac-nya ada, tetap tampilkan mac-nya
                selectedPrinter = KmpPrinterDevice(name = "Saved Printer", address = savedAddress)
                isConnected = true
            }
        }
    }

    // 🔄 2. REFRESH DATA PRINTER SETIAP KALI DIALOG DIBUKA
    LaunchedEffect(showPrinterDialog) {
        if (showPrinterDialog) {
            printerList = getPairedPrintersList()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                color = if (isConnected) Color(0xFF1B5E20) else Color(0xFFB71C1C)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape)
                                .background(if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        )

                        Text(
                            text = if (isConnected) "Bluetooth Printer: Connected!" else "Bluetooth Printer: Disconnected",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Menampilkan data nama & MAC Address REAL yang sedang aktif
                        if (isConnected && selectedPrinter != null) {
                            Text(
                                text = "[${selectedPrinter?.name} • ${selectedPrinter?.address}]",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Button(
                        onClick = { showPrinterDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Printer",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Printer", fontSize = 11.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.Black)
        ) {
            WebViewForLoadedWebApp(
                url = odooUrl, modifier = Modifier.fillMaxSize()
            )
        }
    }

    // =========================================================================
    // DIALOG SELEKSI PRINTER (REAL LIST DATA)
    // =========================================================================
    if (showPrinterDialog) {
        Dialog(onDismissRequest = { showPrinterDialog = false }) {
            BoxWithConstraints {
                val isTablet = maxWidth >= 600.dp

                Card(
                    modifier = Modifier.fillMaxWidth(if (isTablet) 0.5f else 0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Choose Printer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // STATE JIKA LIST PRINTER BLUETOOTH KOSONG
                        if (printerList.isEmpty()) {
                            Text(
                                text = "No paired Bluetooth printers found.\nPlease pair your printer in Android Settings first or grant Bluetooth permissions.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(weight = 1f, fill = false)
                            ) {
                                items(printerList) { printer ->
                                    val isThisSelected =
                                        tempSelectedPrinter?.address == printer.address
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)).background(
                                            if (isThisSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        ).clickable { tempSelectedPrinter = printer }
                                        .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Print,
                                                contentDescription = null,
                                                tint = if (isThisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Column {
                                                Text(
                                                    text = printer.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 15.sp,
                                                    color = if (isThisSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = printer.address, // Menggunakan property .address murni dari modelmu
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (isThisSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showPrinterDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (tempSelectedPrinter != null) {
                                        selectedPrinter = tempSelectedPrinter
                                        isConnected = true
                                        // 🎯 SIMPAN KE REAL SHAREDPREFFERENCES ANDROID
                                        saveSelectedPrinterAddress(tempSelectedPrinter!!.address)
                                    } else {
                                        selectedPrinter = null
                                        isConnected = false
                                    }
                                    showPrinterDialog = false
                                },
                                enabled = tempSelectedPrinter != null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        }
    }
}