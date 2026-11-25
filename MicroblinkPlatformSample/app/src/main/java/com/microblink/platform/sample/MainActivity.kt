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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.microblink.platform.sample.transaction.TransactionClient
import com.microblink.platform.sample.ui.theme.MicroblinkPlatformTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroblinkPlatformTheme {
                val scope = rememberCoroutineScope()
                var creatingTransaction by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            enabled = !creatingTransaction,
                            onClick = {
                                scope.launch {
                                    creatingTransaction = true
                                    try {
                                        startIdentityVerification(TransactionClient.createTransaction())
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        showToast(e.message ?: "Creating the transaction failed")
                                    } finally {
                                        creatingTransaction = false
                                    }
                                }
                            }) {
                            Text(text = if (creatingTransaction) "Creating the transaction…" else "Start Identity Verification")
                        }
                    }
                }
            }
        }
    }

    /**
     * Starts the verification of the transaction [sdkConfiguration] belongs to.
     *
     * In production, your backend creates the transaction with your service account, along with
     * the user's consent, and sends the app the sdkConfiguration from its response. Here
     * TransactionClient stands in for your backend, so you can try the SDK without one: never
     * create the transaction in a released app, as that ships your service account's secret
     * with it. A sdkConfiguration is valid for one transaction only, so get a new one per
     * verification.
     */
    private fun startIdentityVerification(sdkConfiguration: String) {
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
