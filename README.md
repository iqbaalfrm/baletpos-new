# BaletPOS - Sistem Point of Sale untuk Toko Komputer

## Deskripsi
BaletPOS adalah aplikasi desktop Point of Sale (POS) yang dibangun menggunakan **Java 17+**, **JavaFX**, dan **SQLite** untuk kebutuhan toko komputer. Aplikasi ini bersifat **offline-first** dengan semua data tersimpan secara lokal.

## Fitur Utama

### 🛒 Transaksi (POS)
- Kasir dengan input keyboard-friendly
- Pencarian produk berdasarkan SKU atau nama
- Checkout dengan validasi stok otomatis
- Multiple payment method (CASH, TRANSFER, QRIS)
- Cetak struk PDF otomatis
- VOID transaksi (khusus ADMIN)

### 📦 Pembelian (Purchase Order)
- Input pembelian dari supplier
- Auto-generate nomor PO (format: PO-YYYYMMDD-0001)
- Update stok otomatis
- Opsi update HPP produk saat pembelian

### 🔄 Retur & Mutasi
- Retur penjualan dengan referensi invoice
- Pengembalian stok otomatis
- Pencatatan alasan retur

### 💰 Biaya Operasional
- Master kode biaya
- Input biaya dengan auto-generate nomor (EXP-YYYYMMDD-0001)
- Laporan biaya per periode

### 📊 Laporan (Reports)
- **Laporan Penjualan**: Per kategori (Laptop/Peripheral/Service)
- **Laporan Aset**: Inventory valuation (nilai HPP dan nilai jual)
- **Laporan Laba Rugi**: Profit & Loss dengan breakdown lengkap:
  - Pendapatan kotor & bersih (setelah retur)
  - HPP kotor & bersih (setelah reversal)
  - Laba kotor & margin kotor
  - Total biaya operasional
  - Laba/Rugi bersih & margin bersih

### 👤 Otoritas & Keamanan
- Role-Based Access Control (ADMIN / KASIR)
- Audit log untuk aksi penting
- Password hashing menggunakan BCrypt

### 📋 Master Data
- Produk dengan kategori, brand, HPP, margin, harga jual
- Supplier dengan data kontak
- Kode biaya operasional
- Customer

### 📦 Inventory & Stock
- Opening stock per periode
- Stock movement tracking
- Real-time stock update
- Low stock alert

## Teknologi yang Digunakan

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Java 17+ |
| UI | JavaFX 21 + FXML + CSS |
| Theme | AtlantaFX NordLight (Light Only) |
| Controls | ControlsFX (Toast Notifications) |
| Icons | Ikonli FontAwesome5 |
| Database | SQLite (offline-first) |
| ORM | JDBC + DAO Pattern |
| Password | jBCrypt |
| PDF | iText 7 |
| Image | Thumbnailator (resize/compress) |
| Logging | SLF4J + Logback |
| Build | Gradle |

## UI Minimalis Modern

### Prinsip Desain
- 🎨 **Light Mode Only** - Tidak ada dark mode, tampilan clean dan konsisten
- 📐 **Whitespace Lega** - Layout card-based dengan spacing yang baik
- 🔤 **Typography Rapi** - Hierarki visual yang jelas
- 💎 **Komponen Konsisten** - Button, input, table dengan style seragam

### 📷 Foto Produk
- Upload foto produk (JPG, PNG, WebP)
- Auto resize & compress ke max 800px
- Thumbnail 48-64px di tabel produk
- Preview besar di detail panel
- Placeholder otomatis jika foto tidak ada

### 🏠 Layout Shell
- **AppBar (56px)**: Judul halaman + user badge + logout
- **Sidebar (220px)**: Menu navigasi minimal dengan icon
- **Content Area**: FXML halaman per modul

### ⌨️ Keyboard Shortcuts
| Shortcut | Fungsi |
|----------|--------|
| F2 | Fokus ke search field (POS) |
| F4 | Checkout langsung (POS) |
| ESC | Clear cart (POS) |
| F11 | Toggle Fullscreen |
| Enter | Tambah produk pertama |

