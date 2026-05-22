package com.baletpos.controller;

import com.baletpos.App;
import com.baletpos.model.User;
import com.baletpos.service.AuthService;
import com.baletpos.util.Session;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private StackPane contentArea;

    // Admin-only buttons
    @FXML
    private Button purchaseButton;
    @FXML
    private Button supplierButton;
    @FXML
    private Button expenseButton;
    @FXML
    private Button profitLossButton;
    @FXML
    private Button returnButton;
    @FXML
    private Button settingsButton;

    // Dashboard summary labels
    @FXML
    private Label todaySalesCountLabel;
    @FXML
    private Label todaySalesTotalLabel;
    @FXML
    private Label lowStockLabel;
    @FXML
    private Label monthRevenueLabel;

    // Charts
    @FXML
    private PieChart categoryPieChart;
    @FXML
    private BarChart<String, Number> weeklySalesChart;
    @FXML
    private CategoryAxis weeklyXAxis;
    @FXML
    private NumberAxis weeklyYAxis;

    private final AuthService authService;

    public DashboardController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn()) {
            User currentUser = Session.getInstance().getCurrentUser();
            String fullName = currentUser.getFullName();

            if (welcomeLabel != null)
                welcomeLabel.setText("BaletPOS");
            if (roleLabel != null)
                roleLabel.setText(currentUser.getRole().getDisplayName());

            setupRoleBasedAccess();

            if (currentUser.getRole() == User.Role.KASIR) {
                javafx.application.Platform.runLater(this::showPOS);
            } else {
                showDashboard();
            }
        }
    }

    private void setupRoleBasedAccess() {
        Session session = Session.getInstance();
        boolean canManageStore = session.canManageStore();
        boolean canManageFinance = session.canManageFinance();

        setButtonVisible(purchaseButton, canManageStore);
        setButtonVisible(supplierButton, canManageStore);
        setButtonVisible(returnButton, canManageStore);
        setButtonVisible(expenseButton, canManageFinance);
        setButtonVisible(profitLossButton, canManageFinance);
        setButtonVisible(settingsButton, canManageStore || canManageFinance);
    }

    private void setButtonVisible(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
            btn.setDisable(!visible);
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        authService.logout();
        App.setRoot("login");
    }

    @FXML
    private void showPOS() {
        loadView("pos_view");
    }

    @FXML
    public void showProducts() {
        loadView("product_list");
    }

    @FXML
    private void showSales() {
        loadView("sales_report");
    }

    @FXML
    private void showTransactionList() {
        loadView("transaction_list");
    }

    @FXML
    private void showService() {
        loadView("service_view");
    }

    @FXML
    private void showPurchase() {
        if (checkStoreAccess()) {
            loadView("purchase_view");
        }
    }

    @FXML
    private void showSuppliers() {
        if (checkStoreAccess()) {
            loadView("supplier_list");
        }
    }

    @FXML
    private void showExpenses() {
        if (checkFinanceAccess()) {
            loadView("expense_view");
        }
    }

    @FXML
    private void showReturns() {
        if (checkStoreAccess()) {
            loadView("return_mutation");
        }
    }

    @FXML
    private void showSalesReportLaptop() {
        loadView("category_sales_report");
    }

    @FXML
    private void showSalesReportPeripheral() {
        loadView("category_sales_report");
    }

    @FXML
    private void showSalesReportService() {
        loadView("category_sales_report");
    }

    @FXML
    private void showInventoryReport() {
        loadView("inventory_report");
    }

    @FXML
    private void showProfitLossReport() {
        if (checkFinanceAccess()) {
            loadView("profit_loss_report");
        }
    }

    @FXML
    private void showExpenseReport() {
        if (checkFinanceAccess()) {
            loadView("expense_view");
        }
    }

    @FXML
    private void showDashboard() {
        loadView("dashboard_content");
    }

    @FXML
    private void showSettings() {
        if (!Session.getInstance().isAdmin()) {
            showError("Akses ditolak. Setting hanya untuk Admin Toko atau Admin Keuangan.");
            return;
        }
        loadView("settings_view");
    }

    private boolean checkStoreAccess() {
        if (!Session.getInstance().canManageStore()) {
            showError("Akses ditolak. Fitur ini hanya untuk Admin Toko.");
            return false;
        }
        return true;
    }

    private boolean checkFinanceAccess() {
        if (!Session.getInstance().canManageFinance()) {
            showError("Akses ditolak. Fitur ini hanya untuk Admin Keuangan.");
            return false;
        }
        return true;
    }

    @FXML
    private javafx.scene.layout.HBox topBar;
    @FXML
    private javafx.scene.layout.VBox sidebar;

    // ... existing fields ...

    private void enterPosMode() {
        if (topBar != null) {
            topBar.setVisible(false);
            topBar.setManaged(false);
        }
        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        }
    }

    private void exitPosMode() {
        if (topBar != null) {
            topBar.setVisible(true);
            topBar.setManaged(true);
        }
        if (sidebar != null) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
        }
    }

    public void returnToDashboard() {
        exitPosMode();
        showDashboard();
    }

    private void loadView(String fxml) {
        try {
            // Auto layout switch based on view type
            if ("pos_view".equals(fxml)) {
                enterPosMode();
            } else {
                exitPosMode();
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    App.class.getResource("/fxml/" + fxml + ".fxml"));
            javafx.scene.Parent view = loader.load();

            // Inject DashboardController reference if the loaded controller accepts it
            Object controller = loader.getController();
            if (controller instanceof POSController) {
                ((POSController) controller).setDashboardController(this);
            } else if (controller instanceof DashboardContentController) {
                ((DashboardContentController) controller).setParentController(this);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Tidak dapat memuat view: " + fxml);
            // If failed to load POS, exit pos mode to be safe
            if ("pos_view".equals(fxml)) {
                exitPosMode();
            }
        }
    }

    private void showError(String message) {
        com.baletpos.util.ModalUtil.showError("Error", message);
    }
}


