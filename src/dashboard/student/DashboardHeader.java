import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles the top navigation header for TeamUp Dashboard.
 * Includes the breadcrumb, a working global search (live suggestions for projects,
 * your projects and people) and the user avatar menu.
 */
public class DashboardHeader {

    /** Height of the header bar; clicks above this line belong to the header. */
    public static final int HEIGHT = 60;

    // Search bar state
    private String searchText = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW, searchH;

    // Live search results
    private enum Kind { PROJECT, MY_PROJECT, PERSON, SEE_ALL }

    private static final class Hit {
        final Kind kind;
        final String title, sub;
        final Object ref;
        int y; // painted row top (for clicks)

        Hit(Kind kind, String title, String sub, Object ref) {
            this.kind = kind;
            this.title = title;
            this.sub = sub;
            this.ref = ref;
        }
    }

    private static final int MAX_PER_GROUP = 4;
    private static final int ROW_H = 46;
    private final List<Hit> hits = new ArrayList<Hit>();
    private int highlighted = -1;
    private int matchedProjects, matchedPeople;
    private int dropX, dropY, dropW, dropH;
    private int hoveredHit = -1;

    // User menu dropdown state
    private boolean userMenuOpen = false;
    private int avatarBtnX, avatarBtnY, avatarBtnSize = 34;

