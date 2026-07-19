package com.aplikasi.asanekaldadipisne.odoopos.presentation.settings_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val odooColor = Color(0xFF714B67)
    val darkTextColor = Color(0xFF2C2C2C)

    var isPsovEnabled by remember { mutableStateOf(getSavedPsovSetting()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =======================================================
            // KELOMPOK 1: FITUR & FUNGSIONALITAS (SWITCH GROUP)
            // =======================================================
            item {
                Text(
                    text = "Fitur & Fungsionalitas POS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = odooColor,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktifkan fitur pencarian produk menggunakan suara (product search on voice)",
                        color = darkTextColor,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    )
                    Switch(
                        checked = isPsovEnabled,
                        onCheckedChange = { newValue ->
                            isPsovEnabled = newValue
                            savePsovSetting(newValue)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = odooColor,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
                HorizontalDivider(
                    color = Color(0xFFE5E5E5),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // =======================================================
            // KELOMPOK 2: IZIN & HAK AKSES SISTEM (BUTTON GROUP)
            // =======================================================
            item {
                Text(
                    text = "Izin & Hak Akses Perangkat",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = odooColor,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                AudioPermissionRow(
                    odooColor = odooColor,
                    darkTextColor = darkTextColor
                )
            }

            item {
                CameraPermissionRow(
                    odooColor = odooColor,
                    darkTextColor = darkTextColor
                )
            }
        }
    }
}

@Composable
expect fun AudioPermissionRow(odooColor: Color, darkTextColor: Color)

@Composable
expect fun CameraPermissionRow(odooColor: Color, darkTextColor: Color)