package com.baletpos.util;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Reusable Pagination Component for TableView
 * Provides: page navigation, page size selector, info label
 */
public class PaginationControl extends HBox {

    // Properties
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty pageSize = new SimpleIntegerProperty(10);
    private final IntegerProperty totalItems = new SimpleIntegerProperty(0);

    // UI Components
    private final Label infoLabel = new Label("Menampilkan 0-0 dari 0");
    private final Button firstBtn = new Button("<<");
    private final Button prevBtn = new Button("<");
    private final Button nextBtn = new Button(">");
    private final Button lastBtn = new Button(">>");
    private final Label pageLabel = new Label("Halaman 1 dari 1");
    private final ComboBox<Integer> pageSizeCombo = new ComboBox<>();

    // Callback
    private Runnable onPageChange;

    public PaginationControl() {
        super(10);
        setAlignment(Pos.CENTER);
        setPrefHeight(40);
        setMinHeight(40);
        setMaxHeight(40);
        setStyle(
                "-fx-padding: 6 12; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        setupUI();
        setupListeners();
        updateState();
    }

    private void setupUI() {
        // Info Label (Left)
        infoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Spacer
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        // Navigation Buttons (Center)
        String btnStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-min-width: 28; -fx-min-height: 28; -fx-max-height: 28; -fx-padding: 4 8; -fx-cursor: hand;";

        firstBtn.setStyle(btnStyle);
        prevBtn.setStyle(btnStyle);
        nextBtn.setStyle(btnStyle);
        lastBtn.setStyle(btnStyle);

        pageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b; -fx-padding: 0 8;");

        HBox navBox = new HBox(4, firstBtn, prevBtn, pageLabel, nextBtn, lastBtn);
        navBox.setAlignment(Pos.CENTER);

        // Spacer
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Page Size Selector (Right)
        pageSizeCombo.setItems(FXCollections.observableArrayList(10, 20));
        pageSizeCombo.setValue(10);
        pageSizeCombo.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        pageSizeCombo.setPrefWidth(72);
        pageSizeCombo.setMaxHeight(30);

        Label perPageLabel = new Label("per halaman");
        perPageLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        HBox sizeBox = new HBox(8, pageSizeCombo, perPageLabel);
        sizeBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(infoLabel, leftSpacer, navBox, rightSpacer, sizeBox);
    }

    private void setupListeners() {
        // Button actions
        firstBtn.setOnAction(e -> goToPage(0));
        prevBtn.setOnAction(e -> goToPage(currentPage.get() - 1));
        nextBtn.setOnAction(e -> goToPage(currentPage.get() + 1));
        lastBtn.setOnAction(e -> goToPage(getTotalPages() - 1));

        // Page size change
        pageSizeCombo.setOnAction(e -> {
            pageSize.set(pageSizeCombo.getValue());
            currentPage.set(0); // Reset to first page
            triggerPageChange();
        });

        // Property listeners
        totalItems.addListener((obs, old, newVal) -> updateState());
        currentPage.addListener((obs, old, newVal) -> updateState());
        pageSize.addListener((obs, old, newVal) -> updateState());
    }

    private void goToPage(int page) {
        int maxPage = Math.max(0, getTotalPages() - 1);
        int newPage = Math.max(0, Math.min(page, maxPage));

        if (newPage != currentPage.get()) {
            currentPage.set(newPage);
            triggerPageChange();
        }
    }

    private void triggerPageChange() {
        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    private void updateState() {
        int total = totalItems.get();
        int size = pageSize.get();
        int page = currentPage.get();
        int totalPages = getTotalPages();

        // Info label
        int start = total == 0 ? 0 : (page * size) + 1;
        int end = Math.min((page + 1) * size, total);
        infoLabel.setText(String.format("Menampilkan %d-%d dari %d", start, end, total));

        // Page label
        pageLabel.setText(String.format("Halaman %d dari %d", totalPages == 0 ? 0 : page + 1, totalPages));

        // Button states
        boolean isFirstPage = page == 0;
        boolean isLastPage = page >= totalPages - 1 || totalPages == 0;

        firstBtn.setDisable(isFirstPage);
        prevBtn.setDisable(isFirstPage);
        nextBtn.setDisable(isLastPage);
        lastBtn.setDisable(isLastPage);
    }

    // === Public API ===

    public int getTotalPages() {
        int total = totalItems.get();
        int size = pageSize.get();
        return size == 0 ? 0 : (int) Math.ceil((double) total / size);
    }

    public int getCurrentPage() {
        return currentPage.get();
    }

    public void setCurrentPage(int page) {
        currentPage.set(page);
    }

    public int getPageSize() {
        return pageSize.get();
    }

    public void setPageSize(int size) {
        pageSize.set(size);
        pageSizeCombo.setValue(size);
    }

    public int getTotalItems() {
        return totalItems.get();
    }

    public void setTotalItems(int total) {
        totalItems.set(total);
    }

    public int getOffset() {
        return currentPage.get() * pageSize.get();
    }

    public void setOnPageChange(Runnable callback) {
        this.onPageChange = callback;
    }

    /**
     * Reset pagination to first page (useful when filter changes)
     */
    public void resetToFirstPage() {
        currentPage.set(0);
    }

    /**
     * Update pagination after data load
     */
    public void update(int totalCount) {
        setTotalItems(totalCount);
    }

    public IntegerProperty currentPageProperty() {
        return currentPage;
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public IntegerProperty totalItemsProperty() {
        return totalItems;
    }
}


