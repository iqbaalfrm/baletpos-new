# BaletPOS Web Admin Keuangan

Web dashboard untuk role Admin Keuangan. Bisa di-deploy ke Vercel dengan root directory `web-admin`.

Web ini bisa membaca database yang sama dengan desktop jika desktop BaletPOS dijalankan memakai mode Supabase/PostgreSQL.

## Modul Web

Disesuaikan dengan modul desktop:

- Dashboard
- Produk dan Stok
- Pembelian
- Supplier
- Retur / Mutasi
- Biaya
- Penjualan
- Transaksi
- Laba Rugi
- Stock Opname
- Rekonsiliasi Kas
- Setting

Mode tulis ke DB yang sudah aktif:

- Tambah produk
- Tambah supplier
- Stock opname / adjustment stok

Modul lain sudah tampil live dari database yang sama dan bisa diperluas menjadi form edit penuh.

## Deploy Vercel

1. Import repository ke Vercel.
2. Set **Root Directory** ke `web-admin`.
3. Set **Framework Preset** ke `Other`.
4. Build command dikosongkan.
5. Output directory dikosongkan.
6. Set environment variable Vercel:

```text
DATABASE_URL=postgresql://postgres.PROJECT_REF:PASSWORD@HOST:PORT/postgres?sslmode=require
```

Gunakan database Supabase/PostgreSQL yang sama dengan desktop. Desktop memakai format JDBC:

```text
BALETPOS_DB_URL=jdbc:postgresql://HOST:PORT/postgres?sslmode=require
BALETPOS_DB_USER=postgres.PROJECT_REF
BALETPOS_DB_PASSWORD=PASSWORD
```

Vercel API Node memakai format PostgreSQL URI biasa di `DATABASE_URL`.

## Local Preview

Buka `index.html` langsung di browser.

Jika `DATABASE_URL` belum diset atau API gagal, UI akan fallback ke data dummy di `app.js` untuk preview.

Login produksi memakai user tabel `users` dengan role `ADMIN_KEUANGAN`, contoh default desktop:

```text
adminkeuangan / keuangan123
```

Login demo fallback hanya aktif saat preview lokal tanpa API:

```text
finance / finance123
```
