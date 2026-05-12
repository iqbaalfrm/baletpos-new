package com.baletpos.service;

import com.baletpos.model.Sale;
import com.baletpos.util.ReceiptFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.nio.charset.Charset;

public class ReceiptPrinterService {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptPrinterService.class);
    private static final String PRINTER_PROPERTY = "baletpos.receipt.printer";
    private static final String PRINTER_ENV = "BALETPOS_RECEIPT_PRINTER";
    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;

    public void printReceipt(Sale sale) throws PrintException {
        PrintService printer = resolvePrinter();
        byte[] payload = buildEscPosPayload(ReceiptFormatter.format(sale));

        DocPrintJob job = printer.createPrintJob();
        Doc doc = new SimpleDoc(payload, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        job.print(doc, null);
        logger.info("Receipt sent to printer: {}", printer.getName());
    }

    public static String listAvailablePrinters() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services == null || services.length == 0) {
            return "Tidak ada printer terdeteksi.";
        }

        StringBuilder sb = new StringBuilder();
        for (PrintService service : services) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append("- ").append(service.getName());
        }
        return sb.toString();
    }

    private PrintService resolvePrinter() throws PrintException {
        String configuredName = getConfiguredPrinterName();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (configuredName != null && !configuredName.isBlank()) {
            for (PrintService service : services) {
                if (service.getName().equalsIgnoreCase(configuredName)
                        || service.getName().toLowerCase().contains(configuredName.toLowerCase())) {
                    return service;
                }
            }
            throw new PrintException("Printer nota tidak ditemukan: " + configuredName
                    + System.lineSeparator() + "Printer tersedia:"
                    + System.lineSeparator() + listAvailablePrinters());
        }

        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrinter == null) {
            throw new PrintException("Default printer belum diset di Windows."
                    + System.lineSeparator() + "Printer tersedia:"
                    + System.lineSeparator() + listAvailablePrinters());
        }
        return defaultPrinter;
    }

    private String getConfiguredPrinterName() {
        String propertyValue = System.getProperty(PRINTER_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(PRINTER_ENV);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return null;
    }

    private byte[] buildEscPosPayload(String receiptText) {
        Charset charset = Charset.forName("windows-1252");
        byte[] text = normalizeLineEndings(receiptText).getBytes(charset);

        byte[] init = new byte[] { ESC, '@' };
        byte[] feedAndCut = new byte[] {
                '\n', '\n', '\n',
                GS, 'V', 66, 0
        };

        byte[] payload = new byte[init.length + text.length + feedAndCut.length];
        System.arraycopy(init, 0, payload, 0, init.length);
        System.arraycopy(text, 0, payload, init.length, text.length);
        System.arraycopy(feedAndCut, 0, payload, init.length + text.length, feedAndCut.length);
        return payload;
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n");
    }
}
