import java.io.IOException;
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
 * AuthStore handles user registration and authentication backed by salted
 * PBKDF2-HMAC-SHA256 password hashing and thread-safe persistent storage in data/users.csv.
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

        public AuthResult(boolean success, String message, UserRecord user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    static {
        ensureStoreInitialized();
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
     * Registers a new user with a unique random 16-byte salt and PBKDF2-HMAC-SHA256 hash.
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

        synchronized (LOCK) {
            ensureStoreInitialized();
            if (findUserByEmail(cleanEmail) != null) {
                return new AuthResult(false, "An account with this email already exists.", null);
            }
            UserRecord created = createUserRecord(cleanName, cleanEmail, cleanRole, password.toCharArray());
            if (created == null) {
                return new AuthResult(false, "Unable to create account right now.", null);
            }
            return new AuthResult(true, "Account created! Welcome, " + created.name + ".", created);
        }
    }

    /**
     * Authenticates a user by computing the PBKDF2 hash with their stored salt
     * and comparing digests in constant time.
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

        synchronized (LOCK) {
            ensureStoreInitialized();
            UserRecord user = findUserByEmail(cleanEmail);
            if (user == null) {
                return new AuthResult(false, "No account found. Please create an account.", null);
            }
            if (!verifyPassword(password.toCharArray(), user.saltHex, user.hashHex)) {
                return new AuthResult(false, "Incorrect password. Please try again.", null);
            }
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

