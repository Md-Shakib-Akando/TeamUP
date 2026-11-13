import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Floating glass navigation pill.
 * - Becomes more opaque with a soft shadow once the page scrolls.
 * - A spring-driven highlight slides between links (hover wins over the active section).
 * - Collapses into a menu below 1180px.
 */
public class NavBar implements Section {

    public interface NavListener {
        void scrollToSection(String sectionId);
        void openModal(String title, String message, boolean isRegister);
        void openDashboard();
        default void openAdminDashboard() {}
        default void returnToLanding() {}
    }

    private final NavListener listener;
    private String activeSection = "hero";
    private boolean menuOpen = false;
    private final Elastic menuAnim = new Elastic(0f, 260f, 24f);

    private static class Item {
        final String id, label;
        int x, y, w, h;
        boolean hovered;
        Item(String id, String label) { this.id = id; this.label = label; }
    }

    private final List<Item> links = new ArrayList<>();

    // Sliding highlight
    private final Elastic hlX = new Elastic(0f, 300f, 26f);
    private final Elastic hlW = new Elastic(0f, 300f, 26f);
    private final Elastic hlA = new Elastic(0f, 200f, 22f);
    private boolean hlInit = false;

    // Buttons (screen-space)
    private final Rectangle themeR = new Rectangle(), dashR = new Rectangle(), adminR = new Rectangle(),
            loginR = new Rectangle(), startR = new Rectangle(), profileR = new Rectangle(),
            burgerR = new Rectangle(), logoR = new Rectangle();
    private boolean themeH, dashH, adminH, loginH, startH, profileH, burgerH;
    private final Elastic startHov = new Elastic(0f, 260f, 20f);
    private final Elastic profileHov = new Elastic(0f, 260f, 20f);
    private final Rectangle menuR = new Rectangle();
    private AuthStore.UserRecord loggedInUser = null;

    // Profile Dropdown Popover
    private boolean profileMenuOpen = false;
    private final Elastic profileMenuAnim = new Elastic(0f, 260f, 24f);
    private final Rectangle profileDropR = new Rectangle();
    private final Rectangle dropDashR = new Rectangle();
    private final Rectangle dropLogoutR = new Rectangle();
    private boolean dropDashH = false, dropLogoutH = false;

    private float scrolled = 0f; // 0..1 how "docked" the nav looks
    private float lastViewportY = 0f;

    public NavBar(NavListener listener) {
        this.listener = listener;
        links.add(new Item("features", "Features"));
        links.add(new Item("how-it-works", "How it works"));
        links.add(new Item("demo", "Live demo"));
        links.add(new Item("faq", "FAQ"));
    }

    public void setLoggedInUser(AuthStore.UserRecord user) {
        this.loggedInUser = user;
    }

    public AuthStore.UserRecord getLoggedInUser() {
        return loggedInUser != null ? loggedInUser : AuthStore.getCurrentUser();
    }

    public void setActiveSection(String id) { this.activeSection = id; }

    /** True if (x,y) falls on the nav pill, its open menu, or profile dropdown. */
    public boolean captures(int x, int y, int width) {
        if (y <= getHeight(width)) return true;
        if (menuOpen && menuR.contains(x, y)) return true;
        return profileMenuOpen && profileDropR.contains(x, y);
    }

    @Override public String getId() { return "nav"; }
    @Override public int getHeight(int width) { return 72; }

