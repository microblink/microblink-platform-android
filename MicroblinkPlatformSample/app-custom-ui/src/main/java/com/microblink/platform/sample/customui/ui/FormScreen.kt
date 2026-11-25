package com.microblink.platform.sample.customui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.microblink.platform.engine.FileSubtype
import com.microblink.platform.engine.FormField
import com.microblink.platform.engine.NumberSubtype
import com.microblink.platform.engine.TextSubtype
import com.microblink.platform.sample.customui.Screen
import com.microblink.platform.sample.customui.VerificationViewModel
import com.microblink.platform.sample.customui.formatHint
import com.microblink.platform.sample.customui.rememberFilePicker

@Composable
fun FormScreen(screen: Screen.Form, viewModel: VerificationViewModel) {
    StepScaffold(
        title = "A few details",
        onClose = viewModel::cancel,
        bottom = { MainButton("Continue", viewModel::submitForm) },
    ) {
        screen.message?.let { ErrorText(it) }
        screen.form.fields.forEach { field -> Field(field, screen.fieldErrors[field.name], viewModel) }
    }
}

/** One field, drawn by its type: a checkbox, a file to pick, or text. */
@Composable
private fun Field(field: FormField, error: String?, viewModel: VerificationViewModel) {
    val label = (field.displayName ?: field.name) + if (field.isRequired) " *" else ""
    val support = error ?: field.description ?: field.formatHint()?.replaceFirstChar(Char::uppercase)
    when (field) {
        is FormField.Bool -> Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.formText[field.name] == "true",
                onCheckedChange = { viewModel.formText[field.name] = it.toString() },
            )
            Column {
                Text(label)
                support?.let { Support(it, isError = error != null) }
            }
        }
        is FormField.File -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val pick = rememberFilePicker { viewModel.formFiles[field.name] = it }
            FileChoice(label, viewModel.formFiles[field.name]) {
                pick(if (field.subtype == FileSubtype.Pdf) "application/pdf" else "image/*")
            }
            support?.let { Support(it, isError = error != null) }
        }
        else -> OutlinedTextField(
            value = viewModel.formText[field.name].orEmpty(),
            onValueChange = { viewModel.formText[field.name] = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            supportingText = support?.let { { Text(it) } },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType()),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
    }
}

@Composable
private fun Support(text: String, isError: Boolean) {
    if (isError) ErrorText(text) else Text(text, style = MaterialTheme.typography.bodySmall)
}

private fun FormField.keyboardType(): KeyboardType = when (this) {
    is FormField.Number -> if (subtype == NumberSubtype.Integer) KeyboardType.Number else KeyboardType.Decimal
    is FormField.Text -> when (subtype) {
        TextSubtype.Email -> KeyboardType.Email
        TextSubtype.Phone -> KeyboardType.Phone
        else -> KeyboardType.Text
    }
    else -> KeyboardType.Text
}
