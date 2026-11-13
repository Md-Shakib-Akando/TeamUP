import java.awt.*;

/**
 * Top header bar for TeamUp Admin Dashboard.
 * Displays clean breadcrumb: "Administration / <Section>".
 * Search bar and profile dropdown have been completely removed as requested.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminHeader {

    private String breadcrumb = "Overview";

    // Layout bounds kept as off-screen constants for API compatibility
    private int searchX = -9999;
    private int searchY = -9999;
    private int searchW = 0;
    private int searchH = 0;
    private int userX = -9999;
    private int userY = -9999;
    private int userW = 0;
    private int userH = 0;

    public boolean isAnyPopoverOpen() {
        return false;
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = (breadcrumb == null) ? "Overview" : breadcrumb;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public void update(float dt) {
        // No animations needed
    }

    public void paint(Graphics2D g, int x, int y, int w, int h) {
        // Header background
        g.setColor(AdminTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);

        // Header bottom border
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        // Clean breadcrumb: "Administration / <Section>"
        int curX = x + 32;
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

        int breadcrumbEnd = ovX + bfm.stringWidth(breadcrumb) + 24;

        // Right side controls
        int rightPad = 28;
        int rightX = x + w - rightPad;

        // User Avatar button (JL)
        userX = rightX - userW;
        userY = y + 16;
        int avSize = 32;

        if (userHovered || profileOpen) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fillRoundRect(userX - 4, userY - 2, userW + 8, userH + 4, 8, 8);
        }

        g.setColor(AdminTheme.BRAND_TINT);
        g.fillOval(userX, userY + 1, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("JL", userX + 9, userY + 21);
        AdminTheme.drawChevronDown(g, userX + avSize + 4, userY + 14, 8, AdminTheme.TEXT_LIGHT, profileOpen ? 180f : 0f);

        // Dynamic Responsive Search Bar (Bell button removed, giving maximum room)
        int searchRight = userX - 20;
        int availableSpace = searchRight - breadcrumbEnd;
        // Adaptive width: min 200, default 340, up to 420 on large screens
        searchW = Math.max(200, Math.min(420, availableSpace));
        searchX = searchRight - searchW;
        searchY = y + 15;
        searchH = 36;

        RoundRectangle2D searchBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 10, 10);
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(searchBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1f));
        g.draw(searchBox);

        // Search icon
        AdminTheme.drawSearchIcon(g, searchX + 12, searchY + 10, 16, searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_LIGHT);

        // Clear button & shortcut chip bounds
        clearSearchSize = 18;
        clearSearchX = searchX + searchW - clearSearchSize - 10;
        clearSearchY = searchY + (searchH - clearSearchSize) / 2;

        // Text & Caret
        int textLeft = searchX + 36;
        int maxTextWidth = searchW - 74;

        if (searchText.isEmpty() && !searchFocused) {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_LIGHT);
            String placeholder = (searchW < 260) ? "Search..." : "Search users, projects, reports, reviews...";
            g.drawString(placeholder, textLeft, searchY + 23);
        } else {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_DARK);
            FontMetrics tfm = g.getFontMetrics();
            String renderedText = truncateText(tfm, searchText, maxTextWidth);
            g.drawString(renderedText, textLeft, searchY + 23);

            if (searchFocused && ((int) caretAnim % 2 == 0)) {
                int cw = tfm.stringWidth(renderedText);
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(textLeft + cw + 1, searchY + 10, textLeft + cw + 1, searchY + 26);
            }
        }

        // Action icon on right side of search bar:
        // If text is entered, show interactive clear '✕' button
        // Otherwise, show '⌘ K' keyboard shortcut chip
        if (!searchText.isEmpty()) {
            g.setColor(clearHovered ? new Color(0xE2, 0xE8, 0xF0) : new Color(0xF1, 0xF5, 0xF9));
            g.fillOval(clearSearchX, clearSearchY, clearSearchSize, clearSearchSize);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(clearHovered ? AdminTheme.TEXT_DARK : AdminTheme.TEXT_MUTED);
            FontMetrics xfm = g.getFontMetrics();
            g.drawString("✕", clearSearchX + (clearSearchSize - xfm.stringWidth("✕")) / 2, clearSearchY + 13);
        } else {
            int kSizeW = 32;
            int kSizeH = 20;
            int kX = searchX + searchW - kSizeW - 8;
            int kY = searchY + (searchH - kSizeH) / 2;
            RoundRectangle2D kPill = new RoundRectangle2D.Float(kX, kY, kSizeW, kSizeH, 6, 6);
            g.setColor(Color.WHITE);
            g.fill(kPill);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(kPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("⌘ K", kX + 5, kY + 14);
        }

        // Live Search Results Dropdown
        if (searchFocused && !searchText.trim().isEmpty()) {
            paintSearchLiveResults(g, searchX, y + h + 4, x + w);
        }

        // Profile Dropdown
        if (profileOpen) {
            paintProfileDropdown(g, userX - 110, y + h + 4);
        }
    }

    private void paintSearchLiveResults(Graphics2D g, int sx, int sy, int maxRight) {
        List<AdminDataStore.SearchResult> results = AdminDataStore.getInstance().searchAll(searchText);
        searchMatchCount = Math.min(6, results.size());
        searchDropX = sx;
        searchDropY = sy;
        searchDropW = Math.max(searchW, 380);

        if (searchDropX + searchDropW > maxRight - 16) {
            searchDropX = maxRight - 16 - searchDropW;
        }

        searchDropH = (searchMatchCount == 0) ? 68 : (28 + searchMatchCount * 50 + 6);

        // Soft drop shadow
        g.setColor(new Color(0, 0, 0, 16));
        g.fill(new RoundRectangle2D.Float(searchDropX, searchDropY + 3, searchDropW, searchDropH, 12, 12));

        // White card background
        RoundRectangle2D box = new RoundRectangle2D.Float(searchDropX, searchDropY, searchDropW, searchDropH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(box);

        if (searchMatchCount == 0) {
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString("No results found for \"" + searchText + "\"", searchDropX + 16, searchDropY + 28);
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("Check spelling or try searching for users, projects, reports, or reviews.", searchDropX + 16, searchDropY + 48);
            return;
        }

        // Header bar in dropdown
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("SEARCH RESULTS (" + results.size() + ")", searchDropX + 14, searchDropY + 19);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_LIGHT);
        String escHint = "ESC to dismiss";
        g.drawString(escHint, searchDropX + searchDropW - g.getFontMetrics().stringWidth(escHint) - 14, searchDropY + 19);

        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(searchDropX, searchDropY + 28, searchDropX + searchDropW, searchDropY + 28);

        // Results list
        int curY = searchDropY + 28;
        for (int i = 0; i < searchMatchCount; i++) {
            AdminDataStore.SearchResult r = results.get(i);
            int itemY = curY + i * 50;

            // Hover row highlight
            if (i == hoveredResultIdx) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRect(searchDropX + 1, itemY, searchDropW - 2, 50);
            }

            // Entity badge
            Color badgeBg;
            Color badgeText;
            if ("User".equalsIgnoreCase(r.type)) {
                badgeBg = new Color(0xEB, 0xF5, 0xFF);
                badgeText = new Color(0x1E, 0x40, 0xAF);
            } else if ("Project".equalsIgnoreCase(r.type)) {
                badgeBg = new Color(0xF5, 0xF3, 0xFF);
                badgeText = new Color(0x6D, 0x28, 0xD9);
            } else if ("Review".equalsIgnoreCase(r.type)) {
                badgeBg = new Color(0xEC, 0xFD, 0xF5);
                badgeText = new Color(0x04, 0x78, 0x57);
            } else {
                badgeBg = new Color(0xFE, 0xF2, 0xF2);
                badgeText = new Color(0xDC, 0x26, 0x26);
            }

            int bw = 56;
            int bx = searchDropX + searchDropW - bw - 14;
            int by = itemY + 16;
            RoundRectangle2D bPill = new RoundRectangle2D.Float(bx, by, bw, 18, 6, 6);
            g.setColor(badgeBg);
            g.fill(bPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(badgeText);
            FontMetrics bfm = g.getFontMetrics();
            g.drawString(r.type, bx + (bw - bfm.stringWidth(r.type)) / 2, by + 13);

            // Title
            int maxTitleW = searchDropW - bw - 42;
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            String titleStr = truncateText(g.getFontMetrics(), r.title, maxTitleW);
            g.drawString(titleStr, searchDropX + 14, itemY + 20);

            // Subtitle
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            String subStr = truncateText(g.getFontMetrics(), r.subtitle, maxTitleW);
            g.drawString(subStr, searchDropX + 14, itemY + 38);

            // Row divider
            if (i < searchMatchCount - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(searchDropX + 14, itemY + 50, searchDropX + searchDropW - 14, itemY + 50);
            }
        }
    }

    private void paintProfileDropdown(Graphics2D g, int px, int py) {
        int dropW = 200;
        int dropH = 156;
        RoundRectangle2D dropBox = new RoundRectangle2D.Float(px, py, dropW, dropH, 12, 12);

        g.setColor(new Color(0, 0, 0, 14));
        g.fill(new RoundRectangle2D.Float(px, py + 3, dropW, dropH, 12, 12));

        g.setColor(Color.WHITE);
        g.fill(dropBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(dropBox);

        String[] items = {"Switch to Student View", "Return to Landing Page", "Admin Settings", "Log Out"};
        int iy = py + 8;
        for (int i = 0; i < items.length; i++) {
            int itemY = iy + i * 36;
            if (i == profileHoverIdx) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(px + 6, itemY, dropW - 12, 32, 6, 6);
            }
            g.setFont(AdminTheme.FONT_REG_12);
            if (i == 3) {
                g.setColor(new Color(0xEF, 0x44, 0x44)); // red
            } else {
                g.setColor(AdminTheme.TEXT_DARK);
            }
            g.drawString(items[i], px + 16, itemY + 21);
        }
    }

    public boolean onMouseMove(int mx, int my) {
        return false;
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Clear button click
        if (!searchText.isEmpty() && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize && my >= clearSearchY && my <= clearSearchY + clearSearchSize) {
            searchText = "";
            searchMatchCount = 0;
            hoveredResultIdx = -1;
            dashboard.showToast("Search cleared");
            return true;
        }

        // 2. Search Live Result click
        if (searchFocused && !searchText.trim().isEmpty() && searchMatchCount > 0) {
            int listStartY = searchDropY + 28;
            if (mx >= searchDropX && mx <= searchDropX + searchDropW && my >= listStartY && my <= listStartY + searchMatchCount * 50) {
                int clickedIdx = (my - listStartY) / 50;
                List<AdminDataStore.SearchResult> results = AdminDataStore.getInstance().searchAll(searchText);
                if (clickedIdx >= 0 && clickedIdx < results.size()) {
                    AdminDataStore.SearchResult chosen = results.get(clickedIdx);
                    searchFocused = false;
                    searchText = "";
                    searchMatchCount = 0;
                    hoveredResultIdx = -1;
                    if ("User".equalsIgnoreCase(chosen.type)) {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.USERS);
                    } else if ("Project".equalsIgnoreCase(chosen.type)) {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.PROJECTS);
                    } else if ("Review".equalsIgnoreCase(chosen.type)) {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.FEEDBACK);
                    } else {
                        dashboard.navigateTo(AdminSidebar.AdminNavItem.REPORTS);
                    }
                    dashboard.showToast("Navigated to: " + chosen.title);
                    return true;
                }
            }
        }

        // 3. User Avatar / Profile toggle
        if (mx >= userX && mx <= userX + userW && my >= userY && my <= userY + userH) {
            profileOpen = !profileOpen;
            return true;
        }

        // 4. Profile menu items click
        if (profileOpen) {
            int px = userX - 110;
            int py = 68 + 4;
            int dropW = 200;
            int dropH = 156;
            if (mx >= px && mx <= px + dropW && my >= py && my <= py + dropH) {
                int clickedIdx = (my - (py + 8)) / 36;
                profileOpen = false;
                if (clickedIdx == 0) {
                    dashboard.switchToStudentDashboard();
                } else if (clickedIdx == 1) {
                    dashboard.returnToLanding();
                } else if (clickedIdx == 2) {
                    dashboard.navigateTo(AdminSidebar.AdminNavItem.SETTINGS);
                } else if (clickedIdx >= 3) {
                    AuthStore.logout();
                    dashboard.returnToLanding();
                }
                return true;
            }
            profileOpen = false;
        }

        // 5. Search bar focus click
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            profileOpen = false;
            return true;
        } else {
            searchFocused = false;
        }

        return false;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        return false;
    }

    // API getters and setters for compatibility
    public boolean isSearchFocused() { return false; }
    public void setSearchFocused(boolean f) { }
    public String getSearchText() { return ""; }
    public void setSearchText(String text) { }
    public int getSearchMatchCount() { return 0; }
    public boolean isProfileOpen() { return false; }
    public void setProfileOpen(boolean open) { }
    public boolean isClearHovered() { return false; }
    public int getHoveredResultIdx() { return -1; }
    public int getSearchX() { return searchX; }
    public int getSearchY() { return searchY; }
    public int getSearchW() { return searchW; }
    public int getSearchH() { return searchH; }
    public int getUserX() { return userX; }
}
