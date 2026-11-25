package com.microblink.platform.sample.customui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microblink.platform.engine.AllowedDocument
import com.microblink.platform.engine.CancelReason
import com.microblink.platform.engine.CardScanResult
import com.microblink.platform.engine.Engine
import com.microblink.platform.engine.EngineConfiguration
import com.microblink.platform.engine.EngineError
import com.microblink.platform.engine.EngineException
import com.microblink.platform.engine.InputRequest
import com.microblink.platform.engine.RetryAction
import com.microblink.platform.engine.ScannerAppearance
import com.microblink.platform.engine.ServiceSettings
import com.microblink.platform.engine.Session
import com.microblink.platform.engine.StepAction
import com.microblink.platform.engine.StepBlocker
import com.microblink.platform.engine.StepInput
import com.microblink.platform.engine.StepOutput
import com.microblink.platform.engine.StepResult
import com.microblink.platform.engine.UploadFile
import com.microblink.platform.sample.transaction.TransactionClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Runs one verification on Engine: creates the transaction, starts its session, and turns every
 * step Engine returns into a [Screen]. It lives in a ViewModel so the session survives rotation.
 */
class VerificationViewModel : ViewModel() {

    var screen: Screen by mutableStateOf(Screen.Start)
        private set

    /** The running session, whose status the progress screen shows. */
    var session: Session? by mutableStateOf(null)
        private set

    // What the user typed and picked on the current input screen. It lives here, not in the
    // screens, so it survives an error that sends the user back to the same screen.
    val formText = mutableStateMapOf<String, String>()
    val formFiles = mutableStateMapOf<String, UploadFile>()
    var front: UploadFile? by mutableStateOf(null)
    var back: UploadFile? by mutableStateOf(null)
    var document: AllowedDocument? by mutableStateOf(null)
    var documentFile: UploadFile? by mutableStateOf(null)

    /** The input screen to return to when Engine asks for the input again. */
    private var lastInput: Screen? = null

    /** The step that asked for [lastInput]. */
    private var lastAction: StepAction? = null

    /** The card a card scan step read, for the result screen. */
    private var card: CardScanResult? = null

    /** Creates a transaction and starts its session. [appearance] styles Engine's scanner screens. */
    fun start(appearance: ScannerAppearance) {
        screen = Screen.Working
        viewModelScope.launch {
            try {
                val sdkConfiguration = TransactionClient.createTransaction()
                val started = Engine.startSession(EngineConfiguration(ServiceSettings(sdkConfiguration), appearance)).getOrThrow()
                session = started.session
                show(started.first)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = (e as? EngineException)?.error?.describe() ?: e.message ?: "The verification couldn't start."
                screen = Screen.Error(message, canRetry = false)
            }
        }
    }

    /** Advances the session by one step, giving it [input] when the current step asked for some. */
    fun next(input: StepInput = StepInput.None) {
        val session = session ?: return
        screen = Screen.Working
        viewModelScope.launch { show(session.next(input)) }
    }

    fun submitForm() {
        val form = (screen as? Screen.Form)?.form ?: return
        val values = form.fields.associateWith { it.toValue(formText[it.name].orEmpty(), formFiles[it.name]) }
        val unreadable = values.filterValues { it == null }.keys
        if (unreadable.isNotEmpty()) {
            screen = Screen.Form(form, fieldErrors = unreadable.associate { it.name to "Enter ${it.formatHint()}" })
            return
        }
        next(StepInput.FormSubmission(values.values.filterNotNull()))
    }

    fun submitUploadId() {
        next(StepInput.UploadIdDocument(front ?: return, back))
    }

    fun submitUploadNonId() {
        val request = (screen as? Screen.UploadNonId)?.request ?: return
        next(StepInput.UploadNonIdDocument(document ?: request.allowedDocuments.singleOrNull(), documentFile ?: return))
    }

    /** Cancels the running session, or starts over if none is running. */
    fun cancel() {
        val session = session ?: return startOver()
        screen = Screen.Working
        viewModelScope.launch { screen = Screen.Done(session.cancel(CancelReason.UserCanceled), card) }
    }

    fun startOver() {
        session = null
        lastInput = null
        lastAction = null
        card = null
        clearDrafts()
        screen = Screen.Start
    }

    private fun show(result: StepResult) {
        (result.lastStepOutput as? StepOutput.CardScan)?.let { card = it.result }
        screen = when (result) {
            is StepResult.Ready -> when (val action = result.action) {
                // Engine shows the scanner and face screens itself.
                is StepAction.EngineStep -> return next()
                is StepAction.Start -> input(Screen.Form(action.form), action)
                is StepAction.InputStep -> input(
                    when (val request = action.request) {
                        is InputRequest.Form -> Screen.Form(request)
                        is InputRequest.UploadIdDocument -> Screen.UploadId(request)
                        is InputRequest.UploadNonIdDocument -> Screen.UploadNonId(request)
                    },
                    action,
                )
            }
            is StepResult.Blocked -> when (result.blocker) {
                is StepBlocker.PermissionRequired -> Screen.CameraPermission
            }
            is StepResult.Failed -> failed(result)
            is StepResult.Finished -> Screen.Done(result.outcome, card)
        }
    }

    /**
     * Shows an input screen. A new step starts from the form's default values; the same step asked
     * again, as after a submit whose answer Engine lost, keeps what the user entered.
     */
    private fun input(screen: Screen, action: StepAction): Screen {
        if (!action.isSameInputAs(lastAction)) {
            clearDrafts()
            (screen as? Screen.Form)?.form?.fields?.forEach { field -> field.defaultValue?.let { formText[field.name] = it } }
        }
        lastInput = screen
        lastAction = action
        return screen
    }

    private fun failed(result: StepResult.Failed): Screen {
        val message = result.error.describe()
        return when (val retry = result.retryAction) {
            null -> Screen.Error(message, canRetry = false)
            RetryAction.CallNextWithoutInput -> Screen.Error(message, canRetry = true)
            // Back to the same input screen, keeping what the user typed and picked.
            is RetryAction.ResubmitHostInput -> when (val again = lastInput) {
                is Screen.Form -> again.copy(
                    fieldErrors = (result.error as? EngineError.InvalidFormInput)?.errors.orEmpty()
                        .associate { it.fieldName to it.reason.describe() },
                    message = message,
                )
                is Screen.UploadId -> again.copy(message = message)
                is Screen.UploadNonId -> again.copy(message = message)
                else -> Screen.Error("Engine asked again for ${retry.kind} input, which this app never showed.", canRetry = false)
            }
        }
    }

    private fun clearDrafts() {
        formText.clear()
        formFiles.clear()
        front = null
        back = null
        document = null
        documentFile = null
    }
}

/**
 * Whether this asks for the same input as [previous], as when Engine asks again after losing a
 * submit's answer. Two steps can have the same fields, so the step has to match too.
 */
internal fun StepAction.isSameInputAs(previous: StepAction?): Boolean = when (this) {
    is StepAction.InputStep -> previous is StepAction.InputStep && previous.step.id == step.id && previous.request == request
    is StepAction.Start -> previous == this
    is StepAction.EngineStep -> false
}
