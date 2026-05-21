const currency = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0,
});

const demoCredentials = {
  username: "finance",
  password: "finance123",
};

let liveMode = false;

function isLocalPreview() {
  return ["localhost", "127.0.0.1", ""].includes(window.location.hostname);
}

function setConnectionStatus(text, state = "warning") {
  const target = document.querySelector("#connection-status");
  if (!target) return;
  target.textContent = text;
  target.classList.toggle("live", state === "live");
  target.classList.toggle("error", state === "error");
}

const chartData = [
  { day: "Sen", revenue: 17.5, profit: 4.3 },
  { day: "Sel", revenue: 22.1, profit: 5.6 },
  { day: "Rab", revenue: 18.8, profit: 4.7 },
  { day: "Kam", revenue: 25.4, profit: 6.8 },
  { day: "Jum", revenue: 31.2, profit: 8.1 },
  { day: "Sab", revenue: 28.7, profit: 7.4 },
  { day: "Min", revenue: 20.6, profit: 5.2 },
];

const transactions = [
  { invoice: "INV-20260521-0008", time: "14:20", method: "QRIS", total: 4250000, status: "Berhasil" },
  { invoice: "INV-20260521-0007", time: "13:48", method: "Transfer BCA", total: 12100000, status: "Berhasil" },
  { invoice: "INV-20260521-0006", time: "13:12", method: "Cash", total: 850000, status: "Pending" },
  { invoice: "INV-20260521-0005", time: "12:44", method: "Akulaku", total: 7400000, status: "Review" },
  { invoice: "INV-20260521-0004", time: "11:39", method: "QRIS", total: 2100000, status: "Berhasil" },
];

const expenses = [
  { code: "EXP-202605-018", category: "Operasional", note: "Internet toko Mei", total: 650000, status: "Disetujui" },
  { code: "EXP-202605-017", category: "Logistik", note: "Ongkir supplier SSD", total: 380000, status: "Pending" },
  { code: "EXP-202605-016", category: "Marketing", note: "Iklan marketplace", total: 1250000, status: "Review" },
  { code: "EXP-202605-015", category: "Maintenance", note: "Perbaikan printer nota", total: 475000, status: "Disetujui" },
];

const payments = [
  { name: "QRIS", amount: 42850000, pct: 34 },
  { name: "Transfer", amount: 38400000, pct: 30 },
  { name: "Cash", amount: 27100000, pct: 22 },
  { name: "Kredit", amount: 18100000, pct: 14 },
];

const approvals = [
  { title: "Ongkir supplier SSD", meta: "Logistik - Rp 380.000", status: "Pending" },
  { title: "Iklan marketplace", meta: "Marketing - Rp 1.250.000", status: "Review" },
  { title: "Refund selisih transfer", meta: "Penjualan - Rp 125.000", status: "Pending" },
];

const profitRows = [
  ["Penjualan Kotor", 134900000],
  ["Retur Penjualan", -8450000],
  ["Pendapatan Bersih", 126450000, "total"],
  ["HPP Penjualan", -94670000],
  ["Gross Profit", 31780000, "total"],
  ["Biaya Operasional", -12380000],
  ["Laba Bersih", 19400000, "total"],
];

let stockItems = [
  { sku: "VGA-NV-4090-A", name: "RTX 4090 24GB", system: 4, physical: 4, hpp: 28500000, status: "Aman", note: "" },
  { sku: "CPU-INT-149-K", name: "Core i9 14900K", system: 6, physical: 5, hpp: 8750000, status: "Selisih", note: "Satu unit display" },
  { sku: "SSD-SAM-990-2", name: "Samsung 990 Pro 2TB", system: 12, physical: 12, hpp: 2100000, status: "Aman", note: "" },
  { sku: "MS-LOG-GPRO", name: "Logitech G Pro X", system: 2, physical: 2, hpp: 1320000, status: "Kritis", note: "Di bawah minimum" },
  { sku: "RAM-CRU-D5", name: "Crucial DDR5 16GB", system: 1, physical: 1, hpp: 760000, status: "Kritis", note: "Segera restock" },
  { sku: "HDD-WD-1TB", name: "WD Blue 1TB HDD", system: 5, physical: 4, hpp: 540000, status: "Selisih", note: "Cek rak gudang" },
];

