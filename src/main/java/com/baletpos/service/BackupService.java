package com.baletpos.service;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.DatabaseDialect;
import com.baletpos.config.LocalAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "baletpos-backup");
        thread.setDaemon(true);
        return thread;
    });
    private static boolean started;

    public static synchronized void start() {
        if (started || !isEnabled()) {
            return;
        }
        started = true;

        long initialDelay = secondsUntilNextRun();
        SCHEDULER.scheduleWithFixedDelay(
                BackupService::runDailyBackupSafely,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);

        if (!hasBackupToday()) {
            SCHEDULER.schedule(BackupService::runDailyBackupSafely, 30, TimeUnit.SECONDS);
        }
        logger.info("Automatic backup scheduled. Next daily run in {} seconds.", initialDelay);
    }

    public static void runDailyBackupSafely() {
        try {
            Path backupFile = createBackup();
            uploadToGoogleDriveIfConfigured(backupFile);
        } catch (Exception e) {
            logger.error("Automatic backup failed", e);
        }
    }

    public static Path createBackup() throws Exception {
        Path backupDir = getBackupDir();
        Files.createDirectories(backupDir);

        String dbMode = DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES ? "supabase" : "sqlite";
        Path output = backupDir.resolve("baletpos-" + dbMode + "-" + LocalDateTime.now().format(FILE_TS) + ".zip");

        try (Connection conn = DatabaseConfig.getConnection();
                ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output), StandardCharsets.UTF_8)) {
            addManifest(zip, dbMode);
            for (String tableName : getTableNames(conn)) {
                writeTableCsv(conn, zip, tableName);
            }
        }

        logger.info("Database backup created: {}", output);
        return output;
    }

    private static void addManifest(ZipOutputStream zip, String dbMode) throws IOException {
        zip.putNextEntry(new ZipEntry("manifest.txt"));
        String manifest = "BaletPOS Backup\n" +
                "created_at=" + LocalDateTime.now() + "\n" +
                "database=" + dbMode + "\n";
        zip.write(manifest.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static List<String> getTableNames(Connection conn) throws Exception {
        List<String> tables = new ArrayList<>();
        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            try (ResultSet rs = conn.getMetaData().getTables(null, "public", "%", new String[] { "TABLE" })) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } else {
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        }
        tables.sort(String::compareToIgnoreCase);
        return tables;
    }

    private static void writeTableCsv(Connection conn, ZipOutputStream zip, String tableName) throws Exception {
        String safeTable = tableName.replace("\"", "\"\"");
        String sql = DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES
                ? "SELECT * FROM public.\"" + safeTable + "\""
                : "SELECT * FROM \"" + safeTable + "\"";

        zip.putNextEntry(new ZipEntry("tables/" + tableName + ".csv"));
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) {
                    zip.write(',');
                }
                zip.write(csv(meta.getColumnLabel(i)).getBytes(StandardCharsets.UTF_8));
            }
            zip.write('\n');

            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        zip.write(',');
                    }
                    Object value = rs.getObject(i);
                    zip.write(csv(value == null ? "" : String.valueOf(value)).getBytes(StandardCharsets.UTF_8));
                }
                zip.write('\n');
            }
        }
        zip.closeEntry();
    }

    private static String csv(String value) {
        boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }

    private static void uploadToGoogleDriveIfConfigured(Path backupFile) throws Exception {
        String serviceAccountJson = getConfigValue(
                "baletpos.backup.drive.serviceAccountJson",
                "BALETPOS_BACKUP_DRIVE_SERVICE_ACCOUNT_JSON");
        String serviceAccountPath = getConfigValue(
                "baletpos.backup.drive.serviceAccountPath",
                "BALETPOS_BACKUP_DRIVE_SERVICE_ACCOUNT_PATH");

        if ((serviceAccountJson == null || serviceAccountJson.isBlank())
                && serviceAccountPath != null
                && !serviceAccountPath.isBlank()) {
            serviceAccountJson = Files.readString(Paths.get(serviceAccountPath), StandardCharsets.UTF_8);
        }

        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            logger.info("Google Drive backup upload skipped: service account not configured.");
            return;
        }

        String token = createGoogleAccessToken(serviceAccountJson);
        String folderId = getConfigValue("baletpos.backup.drive.folderId", "BALETPOS_BACKUP_DRIVE_FOLDER_ID");
        uploadFileToDrive(token, folderId, backupFile);
    }

    private static String createGoogleAccessToken(String serviceAccountJson) throws Exception {
        String clientEmail = jsonValue(serviceAccountJson, "client_email");
        String privateKeyPem = jsonValue(serviceAccountJson, "private_key").replace("\\n", "\n");
        String tokenUri = jsonValue(serviceAccountJson, "token_uri");
        if (tokenUri == null || tokenUri.isBlank()) {
            tokenUri = "https://oauth2.googleapis.com/token";
        }

        long now = System.currentTimeMillis() / 1000;
        String header = base64Url("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = base64Url(("{" +
                "\"iss\":\"" + clientEmail + "\"," +
                "\"scope\":\"https://www.googleapis.com/auth/drive.file\"," +
                "\"aud\":\"" + tokenUri + "\"," +
                "\"iat\":" + now + "," +
                "\"exp\":" + (now + 3600) +
                "}").getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + payload;

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(parsePrivateKey(privateKeyPem));
        signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
        String assertion = signingInput + "." + base64Url(signature.sign());

        String body = "grant_type=" + url("urn:ietf:params:oauth:grant-type:jwt-bearer")
                + "&assertion=" + url(assertion);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Google token request failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return jsonValue(response.body(), "access_token");
    }

    private static void uploadFileToDrive(String accessToken, String folderId, Path file) throws Exception {
        String boundary = "baletpos-backup-" + System.currentTimeMillis();
        String fileName = file.getFileName().toString();
        String metadata = folderId == null || folderId.isBlank()
                ? "{\"name\":\"" + fileName + "\"}"
                : "{\"name\":\"" + fileName + "\",\"parents\":[\"" + folderId + "\"]}";

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        body.write(metadata.getBytes(StandardCharsets.UTF_8));
        body.write(("\r\n--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write("Content-Type: application/zip\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        body.write(Files.readAllBytes(file));
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"))
                .timeout(Duration.ofMinutes(2))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "multipart/related; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Google Drive upload failed: HTTP " + response.statusCode() + " " + response.body());
        }
        logger.info("Database backup uploaded to Google Drive: {}", fileName);
    }

    private static PrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
        String key = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private static String jsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return "";
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        int firstQuote = json.indexOf('"', colon + 1);
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = firstQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                value.append(c);
                escaped = false;
            } else if (c == '\\') {
                value.append(c);
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static long secondsUntilNextRun() {
        LocalTime backupTime = getBackupTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDateTime.of(LocalDate.now(), backupTime);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Math.max(60, Duration.between(now, next).getSeconds());
    }

    private static LocalTime getBackupTime() {
        String configured = getConfigValue("baletpos.backup.time", "BALETPOS_BACKUP_TIME");
        if (configured == null || configured.isBlank()) {
            return LocalTime.of(23, 0);
        }
        try {
            return LocalTime.parse(configured.trim());
        } catch (Exception e) {
            logger.warn("Invalid backup time '{}', using 23:00", configured);
            return LocalTime.of(23, 0);
        }
    }

    private static boolean hasBackupToday() {
        try {
            Path dir = getBackupDir();
            if (!Files.exists(dir)) {
                return false;
            }
            LocalDate today = LocalDate.now();
            try (var stream = Files.list(dir)) {
                return stream
                        .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip"))
                        .anyMatch(path -> {
                            try {
                                return Files.getLastModifiedTime(path)
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                        .equals(today);
                            } catch (IOException e) {
                                return false;
                            }
                        });
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static Path getBackupDir() {
        String configured = getConfigValue("baletpos.backup.dir", "BALETPOS_BACKUP_DIR");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), ".baletpos", "backups");
    }

    private static boolean isEnabled() {
        String configured = getConfigValue("baletpos.backup.enabled", "BALETPOS_BACKUP_ENABLED");
        return configured == null || configured.isBlank() || configured.equalsIgnoreCase("true");
    }

    private static String getConfigValue(String propertyName, String envName) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return LocalAppConfig.get(propertyName);
    }
}
