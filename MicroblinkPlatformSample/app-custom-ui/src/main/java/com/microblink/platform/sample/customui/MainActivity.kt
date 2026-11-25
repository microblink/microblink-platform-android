package com.microblink.platform.sample.customui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.microblink.platform.sample.customui.ui.CustomTheme
import com.microblink.platform.sample.customui.ui.ErrorScreen
import com.microblink.platform.sample.customui.ui.FormScreen
import com.microblink.platform.sample.customui.ui.PermissionScreen
import com.microblink.platform.sample.customui.ui.ProgressScreen
import com.microblink.platform.sample.customui.ui.ResultScreen
import com.microblink.platform.sample.customui.ui.StartScreen
import com.microblink.platform.sample.customui.ui.UploadIdScreen
import com.microblink.platform.sample.customui.ui.UploadNonIdScreen
import com.microblink.platform.sample.customui.ui.scannerAppearance

/** Runs a verification on Engine with this app's own screens. */
class MainActivity : ComponentActivity() {

    private val viewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Verification(viewModel)
                }
            }
        }
    }
}

/** Shows the screen the verification is on. */
@Composable
private fun Verification(viewModel: VerificationViewModel) {
    val screen = viewModel.screen
    // Back works like Cancel, so leaving ends the session instead of abandoning it. While Engine
    // works, back waits for it.
    BackHandler(enabled = screen != Screen.Start && screen !is Screen.Done) {
        when {
            screen == Screen.Working -> Unit
            screen is Screen.Error && !screen.canRetry -> viewModel.startOver()
            else -> viewModel.cancel()
        }
    }
    when (screen) {
        Screen.Start -> StartScreen(onStart = { viewModel.start(scannerAppearance) })
        Screen.Working -> ProgressScreen(viewModel.session?.status?.collectAsState()?.value)
        is Screen.Form -> FormScreen(screen, viewModel)
        is Screen.UploadId -> UploadIdScreen(screen, viewModel)
        is Screen.UploadNonId -> UploadNonIdScreen(screen, viewModel)
        Screen.CameraPermission -> PermissionScreen(onAnswered = { viewModel.next() }, onClose = viewModel::cancel)
        is Screen.Error -> ErrorScreen(
            screen,
            onRetry = { viewModel.next() },
            onClose = { if (screen.canRetry) viewModel.cancel() else viewModel.startOver() },
        )
        is Screen.Done -> ResultScreen(screen, onStartOver = viewModel::startOver)
    }
}