let products = [];
let purchases = [];
let suppliers = [];
let returnsData = [];
let movements = [];
let settingsData = [];

function badge(status) {
  const normalized = status.toLowerCase();
  const cls = normalized.includes("berhasil") || normalized.includes("disetujui") || normalized.includes("completed")
    || normalized.includes("aman")
    ? "success"
    : normalized.includes("review") || normalized.includes("selisih")
      ? "review"
      : "pending";
  return `<span class="badge ${cls}">${status}</span>`;
}

function renderChart() {
  const target = document.querySelector("#cash-chart");
  const max = Math.max(...chartData.map((item) => item.revenue));
  target.innerHTML = chartData.map((item) => `
    <div class="bar-group" title="${item.day}: omzet ${item.revenue} juta, laba ${item.profit} juta">
      <div class="bars">
        <div class="bar revenue" style="height:${(item.revenue / max) * 100}%"></div>
        <div class="bar profit" style="height:${(item.profit / max) * 100}%"></div>
      </div>
      <div class="bar-label">${item.day}</div>
    </div>
  `).join("");
}

async function apiGet(path) {
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(`API ${path} gagal: ${response.status}`);
  }
  return response.json();
}

async function apiPost(path, body) {
  const response = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.error || `API ${path} gagal: ${response.status}`);
  }
  return response.json();
}

function currentPeriod() {
  return document.querySelector("#period-select").value || "month";
}

function normalizeStatus(status) {
  if (status === "COMPLETED") return "Berhasil";
  if (status === "VOIDED") return "Dibatalkan";
  if (status === "RETURNED") return "Retur";
  return status || "Pending";
}

function renderTransactions() {
  const rows = transactions.map((item) => `
    <tr>
      <td><strong>${item.invoice}</strong></td>
      <td>${item.time}</td>
      <td>${item.method}</td>
      <td class="right">${currency.format(item.total)}</td>
      <td>${badge(item.status)}</td>
    </tr>
  `).join("");
  document.querySelector("#transaction-table").innerHTML = rows;
  document.querySelector("#sales-table").innerHTML = transactions.map((item, index) => `
    <tr>
      <td><strong>${item.invoice}</strong></td>
      <td>${index % 2 === 0 ? "Rina" : "Admin Toko"}</td>
      <td>${item.method}</td>
      <td class="right">${currency.format(item.total)}</td>
      <td>${badge(item.status)}</td>
    </tr>
  `).join("");
  document.querySelector("#full-transaction-table").innerHTML = transactions.map((item, index) => `
    <tr>
      <td><strong>${item.invoice}</strong></td>
      <td>${item.time}</td>
      <td>${item.cashier || (index % 2 === 0 ? "Rina" : "Admin Toko")}</td>
      <td>${item.method}</td>
      <td class="right">${currency.format(item.total)}</td>
      <td>${badge(item.status)}</td>
    </tr>
  `).join("");
}

function renderExpenses() {
  document.querySelector("#expense-table").innerHTML = expenses.map((item) => `
    <tr>
      <td><strong>${item.code}</strong></td>
      <td>${item.category}</td>
      <td>${item.note}</td>
      <td class="right">${currency.format(item.total)}</td>
      <td>${badge(item.status)}</td>
    </tr>
  `).join("");
}

function renderPayments() {
  document.querySelector("#payment-list").innerHTML = payments.map((item) => `
    <div class="payment-item">
      <strong>${item.name}</strong>
      <span>${currency.format(item.amount)}</span>
      <div class="payment-track"><i style="width:${item.pct}%"></i></div>
    </div>
  `).join("");
}

function renderApprovals() {
  document.querySelector("#approval-list").innerHTML = approvals.map((item) => `
    <div class="approval-item">
      <strong>${item.title}</strong>
      <span>${item.meta}</span>
      ${badge(item.status)}
    </div>
  `).join("");
}

