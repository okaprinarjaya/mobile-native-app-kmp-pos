package com.aplikasi.asanekaldadipisne.odoopos.presentation.settings_screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
actual fun AudioPermissionRow(odooColor: Color, darkTextColor: Color) {
    val context = LocalContext.current

    var isAudioPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isAudioPermissionGranted = isGranted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = "Berikan Akses Audio Recording",
                color = darkTextColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dibutuhkan agar fitur pencarian suara dapat menangkap audio microphone.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                if (!isAudioPermissionGranted) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = odooColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isAudioPermissionGranted
        ) {
            Text(
                text = if (isAudioPermissionGranted) {
                    "Akses audio recording sudah diberikan"
                } else {
                    "Berikan akses audio recording"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE5E5E5), modifier = Modifier.padding(top = 12.dp))
}

@Composable
actual fun CameraPermissionRow(
    odooColor: Color,
    darkTextColor: Color
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = "Berikan Akses Kamera",
                color = darkTextColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dibutuhkan agar dapat menggunakan kamera.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                if (!hasCameraPermission) {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = odooColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !hasCameraPermission
        ) {
            Text(
                text = if (hasCameraPermission) {
                    "Akses kamera sudah diberikan"
                } else {
                    "Berikan akses kamera"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
    HorizontalDivider(
        color = Color(0xFFE5E5E5),
        modifier = Modifier.padding(top = 8.dp)
    )
}