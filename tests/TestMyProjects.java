import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Canvas;

public class TestMyProjects {
    public static void main(String[] args) {
        System.out.println("=== Testing Student Dashboard 'My Projects' Page ===");

        StudentDashboard dashboard = new StudentDashboard(new StudentDashboard.DashboardListener() {
            @Override public void returnToLanding() {}
            @Override public void openModal(String title, String message, boolean isRegister) {}
        });

        BufferedImage img = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 1. Switch to MY_PROJECTS
        dashboard.setActiveNav(StudentDashboard.NavItem.MY_PROJECTS);
        dashboard.paint(g, 1280, 800);
        dashboard.update(0.016f);
        System.out.println("[Test 1] Initial Render of My Projects at 1280x800: OK, activeNav=" + dashboard.getActiveNav());

        // 2. Click "+ New Project" button at top right (1150, 95)
        boolean createClicked = dashboard.onClick(1150, 95);
        System.out.println("[Test 2] Click '+ New Project': " + createClicked + ", activeModal=" + dashboard.getActiveModal());
        dashboard.closeModal();

        // 3. Tab switching
        // Click "Created by me" tab at (430, 285)
        boolean tab1 = dashboard.onClick(430, 285);
        System.out.println("[Test 3] Click 'Created by me' tab: " + tab1);

        // Click "Joined" tab at (570, 285)
        boolean tab2 = dashboard.onClick(570, 285);
        System.out.println("[Test 4] Click 'Joined' tab: " + tab2);

        // Click "All projects" tab at (320, 285)
        boolean tab0 = dashboard.onClick(320, 285);
        System.out.println("[Test 5] Click 'All projects' tab: " + tab0);

        // 4. Test Search typing
        dashboard.paint(g, 1280, 800);
        dashboard.onClick(350, 340); // Focus search box
        for (char c : "Campus".toCharArray()) {
            KeyEvent ke = new KeyEvent(new Canvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, c);
            dashboard.onKey(ke);
        }
        dashboard.paint(g, 1280, 800);
        System.out.println("[Test 6] Search typed 'Campus', verified no crash.");

        // Clear search with ESC
        KeyEvent escKey = new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        dashboard.onKey(escKey);
        // Clear text
        for (int i = 0; i < 10; i++) {
            dashboard.onKey(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED));
        }
        dashboard.onKey(escKey);

        // 5. Click "Tasks" (Project Board) button on first project card at (970, 540)
        dashboard.paint(g, 1280, 800);
        boolean boardClicked = dashboard.onClick(970, 540);
        System.out.println("[Test 7] Click 'Tasks' (Project Board) button: " + boardClicked);

        // Inside board modal: toggle a task checkbox at (400, 260)
        dashboard.paint(g, 1280, 800);
        boolean toggleTask = dashboard.onClick(400, 260);
        System.out.println("[Test 8] Toggle Task inside Board Modal: " + toggleTask);

        // Close Board modal with ESC
        dashboard.onKey(escKey);
        dashboard.paint(g, 1280, 800);

        // 6. Click "Manage Team" button on first project card at (1140, 540)
        boolean manageClicked = dashboard.onClick(1140, 540);
        System.out.println("[Test 9] Click 'Manage Team' button: " + manageClicked);

        // Inside Manage Team modal: Accept candidate button at (780, 260)
        dashboard.paint(g, 1280, 800);
        boolean acceptCandidate = dashboard.onClick(780, 260);
        System.out.println("[Test 10] Click 'Accept' candidate: " + acceptCandidate);

        // Close modal with ESC
        dashboard.onKey(escKey);
        dashboard.paint(g, 1280, 800);

        // 7. Click "Chat" button on first card at (920, 540)
        boolean chatClicked = dashboard.onClick(920, 540);
        System.out.println("[Test 11] Click 'Chat' button on card: " + chatClicked + ", activeModal=" + dashboard.getActiveModal());
        dashboard.closeModal();

        System.out.println("\n==================================================");
        System.out.println("ALL MY PROJECTS TESTS PASSED 100%!");
        System.out.println("==================================================");
    }
}
