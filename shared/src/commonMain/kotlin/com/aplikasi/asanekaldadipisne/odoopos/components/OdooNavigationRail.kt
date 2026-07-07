package com.aplikasi.asanekaldadipisne.odoopos.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
        containerColor = Color(0xFF1E1E2C), // Warna gelap elegan khusus sidebar
        contentColor = Color.White, modifier = modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.size(24.dp))

        // TAB 1: POINT OF SALE
        NavigationRailItem(
            selected = currentTab == OdooTab.POS,
            onClick = { onTabSelected(OdooTab.POS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "POS UI",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("POS", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color(0xFF1E1E2C),
                selectedTextColor = Color.White,
                indicatorColor = Color.White, // Lingkaran background saat aktif
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        // TAB 2: ODOO ORDERS LIST
        NavigationRailItem(
            selected = currentTab == OdooTab.ORDERS,
            onClick = { onTabSelected(OdooTab.ORDERS) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Odoo Orders",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Orders", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color(0xFF1E1E2C),
                selectedTextColor = Color.White,
                indicatorColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
    }
}