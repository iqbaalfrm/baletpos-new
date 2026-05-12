package com.baletpos.util;

import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility untuk mengelola gambar produk
 * - Validasi ekstensi
 * - Resize dan compress
 * - Simpan ke local storage
 * - Cache management
 */
public class ImageUtil {
    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    // Allowed extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    // Max dimensions
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final int THUMBNAIL_SIZE = 64;

    // JPEG quality (0.0 - 1.0)
    private static final double COMPRESSION_QUALITY = 0.85;

    // Image directory
    private static final Path IMAGE_DIR;
    private static final Path PLACEHOLDER_PATH;

    // Image cache (weak references for memory efficiency)
    private static final Map<String, Image> imageCache = new WeakHashMap<>();
    private static final Map<String, Image> thumbnailCache = new WeakHashMap<>();

    // Placeholder image
    private static Image placeholderImage;
    private static Image placeholderThumbnail;

    static {
        // Use relative path from application working directory
        IMAGE_DIR = Paths.get(".", "data", "images", "products").toAbsolutePath().normalize();
        PLACEHOLDER_PATH = IMAGE_DIR.resolve("placeholder.png");
        logger.info("Product images directory: {}", IMAGE_DIR);

        // Ensure directory exists
        try {
            Files.createDirectories(IMAGE_DIR);

            // Create placeholder if not exists
            if (!Files.exists(PLACEHOLDER_PATH)) {
                ensureDefaultImage();
            }

            // Load placeholder images
            loadPlaceholders();

        } catch (IOException e) {
            logger.error("Failed to create image directory", e);
        }
    }

