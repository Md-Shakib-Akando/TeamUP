import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Sidebar navigation for TeamUp Admin Dashboard.
 * Includes TeamUp Admin badge, 7 navigation items, return to landing link, and admin profile card.
 */
public class AdminSidebar {

    public enum AdminNavItem {
        OVERVIEW("Overview"),
        USERS("Users"),
        PROJECTS("Projects"),
        REPORTS("Reports & Reviews"),
        SKILLS("Categories & Skills"),
        ACTIVITY("Activity Logs"),
        SETTINGS("Settings");

        public final String label;
        AdminNavItem(String label) { this.label = label; }
    }

    private AdminNavItem activeNav = AdminNavItem.OVERVIEW;
    private AdminNavItem hoveredNav = null;

    private int returnBtnX, returnBtnY, returnBtnW, returnBtnH;
    private boolean returnBtnHovered = false;

    public AdminNavItem getActiveNav() { return activeNav; }
    public void setActiveNav(AdminNavItem item) { this.activeNav = item; }

    public void paint(Graphics2D g, int x, int y, int w, int h, AdminDashboard dashboard) {
        g.setColor(AdminTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x + w - 1, y, x + w - 1, y + h);

        int curY = y + 20;
        int padX = 18;

        // Brand Logo + "TeamUp" + "Admin" badge
        paintLogo(g, x + padX, curY);
        curY += 54;

        // Navigation Items
        int itemH = 38;
        int itemW = w - padX * 2;

        for (AdminNavItem item : AdminNavItem.values()) {
            boolean isSel = (item == activeNav);
            boolean isHov = (item == hoveredNav);
            int itemX = x + padX;
            int itemY = curY;

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemX, itemY, itemW, itemH, 10, 10);
            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_TINT);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fill(itemShape);
            }

            Color iconCol = isSel ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MUTED;
            paintNavIcon(g, item, itemX + 12, itemY + 11, 16, iconCol);

            g.setFont(isSel ? AdminTheme.FONT_BOLD_13 : AdminTheme.FONT_REG_13);
            g.setColor(isSel ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MUTED);
            g.drawString(item.label, itemX + 38, itemY + 24);

            curY += itemH + 4;
        }

        // Return to Landing Page Button
        curY += 12;
        returnBtnX = x + padX;
        returnBtnY = curY;
        returnBtnW = w - padX * 2;
        returnBtnH = 34;
        RoundRectangle2D retShape = new RoundRectangle2D.Float(returnBtnX, returnBtnY, returnBtnW, returnBtnH, 10, 10);
        g.setColor(returnBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(retShape);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(retShape);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("← Back to Landing Page", returnBtnX + 14, returnBtnY + 21);

        // Bottom Admin Profile Card
        int bottomH = 68;
        int bottomY = h - bottomH;
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, bottomY, x + w, bottomY);

        int avX = x + padX;
        int avY = bottomY + 14;
        int avSize = 36;
        g.setColor(AdminTheme.BRAND_TINT);
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("JL", avX + 10, avY + 23);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Jordan Lee", avX + 44, avY + 16);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Platform administrator", avX + 44, avY + 31);

        Draw.drawChevronDown(g, x + w - padX - 8, avY + 14, 10, AdminTheme.TEXT_LIGHT, 0f);
    }

    private void paintLogo(Graphics2D g, int x, int y) {
        int iconSize = 28;
        RoundRectangle2D logoBg = new RoundRectangle2D.Float(x, y, iconSize, iconSize, 8, 8);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.fill(logoBg);

        g.setColor(Color.WHITE);
        int d = 6;
        g.fillRoundRect(x + 6, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 6, y + 16, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 16, d, d, 2, 2);

        // Brand Text
        g.setFont(AdminTheme.FONT_TITLE);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("TeamUp", x + 36, y + 21);

        // "Admin" Pill Badge
        int badgeX = x + 108;
        int badgeY = y + 4;
        int badgeW = 46;
        int badgeH = 20;
        RoundRectangle2D pill = new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, 10, 10);
        g.setColor(new Color(0xEE, 0xF2, 0xFF));
        g.fill(pill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(new Color(0x63, 0x66, 0xF1));
        g.drawString("Admin", badgeX + 7, badgeY + 14);
    }

    private void paintNavIcon(Graphics2D g, AdminNavItem item, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f));

        switch (item) {
            case OVERVIEW:
                int s = 6;
                g.drawRoundRect(x, y, s, s, 2, 2);
                g.drawRoundRect(x + 8, y, s, s, 2, 2);
                g.drawRoundRect(x, y + 8, s, s, 2, 2);
                g.drawRoundRect(x + 8, y + 8, s, s, 2, 2);
                break;
            case USERS:
                g.drawOval(x + 3, y, 6, 6);
                g.drawArc(x, y + 8, 12, 8, 0, 180);
                g.drawOval(x + 10, y + 2, 4, 4);
                break;
            case PROJECTS:
                g.drawRoundRect(x, y + 2, size - 2, size - 4, 3, 3);
                g.drawLine(x, y + 5, x + 5, y + 5);
                break;
            case REPORTS:
                g.drawLine(x + 2, y, x + 2, y + size);
                g.drawRect(x + 2, y + 1, 10, 7);
                break;
            case SKILLS:
                g.drawOval(x + 2, y + 2, size - 4, size - 4);
                g.drawOval(x + 5, y + 5, size - 10, size - 10);
                break;
            case ACTIVITY:
                g.drawOval(x + 1, y + 1, size - 2, size - 2);
                g.drawLine(x + 8, y + 4, x + 8, y + 8);
                g.drawLine(x + 8, y + 8, x + 12, y + 8);
                break;
            case SETTINGS:
                g.drawOval(x + 3, y + 3, size - 6, size - 6);
                g.drawOval(x + 5, y + 5, size - 10, size - 10);
                break;
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        if (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH) {
            dashboard.returnToLanding();
            return true;
        }

        int sideW = 230;
        int padX = 18;
        int curY = 20 + 54;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (AdminNavItem item : AdminNavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                activeNav = item;
                dashboard.showToast("Switched to " + item.label);
                return true;
            }
            curY += itemH + 4;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p1 = returnBtnHovered;
        returnBtnHovered = (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH);
        if (returnBtnHovered != p1) changed = true;

        AdminNavItem oldHover = hoveredNav;
        hoveredNav = null;

        int sideW = 230;
        int padX = 18;
        int curY = 20 + 54;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (AdminNavItem item : AdminNavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                hoveredNav = item;
                break;
            }
            curY += itemH + 4;
        }
        if (hoveredNav != oldHover) changed = true;

        return changed || returnBtnHovered;
    }
}