    @Override
    public void update(float dt) {
        scrolled = Anim.approach(scrolled, lastViewportY > 24 ? 1f : 0f, dt, 8f);
        menuAnim.setTarget(menuOpen ? 1f : 0f);
        menuAnim.update(dt);
        startHov.setTarget(startH ? 1f : 0f);
        startHov.update(dt);
        profileHov.setTarget(profileH ? 1f : 0f);
        profileHov.update(dt);
        profileMenuAnim.setTarget(profileMenuOpen ? 1f : 0f);
        profileMenuAnim.update(dt);

        Item target = null;
        for (Item i : links) if (i.hovered) target = i;
        if (target == null) for (Item i : links) if (i.id.equalsIgnoreCase(activeSection)) target = i;
        if (target != null && target.w > 0) {
            if (!hlInit) { hlX.setValue(target.x); hlW.setValue(target.w); hlInit = true; }
            hlX.setTarget(target.x);
            hlW.setTarget(target.w);
            hlA.setTarget(1f);
        } else {
            hlA.setTarget(0f);
        }
        hlX.update(dt);
        hlW.update(dt);
        hlA.update(dt);
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        lastViewportY = viewportY;
        Graphics2D n = (Graphics2D) g.create();
        Draw.setQualityHints(n);

        float intro = Fx.easeOutQuart((Anim.pageLoadTime - 0.05f) / 0.8f);
        Fx.fadeBy(n, Fx.clamp(intro));
        n.translate(0, -(1f - intro) * 30f);

        boolean tiny = width < 640;
        boolean compact = width < 1080;
        int margin = width > 900 ? 20 : 10;
        int px = Math.max(margin, (width - 1240) / 2);
        int pw = width - px * 2;
        int ph = 56;
        int py = y + 10;
        RoundRectangle2D.Float pill = Fx.rr(px, py, pw, ph, 18);

        // Glass surface
        Fx.softShadow(n, px, py, pw, ph, 18, (Fx.dark() ? 0.7f : 0.25f) * scrolled, 22);
        Color base = Fx.dark() ? new Color(0x0B, 0x0B, 0x1A) : new Color(0xFF, 0xFF, 0xFF);
        n.setColor(Fx.a(base, Math.round(110 + 120 * scrolled)));
        n.fill(pill);
        n.setColor(Fx.mix(Fx.a(Fx.border(), 0), Fx.borderHi(), 0.4f + 0.6f * scrolled));
        n.setStroke(new BasicStroke(1f));
        n.draw(pill);
        Fx.spotlightBorder(n, pill, Fx.VIOLET, 0.6f);

        int cy = py + ph / 2;

        // Logo
        int lx = px + 16;
        Draw.drawLogo(n, lx, cy - 15, 30);
        n.setFont(Fx.heading(19));
        n.setColor(Fx.text());
        Fx.textMid(n, "TeamUp", lx + 40, cy);
        int brandEnd = lx + 40 + n.getFontMetrics().stringWidth("TeamUp");
        logoR.setBounds(lx, py, brandEnd - lx, ph);

        int right = px + pw - 10;

        // Primary CTA or Profile Icon (when logged in)
        AuthStore.UserRecord curUser = getLoggedInUser();
        boolean loggedIn = (curUser != null);

        if (loggedIn) {
            startR.setBounds(-100, -100, 0, 0);
            int pwSize = 40, phSize = 40;
            profileR.setBounds(right - pwSize, cy - phSize / 2, pwSize, phSize);
            paintProfileBadge(n, profileR, curUser, profileHov.getValue());
            right = profileR.x - 10;
        } else {
            profileR.setBounds(-100, -100, 0, 0);
            int sw = tiny ? 0 : 128, sh = 40;
            if (!tiny) {
                startR.setBounds(right - sw, cy - sh / 2, sw, sh);
                Fx.primaryButton(n, startR.x, startR.y, sw, sh, "Get started", startHov.getValue(), 0f, false);
                right = startR.x - 8;
            } else {
                startR.setBounds(-100, -100, 0, 0);
            }
        }

        n.setFont(Fx.bold(14));
        FontMetrics fm = n.getFontMetrics();

        loginR.setBounds(-100, -100, 0, 0);

        if (compact) {
            burgerR.setBounds(right - 40, cy - 20, 40, 40);
            if (burgerH || menuOpen) {
                n.setColor(Fx.subtle());
                n.fill(Fx.rr(burgerR.x, burgerR.y, 40, 40, 12));
            }
            paintBurger(n, burgerR.x + 10, burgerR.y + 10, 20, menuAnim.getValue());
            right = burgerR.x - 4;
            dashR.setBounds(-100, -100, 0, 0);
            adminR.setBounds(-100, -100, 0, 0);
        } else {
            burgerR.setBounds(-100, -100, 0, 0);
            // Divider
            n.setColor(Fx.border());
            n.drawLine(right - 4, cy - 12, right - 4, cy + 12);
            right -= 12;
            adminR.setBounds(-100, -100, 0, 0);
            if (loggedIn) {
                // Logged in (student or admin): one Dashboard button, routed by role
                int dw = fm.stringWidth("Dashboard") + 46;
                dashR.setBounds(right - dw, cy - 20, dw, 40);
                paintDashboardButton(n, dashR, dashH);
                right = dashR.x - 4;
            } else {
                dashR.setBounds(-100, -100, 0, 0);
                int lw = fm.stringWidth("Log in") + 28;
                loginR.setBounds(right - lw, cy - 20, lw, 40);
                paintTextButton(n, loginR, "Log in", loginH);
                right = loginR.x - 4;
            }
        }

        // Theme toggle
        themeR.setBounds(right - 40, cy - 20, 40, 40);
        if (themeH) {
            n.setColor(Fx.subtle());
            n.fill(Fx.rr(themeR.x, themeR.y, 40, 40, 12));
        }
        paintThemeIcon(n, themeR.x + 10, themeR.y + 10, 20);

        // Centre links (desktop) inside the space between brand and the right cluster
        if (!compact) {
            int gap = 2;
            int total = 0;
            for (Item i : links) total += fm.stringWidth(i.label) + 26 + gap;
            total -= gap;
            int free0 = brandEnd + 20, free1 = themeR.x - 16;
            int x = Math.round(width / 2f - total / 2f);
            if (x < free0 || x + total > free1) x = Math.round((free0 + free1) / 2f - total / 2f);
            if (x < free0) x = free0;
            for (Item i : links) {
                i.w = fm.stringWidth(i.label) + 26;
                i.h = 38;
                i.x = x;
                i.y = cy - 19;
                x += i.w + gap;
            }
            // sliding highlight
            float a = hlA.getValue();
            if (a > 0.01f) {
                RoundRectangle2D.Float hl = Fx.rr(hlX.getValue(), cy - 19, hlW.getValue(), 38, 12);
                n.setColor(Fx.a(Fx.dark() ? Color.WHITE : new Color(20, 18, 50), Math.round((Fx.dark() ? 18 : 12) * a)));
                n.fill(hl);
            }
            for (Item i : links) {
                boolean act = i.id.equalsIgnoreCase(activeSection);
                n.setColor(act || i.hovered ? Fx.text() : Fx.text2());
                n.setFont(Fx.bold(14));
                Fx.textCenter(n, i.label, i.x + i.w / 2f, cy);
                if (act) {
                    n.setPaint(new GradientPaint(i.x + i.w / 2f - 8, 0, Fx.PINK, i.x + i.w / 2f + 8, 0, Fx.VIOLET));
                    n.fill(Fx.rr(i.x + i.w / 2f - 8, cy + 13, 16, 3, 1.5f));
                }
            }
        } else {
            for (Item i : links) i.w = 0;
        }

        n.dispose();

        float m = menuAnim.getValue();
        if (compact && m > 0.01f) paintMenu(g, px, py + ph + 8, pw, m, tiny);
        else menuR.setBounds(-100, -100, 0, 0);

        float pm = profileMenuAnim.getValue();
        if (pm > 0.01f && getLoggedInUser() != null) {
            paintProfileDropdown(g, py + ph + 8, pm);
        } else {
            profileDropR.setBounds(-100, -100, 0, 0);
            dropDashR.setBounds(-100, -100, 0, 0);
            dropLogoutR.setBounds(-100, -100, 0, 0);
        }
    }

