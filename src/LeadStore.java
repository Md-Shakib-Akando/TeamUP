import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * LeadStore handles validation and thread-safe persistent storage of lead entries
 * to data/leads.csv. Enforces CSV injection prevention and RFC-4180 escaping.
 */
public class LeadStore {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path CSV_PATH = DATA_DIR.resolve("leads.csv");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_+%-]+(\\.[A-Za-z0-9_+%-]+)*@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
    );
    private static final Object LOCK = new Object();

    static {
        ensureFileExists();
    }

    private static void ensureFileExists() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            if (!Files.exists(CSV_PATH)) {
                String header = "timestamp,email,role,source\n";
                Files.write(CSV_PATH, header.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            System.err.println("[LeadStore] Warning: could not initialize leads.csv: " + e.getMessage());
        }
    }

    public static class ValidationResult {
        public final boolean success;
        public final String message;

        public ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /** Validates standard email address syntax (e.g. abcd@gmail.com). */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String cleanEmail = email.trim();
        if (cleanEmail.isEmpty() || cleanEmail.length() > 254) return false;
        int at = cleanEmail.indexOf('@');
        if (at <= 0 || at > 64) return false;
        return EMAIL_PATTERN.matcher(cleanEmail).matches();
    }

    public static ValidationResult saveLead(String email, String role) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Please enter your email address (e.g. abcd@gmail.com).");
        }
        String cleanEmail = email.trim();
        if (cleanEmail.length() > 254) {
            return new ValidationResult(false, "Email is too long (max 254 characters).");
        }
        if (!isValidEmail(cleanEmail)) {
            return new ValidationResult(false, "Invalid email structure - please enter a valid email (e.g. abcd@gmail.com).");
        }

        String cleanRole = (role == null || role.trim().isEmpty()) ? "Student" : role.trim();
        if (cleanRole.length() > 64) {
            cleanRole = cleanRole.substring(0, 64);
        }

        String safeEmail = sanitizeForCsv(cleanEmail);
        String safeRole = sanitizeForCsv(cleanRole);
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        String record = escapeCsvField(timestamp) + ","
                      + escapeCsvField(safeEmail) + ","
                      + escapeCsvField(safeRole) + ","
                      + escapeCsvField("LandingPage-v1") + "\n";

        synchronized (LOCK) {
            try {
                if (!Files.exists(DATA_DIR)) {
                    Files.createDirectories(DATA_DIR);
                }
                if (!Files.exists(CSV_PATH)) {
                    String header = "timestamp,email,role,source\n";
                    Files.write(CSV_PATH, header.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
                Files.write(CSV_PATH, record.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return new ValidationResult(true, "Thank you for joining! We'll notify you as soon as spots open.");
            } catch (IOException ex) {
                return new ValidationResult(false, "Unable to save lead at this time. Please try again later.");
            }
        }
    }

    /**
     * CSV Injection guard: If a field starts with =, +, -, @, \t, or \r, prefix with a single quote.
     */
    private static String sanitizeForCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        boolean mustQuote = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        String escaped = field.replace("\"", "\"\"");
        if (mustQuote) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}

