import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Pixel-perfect, fully responsive Sticky Glassmorphism Navigation Bar for TeamUp.
 * Adapts fluidly across widescreen (4K/1080p), laptop, tablet, and mobile (<840px).
 * Includes smooth mobile drawer slide animation, active link gliding, and theme toggle.
 */
public class NavBar implements Section {

    public interface NavListener {
        void scrollToSection(String sectionId);
        void openModal(String title, String message, boolean isRegister);
        void openDashboard();
        default void openAdminDashboard() {}
    }

    private final NavListener listener;
    private String activeSection = "hero";
    private boolean mobileMenuOpen = false;
    private float mobileMenuProgress = 0f; // 0f = closed, 1f = open

    // Navigation Link model
    private static class LinkItem {
        String id;
        String label;
        int x, y, w, h;
        boolean hovered;
        LinkItem(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    private final List<LinkItem> links = new ArrayList<LinkItem>();

    // Interactive button bounds
    private int themeX, themeY, themeSize = 34;
    private boolean themeHovered = false;

    private int dashX, dashY, dashW = 104, dashH = 34;
    private boolean dashHovered = false;

    private int adminX, adminY, adminW = 82, adminH = 34;
    private boolean adminHovered = false;

    private int loginX, loginY, loginW = 84, loginH = 34;
    private boolean loginHovered = false;

    private int startX, startY, startW = 120, startH = 34;
    private boolean startHovered = false;

    private int burgerX, burgerY, burgerSize = 36;
    private boolean burgerHovered = false;

    // Mobile quick switcher cards
    private int mobDashX, mobDashY, mobDashW, mobDashH;
    private boolean mobDashHovered = false;
    private int mobAdminX, mobAdminY, mobAdminW, mobAdminH;
    private boolean mobAdminHovered = false;

    // Gliding active indicator
    private float activePillX = -100f;
    private float activePillW = 0f;
    private float targetPillX = -100f;
    private float targetPillW = 0f;

    public NavBar(NavListener listener) {
        this.listener = listener;
        links.add(new LinkItem("features", "Features"));
        links.add(new LinkItem("how-it-works", "How it works"));
        links.add(new LinkItem("demo", "Matching Engine"));
        links.add(new LinkItem("compare", "Compare"));
        links.add(new LinkItem("faq", "FAQ"));
    }

    public void setActiveSection(String id) {
        this.activeSection = id;
    }

    public boolean isMobileMenuOpen() {
        return mobileMenuOpen;
    }

    public void closeMobileMenu() {
        this.mobileMenuOpen = false;
    }

    @Override
    public String getId() {
        return "nav";
    }

    @Override
    public int getHeight(int width) {
        return 72;
    }

    @Override
    public void update(float dt) {
        // Smooth drawer opening/closing physics
        float targetProgress = mobileMenuOpen ? 1.0f : 0.0f;
        mobileMenuProgress = Anim.approach(mobileMenuProgress, targetProgress, dt, 14.0f);

        // Smooth active pill gliding
        activePillX = Anim.approach(activePillX, targetPillX, dt, 16.0f);
        activePillW = Anim.approach(activePillW, targetPillW, dt, 16.0f);
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        boolean isMobile = width < 840;
        boolean isCompactDesktop = width >= 840 && width < 1140;

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // Sticky Glass Surface with scroll shadow
        Color navBg = Theme.navBg();
        g2.setColor(navBg);
        g2.fillRect(0, y, width, h);

        // Elevation line/shadow when scrolled down
        boolean scrolled = (viewportY > 8);
        if (scrolled) {
            g2.setColor(Theme.isDark ? new Color(0, 0, 0, 45) : new Color(0, 0, 0, 12));
            g2.fillRect(0, y + h, width, 6);
        }

        g2.setColor(Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(0, y + h - 1, width, y + h - 1);

        int padX = width > 1200 ? 56 : (width > 600 ? 28 : 16);

        // 1. Logo & Brand
        int logoSize = 34;
        int logoY = y + (h - logoSize) / 2;
        Draw.drawLogo(g2, padX, logoY, logoSize);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(20 * Theme.fontScale)));
        FontMetrics lfm = g2.getFontMetrics();
        int brandX = padX + logoSize + 10;
        int brandY = y + (h - lfm.getHeight()) / 2 + lfm.getAscent();
        g2.setColor(Theme.textPrimary());
        g2.drawString("TeamUp", brandX, brandY);

        // Platform subtitle badge on wide screens
        if (width >= 1280) {
            int subX = brandX + lfm.stringWidth("TeamUp") + 12;
            int subY = y + (h - 22) / 2;
            RoundRectangle2D subPill = new RoundRectangle2D.Float(subX, subY, 78, 22, 11, 11);
            g2.setColor(Theme.isDark ? new Color(255, 255, 255, 12) : new Color(0, 0, 0, 8));
            g2.fill(subPill);
            g2.setFont(Theme.fontSmallBold());
            g2.setColor(Theme.BRAND_PINK);
            g2.drawString("STUDENT", subX + 11, subY + 15);
        }

        if (!isMobile) {
            // 2. Desktop Navigation Links
            g2.setFont(Theme.fontBodyBold());
            FontMetrics fm = g2.getFontMetrics();

            int linkSpacing = isCompactDesktop ? 16 : 24;
            int curX = brandX + lfm.stringWidth("TeamUp") + (width >= 1280 ? 104 : 32);

            boolean foundActive = false;
            for (LinkItem item : links) {
                item.w = fm.stringWidth(item.label) + 16;
                item.h = 32;
                item.x = curX;
                item.y = y + (h - item.h) / 2;

                boolean isActive = item.id.equalsIgnoreCase(activeSection);
                if (isActive) {
                    targetPillX = item.x;
                    targetPillW = item.w;
                    foundActive = true;
                }

                // Hover pill
                if (item.hovered && !isActive) {
                    g2.setColor(Theme.isDark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 6));
                    g2.fill(new RoundRectangle2D.Float(item.x, item.y, item.w, item.h, 10, 10));
                }

                g2.setColor(isActive ? Theme.BRAND_PINK : (item.hovered ? Theme.textPrimary() : Theme.textSecondary()));
                int tx = item.x + (item.w - fm.stringWidth(item.label)) / 2;
                int ty = item.y + (item.h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(item.label, tx, ty);

                curX += item.w + linkSpacing;
            }

            // Draw animated active gliding indicator pill
            if (foundActive && activePillW > 0) {
                g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 26));
                g2.fill(new RoundRectangle2D.Float(activePillX, y + (h - 32) / 2, activePillW, 32, 10, 10));
                // Active bottom line dot
                g2.setColor(Theme.BRAND_PINK);
                g2.fillRoundRect(Math.round(activePillX + activePillW / 2 - 8), y + h - 4, 16, 3, 2, 2);
            }

