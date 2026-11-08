import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Top header bar for TeamUp Admin Dashboard.
 * Breadcrumb, live global search (⌘ K), notification center, and admin profile menu.
 * Wired directly to AdminDataStore for cross-platform global search.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminHeader {

    private String breadcrumb = "Overview";

    private int searchX, searchY, searchW = 320, searchH = 36;
    private int bellX, bellY, bellSize = 34;
    private int userX, userY, userW = 42, userH = 34;

    private boolean searchFocused = false;
    private String searchText = "";
    private float caretAnim = 0f;

    private boolean profileOpen = false;
    private boolean notifOpen = false;

    // Search popup bounds
    private int searchDropX, searchDropY, searchDropW, searchDropH;
    private int searchMatchCount = 0;

    public boolean isAnyPopoverOpen() {
        return profileOpen || notifOpen || (searchFocused && !searchText.trim().isEmpty() && searchMatchCount > 0);
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public void update(float dt) {
        caretAnim += dt * 3.5f;
        if (caretAnim > 2f) caretAnim -= 2f;
    }

    public void paint(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(AdminTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        int curX = x + 32;

        // Breadcrumb: "Administration / <Section>"
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Administration", curX, y + 36);

        FontMetrics bfm = g.getFontMetrics();
        int slashX = curX + bfm.stringWidth("Administration") + 8;
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("/", slashX, y + 36);

        int ovX = slashX + 12;
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(breadcrumb, ovX, y + 36);

        // Right side controls
        int rightPad = 32;
        int rightX = x + w - rightPad;

        // User Avatar button (JL)
        userX = rightX - userW;
        userY = y + 16;
        int avSize = 32;
        g.setColor(AdminTheme.BRAND_TINT);
        g.fillOval(userX, userY + 1, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("JL", userX + 9, userY + 21);
        Draw.drawChevronDown(g, userX + avSize + 4, userY + 14, 8, AdminTheme.TEXT_LIGHT, 0f);

        // Notification Bell Icon
        bellX = userX - 44;
        bellY = y + 17;
        paintBellIcon(g, bellX, bellY);

        // Search pill with "⌘ K" shortcut
        searchW = 310;
        searchX = bellX - searchW - 18;
        searchY = y + 15;
        searchH = 34;

        RoundRectangle2D searchBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 10, 10);
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(searchBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1f));
        g.draw(searchBox);

        // Search icon
        Draw.drawSearchIcon(g, searchX + 11, searchY + 9, 16, AdminTheme.TEXT_LIGHT);

        // Placeholder or entered text
        if (searchText.isEmpty() && !searchFocused) {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Search users, projects, reports...", searchX + 34, searchY + 22);
        } else {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(searchText, searchX + 34, searchY + 22);

            if (searchFocused && ((int) caretAnim % 2 == 0)) {
                FontMetrics sfm = g.getFontMetrics();
                int cw = sfm.stringWidth(searchText);
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.drawLine(searchX + 34 + cw + 1, searchY + 9, searchX + 34 + cw + 1, searchY + 25);
            }
        }

        // "⌘ K" Pill chip
        int kSizeW = 28;
        int kSizeH = 20;
        int kX = searchX + searchW - kSizeW - 8;
        int kY = searchY + 7;
        RoundRectangle2D kPill = new RoundRectangle2D.Float(kX, kY, kSizeW, kSizeH, 6, 6);
        g.setColor(Color.WHITE);
        g.fill(kPill);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(kPill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("⌘ K", kX + 4, kY + 14);

        // Popovers
        if (searchFocused && !searchText.trim().isEmpty()) {
            paintSearchLiveResults(g, searchX, y + h + 4);
        }

        if (notifOpen) {
            paintNotificationsDropdown(g, bellX - 160, y + h + 4);
        }

        if (profileOpen) {
            paintProfileDropdown(g, userX - 110, y + h + 4);
        }
    }

    private void paintBellIcon(Graphics2D g, int bx, int by) {
        g.setColor(AdminTheme.TEXT_MUTED);
        g.setStroke(new BasicStroke(1.4f));
        g.drawArc(bx + 4, by + 4, 14, 14, 0, 180);
        g.drawLine(bx + 4, by + 11, bx + 18, by + 11);
        g.drawLine(bx + 9, by + 16, bx + 13, by + 16);

        // Unread red dot
        g.setColor(new Color(0xEF, 0x44, 0x44));
        g.fillOval(bx + 15, by + 3, 6, 6);
    }

    private void paintSearchLiveResults(Graphics2D g, int sx, int sy) {
        List<AdminDataStore.SearchResult> results = AdminDataStore.getInstance().searchAll(searchText);
        searchMatchCount = Math.min(5, results.size());
        searchDropX = sx;
        searchDropY = sy;
        searchDropW = 340;
        searchDropH = searchMatchCount == 0 ? 54 : (searchMatchCount * 52 + 16);

        RoundRectangle2D box = new RoundRectangle2D.Float(searchDropX, searchDropY, searchDropW, searchDropH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(box);

        if (searchMatchCount == 0) {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No matching records found for '" + searchText + "'", searchDropX + 16, searchDropY + 32);
            return;
        }

        int curY = searchDropY + 12;
        for (int i = 0; i < searchMatchCount; i++) {
            AdminDataStore.SearchResult r = results.get(i);
            // Title
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(r.title, searchDropX + 14, curY + 16);

            // Subtitle
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(r.subtitle, searchDropX + 14, curY + 32);

            // Type badge
            int tw = 54;
            int tx = searchDropX + searchDropW - tw - 12;
            RoundRectangle2D tPill = new RoundRectangle2D.Float(tx, curY + 6, tw, 18, 6, 6);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(tPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString(r.type, tx + 8, curY + 19);

            curY += 50;
            if (i < searchMatchCount - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(searchDropX + 14, curY - 2, searchDropX + searchDropW - 14, curY - 2);
            }
        }
    }

    private void paintNotificationsDropdown(Graphics2D g, int px, int py) {
        int dropW = 280;
        int dropH = 180;
        RoundRectangle2D dropBox = new RoundRectangle2D.Float(px, py, dropW, dropH, 12, 12);

        g.setColor(Color.WHITE);
        g.fill(dropBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(dropBox);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Platform Alerts (3 unread)", px + 16, py + 26);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(px, py + 38, px + dropW, py + 38);

        String[] alerts = {
                "Report R-1048: Hostile message flagged",
                "Project SkillSwap awaiting review",
                "Noah Williams suspended by Jordan Lee"
        };
        int iy = py + 62;
        for (String a : alerts) {
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString("• " + a, px + 14, iy);
            iy += 40;
        }
    }

    private void paintProfileDropdown(Graphics2D g, int px, int py) {
        int dropW = 190;
        int dropH = 110;
        RoundRectangle2D dropBox = new RoundRectangle2D.Float(px, py, dropW, dropH, 12, 12);

        g.setColor(new Color(0, 0, 0, 14));
        g.fill(new RoundRectangle2D.Float(px, py + 4, dropW, dropH, 12, 12));

        g.setColor(Color.WHITE);
        g.fill(dropBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(dropBox);

        String[] items = {"Switch to Student View", "Return to Landing Page", "Admin Settings"};
        int iy = py + 24;
        for (String item : items) {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(item, px + 16, iy);
            iy += 30;
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // Search Live Result click
        if (searchFocused && !searchText.trim().isEmpty() && searchMatchCount > 0) {
            if (mx >= searchDropX && mx <= searchDropX + searchDropW && my >= searchDropY && my <= searchDropY + searchDropH) {
                int clickedIdx = (my - searchDropY - 12) / 50;
                List<AdminDataStore.SearchResult> results = AdminDataStore.getInstance().searchAll(searchText);
                if (clickedIdx >= 0 && clickedIdx < results.size()) {
                    AdminDataStore.SearchResult chosen = results.get(clickedIdx);
                    searchFocused = false;
                    searchText = "";
                    if ("User".equalsIgnoreCase(chosen.type)) {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.USERS);
                    } else if ("Project".equalsIgnoreCase(chosen.type)) {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.PROJECTS);
                    } else {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.REPORTS);
                    }
                    dashboard.showToast("Navigated to: " + chosen.title);
                    return true;
                }
            }
        }

        // Profile toggle
        if (mx >= userX && mx <= userX + userW && my >= userY && my <= userY + userH) {
            profileOpen = !profileOpen;
            notifOpen = false;
            return true;
        }

        // Bell click
        if (mx >= bellX && mx <= bellX + 28 && my >= bellY && my <= bellY + 28) {
            notifOpen = !notifOpen;
            profileOpen = false;
            return true;
        }

        // Profile menu items click
        if (profileOpen) {
            int px = userX - 110;
            int py = 68 + 4;
            if (mx >= px && mx <= px + 190 && my >= py && my <= py + 110) {
                int clickedIdx = (my - py) / 34;
                profileOpen = false;
                if (clickedIdx == 0) {
                    dashboard.switchToStudentDashboard();
                } else if (clickedIdx == 1) {
                    dashboard.returnToLanding();
                } else {
                    dashboard.navigateTo(AdminSidebar.AdminNavItem.SETTINGS);
                }
                return true;
            }
            profileOpen = false;
        }

        if (notifOpen) {
            notifOpen = false;
        }

        // Search bar focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        return false;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (!searchFocused) return false;

        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            if (!searchText.isEmpty()) {
                dashboard.showToast("Filtered platform for: " + searchText);
            }
            return true;
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            searchFocused = false;
            return true;
        } else if (ch >= 32 && ch <= 126) {
            searchText += ch;
            return true;
        }
        return false;
    }
}
