package com.aplikasi.asanekaldadipisne.odoopos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.KmpPrinterDevice
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getPairedBluetoothPrintersList

@Composable
fun BluetoothPrinterSelectionDialog(
    currentSelectedPrinter: KmpPrinterDevice?,
    onDismissRequest: () -> Unit,
    onConfirmConnect: (KmpPrinterDevice) -> Unit,
    onDeviceGone: (PrinterConnectionType) -> Unit,
    isSelectionDialogOpened: Boolean = false,
    modifier: Modifier = Modifier
) {
    var printerList by remember { mutableStateOf(emptyList<KmpPrinterDevice>()) }
    var tempSelectedPrinter by remember(currentSelectedPrinter) {
        mutableStateOf(
            currentSelectedPrinter
        )
    }

    LaunchedEffect(isSelectionDialogOpened) {
        if (isSelectionDialogOpened) {
            printerList = getPairedBluetoothPrintersList()
            if (printerList.isEmpty()) {
                onDeviceGone(PrinterConnectionType.BLUETOOTH)
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints(modifier = modifier) {
            val isTablet = maxWidth >= 600.dp

            Card(
                modifier = Modifier
                    .fillMaxWidth(if (isTablet) 0.5f else 0.9f)
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
                                val isThisSelected = tempSelectedPrinter?.address == printer.address
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isThisSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { tempSelectedPrinter = printer }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
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
                                                text = printer.address,
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
                        TextButton(onClick = onDismissRequest) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                tempSelectedPrinter?.let { printer ->
                                    onConfirmConnect(printer)
                                }
                            },
                            enabled = tempSelectedPrinter != null && printerList.isNotEmpty(),
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