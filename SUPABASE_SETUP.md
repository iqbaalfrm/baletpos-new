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
3. Mengatur ulang user default:
   - `admin` / `admin123`
   - `kasir` / `kasir123`

## 4. Catatan Produksi

- Jangan commit password Supabase ke repo.
- Pakai koneksi pooler Supabase untuk aplikasi desktop.
- Backup tetap wajib, walaupun memakai cloud.
- Free tier cukup untuk awal, tapi proposal harus menyebutkan bahwa batas gratis mengikuti kebijakan Supabase.