    /**
     * Validasi ekstensi file
     */
    public static boolean isValidExtension(File file) {
        String name = file.getName().toLowerCase();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            String ext = name.substring(dotIndex + 1);
            return ALLOWED_EXTENSIONS.contains(ext);
        }
        return false;
    }

    /**
     * Validasi ekstensi dari path
     */
    public static boolean isValidExtension(String path) {
        if (path == null || path.isEmpty())
            return false;
        String name = path.toLowerCase();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            String ext = name.substring(dotIndex + 1);
            return ALLOWED_EXTENSIONS.contains(ext);
        }
        return false;
    }

    /**
     * Process dan simpan gambar produk
     * 
     * @param sourceFile File sumber
     * @param sku        SKU produk
     * @return Relative path untuk disimpan ke database, atau null jika gagal
     */
    public static String saveProductImage(File sourceFile, String sku) {
        logger.info("[UPLOAD] Source file: {}", sourceFile != null ? sourceFile.getAbsolutePath() : "null");

        if (sourceFile == null || !sourceFile.exists()) {
            logger.warn("[UPLOAD] Source file is null or doesn't exist");
            return null;
        }

        if (!isValidExtension(sourceFile)) {
            logger.warn("[UPLOAD] Invalid file extension: {}", sourceFile.getName());
            return null;
        }

        try {
            // Ensure directory exists
            Files.createDirectories(IMAGE_DIR);
            logger.info("[UPLOAD] Target directory: {}", IMAGE_DIR);

            // Generate unique filename
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String filename = sku.replaceAll("[^a-zA-Z0-9-]", "_") + "_" + uuid + ".jpg";
            Path targetPath = IMAGE_DIR.resolve(filename);

            logger.info("[UPLOAD] Target path: {}", targetPath.toAbsolutePath());

            // Read, resize, and save
            BufferedImage original = ImageIO.read(sourceFile);
            if (original == null) {
                logger.error("[UPLOAD] Failed to read image: {}", sourceFile.getName());
                return null;
            }

            // Resize if larger than max
            int width = original.getWidth();
            int height = original.getHeight();

            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                Thumbnails.of(original)
                        .size(MAX_WIDTH, MAX_HEIGHT)
                        .outputQuality(COMPRESSION_QUALITY)
                        .outputFormat("jpg")
                        .toFile(targetPath.toFile());
            } else {
                // Just compress without resize
                Thumbnails.of(original)
                        .scale(1.0)
                        .outputQuality(COMPRESSION_QUALITY)
                        .outputFormat("jpg")
                        .toFile(targetPath.toFile());
            }

            // Verify file saved successfully
            boolean exists = Files.exists(targetPath);
            long size = exists ? Files.size(targetPath) : 0;
            logger.info("[UPLOAD] File saved: exists={}, size={} bytes", exists, size);

            if (!exists) {
                logger.error("[UPLOAD] File was not saved properly!");
                return null;
            }

            // Clear cache for this path
            imageCache.remove(filename);
            thumbnailCache.remove(filename);

            // Return RELATIVE path for DB storage
            String relativePath = "data/images/products/" + filename;
            logger.info("[UPLOAD] Returning relative path for DB: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            logger.error("[UPLOAD] Failed to save product image", e);
            return null;
        }
    }

    /**
     * Load gambar produk dari path (dengan cache)
     */
    public static Image loadProductImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return getPlaceholder();
        }

        // Check cache
        if (imageCache.containsKey(imagePath)) {
            return imageCache.get(imagePath);
        }

        try {
            // Resolve path: if it starts with "data/", resolve from working dir
            // Otherwise assume it's just filename in IMAGE_DIR
            Path fullPath;
            if (imagePath.startsWith("data/") || imagePath.startsWith("data\\")) {
                fullPath = Paths.get(".", imagePath).toAbsolutePath().normalize();
            } else {
                fullPath = IMAGE_DIR.resolve(imagePath);
            }

            logger.debug("[LOAD] Resolving image path: {} -> {}", imagePath, fullPath);

            if (Files.exists(fullPath)) {
                Image image = new Image(fullPath.toUri().toString());
                if (!image.isError()) {
                    imageCache.put(imagePath, image);
                    return image;
                }
            } else {
                logger.warn("[LOAD] Image file not found: {}", fullPath);
            }
        } catch (Exception e) {
            logger.warn("[LOAD] Failed to load image: {}", imagePath, e);
        }

        return getPlaceholder();
    }

    /**
     * Load thumbnail produk (64x64) dengan cache
     */
    public static Image loadProductThumbnail(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return getPlaceholderThumbnail();
        }

        // Check cache
        if (thumbnailCache.containsKey(imagePath)) {
            return thumbnailCache.get(imagePath);
        }

        try {
            // Resolve path: if it starts with "data/", resolve from working dir
            Path fullPath;
            if (imagePath.startsWith("data/") || imagePath.startsWith("data\\")) {
                fullPath = Paths.get(".", imagePath).toAbsolutePath().normalize();
            } else {
                fullPath = IMAGE_DIR.resolve(imagePath);
            }

            if (Files.exists(fullPath)) {
                // Load with specified dimensions for performance
                Image thumbnail = new Image(fullPath.toUri().toString(),
                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true, true);

                if (!thumbnail.isError()) {
                    thumbnailCache.put(imagePath, thumbnail);
                    return thumbnail;
                }
            }
        } catch (Exception e) {
            logger.warn("[LOAD] Failed to load thumbnail: {}", imagePath);
        }

        return getPlaceholderThumbnail();
    }

    /**
     * Hapus gambar produk
     */
    public static boolean deleteProductImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        try {
            // Resolve path: if it starts with "data/", resolve from working dir
            Path fullPath;
            if (imagePath.startsWith("data/") || imagePath.startsWith("data\\")) {
                fullPath = Paths.get(".", imagePath).toAbsolutePath().normalize();
            } else {
                fullPath = IMAGE_DIR.resolve(imagePath);
            }

            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                imageCache.remove(imagePath);
                thumbnailCache.remove(imagePath);
                logger.info("[DELETE] Image deleted: {}", fullPath);
                return true;
            }
        } catch (IOException e) {
            logger.error("[DELETE] Failed to delete image: {}", imagePath, e);
        }
        return false;
    }

    /**
     * Get placeholder image
     */
    public static Image getPlaceholder() {
        return placeholderImage != null ? placeholderImage : createDefaultPlaceholder();
    }

    /**
     * Get placeholder thumbnail
     */
    public static Image getPlaceholderThumbnail() {
        return placeholderThumbnail != null ? placeholderThumbnail : createDefaultPlaceholder();
    }

    /**
     * Get image directory path
     */
    public static Path getImageDirectory() {
        return IMAGE_DIR;
    }

    /**
     * Clear all caches
     */
    public static void clearCache() {
        imageCache.clear();
        thumbnailCache.clear();
    }

    // ====== Private methods ======

    private static void ensureDefaultImage() {
        if (Files.exists(PLACEHOLDER_PATH)) {
            return;
        }

        logger.info("Default image not found. Attempting to download...");
        boolean downloaded = downloadDefaultImage();

        if (!downloaded) {
            logger.warn("Download failed. Generating local placeholder.");
            generateLocalPlaceholder();
        }
    }

    private static boolean downloadDefaultImage() {
        // Public placeholder URL
        String urlStr = "https://dummyimage.com/256x256/e2e8f0/1e293b.png&text=No+Image";

        try {
            java.net.URLConnection conn = java.net.URI.create(urlStr).toURL().openConnection();
            conn.setConnectTimeout(5000); // 5 seconds timeout
            conn.setReadTimeout(5000);

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, PLACEHOLDER_PATH);
                logger.info("Default image downloaded successfully from {}", urlStr);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to download default image: {}", e.getMessage());
            return false;
        }
    }

    private static void generateLocalPlaceholder() {
        try {
            // Create a simple gray placeholder
            BufferedImage placeholder = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = placeholder.createGraphics();

            // Background (Slate-200 like)
            g2d.setColor(new java.awt.Color(226, 232, 240));
            g2d.fillRect(0, 0, 256, 256);

            // Border
            g2d.setColor(new java.awt.Color(203, 213, 225));
            g2d.drawRect(0, 0, 255, 255);

            // Text
            g2d.setColor(new java.awt.Color(100, 116, 139));
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));

            String text = "NO IMAGE";
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int x = (256 - fm.stringWidth(text)) / 2;
            int y = (256 - fm.getHeight()) / 2 + fm.getAscent();

            g2d.drawString(text, x, y);

            g2d.dispose();

            ImageIO.write(placeholder, "png", PLACEHOLDER_PATH.toFile());
            logger.info("Local placeholder image generated");

        } catch (IOException e) {
            logger.error("Failed to create local placeholder", e);
        }
    }

    private static void loadPlaceholders() {
        try {
            if (Files.exists(PLACEHOLDER_PATH)) {
                placeholderImage = new Image(PLACEHOLDER_PATH.toUri().toString());
                placeholderThumbnail = new Image(PLACEHOLDER_PATH.toUri().toString(),
                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);
            }
        } catch (Exception e) {
            logger.error("Failed to load placeholders", e);
        }
    }

    private static Image createDefaultPlaceholder() {
        // Return a minimal placeholder if file-based one fails
        return new Image(ImageUtil.class.getResourceAsStream("/images/placeholder.png"));
    }

    /**
     * Copy dummy images from resources to data folder (for first run)
     */
    public static void copyDummyImages() {
        String[] dummyImages = {
                "laptop_sample.jpg",
                "peripheral_sample.jpg",
                "service_sample.jpg"
        };

        for (String imageName : dummyImages) {
            try {
                InputStream is = ImageUtil.class.getResourceAsStream("/dummy_images/" + imageName);
                if (is != null) {
                    Path targetPath = IMAGE_DIR.resolve(imageName);
                    if (!Files.exists(targetPath)) {
                        Files.copy(is, targetPath);
                        logger.info("Copied dummy image: {}", imageName);
                    }
                    is.close();
                }
            } catch (Exception e) {
                logger.warn("Failed to copy dummy image: {}", imageName);
            }
        }
    }
}


