package com.microblink.platform.sample.customui

import com.microblink.platform.engine.EngineError
import com.microblink.platform.engine.FormFieldErrorReason
import com.microblink.platform.engine.StepValidationReason
import com.microblink.platform.engine.UploadInputErrorReason

/** What went wrong, in one sentence for the user. */
fun EngineError.describe(): String = when (this) {
    EngineError.NoNetwork -> "You're offline. Check your connection and try again."
    EngineError.Timeout -> "The connection timed out. Try again."
    EngineError.VerificationUnavailable -> "Verification isn't available right now."
    EngineError.RetryLimitExceeded -> "There were too many attempts, so the verification ended."
    is EngineError.StepValidation -> when (reason) {
        StepValidationReason.StepCanceled -> "The step was closed before it finished."
        StepValidationReason.RetakeSelfie -> "The selfie couldn't be used. Take it again."
        else -> "That didn't work. Try the step again."
    }
    is EngineError.MissingScannerSdk -> "This app doesn't include the scanner SDK for the $stepType step."
    is EngineError.UnsupportedWorkflowVersion -> "This workflow was built for SDK $workflowVersion, which SDK $sdkVersion can't run."
    is EngineError.InvalidNextInput -> "The app sent the wrong input for this step."
    is EngineError.InvalidFormInput -> "Check the highlighted fields."
    is EngineError.InvalidUploadInput -> (side?.let { "$it side: " } ?: "") + when (reason) {
        UploadInputErrorReason.BackSideRequired -> "Add the back side too."
        UploadInputErrorReason.EmptyFile -> "That file is empty."
        UploadInputErrorReason.UnsupportedFileType -> "That file type isn't accepted."
        UploadInputErrorReason.FileTooLarge -> "That file is larger than 20 MB."
        UploadInputErrorReason.DocumentTypeRequired -> "Choose which document you're uploading."
        UploadInputErrorReason.DocumentTypeNotAllowed -> "That document isn't accepted here."
    }
    is EngineError.Internal -> "Something went wrong: $message"
}

/** Why a form field was refused, shown under the field. */
fun FormFieldErrorReason.describe(): String = when (this) {
    FormFieldErrorReason.Required -> "Required"
    FormFieldErrorReason.WrongType -> "Not the right kind of value"
    FormFieldErrorReason.InvalidFormat -> "Not a valid value"
    FormFieldErrorReason.OutOfRange -> "Out of range"
    FormFieldErrorReason.TooLarge -> "The file is larger than 20 MB"
}
