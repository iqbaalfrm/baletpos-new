package com.baletpos.util;

import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class ExcelExportUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);

    public static <T> void export(Stage owner, String title, List<String> headers,
            List<Function<T, Object>> cellValueFactories, ObservableList<T> items) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Excel");
        fileChooser.setInitialFileName(title.replace(" ", "_") + "_" + LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(owner);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(title);

                // Header Style
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                // Create Header Row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Create Data Rows
                int rowNum = 1;
                for (T item : items) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 0; i < cellValueFactories.size(); i++) {
                        Cell cell = row.createCell(i);
                        Object value = cellValueFactories.get(i).apply(item);
                        if (value == null) {
                            cell.setCellValue("");
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Auto size columns
                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                ModalUtil.showSuccess("Export Berhasil", "File berhasil disimpan ke: " + file.getAbsolutePath());
                logger.info("Exported excel to: {}", file.getAbsolutePath());

            } catch (IOException e) {
                logger.error("Failed to export excel", e);
                ModalUtil.showError("Export Gagal", "Gagal menyimpan file: " + e.getMessage());
            }
        }
    }
}


