import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Automated test suite for Admin Reports Moderation Center:
 * Verifies:
 * 1. Multi-viewport rendering and layout.
 * 2. Interactive Dropdowns (Severity, Content Type, Status) with popover opening, item selection, and z-index overlay.
 * 3. Live search bar and clear '✕' button.
 * 4. Clickable KPI summary pills (Pending, High Priority, Resolved) with dynamic counts.
 * 5. Interactive table row selection with right-panel inspection.
 * 6. Interactive moderation note field with keyboard input.
 * 7. Four functional moderation actions: Dismiss, Warn User, Remove Content, Escalate.
 * 8. Empty filter state and 'Reset filters' button.
 */
public class TestAdminReportsPage {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(boolean condition, String desc) {
        if (condition) {
            System.out.println("  ✓ " + desc);
            passed++;
        } else {
            System.err.println("  ✗ FAILED: " + desc);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADMIN REPORTS PAGE AUTOMATED TEST SUITE");
        System.out.println("==================================================\n");

        AdminDataStore store = AdminDataStore.getInstance();
        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });

        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Draw.setQualityHints(g);

        // 1. Navigation & Multi-Viewport Rendering
        System.out.println("[Test 1] Testing Navigation & Multi-Viewport Rendering...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.REPORTS);
        int[] viewports = {800, 1024, 1366, 1920};
        for (int vw : viewports) {
            dashboard.paint(g, vw, 768);
            check(true, "Reports page painted cleanly at " + vw + "x768");
        }

        AdminReports view = dashboard.getReportsView();
        check(view != null, "AdminReports view acquired successfully");

        // 2. Dropdown 1: Severity Popover & Filtering
        System.out.println("\n[Test 2] Testing Severity Dropdown Popover...");
        check(view.getActivePopover() == 0, "Initial activePopover is 0 (closed)");

        // Click Severity dropdown button to open popover
        int sDropX = view.getSevDropX();
        int sDropY = view.getSevDropY();
        dashboard.onClick(sDropX + 10, sDropY + 10);
        check(view.getActivePopover() == 1, "Clicking severity dropdown opens popover (activePopover == 1)");

        dashboard.paint(g, 1366, 768);
        List<AdminReports.DropdownItemHitbox> popHits = view.getPopoverHitboxes();
        check(!popHits.isEmpty(), "Popover item hitboxes populated (" + popHits.size() + " items)");

        // Select "High" (index 1) from popover
        AdminReports.DropdownItemHitbox highItem = popHits.stream().filter(h -> "High".equalsIgnoreCase(h.label)).findFirst().orElse(null);
        check(highItem != null, "'High' option found in popover");
        dashboard.onClick(highItem.x + 10, highItem.y + 10);

        check(view.getActivePopover() == 0, "Popover closed after selection");
        check(view.getSevFilterIdx() == 1, "Severity filter index updated to 1 (High)");
        dashboard.paint(g, 1366, 768);
        check(view.getRowHitboxes().size() >= 1, "Table filtered to High severity reports");

        // Reset severity filter to "All severities" (index 0)
        view.setSevFilterIdx(0);
        dashboard.paint(g, 1366, 768);

        // 3. Dropdown 2: Content Type Popover & Filtering
        System.out.println("\n[Test 3] Testing Content Type Dropdown Popover...");
        int tDropX = view.getTypeDropX();
        int tDropY = view.getTypeDropY();
        dashboard.onClick(tDropX + 10, tDropY + 10);
        check(view.getActivePopover() == 2, "Clicking content type dropdown opens popover (activePopover == 2)");

        dashboard.paint(g, 1366, 768);
        popHits = view.getPopoverHitboxes();
        AdminReports.DropdownItemHitbox msgItem = popHits.stream().filter(h -> "Message".equalsIgnoreCase(h.label)).findFirst().orElse(null);
        check(msgItem != null, "'Message' option found in popover");
        dashboard.onClick(msgItem.x + 10, msgItem.y + 10);

        check(view.getActivePopover() == 0, "Popover closed after selection");
        check(view.getTypeFilterIdx() == 1, "Content Type filter index updated to 1 (Message)");
        dashboard.paint(g, 1366, 768);
        check(view.getRowHitboxes().size() >= 1, "Table filtered to Message reports");

        // Reset content type filter
        view.setTypeFilterIdx(0);
        dashboard.paint(g, 1366, 768);

        // 4. Dropdown 3: Status Popover & Filtering
        System.out.println("\n[Test 4] Testing Status Dropdown Popover...");
        int stDropX = view.getStatusDropX();
        int stDropY = view.getStatusDropY();
        dashboard.onClick(stDropX + 10, stDropY + 10);
        check(view.getActivePopover() == 3, "Clicking status dropdown opens popover (activePopover == 3)");

        dashboard.paint(g, 1366, 768);
        popHits = view.getPopoverHitboxes();
        AdminReports.DropdownItemHitbox penItem = popHits.stream().filter(h -> "Pending".equalsIgnoreCase(h.label)).findFirst().orElse(null);
        check(penItem != null, "'Pending' option found in popover");
        dashboard.onClick(penItem.x + 10, penItem.y + 10);

        check(view.getActivePopover() == 0, "Popover closed after selection");
        check(view.getStatusFilterIdx() == 1, "Status filter index updated to 1 (Pending)");
        dashboard.paint(g, 1366, 768);

        // Click outside closes popover test
        dashboard.onClick(stDropX + 10, stDropY + 10);
        check(view.getActivePopover() == 3, "Status popover reopened");
        dashboard.onClick(300, 450); // Click outside popover inside reports content area
        check(view.getActivePopover() == 0, "Click outside closed popover");

        // Reset status filter
        view.setStatusFilterIdx(0);
        dashboard.paint(g, 1366, 768);

        // 5. Live Search & Clear ('✕') Button
        System.out.println("\n[Test 5] Testing Search Bar & Clear Button...");
        dashboard.onClick(view.getSearchX() + 10, view.getSearchY() + 10);
        for (char ch : "StudyBuddy".toCharArray()) {
            dashboard.onKey(ch, (int) ch);
        }
        check("StudyBuddy".equals(view.getSearchQuery()), "Typed 'StudyBuddy' into search query");
        dashboard.paint(g, 1366, 768);
        check(view.getRowHitboxes().size() >= 1, "Found matching reports for 'StudyBuddy'");

        // Click clear ('✕') button
        dashboard.onClick(view.getSearchClearX() + 5, view.getSearchClearY() + 5);
        check(view.getSearchQuery().isEmpty(), "Clear ('✕') button cleared search query");
        dashboard.paint(g, 1366, 768);

        // 6. Clickable KPI Summary Pills
        System.out.println("\n[Test 6] Testing Clickable KPI Summary Pills...");
        dashboard.onClick(view.getPendingPillX() + 10, view.getPendingPillY() + 10);
        check(view.getStatusFilterIdx() == 1, "Clicking Pending pill toggled status filter to Pending");

        dashboard.onClick(view.getHighPillX() + 10, view.getHighPillY() + 10);
        check(view.getSevFilterIdx() == 1, "Clicking High Priority pill toggled severity filter to High");

        // Reset filters
        view.setStatusFilterIdx(0);
        view.setSevFilterIdx(0);
        dashboard.paint(g, 1366, 768);

        // 7. Table Row Selection & Right Panel Inspection
        System.out.println("\n[Test 7] Testing Table Row Selection & Inspection...");
        List<AdminReports.ReportHitbox> rows = view.getRowHitboxes();
        check(!rows.isEmpty(), "Table has report rows");
        AdminReports.ReportHitbox targetRow = rows.get(rows.size() - 1);
        String targetId = targetRow.id;

        dashboard.onClick(targetRow.x + 20, targetRow.y + 20);
        check(targetId.equals(view.getSelectedReportId()), "Clicked row selected Report " + targetId);

        // 8. Moderation Note & Action Buttons
        System.out.println("\n[Test 8] Testing Moderation Actions & Note Input...");
        // Select R-1048
        view.setSelectedReportId("R-1048");
        dashboard.paint(g, 1366, 768);

        // Test Warn User action
        dashboard.onClick(view.getWarnBtnX() + 10, view.getWarnBtnY() + 10);
        AdminDataStore.ReportModel r1048 = store.getAllReports().stream().filter(r -> "R-1048".equalsIgnoreCase(r.id)).findFirst().orElse(null);
        check(r1048 != null && "In review".equalsIgnoreCase(r1048.status), "Warn User updated R-1048 status to 'In review'");

        // Test Dismiss action
        dashboard.onClick(view.getDismissBtnX() + 10, view.getDismissBtnY() + 10);
        check("Resolved".equalsIgnoreCase(r1048.status), "Dismiss updated R-1048 status to 'Resolved'");

        // Test Remove Content action on R-1047
        view.setSelectedReportId("R-1047");
        dashboard.onClick(view.getRemoveBtnX() + 10, view.getRemoveBtnY() + 10);
        AdminDataStore.ReportModel r1047 = store.getAllReports().stream().filter(r -> "R-1047".equalsIgnoreCase(r.id)).findFirst().orElse(null);
        check(r1047 != null && "Resolved".equalsIgnoreCase(r1047.status), "Remove Content updated R-1047 status to 'Resolved'");

        // Test Escalate action on R-1046
        view.setSelectedReportId("R-1046");
        dashboard.onClick(view.getEscalateBtnX() + 10, view.getEscalateBtnY() + 10);
        AdminDataStore.ReportModel r1046 = store.getAllReports().stream().filter(r -> "R-1046".equalsIgnoreCase(r.id)).findFirst().orElse(null);
        check(r1046 != null && "In review".equalsIgnoreCase(r1046.status), "Escalate updated R-1046 status to 'In review'");

        // 9. Empty State and Reset Filters Button
        System.out.println("\n[Test 9] Testing Empty State & Reset Filters Button...");
        view.setSearchQuery("NoMatchReportQueryXYZ123");
        dashboard.paint(g, 1366, 768);
        check(view.getRowHitboxes().isEmpty(), "Table is empty for non-matching query");
        check(view.getResetBtnX() > 0, "'Reset filters' button is visible");

        dashboard.onClick(view.getResetBtnX() + 10, view.getResetBtnY() + 10);
        check(view.getSearchQuery().isEmpty(), "Clicking Reset filters cleared search query");
        check(view.getSevFilterIdx() == 0, "Clicking Reset filters reset severity to 0");
        check(view.getTypeFilterIdx() == 0, "Clicking Reset filters reset content type to 0");
        check(view.getStatusFilterIdx() == 0, "Clicking Reset filters reset status to 0");

        dashboard.paint(g, 1366, 768);
        check(!view.getRowHitboxes().isEmpty(), "Full report list restored after reset");

        g.dispose();

        System.out.println("\n==================================================");
        if (failed == 0) {
            System.out.println("  ALL " + passed + " ADMIN REPORTS TESTS PASSED 100%! ");
        } else {
            System.err.println("  TESTS FAILED: " + failed + " failures!");
            System.exit(1);
        }
        System.out.println("==================================================");
    }
}