            // 3. Right Action Buttons
            int rightX = width - padX;

            // "Get Started" Primary CTA
            startW = 118;
            startH = 36;
            startX = rightX - startW;
            startY = y + (h - startH) / 2;
            Draw.drawGradientButton(g2, startX, startY, startW, startH, "Get Started", startHovered, false, false, false);

            // "Log in" Ghost Button
            loginW = 76;
            loginH = 36;
            loginX = startX - loginW - 8;
            loginY = y + (h - loginH) / 2;
            Draw.drawGhostButton(g2, loginX, loginY, loginW, loginH, "Log in", loginHovered, false, false);

            // "Dashboard" (Student Workspace)
            dashW = isCompactDesktop ? 94 : 106;
            dashH = 36;
            dashX = loginX - dashW - 8;
            dashY = y + (h - dashH) / 2;
            Draw.drawGhostButton(g2, dashX, dashY, dashW, dashH, isCompactDesktop ? "App" : "Workspace", dashHovered, false, false);
            // Green indicator dot for Workspace
            g2.setColor(Theme.SUCCESS_GREEN);
            g2.fillOval(dashX + 10, dashY + (dashH - 6) / 2, 6, 6);

            // "Admin" Console Button (hidden on tight compact, accessible via switcher)
            if (!isCompactDesktop || width >= 980) {
                adminW = 74;
                adminH = 36;
                adminX = dashX - adminW - 8;
                adminY = y + (h - adminH) / 2;
                Draw.drawGhostButton(g2, adminX, adminY, adminW, adminH, "Admin", adminHovered, false, false);
            } else {
                adminX = -200;
            }

