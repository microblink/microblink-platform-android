package com.microblink.platform.sample.customui

import com.microblink.platform.engine.CardScanResult
import com.microblink.platform.engine.InputRequest
import com.microblink.platform.engine.SessionOutcome

/** What the app shows. [VerificationViewModel] turns each step Engine returns into one of these. */
sealed interface Screen {

    /** Nothing running: a button that starts a verification. */
    data object Start : Screen

    /** Waiting for the backend or a scanner. */
    data object Working : Screen

    /** A form to fill in, with the errors of its last submission. */
    data class Form(
        val form: InputRequest.Form,
        val fieldErrors: Map<String, String> = emptyMap(),
        val message: String? = null,
    ) : Screen

    /** One or both sides of an ID to upload. */
    data class UploadId(val request: InputRequest.UploadIdDocument, val message: String? = null) : Screen

    /** A document to choose and upload. */
    data class UploadNonId(val request: InputRequest.UploadNonIdDocument, val message: String? = null) : Screen

    /** The next step needs the camera. */
    data object CameraPermission : Screen

    /** Something went wrong; [canRetry] says whether the step can be tried again. */
    data class Error(val message: String, val canRetry: Boolean) : Screen

    /** The verification is over. */
    data class Done(val outcome: SessionOutcome, val card: CardScanResult?) : Screen
}
