package com.microblink.platform.sample.customui.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.microblink.platform.engine.AllowedDocument
import com.microblink.platform.sample.customui.Screen
import com.microblink.platform.sample.customui.VerificationViewModel
import com.microblink.platform.sample.customui.rememberFilePicker

@Composable
fun UploadNonIdScreen(screen: Screen.UploadNonId, viewModel: VerificationViewModel) {
    val documents = screen.request.allowedDocuments
    val chosen = viewModel.document ?: documents.singleOrNull()
    val pick = rememberFilePicker { viewModel.documentFile = it }
    StepScaffold(
        title = "Upload a document",
        onClose = viewModel::cancel,
        bottom = {
            MainButton("Upload", viewModel::submitUploadNonId, enabled = chosen != null && viewModel.documentFile != null)
        },
    ) {
        screen.message?.let { ErrorText(it) }
        if (documents.size > 1) {
            documents.forEach { document ->
                DocumentOption(document, selected = document == viewModel.document) { viewModel.document = document }
            }
        } else {
            chosen?.description?.let { Text(it) }
        }
        FileChoice(chosen?.name() ?: "Document", viewModel.documentFile) { pick("*/*") }
    }
}

@Composable
private fun DocumentOption(document: AllowedDocument, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column {
            Text(document.name())
            document.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private fun AllowedDocument.name(): String = displayName ?: documentType.name
