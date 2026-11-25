package com.microblink.platform.sample.customui.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.microblink.platform.sample.customui.Screen
import com.microblink.platform.sample.customui.VerificationViewModel
import com.microblink.platform.sample.customui.rememberFilePicker

@Composable
fun UploadIdScreen(screen: Screen.UploadId, viewModel: VerificationViewModel) {
    val request = screen.request
    val pickFront = rememberFilePicker { viewModel.front = it }
    val pickBack = rememberFilePicker { viewModel.back = it }
    StepScaffold(
        title = request.displayName ?: "Upload your ID",
        onClose = viewModel::cancel,
        bottom = {
            MainButton(
                "Upload",
                viewModel::submitUploadId,
                enabled = viewModel.front != null && (request.isOnlyFrontSide || viewModel.back != null),
            )
        },
    ) {
        request.description?.let { Text(it) }
        screen.message?.let { ErrorText(it) }
        FileChoice("Front side", viewModel.front) { pickFront("image/*") }
        if (!request.isOnlyFrontSide) FileChoice("Back side", viewModel.back) { pickBack("image/*") }
    }
}
