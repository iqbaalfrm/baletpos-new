package com.baletpos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class LocalAppConfig {
    private static final Logger logger = LoggerFactory.getLogger(LocalAppConfig.class);
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".baletpos", "config.properties");

    private LocalAppConfig() {
    }

    public static Properties load() {
        Properties props = new Properties();
        if (!Files.exists(CONFIG_PATH)) {
            return props;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            props.load(input);
        } catch (IOException e) {
            logger.warn("Failed to read local config: {}", e.getMessage());
        }
        return props;
    }

    public static void save(Properties props) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            props.store(output, "BaletPOS local settings");
        }
    }

    public static String get(String key) {
        String value = load().getProperty(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
