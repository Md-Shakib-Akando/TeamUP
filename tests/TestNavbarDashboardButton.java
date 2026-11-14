import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

/** The landing navbar shows "Dashboard" only after login, and routes by role (student / admin). */
public class TestNavbarDashboardButton {

    private static int failures = 0;
    private static String lastAction = "";

    private static void check(boolean ok, String label) {
        System.out.println((ok ? "  [PASS] " : "  [FAIL] ") + label);
        if (!ok) failures++;
    }

    private static Rectangle rect(NavBar nav, String name) throws Exception {
        Field f = NavBar.class.getDeclaredField(name);
        f.setAccessible(true);
        return (Rectangle) f.get(nav);
    }

    private static boolean visible(Rectangle r) {
        return r.x >= 0 && r.width > 0;
    }

    private static void click(NavBar nav, Rectangle r) {
        nav.onClick(r.x + r.width / 2, r.y + r.height / 2);
    }

    private static void paint(NavBar nav, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        nav.update(0.5f);
        nav.paint(g, 0, w, 0, h);
        nav.update(0.5f);
        nav.paint(g, 0, w, 0, h);
        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== TestNavbarDashboardButton ===");
        AuthStore.logout();
        NavBar nav = new NavBar(new NavBar.NavListener() {
            @Override public void scrollToSection(String id) {}
            @Override public void openModal(String title, String message, boolean isRegister) { lastAction = "modal:" + title; }
            @Override public void openDashboard() { lastAction = "student"; }
            @Override public void openAdminDashboard() { lastAction = "admin"; }
        });

        // Desktop, logged out
        nav.setLoggedInUser(null);
        paint(nav, 1280, 800);
        check(!visible(rect(nav, "dashR")), "Logged out: no Dashboard button");
        check(!visible(rect(nav, "adminR")), "Logged out: no Admin button");
        check(visible(rect(nav, "loginR")), "Logged out: Log in button visible");
        click(nav, rect(nav, "loginR"));
        check(lastAction.startsWith("modal:"), "Log in opens the login modal (" + lastAction + ")");

        // Desktop, student
        nav.setLoggedInUser(new AuthStore.UserRecord("Priya Shah", "priya@example.com", "Student", "", "", ""));
        paint(nav, 1280, 800);
        check(visible(rect(nav, "dashR")), "Student: Dashboard button visible");
        check(!visible(rect(nav, "loginR")), "Student: Log in hidden");
        lastAction = "";
        click(nav, rect(nav, "dashR"));
        check("student".equals(lastAction), "Student: Dashboard opens student dashboard");

        // Desktop, admin
        nav.setLoggedInUser(new AuthStore.UserRecord("Jordan Lee", "admin@teamup.edu", "Admin", "", "", ""));
        paint(nav, 1280, 800);
        lastAction = "";
        click(nav, rect(nav, "dashR"));
        check("admin".equals(lastAction), "Admin: Dashboard opens admin dashboard");

        // Mobile menu
        NavBar mob = new NavBar(new NavBar.NavListener() {
            @Override public void scrollToSection(String id) {}
            @Override public void openModal(String title, String message, boolean isRegister) { lastAction = "modal"; }
            @Override public void openDashboard() { lastAction = "student"; }
            @Override public void openAdminDashboard() { lastAction = "admin"; }
        });
        mob.setLoggedInUser(null);
        paint(mob, 400, 800);
        click(mob, rect(mob, "burgerR"));
        paint(mob, 400, 800);
        check(!visible(rect(mob, "dashR")) && visible(rect(mob, "loginR")), "Mobile logged out: Log in row, no Dashboard row");

        mob.setLoggedInUser(new AuthStore.UserRecord("Priya Shah", "priya@example.com", "Student", "", "", ""));
        paint(mob, 400, 800);
        check(visible(rect(mob, "dashR")) && !visible(rect(mob, "loginR")), "Mobile logged in: Dashboard row shown");
        lastAction = "";
        click(mob, rect(mob, "dashR"));
        check("student".equals(lastAction) && !mob.isMenuOpen(), "Mobile: Dashboard row opens dashboard and closes menu");

        System.out.println(failures == 0 ? "=== ALL NAVBAR DASHBOARD TESTS PASSED ===" : ("=== " + failures + " FAILURE(S) ==="));
        System.exit(failures == 0 ? 0 : 1);
    }
}
