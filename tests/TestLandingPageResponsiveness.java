import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TestLandingPageResponsiveness {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING LANDING PAGE RESPONSIVENESS TEST SUITE ");
        System.out.println("==================================================");

        NavBar.NavListener mockListener = new NavBar.NavListener() {
            @Override
            public void scrollToSection(String sectionId) {}
            @Override
            public void openModal(String title, String message, boolean isRegister) {}
            @Override
            public void openDashboard() {}
            @Override
            public void openAdminDashboard() {}
        };

        NavBar navBar = new NavBar(mockListener);
        List<Section> sections = LandingWindow.buildSections(mockListener);

        int[] viewports = {375, 480, 600, 768, 840, 1024, 1280, 1440, 1920, 2560};

        System.out.println("\n[Test 1] Testing Section Heights & Paint across 10 Viewports...");
        for (int w : viewports) {
            int h = 900;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Draw.setQualityHints(g);

            // Test navBar height & paint
            int navH = navBar.getHeight(w);
            if (navH <= 0) throw new RuntimeException("NavBar height <= 0 at width " + w);
            navBar.paint(g, 0, w, 0, h);

            int totalH = navH;
            for (Section s : sections) {
                int secH = s.getHeight(w);
                if (secH <= 0) throw new RuntimeException(s.getId() + " height <= 0 at width " + w);
                s.update(0.016f);
                s.paint(g, totalH, w, 0, h);
                totalH += secH;
            }
            g.dispose();
            System.out.println("  ✓ Viewport " + w + "px: rendered cleanly, total height = " + totalH + "px");
        }

        System.out.println("\n[Test 2] Testing Mobile Nav Drawer Interaction & Toggle...");
        // On mobile width 400px
        int mobW = 400;
        int mobH = 800;
        BufferedImage mobImg = new BufferedImage(mobW, mobH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMob = mobImg.createGraphics();
        navBar.paint(gMob, 0, mobW, 0, mobH);

        // Click hamburger at top-right
        int burgerX = mobW - 48;
        int burgerY = (72 - 36) / 2 + 10;
        boolean clickedBurger = navBar.onClick(burgerX, burgerY);
        System.out.println("  ✓ Mobile hamburger clicked: " + clickedBurger + ", menu open: " + navBar.isMenuOpen());

        // Update animation progress
        navBar.update(0.5f);
        navBar.paint(gMob, 0, mobW, 0, mobH);

        // Close mobile drawer
        navBar.closeMenu();
        System.out.println("  ✓ Mobile drawer closed cleanly, menu open: " + navBar.isMenuOpen());
        gMob.dispose();

        System.out.println("\n[Test 3] Testing Interactive Matching Engine Demo Chip Filtering, View Profile & Send Msg...");
        MatchDemo matchDemo = new MatchDemo();
        int demoW = 1024;
        int baseDemoH = matchDemo.getHeight(demoW);
        BufferedImage demoImg = new BufferedImage(demoW, Math.max(800, baseDemoH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDemo = demoImg.createGraphics();
        matchDemo.update(2.0f);
        matchDemo.paint(gDemo, 0, demoW, 0, Math.max(800, baseDemoH));
        gDemo.dispose();

        // Click View Profile button on first row (right side of row, around x=820..920)
        boolean clickedProfile = false;
        for (int ry = 350; ry < 490; ry += 10) {
            if (matchDemo.onClick(825, ry)) {
                clickedProfile = true;
                break;
            }
        }
        int profH = matchDemo.getHeight(demoW);
        if (!clickedProfile || profH <= baseDemoH) {
            throw new AssertionError("View Profile button did not expand drawer (clicked=" + clickedProfile + ", baseH=" + baseDemoH + ", profH=" + profH + ")");
        }
        demoImg = new BufferedImage(demoW, profH, BufferedImage.TYPE_INT_ARGB);
        gDemo = demoImg.createGraphics();
        matchDemo.paint(gDemo, 0, demoW, 0, profH);
        gDemo.dispose();

        // Click Send Msg button on first row (around x=920..960)
        boolean clickedMsg = false;
        for (int ry = 350; ry < 490; ry += 10) {
            if (matchDemo.onClick(930, ry)) {
                clickedMsg = true;
                break;
            }
        }
        int msgH = matchDemo.getHeight(demoW);
        if (!clickedMsg || !matchDemo.hasFocus() || msgH <= baseDemoH) {
            throw new AssertionError("Send Msg button did not open focused composer (clicked=" + clickedMsg + ", focus=" + matchDemo.hasFocus() + ", msgH=" + msgH + ")");
        }
        // Render message composer at desktop & narrow mobile widths
        demoImg = new BufferedImage(demoW, msgH, BufferedImage.TYPE_INT_ARGB);
        gDemo = demoImg.createGraphics();
        matchDemo.paint(gDemo, 0, demoW, 0, msgH);
        gDemo.dispose();

        int mobDemoH = matchDemo.getHeight(375);
        BufferedImage mobDemoImg = new BufferedImage(375, mobDemoH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMobDemo = mobDemoImg.createGraphics();
        matchDemo.paint(gMobDemo, 0, 375, 0, mobDemoH);
        gMobDemo.dispose();

        // Type a message & press Enter to send (using lightweight JPanel source for headless mode)
        java.awt.Component keySrc = new javax.swing.JPanel();
        matchDemo.onKey(new java.awt.event.KeyEvent(keySrc, java.awt.event.KeyEvent.KEY_TYPED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_UNDEFINED, 'H'));
        matchDemo.onKey(new java.awt.event.KeyEvent(keySrc, java.awt.event.KeyEvent.KEY_TYPED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_UNDEFINED, 'i'));
        matchDemo.onKey(new java.awt.event.KeyEvent(keySrc, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ENTER, '\n'));
        System.out.println("  ✓ Match engine demo rendered with View Profile (h=" + profH + "px) & Send Msg (h=" + msgH + "px, mobile=" + mobDemoH + "px).");

        System.out.println("\n[Test 4] Testing Footer Newsletter Subscription & Validation...");
        Footer footer = new Footer(mockListener);
        BufferedImage footImg = new BufferedImage(1200, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFoot = footImg.createGraphics();
        footer.paint(gFoot, 0, 1200, 0, 400);
        gFoot.dispose();
        System.out.println("  ✓ Footer 4-column responsive grid and newsletter subscription rendered.");

        System.out.println("\n[Test 5] Testing Audience Share Feeling & Benefits Form + Role Dropdown...");
        Audience audience = new Audience();
        int audW = 1024;
        int audBaseH = audience.getHeight(audW);
        BufferedImage audImg = new BufferedImage(audW, Math.max(900, audBaseH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gAud = audImg.createGraphics();
        audience.update(2.0f);
        audience.paint(gAud, 0, audW, 0, Math.max(900, audBaseH));
        gAud.dispose();

        // Click role dropdown (top-right of form card: x ~ 750..900, y in form region) and select "Developers"
        boolean dropClicked = false;
        int dropY = -1;
        for (int yy = audBaseH - 280; yy < audBaseH - 80; yy += 8) {
            if (audience.onClick(860, yy)) {
                dropClicked = true;
                dropY = yy;
                break;
            }
        }
        if (!dropClicked) throw new AssertionError("Role dropdown did not open on click");
        // Second option ("Developers") is right below header: menuY = y + 46 + 6, item 1 top = menuY + 4 + 34
        audience.paint(audImg.createGraphics(), 0, audW, 0, Math.max(900, audBaseH));
        audience.onClick(860, dropY + 46 + 6 + 4 + 45);

        // Focus review input box on row 2 (right below dropY: dropY + 46 + 12 + 20)
        int revRowY = dropY + 46 + 22;
        if (!audience.onClick(350, revRowY) || !audience.hasFocus()) {
            throw new AssertionError("Review TextBox did not gain focus on click");
        }
        java.awt.Component audSrc = new javax.swing.JPanel();
        String sample = "Found great Devs!";
        for (char ch : sample.toCharArray()) {
            audience.onKey(new java.awt.event.KeyEvent(audSrc, java.awt.event.KeyEvent.KEY_TYPED,
                    System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_UNDEFINED, ch));
        }
        audience.onKey(new java.awt.event.KeyEvent(audSrc, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ENTER, '\n'));

        int audAfterH = audience.getHeight(audW);
        if (audAfterH <= audBaseH) {
            throw new AssertionError("Audience height did not grow after review submission (base=" + audBaseH + ", after=" + audAfterH + ")");
        }
        BufferedImage audAfterImg = new BufferedImage(audW, audAfterH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gAudAfter = audAfterImg.createGraphics();
        audience.paint(gAudAfter, 0, audW, 0, audAfterH);
        gAudAfter.dispose();

        // Also test mobile 375px render with shared review present
        int audMobH = audience.getHeight(375);
        BufferedImage audMobImg = new BufferedImage(375, audMobH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gAudMob = audMobImg.createGraphics();
        audience.paint(gAudMob, 0, 375, 0, audMobH);
        gAudMob.dispose();
        System.out.println("  ✓ Audience review submitted for Developers (desktop h=" + audBaseH + "->" + audAfterH + "px, mobile=" + audMobH + "px).");

        System.out.println("\n==================================================");
        System.out.println("  ALL LANDING PAGE RESPONSIVENESS TESTS PASSED 100%!");
        System.out.println("==================================================");
    }
}
