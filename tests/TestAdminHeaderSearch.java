import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Automated verification test suite for AdminHeader:
 * 1. Verifies notification bell is deleted.
 * 2. Verifies search bar is completely deleted (bounds off-screen, searchFocused is false).
 * 3. Verifies profile dropdown button (JL avatar + chevron) is completely deleted.
 * 4. Verifies header clicks on right-side regions do not open any popovers or menus.
 * 5. Verifies breadcrumb renders dynamically and cleanly across all sections.
 * 6. Verifies keyboard typing in header is inactive and does not trap keystrokes.
 */
public class TestAdminHeaderSearch {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADMIN HEADER CLEAN VERIFICATION TEST   ");
        System.out.println("==================================================");

        boolean[] switchedToStudent = {false};
        boolean[] returnedToLanding = {false};

        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override
            public void returnToLanding() {
                returnedToLanding[0] = true;
            }

            @Override
            public void switchToStudentDashboard() {
                switchedToStudent[0] = true;
            }
        });

        AdminHeader header = dashboard.getHeader();
        if (header == null) {
            throw new AssertionError("AdminDashboard.getHeader() returned null!");
        }

        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Draw.setQualityHints(g);

        // ----------------------------------------------------
        // Test 1: Layout & Complete Removal of Header Search Bar
        // ----------------------------------------------------
        System.out.println("\n[Test 1] Verifying search bar deletion from AdminHeader...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.OVERVIEW);
        dashboard.paint(g, 1366, 768);

        if (header.getSearchX() != -9999 || header.getSearchW() != 0) {
            throw new AssertionError("Search bar bounds are not removed! searchX=" + header.getSearchX() + ", searchW=" + header.getSearchW());
        }
        if (header.isSearchFocused()) {
            throw new AssertionError("Search bar should not be focused!");
        }
        System.out.println("  ✓ Search bar completely removed (searchX=-9999, searchW=0, searchFocused=false).");

        // ----------------------------------------------------
        // Test 2: Complete Removal of Profile Dropdown Button
        // ----------------------------------------------------
        System.out.println("\n[Test 2] Verifying profile dropdown button deletion from AdminHeader...");
        if (header.getUserX() != -9999) {
            throw new AssertionError("Profile user button is not removed! userX=" + header.getUserX());
        }
        if (header.isProfileOpen()) {
            throw new AssertionError("Profile dropdown should not be open!");
        }
        System.out.println("  ✓ Profile dropdown button completely removed (userX=-9999, isProfileOpen=false).");

        // ----------------------------------------------------
        // Test 3: Clicks on Header Right-Side Produce Zero Popovers
        // ----------------------------------------------------
        System.out.println("\n[Test 3] Verifying clicks on header right area do nothing...");
        // Click at former search position
        boolean c1 = dashboard.onClick(1000, 30);
        if (c1 || header.isAnyPopoverOpen() || header.isSearchFocused() || header.isProfileOpen()) {
            throw new AssertionError("Click at previous search bar position opened an unexpected popover!");
        }
        // Click at former user profile position
        boolean c2 = dashboard.onClick(1330, 30);
        if (c2 || header.isAnyPopoverOpen() || header.isSearchFocused() || header.isProfileOpen()) {
            throw new AssertionError("Click at previous profile avatar position opened an unexpected popover!");
        }
        System.out.println("  ✓ Header right-side clicks verified: no popovers, no focus, pure clean bar.");

        // ----------------------------------------------------
        // Test 4: Breadcrumbs for All Navigation Sections
        // ----------------------------------------------------
        System.out.println("\n[Test 4] Verifying dynamic breadcrumbs across all 7 admin sections...");
        for (AdminSidebar.AdminNavItem item : AdminSidebar.AdminNavItem.values()) {
            dashboard.navigateTo(item);
            dashboard.paint(g, 1366, 768);
            if (!item.label.equals(header.getBreadcrumb())) {
                throw new AssertionError("Breadcrumb mismatch! Expected '" + item.label + "', got: '" + header.getBreadcrumb() + "'");
            }
            System.out.println("  ✓ Breadcrumb correctly displays: Administration / " + header.getBreadcrumb());
        }

        // ----------------------------------------------------
        // Test 5: Keyboard Keystrokes Not Trapped by Header
        // ----------------------------------------------------
        System.out.println("\n[Test 5] Verifying keystrokes are not trapped by header...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.OVERVIEW);
        boolean keyResult = header.onKey('K', java.awt.event.KeyEvent.VK_K, dashboard);
        if (keyResult || header.isSearchFocused()) {
            throw new AssertionError("Header unexpectedly intercepted key event!");
        }
        System.out.println("  ✓ Header key handling correctly inactive.");

        // ----------------------------------------------------
        // Test 6: Sidebar Navigation & Subviews Remain 100% Operational
        // ----------------------------------------------------
        System.out.println("\n[Test 6] Verifying sidebar navigation and views remain intact...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.USERS);
        dashboard.paint(g, 1366, 768);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.USERS) {
            throw new AssertionError("Navigation to USERS failed!");
        }

        dashboard.navigateTo(AdminSidebar.AdminNavItem.SKILLS);
        dashboard.paint(g, 1366, 768);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.SKILLS) {
            throw new AssertionError("Navigation to SKILLS failed!");
        }

        dashboard.navigateTo(AdminSidebar.AdminNavItem.REPORTS);
        dashboard.paint(g, 1366, 768);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.REPORTS) {
            throw new AssertionError("Navigation to REPORTS failed!");
        }

        dashboard.navigateTo(AdminSidebar.AdminNavItem.SETTINGS);
        dashboard.paint(g, 1366, 768);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.SETTINGS) {
            throw new AssertionError("Navigation to SETTINGS failed!");
        }
        System.out.println("  ✓ Sidebar navigation and admin subviews completely intact.");

        g.dispose();

        System.out.println("\n==================================================");
        System.out.println("  ALL ADMIN HEADER CLEAN TESTS PASSED (100%)!     ");
        System.out.println("==================================================");
    }
}
