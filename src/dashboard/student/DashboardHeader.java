import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Handles the top navigation header for TeamUp Dashboard.
 * Includes search bar, notification dropdown, user profile menu, and "+ Create project" ghost button.
 */
public class DashboardHeader {

    // Search bar state
    private String searchText = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW, searchH;

    // Notification dropdown state
    private boolean notifOpen = false;
    private int bellBtnX, bellBtnY, bellBtnSize = 34;

    // User menu dropdown state
    private boolean userMenuOpen = false;
    private int avatarBtnX, avatarBtnY, avatarBtnSize = 34;

    // Top "+ Create project" button state
    private int topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH;
    private boolean topCreateBtnHovered = false;

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

        // Search Pill Input
        searchX = curX;
        searchY = y + 12;
        searchW = Math.min(320, w - 460);
        searchH = 36;
        RoundRectangle2D searchPill = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, searchH, searchH);
        g.setColor(searchFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(searchPill);
        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(searchPill);

        // Search Icon
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 11, 10, 10);
        g.drawLine(searchX + 22, searchY + 19, searchX + 26, searchY + 23);

        g.setFont(DashboardTheme.FONT_REG_12);
        if (searchText.isEmpty() && !searchFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Search projects, people and teams...", searchX + 34, searchY + 22);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(searchText + (searchFocused && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""), searchX + 34, searchY + 22);
        }

        // Right side controls
        int rightX = x + w - padX;

        // User Avatar Circle
        avatarBtnSize = 34;
        rightX -= avatarBtnSize;
        avatarBtnX = rightX;
        avatarBtnY = y + (h - avatarBtnSize) / 2;
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(avatarBtnX, avatarBtnY, avatarBtnSize, avatarBtnSize);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("AM", avatarBtnX + 8, avatarBtnY + 22);

        // Bell Icon
        rightX -= 36;
        bellBtnSize = 34;
        bellBtnX = rightX;
        bellBtnY = y + (h - bellBtnSize) / 2;
        int iconX = bellBtnX + 10;
        int iconY = bellBtnY + 10;

        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(iconX, iconY, 14, 14, 0, 180);
        g.drawLine(iconX - 2, iconY + 14, iconX + 16, iconY + 14);
        g.drawOval(iconX + 5, iconY + 14, 4, 3);
        // Purple notification dot
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.fillOval(iconX + 11, iconY - 1, 6, 6);

        // "+ Create project" Ghost Pill Button
        rightX -= 140;
        topCreateBtnX = rightX;
        topCreateBtnY = y + 13;
        topCreateBtnW = 126;
        topCreateBtnH = 34;
        RoundRectangle2D topBtnShape = new RoundRectangle2D.Float(topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH, topCreateBtnH, topCreateBtnH);
        g.setColor(topCreateBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(topBtnShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(topBtnShape);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("+ Create project", topCreateBtnX + 14, topCreateBtnY + 21);

        // Notification dropdown if open
        if (notifOpen) {
            paintNotificationDropdown(g, x + w, dashboard.getNotifications());
        }

        // User menu dropdown if open
        if (userMenuOpen) {
            paintUserMenuDropdown(g, x + w, dashboard.getProfileName());
        }
    }

    private void paintNotificationDropdown(Graphics2D g, int width, List<String> notifications) {
        int w = 310;
        int h = 230;
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
        g.drawString("Notifications", x + 16, y + 24);

        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("Mark all read", x + w - 85, y + 24);

        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x, y + 36, x + w, y + 36);

        int itemY = y + 54;
        g.setFont(DashboardTheme.FONT_REG_12);
        for (String notif : notifications) {
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.fillOval(x + 14, itemY - 6, 6, 6);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(notif, x + 28, itemY);
            itemY += 40;
        }
    }

    private void paintUserMenuDropdown(Graphics2D g, int width, String profileName) {
        int w = 210;
        int h = 170;
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

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("alex.morgan@uw.edu", x + 16, y + 42);

        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x, y + 54, x + w, y + 54);

        int itemY = y + 78;
        String[] menuItems = {"Profile Settings", "Switch Workspace", "← Back to Landing Page"};
        g.setFont(DashboardTheme.FONT_REG_12);
        for (String mi : menuItems) {
            g.setColor(mi.startsWith("←") ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_DARK);
            g.drawString(mi, x + 16, itemY);
            itemY += 32;
        }
    }

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // Notification Bell Toggle
        if (mx >= bellBtnX && mx <= bellBtnX + bellBtnSize && my >= bellBtnY && my <= bellBtnY + bellBtnSize) {
            notifOpen = !notifOpen;
            userMenuOpen = false;
            return true;
        }

        // User Avatar Menu Toggle
        if (mx >= avatarBtnX && mx <= avatarBtnX + avatarBtnSize && my >= avatarBtnY && my <= avatarBtnY + avatarBtnSize) {
            userMenuOpen = !userMenuOpen;
            notifOpen = false;
            return true;
        }

        // Notification popup click to dismiss
        if (notifOpen) {
            notifOpen = false;
            dashboard.showToast("Notifications marked as read.");
            return true;
        }

        // User menu dropdown click to open profile
        if (userMenuOpen) {
            userMenuOpen = false;
            dashboard.openModal(StudentDashboard.ActiveModal.COMPLETE_PROFILE);
            return true;
        }

        // Search bar focus
        searchFocused = (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH);

        // Top "+ Create project" button
        if (mx >= topCreateBtnX && mx <= topCreateBtnX + topCreateBtnW &&
                my >= topCreateBtnY && my <= topCreateBtnY + topCreateBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.CREATE_PROJECT);
            return true;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        boolean p3 = topCreateBtnHovered;
        topCreateBtnHovered = (mx >= topCreateBtnX && mx <= topCreateBtnX + topCreateBtnW &&
                my >= topCreateBtnY && my <= topCreateBtnY + topCreateBtnH);
        if (topCreateBtnHovered != p3) changed = true;
        return changed || topCreateBtnHovered;
    }

    public boolean onKey(KeyEvent e, StudentDashboard dashboard) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (notifOpen) { notifOpen = false; return true; }
            if (userMenuOpen) { userMenuOpen = false; return true; }
        }

        // Typing into search box
        if (searchFocused) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (searchText.length() > 0) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                dashboard.showToast("Search results for '" + searchText + "': 2 projects, 2 peers matching.");
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && searchText.length() < 40) {
                searchText += c;
                return true;
            }
        }
        return false;
    }

    public void closePopups() {
        notifOpen = false;
        userMenuOpen = false;
    }
}

