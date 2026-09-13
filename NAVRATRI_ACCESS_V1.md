# Navratri Access Platform v1

A complete source handoff was generated in this ChatGPT session as `navratri-access-platform-v1.zip`.

## Architecture

- Laravel 13 / PHP 8.3+ admin panel and device API
- public customer booking flow with pending/paid lifecycle
- HMAC-signed payment webhook; no browser-side payment-success trust
- Android Kotlin / Jetpack Compose scanner
- 15-digit visible ticket IDs + Ed25519-signed QR tokens
- offline Room/SQLite ticket database
- multi-day passes using one QR across selected event days
- per-day OUTSIDE -> INSIDE -> OUTSIDE state machine
- mandatory EXIT before re-entry
- configurable re-entry limits and cooldown
- Google Nearby Connections `P2P_CLUSTER` scanner-to-scanner synchronization
- HMAC authenticated peer messages
- short distributed claim window to reduce same-second duplicate scans
- pre-signed offline inventory pool for counter sales without storing private signing keys on Android
- themed scanner templates configurable from admin
- post-event cloud scan reconciliation

## Generated package checksum

SHA-256: `c76fdf97432d73bed53442946d5052445fdafe392e3466b0d311bd1908e94aa7`

## Operational security note

No fully offline serverless topology can provide strict global duplicate prevention during a network partition. The Android app therefore exposes mesh health, supports an isolated-scanner blocking policy, performs peer claim arbitration, and keeps an immutable audit log. If strict linearizability is ever required, a common local or online authority must be added.

## Payment integration note

The source is gateway-neutral. Before production, connect the webhook contract to the selected provider (for example Razorpay, PhonePe or Cashfree) and configure provider credentials. Paid bookings issue tickets only after a server-verified confirmation.

## Current handoff status

Backend PHP files passed local `php -l` syntax validation and the final ZIP passed archive integrity validation. Android source is structured for AGP 9.4 / compileSdk 37 / minSdk 28 and GitHub CI is included in the generated ZIP. The local execution environment did not contain an Android SDK or Composer dependency cache, so full dependency-resolved APK/Laravel runtime builds should be run in CI or the developer workstation.
