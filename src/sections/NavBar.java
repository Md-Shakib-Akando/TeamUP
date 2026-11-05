import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

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

    // Hit areas
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

    private final List<LinkItem> links = new ArrayList<>();
    private int themeX, themeY, themeSize = 34;
    private boolean themeHovered = false;

    private int dashX, dashY, dashW = 94, dashH = 34;
    private boolean dashHovered = false;
    private int adminX, adminY, adminW = 78, adminH = 34;
    private boolean adminHovered = false;

    private int loginX, loginY, loginW = 86, loginH = 34;
    private boolean loginHovered = false;

    private int startX, startY, startW = 116, startH = 34;
    private boolean startHovered = false;

    private int burgerX, burgerY, burgerSize = 36;
    private boolean burgerHovered = false;

    public NavBar(NavListener listener) {
        this.listener = listener;
        links.add(new LinkItem("features", "Features"));
        links.add(new LinkItem("how-it-works", "How it works"));
        links.add(new LinkItem("demo", "Demo"));
        links.add(new LinkItem("compare", "Compare"));
        links.add(new LinkItem("faq", "FAQ"));
    }

    public void setActiveSection(String id) {
        this.activeSection = id;
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
    public void update(float dt) {}

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        boolean isMobile = width < 780;

        // Sticky Glass Surface
        g.setColor(Theme.navBg());
        g.fillRect(0, y, width, h);

        // Subtle bottom border
        g.setColor(Theme.cardBorder());
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(0, y + h - 1, width, y + h - 1);

        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 16);

        // Logo
        int logoSize = 34;
        int logoY = y + (h - logoSize) / 2;
        Draw.drawLogo(g, padX, logoY, logoSize);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(20 * Theme.fontScale)));
        FontMetrics lfm = g.getFontMetrics();
        int brandX = padX + logoSize + 10;
        int brandY = y + (h - lfm.getHeight()) / 2 + lfm.getAscent();
        g.setColor(Theme.textPrimary());
        g.drawString("TeamUp", brandX, brandY);

        if (!isMobile) {
            // Desktop links centered or aligned
            g.setFont(Theme.fontBodyBold());
            FontMetrics fm = g.getFontMetrics();

            int linkSpacing = 28;
            int totalLinksW = 0;
            for (LinkItem item : links) {
                item.w = fm.stringWidth(item.label) + 16;
                item.h = 32;
                totalLinksW += item.w + linkSpacing;
            }

            int curX = brandX + lfm.stringWidth("TeamUp") + 40;
            for (LinkItem item : links) {
                item.x = curX;
                item.y = y + (h - item.h) / 2;

                boolean isActive = item.id.equalsIgnoreCase(activeSection);
                if (item.hovered || isActive) {
                    Color pillBg = isActive ? (Theme.isDark ? new Color(255, 255, 255, 18) : new Color(0, 0, 0, 10))
                                            : (Theme.isDark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 5));
                    g.setColor(pillBg);
                    g.fill(new RoundRectangle2D.Float(item.x, item.y, item.w, item.h, 12, 12));
                }

                if (isActive) {
                    g.setColor(Theme.BRAND_PINK);
                } else if (item.hovered) {
                    g.setColor(Theme.textPrimary());
                } else {
                    g.setColor(Theme.textSecondary());
                }

                int tx = item.x + (item.w - fm.stringWidth(item.label)) / 2;
                int ty = item.y + (item.h - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(item.label, tx, ty);

                curX += item.w + linkSpacing;
            }

            // Right side buttons
            int rightX = width - padX;

            // "Get Started"
            startX = rightX - startW;
            startY = y + (h - startH) / 2;
            Draw.drawGradientButton(g, startX, startY, startW, startH, "Get Started", startHovered, false, false, false);

            // "Log in"
            loginX = startX - loginW - 10;
            loginY = y + (h - loginH) / 2;
            Draw.drawGhostButton(g, loginX, loginY, loginW, loginH, "Log in", loginHovered, false, false);

            // "Dashboard"
            dashX = loginX - dashW - 10;
            dashY = y + (h - dashH) / 2;
            Draw.drawGhostButton(g, dashX, dashY, dashW, dashH, "Dashboard", dashHovered, false, false);

            // "Admin"
            adminX = dashX - adminW - 10;
            adminY = y + (h - adminH) / 2;
            Draw.drawGhostButton(g, adminX, adminY, adminW, adminH, "Admin", adminHovered, false, false);

            // Theme toggle button
            themeX = adminX - themeSize - 14;
            themeY = y + (h - themeSize) / 2;
            if (themeHovered) {
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 12));
                g.fill(new RoundRectangle2D.Float(themeX, themeY, themeSize, themeSize, 10, 10));
            }
            Draw.drawThemeIcon(g, themeX + 7, themeY + 7, 20);

        } else {
            // Mobile: theme button and hamburger icon
            int rightX = width - padX;

            burgerX = rightX - burgerSize;
            burgerY = y + (h - burgerSize) / 2;

            themeX = burgerX - themeSize - 12;
            themeY = y + (h - themeSize) / 2;

            if (themeHovered) {
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 12));
                g.fill(new RoundRectangle2D.Float(themeX, themeY, themeSize, themeSize, 10, 10));
            }
            Draw.drawThemeIcon(g, themeX + 7, themeY + 7, 20);

            if (burgerHovered) {
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 12));
                g.fill(new RoundRectangle2D.Float(burgerX, burgerY, burgerSize, burgerSize, 10, 10));
            }

            if (mobileMenuOpen) {
                Draw.drawCloseIcon(g, burgerX + 8, burgerY + 8, 20, Theme.textPrimary());
                paintMobileMenu(g, y + h, width);
            } else {
                Draw.drawHamburgerIcon(g, burgerX + 8, burgerY + 8, 20, Theme.textPrimary());
            }
        }
    }

    private void paintMobileMenu(Graphics2D g, int topY, int width) {
        int menuH = 340;
        g.setColor(Theme.navBg());
        g.fillRect(0, topY, width, menuH);

        g.setColor(Theme.cardBorder());
        g.drawLine(0, topY + menuH, width, topY + menuH);

        g.setFont(Theme.fontBodyBold());
        FontMetrics fm = g.getFontMetrics();
        int curY = topY + 20;

        for (LinkItem item : links) {
            item.x = 24;
            item.y = curY;
            item.w = width - 48;
            item.h = 42;

            boolean isActive = item.id.equalsIgnoreCase(activeSection);
            if (item.hovered || isActive) {
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 10));
                g.fill(new RoundRectangle2D.Float(item.x, item.y, item.w, item.h, 10, 10));
            }

            g.setColor(isActive ? Theme.BRAND_PINK : Theme.textPrimary());
            int ty = item.y + (item.h - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(item.label, item.x + 16, ty);

            curY += item.h + 8;
        }

        // Mobile buttons
        loginX = 24;
        loginY = curY + 10;
        loginW = (width - 60) / 2;
        loginH = 40;
        Draw.drawGhostButton(g, loginX, loginY, loginW, loginH, "Log in", loginHovered, false, false);

        startX = loginX + loginW + 12;
        startY = loginY;
        startW = loginW;
        startH = 40;
        Draw.drawGradientButton(g, startX, startY, startW, startH, "Get Started", startHovered, false, false, false);
    }

    @Override
    public boolean onClick(int mx, int my) {
        // Logo click -> scroll to top
        if (mx >= 16 && mx <= 160 && my >= 0 && my <= 72) {
            if (listener != null) listener.scrollToSection("hero");
            return true;
        }

        // Theme toggle
        if (mx >= themeX && mx <= themeX + themeSize && my >= themeY && my <= themeY + themeSize) {
            Theme.toggleTheme();
            return true;
        }

        // Hamburger button
        if (mx >= burgerX && mx <= burgerX + burgerSize && my >= burgerY && my <= burgerY + burgerSize) {
            mobileMenuOpen = !mobileMenuOpen;
            return true;
        }

        // "Dashboard"
        if (mx >= dashX && mx <= dashX + dashW && my >= dashY && my <= dashY + dashH) {
            if (listener != null) listener.openDashboard();
            mobileMenuOpen = false;
            return true;
        }

        // "Admin"
        if (mx >= adminX && mx <= adminX + adminW && my >= adminY && my <= adminY + adminH) {
            if (listener != null) listener.openAdminDashboard();
            mobileMenuOpen = false;
            return true;
        }

        // "Log in"
        if (mx >= loginX && mx <= loginX + loginW && my >= loginY && my <= loginY + loginH) {
            if (listener != null) {
                listener.openModal("TeamUp Account Login", "Enter your credentials to access your projects, messages, and saved teams.", false);
            }
            mobileMenuOpen = false;
            return true;
        }

        // "Get Started"
        if (mx >= startX && mx <= startX + startW && my >= startY && my <= startY + startH) {
            if (listener != null) {
                listener.openModal("Create your TeamUp Account", "Join thousands of students, developers, and researchers building great projects.", true);
            }
            mobileMenuOpen = false;
            return true;
        }

        // Links
        for (LinkItem item : links) {
            if (mx >= item.x && mx <= item.x + item.w && my >= item.y && my <= item.y + item.h) {
                if (listener != null) listener.scrollToSection(item.id);
                mobileMenuOpen = false;
                return true;
            }
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

        themeHovered = (mx >= themeX && mx <= themeX + themeSize && my >= themeY && my <= themeY + themeSize);
        dashHovered = (mx >= dashX && mx <= dashX + dashW && my >= dashY && my <= dashY + dashH);
        adminHovered = (mx >= adminX && mx <= adminX + adminW && my >= adminY && my <= adminY + adminH);
        loginHovered = (mx >= loginX && mx <= loginX + loginW && my >= loginY && my <= loginY + loginH);
        startHovered = (mx >= startX && mx <= startX + startW && my >= startY && my <= startY + startH);
        burgerHovered = (mx >= burgerX && mx <= burgerX + burgerSize && my >= burgerY && my <= burgerY + burgerSize);

        boolean linkChanged = false;
        for (LinkItem item : links) {
            boolean was = item.hovered;
            item.hovered = (mx >= item.x && mx <= item.x + item.w && my >= item.y && my <= item.y + item.h);
            if (item.hovered != was) linkChanged = true;
        }

        return themeHovered != prevTheme || loginHovered != prevLogin || startHovered != prevStart || burgerHovered != prevBurger || linkChanged;
    }

    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }
    @Override
    public boolean onKey(KeyEvent e) { return false; }
}