    /** "Dashboard" pill with a small grid icon, shown only to logged-in users. */
    private void paintDashboardButton(Graphics2D g, Rectangle r, boolean hover) {
        Graphics2D b = (Graphics2D) g.create();
        Draw.setQualityHints(b);
        b.setColor(hover ? Fx.subtle() : Fx.a(Fx.VIOLET, Fx.dark() ? 34 : 20));
        b.fill(Fx.rr(r.x, r.y, r.width, r.height, 12));
        // 2x2 grid icon
        int ix = r.x + 13, iy = r.y + r.height / 2 - 6, cell = 5;
        b.setColor(hover ? Fx.text() : Fx.VIOLET);
        for (int k = 0; k < 4; k++) {
            b.fill(Fx.rr(ix + (k % 2) * (cell + 2), iy + (k / 2) * (cell + 2), cell, cell, 2));
        }
        b.setFont(Fx.bold(14));
        b.setColor(hover ? Fx.text() : Fx.text2());
        Fx.textMid(b, "Dashboard", ix + cell * 2 + 9, r.y + r.height / 2f);
        b.dispose();
    }

    private void paintTextButton(Graphics2D g, Rectangle r, String label, boolean hover) {
        if (hover) {
            g.setColor(Fx.subtle());
            g.fill(Fx.rr(r.x, r.y, r.width, r.height, 12));
        }
        g.setFont(Fx.bold(14));
        g.setColor(hover ? Fx.text() : Fx.text2());
        Fx.textCenter(g, label, r.x + r.width / 2f, r.y + r.height / 2f);
    }

