# Navratri Access Platform V2

Offline-first event access control for high-volume Navratri / concert / festival gates.

## V2 operating model

1. Online bookings issue Ed25519-signed QR tickets.
2. Bulk physical QR cards are generated and **pre-printed before event day**.
3. Pre-printed cards stay `INVENTORY` and are invalid at entry.
4. A `HANDOVER` scanner activates a physical card only when staff gives it to a visitor.
5. Entry scanners enforce `OUTSIDE -> INSIDE`; another entry is blocked until an EXIT scan returns the ticket to `OUTSIDE`.
6. The same QR can hold 2–11+ selected event-day entitlements. Every event day has a separate state.
7. Android scanners verify tickets from Room/SQLite and synchronize live state over Google Nearby Connections `P2P_CLUSTER`; internet is not required for live scanning.

## V2 features

- Laravel 13 admin / API
- Android Kotlin + Jetpack Compose scanner
- 15-digit public ticket codes
- Ed25519 signed QR payloads
- signed `activation_required` flag for pre-print inventory
- pre-print inventory batches with print / physical assignment workflow
- HANDOVER mode: inventory becomes active only at visitor handover
- ENTRY / EXIT / RE-ENTRY enforcement
- multi-day same-QR passes
- pass-level people count, identity mode, access zones and re-entry rules
- venue capacity checks
- scanner isolation blocking
- deterministic peer claim window for concurrent duplicate scans
- automatic coordinator election (lowest active device ID)
- device revoke / enable controls
- device peer-health heartbeat
- themed multilingual scanner UI
- fraud / denied-scan audit feed
- offline scan queue + later cloud reconciliation
- server-verified payment lifecycle scaffolding

## Pre-print security rule

A printed QR is **not automatically a valid ticket**. Inventory QR payloads are cryptographically signed with `activation_required=true`. Even if such a QR is presented to a scanner that did not receive that ticket in its event pack, the Android app treats it as inventory, not as a dynamic active ticket. The HANDOVER workflow is required before entry.

## Offline consistency boundary

No serverless offline system can guarantee strict global duplicate prevention if scanners are physically network-partitioned. V2 mitigates this by blocking isolated scanners when configured, using a short distributed claim window, replicating committed entry/exit state over Nearby, exposing mesh health, and preserving an audit log. If strict linearizability is ever required, add a shared local or online authority.

## Current build target

- Android compileSdk / targetSdk 37
- Google Play services Nearby 19.5.0
- Room 2.8.5
- CameraX 1.6.2
- Compose UI 1.12.1 / Material3 1.4.0
- Laravel 13 / PHP 8.3+

The authoritative frozen ZIP checksum is kept in the branch-level `NAVRATRI_ACCESS_V2.md` handoff manifest so the archive README does not need to checksum itself.
