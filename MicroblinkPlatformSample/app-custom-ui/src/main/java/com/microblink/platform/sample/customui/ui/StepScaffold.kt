package com.microblink.platform.sample.customui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.microblink.platform.engine.UploadFile

/** The frame every screen shares: a cancel button, a title, scrolling content, and buttons at the bottom. */
@Composable
fun StepScaffold(
    title: String,
    onClose: (() -> Unit)?,
    bottom: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
    ) {
        if (onClose != null) TextButton(onClick = onClose) { Text("Cancel") }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            content()
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { bottom() }
    }
}

/** The screen's main action. */
@Composable
fun MainButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick, Modifier.fillMaxWidth().height(52.dp), enabled = enabled, shape = MaterialTheme.shapes.small) {
        Text(text)
    }
}

/** A less prominent action. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.small) {
        Text(text)
    }
}

/** What went wrong. */
@Composable
fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}

/** A file to pick, showing the name of the one picked. */
@Composable
fun FileChoice(label: String, file: UploadFile?, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        SecondaryButton(file?.fileName ?: "Choose a file", onClick)
    }
}