    public void paint(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        g.setColor(DashboardTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        int padX = 28;
        int curX = x + padX;

        // Breadcrumb: "Workspace ›"
        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Workspace", curX, y + 35);
        curX += 75;
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("›", curX, y + 35);
        curX += 20;

        // Search pill
        searchX = curX;
        searchY = y + 12;
        searchW = Math.max(220, Math.min(380, w - 460));
        searchH = 36;
        RoundRectangle2D searchPill = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, searchH, searchH);
        g.setColor(searchFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(searchPill);
        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1.2f));
        g.draw(searchPill);

        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 11, 10, 10);
        g.drawLine(searchX + 22, searchY + 19, searchX + 26, searchY + 23);

        g.setFont(DashboardTheme.FONT_REG_12);
        FontMetrics fm = g.getFontMetrics();
        int textX = searchX + 34;
        int textMax = searchW - 34 - 30;
        if (searchText.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString(searchFocused ? "Type to search…" : "Search projects, people and teams...", textX, searchY + 22);
            if (searchFocused && caretOn()) {
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(textX, searchY + 10, textX, searchY + 26);
            }
        } else {
            String shown = searchText;
            while (shown.length() > 1 && fm.stringWidth(shown) > textMax) shown = shown.substring(1);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(shown, textX, searchY + 22);
            if (searchFocused && caretOn()) {
                int cx = textX + fm.stringWidth(shown) + 1;
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(cx, searchY + 10, cx, searchY + 26);
            }
            // clear (×) button
            int cxBtn = searchX + searchW - 26;
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cxBtn + 4, searchY + 14, cxBtn + 12, searchY + 22);
            g.drawLine(cxBtn + 12, searchY + 14, cxBtn + 4, searchY + 22);
        }

        // Right side: user avatar
        int rightX = x + w - padX;
        avatarBtnSize = 34;
        rightX -= avatarBtnSize;
        avatarBtnX = rightX;
        avatarBtnY = y + (h - avatarBtnSize) / 2;
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(avatarBtnX, avatarBtnY, avatarBtnSize, avatarBtnSize);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String ini = dashboard.getProfileInitials();
        g.drawString(ini, avatarBtnX + (avatarBtnSize - g.getFontMetrics().stringWidth(ini)) / 2, avatarBtnY + 22);

        if (suggestionsVisible()) {
            paintSuggestions(g);
        } else {
            dropW = 0;
            dropH = 0;
        }

        if (userMenuOpen) {
            paintUserMenuDropdown(g, x + w, dashboard.getProfileName());
        }
    }

    private static boolean caretOn() {
        return System.currentTimeMillis() % 1000 < 550;
    }

    private boolean suggestionsVisible() {
        return searchFocused && !searchText.trim().isEmpty();
    }

    // ---------------------------------------------------------------- search

    /** Recomputes the live suggestions from the data each page has loaded. */
    private void refreshResults(StudentDashboard d) {
        hits.clear();
        highlighted = -1;
        String q = searchText.trim().toLowerCase(Locale.ROOT);
        matchedProjects = 0;
        matchedPeople = 0;
        if (q.isEmpty()) return;

        List<Hit> projects = new ArrayList<Hit>();
        for (DashboardProjects.ProjectCard p : d.getProjectsView().getAllProjects()) {
            if (matches(q, p.title, p.category, p.description, p.creatorName, join(p.skills))) {
                matchedProjects++;
                if (projects.size() < MAX_PER_GROUP)
                    projects.add(new Hit(Kind.PROJECT, p.title, p.category + "  ·  " + p.currentMembers + " / " + p.maxMembers + " members", p));
            }
        }
        List<Hit> mine = new ArrayList<Hit>();
        for (DashboardMyProjects.MyProjectCard p : d.getMyProjectsView().getAllProjects()) {
            if (matches(q, p.title, p.category, p.description, p.myRole, join(p.skills))) {
                if (mine.size() < MAX_PER_GROUP)
                    mine.add(new Hit(Kind.MY_PROJECT, p.title, (p.isLead ? "You lead" : "Member") + "  ·  " + p.status, p));
            }
        }
        List<Hit> people = new ArrayList<Hit>();
        for (DashboardTeammates.TeammateCard t : d.getTeammatesView().getAllTeammates()) {
            if (matches(q, t.name, t.role, t.university, join(t.skills))) {
                matchedPeople++;
                if (people.size() < MAX_PER_GROUP)
                    people.add(new Hit(Kind.PERSON, t.name, joinNonEmpty(t.role, t.university), t));
            }
        }
        hits.addAll(projects);
        hits.addAll(mine);
        hits.addAll(people);
        hits.add(new Hit(Kind.SEE_ALL, "See all results for \"" + searchText.trim() + "\"",
                matchedProjects >= matchedPeople ? "Opens Discover Projects" : "Opens Find Teammates", null));
    }

    private static boolean matches(String q, String... fields) {
        for (String f : fields) {
            if (f != null && f.toLowerCase(Locale.ROOT).contains(q)) return true;
        }
        return false;
    }

    private static String join(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) for (String s : list) sb.append(s).append(' ');
        return sb.toString();
    }

    private static String joinNonEmpty(String a, String b) {
        boolean ha = a != null && !a.trim().isEmpty(), hb = b != null && !b.trim().isEmpty();
        if (ha && hb) return a.trim() + "  ·  " + b.trim();
        return ha ? a.trim() : (hb ? b.trim() : "");
    }

    /** Runs a suggestion (or the "see all" row). */
    private void activate(Hit hit, StudentDashboard d) {
        String q = searchText.trim();
        searchFocused = false;
        switch (hit.kind) {
            case PROJECT:
                d.setActiveNav(StudentDashboard.NavItem.DISCOVER_PROJECTS);
                d.getProjectsView().openDetails((DashboardProjects.ProjectCard) hit.ref, d);
                break;
            case MY_PROJECT:
                d.setActiveNav(StudentDashboard.NavItem.MY_PROJECTS);
                d.getMyProjectsView().setSearchKeyword(((DashboardMyProjects.MyProjectCard) hit.ref).title);
                break;
            case PERSON:
                d.setActiveNav(StudentDashboard.NavItem.FIND_TEAMMATES);
                d.getTeammatesView().openProfile((DashboardTeammates.TeammateCard) hit.ref);
                break;
            default:
                showAllResults(q, d);
                break;
        }
    }

    /** Enter / "See all": opens the page with the most matches, filtered by the query. */
    private void showAllResults(String q, StudentDashboard d) {
        searchFocused = false;
        if (matchedPeople > matchedProjects) {
            d.setActiveNav(StudentDashboard.NavItem.FIND_TEAMMATES);
            d.getTeammatesView().setSearchKeyword(q);
            d.showToast(matchedPeople + (matchedPeople == 1 ? " person" : " people") + " matching \"" + q + "\"");
        } else {
            d.setActiveNav(StudentDashboard.NavItem.DISCOVER_PROJECTS);
            d.getProjectsView().setSearchKeyword(q);
            d.showToast(matchedProjects == 0 ? "No projects match \"" + q + "\""
                    : matchedProjects + (matchedProjects == 1 ? " project" : " projects") + " matching \"" + q + "\"");
        }
    }

    private void paintSuggestions(Graphics2D g) {
        dropX = searchX;
        dropY = searchY + searchH + 8;
        dropW = Math.max(searchW, 380);

        // measure: rows + section headers
        int h = 8;
        Kind last = null;
        for (Hit hit : hits) {
            if (hit.kind != last && hit.kind != Kind.SEE_ALL) h += 28;
            if (hit.kind == Kind.SEE_ALL) h += 8;
            h += ROW_H;
            last = hit.kind;
        }
        boolean nothing = hits.size() == 1; // only "see all"
        if (nothing) h += 40;
        h += 8;
        dropH = h;

        g.setColor(new Color(15, 23, 42, 28));
        g.fill(new RoundRectangle2D.Float(dropX, dropY + 4, dropW, dropH, 14, 14));
        RoundRectangle2D box = new RoundRectangle2D.Float(dropX, dropY, dropW, dropH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        int cy = dropY + 8;
        if (nothing) {
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(fit(g, "No projects or people match \"" + searchText.trim() + "\"", dropW - 32), dropX + 16, cy + 24);
            cy += 40;
        }
        last = null;
        for (int i = 0; i < hits.size(); i++) {
            Hit hit = hits.get(i);
            if (hit.kind != last && hit.kind != Kind.SEE_ALL) {
                g.setFont(DashboardTheme.FONT_BOLD_10);
                g.setColor(DashboardTheme.TEXT_LIGHT);
                String head = hit.kind == Kind.PROJECT ? "PROJECTS" : hit.kind == Kind.MY_PROJECT ? "MY PROJECTS" : "PEOPLE";
                g.drawString(head, dropX + 16, cy + 19);
                cy += 28;
            }
            if (hit.kind == Kind.SEE_ALL) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(dropX, cy + 4, dropX + dropW, cy + 4);
                cy += 8;
            }
            hit.y = cy;
            boolean active = i == highlighted || i == hoveredHit;
            if (active) {
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(new RoundRectangle2D.Float(dropX + 6, cy + 2, dropW - 12, ROW_H - 4, 10, 10));
            }
            paintHitIcon(g, hit, dropX + 16, cy + (ROW_H - 28) / 2);
            int tx = dropX + 56;
            int tw = dropW - 56 - 16;
            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(hit.kind == Kind.SEE_ALL ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_DARK);
            g.drawString(fit(g, hit.title, tw), tx, cy + 20);
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(fit(g, hit.sub, tw), tx, cy + 36);
            cy += ROW_H;
            last = hit.kind;
        }
    }

    private void paintHitIcon(Graphics2D g, Hit hit, int x, int y) {
        int s = 28;
        if (hit.kind == Kind.PERSON) {
            DashboardTeammates.TeammateCard t = (DashboardTeammates.TeammateCard) hit.ref;
            g.setColor(t.avatarBg != null ? t.avatarBg : DashboardTheme.PURPLE_TINT_BG);
            g.fillOval(x, y, s, s);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(t.avatarText != null ? t.avatarText : DashboardTheme.BRAND_PURPLE);
            FontMetrics fm = g.getFontMetrics();
            String ini = t.initials == null ? "" : t.initials;
            g.drawString(ini, x + (s - fm.stringWidth(ini)) / 2, y + (s + fm.getAscent() - fm.getDescent()) / 2);
            return;
        }
        g.setColor(hit.kind == Kind.SEE_ALL ? new Color(0xF1, 0xF5, 0xF9) : DashboardTheme.PURPLE_TINT_BG);
        g.fill(new RoundRectangle2D.Float(x, y, s, s, 8, 8));
        g.setColor(hit.kind == Kind.SEE_ALL ? DashboardTheme.TEXT_MUTED : DashboardTheme.BRAND_PURPLE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (hit.kind == Kind.SEE_ALL) {
            g.drawOval(x + 8, y + 8, 9, 9);
            g.drawLine(x + 15, y + 15, x + 20, y + 20);
        } else {
            g.drawRoundRect(x + 7, y + 9, 14, 11, 3, 3);
            g.drawLine(x + 8, y + 9, x + 11, y + 7);
            g.drawLine(x + 11, y + 7, x + 14, y + 9);
        }
    }

    private static String fit(Graphics2D g, String s, int maxW) {
        if (s == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    // ---------------------------------------------------------------- user menu

    /** Avatar menu height: header (name + email) plus three rows. */
    private static final int MENU_H = 160;

    private void paintUserMenuDropdown(Graphics2D g, int width, String profileName) {
        int w = 210;
        int h = MENU_H;
        int x = width - w - 20;
        int y = 64;

        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(new Color(0, 0, 0, 35));
        g.fill(new RoundRectangle2D.Float(x, y + 4, w, h, 14, 14));

        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(profileName, x + 16, y + 26);

        AuthStore.UserRecord cur = AuthStore.getCurrentUser();
        String userEmail = (cur != null && cur.email != null) ? cur.email : "student@teamup.edu";
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(userEmail, x + 16, y + 42);

        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x, y + 54, x + w, y + 54);

        int itemY = y + 78;
        // No "Log Out" here: signing out happens only from the landing page.
        String[] menuItems = {"Profile Settings", "Switch Workspace", "← Back to Landing Page"};
        g.setFont(DashboardTheme.FONT_REG_12);
        for (String mi : menuItems) {
            g.setColor(mi.startsWith("←") ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_DARK);
            g.drawString(mi, x + 16, itemY);
            itemY += 30;
        }
    }

    // ---------------------------------------------------------------- input

    /**
     * True when this click belongs to the header: the header bar itself, the open
     * suggestions list, or anywhere while the avatar menu is open (to close it).
     */
    public boolean wantsClick(int mx, int my) {
        if (my < HEIGHT) return true;
        if (userMenuOpen) return true;
        return suggestionsVisible() && inside(mx, my, dropX, dropY, dropW, dropH);
    }

    /** Leaves the search box (called when the user clicks somewhere on the page). */
    public void blurSearch() {
        searchFocused = false;
        hoveredHit = -1;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return w > 0 && h > 0 && mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // Suggestions list
        if (suggestionsVisible() && inside(mx, my, dropX, dropY, dropW, dropH)) {
            for (Hit hit : hits) {
                if (my >= hit.y && my < hit.y + ROW_H) {
                    activate(hit, dashboard);
                    return true;
                }
            }
            return true;
        }

        // User Avatar Menu Toggle
        if (mx >= avatarBtnX && mx <= avatarBtnX + avatarBtnSize && my >= avatarBtnY && my <= avatarBtnY + avatarBtnSize) {
            userMenuOpen = !userMenuOpen;
            searchFocused = false;
            return true;
        }

        // User menu dropdown click
        if (userMenuOpen) {
            int menuW = 210, menuH = MENU_H;
            int menuX = avatarBtnX + avatarBtnSize - menuW, menuY = 64;
            if (mx >= menuX && mx <= menuX + menuW && my >= menuY && my <= menuY + menuH) {
                if (my < menuY + 56) return true; // name / email area: keep the menu open
                int clickedIdx = (my - (menuY + 58)) / 30;
                userMenuOpen = false;
                if (clickedIdx == 0) {
                    dashboard.openModal(StudentDashboard.ActiveModal.COMPLETE_PROFILE);
                } else if (clickedIdx == 1) {
                    dashboard.showToast("Switched active workspace.");
                } else if (clickedIdx == 2) {
                    dashboard.returnToLanding();
                }
                return true;
            }
            userMenuOpen = false;
            return true;
        }

        // Search pill: clear button, or focus
        if (inside(mx, my, searchX, searchY, searchW, searchH)) {
            if (!searchText.isEmpty() && mx >= searchX + searchW - 30) {
                searchText = "";
                refreshResults(dashboard);
            }
            searchFocused = true;
            refreshResults(dashboard);
            return true;
        }

        searchFocused = false;
        return my < HEIGHT;
    }

    public boolean onMouseMove(int mx, int my) {
        int prev = hoveredHit;
        hoveredHit = -1;
        if (suggestionsVisible() && inside(mx, my, dropX, dropY, dropW, dropH)) {
            for (int i = 0; i < hits.size(); i++) {
                if (my >= hits.get(i).y && my < hits.get(i).y + ROW_H) hoveredHit = i;
            }
        }
        boolean overSearch = inside(mx, my, searchX, searchY, searchW, searchH);
        boolean overAvatar = inside(mx, my, avatarBtnX, avatarBtnY, avatarBtnSize, avatarBtnSize);
        return hoveredHit != prev || hoveredHit >= 0 || overSearch || overAvatar;
    }

    public boolean onKey(KeyEvent e, StudentDashboard dashboard) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            if (userMenuOpen) { userMenuOpen = false; return true; }
            if (searchFocused) { searchFocused = false; return true; }
        }
        if (!searchFocused) return false;

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP) {
            if (!hits.isEmpty()) {
                int n = hits.size();
                highlighted = code == KeyEvent.VK_DOWN ? (highlighted + 1) % n : (highlighted <= 0 ? n - 1 : highlighted - 1);
            }
            return true;
        }
        if (code == KeyEvent.VK_ENTER) {
            if (searchText.trim().isEmpty()) return true;
            if (highlighted >= 0 && highlighted < hits.size()) activate(hits.get(highlighted), dashboard);
            else showAllResults(searchText.trim(), dashboard);
            return true;
        }
        if (code == KeyEvent.VK_BACK_SPACE) {
            if (searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                refreshResults(dashboard);
            }
            return true;
        }
        char c = e.getKeyChar();
        if (c >= 32 && c != 127 && c != KeyEvent.CHAR_UNDEFINED && !e.isControlDown() && searchText.length() < 60) {
            searchText += c;
            refreshResults(dashboard);
            return true;
        }
        return true; // focused search box swallows other keys (arrows, shift…)
    }

    public void closePopups() {
        userMenuOpen = false;
        searchFocused = false;
    }
}
