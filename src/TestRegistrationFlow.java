public class TestRegistrationFlow {
    public static void main(String[] args) {
        System.out.println("=== Testing TeamUp AuthStore Registration Flow ===");

        // Ensure we are testing the online backend
        AuthStore.setOfflineMode(false);
        AuthStore.ensureServerRunning();

        String testName = "Zubair Rahman";
        String testEmail = "zubair_" + System.currentTimeMillis() + "@test.edu";
        String testPass = "securePass123";
        String testRole = "Developer";

        System.out.println("Calling AuthStore.register for: " + testEmail);
        AuthStore.AuthResult regRes = AuthStore.register(testName, testEmail, testPass, testRole);

        System.out.println("Registration success: " + regRes.success);
        System.out.println("Registration message: " + regRes.message);
        if (regRes.user != null) {
            System.out.println("User name: " + regRes.user.name);
            System.out.println("User email: " + regRes.user.email);
            System.out.println("User role: " + regRes.user.role);
        }
        System.out.println("User token: " + (regRes.token != null ? "Present (valid)" : "None"));

        if (!regRes.success) {
            System.err.println("[FAIL] Registration failed!");
            System.exit(1);
        }

        // Test duplicate registration detection
        System.out.println("\nTesting duplicate registration detection...");
        AuthStore.AuthResult dupRes = AuthStore.register(testName, testEmail, testPass, testRole);
        System.out.println("Duplicate registration success: " + dupRes.success);
        System.out.println("Duplicate registration message: " + dupRes.message);
        if (dupRes.success) {
            System.err.println("[FAIL] Duplicate registration should have been rejected!");
            System.exit(1);
        }

        // Test Login
        System.out.println("\nTesting AuthStore.login with newly created user...");
        AuthStore.AuthResult loginRes = AuthStore.login(testEmail, testPass);
        System.out.println("Login success: " + loginRes.success);
        System.out.println("Login message: " + loginRes.message);
        if (!loginRes.success) {
            System.err.println("[FAIL] Login failed!");
            System.exit(1);
        }

        System.out.println("\n>>> ALL REGISTRATION & LOGIN TESTS PASSED SUCCESSFULLY! <<<");
        System.exit(0);
    }
}

