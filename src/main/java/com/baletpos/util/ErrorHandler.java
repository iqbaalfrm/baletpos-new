package com.baletpos.util;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Central Error Handler for converting technical exceptions to user-friendly
 * messages.
 */
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public static String getUserFriendlyMessage(String context, Throwable e) {
        // Log technical detail
        logger.error("Error in [{}]: {}", context, e.getMessage(), e);

        String msg = e.getMessage();
        if (msg == null)
            msg = "";
        String msgLower = msg.toLowerCase();

        // 1. Database Errors
        if (e instanceof SQLException || msgLower.contains("sqlite") || msgLower.contains("sql")) {
            if (msgLower.contains("constraint") || msgLower.contains("unique")) {
                return "Data duplikat atau tidak valid. Silakan cek input Anda.";
            }
            if (msgLower.contains("lock") || msgLower.contains("busy") || msgLower.contains("locked")) {
                return "Database sedang sibuk. Silakan coba sesaat lagi.";
            }
            if (msgLower.contains("not implemented")) {
                return "Terjadi kesalahan driver database. Hubungi Technical Support.";
            }
            if (msgLower.contains("stok tidak mencukupi")) {
                return msg; // Pesan ini aman karena dilempar manual dari DAO
            }
            return "Gagal menyimpan data ke database. Silakan coba lagi.";
        }

        // 2. Input Errors
        if (e instanceof NumberFormatException) {
            return "Format angka tidak valid. Cek input harga/jumlah.";
        }

        // 3. Logic Errors (Safe to show)
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return msg;
        }

        // 4. Generic/Unknown
        return "Terjadi kesalahan sistem (" + e.getClass().getSimpleName() + "). Hubungi Admin.";
    }

    public static void handle(String context, Throwable e) {
        String userMsg = getUserFriendlyMessage(context, e);

        Platform.runLater(() -> {
            ModalUtil.showError("Terjadi Kesalahan", userMsg);
        });
    }
}


