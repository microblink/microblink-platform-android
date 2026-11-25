package com.microblink.platform.sample.customui.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Asks for the camera, which Engine needs for its next step. [onAnswered] runs whatever the answer. */
@Composable
fun PermissionScreen(onAnswered: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onAnswered() }
    StepScaffold(
        title = "Allow the camera",
        onClose = onClose,
        bottom = {
            MainButton("Allow the camera", { request.launch(Manifest.permission.CAMERA) })
            SecondaryButton("Open settings") {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            }
        },
    ) {
        Text("The next step uses the camera. If you turned it down before, allow it in this app's settings, then come back.")
    }
}
