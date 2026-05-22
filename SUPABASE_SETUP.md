# Supabase Setup BaletPOS

BaletPOS tetap memakai SQLite lokal secara default. Mode Supabase/PostgreSQL aktif hanya jika `BALETPOS_DB_URL` diset.

## 1. Buat Project Supabase

1. Buat project baru di Supabase.
2. Buka `Project Settings > Database`.
3. Ambil connection string JDBC dari database PostgreSQL.
4. Pastikan connection string memakai SSL.

Contoh format:

```text
jdbc:postgresql://aws-xxx.pooler.supabase.com:6543/postgres?sslmode=require
```

Project trial saat ini terdeteksi:

```text
project_ref=wlubhewrckakwghmcsxi
region=Northeast Asia (Seoul)
pooler_host=aws-1-ap-northeast-2.pooler.supabase.com
```

Untuk Vercel/serverless gunakan Transaction Pooler:

```text
DATABASE_URL=postgresql://postgres.wlubhewrckakwghmcsxi:PASSWORD@aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?sslmode=require
```

Untuk desktop Java gunakan JDBC ke pooler yang sama:

```text
BALETPOS_DB_URL=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
BALETPOS_DB_USER=postgres.wlubhewrckakwghmcsxi
BALETPOS_DB_PASSWORD=PASSWORD
```

Atau simpan permanen ke konfigurasi lokal desktop:

```powershell
.\scripts\configure-supabase-desktop.ps1 -DbPassword "PASSWORD"
```

Migrasi data SQLite lokal ke Supabase:

```powershell
cd web-admin
npm install
$env:DATABASE_URL="postgresql://postgres.wlubhewrckakwghmcsxi:PASSWORD@aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?sslmode=require"
$env:CONFIRM_MIGRATE="YES"
npm run migrate:sqlite
```

Migrasi ini melakukan `TRUNCATE ... CASCADE` di database Supabase lalu memasukkan data dari
`C:\Users\<user>\.baletpos\baletpos.db`. Jalankan hanya untuk database trial/target yang memang siap ditimpa.

## 2. Set Environment Variable

PowerShell sementara untuk sesi terminal saat ini:

```powershell
$env:BALETPOS_DB_URL="jdbc:postgresql://HOST:PORT/postgres?sslmode=require"
$env:BALETPOS_DB_USER="postgres.PROJECT_REF"
$env:BALETPOS_DB_PASSWORD="PASSWORD_DATABASE_SUPABASE"
.\gradlew.bat run
```

Alternatif tanpa env var:

```powershell
.\gradlew.bat run -Dbaletpos.db.url="jdbc:postgresql://HOST:PORT/postgres?sslmode=require" -Dbaletpos.db.user="postgres.PROJECT_REF" -Dbaletpos.db.password="PASSWORD_DATABASE_SUPABASE"
```

## 3. First Run

Saat pertama kali jalan dengan `BALETPOS_DB_URL`, aplikasi akan:

1. Membuat tabel dari `src/main/resources/sql/schema_postgres.sql`.
2. Menjalankan seed data dari `src/main/resources/sql/seed.sql`.
3. Mengatur ulang user trial aktif:
   - `kasir` / `kasir123`
   - `admintoko` / `toko123`
   - `adminkeuangan` / `keuangan123`
4. Menonaktifkan akun legacy `admin`, `kasir2`, dan `teknisi`.

## 4. Backup Otomatis Harian ke Google Drive

Backup otomatis aktif secara default. Aplikasi membuat file ZIP berisi CSV semua tabel ke:

```text
C:\Users\<user>\.baletpos\backups\
```

Jam backup default: `23:00`.

Untuk upload ke Google Drive, buat Google Cloud service account, aktifkan Google Drive API, lalu share folder Drive tujuan ke email service account tersebut. Setelah itu set env var:

```powershell
$env:BALETPOS_BACKUP_DRIVE_SERVICE_ACCOUNT_PATH="C:\path\service-account.json"
$env:BALETPOS_BACKUP_DRIVE_FOLDER_ID="GOOGLE_DRIVE_FOLDER_ID"
$env:BALETPOS_BACKUP_TIME="23:00"
```

Alternatif kalau tidak mau pakai path file:

```powershell
$env:BALETPOS_BACKUP_DRIVE_SERVICE_ACCOUNT_JSON="<isi-json-service-account>"
```

Konfigurasi opsional:

```powershell
$env:BALETPOS_BACKUP_ENABLED="true"
$env:BALETPOS_BACKUP_DIR="D:\BaletPOSBackups"
```

Jika credential Drive belum diset, backup tetap dibuat lokal dan upload Drive dilewati.

## 5. Catatan Produksi

- Jangan commit password Supabase ke repo.
- Pakai koneksi pooler Supabase untuk aplikasi desktop.
- Backup tetap wajib, walaupun memakai cloud.
- Free tier cukup untuk awal, tapi proposal harus menyebutkan bahwa batas gratis mengikuti kebijakan Supabase.
