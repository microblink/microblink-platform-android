# Migrating from 1.x to 2.0.0

2.0.0 changes how a verification starts: your backend creates the transaction, and the SDK only runs it. There is no proxy and no consent in the app anymore. Most of the public API was renamed along the way. This guide lists every change an integration needs.

## Requirements

| | 1.x | 2.0.0 |
| --- | --- | --- |
| minSdk | 24 | **26** |
| Android Gradle Plugin | 8.x | **9.1** or newer |
| compileSdk | 36 | **37** or newer |
| ABIs | `armeabi-v7a`, `arm64-v8a` | unchanged |

Compose 1.12 and Material 3 1.5, which the SDK's UI uses, require AGP 9.1 and compileSdk 37.

## Dependencies

1.x brought BlinkID Verify and BlinkCard with it. 2.0.0 brings no scanner SDK: add the ones your workflows use, with versions from the new BOM.

```kotlin
// 1.x
implementation("com.microblink:microblink-platform:1.10.3")

// 2.0.0
implementation(platform("com.microblink:microblink-platform-bom:2.0.0"))
implementation("com.microblink:microblink-platform")
implementation("com.microblink:blinkid-verify-ux") // Scan ID, Scan & Verify ID
implementation("com.microblink:blinkcard-ux")      // Scan Payment Card
implementation("com.microblink:biometrics-ux")     // face capture
```

A workflow step whose scanner SDK is missing fails with `EngineError.MissingScannerSdk`.

## Starting a verification

### The transaction comes from your backend

In 1.x the SDK started the transaction through your proxy, from a `workflowId`, proxy settings and the user's consent. In 2.0.0 your backend creates the transaction with the Microblink Platform API, records the consent there, and passes the response's `sdkConfiguration` to the app. Your proxy is no longer needed.

These 1.x types are gone, with nothing in their place on the app side:

* `MicroblinkPlatformServiceSettings` (`workflowId`, `proxySettings`, `consent`, `correlationId`)
* `MicroblinkPlatformProxySettings`
* `MicroblinkPlatformConsent`, and the SDK's consent screen

Set the correlation ID and the consent when your backend creates the transaction.

### Configuration

```kotlin
// 1.x
MicroblinkPlatform.startVerification(
    this,
    MicroblinkPlatformConfig(
        mbpResultListener = resultListener,
        mbpServiceSettings = MicroblinkPlatformServiceSettings(
            workflowId = "your_workflow_id",
            proxySettings = MicroblinkPlatformProxySettings(url = "your_url"),
            consent = MicroblinkPlatformConsent(userId = "user_id", isProcessingStoringAllowed = true),
        ),
        mbpUiSettings = MicroblinkPlatformUiSettings(),
        mbpCardScanResultListener = cardListener,
    ),
)

// 2.0.0
MicroblinkPlatform.startVerification(
    this,
    MicroblinkPlatformConfiguration(
        engine = EngineConfiguration(ServiceSettings(sdkConfiguration = sdkConfiguration)),
        resultListener = resultListener,
        ui = UiConfiguration(),
        cardScanResultListener = cardListener,
    ),
)
```

| 1.x | 2.0.0 |
| --- | --- |
| `MicroblinkPlatformConfig` | `MicroblinkPlatformConfiguration` |
| `mbpResultListener` | `resultListener` |
| `mbpServiceSettings` | `engine` (`EngineConfiguration(ServiceSettings(sdkConfiguration))`) |
| `mbpUiSettings` (`MicroblinkPlatformUiSettings`) | `ui` (`UiConfiguration`) |
| `mbpCardScanResultListener` | `cardScanResultListener` |

`startVerification` now throws `IllegalStateException` if a verification is already running.

## Results

`MicroblinkPlatformResultListener` keeps its name, but its functions take the engine's types (package `com.microblink.platform.engine`), and it has a new function for errors.

| 1.x | 2.0.0 |
| --- | --- |
| `onVerificationFinished(mbpResult: MicroblinkPlatformResult)` | `onVerificationFinished(outcome: SessionOutcome.Verification)` |
| `MicroblinkPlatformResult.FinishedState.Success` | `VerificationState.Accept` |
| `MicroblinkPlatformResult.FinishedState.Failure` | `VerificationState.Reject` |
| `MicroblinkPlatformResult.FinishedState.Review` | `VerificationState.Review` |
| `onVerificationCanceled(cancelState: MicroblinkPlatformCancelState)` | `onVerificationCanceled(outcome: SessionOutcome.Canceled)` |
| `CancelReason.UserCanceled` | `CancelReason.UserCanceled` |
| `CancelReason.ConsentDenied` | removed: the SDK no longer asks for consent |
| — | **new:** `onVerificationFailed(transactionId: String?, error: EngineError)` |

`onVerificationFailed` is called when the verification ends on an error the user can't retry, such as `EngineError.NoNetwork`, `EngineError.MissingScannerSdk` or `EngineError.UnsupportedWorkflowVersion`. Every implementation has to add it.

### Card scan results

`MicroblinkPlatformCardScanResultListener.onCardScanned` now receives `com.microblink.platform.engine.CardScanResult`. Its fields are the same, and `expiryDate` is a `CardDate` (1.x: `Date`) with the same `day`, `month` and `year`. Its `toString()` is masked, so logging it no longer leaks the card.

## Workflow versions

The SDK now checks which SDK version the transaction's workflow was built for, and refuses one built for an older major.minor version: SDK 2.0.0 refuses a workflow built for 1.x. The verification then ends with `EngineError.UnsupportedWorkflowVersion`. Update your workflows to 2.0 before moving the app to 2.0.0.

## Customization

| 1.x (`MicroblinkPlatformUiSettings`) | 2.0.0 (`UiConfiguration`) |
| --- | --- |
| `colorScheme` | `colorScheme`, which now also colors the scanner and face screens |
| `mbpImages` | `images` (`MicroblinkPlatformImages`, same fields) |
| `buttonShape = ButtonShape(shape)` | `buttonShape = shape`, now applied to every button and dialog button |
| `typography` | `typography` (`MicroblinkPlatformTypography`, new styles, see below) |
| `documentScanningTypography` (`ParcelableUiTypography`) | `scannerTypography` (`ScannerTypography`), for the document, card and face screens |

`MicroblinkPlatformTypography` replaced its per-screen styles (`homeTitle`, …) with 12 styles, one per kind of text: `heading`, `description`, `subtitle`, `homeStepTitle`, `homeStepDescription`, `info`, `fieldTitle`, `fieldBody`, `fieldDescription`, `titleProgressAndErrors`, `dialogTitle` and `button`. `MicroblinkPlatformTypography.default(fontFamily)` still changes only the font. The [Kotlin documentation](https://microblink.github.io/microblink-platform-android/microblink-platform/com.microblink.platform/-microblink-platform-typography/index.html) says where each style is used.

The SDK uses a light theme in 2.0.0, as 1.x did.

## Strings

Strings were added, removed and renamed. Check your overrides and translations against the new [`strings.xml`](strings/strings.xml): an override of a key that no longer exists is silently ignored.
