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
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ReceiptPrinterService {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptPrinterService.class);
    private static final String PRINTER_PROPERTY = "baletpos.receipt.printer";
    private static final String PRINTER_ENV = "BALETPOS_RECEIPT_PRINTER";
    private static final String LOGO_RESOURCE = "/images/logonota.png";
    private static final int RECEIPT_WIDTH_CHARS = 80;
    private static final int LOGO_WIDTH_PIXELS = 220;
    private static final byte ESC = 0x1B;

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
        byte[] logo = buildLogoPayload();
        byte[] text = normalizeLineEndings(receiptText).getBytes(charset);

        // Dot-matrix continuous form 9.5" x 11":
        // ESC @       reset printer
        // ESC P       pica / 10 CPI, stable for 80-column receipts
        // ESC 2       1/6" line spacing
        // ESC O       disable skip-over-perforation
        // ESC C 0 11  page length = 11 inches
        byte[] init = new byte[] { ESC, '@', ESC, 'P', ESC, '2', ESC, 'O', ESC, 'C', 0, 11 };

        // Form Feed moves to the next perforation after the receipt.
        byte[] feed = new byte[] { 0x0C };

        byte[] payload = new byte[init.length + logo.length + text.length + feed.length];
        System.arraycopy(init, 0, payload, 0, init.length);
        System.arraycopy(logo, 0, payload, init.length, logo.length);
        System.arraycopy(text, 0, payload, init.length + logo.length, text.length);
        System.arraycopy(feed, 0, payload, init.length + logo.length + text.length, feed.length);
        return payload;
    }

    private byte[] buildLogoPayload() {
        try (InputStream input = ReceiptPrinterService.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (input == null) {
                logger.warn("Receipt logo resource not found: {}", LOGO_RESOURCE);
                return new byte[0];
            }

            BufferedImage source = ImageIO.read(input);
            if (source == null) {
                logger.warn("Receipt logo could not be decoded: {}", LOGO_RESOURCE);
                return new byte[0];
            }

            BufferedImage logo = scaleLogo(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int leftPaddingChars = Math.max(0, (RECEIPT_WIDTH_CHARS - (int) Math.ceil(logo.getWidth() / 6.0)) / 2);
            byte[] padding = " ".repeat(leftPaddingChars).getBytes(Charset.forName("windows-1252"));

            out.write('\r');
            out.write('\n');
            for (int y = 0; y < logo.getHeight(); y += 8) {
                out.write(padding);
                out.write(ESC);
                out.write('*');
                out.write(0); // ESC/P 8-dot single-density, compatible with common dot-matrix printers.
                out.write(logo.getWidth() & 0xFF);
                out.write((logo.getWidth() >> 8) & 0xFF);

                for (int x = 0; x < logo.getWidth(); x++) {
                    int column = 0;
                    for (int bit = 0; bit < 8; bit++) {
                        int py = y + bit;
                        if (py < logo.getHeight() && shouldPrintDot(logo.getRGB(x, py))) {
                            column |= 1 << (7 - bit);
                        }
                    }
                    out.write(column);
                }
                out.write('\r');
                out.write('\n');
            }
            out.write('\r');
            out.write('\n');
            return out.toByteArray();
        } catch (Exception e) {
            logger.warn("Receipt logo skipped: {}", e.getMessage());
            return new byte[0];
        }
    }

    private BufferedImage scaleLogo(BufferedImage source) {
        int width = Math.min(LOGO_WIDTH_PIXELS, source.getWidth());
        int height = Math.max(1, (int) Math.round(source.getHeight() * (width / (double) source.getWidth())));

        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }

    private boolean shouldPrintDot(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha < 24) {
            return false;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
        return luminance > 35;
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n");
    }
}
