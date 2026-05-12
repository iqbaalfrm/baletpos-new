package com.baletpos.controller;

import com.baletpos.dao.SupplierDAO;
import com.baletpos.model.Supplier;
import com.baletpos.model.User;
import com.baletpos.util.PaginationControl;
import com.baletpos.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller untuk mengelola data Supplier
 */
public class SupplierController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Supplier> supplierTable;

    @FXML
    private TableColumn<Supplier, String> codeCol;

    @FXML
    private TableColumn<Supplier, String> nameCol;

    @FXML
    private TableColumn<Supplier, String> contactCol;

    @FXML
    private TableColumn<Supplier, String> phoneCol;

    @FXML
    private TableColumn<Supplier, String> emailCol;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private VBox tableContainer;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private PaginationControl pagination;
    private String currentSearchQuery = "";

    @FXML
    public void initialize() {
        setupTable();
        setupPagination();
        loadSuppliers();
        setupPermissions();

        // Selection listener
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            editButton.setDisable(newVal == null);
            deleteButton.setDisable(newVal == null);
        });

        // Search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSuppliers(newVal));
    }

    private void setupTable() {
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        supplierTable.setItems(suppliers);
    }

    private void setupPermissions() {
        User currentUser = Session.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == User.Role.ADMIN;

        addButton.setDisable(!isAdmin);
        editButton.setDisable(true);
        deleteButton.setDisable(!isAdmin);
    }

    private void loadSuppliers() {
        suppliers.clear();
        int total = supplierDAO.countFiltered(currentSearchQuery);
        pagination.update(total);

        int limit = pagination.getPageSize();
        int offset = pagination.getOffset();
        suppliers.addAll(supplierDAO.findAllPaged(limit, offset, currentSearchQuery));
    }

    private void filterSuppliers(String query) {
        currentSearchQuery = query != null ? query.trim() : "";
        pagination.resetToFirstPage();
        loadSuppliers();
    }

    private void setupPagination() {
        pagination = new PaginationControl();
        pagination.setOnPageChange(this::loadSuppliers);
        if (tableContainer != null) {
            tableContainer.getChildren().add(pagination);
        }
    }

    @FXML
    private void handleAdd() {
        showSupplierDialog(null);
    }

    @FXML
    private void handleEdit() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSupplierDialog(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = com.baletpos.util.ModalUtil.showConfirmDanger(
                    "Hapus Supplier",
                    "Apakah Anda yakin ingin menghapus supplier \"" + selected.getName() + "\"?");

            if (confirm) {
                try {
                    supplierDAO.delete(selected.getId());
                    loadSuppliers();
                    showInfo("Supplier berhasil dihapus");
                } catch (Exception e) {
                    showError("Gagal menghapus supplier: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadSuppliers();
    }

    private void showSupplierDialog(Supplier supplier) {
        javafx.stage.Stage modal = new javafx.stage.Stage();
        com.baletpos.util.ModalUtil.applyModalDefaults(modal);
        modal.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // --- Container ---
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
        root.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 0);");
        root.setPadding(new Insets(0));
        root.setPrefWidth(520);

        // --- Header ---
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 32, 24, 32));
        header.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        javafx.scene.layout.VBox titleBox = new javafx.scene.layout.VBox(4);
        Label title = new Label(supplier == null ? "Tambah Supplier Baru" : "Edit Supplier");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label subtitle = new Label(
                supplier == null ? "Masukkan informasi supplier di bawah ini." : "Perbarui informasi supplier.");
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
        javafx.scene.layout.VBox formContent = new javafx.scene.layout.VBox(16);
        formContent.setPadding(new Insets(32));

        // Form Fields
        TextField codeField = createStyledTextField(supplier != null ? supplier.getCode() : "",
                "Kode Supplier (contoh: SUP-001)");
        codeField.setEditable(supplier == null);
        if (supplier != null)
            codeField.setStyle(codeField.getStyle() + "-fx-background-color: #f1f5f9;");

        TextField nameField = createStyledTextField(supplier != null ? supplier.getName() : "", "Nama Supplier");
        TextField contactField = createStyledTextField(supplier != null ? supplier.getContact() : "",
                "Nama Contact Person");
        TextField phoneField = createStyledTextField(supplier != null ? supplier.getPhone() : "", "No. Telepon");
        TextField emailField = createStyledTextField(supplier != null ? supplier.getEmail() : "", "Email");

        TextArea addressArea = new TextArea(supplier != null ? supplier.getAddress() : "");
        addressArea.setPromptText("Alamat lengkap");
        addressArea.setPrefRowCount(2);
        addressArea.setWrapText(true);
        addressArea.setStyle(
                "-fx-control-inner-background: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4; -fx-font-size: 14px;");

        // Two-column layout
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setPercentWidth(50);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createInputGroup("Kode Supplier", codeField), 0, 0);
        grid.add(createInputGroup("Nama Supplier", nameField), 1, 0);
        grid.add(createInputGroup("Contact Person", contactField), 0, 1);
        grid.add(createInputGroup("Telepon", phoneField), 1, 1);
        grid.add(createInputGroup("Email", emailField), 0, 2);
        grid.add(createInputGroup("Alamat", addressArea), 0, 3, 2, 1);

        formContent.getChildren().add(grid);

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

        Button saveBtn = new Button("Simpan Supplier");
        saveBtn.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 32; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-background-color: #1E40AF; -fx-text-fill: white;");

        footer.getChildren().addAll(cancelBtn, saveBtn);

        // --- Save Action ---
        saveBtn.setOnAction(e -> {
            if (codeField.getText().isBlank() || nameField.getText().isBlank()) {
                showError("Kode dan Nama tidak boleh kosong");
                return;
            }

            try {
                Supplier result = supplier != null ? supplier : new Supplier();
                if (supplier == null) {
                    result.setCode(codeField.getText().trim().toUpperCase());
                }
                result.setName(nameField.getText().trim());
                result.setContact(contactField.getText().trim());
                result.setPhone(phoneField.getText().trim());
                result.setEmail(emailField.getText().trim());
                result.setAddress(addressArea.getText().trim());

                supplierDAO.save(result);
                loadSuppliers();
                modal.close();
                showInfo(" Supplier berhasil disimpan");
            } catch (Exception ex) {
                showError("Gagal menyimpan supplier: " + ex.getMessage());
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

    private javafx.scene.layout.VBox createInputGroup(String labelText, javafx.scene.Node field) {
        javafx.scene.layout.VBox group = new javafx.scene.layout.VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        group.getChildren().addAll(label, field);
        return group;
    }

    private void showError(String message) {
        com.baletpos.util.ModalUtil.showError("Error", message);
    }

    private void showInfo(String message) {
        com.baletpos.util.ModalUtil.showSuccess("Berhasil", message);
    }
}


