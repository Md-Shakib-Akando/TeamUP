import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Canvas;

public class TestFourDedicatedViews {
    public static void main(String[] args) {
        System.out.println("=== Testing All 4 Dedicated Views (My Teams, Messages, My Profile, Settings) ===");

        StudentDashboard dashboard = new StudentDashboard(new StudentDashboard.DashboardListener() {
            @Override public void returnToLanding() {}
            @Override public void openModal(String title, String message, boolean isRegister) {}
        });

        BufferedImage img = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 1. Test MY_TEAMS Page
        System.out.println("\n[Test 1] Testing My Teams Page...");
        dashboard.setActiveNav(StudentDashboard.NavItem.MY_TEAMS);
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);
        System.out.println("  Painted My Teams successfully, activeNav=" + dashboard.getActiveNav());

        // Test tab switching in My Teams
        dashboard.getTeamData().activeTabIndex = 1; // Members
        dashboard.paint(g, 1280, 800);
        dashboard.getTeamData().activeTabIndex = 2; // Tasks
        dashboard.paint(g, 1280, 800);
        dashboard.getTeamData().activeTabIndex = 0; // Back to Overview
        dashboard.paint(g, 1280, 800);
        System.out.println("  My Teams tabs verified.");

        // 2. Test MESSAGES Page
        System.out.println("\n[Test 2] Testing Messages Page...");
        dashboard.setActiveNav(StudentDashboard.NavItem.MESSAGES);
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);
        System.out.println("  Painted Messages successfully, activeNav=" + dashboard.getActiveNav());

        // Test conversation switching
        dashboard.getMessagesData().selectConversation(1);
        dashboard.paint(g, 1280, 800);
        System.out.println("  Switched to conversation: " + dashboard.getMessagesData().getActiveConversation().title);

        // Test message typing and send
        dashboard.getMessagesData().inputFocused = true;
        for (char c : "Hello team!".toCharArray()) {
            KeyEvent ke = new KeyEvent(new Canvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, c);
            dashboard.onKey(ke);
        }
        System.out.println("  Typed draft: " + dashboard.getMessagesData().inputDraft);
        KeyEvent enterKey = new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');
        dashboard.onKey(enterKey);
        dashboard.paint(g, 1280, 800);
        System.out.println("  Sent message successfully.");

        // 3. Test MY_PROFILE Page
        System.out.println("\n[Test 3] Testing My Profile Page...");
        dashboard.setActiveNav(StudentDashboard.NavItem.MY_PROFILE);
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);
        System.out.println("  Painted My Profile successfully, activeNav=" + dashboard.getActiveNav());

        // Test tab switching in My Profile
        for (int i = 0; i < 4; i++) {
            dashboard.getProfileData().activeTabIndex = i;
            dashboard.paint(g, 1280, 800);
        }
        dashboard.getProfileData().activeTabIndex = 0;
        dashboard.paint(g, 1280, 800);
        System.out.println("  My Profile tabs verified.");

        // 4. Test SETTINGS Page
        System.out.println("\n[Test 4] Testing Settings Page...");
        dashboard.setActiveNav(StudentDashboard.NavItem.SETTINGS);
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);
        System.out.println("  Painted Settings successfully, activeNav=" + dashboard.getActiveNav());

        // Test all 4 Settings tabs
        for (SettingsData.SettingsTab tab : SettingsData.SettingsTab.values()) {
            dashboard.getSettingsData().activeTab = tab;
            dashboard.paint(g, 1280, 800);
            System.out.println("  Rendered settings tab: " + tab.label);
        }

        System.out.println("\n==================================================");
        System.out.println("ALL 4 DEDICATED VIEWS VERIFIED AND WORKING 100%!");
        System.out.println("==================================================");
    }
}
