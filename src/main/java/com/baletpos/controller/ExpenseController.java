package com.baletpos.controller;

import com.baletpos.dao.*;
import com.baletpos.model.*;
import com.baletpos.util.PaginationControl;
import com.baletpos.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller untuk mengelola Biaya Operasional
 */
public class ExpenseController {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<ExpenseCode> expenseCodeCombo;

    @FXML
    private TableView<Expense> expenseTable;

    @FXML
    private TableColumn<Expense, String> numberCol;

    @FXML
    private TableColumn<Expense, String> dateCol;

    @FXML
    private TableColumn<Expense, String> codeCol;

    @FXML
    private TableColumn<Expense, String> codeNameCol;

    @FXML
    private TableColumn<Expense, String> amountCol;

    @FXML
    private TableColumn<Expense, String> descriptionCol;

    @FXML
    private Label totalLabel;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private VBox tableContainer;

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final ExpenseCodeDAO expenseCodeDAO = new ExpenseCodeDAO();
    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    private List<ExpenseCode> allExpenseCodes;
    private List<Expense> allExpenses = new ArrayList<>();
    private PaginationControl pagination;

    @FXML
    public void initialize() {
        currencyFormat.setMaximumFractionDigits(0);

        setupTable();
        setupPagination();

        try {
            loadExpenseCodes();
            setupFilters();
            loadExpenses();
        } catch (Exception e) {
            System.err.println("Error loading expense data: " + e.getMessage());
            e.printStackTrace();
        }

        setupPermissions();

        // Selection listener
        expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null);
        });
    }

    private void setupTable() {
        numberCol.setCellValueFactory(new PropertyValueFactory<>("expenseNumber"));
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getExpenseDate() != null ? cell.getValue().getExpenseDate().toString() : ""));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("expenseCode"));
        codeNameCol.setCellValueFactory(new PropertyValueFactory<>("expenseCodeName"));
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty(
                currencyFormat.format(cell.getValue().getAmount())));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        expenseTable.setItems(expenses);
    }

    private void loadExpenseCodes() {
        allExpenseCodes = expenseCodeDAO.findAll();
        ObservableList<ExpenseCode> codeOptions = FXCollections.observableArrayList();
        codeOptions.add(null); // All codes
        codeOptions.addAll(allExpenseCodes);
        expenseCodeCombo.setItems(codeOptions);
    }

    private void setupFilters() {
        // Default to current month
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));

        // Add listeners
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadExpenses());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadExpenses());
        expenseCodeCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadExpenses());
    }

    private void setupPermissions() {
        User currentUser = Session.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == User.Role.ADMIN;

        addButton.setDisable(!isAdmin);
        deleteButton.setDisable(true);
    }

    private void loadExpenses() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        ExpenseCode selectedCode = expenseCodeCombo.getValue();

        if (startDate == null || endDate == null) {
            return;
        }

        List<Expense> result;
        if (selectedCode != null) {
            result = expenseDAO.findByDateRangeAndCode(startDate, endDate, selectedCode.getId());
        } else {
            result = expenseDAO.findByDateRange(startDate, endDate);
        }

        allExpenses = result;
        applyPagination();
    }

    private void setupPagination() {
        pagination = new PaginationControl();
        pagination.setOnPageChange(this::applyPagination);
        if (tableContainer != null) {
            tableContainer.getChildren().add(pagination);
        }
    }

    private void applyPagination() {
        if (allExpenses == null) {
            allExpenses = new ArrayList<>();
        }

        pagination.update(allExpenses.size());

        int from = pagination.getOffset();
        int to = Math.min(from + pagination.getPageSize(), allExpenses.size());

        expenses.clear();
        if (from < to) {
            expenses.addAll(allExpenses.subList(from, to));
        }

        calculateTotal();
    }

    private void calculateTotal() {
        BigDecimal total = allExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: " + currencyFormat.format(total));
    }

    @FXML
    private void handleAdd() {
        javafx.stage.Stage modal = new javafx.stage.Stage();
        com.baletpos.util.ModalUtil.applyModalDefaults(modal);
        modal.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // --- Container ---
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 0);");
        root.setPadding(new Insets(0));
        root.setPrefWidth(480);

        // --- Header ---
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 32, 24, 32));
        header.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        VBox titleBox = new VBox(4);
        Label title = new Label(" Tambah Biaya Operasional");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Masukkan informasi biaya operasional di bawah ini.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        javafx.scene.shape.SVGPath closeIcon = new javafx.scene.shape.SVGPath();
        closeIcon.setContent("M6 18L18 6M6 6l12 12");
        closeIcon.setStroke(javafx.scene.paint.Color.web("#94a3b8"));
        closeIcon.setStrokeWidth(2);

        Button closeBtn = new Button();
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modal.close());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, closeBtn);

        // --- Form Content ---
        VBox formContent = new VBox(20);
        formContent.setPadding(new Insets(32));

        // Form Fields
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");

        ComboBox<ExpenseCode> codeCombo = new ComboBox<>(FXCollections.observableArrayList(allExpenseCodes));
        codeCombo.setPromptText("Pilih kode biaya...");
        codeCombo.setMaxWidth(Double.MAX_VALUE);
        codeCombo.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");

        TextField amountField = createStyledTextField("", "Contoh: 500000");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Keterangan biaya (opsional)");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle(
                "-fx-control-inner-background: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4; -fx-font-size: 14px; -fx-faint-focus-color: transparent;");

        formContent.getChildren().addAll(
                createInputGroup("Tanggal", datePicker),
                createInputGroup("Kode Biaya", codeCombo),
                createInputGroup("Nominal (Rp)", amountField),
                createInputGroup("Keterangan", descArea));

        // --- Footer ---
        javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(12);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 32, 24, 32));
        footer.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Batal");
        cancelBtn.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 24; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        cancelBtn.setOnAction(e -> modal.close());

        Button saveBtn = new Button("Simpan Biaya");
        saveBtn.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 32; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-background-color: #1E40AF; -fx-text-fill: white;");

        footer.getChildren().addAll(cancelBtn, saveBtn);

        // --- Save Action ---
        saveBtn.setOnAction(e -> {
            if (codeCombo.getValue() == null) {
                showError("Pilih kode biaya terlebih dahulu");
                return;
            }
            if (amountField.getText().isBlank()) {
                showError("Masukkan nominal biaya");
                return;
            }

            try {
                int amount = Integer.parseInt(amountField.getText().replaceAll("[^0-9]", ""));

                Expense expense = new Expense();
                expense.setExpenseDate(datePicker.getValue());
                expense.setExpenseCodeId(codeCombo.getValue().getId());
                expense.setAmount(BigDecimal.valueOf(amount));
                expense.setDescription(descArea.getText().trim());
                expense.setCreatedBy(Session.getInstance().getCurrentUser().getId());

                expenseDAO.create(expense);
                loadExpenses();
                modal.close();
                showInfo(" Biaya berhasil disimpan!\nNomor: " + expense.getExpenseNumber());
            } catch (NumberFormatException ex) {
                showError("Nominal tidak valid");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Gagal menyimpan biaya: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(header, formContent, footer);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private TextField createStyledTextField(String text, String prompt) {
        TextField field = new TextField(text);
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 12 14; -fx-font-size: 14px;");
        return field;
    }

    private VBox createInputGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        group.getChildren().addAll(label, field);
        return group;
    }

    @FXML
    private void handleDelete() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = com.baletpos.util.ModalUtil.showConfirmDanger(
                    "Hapus Biaya",
                    "Apakah Anda yakin ingin menghapus biaya \"" + selected.getExpenseNumber() + "\"?");

            if (confirm) {
                try {
                    expenseDAO.delete(selected.getId());
                    loadExpenses();
                    showInfo("Biaya berhasil dihapus");
                } catch (Exception e) {
                    showError("Gagal menghapus biaya: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadExpenses();
    }

    @FXML
    private void handleManageCodes() {
        // Show dialog to manage expense codes
        showExpenseCodesDialog();
    }

    @SuppressWarnings("unchecked")
    private void showExpenseCodesDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Kelola Kode Biaya");
        dialog.setHeaderText("Daftar Kode Biaya Operasional");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<ExpenseCode> codeTable = new TableView<>();
        TableColumn<ExpenseCode, String> codeCol = new TableColumn<>("Kode");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<ExpenseCode, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ExpenseCode, String> descCol = new TableColumn<>("Deskripsi");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        codeTable.getColumns().addAll(codeCol, nameCol, descCol);
        codeTable.setItems(FXCollections.observableArrayList(allExpenseCodes));
        codeTable.setPrefSize(500, 300);

        dialog.getDialogPane().setContent(codeTable);
        dialog.showAndWait();
    }

    private void showError(String message) {
        com.baletpos.util.ModalUtil.showError("Error", message);
    }

    private void showInfo(String message) {
        com.baletpos.util.ModalUtil.showSuccess("Berhasil", message);
    }
}


