package com.microblink.platform.sample.customui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.microblink.platform.engine.SessionOutcome
import com.microblink.platform.engine.VerificationState
import com.microblink.platform.sample.customui.Screen

@Composable
fun ResultScreen(screen: Screen.Done, onStartOver: () -> Unit) {
    val (title, text, transactionId) = when (val outcome = screen.outcome) {
        is SessionOutcome.Verification -> when (outcome.state) {
            VerificationState.Accept -> Triple("Verified", "Your identity is verified.", outcome.transactionId)
            VerificationState.Reject -> Triple("Not verified", "Your identity couldn't be verified.", outcome.transactionId)
            VerificationState.Review -> Triple("In review", "Someone will review your verification.", outcome.transactionId)
        }
        is SessionOutcome.Canceled -> Triple("Canceled", "The verification was canceled.", outcome.transactionId)
    }
    StepScaffold(title = title, onClose = null, bottom = { MainButton("Start over", onStartOver) }) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        transactionId?.let { Text("Transaction $it", style = MaterialTheme.typography.bodySmall) }
        // The card's toString() is masked, so showing it doesn't reveal the card number.
        screen.card?.let { Text("Card: $it", style = MaterialTheme.typography.bodySmall) }
    }
}
