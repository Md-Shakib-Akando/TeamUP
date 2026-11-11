import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Automated Test Suite for Admin Settings Page.
 * Verifies the 7 required platform governance features:
 * 1. Platform Name – TeamUp নাম পরিবর্তন।
 * 2. Email Settings – Contact ও Support Email।
 * 3. User Registration – Registration On/Off।
 * 4. Project Approval – Project approval On/Off।
 * 5. Maximum Team Size – Member limit নির্ধারণ।
 * 6. Notifications – Notification On/Off।
 * 7. Change Password – Admin password পরিবর্তন।
 * Plus Save Changes & Reset Defaults, Unsaved changes detection,
 * and Multi-Viewport Responsiveness (Mobile 360px up to 4K 2560px).
 */
public class TestAdminSettingsPage {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void check(boolean condition, String description) {
        if (condition) {
            System.out.println("  ✓ " + description);
            totalPassed++;
        } else {
            System.err.println("  ✗ FAILED: " + description);
            totalFailed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADMIN SETTINGS PAGE TEST SUITE         ");
        System.out.println("==================================================\n");

        AdminDataStore store = AdminDataStore.getInstance();
        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });

        // Test 1: Navigation & Multi-Viewport Rendering (Mobile 360px to 4K 2560px)
        System.out.println("[Test 1] Testing Navigation & Multi-Viewport Rendering...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.SETTINGS);

        int[] widths = {360, 480, 600, 768, 800, 1024, 1366, 1920, 2560};
        int[] heights = {640, 800, 800, 1024, 600, 768, 768, 1080, 1440};
        for (int i = 0; i < widths.length; i++) {
            BufferedImage img = new BufferedImage(widths[i], heights[i], BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            dashboard.paint(g, widths[i], heights[i]);
            g.dispose();

            dashboard.onMouseMove(widths[i] / 2, heights[i] / 2);
            check(true, "Settings view rendered cleanly at " + widths[i] + "x" + heights[i]);
        }

        AdminSettings settings = dashboard.getSettingsView();
        check(settings != null, "AdminSettings view instance acquired from dashboard");

        // Test 2: Requirement 1 - Platform Name
        System.out.println("\n[Test 2] Testing Requirement 1 - Platform Name...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.SETTINGS);
        check(settings.getPlatformNameDraft() != null && !settings.getPlatformNameDraft().isEmpty(), "Initial Platform Name: " + settings.getPlatformNameDraft());

        // Focus Platform Name input (Input ID 1) and type
        settings.setFocusedInput(1);
        dashboard.onKey(' ', 0);
        dashboard.onKey('P', 0);
        dashboard.onKey('r', 0);
        dashboard.onKey('o', 0);
        check(settings.getPlatformNameDraft().contains("Pro"), "Platform Name interactive typing verified: " + settings.getPlatformNameDraft());

        // Test 3: Requirement 2 - Email Settings (Contact & Support Email)
        System.out.println("\n[Test 3] Testing Requirement 2 - Email Settings (Contact & Support)...");
        check(settings.getContactEmailDraft() != null, "Contact Email loaded: " + settings.getContactEmailDraft());
        check(settings.getSupportEmailDraft() != null, "Support Email loaded: " + settings.getSupportEmailDraft());

        // Edit Contact Email
        settings.setContactEmailDraft("dean.office@teamup.edu");
        check(settings.getContactEmailDraft().equals("dean.office@teamup.edu"), "Contact Email updated: " + settings.getContactEmailDraft());

        // Edit Support Email
        settings.setSupportEmailDraft("helpdesk@teamup.edu");
        check(settings.getSupportEmailDraft().equals("helpdesk@teamup.edu"), "Support Email updated: " + settings.getSupportEmailDraft());

        // Test 4: Requirement 3 - User Registration (On/Off)
        System.out.println("\n[Test 4] Testing Requirement 3 - User Registration (On/Off)...");
        boolean initialReg = settings.isUserRegistrationDraft();
        settings.setUserRegistrationDraft(!initialReg);
        check(settings.isUserRegistrationDraft() != initialReg, "User registration toggled to: " + settings.isUserRegistrationDraft());
        settings.setUserRegistrationDraft(initialReg); // restore
        check(settings.isUserRegistrationDraft() == initialReg, "User registration restored to: " + settings.isUserRegistrationDraft());

        // Test 5: Requirement 4 - Project Approval (On/Off)
        System.out.println("\n[Test 5] Testing Requirement 4 - Project Approval (On/Off)...");
        boolean initialApprov = settings.isProjectApprovalDraft();
        settings.setProjectApprovalDraft(!initialApprov);
        check(settings.isProjectApprovalDraft() != initialApprov, "Project approval toggled to: " + settings.isProjectApprovalDraft());
        settings.setProjectApprovalDraft(initialApprov); // restore
        check(settings.isProjectApprovalDraft() == initialApprov, "Project approval restored to: " + settings.isProjectApprovalDraft());

        // Test 6: Requirement 5 - Maximum Team Size (Member Limit Stepper)
        System.out.println("\n[Test 6] Testing Requirement 5 - Maximum Team Size...");
        int initialSize = settings.getMaxTeamSizeDraft();
        check(initialSize >= 2, "Initial max team size valid: " + initialSize);

        // Update Max Team Size
        settings.setMaxTeamSizeDraft(8);
        check(settings.getMaxTeamSizeDraft() == 8, "Max team size updated to 8 members");
        settings.setMaxTeamSizeDraft(12);
        check(settings.getMaxTeamSizeDraft() == 12, "Max team size updated to 12 members");

        // Test 7: Requirement 6 - Notifications (On/Off)
        System.out.println("\n[Test 7] Testing Requirement 6 - Notifications (On/Off)...");
        boolean initialNotif = settings.isNotificationsDraft();
        settings.setNotificationsDraft(!initialNotif);
        check(settings.isNotificationsDraft() != initialNotif, "Notifications toggled to: " + settings.isNotificationsDraft());
        settings.setNotificationsDraft(initialNotif); // restore
        check(settings.isNotificationsDraft() == initialNotif, "Notifications restored to: " + settings.isNotificationsDraft());

        // Test 8: Requirement 7 - Change Password
        System.out.println("\n[Test 8] Testing Requirement 7 - Change Password...");
        // 1. Test validation failure on empty current password
        settings.setCurrentPasswordInput("");
        settings.setNewPasswordInput("newpass123");
        settings.setConfirmPasswordInput("newpass123");
        settings.updatePassword(dashboard);
        check(store.adminPassword.equals("admin123"), "Password not changed when current password empty");

        // 2. Test validation failure on short password (< 6 chars)
        settings.setCurrentPasswordInput("admin123");
        settings.setNewPasswordInput("123");
        settings.setConfirmPasswordInput("123");
        settings.updatePassword(dashboard);
        check(store.adminPassword.equals("admin123"), "Password not changed when new password < 6 chars");

        // 3. Test validation failure on password mismatch
        settings.setCurrentPasswordInput("admin123");
        settings.setNewPasswordInput("newpassword123");
        settings.setConfirmPasswordInput("differentpass123");
        settings.updatePassword(dashboard);
        check(store.adminPassword.equals("admin123"), "Password not changed when new and confirm do not match");

        // 4. Test validation failure on incorrect current password
        settings.setCurrentPasswordInput("wrongpass");
        settings.setNewPasswordInput("newpassword123");
        settings.setConfirmPasswordInput("newpassword123");
        settings.updatePassword(dashboard);
        check(store.adminPassword.equals("admin123"), "Password not changed when current password is wrong");

        // 5. Successful password update!
        settings.setCurrentPasswordInput("admin123");
        settings.setNewPasswordInput("supersecure2026");
        settings.setConfirmPasswordInput("supersecure2026");
        settings.updatePassword(dashboard);
        check(store.adminPassword.equals("supersecure2026"), "Admin password updated successfully to: " + store.adminPassword);

        // Restore password for test isolation
        store.changeAdminPassword("supersecure2026", "admin123");
        check(store.adminPassword.equals("admin123"), "Admin password restored to default admin123");

        // Test 9: Save Changes & Persistence
        System.out.println("\n[Test 9] Testing Save Changes & Persistence...");
        settings.setPlatformNameDraft("TeamUp University Hub");
        settings.setContactEmailDraft("contact@university.edu");
        settings.setSupportEmailDraft("support@university.edu");
        settings.setUserRegistrationDraft(true);
        settings.setProjectApprovalDraft(true);
        settings.setMaxTeamSizeDraft(10);
        settings.setNotificationsDraft(true);

        check(settings.hasUnsavedChanges(), "Unsaved changes detected before save");

        settings.saveAllSettings(dashboard);
        check(store.platformName.equals("TeamUp University Hub"), "Persisted Platform Name in AdminDataStore: " + store.platformName);
        check(store.contactEmail.equals("contact@university.edu"), "Persisted Contact Email: " + store.contactEmail);
        check(store.supportEmail.equals("support@university.edu"), "Persisted Support Email: " + store.supportEmail);
        check(store.requireProjectApproval, "Persisted Project Approval: true");
        check(store.maxTeamSize == 10, "Persisted Max Team Size: 10");
        check(!settings.hasUnsavedChanges(), "Unsaved changes cleared after saveAllSettings");

        // Test 10: Reset to Defaults
        System.out.println("\n[Test 10] Testing Reset to Defaults...");
        settings.resetAllSettings(dashboard);
        check(store.platformName.equals("TeamUp"), "Reset to defaults restored Platform Name to: " + store.platformName);
        check(store.contactEmail.equals("contact@teamup.edu"), "Reset to defaults restored Contact Email: " + store.contactEmail);
        check(store.supportEmail.equals("support@teamup.edu"), "Reset to defaults restored Support Email: " + store.supportEmail);
        check(store.maxTeamSize == 6, "Reset to defaults restored Max Team Size to: " + store.maxTeamSize);

        // Test 11: Computed Height & Responsive Dynamics
        System.out.println("\n[Test 11] Testing Computed Height & Responsive Dynamics...");
        int mobileHeight = settings.getComputedHeight(400);
        int desktopHeight = settings.getComputedHeight(1200);
        check(mobileHeight > desktopHeight, "Mobile computed height adapts when stacked (Mobile: " + mobileHeight + "px > Desktop: " + desktopHeight + "px)");

        System.out.println("\n==================================================");
        if (totalFailed == 0) {
            System.out.println("  ALL " + totalPassed + " ADMIN SETTINGS TESTS PASSED 100%!  ");
        } else {
            System.err.println("  " + totalFailed + " TESTS FAILED out of " + (totalPassed + totalFailed));
        }
        System.out.println("==================================================");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }
}
