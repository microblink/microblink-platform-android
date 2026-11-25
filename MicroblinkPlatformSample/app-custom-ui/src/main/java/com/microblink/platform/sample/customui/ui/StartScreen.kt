package com.microblink.platform.sample.customui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun StartScreen(onStart: () -> Unit) {
    StepScaffold(
        title = "Verify your identity",
        onClose = null,
        bottom = { MainButton("Start verification", onStart) },
    ) {
        Text(
            "This app runs the verification on the Microblink Platform Engine and draws its own screens. " +
                "Only the scanner and face screens come from the SDK, in this app's colors and fonts.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
