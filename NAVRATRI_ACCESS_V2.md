# Navratri Access Platform V2

V2 converts the V1 scanner MVP into an event-operations product.

## Major V2 changes

- Redesigned Laravel admin Command Center with live inside count, capacity %, gate/device health, pre-print stock, and fraud feed.
- Pre-print inventory batches: physical QR cards are generated and printed before event day.
- Printed cards remain `INVENTORY`; they become valid only when a `HANDOVER` scanner activates them while staff gives the card to the visitor.
- Signed QR payload now contains `activation_required` so missing local-pack data cannot accidentally turn a pre-print QR into an active dynamic ticket.
- ENTRY -> INSIDE -> EXIT -> OUTSIDE -> RE-ENTRY state flow retained across selected multi-day entitlements.
- Pass-level people count, identity protection level, access zones, re-entry limits, and cooldown configuration.
- Venue capacity and warning thresholds.
- Device gate labels, revoke/enable controls, mesh-health heartbeat and peer counts.
- Nearby P2P_CLUSTER coordinator election using lowest active device ID.
- Isolated-scanner blocking and short distributed claim arbitration for duplicate scans.
- Multilingual / themed Android scanner UI and HANDOVER mode.
- Denied scan attempts are queued into the audit feed when the ticket is known.
- Offline scan queue + later cloud reconciliation.

## Printing model

No printer is required on event night. Generate a batch in advance, print all signed QR cards, seal/assign the stock to a counter, then activate each card only during physical handover. Unused or stolen pre-printed cards are rejected at ENTRY as `HANDOVER REQUIRED`.

## UI previews in the handoff ZIP

- `docs/ui-preview/admin-dashboard.png`
- `docs/ui-preview/mobile-scanner.png`
- `docs/ui-preview/mobile-result.png`

## Validation

- 51 PHP source files passed `php -l` syntax validation.
- V2 ZIP archive integrity test passed.
- Android source was updated to V2. Full Android dependency-resolved build should run in CI / Android Studio because the local execution environment did not include the Android Gradle SDK dependency set.

## Package checksum

`navratri-access-platform-v2.zip`

SHA-256: `54ea3176f46402c1957b16c487d6f6cd87275cbd80c96808d1e592cb257e4fb3`

## Security boundary

A completely serverless offline system cannot guarantee strict global duplicate prevention while scanners are network-partitioned. V2 therefore blocks isolated scanners when configured, uses peer claim arbitration, replicates committed states, elects a coordinator for health/operations visibility, and keeps audit logs. Strict linearizability would require a shared local or online authority.
