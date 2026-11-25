package com.microblink.platform.sample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.microblink.platform.MicroblinkPlatform
import com.microblink.platform.MicroblinkPlatformConfiguration
import com.microblink.platform.MicroblinkPlatformResultListener
import com.microblink.platform.UiConfiguration
import com.microblink.platform.engine.EngineConfiguration
import com.microblink.platform.engine.EngineError
import com.microblink.platform.engine.ServiceSettings
import com.microblink.platform.engine.SessionOutcome
import com.microblink.platform.engine.VerificationState
import com.microblink.platform.sample.ui.theme.MicroblinkPlatformTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroblinkPlatformTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                startIdentityVerification()
                            }) {
                            Text(text = "Start Identity Verification")
                        }
                    }
                }
            }
        }
    }

    private fun startIdentityVerification() {
        // IMPORTANT: Replace with the sdkConfiguration of a transaction your backend created.
        // Your backend creates the transaction with your service account, along with the user's
        // consent, and returns the sdkConfiguration from its response to the app. Never create the
        // transaction in the app: that would ship your service account's secret with it.
        // A sdkConfiguration is valid for one transaction only, so fetch a new one per verification.
        val sdkConfiguration = "your_sdk_configuration"

        MicroblinkPlatform.startVerification(
            activity = this@MainActivity, MicroblinkPlatformConfiguration(
                engine = EngineConfiguration(ServiceSettings(sdkConfiguration = sdkConfiguration)),
                resultListener = object : MicroblinkPlatformResultListener {
                    override fun onVerificationFinished(outcome: SessionOutcome.Verification) {
                        showToast(
                            "Verification " + when (outcome.state) {
                                VerificationState.Accept -> "successful"
                                VerificationState.Reject -> "failed"
                                VerificationState.Review -> "requires manual review"
                            }
                        )
                    }

                    override fun onVerificationCanceled(outcome: SessionOutcome.Canceled) {
                        showToast("Verification canceled")
                    }

                    override fun onVerificationFailed(transactionId: String?, error: EngineError) {
                        Log.e("MainActivity", "Verification $transactionId failed: $error")
                        showToast("Verification could not be completed")
                    }
                },
                ui = UiConfiguration(
                    // customize the UI if needed
                ),
                cardScanResultListener = { card ->
                    // The card's toString() is masked, so logging it doesn't leak the card number.
                    Log.i("MainActivity", "Card scanned: $card")
                }
            )
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
