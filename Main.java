/**
 * Entry point for TeamUp Landing Page desktop application.
 * Pure core Java - drawn on java.awt.Canvas with double buffering.
 */
public class Main {
    public static void main(String[] args) {
        // Configure high-DPI and anti-aliasing hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("sun.java2d.opengl", "true");

        LandingWindow window = new LandingWindow();
        if (args.length > 0 && ("--dashboard".equalsIgnoreCase(args[0]) || "-d".equalsIgnoreCase(args[0]))) {
            window.showDashboard = true;
        }
        window.start();
    }
}

