package com.microblink.platform.sample.customui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.microblink.platform.engine.UploadFile

/** The file at [uri] as Engine uploads it. Engine reads its bytes only when it sends them. */
fun Context.uploadFile(uri: Uri): UploadFile {
    var name = uri.lastPathSegment ?: "file"
    var size: Long? = null
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(0)?.let { name = it }
            if (!cursor.isNull(1)) size = cursor.getLong(1)
        }
    }
    return UploadFile(
        contentType = contentResolver.getType(uri) ?: "application/octet-stream",
        fileName = name,
        sizeBytes = size,
        openStream = { checkNotNull(contentResolver.openInputStream(uri)) { "Can't read $uri" } },
    )
}

/** Remembers a file picker. Call the function it returns with the MIME type of the files to offer. */
@Composable
fun rememberFilePicker(onPicked: (UploadFile) -> Unit): (mimeType: String) -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPicked(context.uploadFile(it)) }
    }
    return { mimeType -> launcher.launch(mimeType) }
}
