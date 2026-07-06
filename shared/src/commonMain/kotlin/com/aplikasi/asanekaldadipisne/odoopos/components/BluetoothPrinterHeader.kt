package com.aplikasi.asanekaldadipisne.odoopos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.KmpPrinterDevice
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.checkPrinterConnection
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class PrinterState {
    NOT_SET,    // Merah
    CONNECTED,  // Hijau
    OFFLINE     // Kuning
}

@Composable
fun BluetoothPrinterHeader(
    selectedPrinter: KmpPrinterDevice?,
    onSetPrinterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var printerState by remember { mutableStateOf(PrinterState.NOT_SET) }

    // 🔄 FIX BUG INSTANT KUNING: Polling loop dengan Jeda Toleransi
    LaunchedEffect(selectedPrinter) {
        if (selectedPrinter != null) {
            // 1. Set awal langsung HIJAU saat berhasil dipilih/dimuat
            printerState = PrinterState.CONNECTED

            // 2. Berikan napas / Jeda Toleransi 5 detik agar hardware link & js bridge sukses terikat
            delay(5000.milliseconds)

            // 3. Setelah 5 detik, baru mulai monitoring berkala setiap 3 detik
            while (true) {
                val isAlive = checkPrinterConnection(selectedPrinter.address)
                printerState = if (isAlive) PrinterState.CONNECTED else PrinterState.OFFLINE
                delay(3000.milliseconds)
            }
        } else {
            printerState = PrinterState.NOT_SET
        }
    }

    // Pemetaan warna dinamis berdasarkan state komponen
    val headerBgColor = when (printerState) {
        PrinterState.CONNECTED -> Color(0xFF1B5E20) // Hijau
        PrinterState.OFFLINE -> Color(0xFFFBC02D)   // Kuning
        PrinterState.NOT_SET -> Color(0xFFB71C1C)   // Merah
    }

    val headerContentColor =
        if (printerState == PrinterState.OFFLINE) Color(0xFF212121) else Color.White
    val indicatorColor = when (printerState) {
        PrinterState.CONNECTED -> Color(0xFF4CAF50)
        PrinterState.OFFLINE -> Color(0xFFE65100)
        PrinterState.NOT_SET -> Color(0xFFFF5252)
    }

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = headerBgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )

                Text(
                    text = when (printerState) {
                        PrinterState.CONNECTED -> "Bluetooth Printer: Connected!"
                        PrinterState.OFFLINE -> "Bluetooth Printer: Disconnected (Offline)"
                        PrinterState.NOT_SET -> "Bluetooth Printer: Not Set"
                    },
                    color = headerContentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (selectedPrinter != null) {
                    Text(
                        text = "[${selectedPrinter.name} • ${selectedPrinter.address}]",
                        color = headerContentColor.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Button(
                onClick = onSetPrinterClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = headerContentColor.copy(alpha = 0.15f),
                    contentColor = headerContentColor
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
                    text = "Set Printer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}