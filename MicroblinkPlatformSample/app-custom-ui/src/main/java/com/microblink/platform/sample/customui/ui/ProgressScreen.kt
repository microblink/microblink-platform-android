package com.microblink.platform.sample.customui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.microblink.platform.engine.EngineStatus
import com.microblink.platform.engine.EngineStepStatus

/** Shown while Engine works, with what it's doing from [status]. */
@Composable
fun ProgressScreen(status: EngineStatus?) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            when (status) {
                is EngineStatus.UploadingFile -> "Uploading ${status.percentComplete}%"
                is EngineStatus.RunningEngineStep ->
                    if (status.progress.status == EngineStepStatus.Submitting) "Sending the scan…" else "Opening the scanner…"
                else -> "Please wait…"
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
