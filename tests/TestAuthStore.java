import java.nio.file.*;

public class TestAuthStore {
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  STARTING AUTHSTORE PBKDF2 & AUTH TEST SUITE     ");
        System.out.println("==================================================");

        // Force offline mode for unit testing PBKDF2 crypto and local CSV isolation
        AuthStore.setOfflineMode(true);

        // 1. Test PBKDF2 hashing & random salt uniqueness
        byte[] salt1 = AuthStore.generateSalt();
        byte[] salt2 = AuthStore.generateSalt();
        String hexSalt1 = AuthStore.toHex(salt1);
        String hexSalt2 = AuthStore.toHex(salt2);
        assertCond(!hexSalt1.equals(hexSalt2), "Random salts must be distinct");
        assertCond(salt1.length == 16, "Salt must be 16 bytes");

        byte[] hash1 = AuthStore.pbkdf2("Secret123!".toCharArray(), salt1);
        byte[] hash2 = AuthStore.pbkdf2("Secret123!".toCharArray(), salt2);
        String hexHash1 = AuthStore.toHex(hash1);
        String hexHash2 = AuthStore.toHex(hash2);
        assertCond(hash1.length == 32, "PBKDF2 SHA-256 hash must be 32 bytes (256 bits)");
        assertCond(!hexHash1.equals(hexHash2), "Same password with different salts must produce different hashes");

        assertCond(AuthStore.verifyPassword("Secret123!".toCharArray(), hexSalt1, hexHash1), "Correct password must verify");
        assertCond(!AuthStore.verifyPassword("Wrong123!".toCharArray(), hexSalt1, hexHash1), "Wrong password must fail verify");

        // 2. Test validation & register/login roundtrip
        String testEmail = "testuser_" + System.currentTimeMillis() + "@university.edu";
        AuthStore.AuthResult weakPass = AuthStore.register("Test User", testEmail, "123", "Developer");
        assertCond(!weakPass.success, "Short password should be rejected");

        AuthStore.AuthResult badEmail = AuthStore.register("Test User", "not-an-email", "password123", "Developer");
        assertCond(!badEmail.success, "Invalid email should be rejected");

        AuthStore.AuthResult regOk = AuthStore.register("Test User", testEmail, "passWord99", "Designer");
        assertCond(regOk.success && regOk.user != null, "Valid registration should succeed: " + regOk.message);
        assertCond("Designer".equals(regOk.user.role), "Role should match");

        // Verify password is NOT stored in plaintext in data/users.csv
        Path usersCsv = Paths.get("data", "users.csv");
        String csvContent = new String(Files.readAllBytes(usersCsv), java.nio.charset.StandardCharsets.UTF_8);
        assertCond(csvContent.contains(testEmail), "Registered email must be persisted in data/users.csv");
        assertCond(!csvContent.contains("passWord99"), "Plaintext password must NEVER appear in data/users.csv");

        // Duplicate registration should fail
        AuthStore.AuthResult dup = AuthStore.register("Test User", testEmail, "passWord99", "Designer");
        assertCond(!dup.success, "Duplicate email registration must fail");

        // Login with wrong password should fail
        AuthStore.AuthResult wrongLogin = AuthStore.login(testEmail, "wrongPass");
        assertCond(!wrongLogin.success, "Login with wrong password must fail");

        // Login with correct password should succeed
        AuthStore.AuthResult okLogin = AuthStore.login(testEmail, "passWord99");
        assertCond(okLogin.success && okLogin.user != null && "Test User".equals(okLogin.user.name),
                "Login with valid credentials must succeed");

        // Default demo account login should succeed
        AuthStore.AuthResult demoLogin = AuthStore.login("alex@university.edu", "teamup123");
        assertCond(demoLogin.success, "Default demo account login must succeed");

        // Clean up the synthetic test user row from data/users.csv
        java.util.List<String> kept = new java.util.ArrayList<>();
        for (String line : Files.readAllLines(usersCsv)) {
            if (!line.contains(testEmail)) kept.add(line);
        }
        Files.write(usersCsv, kept);

        // 3. Test JSON parsing & escaping utilities
        String sampleJson = "{\"success\":true,\"message\":\"Welcome back!\",\"token\":\"xyz123\",\"user\":{\"name\":\"John\",\"role\":\"Admin\"}}";
        assertCond(AuthStore.extractJsonBoolean(sampleJson, "success"), "JSON boolean true should be extracted");
        assertCond("Welcome back!".equals(AuthStore.extractJsonString(sampleJson, "message")), "JSON message should match");
        assertCond("xyz123".equals(AuthStore.extractJsonString(sampleJson, "token")), "JSON token should match");
        assertCond("Admin".equals(AuthStore.extractJsonString(sampleJson, "role")), "JSON role should match");
        assertCond("hello \\\"world\\\"".equals(AuthStore.escapeJson("hello \"world\"")), "JSON quote escape should match");

        System.out.println("  [OK] PBKDF2-HMAC-SHA256 salting, verification, registration & login passed.");
        System.out.println("  [OK] JSON parser & REST communication helpers validated.");
        System.out.println("ALL AUTHSTORE TESTS PASSED!");
    }

    private static void assertCond(boolean cond, String msg) {
        if (!cond) throw new AssertionError("Assertion failed: " + msg);
    }
}

