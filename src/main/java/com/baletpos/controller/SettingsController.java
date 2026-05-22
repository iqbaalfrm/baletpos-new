package com.baletpos.controller;

import com.baletpos.config.LocalAppConfig;
import com.baletpos.util.ModalUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SettingsController {
    @FXML
    private TextField dbUrlField;
    @FXML
    private TextField dbUserField;
    @FXML
    private PasswordField dbPasswordField;
    @FXML
    private CheckBox backupEnabledCheck;
    @FXML
    private TextField backupDirField;
    @FXML
    private TextField backupTimeField;
    @FXML
    private TextField driveServiceAccountPathField;
    @FXML
    private TextField driveFolderIdField;
    @FXML
    private Label configPathLabel;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings() {
        Properties props = LocalAppConfig.load();
        dbUrlField.setText(props.getProperty("baletpos.db.url", ""));
        dbUserField.setText(props.getProperty("baletpos.db.user", ""));
        dbPasswordField.setText(props.getProperty("baletpos.db.password", ""));
        backupEnabledCheck.setSelected(Boolean.parseBoolean(props.getProperty("baletpos.backup.enabled", "true")));
        backupDirField.setText(props.getProperty("baletpos.backup.dir", ""));
        backupTimeField.setText(props.getProperty("baletpos.backup.time", "23:00"));
        driveServiceAccountPathField.setText(props.getProperty("baletpos.backup.drive.serviceAccountPath", ""));
        driveFolderIdField.setText(props.getProperty("baletpos.backup.drive.folderId", ""));
        configPathLabel.setText(LocalAppConfig.getConfigPath().toString());
    }

    @FXML
    private void handleBrowseServiceAccount() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Service Account JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(saveButton.getScene().getWindow());
        if (file != null) {
            driveServiceAccountPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleTestSupabase() {
        String url = dbUrlField.getText().trim();
        if (url.isBlank()) {
            ModalUtil.showWarning("Konfigurasi Kosong", "Isi JDBC URL Supabase dulu.");
            return;
        }

        try (Connection ignored = DriverManager.getConnection(
                url,
                dbUserField.getText().trim(),
                dbPasswordField.getText())) {
            ModalUtil.showSuccess("Berhasil", "Koneksi Supabase berhasil.");
        } catch (Exception e) {
            ModalUtil.showError("Koneksi Gagal", e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            Properties props = LocalAppConfig.load();
            putOrRemove(props, "baletpos.db.url", dbUrlField.getText());
            putOrRemove(props, "baletpos.db.user", dbUserField.getText());
            putOrRemove(props, "baletpos.db.password", dbPasswordField.getText());
            props.setProperty("baletpos.backup.enabled", String.valueOf(backupEnabledCheck.isSelected()));
            putOrRemove(props, "baletpos.backup.dir", backupDirField.getText());
            putOrRemove(props, "baletpos.backup.time", backupTimeField.getText());
            putOrRemove(props, "baletpos.backup.drive.serviceAccountPath", driveServiceAccountPathField.getText());
            putOrRemove(props, "baletpos.backup.drive.folderId", driveFolderIdField.getText());
            LocalAppConfig.save(props);
            ModalUtil.showSuccess("Setting Disimpan",
                    "Setting tersimpan. Restart aplikasi agar mode database Supabase aktif.");
        } catch (Exception e) {
            ModalUtil.showError("Gagal Menyimpan", e.getMessage());
        }
    }

    private void putOrRemove(Properties props, String key, String value) {
        if (value == null || value.trim().isBlank()) {
            props.remove(key);
        } else {
            props.setProperty(key, value.trim());
        }
    }
}
