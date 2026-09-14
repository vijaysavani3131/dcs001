# eventmanagement.isavgo.com deployment

Target supplied by owner:
`/home/p7u0n6yuynp2/public_html/eventmanagement.isavgo.com`

## Requirements
- PHP 8.3+ with openssl, sodium, pdo_mysql, mbstring, tokenizer, xml, ctype, fileinfo
- Composer 2
- MySQL/MariaDB
- Apache mod_rewrite
- HTTPS certificate for `eventmanagement.isavgo.com`

## Recommended document-root layout
Best option: upload the `backend/` directory to the target folder, then set the subdomain Document Root to:
`/home/p7u0n6yuynp2/public_html/eventmanagement.isavgo.com/public`

If cPanel does not allow changing Document Root, keep it at the supplied target. The included root `.htaccess` internally forwards traffic to `public/` and blocks Laravel internals.

## Install
```bash
cd /home/p7u0n6yuynp2/public_html/eventmanagement.isavgo.com
composer install --no-dev --optimize-autoloader
cp .env.production.example .env
php artisan key:generate
```

Edit `.env` and fill `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `PAYMENT_WEBHOOK_SECRET`.

Create database/user in cPanel MySQL Databases, grant ALL privileges, then:
```bash
php artisan migrate --force
php artisan db:seed --force
php artisan storage:link || true
php artisan optimize:clear
php artisan config:cache
php artisan route:cache
php artisan view:cache
chmod -R 775 storage bootstrap/cache
```

## Verify
- Open `https://eventmanagement.isavgo.com/login`
- Log in with `ADMIN_EMAIL` / `ADMIN_PASSWORD` from `.env`
- Create Event -> event days -> pass -> scanner device
- Copy the one-time activation code shown for that device

## Android scanner activation
On first app launch:
- Backend URL: `https://eventmanagement.isavgo.com`
- Event ID: ID shown in admin URL/table
- Activation code: one-time device code from admin
- Grant Camera + Nearby/Bluetooth permissions
- Refresh the full event pack while internet is available
- Confirm STATUS shows ticket pack READY and mesh peers are visible

## Peak-event procedure
1. Five hours before opening, connect all scanners to internet and refresh the pack.
2. Confirm each scanner has correct role/gate and status SECURE.
3. Switch entry devices to MULTI during peak crowd.
4. Keep 3-6 QR phones/cards clearly separated in the marked scan zone.
5. Green GO -> release visitor. Red STOP -> divert to help desk and read displayed reason.
6. Exit devices remain in EXIT mode so re-entry is impossible until exit is recorded.
7. Internet can be removed after pack sync; keep Nearby Wi-Fi/Bluetooth enabled.

## Production caution
Do not run a peak event until the exact phone model has passed a real-lighting stress test with four simultaneous QRs, then six. A fully partitioned scanner cannot know what another isolated scanner accepted; keep `block_isolated_scanners` enabled.