function renderStatement() {
  document.querySelector("#profit-statement").innerHTML = profitRows.map(([label, amount, type]) => `
    <div class="statement-row ${type === "total" ? "total" : ""}">
      <strong>${label}</strong>
      <span>${currency.format(amount)}</span>
    </div>
  `).join("");
}

function renderProducts() {
  document.querySelector("#product-table").innerHTML = products.map((item) => `
    <tr>
      <td><strong>${item.sku}</strong></td>
      <td>${item.name}</td>
      <td>${item.type}</td>
      <td class="right">${item.stock}</td>
      <td class="right">${currency.format(Number(item.selling_price || 0))}</td>
    </tr>
  `).join("");
}

function renderPurchases() {
  document.querySelector("#purchase-table").innerHTML = purchases.map((item) => `
    <tr>
      <td><strong>${item.number}</strong></td>
      <td>${new Date(item.date).toLocaleDateString("id-ID")}</td>
      <td>${item.supplier}</td>
      <td class="right">${currency.format(Number(item.total || 0))}</td>
      <td>${badge(item.status || "Completed")}</td>
    </tr>
  `).join("");
}

function renderSuppliers() {
  document.querySelector("#supplier-table").innerHTML = suppliers.map((item) => `
    <tr>
      <td><strong>${item.code}</strong></td>
      <td>${item.name}</td>
      <td>${item.contact || "-"}</td>
      <td>${item.phone || "-"}</td>
      <td>${item.email || "-"}</td>
    </tr>
  `).join("");
}

function renderReturns() {
  document.querySelector("#return-table").innerHTML = returnsData.map((item) => `
    <tr>
      <td><strong>${item.number}</strong></td>
      <td>${item.invoice}</td>
      <td>${new Date(item.date).toLocaleDateString("id-ID")}</td>
      <td class="right">${currency.format(Number(item.total || 0))}</td>
      <td>${badge(item.status || "Completed")}</td>
    </tr>
  `).join("");

  document.querySelector("#movement-table").innerHTML = movements.map((item) => `
    <tr>
      <td>${new Date(item.date).toLocaleString("id-ID")}</td>
      <td>${item.product}</td>
      <td>${item.type}</td>
      <td class="right">${Number(item.qty || 0)}</td>
    </tr>
  `).join("");
}

function renderSettings() {
  document.querySelector("#setting-table").innerHTML = settingsData.map((item) => `
    <tr>
      <td><strong>${item.key}</strong></td>
      <td>${item.value}</td>
      <td>${item.description || "-"}</td>
    </tr>
  `).join("");
}

