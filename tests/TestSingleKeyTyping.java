import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class TestSingleKeyTyping {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING STUDENT DASHBOARD TYPING & HEADER TEST ");
        System.out.println("==================================================");

        StudentDashboard dashboard = new StudentDashboard(new StudentDashboard.DashboardListener() {
            @Override public void returnToLanding() {}
            @Override public void openModal(String title, String message, boolean isRegister) {}
        });

        BufferedImage img = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);

        // [Test 1] Verify "+ Create project" button is removed from Dashboard navbar header
        System.out.println("\n[Test 1] Verifying '+ Create project' is removed from navbar header...");
        // Previous top button was around (1080, 30)
        boolean navbarBtnClicked = dashboard.onClick(1080, 30);
        assertCond(!navbarBtnClicked, "Navbar header should not have Create Project button click handler");
        assertCond(dashboard.getActiveModal() == StudentDashboard.ActiveModal.NONE,
                "Navbar click at (1080, 30) must not open Create Project modal");
        System.out.println("  ✓ Confirmed: navbar header '+ Create project' button is completely removed.");

        // [Test 2] Verify Overview '+ Create Project' page button still works cleanly
        System.out.println("\n[Test 2] Verifying page-level '+ Create Project' button on Overview works...");
        dashboard.setActiveNav(StudentDashboard.NavItem.OVERVIEW);
        dashboard.paint(g, 1280, 800);
        boolean overviewBtnClicked = dashboard.onClick(1120, 100);
        assertCond(overviewBtnClicked && dashboard.getActiveModal() == StudentDashboard.ActiveModal.CREATE_PROJECT,
                "Overview page button must open Create Project modal");
        System.out.println("  ✓ Confirmed: Overview page '+ Create Project' opens modal cleanly.");

        // [Test 3] Test Modal Text Field Single-Typing (prevent double typing)
        System.out.println("\n[Test 3] Testing Modal inputs: verifying single character typing per keypress...");
        // Repaint to compute modal bounds
        dashboard.paint(g, 1280, 800);
        // Focus modal title field
        dashboard.onClick(400, 205);
        Canvas dummyCanvas = new Canvas();
        // Simulate real-life AWT keystroke: KEY_PRESSED (handled by dashboard) + KEY_TYPED (skipped by LandingWindow)
        String titleInput = "Rover AI";
        for (char ch : titleInput.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        // Verify via TAB to Description
        dashboard.onKey(new KeyEvent(dummyCanvas, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_TAB, KeyEvent.CHAR_UNDEFINED));
        String descInput = "Autonomous robot";
        for (char ch : descInput.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        // Submit via ENTER on description
        dashboard.onKey(new KeyEvent(dummyCanvas, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        dashboard.paint(g, 1280, 800);

        String createdTitle = dashboard.getProjects().get(0).title;
        System.out.println("  Created project title: '" + createdTitle + "'");
        assertCond("Rover AI".equals(createdTitle),
                "Project title must be 'Rover AI' (was: '" + createdTitle + "') - no double typing!");
        System.out.println("  ✓ Confirmed: Modal title typed with exact 1:1 character accuracy (no double typing).");

        // [Test 4] Test Header Search Bar Single-Typing
        System.out.println("\n[Test 4] Testing Header Search Bar single character typing...");
        dashboard.paint(g, 1280, 800);
        // Click search bar in header (searchX = 363, searchY = 12..48)
        dashboard.onClick(400, 30);
        String headerQuery = "Machine";
        for (char ch : headerQuery.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        String headerText = dashboard.getHeader().getSearchText();
        System.out.println("  Header search text: '" + headerText + "'");
        assertCond("Machine".equals(headerText),
                "Header search must be 'Machine' (was: '" + headerText + "') - no double typing!");
        System.out.println("  ✓ Confirmed: Header search bar typed with exact 1:1 character accuracy.");

        // [Test 5] Test Discover Projects Search Bar Single-Typing
        System.out.println("\n[Test 5] Testing Discover Projects Search Bar single character typing...");
        dashboard.setActiveNav(StudentDashboard.NavItem.DISCOVER_PROJECTS);
        dashboard.paint(g, 1280, 800);
        // Focus projects search box (searchX = 262, searchY = 160, searchH = 40)
        dashboard.onClick(400, 180);
        String projQuery = "Kotlin";
        for (char ch : projQuery.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        String projText = dashboard.getProjectsView().getSearchKeyword();
        System.out.println("  Projects search text: '" + projText + "'");
        assertCond("Kotlin".equals(projText),
                "Projects search must be 'Kotlin' (was: '" + projText + "') - no double typing!");
        System.out.println("  ✓ Confirmed: Projects search bar typed with exact 1:1 character accuracy.");

        // [Test 6] Test Find Teammates Search Bar Single-Typing
        System.out.println("\n[Test 6] Testing Find Teammates Search Bar single character typing...");
        dashboard.setActiveNav(StudentDashboard.NavItem.FIND_TEAMMATES);
        dashboard.paint(g, 1280, 800);
        // Focus teammates search box (searchX = 262, searchY = 160, searchH = 40)
        dashboard.onClick(400, 180);
        String teamQuery = "Design";
        for (char ch : teamQuery.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        String teamText = dashboard.getTeammatesView().getSearchKeyword();
        System.out.println("  Teammates search text: '" + teamText + "'");
        assertCond("Design".equals(teamText),
                "Teammates search must be 'Design' (was: '" + teamText + "') - no double typing!");
        System.out.println("  ✓ Confirmed: Teammates search bar typed with exact 1:1 character accuracy.");

        // [Test 7] Test My Projects Search Bar Single-Typing
        System.out.println("\n[Test 7] Testing My Projects Search Bar single character typing...");
        dashboard.setActiveNav(StudentDashboard.NavItem.MY_PROJECTS);
        dashboard.paint(g, 1280, 800);
        // Focus my projects search box (searchX = 262, searchY = 320, searchH = 38)
        dashboard.onClick(400, 335);
        String myQuery = "Frontend";
        for (char ch : myQuery.toCharArray()) {
            simulateAwtKeystroke(dashboard, dummyCanvas, ch);
        }
        String myText = dashboard.getMyProjectsView().getSearchKeyword();
        System.out.println("  My Projects search text: '" + myText + "'");
        assertCond("Frontend".equals(myText),
                "My Projects search must be 'Frontend' (was: '" + myText + "') - no double typing!");
        System.out.println("  ✓ Confirmed: My Projects search bar typed with exact 1:1 character accuracy.");

        System.out.println("\n==================================================");
        System.out.println("  ALL DASHBOARD TYPING & HEADER TESTS PASSED 100%!");
        System.out.println("==================================================");
    }

    /**
     * Simulates the exact dispatch sequence in LandingWindow:
     * - keyPressed is delegated to dashboard.onKey(e)
     * - keyTyped is safely ignored for showDashboard (returns immediately)
     */
    private static void simulateAwtKeystroke(StudentDashboard dashboard, Canvas c, char ch) {
        int keyCode = Character.toUpperCase(ch);
        KeyEvent kp = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, ch);
        dashboard.onKey(kp);

        // LandingWindow.keyTyped now returns without calling dashboard.onKey(kt), avoiding double typing
    }

    private static void assertCond(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAILED: " + msg);
    }
}
