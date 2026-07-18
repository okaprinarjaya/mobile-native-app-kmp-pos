package com.aplikasi.asanekaldadipisne.odoopos.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aplikasi.asanekaldadipisne.odoopos.PrinterController
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.KmpPrinterDevice
import com.aplikasi.asanekaldadipisne.odoopos.presentation.landing.getUSBPrinterList

@Composable
fun USBPrinterSelectionDialog(
    printerController: PrinterController,
    onDismiss: () -> Unit,
    onUsbPrinterSelected: (KmpPrinterDevice) -> Unit,
    onDeviceGone: (PrinterConnectionType) -> Unit
) {
    var isDetecting by remember { mutableStateOf(false) }
    var detectedDevices by remember { mutableStateOf<List<KmpPrinterDevice>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(hasSearched) {
        if (hasSearched && detectedDevices.isEmpty()) {
            onDeviceGone(PrinterConnectionType.USB)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "USB PRINTER SETUP",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Step 1: CONNECT PRINTER TO USB-C HUB.\n(Ensure printer power is turned ON).",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isDetecting = true
                        detectedDevices = getUSBPrinterList()
                        isDetecting = false
                        hasSearched = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C36)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("DETECT USB PRINTER", color = Color.White, fontSize = 12.sp)
                    }
                }

                if (hasSearched) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (detectedDevices.isEmpty()) {
                        Text(
                            text = "No USB Printer detected.\nPlease check USB cable connection or power.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp
                        )
                    } else {
                        detectedDevices.forEach { device ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF22222A)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "PRINTER DETECTED",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        device.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            printerController.connectUSBPrinter(
                                                onSucess = {
                                                    onUsbPrinterSelected(device)
                                                },
                                                onError = {}
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFF4CAF50
                                            )
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("CONNECT", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "*Requires USB-C Hub with Power Delivery for simultaneous charging.",
                    color = Color(0xFFFF9F0A),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}