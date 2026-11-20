import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * AuthStore handles user registration and authentication backed by:
 * 1. Node.js + Express + MongoDB Atlas backend REST API — hosted on Render
 *    (https://teamup-r3oz.onrender.com/api). Override with -Dteamup.api.url=... or the
 *    TEAMUP_API_URL environment variable (e.g. http://localhost:5050/api for local development).
 * 2. Automatic offline fallback with salted PBKDF2-HMAC-SHA256 password hashing
 *    and thread-safe persistent storage in data/users.csv.
 */
public class AuthStore {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_PATH = DATA_DIR.resolve("users.csv");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Object LOCK = new Object();

    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 65536;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Node.js Backend API configuration
    /** The live TeamUp backend (Render). */
    public static final String DEFAULT_API_URL = "https://teamup-r3oz.onrender.com/api";
    private static volatile String apiBaseUrl = System.getProperty("teamup.api.url",
            System.getenv("TEAMUP_API_URL") != null ? System.getenv("TEAMUP_API_URL") : DEFAULT_API_URL);
    private static volatile String currentToken = null;
    private static volatile UserRecord currentUser = null;
    private static volatile boolean offlineMode = false;
    private static Process serverProcess = null;

    public static class UserRecord {
        public final String name;
        public final String email;
        public final String role;
        public final String saltHex;
        public final String hashHex;
        public final String createdAt;

        public UserRecord(String name, String email, String role, String saltHex, String hashHex, String createdAt) {
            this.name = name;
            this.email = email;
            this.role = role;
            this.saltHex = saltHex;
            this.hashHex = hashHex;
            this.createdAt = createdAt;
        }
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final UserRecord user;
        public final String token;

        public AuthResult(boolean success, String message, UserRecord user) {
            this(success, message, user, null);
        }

        public AuthResult(boolean success, String message, UserRecord user, String token) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.token = token;
        }
    }

    static {
        ensureStoreInitialized();
        // Local backend: start it if needed. Hosted backend: wake it up (Render free instances
        // sleep when idle and take up to a minute to start) so it's ready by the time you log in.
        Thread starter = new Thread(() -> {
            if (isLocalApi()) ensureServerRunning();
            else warmUpRemote();
        }, "TeamUp-Backend-Warmup");
        starter.setDaemon(true);
        starter.start();
    }

    public static void setOfflineMode(boolean offline) {
        offlineMode = offline;
    }

    public static boolean isOfflineMode() {
        return offlineMode;
    }

    public static String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static void setApiBaseUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            apiBaseUrl = url.trim();
        }
    }

    public static String getCurrentToken() {
        return currentToken;
    }

    public static UserRecord getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        currentToken = null;
    }

    private static String connectionErrorMessage() {
        return isLocalApi()
                ? "Could not connect to the local backend at " + apiBaseUrl + ". Run 'npm start' in the server/ folder."
                : "Could not reach the TeamUp server. Check your internet connection and try again.";
    }

    /** True when the API points at this computer (local development), not the hosted backend. */
    public static boolean isLocalApi() {
        try {
            String host = URI.create(apiBaseUrl).getHost();
            return host != null && (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("::1"));
        } catch (Exception e) {
            return false;
        }
    }

    /** One slow health request so a sleeping hosted backend starts booting in the background. */
    private static void warmUpRemote() {
        try {
            String fullUrl = apiBaseUrl.endsWith("/") ? (apiBaseUrl + "health") : (apiBaseUrl + "/health");
            HttpURLConnection conn = (HttpURLConnection) URI.create(fullUrl).toURL().openConnection();
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(70000);
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {
            // offline: the app keeps working with what it has
        }
    }

    /** Starts the local Node server (server/) when the API is local and nothing is listening yet. */
    public static synchronized void ensureServerRunning() {
        if (offlineMode) return;
        if (!isLocalApi()) return; // hosted backend: never spawn a local server
        if (isServerAlive()) return;

        File serverDir = new File("server");
        File serverScript = new File(serverDir, "server.js");
        if (!serverScript.exists()) return;

        try {
            ProcessBuilder pb = new ProcessBuilder("node", "server.js");
            pb.directory(serverDir);
            pb.redirectErrorStream(true);
            serverProcess = pb.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (serverProcess != null && serverProcess.isAlive()) {
                    try {
                        serverProcess.destroy();
                    } catch (Exception ignored) {}
                }
            }));

            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 3500) {
                if (isServerAlive()) {
                    System.out.println("[AuthStore] Node.js backend server auto-started and connected on port 5050.");
                    return;
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            System.err.println("[AuthStore] Note: Could not auto-start node server: " + e.getMessage());
        }
    }

    public static boolean isServerAlive() {
        try {
            String fullUrl = apiBaseUrl.endsWith("/") ? (apiBaseUrl + "health") : (apiBaseUrl + "/health");
            URL url = URI.create(fullUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(isLocalApi() ? 800 : 10000);
            conn.setReadTimeout(isLocalApi() ? 800 : 10000);
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                try (InputStream is = conn.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[1024];
                    int r;
                    while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
                    String body = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    return body.contains("TeamUp");
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static void ensureStoreInitialized() {
        synchronized (LOCK) {
            try {
                if (!Files.exists(DATA_DIR)) {
                    Files.createDirectories(DATA_DIR);
                }
                if (!Files.exists(USERS_PATH)) {
                    String header = "name,email,role,saltHex,hashHex,createdAt\n";
                    Files.write(USERS_PATH, header.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    seedDefaultUsers();
                }
            } catch (IOException e) {
                System.err.println("[AuthStore] Warning: could not initialize users.csv: " + e.getMessage());
            }
        }
    }

    private static void seedDefaultUsers() {
        createUserRecord("Alex Rivera", "alex@university.edu", "Student", "teamup123".toCharArray());
        createUserRecord("Jordan Lee", "admin@teamup.edu", "Admin", "admin123".toCharArray());
    }

    /**
     * Registers a new user directly in MongoDB Atlas via Node.js backend.
     */
    public static AuthResult register(String name, String email, String password, String role) {
        if (name == null || name.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your full name.", null);
        }
        String cleanName = name.trim();
        if (cleanName.length() > 80) cleanName = cleanName.substring(0, 80);

        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your email address.", null);
        }
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        if (cleanEmail.length() > 254 || !EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return new AuthResult(false, "Please enter a valid email address.", null);
        }

        if (password == null || password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.", null);
        }

        String cleanRole = (role == null || role.trim().isEmpty()) ? "Student" : role.trim();
        if (cleanRole.length() > 64) cleanRole = cleanRole.substring(0, 64);

        // 1. Online registration with Node.js + MongoDB Atlas backend
        if (!offlineMode) {
            ensureServerRunning();
            String jsonPayload = "{\"name\":\"" + escapeJson(cleanName) + "\","
                    + "\"email\":\"" + escapeJson(cleanEmail) + "\","
                    + "\"password\":\"" + escapeJson(password) + "\","
                    + "\"role\":\"" + escapeJson(cleanRole) + "\"}";

            try {
                HttpResp resp = postJson("/auth/register", jsonPayload);
                if (resp.status == 503) {
                    try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                    resp = postJson("/auth/register", jsonPayload);
                }
                boolean ok = extractJsonBoolean(resp.body, "success");
                String msg = extractJsonString(resp.body, "message");

                if (resp.status >= 200 && resp.status < 300 && ok) {
                    String token = extractJsonString(resp.body, "token");
                    String retRole = extractJsonString(resp.body, "role");
                    if (retRole == null || retRole.isEmpty()) retRole = cleanRole;
                    String retName = extractJsonString(resp.body, "name");
                    if (retName == null || retName.isEmpty()) retName = cleanName;

                    currentToken = token;
                    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                    UserRecord user = new UserRecord(retName, cleanEmail, retRole, "", "", ts);
                    currentUser = user;

                    // Sync locally so local CSV lookups and offline tests stay consistent
                    synchronized (LOCK) {
                        ensureStoreInitialized();
                        if (findUserByEmail(cleanEmail) == null) {
                            createUserRecord(retName, cleanEmail, retRole, password.toCharArray());
                        }
                    }

                    String successMsg = (msg != null && !msg.isEmpty()) ? msg : ("Account created! Welcome, " + retName + ".");
                    return new AuthResult(true, successMsg, user, token);
                } else {
                    String errorMsg = (msg != null && !msg.isEmpty()) ? msg : "Unable to register account.";
                    return new AuthResult(false, errorMsg, null);
                }
            } catch (IOException e) {
                return new AuthResult(false,
                        connectionErrorMessage(),
                        null);
            }
        }

        // 2. Explicit offline mode fallback: local PBKDF2 hashing in data/users.csv
        synchronized (LOCK) {
            ensureStoreInitialized();
            if (findUserByEmail(cleanEmail) != null) {
                return new AuthResult(false, "An account with this email already exists.", null);
            }
            UserRecord created = createUserRecord(cleanName, cleanEmail, cleanRole, password.toCharArray());
            if (created == null) {
                return new AuthResult(false, "Unable to create account right now.", null);
            }
            currentUser = created;
            return new AuthResult(true, "Account created! Welcome, " + created.name + ".", created);
        }
    }

    /**
     * Authenticates a user against MongoDB Atlas via Node.js backend.
     */
    public static AuthResult login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your email address.", null);
        }
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return new AuthResult(false, "Please enter a valid email address.", null);
        }
        if (password == null || password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.", null);
        }

        // 1. Online authentication with Node.js + MongoDB Atlas backend
        if (!offlineMode) {
            ensureServerRunning();
            String jsonPayload = "{\"email\":\"" + escapeJson(cleanEmail) + "\","
                    + "\"password\":\"" + escapeJson(password) + "\"}";

            try {
                HttpResp resp = postJson("/auth/login", jsonPayload);
                if (resp.status == 503) {
                    try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                    resp = postJson("/auth/login", jsonPayload);
                }
                boolean ok = extractJsonBoolean(resp.body, "success");
                String msg = extractJsonString(resp.body, "message");

                if (resp.status >= 200 && resp.status < 300 && ok) {
                    String token = extractJsonString(resp.body, "token");
                    String retRole = extractJsonString(resp.body, "role");
                    if (retRole == null || retRole.isEmpty()) retRole = "Student";
                    String retName = extractJsonString(resp.body, "name");
                    if (retName == null || retName.isEmpty()) retName = cleanEmail;

                    currentToken = token;
                    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                    UserRecord user = new UserRecord(retName, cleanEmail, retRole, "", "", ts);
                    currentUser = user;

                    String successMsg = (msg != null && !msg.isEmpty()) ? msg : ("Welcome back, " + retName + "!");
                    return new AuthResult(true, successMsg, user, token);
                } else {
                    String errorMsg = (msg != null && !msg.isEmpty()) ? msg : "Invalid email or password.";
                    return new AuthResult(false, errorMsg, null);
                }
            } catch (IOException e) {
                // If backend is down, check local cache for offline convenience
                synchronized (LOCK) {
                    ensureStoreInitialized();
                    UserRecord user = findUserByEmail(cleanEmail);
                    if (user != null && verifyPassword(password.toCharArray(), user.saltHex, user.hashHex)) {
                        currentUser = user;
                        return new AuthResult(true, "Welcome back, " + user.name + "! (Offline Cache)", user);
                    }
                }
                return new AuthResult(false,
                        connectionErrorMessage(),
                        null);
            }
        }

        // 2. Explicit offline mode fallback: local PBKDF2 verification against data/users.csv
        synchronized (LOCK) {
            ensureStoreInitialized();
            UserRecord user = findUserByEmail(cleanEmail);
            if (user == null) {
                return new AuthResult(false, "No account found. Please create an account.", null);
            }
            if (!verifyPassword(password.toCharArray(), user.saltHex, user.hashHex)) {
                return new AuthResult(false, "Incorrect password. Please try again.", null);
            }
            currentUser = user;
            return new AuthResult(true, "Welcome back, " + user.name + "!", user);
        }
    }

    public static UserRecord findUserByEmail(String email) {
        if (email == null) return null;
        String target = email.trim().toLowerCase(Locale.ROOT);
        for (UserRecord u : loadUsers()) {
            if (u.email.equalsIgnoreCase(target)) return u;
        }
        return null;
    }

    private static UserRecord createUserRecord(String name, String email, String role, char[] password) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(password, salt);
        String saltHex = toHex(salt);
        String hashHex = toHex(hash);
        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        String safeName = sanitizeForCsv(name);
        String safeEmail = sanitizeForCsv(email.toLowerCase(Locale.ROOT));
        String safeRole = sanitizeForCsv(role);

        String line = escapeCsvField(safeName) + ","
                + escapeCsvField(safeEmail) + ","
                + escapeCsvField(safeRole) + ","
                + escapeCsvField(saltHex) + ","
                + escapeCsvField(hashHex) + ","
                + escapeCsvField(ts) + "\n";
        try {
            Files.write(USERS_PATH, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new UserRecord(name, email.toLowerCase(Locale.ROOT), role, saltHex, hashHex, ts);
        } catch (IOException e) {
            System.err.println("[AuthStore] Failed to write user record: " + e.getMessage());
            return null;
        }
    }

    private static List<UserRecord> loadUsers() {
        List<UserRecord> list = new ArrayList<>();
        if (!Files.exists(USERS_PATH)) return list;
        try {
            List<String> lines = Files.readAllLines(USERS_PATH, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                List<String> cols = parseCsvLine(line);
                if (cols.size() >= 6) {
                    String name = unsanitizeFromCsv(cols.get(0));
                    String email = unsanitizeFromCsv(cols.get(1));
                    String role = unsanitizeFromCsv(cols.get(2));
                    String saltHex = cols.get(3);
                    String hashHex = cols.get(4);
                    String createdAt = cols.get(5);
                    list.add(new UserRecord(name, email, role, saltHex, hashHex, createdAt));
                }
            }
        } catch (IOException e) {
            System.err.println("[AuthStore] Failed to read users.csv: " + e.getMessage());
        }
        return list;
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] pbkdf2(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(char[] password, String saltHex, String expectedHashHex) {
        byte[] salt = fromHex(saltHex);
        byte[] expected = fromHex(expectedHashHex);
        byte[] actual = pbkdf2(password, salt);
        return MessageDigest.isEqual(actual, expected);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    public static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    // =========================================================================
    // HTTP Communication & Lightweight JSON Helpers (Zero External Dependencies)
    // =========================================================================

    private static class HttpResp {
        final int status;
        final String body;

        HttpResp(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    private static HttpResp postJson(String endpoint, String jsonPayload) throws IOException {
        String fullUrl = apiBaseUrl.endsWith("/") ? (apiBaseUrl.substring(0, apiBaseUrl.length() - 1) + endpoint)
                : (apiBaseUrl + endpoint);
        URL url = URI.create(fullUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        // the hosted backend may need up to a minute to wake from sleep
        conn.setConnectTimeout(isLocalApi() ? 5000 : 20000);
        conn.setReadTimeout(isLocalApi() ? 10000 : 70000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        byte[] bytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String body = "";
        if (is != null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                int r;
                while ((r = is.read(buf)) != -1) {
                    bos.write(buf, 0, r);
                }
                body = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
        }
        return new HttpResp(status, body);
    }

    public static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon == -1) return null;
        int quoteStart = json.indexOf('"', colon + 1);
        if (quoteStart == -1) return null;

        String between = json.substring(colon + 1, quoteStart).trim();
        if (!between.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = quoteStart + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (esc) {
                if (c == '"') sb.append('"');
                else if (c == '\\') sb.append('\\');
                else if (c == 'n') sb.append('\n');
                else if (c == 'r') sb.append('\r');
                else if (c == 't') sb.append('\t');
                else if (c == '/') sb.append('/');
                else sb.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean extractJsonBoolean(String json, String key) {
        if (json == null) return false;
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return false;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon == -1) return false;
        String sub = json.substring(colon + 1).trim();
        return sub.startsWith("true");
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // CSV Utilities
    // =========================================================================

    private static String sanitizeForCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    private static String unsanitizeFromCsv(String value) {
        if (value != null && value.startsWith("'") && value.length() > 1) {
            char second = value.charAt(1);
            if (second == '=' || second == '+' || second == '-' || second == '@' || second == '\t' || second == '\r') {
                return value.substring(1);
            }
        }
        return value;
    }

    private static String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    fields.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        fields.add(cur.toString());
        return fields;
    }
}
