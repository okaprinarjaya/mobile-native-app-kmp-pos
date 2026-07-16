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
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.KmpPrinterDevice
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getPairedBluetoothPrintersList

@Composable
fun PrinterConnectionHeader(
    printerController: PrinterController,
    selectedPrinterConnectionType: SelectedPrinterConnectionTypeState,
    bluetoothPrinterList: List<KmpPrinterDevice> = remember { getPairedBluetoothPrintersList() },
    onBluetoothPrinterSelected: (KmpPrinterDevice) -> Unit,
    onUSBPrinterSelected: (KmpPrinterDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrinterConnectionTypeSelectionModal by remember { mutableStateOf(false) }
    var showBluetoothDialog by remember { mutableStateOf(false) }
    var showUSBDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        when (selectedPrinterConnectionType.connectionType) {
            PrinterConnectionType.BLUETOOTH -> {
                BluetoothPrinterHeader(
                    selectedPrinter = selectedPrinterConnectionType.bluetoothDevice,
                    onSetPrinterClick = { showPrinterConnectionTypeSelectionModal = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PrinterConnectionType.USB -> {
                USBPrinterHeader(
                    selectedPrinter = selectedPrinterConnectionType.usbDevice,
                    onSetPrinterClick = { showPrinterConnectionTypeSelectionModal = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PrinterConnectionType.NONE -> {
                DefaultDisconnectedHeader(
                    onSetupClick = { showPrinterConnectionTypeSelectionModal = true }
                )
            }
        }

        if (showPrinterConnectionTypeSelectionModal) {
            PopupPrinterConnectionSelection(
                onDismiss = { showPrinterConnectionTypeSelectionModal = false },
                onSelectBluetooth = {
                    showPrinterConnectionTypeSelectionModal = false
                    showBluetoothDialog = true
                },
                onSelectUsb = {
                    showPrinterConnectionTypeSelectionModal = false
                    showUSBDialog = true
                }
            )
        }

        // 2.2.1 Dialog Bluetooth (Gunakan komponen lama yang nanti kamu rename)
        if (showBluetoothDialog) {
            BluetoothPrinterSelectionDialog(
                printerList = bluetoothPrinterList,
                currentSelectedPrinter = selectedPrinterConnectionType.bluetoothDevice,
                onDismissRequest = { showBluetoothDialog = false },
                onConfirmConnect = { selectedDevice ->
                    showBluetoothDialog = false
                    onBluetoothPrinterSelected(selectedDevice)
                }
            )
        }

        // 2.2.2 Dialog USB Komponen Baru
        if (showUSBDialog) {
            USBPrinterSelectionDialog(
                printerController = printerController,
                onDismiss = { showUSBDialog = false },
                onUsbPrinterSelected = { device ->
                    showUSBDialog = false
                    onUSBPrinterSelected(device)
                }
            )
        }
    }
}

@Composable
private fun DefaultDisconnectedHeader(
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFB71C1C), // Merah solid senada dengan state NOT_SET kamu
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
                    modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFF5252))
                )
                Text(
                    text = "No Printer Connection Configured",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onSetupClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Setup",
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Setup Printer", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}