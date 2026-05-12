package com.baletpos.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Utility untuk membuat button modern di TableView.
 * Mengganti button bawaan JavaFX yang abu-abu dan kotak.
 */
public class TableButtonUtil {

    // =============================================
    // ICON BUTTONS (32x32, untuk action di table)
    // =============================================

    /**
     * Buat button View/Detail (biru)
     */
    public static Button viewButton(Runnable onClick) {
        return createIconButton("Lihat", "Lihat Detail", "btn-table-view", onClick);
    }

    /**
     * Buat button Edit (kuning/amber)
     */
    public static Button editButton(Runnable onClick) {
        return createIconButton("Edit", "Edit", "btn-table-edit", onClick);
    }

    /**
     * Buat button Delete (merah)
     */
    public static Button deleteButton(Runnable onClick) {
        return createIconButton("Hapus", "Hapus", "btn-table-delete", onClick);
    }

    /**
     * Buat button Print (teal)
     */
    public static Button printButton(Runnable onClick) {
        return createIconButton("Cetak", "Cetak", "btn-table-print", onClick);
    }

    /**
     * Buat button Approve/Success (hijau)
     */
    public static Button successButton(String icon, String tooltip, Runnable onClick) {
        return createIconButton(icon, tooltip, "btn-table-success", onClick);
    }

    // =============================================
    // TEXT BUTTONS (untuk aksi dengan label)
    // =============================================

    /**
     * Buat text button Primary (biru solid)
     */
    public static Button primaryTextButton(String text, Runnable onClick) {
        return createTextButton(text, "btn-table-text btn-table-text-primary", onClick);
    }

    /**
     * Buat text button Secondary (abu-abu outline)
     */
    public static Button secondaryTextButton(String text, Runnable onClick) {
        return createTextButton(text, "btn-table-text btn-table-text-secondary", onClick);
    }

    /**
     * Buat text button Danger (merah)
     */
    public static Button dangerTextButton(String text, Runnable onClick) {
        return createTextButton(text, "btn-table-text btn-table-text-danger", onClick);
    }

    // =============================================
    // ACTION BOX (Container untuk beberapa button)
    // =============================================

    /**
     * Buat container HBox untuk group button
     */
    public static HBox actionBox(Button... buttons) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(buttons);
        return box;
    }

    // =============================================
    // INTERNAL BUILDERS
    // =============================================

    private static Button createIconButton(String icon, String tooltipText, String styleClass, Runnable onClick) {
        Button btn = new Button(icon);
        btn.getStyleClass().addAll("btn-table-action", styleClass);
        btn.setTooltip(new Tooltip(tooltipText));
        btn.setOnAction(e -> {
            if (onClick != null)
                onClick.run();
        });
        return btn;
    }

    private static Button createTextButton(String text, String styleClasses, Runnable onClick) {
        Button btn = new Button(text);
        for (String cls : styleClasses.split(" ")) {
            btn.getStyleClass().add(cls);
        }
        btn.setOnAction(e -> {
            if (onClick != null)
                onClick.run();
        });
        return btn;
    }
}