async function loadLiveData() {
  try {
    const period = currentPeriod();
    const [health, dashboard, salesData, expensesData, profitData, stockData, productData, purchaseData, supplierData, returnsPayload, settingsPayload] = await Promise.all([
      apiGet("/api/health"),
      apiGet(`/api/dashboard?period=${period}`),
      apiGet(`/api/sales?period=${period}`),
      apiGet(`/api/expenses?period=${period}`),
      apiGet(`/api/profit?period=${period}`),
      apiGet("/api/stock"),
      apiGet("/api/products"),
      apiGet(`/api/purchases?period=${period}`),
      apiGet("/api/suppliers"),
      apiGet("/api/returns"),
      apiGet("/api/settings"),
    ]);

    liveMode = true;
    setConnectionStatus(`Live DB: ${health.counts.sales} sales, ${health.counts.products} produk`, "live");

    const summary = dashboard.summary || {};
    document.querySelector("#net-revenue").textContent = currency.format(Number(summary.net_revenue || 0));
    document.querySelector("#gross-profit").textContent = currency.format(Number(summary.gross_profit || 0));
    document.querySelector("#expense-total").textContent = currency.format(Number(summary.expenses || 0));
    document.querySelector("#net-profit").textContent = currency.format(Number(summary.net_profit || 0));

    chartData.splice(0, chartData.length, ...(dashboard.trend || []).map((item) => ({
      day: item.day,
      revenue: Number(item.revenue || 0) / 1_000_000,
      profit: Number(item.profit || 0) / 1_000_000,
    })));

    const liveTransactions = (salesData.sales && salesData.sales.length > 0)
      ? salesData.sales
      : (dashboard.transactions || []);
    transactions.splice(0, transactions.length, ...liveTransactions.map((item) => ({
      invoice: item.invoice,
      time: item.time ? new Date(item.time).toLocaleTimeString("id-ID", { hour: "2-digit", minute: "2-digit" }) : "-",
      cashier: item.cashier,
      method: item.method,
      total: Number(item.total || 0),
      status: normalizeStatus(item.status),
    })));

    payments.splice(0, payments.length, ...(dashboard.payments || []).map((item) => ({
      name: item.name,
      amount: Number(item.amount || 0),
      pct: 0,
    })));
    const maxPayment = Math.max(...payments.map((item) => item.amount), 1);
    payments.forEach((item) => {
      item.pct = Math.round((item.amount / maxPayment) * 100);
    });

    approvals.splice(0, approvals.length, ...(dashboard.approvals || []));
    expenses.splice(0, expenses.length, ...(expensesData.expenses || []).map((item) => ({
      code: item.code,
      category: item.category,
      note: item.note || "-",
      total: Number(item.total || 0),
      status: item.status,
    })));
    profitRows.splice(0, profitRows.length, ...(profitData.rows || []));
    stockItems = (stockData.stock || []).map((item) => ({
      sku: item.sku,
      name: item.name,
      system: Number(item.system || 0),
      physical: Number(item.physical || 0),
      hpp: Number(item.hpp || 0),
      status: "Aman",
      note: "",
    }));
    products = productData.products || [];
    purchases = purchaseData.purchases || [];
    suppliers = supplierData.suppliers || [];
    returnsData = returnsPayload.returns || [];
    movements = returnsPayload.movements || [];
    settingsData = settingsPayload.settings || [];

    renderChart();
    renderTransactions();
    renderExpenses();
    renderPayments();
    renderApprovals();
    renderStatement();
    renderProducts();
    renderPurchases();
    renderSuppliers();
    renderReturns();
    renderSettings();
    renderStockOptions();
    renderStock();
  } catch (error) {
    liveMode = false;
    console.warn("Live DB belum aktif.", error);
    setConnectionStatus("DB belum tersambung", "error");
    if (!isLocalPreview()) {
      alert("Database Vercel belum tersambung. Set DATABASE_URL ke database yang sama dengan desktop.");
    }
  }
}

function stockStatus(item) {
  const diff = item.physical - item.system;
  if (diff !== 0) return "Selisih";
  if (item.system <= 5) return "Kritis";
  return "Aman";
}

function renderStock() {
  const filter = document.querySelector("#stock-filter")?.value || "all";
  const query = (document.querySelector("#stock-search")?.value || "").trim().toLowerCase();
  const items = stockItems
    .map((item) => ({ ...item, status: stockStatus(item), diff: item.physical - item.system }))
    .filter((item) => {
      const matchesQuery = `${item.sku} ${item.name}`.toLowerCase().includes(query);
      const matchesFilter = filter === "all"
        || (filter === "critical" && item.status === "Kritis")
        || (filter === "diff" && item.diff !== 0);
      return matchesQuery && matchesFilter;
    });

  document.querySelector("#stock-table").innerHTML = items.map((item) => `
    <tr>
      <td><strong>${item.sku}</strong></td>
      <td>${item.name}</td>
      <td class="right">${item.system}</td>
      <td class="right">${item.physical}</td>
      <td class="right">${item.diff > 0 ? "+" : ""}${item.diff}</td>
      <td>${badge(item.status)}</td>
    </tr>
  `).join("");

  const critical = stockItems.filter((item) => stockStatus(item) === "Kritis").length;
  const counted = stockItems.filter((item) => Number.isFinite(item.physical)).length;
  const diffValue = stockItems.reduce((total, item) => {
    return total + Math.abs(item.physical - item.system) * item.hpp;
  }, 0);

  document.querySelector("#stock-sku-total").textContent = String(stockItems.length);
  document.querySelector("#stock-critical-total").textContent = String(critical);
  document.querySelector("#stock-counted-total").textContent = `${Math.round((counted / stockItems.length) * 100)}%`;
  document.querySelector("#stock-diff-total").textContent = currency.format(diffValue);
  renderStockAlerts();
}

