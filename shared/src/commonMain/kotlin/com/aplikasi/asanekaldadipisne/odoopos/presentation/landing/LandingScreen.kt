package com.aplikasi.asanekaldadipisne.odoopos.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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


@Composable
fun LandingScreen(onNavigateToWebView: (String) -> Unit) {
    var webUrl by remember { mutableStateOf("http://100.107.185.4:8069") }
    var printerList by remember { mutableStateOf(emptyList<KmpPrinterDevice>()) }
    var selectedPrinterAddress by remember { mutableStateOf(getSavedPrinterAddress()) }
    var showPrinterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Title
            Text(
                text = "Odoo POS Gateway",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )

            // CARD 1: KONFIGURASI PRINTER (Gaya Material 3)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Perubahan M3
                colors = CardDefaults.cardColors(containerColor = Color.White), // Perubahan M3
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Printer Kasir (Bluetooth)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF42474E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedPrinterAddress.isNullProperty()) "Belum Terhubung" else "Printer Aktif",
                                fontSize = 12.sp,
                                color = if (selectedPrinterAddress.isNullProperty()) Color.Red else Color(
                                    0xFF006633
                                )
                            )
                            Text(
                                text = selectedPrinterAddress ?: "Silakan pilih mesin thermal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1C1E)
                            )
                        }

                        // BUTTON PILIH PRINTER (Gaya Material 3)
                        Button(
                            onClick = {
                                printerList = getPairedPrintersList()
                                showPrinterDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)), // Menggunakan containerColor
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pilih / Cari", color = Color.White)
                        }
                    }
                }
            }

            // CARD 2: KONFIGURASI URL ODOO (Gaya Material 3)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Perubahan M3
                colors = CardDefaults.cardColors(containerColor = Color.White), // Perubahan M3
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alamat Server Odoo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF42474E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { webUrl = it },
                        label = { Text("Web URL Address") },
                        placeholder = { Text("http://192.168.x.x:8069") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // TOMBOL MASUK ODOO (Gaya Material 3)
        Button(
            onClick = {
                if (webUrl.isNotEmpty()) {
                    onNavigateToWebView(webUrl)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E72)), // Menggunakan containerColor
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Buka Aplikasi Odoo POS",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // DIALOG POP-UP (Gaya Material 3)
    if (showPrinterDialog) {
        AlertDialog(
            onDismissRequest = { showPrinterDialog = false },
            title = { Text(text = "Pilih Printer Paired", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (printerList.isEmpty()) {
                        Text(
                            text = "Tidak ada printer Bluetooth yang ter-pairing atau Izin Bluetooth Belum Diberikan.\n\nPastikan Anda sudah pairing printer di menu Pengaturan HP Anda.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        printerList.forEach { printer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                    .clickable {
                                        saveSelectedPrinterAddress(printer.address)
                                        selectedPrinterAddress = printer.address
                                        showPrinterDialog = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = printer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = printer.address,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrinterDialog = false }) {
                    Text("Tutup", color = Color(0xFF6750A4))
                }
            }
        )
    }
}

fun String?.isNullProperty(): Boolean {
    return this == null || this.trim().isEmpty() || this.contains("Belum memilih")
}