## Struktur Proyek

```
baletpos/
├── src/
│   ├── main/
│   │   ├── java/com/baletpos/
│   │   │   ├── config/          # Database configuration
│   │   │   ├── controller/      # JavaFX controllers
│   │   │   ├── dao/             # Data Access Objects
│   │   │   ├── model/           # Entity models
│   │   │   ├── service/         # Business logic services
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── fxml/            # JavaFX FXML views
│   │       ├── css/             # Stylesheets
│   │       └── sql/             # Schema & Seed data
│   └── test/
│       └── java/com/baletpos/   # Unit tests
├── build.gradle                  # Gradle build config
└── README.md
```

## Cara Menjalankan

### Prasyarat
- Java 17 atau lebih baru
- Gradle (opsional, sudah include wrapper)

### Menjalankan Aplikasi
```bash
# Clone repository
git clone <repository-url>
cd baletpos

# Build dan run
./gradlew run

# Atau di Windows
gradlew.bat run
```

### Login Default
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| kasir | kasir123 | KASIR |

## Database

Database SQLite akan dibuat otomatis di:
- **Windows**: `C:\Users\<user>\baletpos\baletpos.db`
- **macOS/Linux**: `~/baletpos/baletpos.db`

### Schema
Lihat file `src/main/resources/sql/schema.sql` untuk struktur tabel lengkap.

### Seed Data
Lihat file `src/main/resources/sql/seed.sql` untuk data awal.

## Format Penomoran (Nomerator)

| Jenis | Format | Contoh |
|-------|--------|--------|
| Invoice POS | INV-YYYYMMDD-XXXX | INV-20260107-0001 |
| Purchase Order | PO-YYYYMMDD-XXXX | PO-20260107-0001 |
| Expense | EXP-YYYYMMDD-XXXX | EXP-20260107-0001 |
| Sales Return | RT-YYYYMMDD-XXXX | RT-20260107-0001 |

Semua nomor akan **reset setiap hari** dimulai dari 0001.

## Alur Kerja

### 1. Alur Penjualan (Kasir)
```
Buka POS → Cari produk → Tambah ke keranjang → Input pembayaran → Checkout → Cetak struk
```

### 2. Alur Pembelian (Admin)
```
Buka Pembelian → Pilih supplier → Tambah produk → Input HPP → Simpan → Stok bertambah
```

### 3. Alur Retur Penjualan (Admin)
```
Buka Retur → Cari invoice → Pilih item diretur → Input alasan → Simpan → Stok kembali
```

## Unit Tests

Menjalankan unit tests:
```bash
./gradlew test
```

Test mencakup:
- Format nomerator (Invoice, PO, Expense, Return)
- Perhitungan laba rugi
- Integritas stock movement

## Pengembangan Selanjutnya

Fitur yang sudah diimplementasikan:
- [x] Export laporan ke PDF (Laba Rugi, Penjualan per Kategori, Aset)
- [x] Dashboard analytics dengan grafik (PieChart & BarChart)

Fitur yang masih dalam pengembangan:
- [ ] Export laporan ke Excel
- [ ] Barcode scanner integration
- [ ] Multi-user concurrent access
- [ ] Cloud sync (opsional)

### Export PDF
Laporan PDF akan disimpan di folder:
- **Windows**: `C:\Users\<user>\baletpos\reports\`
- **macOS/Linux**: `~/baletpos/reports/`

### Dashboard Analytics
Dashboard menampilkan:
- **Pie Chart**: Distribusi penjualan per kategori (Laptop/Peripheral/Service) bulan ini
- **Bar Chart**: Trend penjualan 7 hari terakhir (Omzet & Laba Kotor)

## Lisensi
MIT License

## Kontributor
Dikembangkan sebagai proyek skripsi.

---
**BaletPOS v1.0.0** - Sistem POS Toko Komputer
