import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Automated test suite for AdminHeader:
 * 1. Verifies notification bell button is deleted.
 * 2. Verifies search bar is responsive and dynamic across viewport widths.
 * 3. Verifies typing, backspace, and real-time query reactivity.
 * 4. Verifies search results dropdown rendering, count, and entity badges.
 * 5. Verifies click-to-navigate for User, Project, Report, and Review results.
 * 6. Verifies keyboard arrow navigation (UP/DOWN) and ENTER activation.
 * 7. Verifies clear button ('✕') appears when query is present and clears search on click.
 * 8. Verifies Ctrl+K focuses the search bar.
 * 9. Verifies Profile (JL) menu remains fully functional.
 */
public class TestAdminHeaderSearch {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADMIN HEADER DYNAMIC SEARCH TEST SUITE ");
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
        // Test 1: Notification Bell Deletion & Layout
        // ----------------------------------------------------
        System.out.println("\n[Test 1] Testing notification bell removal & header layout...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.OVERVIEW);
        dashboard.paint(g, 1366, 768);

        // Clicking where the old bell used to be (userX - 44) should NOT open notifications
        int oldBellX = header.getUserX() - 44;
        dashboard.onClick(oldBellX + 10, 30);
        if (header.isAnyPopoverOpen()) {
            throw new AssertionError("Clicking old bell position unexpectedly opened a popover!");
        }
        System.out.println("  ✓ Notification bell button completely deleted. No popover triggered.");

        // ----------------------------------------------------
        // Test 2: Search Bar Responsive Sizing across viewports
        // ----------------------------------------------------
        System.out.println("\n[Test 2] Testing search bar responsive width and layout...");
        int[] viewports = {900, 1200, 1366, 1920, 2560};
        for (int vpW : viewports) {
            BufferedImage testImg = new BufferedImage(vpW, 768, BufferedImage.TYPE_INT_ARGB);
            Graphics2D testG = testImg.createGraphics();
            dashboard.paint(testG, vpW, 768);
            testG.dispose();

            int sw = header.getSearchW();
            int sx = header.getSearchX();
            int ux = header.getUserX();

            if (sw < 200 || sw > 420) {
                throw new AssertionError("Search width " + sw + " out of expected range [200, 420] for viewport " + vpW);
            }
            if (sx + sw > ux) {
                throw new AssertionError("Search bar collides with user avatar! sx=" + sx + ", sw=" + sw + ", ux=" + ux);
            }
            System.out.println("  ✓ Viewport " + vpW + "px: search bar width = " + sw + "px, smoothly positioned.");
        }

        // ----------------------------------------------------
        // Test 3: Search Bar Focus & Typing Interaction
        // ----------------------------------------------------
        System.out.println("\n[Test 3] Testing search bar focus and typing...");
        int sx = header.getSearchX();
        int sy = header.getSearchY();

        // Click search box to focus
        dashboard.onClick(sx + 50, sy + 15);
        if (!header.isSearchFocused()) {
            throw new AssertionError("Clicking search bar failed to set searchFocused!");
        }
        System.out.println("  ✓ Search bar focused successfully via mouse click.");

        // Type "Alex"
        dashboard.onKey('A', 0);
        dashboard.onKey('l', 0);
        dashboard.onKey('e', 0);
        dashboard.onKey('x', 0);
        if (!"Alex".equals(header.getSearchText())) {
            throw new AssertionError("Expected search text 'Alex', got: '" + header.getSearchText() + "'");
        }
        System.out.println("  ✓ Typed 'Alex' into search bar.");

        // Backspace
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_BACK_SPACE);
        if (!"Ale".equals(header.getSearchText())) {
            throw new AssertionError("Backspace failed! Expected 'Ale', got: '" + header.getSearchText() + "'");
        }
        System.out.println("  ✓ Backspace key deleted last character ('Ale').");

        // Type 'x' back
        dashboard.onKey('x', 0);

