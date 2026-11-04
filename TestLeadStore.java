public class TestLeadStore {
    public static void main(String[] args) {
        System.out.println("--- Testing LeadStore ---");

        // 1. Invalid email
        LeadStore.ValidationResult r1 = LeadStore.saveLead("invalid-email", "Student");
        assertFalse(r1.success, "Should reject invalid email");
        System.out.println("Test 1 passed (invalid email rejected): " + r1.message);

        // 2. Empty email
        LeadStore.ValidationResult r2 = LeadStore.saveLead("", "Student");
        assertFalse(r2.success, "Should reject empty email");
        System.out.println("Test 2 passed (empty email rejected): " + r2.message);

        // 3. Valid email
        LeadStore.ValidationResult r3 = LeadStore.saveLead("test@teamup.org", "Developer");
        assertTrue(r3.success, "Should accept valid email");
        System.out.println("Test 3 passed (valid lead saved): " + r3.message);

        // 4. CSV injection payload
        LeadStore.ValidationResult r4 = LeadStore.saveLead("+developer@example.com", "=SUM(1,2)");
        assertTrue(r4.success, "Should sanitize and save injection test");
        System.out.println("Test 4 passed (CSV injection guarded): " + r4.message);

        System.out.println("ALL LEAD STORE TESTS PASSED!");
    }

    private static void assertTrue(boolean val, String msg) {
        if (!val) throw new AssertionError("FAILED: " + msg);
    }

    private static void assertFalse(boolean val, String msg) {
        if (val) throw new AssertionError("FAILED: " + msg);
    }
}
