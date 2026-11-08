/**
 * Entry point for TeamUp Landing Page desktop application.
 * Pure core Java - drawn on java.awt.Canvas with double buffering.
 */
public class Main {
    public static void main(String[] args) {
        // Configure high-DPI and anti-aliasing hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        // Windows: keep the default Direct3D pipeline (measured faster than OpenGL here).
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            System.setProperty("sun.java2d.opengl", "true");
        }

        LandingWindow window = new LandingWindow();
        if (args.length > 0) {
            if ("--admin".equalsIgnoreCase(args[0]) || "-a".equalsIgnoreCase(args[0])) {
                window.showAdminDashboard = true;
            } else if ("--dashboard".equalsIgnoreCase(args[0]) || "-d".equalsIgnoreCase(args[0])) {
                window.showDashboard = true;
            }
        }
        window.start();
    }
}
