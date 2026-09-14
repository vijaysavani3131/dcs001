# Multi-QR Crowd Scan Mode

## Goal

High-throughput ENTRY mode for peak Navratri crowd windows. One Android camera can track multiple QR codes in the same live frame and draw an AR-style status overlay directly above each code.

## Visual behavior

Each detected QR receives a live bounding box:

- **GREEN / GO** — entry committed successfully.
- **RED / STOP** — rejected, with reason such as `EXIT REQUIRED`, `WRONG DAY`, `HANDOVER REQUIRED`, `BLOCKED`, `CAPACITY REACHED`, or `DUPLICATE RACE`.
- **AMBER / VERIFYING** — QR decoded and offline ticket + mesh claim is being checked.
- **AMBER / MOVE CLOSER** — a QR-like code is visible but ML Kit cannot decode it yet.

The result stays attached to the QR while the visitor holds it in the camera. A code is processed only once while continuously visible; it becomes eligible for a new scan only after leaving the frame for roughly 1.2 seconds. This prevents a successful green ENTRY from immediately becoming `ALREADY INSIDE` on the next video frame.

## Camera pipeline

- CameraX `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`
- Bundled ML Kit barcode scanner
- QR format only for lower latency
- `enableAllPotentialBarcodes()` for undecoded/potential QR boxes
- Normalized bounding boxes rendered as Compose overlays
- Maximum eight observations tracked per frame
- Recommended operating target: **3–6 clearly separated QR codes per camera**

## Security pipeline for every decoded QR

1. Ed25519 signature verify offline.
2. Event and active-day entitlement check.
3. Pre-print HANDOVER status check.
4. Ticket state check (`OUTSIDE` / `INSIDE`).
5. Re-entry limit/cooldown check.
6. Venue capacity check.
7. Scanner mesh health check.
8. Distributed claim to reduce concurrent duplicate acceptance.
9. Local Room transaction commits ENTRY.
10. Commit is broadcast over Nearby and queued for later cloud audit sync.

## Recommended physical gate layout

Do not treat one phone as a camera for an entire uncontrolled crowd. Create a controlled scanning zone per lane.

Suggested lane:

```text
QUEUE -> QR READY LINE -> MULTI-SCAN ZONE -> SECURITY RELEASE
                         [1] [2] [3]
                         [4] [5] [6]
                            CAMERA
```

Mark 4–6 QR presentation positions so visitors do not overlap one another's phones. Keep screens bright and QR codes large. A fixed/mounted scanner device gives better focus and consistent geometry than a guard waving a phone.

## Practical performance target

The feature is designed to reduce the human pause between individual scans, not to promise unlimited simultaneous decoding. Actual capacity depends on phone camera quality, lighting, QR physical size, screen brightness, distance, motion, and mesh claim latency.

Start production testing at four simultaneous QR codes. Increase to six only after device-specific stress tests. The source currently caps tracking at eight observations to prevent UI/processing overload.

## Files

- `android/app/src/main/java/com/navratri/access/MultiQrCamera.kt`
- `android/app/src/main/java/com/navratri/access/MainViewModel.kt`
- `android/app/src/main/java/com/navratri/access/NavratriUi.kt`

## Branch

`feature/navratri-access-platform-v2-multiqr`
