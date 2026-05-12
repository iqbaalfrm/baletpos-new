package com.baletpos.util;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

/**
 * Utility untuk menampilkan notifikasi toast menggunakan ControlsFX
 * Menggunakan style minimalis light mode.
 */
public class NotificationUtil {

    private static final Duration DEFAULT_DURATION = Duration.seconds(4);
    private static final Duration LONG_DURATION = Duration.seconds(6);

    /**
     * Tampilkan notifikasi sukses
     */
    public static void success(String title, String message) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.CHECK_CIRCLE);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title(title)
                .text(message)
                .graphic(icon)
                .hideAfter(DEFAULT_DURATION)
                .position(Pos.TOP_RIGHT)
                .styleClass("notification-success")
                .show();
    }

    public static void successShort(String message) {
        success("Berhasil", message);
    }

    /**
     * Tampilkan notifikasi error
     */
    public static void error(String title, String message) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title(title)
                .text(message)
                .graphic(icon)
                .hideAfter(LONG_DURATION)
                .position(Pos.TOP_RIGHT)
                .styleClass("notification-error")
                .show();
    }

    public static void errorShort(String message) {
        error("Error", message);
    }

    /**
     * Tampilkan notifikasi warning
     */
    public static void warning(String title, String message) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title(title)
                .text(message)
                .graphic(icon)
                .hideAfter(DEFAULT_DURATION)
                .position(Pos.TOP_RIGHT)
                .styleClass("notification-warning")
                .show();
    }

    /**
     * Tampilkan notifikasi info
     */
    public static void info(String title, String message) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.INFO_CIRCLE);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title(title)
                .text(message)
                .graphic(icon)
                .hideAfter(DEFAULT_DURATION)
                .position(Pos.TOP_RIGHT)
                .styleClass("notification-info")
                .show();
    }

    public static void infoShort(String message) {
        info("Info", message);
    }

    /**
     * Tampilkan notifikasi penjualan berhasil (DI TENGAH)
     */
    public static void saleComplete(String invoiceNumber, String total) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.CASH_REGISTER);
        icon.setIconSize(28);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title("Transaksi Berhasil!")
                .text("Invoice: " + invoiceNumber + "\nTotal: " + total)
                .graphic(icon)
                .hideAfter(LONG_DURATION)
                .position(Pos.TOP_CENTER)
                .styleClass("notification-success")
                .show();
    }

    /**
     * Tampilkan notifikasi void berhasil
     */
    public static void voidSuccess(String invoiceNumber) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.BAN);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title("Transaksi Dibatalkan")
                .text("Invoice " + invoiceNumber + " berhasil di-VOID")
                .graphic(icon)
                .hideAfter(DEFAULT_DURATION)
                .position(Pos.TOP_RIGHT)
                .styleClass("notification-warning")
                .show();
    }

    /**
     * Tampilkan notifikasi stok rendah (POJOK KANAN BAWAH)
     */
    public static void lowStockAlert(String productName, int currentStock) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.BOX_OPEN);
        icon.setIconSize(24);
        icon.setStyle("-fx-fill: #1E40AF;");

        Notifications.create()
                .title("Peringatan Stok!")
                .text(productName + " tersisa " + currentStock + " unit")
                .graphic(icon)
                .hideAfter(LONG_DURATION)
                .position(Pos.BOTTOM_RIGHT)
                .styleClass("notification-error")
                .show();
    }
}