function renderStockOptions() {
  const select = document.querySelector("#stock-sku");
  select.innerHTML = stockItems.map((item) => `
    <option value="${item.sku}">${item.sku} - ${item.name}</option>
  `).join("");
}

function renderStockAlerts() {
  const alerts = stockItems
    .map((item) => ({ ...item, status: stockStatus(item), diff: item.physical - item.system }))
    .filter((item) => item.status !== "Aman")
    .slice(0, 4);
  document.querySelector("#stock-alerts").innerHTML = alerts.map((item) => `
    <div class="stock-alert">
      <strong>${item.status}: ${item.name}</strong>
      <span>${item.sku} - Sistem ${item.system}, fisik ${item.physical}, selisih ${item.diff}</span>
    </div>
  `).join("");
}

function setView(viewId) {
  document.querySelectorAll(".view").forEach((view) => view.classList.remove("active-view"));
  document.querySelector(`#${viewId}`).classList.add("active-view");
  document.querySelectorAll(".nav a").forEach((link) => {
    link.classList.toggle("active", link.dataset.view === viewId);
  });
  const titles = {
    dashboard: "Dashboard Keuangan",
    products: "Produk",
    purchases: "Pembelian",
    suppliers: "Supplier",
    returns: "Retur / Mutasi",
    profit: "Laba Rugi",
    sales: "Laporan Penjualan",
    transactions: "Transaksi",
    expenses: "Laporan Biaya",
    stock: "Stock Opname",
    reconcile: "Rekonsiliasi Kas",
    settings: "Setting",
  };
  document.querySelector("#page-title").textContent = titles[viewId];
}

function setupNavigation() {
  document.querySelectorAll(".nav a").forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      setView(link.dataset.view);
      history.replaceState(null, "", link.getAttribute("href"));
    });
  });
}

function setupSearch() {
  const input = document.querySelector("#global-search");
  input.addEventListener("input", () => {
    const query = input.value.trim().toLowerCase();
    document.querySelectorAll("tbody tr").forEach((row) => {
      row.hidden = query.length > 0 && !row.textContent.toLowerCase().includes(query);
    });
  });
}

function setupExport() {
  document.querySelector("#export-btn").addEventListener("click", () => {
    const header = "invoice,waktu,metode,total,status";
    const body = transactions.map((item) => [
      item.invoice,
      item.time,
      item.method,
      item.total,
      item.status,
    ].join(","));
    const blob = new Blob([[header, ...body].join("\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "baletpos-laporan-penjualan.csv";
    link.click();
    URL.revokeObjectURL(url);
  });

  document.querySelector("#export-stock-btn").addEventListener("click", () => {
    const header = "sku,produk,stok_sistem,stok_fisik,selisih,status,catatan";
    const body = stockItems.map((item) => {
      const diff = item.physical - item.system;
      return [
        item.sku,
        item.name,
        item.system,
        item.physical,
        diff,
        stockStatus(item),
        item.note,
      ].join(",");
    });
    const blob = new Blob([[header, ...body].join("\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "baletpos-stock-opname.csv";
    link.click();
    URL.revokeObjectURL(url);
  });
}

function setupReconcile() {
  document.querySelector("#reconcile-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const systemCash = Number(form.get("systemCash") || 0);
    const actualCash = Number(form.get("actualCash") || 0);
    const diff = actualCash - systemCash;
    const output = document.querySelector("#cash-diff");
    output.textContent = currency.format(diff);
    output.style.color = diff === 0 ? "var(--good)" : "var(--danger)";
  });
}

function setupStockOpname() {
  document.querySelector("#stock-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const sku = form.get("sku");
    const item = stockItems.find((entry) => entry.sku === sku);
    if (!item) return;

    item.physical = Number(form.get("physical") || 0);
    item.note = String(form.get("note") || "");
    renderStock();

    if (liveMode) {
      apiPost("/api/stock", {
        sku,
        physical: item.physical,
        note: item.note,
      })
        .then(loadLiveData)
        .catch((error) => alert(error.message));
    }
  });

  document.querySelector("#stock-filter").addEventListener("change", renderStock);
  document.querySelector("#stock-search").addEventListener("input", renderStock);
  document.querySelector("#stock-sku").addEventListener("change", (event) => {
    const item = stockItems.find((entry) => entry.sku === event.target.value);
    if (!item) return;
    document.querySelector("#stock-form input[name='physical']").value = item.physical;
    document.querySelector("#stock-form textarea[name='note']").value = item.note;
  });
}

function setupManagementForms() {
  document.querySelector("#product-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await apiPost("/api/products", {
        sku: form.get("sku"),
        name: form.get("name"),
        type: form.get("type"),
        hpp: form.get("hpp"),
        sellingPrice: form.get("sellingPrice"),
        stock: form.get("stock"),
      });
      event.currentTarget.reset();
      await loadLiveData();
    } catch (error) {
      alert(error.message);
    }
  });

  document.querySelector("#supplier-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await apiPost("/api/suppliers", {
        code: form.get("code"),
        name: form.get("name"),
        contact: form.get("contact"),
        phone: form.get("phone"),
        email: form.get("email"),
        address: form.get("address"),
      });
      event.currentTarget.reset();
      await loadLiveData();
    } catch (error) {
      alert(error.message);
    }
  });
}