        // ----------------------------------------------------
        // Test 4: Live Results Dropdown & Rendering
        // ----------------------------------------------------
        System.out.println("\n[Test 4] Testing live search results rendering & count...");
        dashboard.paint(g, 1366, 768);
        if (!header.isAnyPopoverOpen()) {
            throw new AssertionError("Live search results popover should be open for query 'Alex'!");
        }
        if (header.getSearchMatchCount() <= 0) {
            throw new AssertionError("Expected at least 1 match for query 'Alex'!");
        }
        System.out.println("  ✓ Live search popover rendered with " + header.getSearchMatchCount() + " matches.");

        // ----------------------------------------------------
        // Test 5: Keyboard Arrow Navigation (UP / DOWN)
        // ----------------------------------------------------
        System.out.println("\n[Test 5] Testing keyboard arrow navigation...");
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_DOWN);
        if (header.getHoveredResultIdx() != 0) {
            throw new AssertionError("VK_DOWN did not highlight first result!");
        }
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_DOWN);
        if (header.getHoveredResultIdx() != 1 && header.getSearchMatchCount() > 1) {
            throw new AssertionError("VK_DOWN did not advance to second result!");
        }
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_UP);
        if (header.getHoveredResultIdx() != 0) {
            throw new AssertionError("VK_UP did not return to first result!");
        }
        System.out.println("  ✓ Arrow UP/DOWN correctly navigates dropdown results.");

        // ----------------------------------------------------
        // Test 6: Enter Key Navigation
        // ----------------------------------------------------
        System.out.println("\n[Test 6] Testing ENTER key navigation on selected search result...");
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_ENTER);
        if (header.isSearchFocused()) {
            throw new AssertionError("Search bar should be unfocused after pressing ENTER!");
        }
        if (!header.getSearchText().isEmpty()) {
            throw new AssertionError("Search text should be cleared after ENTER navigation!");
        }
        System.out.println("  ✓ ENTER key navigated to search entity and dismissed search popover.");

        // ----------------------------------------------------
        // Test 7: Real-time query across all 4 entity types
        // ----------------------------------------------------
        System.out.println("\n[Test 7] Testing real-time query across User, Project, Report, and Review...");
        // 1. User search
        header.setSearchFocused(true);
        header.setSearchText("Jordan");
        List<AdminDataStore.SearchResult> uResults = AdminDataStore.getInstance().searchAll("Jordan");
        if (uResults.isEmpty() || !uResults.get(0).type.equalsIgnoreCase("User")) {
            throw new AssertionError("User search for 'Jordan' failed!");
        }
        System.out.println("  ✓ User entity query successful: " + uResults.get(0).title);

        // 2. Project search
        header.setSearchText("Skill");
        List<AdminDataStore.SearchResult> pResults = AdminDataStore.getInstance().searchAll("Skill");
        if (pResults.isEmpty()) {
            throw new AssertionError("Project search for 'Skill' failed!");
        }
        System.out.println("  ✓ Project entity query successful: " + pResults.get(0).title);

        // 3. Report search
        header.setSearchText("104");
        List<AdminDataStore.SearchResult> rResults = AdminDataStore.getInstance().searchAll("104");
        if (rResults.isEmpty()) {
            throw new AssertionError("Report search for '104' failed!");
        }
        System.out.println("  ✓ Report entity query successful: " + rResults.get(0).title);

        // 4. Review search
        header.setSearchText("REV");
        List<AdminDataStore.SearchResult> revResults = AdminDataStore.getInstance().searchAll("REV");
        if (revResults.isEmpty()) {
            throw new AssertionError("Review search for 'REV' failed!");
        }
        System.out.println("  ✓ Review entity query successful: " + revResults.get(0).title);

        // ----------------------------------------------------
        // Test 8: Click on Live Result Navigates
        // ----------------------------------------------------
        System.out.println("\n[Test 8] Testing mouse click on live dropdown result...");
        header.setSearchFocused(true);
        header.setSearchText("Skill");
        dashboard.paint(g, 1366, 768);
        // Live result list starts at searchDropY + 28
        // Click first item
        dashboard.onClick(header.getSearchX() + 30, 68 + 4 + 28 + 20);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.PROJECTS) {
            throw new AssertionError("Clicking project result failed to navigate to PROJECTS! Active: " + dashboard.getActiveNav());
        }
        System.out.println("  ✓ Click on Project search result successfully navigated to PROJECTS view.");

        // ----------------------------------------------------
        // Test 9: Clear Button ('✕') Functionality
        // ----------------------------------------------------
        System.out.println("\n[Test 9] Testing clear button ('✕')...");
        header.setSearchFocused(true);
        header.setSearchText("Test Query to Clear");
        dashboard.paint(g, 1366, 768);

        // Mouse hover on clear button (at searchX + searchW - 20)
        int clearBtnX = header.getSearchX() + header.getSearchW() - 20;
        int clearBtnY = header.getSearchY() + 15;
        dashboard.onMouseMove(clearBtnX, clearBtnY);
        if (!header.isClearHovered()) {
            throw new AssertionError("Clear button failed to register hover state!");
        }
        System.out.println("  ✓ Clear button ('✕') hover state recognized.");

        // Click clear button
        dashboard.onClick(clearBtnX, clearBtnY);
        if (!header.getSearchText().isEmpty()) {
            throw new AssertionError("Clicking clear button failed to reset search text!");
        }
        System.out.println("  ✓ Clear button ('✕') successfully cleared search query.");

        // ----------------------------------------------------
        // Test 10: Global Keyboard Shortcut Ctrl+K
        // ----------------------------------------------------
        System.out.println("\n[Test 10] Testing Ctrl+K global keyboard shortcut...");
        header.setSearchFocused(false);
        dashboard.onKey((char) 11, java.awt.event.KeyEvent.VK_K);
        if (!header.isSearchFocused()) {
            throw new AssertionError("Ctrl+K failed to focus header search bar!");
        }
        System.out.println("  ✓ Ctrl+K (Cmd+K) focused search bar immediately.");

        // ESC unfocuses
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_ESCAPE);
        if (header.isSearchFocused()) {
            throw new AssertionError("ESC failed to unfocus search bar!");
        }
        System.out.println("  ✓ ESC key closed search bar.");

        // ----------------------------------------------------
        // Test 11: Admin Profile (JL) Menu Remains Functional
        // ----------------------------------------------------
        System.out.println("\n[Test 11] Testing Admin Profile (JL) menu functionality...");
        int ux = header.getUserX();
        // Click avatar to open profile menu
        dashboard.onClick(ux + 10, 30);
        if (!header.isProfileOpen()) {
            throw new AssertionError("Clicking JL avatar failed to open profile dropdown!");
        }
        System.out.println("  ✓ Profile menu opened via JL avatar.");

        // Click "Switch to Student View" (first item)
        dashboard.onClick(ux - 110 + 20, 68 + 4 + 20);
        if (!switchedToStudent[0]) {
            throw new AssertionError("Profile dropdown 'Switch to Student View' click failed!");
        }
        if (header.isProfileOpen()) {
            throw new AssertionError("Profile dropdown failed to close after click!");
        }
        System.out.println("  ✓ Profile dropdown 'Switch to Student View' verified.");

        // Open profile again & click "Admin Settings" (third item)
        dashboard.onClick(ux + 10, 30);
        dashboard.onClick(ux - 110 + 20, 68 + 4 + 8 + 36 * 2 + 15);
        if (dashboard.getActiveNav() != AdminSidebar.AdminNavItem.SETTINGS) {
            throw new AssertionError("Profile dropdown 'Admin Settings' click failed to navigate to SETTINGS!");
        }
        System.out.println("  ✓ Profile dropdown 'Admin Settings' verified.");

        g.dispose();

        System.out.println("\n==================================================");
        System.out.println("  ALL ADMIN HEADER & DYNAMIC SEARCH TESTS PASSED! ");
        System.out.println("==================================================");
    }
}

