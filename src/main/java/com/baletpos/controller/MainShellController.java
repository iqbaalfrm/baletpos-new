package com.baletpos.controller;

import com.baletpos.App;
import com.baletpos.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * MainShellController - DEPRECATED
 * Replaced by DashboardController. Kept for legacy reference but methods
 * disabled.
 */
public class MainShellController {
    private static final Logger logger = LoggerFactory.getLogger(MainShellController.class);

    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox sidebar;
    @FXML
    private VBox appBar;
    @FXML
    private StackPane contentArea;

    @FXML
    private Label pageTitle;
    @FXML
    private Label userBadge;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnPOS;
    @FXML
    private Button btnProducts;
    @FXML
    private Button btnPurchase;
    @FXML
    private Button btnSupplier;
    @FXML
    private Button btnReturn;
    @FXML
    private Button btnExpense;
    @FXML
    private Button btnSalesReport;
    @FXML
    private Button btnInventoryReport;
    @FXML
    private Button btnProfitLoss;

    @FXML
    public void initialize() {
        // Deprecated
    }

    @FXML
    private void showDashboard() {
    }

    @FXML
    private void showPOS() {
    }

    @FXML
    private void showProducts() {
    }

    @FXML
    private void showPurchase() {
    }

    @FXML
    private void showSuppliers() {
    }

    @FXML
    private void showReturns() {
    }

    @FXML
    private void showExpenses() {
    }

    @FXML
    private void showSalesReport() {
    }

    @FXML
    private void showInventoryReport() {
    }

    @FXML
    private void showProfitLossReport() {
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            logger.error("Failed to logout", e);
        }
    }
}