function showApp() {
  document.querySelector("#login-screen").classList.add("is-hidden");
  document.querySelector("#app-shell").classList.remove("is-hidden");
}

function showLogin() {
  document.querySelector("#login-screen").classList.remove("is-hidden");
  document.querySelector("#app-shell").classList.add("is-hidden");
}

function setupAuth() {
  const isLoggedIn = localStorage.getItem("baletposFinanceLoggedIn") === "true";
  if (isLoggedIn) {
    showApp();
  } else {
    showLogin();
  }

  document.querySelector("#login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const username = String(form.get("username") || "").trim();
    const password = String(form.get("password") || "");
    const error = document.querySelector("#login-error");

    try {
      await apiPost("/api/login", { username, password });
      localStorage.setItem("baletposFinanceLoggedIn", "true");
      error.textContent = "";
      showApp();
      await loadLiveData();
      return;
    } catch (apiError) {
      console.warn("Login DB gagal.", apiError);
    }

    if (isLocalPreview() && username === demoCredentials.username && password === demoCredentials.password) {
      localStorage.setItem("baletposFinanceLoggedIn", "true");
      error.textContent = "";
      showApp();
      setConnectionStatus("Preview lokal: data dummy", "error");
      return;
    }

    error.textContent = isLocalPreview()
      ? "Login DB gagal. Untuk preview lokal bisa pakai finance / finance123."
      : "Login gagal. Pastikan DATABASE_URL Vercel memakai DB desktop yang sama.";
  });

  document.querySelector("#logout-btn").addEventListener("click", () => {
    localStorage.removeItem("baletposFinanceLoggedIn");
    showLogin();
  });
}

function initializeApp() {
  renderChart();
  renderTransactions();
  renderExpenses();
  renderPayments();
  renderApprovals();
  renderStatement();
  renderProducts();
  renderPurchases();
  renderSuppliers();
  renderReturns();
  renderSettings();
  renderStockOptions();
  renderStock();
  setupNavigation();
  setupSearch();
  setupExport();
  setupReconcile();
  setupStockOpname();
  setupManagementForms();
  setupAuth();

  document.querySelector("#period-select").addEventListener("change", loadLiveData);

  const initialView = location.hash.replace("#", "");
  if (["dashboard", "products", "purchases", "suppliers", "returns", "profit", "sales", "transactions", "expenses", "stock", "reconcile", "settings"].includes(initialView)) {
    setView(initialView);
  }

  if (localStorage.getItem("baletposFinanceLoggedIn") === "true") {
    loadLiveData();
  }
}

initializeApp();
