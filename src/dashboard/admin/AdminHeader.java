import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Top header bar for TeamUp Admin Dashboard.
 * Breadcrumb, live responsive global search (⌘ K), and admin profile menu.
 * Fully dynamic: real-time search queries across users, projects, reports, and reviews,
 * adaptive width, clear button, keyboard navigation, and popover overlays.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminHeader {

    private String breadcrumb = "Overview";

    private int searchX, searchY, searchW = 340, searchH = 36;
    private int clearSearchX, clearSearchY, clearSearchSize = 18;
    private boolean clearHovered = false;

    private int userX, userY, userW = 44, userH = 34;
    private boolean userHovered = false;

    private boolean searchFocused = false;
    private String searchText = "";
    private float caretAnim = 0f;

    private boolean profileOpen = false;
    private int profileHoverIdx = -1;

    // Search popup bounds & state
    private int searchDropX, searchDropY, searchDropW, searchDropH;
    private int searchMatchCount = 0;
    private int hoveredResultIdx = -1;

    public boolean isAnyPopoverOpen() {
        return profileOpen || (searchFocused && !searchText.trim().isEmpty() && searchMatchCount > 0);
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public String getBreadcrumb() {
        return breadcrumb;
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
        boolean changed = false;

        // Clear button hover
        boolean prevClear = clearHovered;
        clearHovered = (!searchText.isEmpty() && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize && my >= clearSearchY && my <= clearSearchY + clearSearchSize);
        if (clearHovered != prevClear) changed = true;

        // User avatar hover
        boolean prevUser = userHovered;
        userHovered = (mx >= userX && mx <= userX + userW && my >= userY && my <= userY + userH);
        if (userHovered != prevUser) changed = true;

        // Live search result item hover
        int prevRes = hoveredResultIdx;
        hoveredResultIdx = -1;
        if (searchFocused && !searchText.trim().isEmpty() && searchMatchCount > 0) {
            int listStartY = searchDropY + 28;
            if (mx >= searchDropX && mx <= searchDropX + searchDropW && my >= listStartY && my <= listStartY + searchMatchCount * 50) {
                int idx = (my - listStartY) / 50;
                if (idx >= 0 && idx < searchMatchCount) {
                    hoveredResultIdx = idx;
                }
            }
        }
        if (hoveredResultIdx != prevRes) changed = true;

        // Profile item hover
        int prevProf = profileHoverIdx;
        profileHoverIdx = -1;
        if (profileOpen) {
            int px = userX - 110;
            int py = 68 + 4 + 8;
            if (mx >= px && mx <= px + 200 && my >= py && my <= py + 3 * 36) {
                int idx = (my - py) / 36;
                if (idx >= 0 && idx < 3) {
                    profileHoverIdx = idx;
                }
            }
        }
        if (profileHoverIdx != prevProf) changed = true;

        return changed;
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
        if (!searchFocused) return false;

        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            if (!searchText.trim().isEmpty()) {
                List<AdminDataStore.SearchResult> results = AdminDataStore.getInstance().searchAll(searchText);
                if (!results.isEmpty()) {
                    int targetIdx = (hoveredResultIdx >= 0 && hoveredResultIdx < results.size()) ? hoveredResultIdx : 0;
                    AdminDataStore.SearchResult chosen = results.get(targetIdx);
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
                } else {
                    dashboard.showToast("No matches found for: " + searchText);
                }
            }
            return true;
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            searchFocused = false;
            return true;
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            if (searchMatchCount > 0) {
                hoveredResultIdx = (hoveredResultIdx + 1) % searchMatchCount;
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
            if (searchMatchCount > 0) {
                hoveredResultIdx = (hoveredResultIdx <= 0) ? searchMatchCount - 1 : hoveredResultIdx - 1;
                return true;
            }
        } else if (ch >= 32 && ch <= 126) {
            searchText += ch;
            hoveredResultIdx = -1;
            return true;
        }
        return false;
    }

    private String truncateText(FontMetrics fm, String text, int maxW) {
        if (text == null) return "";
        if (fm.stringWidth(text) <= maxW) return text;
        String ell = "...";
        int ellW = fm.stringWidth(ell);
        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) + ellW > maxW) {
            len--;
        }
        return text.substring(0, Math.max(0, len)) + ell;
    }

    // Getters and setters for testing & dynamic integration
    public boolean isSearchFocused() { return searchFocused; }
    public void setSearchFocused(boolean f) { this.searchFocused = f; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String text) { this.searchText = (text == null) ? "" : text; }
    public int getSearchMatchCount() { return searchMatchCount; }
    public boolean isProfileOpen() { return profileOpen; }
    public void setProfileOpen(boolean open) { this.profileOpen = open; }
    public boolean isClearHovered() { return clearHovered; }
    public int getHoveredResultIdx() { return hoveredResultIdx; }
    public int getSearchX() { return searchX; }
    public int getSearchY() { return searchY; }
    public int getSearchW() { return searchW; }
    public int getSearchH() { return searchH; }
    public int getUserX() { return userX; }
}
