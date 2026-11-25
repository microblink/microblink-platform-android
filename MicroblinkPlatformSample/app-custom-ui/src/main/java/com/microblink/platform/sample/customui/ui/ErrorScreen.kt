package com.microblink.platform.sample.customui.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.microblink.platform.sample.customui.Screen

@Composable
fun ErrorScreen(screen: Screen.Error, onRetry: () -> Unit, onClose: () -> Unit) {
    StepScaffold(
        title = "Something went wrong",
        onClose = null,
        bottom = {
            if (screen.canRetry) MainButton("Try again", onRetry)
            SecondaryButton(if (screen.canRetry) "Cancel the verification" else "Close", onClose)
        },
    ) {
        Text(screen.message)
    }
}
