package com.aplikasi.asanekaldadipisne.odoopos.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aplikasi.asanekaldadipisne.odoopos.OdooTab

@Composable
fun OdooNavigationRail(
    currentTab: OdooTab, onTabSelected: (OdooTab) -> Unit, modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color(0xFF1E1E2C),
        contentColor = Color.White
    ) {
        // ==========================================
        // 🔝 TAB BAGIAN ATAS (BERJEJER VERTIKAL)
        // ==========================================
        Spacer(modifier = Modifier.height(16.dp))

        NavigationRailItem(
            selected = currentTab == OdooTab.POS,
            onClick = { onTabSelected(OdooTab.POS) },
            icon = { Icon(Icons.Default.PointOfSale, contentDescription = "POS") },
            label = { Text("POS", fontSize = 11.sp) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NavigationRailItem(
            selected = currentTab == OdooTab.ORDERS,
            onClick = { onTabSelected(OdooTab.ORDERS) },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") },
            label = { Text("Orders", fontSize = 11.sp) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NavigationRailItem(
            selected = currentTab == OdooTab.SETTINGS,
            onClick = { onTabSelected(OdooTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
            label = { Text("Pengaturan", fontSize = 11.sp) }
        )

        // ==========================================
        // 🚀 SPACER UNTUK MENDORONG TAB KE PALING BAWAH
        // ==========================================
        Spacer(modifier = Modifier.weight(1f))

        // ==========================================
        // 🔻 TAB PALING BAWAH: "KELUAR"
        // ==========================================
        NavigationRailItem(
            selected = false, // Tidak pernah 'selected' karena fungsinya action/trigger
            onClick = { onTabSelected(OdooTab.LOGOUT) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Keluar",
                    tint = Color(0xFFFF5252) // Warna merah khas logout
                )
            },
            label = {
                Text(
                    text = "Keluar",
                    fontSize = 11.sp,
                    color = Color(0xFFFF5252)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}