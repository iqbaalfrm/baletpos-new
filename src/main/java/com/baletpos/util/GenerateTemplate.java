package com.baletpos.util;

import com.baletpos.service.ExcelImportService;
import java.io.File;

public class GenerateTemplate {
    public static void main(String[] args) {
        try {
            ExcelImportService service = new ExcelImportService();
            File output = new File("template_import_produk.xlsx");
            service.generateTemplate(output);
            System.out.println("Template berhasil dibuat: " + output.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