    private void paintBurger(Graphics2D g, int x, int y, int s, float open) {
        Graphics2D b = (Graphics2D) g.create();
        b.setColor(Fx.text());
        b.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float cx = x + s / 2f, cy = y + s / 2f;
        // top & bottom bars rotate into an X, middle fades
        Graphics2D t = (Graphics2D) b.create();
        t.translate(cx, cy);
        t.rotate(open * Math.PI / 4);
        t.drawLine(Math.round(-s * 0.35f), Math.round(-s * 0.25f * (1 - open)), Math.round(s * 0.35f), Math.round(-s * 0.25f * (1 - open)));
        t.dispose();
        Graphics2D bt = (Graphics2D) b.create();
        bt.translate(cx, cy);
        bt.rotate(-open * Math.PI / 4);
        bt.drawLine(Math.round(-s * 0.35f), Math.round(s * 0.25f * (1 - open)), Math.round(s * 0.35f), Math.round(s * 0.25f * (1 - open)));
        bt.dispose();
        Fx.fadeBy(b, 1f - open);
        b.drawLine(Math.round(cx - s * 0.35f), Math.round(cy), Math.round(cx + s * 0.35f), Math.round(cy));
        b.dispose();
    }

    private void paintThemeIcon(Graphics2D g, int x, int y, int s) {
        Graphics2D t = (Graphics2D) g.create();
        float cx = x + s / 2f, cy = y + s / 2f;
        float tt = Theme.themeTransition; // 0 dark (show sun), 1 light (show moon)
        t.setColor(Fx.text());
        t.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Sun (fades/rotates out in light mode)
        if (tt < 0.99f) {
            Graphics2D sun = (Graphics2D) t.create();
            Fx.fadeBy(sun, 1f - tt);
            sun.rotate(tt * Math.PI, cx, cy);
            float r = s * 0.2f;
            sun.draw(new java.awt.geom.Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                sun.draw(new java.awt.geom.Line2D.Float((float) (cx + Math.cos(a) * (r + 3)), (float) (cy + Math.sin(a) * (r + 3)),
                        (float) (cx + Math.cos(a) * (r + 5.5f)), (float) (cy + Math.sin(a) * (r + 5.5f))));
            }
            sun.dispose();
        }
        if (tt > 0.01f) {
            Graphics2D moon = (Graphics2D) t.create();
            Fx.fadeBy(moon, tt);
            moon.rotate((1 - tt) * -Math.PI / 2, cx, cy);
            java.awt.geom.Area a = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Float(cx - s * 0.36f, cy - s * 0.36f, s * 0.72f, s * 0.72f));
            a.subtract(new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Float(cx - s * 0.12f, cy - s * 0.5f, s * 0.68f, s * 0.68f)));
            moon.fill(a);
            moon.dispose();
        }
        t.dispose();
    }

    private void paintProfileBadge(Graphics2D g, Rectangle r, AuthStore.UserRecord user, float hov) {
        Graphics2D p = (Graphics2D) g.create();
        Draw.setQualityHints(p);

        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        int rad = r.width / 2;

        // Soft glow on hover
        if (hov > 0.01f) {
            Fx.glow(p, cx, cy, rad + 8, Fx.PINK, 0.5f * hov);
        }

        // Outer gradient ring
        RoundRectangle2D.Float circle = Fx.rr(r.x, r.y, r.width, r.height, r.width);
        p.setPaint(new GradientPaint(r.x, r.y, Fx.PINK, r.x + r.width, r.y + r.height, Fx.VIOLET));
        p.fill(circle);

        // Inner avatar surface
        int pad = 2;
        RoundRectangle2D.Float inner = Fx.rr(r.x + pad, r.y + pad, r.width - pad * 2, r.height - pad * 2, r.width);
        p.setColor(Fx.dark() ? new Color(0x18, 0x14, 0x2E) : new Color(0xF4, 0xF1, 0xFD));
        p.fill(inner);

        // Initials
        String initials = getInitials(user != null ? user.name : "U");
        p.setFont(Fx.bold(13));
        p.setColor(Fx.dark() ? Color.WHITE : new Color(0x20, 0x12, 0x48));
        Fx.textCenter(p, initials, cx, cy + 1);

        // Online dot indicator
        int dotX = r.x + r.width - 11;
        int dotY = r.y + r.height - 11;
        int dotR = 8;
        p.setColor(Fx.dark() ? new Color(0x0B, 0x0B, 0x1A) : Color.WHITE);
        p.fill(new java.awt.geom.Ellipse2D.Float(dotX - 1, dotY - 1, dotR + 2, dotR + 2));
        p.setColor(new Color(0x10, 0xB9, 0x81));
        p.fill(new java.awt.geom.Ellipse2D.Float(dotX, dotY, dotR, dotR));

        p.dispose();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.trim().substring(0, Math.min(2, name.trim().length())).toUpperCase();
    }

    private void paintProfileDropdown(Graphics2D g, int dropY, float anim) {
        AuthStore.UserRecord user = getLoggedInUser();
        if (user == null) return;

        Graphics2D d = (Graphics2D) g.create();
        Draw.setQualityHints(d);
        Fx.fadeBy(d, anim);
        d.translate(0, -(1f - anim) * 10f);

        int dw = 220;
        int dh = 150;
        int dx = profileR.x + profileR.width - dw;
        if (dx < 10) dx = 10;
        int dy = dropY;

        profileDropR.setBounds(dx, dy, dw, dh);

        // Glass popover card background & border
        Fx.softShadow(d, dx, dy, dw, dh, 14, Fx.dark() ? 0.6f : 0.2f, 16);
        d.setColor(Fx.dark() ? new Color(0x13, 0x11, 0x24, 248) : new Color(255, 255, 255, 250));
        d.fill(Fx.rr(dx, dy, dw, dh, 14));
        d.setColor(Fx.border());
        d.setStroke(new BasicStroke(1f));
        d.draw(Fx.rr(dx, dy, dw, dh, 14));

        // User info header
        int cy = dy + 20;
        d.setFont(Fx.bold(13.5f));
        d.setColor(Fx.text());
        String nameStr = (user.name != null && !user.name.isEmpty()) ? user.name : "User";
        if (nameStr.length() > 22) nameStr = nameStr.substring(0, 20) + "…";
        d.drawString(nameStr, dx + 14, cy);

        cy += 16;
        d.setFont(Fx.body(11f));
        d.setColor(Fx.text3());
        String roleStr = (user.role != null ? user.role : "Student") + " · " + (user.email != null ? user.email : "");
        if (roleStr.length() > 28) roleStr = roleStr.substring(0, 26) + "…";
        d.drawString(roleStr, dx + 14, cy);

        cy += 12;
        d.setColor(Fx.border());
        d.drawLine(dx + 12, cy, dx + dw - 12, cy);

        // Row 1: Go to Dashboard
        cy += 6;
        int rowH = 36;
        dropDashR.setBounds(dx + 8, cy, dw - 16, rowH);
        if (dropDashH) {
            d.setColor(Fx.subtle());
            d.fill(Fx.rr(dropDashR.x, dropDashR.y, dropDashR.width, dropDashR.height, 8));
        }
        d.setFont(Fx.bold(13f));
        d.setColor(dropDashH ? Fx.text() : Fx.text2());
        d.drawString("Open Dashboard", dropDashR.x + 10, dropDashR.y + 23);
        Fx.arrow(d, dropDashR.x + dropDashR.width - 16, dropDashR.y + 23, 9, Fx.text3(), 1.5f);

        // Row 2: Log Out
        cy += rowH + 2;
        dropLogoutR.setBounds(dx + 8, cy, dw - 16, rowH);
        if (dropLogoutH) {
            d.setColor(Fx.a(Fx.RED, Fx.dark() ? 35 : 20));
            d.fill(Fx.rr(dropLogoutR.x, dropLogoutR.y, dropLogoutR.width, dropLogoutR.height, 8));
        }
        d.setFont(Fx.bold(13f));
        d.setColor(dropLogoutH ? Fx.RED : Fx.a(Fx.RED, 220));
        d.drawString("Log Out", dropLogoutR.x + 10, dropLogoutR.y + 23);
        Fx.arrow(d, dropLogoutR.x + dropLogoutR.width - 16, dropLogoutR.y + 23, 9, Fx.RED, 1.5f);

        d.dispose();
    }

    private void paintMenu(Graphics2D g, int x, int y, int w, float m, boolean tiny) {
        Graphics2D n = (Graphics2D) g.create();
        Draw.setQualityHints(n);
        int rowH = 46;
        AuthStore.UserRecord menuUser = getLoggedInUser();
        int rows = links.size() + 1 + (menuUser != null ? 2 : 0);
        int h = 16 + rows * rowH + (tiny && menuUser == null ? 62 : 0) + 8;
        menuR.setBounds(x, y, w, h);
        Fx.fadeBy(n, m);
        n.translate(0, -(1 - m) * 12);
        Fx.card(n, x, y, w, h, 18, 0f, null);
        n.setColor(Fx.dark() ? new Color(0x0B, 0x0B, 0x1A, 235) : new Color(255, 255, 255, 240));
        n.fill(Fx.rr(x, y, w, h, 18));
        n.setColor(Fx.border());
        n.draw(Fx.rr(x, y, w, h, 18));

        int cy = y + 12;
        n.setFont(Fx.bold(15));
        for (Item i : links) {
            i.x = x + 10; i.y = cy; i.w = w - 20; i.h = rowH - 4;
            boolean act = i.id.equalsIgnoreCase(activeSection);
            if (i.hovered || act) {
                n.setColor(Fx.subtle());
                n.fill(Fx.rr(i.x, i.y, i.w, i.h, 12));
            }
            if (act) {
                n.setPaint(new GradientPaint(i.x, 0, Fx.PINK, i.x, i.y + i.h, Fx.VIOLET));
                n.fill(Fx.rr(i.x + 6, i.y + 12, 3, i.h - 24, 1.5f));
            }
            n.setColor(act ? Fx.text() : Fx.text2());
            Fx.textMid(n, i.label, i.x + 18, i.y + i.h / 2f);
            cy += rowH;
        }
        n.setColor(Fx.border());
        n.drawLine(x + 18, cy + 2, x + w - 18, cy + 2);
        cy += 6;
        adminR.setBounds(-100, -100, 0, 0);
        if (menuUser != null) {
            loginR.setBounds(-100, -100, 0, 0);
            dashR.setBounds(x + 10, cy, w - 20, rowH - 4);
            paintMenuRow(n, dashR, isAdmin(menuUser) ? "Admin dashboard" : "Dashboard", dashH);
        } else {
            dashR.setBounds(-100, -100, 0, 0);
            loginR.setBounds(x + 10, cy, w - 20, rowH - 4);
            paintMenuRow(n, loginR, "Log in", loginH);
        }
        cy += rowH;
        if (menuUser != null) {
            startR.setBounds(-100, -100, 0, 0);
            int profH = 42;
            profileR.setBounds(x + 10, cy, w - 20, profH);
            if (profileH) {
                n.setColor(Fx.subtle());
                n.fill(Fx.rr(profileR.x, profileR.y, profileR.width, profileR.height, 12));
            }
            int avX = profileR.x + 10, avY = profileR.y + (profH - 28) / 2;
            n.setPaint(new GradientPaint(avX, avY, Fx.PINK, avX + 28, avY + 28, Fx.VIOLET));
            n.fill(new java.awt.geom.Ellipse2D.Float(avX, avY, 28, 28));
            n.setFont(Fx.bold(11));
            n.setColor(Color.WHITE);
            Fx.textCenter(n, getInitials(menuUser.name), avX + 14, avY + 14);

            n.setFont(Fx.bold(14));
            n.setColor(Fx.text());
            String displayName = (menuUser.name != null && !menuUser.name.isEmpty()) ? menuUser.name : "Profile";
            Fx.textMid(n, displayName, avX + 38, profileR.y + profH / 2f);
            Fx.arrow(n, profileR.x + profileR.width - 26, profileR.y + profH / 2f, 11, Fx.text3(), 1.6f);

            // Log Out row in mobile drawer
            cy += profH + 6;
            dropLogoutR.setBounds(x + 10, cy, w - 20, profH);
            if (dropLogoutH) {
                n.setColor(Fx.a(Fx.RED, Fx.dark() ? 35 : 20));
                n.fill(Fx.rr(dropLogoutR.x, dropLogoutR.y, dropLogoutR.width, dropLogoutR.height, 12));
            }
            n.setFont(Fx.bold(14));
            n.setColor(Fx.RED);
            Fx.textMid(n, "Log Out", x + 24, dropLogoutR.y + profH / 2f);
            Fx.arrow(n, dropLogoutR.x + dropLogoutR.width - 26, dropLogoutR.y + profH / 2f, 11, Fx.RED, 1.6f);
        } else if (tiny) {
            profileR.setBounds(-100, -100, 0, 0);
            dropLogoutR.setBounds(-100, -100, 0, 0);
            startR.setBounds(x + 12, cy + 6, w - 24, 44);
            Fx.primaryButton(n, startR.x, startR.y, w - 24, 44, "Get started", startHov.getValue(), 0f, false);
        }
        n.dispose();
    }

    private void paintMenuRow(Graphics2D n, Rectangle r, String label, boolean hover) {
        if (hover) {
            n.setColor(Fx.subtle());
            n.fill(Fx.rr(r.x, r.y, r.width, r.height, 12));
        }
        n.setFont(Fx.bold(15));
        n.setColor(hover ? Fx.text() : Fx.text2());
        Fx.textMid(n, label, r.x + 18, r.y + r.height / 2f);
        Fx.arrow(n, r.x + r.width - 30, r.y + r.height / 2f, 11, Fx.text3(), 1.6f);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean onClick(int mx, int my) {
        if (themeR.contains(mx, my)) { Theme.toggleTheme(); return true; }
        if (burgerR.contains(mx, my)) { menuOpen = !menuOpen; return true; }
        if (dashR.contains(mx, my)) {
            menuOpen = false;
            profileMenuOpen = false;
            openDashboardForRole();
            return true;
        }
        if (loginR.contains(mx, my)) {
            menuOpen = false;
            if (listener != null) listener.openModal("TeamUp Account Login",
                    "Enter your credentials to access your projects, messages, and saved teams.", false);
            return true;
        }
        // 1. Profile Dropdown & Mobile Menu logout/dashboard actions
        if (profileMenuOpen || menuOpen) {
            if (dropDashR.contains(mx, my)) {
                profileMenuOpen = false;
                menuOpen = false;
                openDashboardForRole();
                return true;
            }
            if (dropLogoutR.contains(mx, my)) {
                profileMenuOpen = false;
                menuOpen = false;
                AuthStore.logout();
                setLoggedInUser(null);
                if (listener != null) listener.returnToLanding();
                return true;
            }
            if (profileMenuOpen && profileDropR.contains(mx, my)) {
                return true; // Click inside dropdown body
            }
        }

        // 2. Profile Avatar Badge click -> toggles dropdown
        if (profileR.contains(mx, my)) {
            profileMenuOpen = !profileMenuOpen;
            return true;
        }

        // Close profile menu if clicking outside
        if (profileMenuOpen) {
            profileMenuOpen = false;
        }
        if (startR.contains(mx, my)) {
            menuOpen = false;
            if (listener != null) listener.openModal("Create your TeamUp Account",
                    "Join students, developers, and researchers building great projects.", true);
            return true;
        }
        for (Item i : links) {
            if (i.w > 0 && Fx.inside(mx, my, i.x, i.y, i.w, i.h)) {
                menuOpen = false;
                if (listener != null) listener.scrollToSection(i.id);
                return true;
            }
        }
        if (logoR.contains(mx, my)) {
            menuOpen = false;
            if (listener != null) listener.scrollToSection("hero");
            return true;
        }
        if (menuOpen && menuR.contains(mx, my)) return true;
        if (menuOpen) { menuOpen = false; return true; }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        themeH = themeR.contains(mx, my);
        dashH = dashR.contains(mx, my);
        adminH = adminR.contains(mx, my);
        loginH = loginR.contains(mx, my);
        startH = startR.contains(mx, my);
        profileH = profileR.contains(mx, my);
        dropDashH = (profileMenuOpen || menuOpen) && dropDashR.contains(mx, my);
        dropLogoutH = (profileMenuOpen || menuOpen) && dropLogoutR.contains(mx, my);
        burgerH = burgerR.contains(mx, my);
        boolean hand = themeH || dashH || adminH || loginH || startH || profileH || dropDashH || dropLogoutH || burgerH || logoR.contains(mx, my);
        for (Item i : links) {
            i.hovered = i.w > 0 && Fx.inside(mx, my, i.x, i.y, i.w, i.h);
            hand |= i.hovered;
        }
        return hand;
    }

    private static boolean isAdmin(AuthStore.UserRecord u) {
        return u != null && "Admin".equalsIgnoreCase(u.role);
    }

    /** Admins go to the admin dashboard, everyone else to the student dashboard. */
    private void openDashboardForRole() {
        if (listener == null) return;
        AuthStore.UserRecord u = getLoggedInUser();
        if (u == null) return;
        if (isAdmin(u)) listener.openAdminDashboard();
        else listener.openDashboard();
    }

    public boolean isMenuOpen() { return menuOpen; }
    public void closeMenu() { menuOpen = false; }

    @Override public boolean onMouseDown(int mx, int my) { return false; }
    @Override public boolean onMouseUp(int mx, int my) { return false; }
    @Override public boolean onKey(KeyEvent e) {
        if (menuOpen && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            menuOpen = false;
            return true;
        }
        return false;
    }
}