            // Theme Toggle Button
            int themeAnchor = (adminX > 0) ? adminX : dashX;
            themeX = themeAnchor - themeSize - 12;
            themeY = y + (h - themeSize) / 2;

            if (themeHovered) {
                g2.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(themeX, themeY, themeSize, themeSize, 10, 10));
            }
            Draw.drawThemeIcon(g2, themeX + 7, themeY + 7, 20);

        } else {
            // 4. Mobile Header View (< 840px)
            int rightX = width - padX;

            burgerSize = 36;
            burgerX = rightX - burgerSize;
            burgerY = y + (h - burgerSize) / 2;

            themeX = burgerX - themeSize - 10;
            themeY = y + (h - themeSize) / 2;

            // Theme toggle icon
            if (themeHovered) {
                g2.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(themeX, themeY, themeSize, themeSize, 10, 10));
            }
            Draw.drawThemeIcon(g2, themeX + 7, themeY + 7, 20);

            // Animated Hamburger / Close Button
            if (burgerHovered) {
                g2.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(burgerX, burgerY, burgerSize, burgerSize, 10, 10));
            }

            paintAnimatedBurger(g2, burgerX, burgerY, burgerSize, mobileMenuProgress);

            // Draw Mobile Sliding Drawer if open / animating
            if (mobileMenuProgress > 0.01f) {
                paintMobileMenu(g2, y + h, width, viewportH);
            }
        }

        g2.dispose();
    }

    private void paintAnimatedBurger(Graphics2D g, int bx, int by, int bsize, float p) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.textPrimary());
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float cx = bx + bsize / 2f;
        float cy = by + bsize / 2f;
        float w2 = 9f;

        // When p = 0: 3 horizontal bars
        // When p = 1: Top and bottom rotate into an 'X', middle fades out
        float lineGap = 6f * (1f - p);
        float angle = p * 45f;

        // Top bar
        Graphics2D gt = (Graphics2D) g2.create();
        gt.translate(cx, cy - lineGap + (p * lineGap));
        gt.rotate(Math.toRadians(angle));
        gt.drawLine(Math.round(-w2), 0, Math.round(w2), 0);
        gt.dispose();

        // Middle bar (fades out)
        if (p < 0.7f) {
            float alpha = 1f - p / 0.7f;
            g2.setColor(new Color(Theme.textPrimary().getRed(), Theme.textPrimary().getGreen(), Theme.textPrimary().getBlue(), Math.round(alpha * 255)));
            g2.drawLine(Math.round(cx - w2), Math.round(cy), Math.round(cx + w2), Math.round(cy));
        }

        // Bottom bar
        Graphics2D gb = (Graphics2D) g2.create();
        gb.translate(cx, cy + lineGap - (p * lineGap));
        gb.rotate(Math.toRadians(-angle));
        gb.drawLine(Math.round(-w2), 0, Math.round(w2), 0);
        gb.dispose();

        g2.dispose();
    }

    private void paintMobileMenu(Graphics2D g, int topY, int width, int viewportH) {
        int padX = width > 500 ? 24 : 16;
        int menuH = 430;
        int animatedH = Math.round(menuH * mobileMenuProgress);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setClip(0, topY, width, animatedH);

        // Glass backdrop card
        g2.setColor(Theme.navBg());
        g2.fillRect(0, topY, width, menuH);

        g2.setColor(Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(0, topY + menuH - 1, width, topY + menuH - 1);

        // Subtle gradient ambient highlight
        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 12));
        g2.fillRect(0, topY, width, 40);

        int curY = topY + 16;
        g2.setFont(Theme.fontBodyBold());
        FontMetrics fm = g2.getFontMetrics();

        // 1. Navigation Links
        for (LinkItem item : links) {
            item.x = padX;
            item.y = curY;
            item.w = width - padX * 2;
            item.h = 40;

            boolean isActive = item.id.equalsIgnoreCase(activeSection);
            if (item.hovered || isActive) {
                g2.setColor(Theme.isDark ? new Color(255, 255, 255, 14) : new Color(0, 0, 0, 8));
                g2.fill(new RoundRectangle2D.Float(item.x, item.y, item.w, item.h, 10, 10));
            }

            // Dot indicator
            g2.setColor(isActive ? Theme.BRAND_PINK : Theme.textMuted());
            g2.fillOval(item.x + 14, item.y + (item.h - 6) / 2, 6, 6);

            g2.setColor(isActive ? Theme.BRAND_PINK : Theme.textPrimary());
            int ty = item.y + (item.h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(item.label, item.x + 30, ty);

            curY += item.h + 4;
        }

        // Divider line
        curY += 8;
        g2.setColor(Theme.cardBorder());
        g2.drawLine(padX, curY, width - padX, curY);
        curY += 14;

        // 2. Direct Workspace Switchers (Student Workspace & Admin Console)
        int cardGap = 10;
        int cardW = (width - padX * 2 - cardGap) / 2;
        int cardH = 44;

        mobDashX = padX;
        mobDashY = curY;
        mobDashW = cardW;
        mobDashH = cardH;

        RoundRectangle2D dBox = new RoundRectangle2D.Float(mobDashX, mobDashY, mobDashW, mobDashH, 10, 10);
        g2.setColor(mobDashHovered ? (Theme.isDark ? new Color(255, 255, 255, 16) : new Color(0, 0, 0, 10)) : Theme.cardBg());
        g2.fill(dBox);
        g2.setColor(Theme.cardBorder());
        g2.draw(dBox);

        g2.setColor(Theme.SUCCESS_GREEN);
        g2.fillOval(mobDashX + 12, mobDashY + (mobDashH - 8) / 2, 8, 8);
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.textPrimary());
        g2.drawString("Workspace", mobDashX + 26, mobDashY + 27);

        mobAdminX = padX + cardW + cardGap;
        mobAdminY = curY;
        mobAdminW = cardW;
        mobAdminH = cardH;

        RoundRectangle2D aBox = new RoundRectangle2D.Float(mobAdminX, mobAdminY, mobAdminW, mobAdminH, 10, 10);
        g2.setColor(mobAdminHovered ? (Theme.isDark ? new Color(255, 255, 255, 16) : new Color(0, 0, 0, 10)) : Theme.cardBg());
        g2.fill(aBox);
        g2.setColor(Theme.cardBorder());
        g2.draw(aBox);

        g2.setColor(Theme.ACCENT_VIOLET);
        g2.fillOval(mobAdminX + 12, mobAdminY + (mobAdminH - 8) / 2, 8, 8);
        g2.setColor(Theme.textPrimary());
        g2.drawString("Admin Panel", mobAdminX + 26, mobAdminY + 27);

        curY += cardH + 16;

        // 3. Auth Actions: "Log in" and "Get Started"
        loginX = padX;
        loginY = curY;
        loginW = (width - padX * 2 - 12) / 2;
        loginH = 42;
        Draw.drawGhostButton(g2, loginX, loginY, loginW, loginH, "Log in", loginHovered, false, false);

        startX = loginX + loginW + 12;
        startY = curY;
        startW = loginW;
        startH = 42;
        Draw.drawGradientButton(g2, startX, startY, startW, startH, "Get Started", startHovered, false, false, false);

        g2.dispose();
    }

    @Override
    public boolean onClick(int mx, int my) {
        // 1. Logo click -> smoothly scroll to top
        if (mx >= 16 && mx <= 180 && my >= 0 && my <= 72) {
            if (listener != null) listener.scrollToSection("hero");
            closeMobileMenu();
            return true;
        }

        // 2. Theme toggle
        if (mx >= themeX && mx <= themeX + themeSize && my >= themeY && my <= themeY + themeSize) {
            Theme.toggleTheme();
            return true;
        }

        // 3. Hamburger toggle button
        if (mx >= burgerX && mx <= burgerX + burgerSize && my >= burgerY && my <= burgerY + burgerSize) {
            mobileMenuOpen = !mobileMenuOpen;
            return true;
        }

        // 4. Mobile Menu Open handling
        if (mobileMenuOpen && mobileMenuProgress > 0.5f) {
            // Mobile Workspace Card
            if (mx >= mobDashX && mx <= mobDashX + mobDashW && my >= mobDashY && my <= mobDashY + mobDashH) {
                if (listener != null) listener.openDashboard();
                closeMobileMenu();
                return true;
            }
            // Mobile Admin Card
            if (mx >= mobAdminX && mx <= mobAdminX + mobAdminW && my >= mobAdminY && my <= mobAdminY + mobAdminH) {
                if (listener != null) listener.openAdminDashboard();
                closeMobileMenu();
                return true;
            }
        }

        // 5. Desktop Dashboard Button
        if (dashX > 0 && mx >= dashX && mx <= dashX + dashW && my >= dashY && my <= dashY + dashH) {
            if (listener != null) listener.openDashboard();
            closeMobileMenu();
            return true;
        }

        // 6. Desktop Admin Button
        if (adminX > 0 && mx >= adminX && mx <= adminX + adminW && my >= adminY && my <= adminY + adminH) {
            if (listener != null) listener.openAdminDashboard();
            closeMobileMenu();
            return true;
        }

        // 7. Log in Button
        if (mx >= loginX && mx <= loginX + loginW && my >= loginY && my <= loginY + loginH) {
            if (listener != null) {
                listener.openModal("TeamUp Account Login", "Enter your credentials to access your projects, messages, and saved teams.", false);
            }
            closeMobileMenu();
            return true;
        }

        // 8. Get Started Button
        if (mx >= startX && mx <= startX + startW && my >= startY && my <= startY + startH) {
            if (listener != null) {
                listener.openModal("Create your TeamUp Account", "Join thousands of students, developers, and researchers building great projects.", true);
            }
            closeMobileMenu();
            return true;
        }

        // 9. Navigation Links
        for (LinkItem item : links) {
            if (mx >= item.x && mx <= item.x + item.w && my >= item.y && my <= item.y + item.h) {
                if (listener != null) listener.scrollToSection(item.id);
                closeMobileMenu();
                return true;
            }
        }

        // If mobile menu was open and user clicked outside it, close the drawer
        if (mobileMenuOpen && my > 72 + 430) {
            closeMobileMenu();
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean prevTheme = themeHovered;
        boolean prevDash = dashHovered;
        boolean prevAdmin = adminHovered;
        boolean prevLogin = loginHovered;
        boolean prevStart = startHovered;
        boolean prevBurger = burgerHovered;
        boolean prevMobD = mobDashHovered;
        boolean prevMobA = mobAdminHovered;

        themeHovered = (mx >= themeX && mx <= themeX + themeSize && my >= themeY && my <= themeY + themeSize);
        dashHovered = (dashX > 0 && mx >= dashX && mx <= dashX + dashW && my >= dashY && my <= dashY + dashH);
        adminHovered = (adminX > 0 && mx >= adminX && mx <= adminX + adminW && my >= adminY && my <= adminY + adminH);
        loginHovered = (mx >= loginX && mx <= loginX + loginW && my >= loginY && my <= loginY + loginH);
        startHovered = (mx >= startX && mx <= startX + startW && my >= startY && my <= startY + startH);
        burgerHovered = (mx >= burgerX && mx <= burgerX + burgerSize && my >= burgerY && my <= burgerY + burgerSize);
        mobDashHovered = (mx >= mobDashX && mx <= mobDashX + mobDashW && my >= mobDashY && my <= mobDashY + mobDashH);
        mobAdminHovered = (mx >= mobAdminX && mx <= mobAdminX + mobAdminW && my >= mobAdminY && my <= mobAdminY + mobAdminH);

        boolean linkChanged = false;
        for (LinkItem item : links) {
            boolean was = item.hovered;
            item.hovered = (mx >= item.x && mx <= item.x + item.w && my >= item.y && my <= item.y + item.h);
            if (item.hovered != was) linkChanged = true;
        }

        return themeHovered != prevTheme || dashHovered != prevDash || adminHovered != prevAdmin ||
                loginHovered != prevLogin || startHovered != prevStart || burgerHovered != prevBurger ||
                mobDashHovered != prevMobD || mobAdminHovered != prevMobA || linkChanged;
    }

    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }

    @Override
    public boolean onKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && mobileMenuOpen) {
            closeMobileMenu();
            return true;
        }
        return false;
    }
}
