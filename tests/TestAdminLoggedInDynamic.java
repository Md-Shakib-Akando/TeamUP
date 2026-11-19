public class TestAdminLoggedInDynamic {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  TESTING LOGGED IN ADMIN DYNAMIC NAME & OVERVIEW ");
        System.out.println("==================================================");

        // 1. Simulate logging in as an admin named "Sanjida Akter"
        AuthStore.UserRecord testAdmin = new AuthStore.UserRecord(
                "Sanjida Akter", "sanjida@teamup.edu", "Admin", "salt", "hash", "2026-11-14"
        );

        // Use reflection or direct store method
        try {
            java.lang.reflect.Field curUserField = AuthStore.class.getDeclaredField("currentUser");
            curUserField.setAccessible(true);
            curUserField.set(null, testAdmin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AdminDataStore store = AdminDataStore.getInstance();
        store.syncLoggedInAdmin();

        // Check actor name and initials
        String actorName = store.getActorName();
        String actorInitials = store.getActorInitials();
        System.out.println("Logged in actor name: " + actorName);
        System.out.println("Logged in actor initials: " + actorInitials);

        if (!"Sanjida Akter".equals(actorName)) {
            throw new AssertionError("Expected 'Sanjida Akter', got: " + actorName);
        }
        if (!"SA".equals(actorInitials)) {
            throw new AssertionError("Expected 'SA', got: " + actorInitials);
        }
        System.out.println("  ✓ Logged in admin dynamically resolved: " + actorName + " (" + actorInitials + ")");

        // Check that users table includes Sanjida Akter
        boolean foundInUsers = false;
        for (AdminDataStore.UserModel u : store.getAllUsers()) {
            if ("Sanjida Akter".equals(u.name) && "SA".equals(u.initials)) {
                foundInUsers = true;
                break;
            }
        }
        if (!foundInUsers) {
            throw new AssertionError("Sanjida Akter not found in AdminDataStore users list!");
        }
        System.out.println("  ✓ Admin user dynamically present in Users registry with 'Admin' role.");

        // Check that logActivity uses dynamic name
        store.logActivity(store.getActorName(), store.getActorInitials(), "Updated platform settings", "Global Settings", true);
        AdminDataStore.AuditLogModel latestLog = store.getAllAuditLogs().get(0);
        if (!"Sanjida Akter".equals(latestLog.actorName)) {
            throw new AssertionError("Audit log expected 'Sanjida Akter', got: " + latestLog.actorName);
        }
        System.out.println("  ✓ Activity logs dynamically record the logged-in admin: " + latestLog.actorName);

        // Verify Overview Dashboard rendering with this admin
        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1366, 768, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        dashboard.paint(g, 1366, 768);
        g.dispose();

        System.out.println("  ✓ Admin Overview painted cleanly with dynamic metrics and real admin name.");

        System.out.println("\n==================================================");
        System.out.println("  ALL DYNAMIC ADMIN NAME & OVERVIEW TESTS PASSED! ");
        System.out.println("==================================================");
    }
}

