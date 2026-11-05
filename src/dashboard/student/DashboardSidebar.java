import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Handles the left sidebar for TeamUp Dashboard.
 * Includes logo, workspace badge, navigation menu items, and landing page switch.
 */
public class DashboardSidebar {

    private int returnBtnX, returnBtnY, returnBtnW, returnBtnH;
    private boolean returnBtnHovered = false;

    public void paint(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        g.setColor(DashboardTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x + w - 1, y, x + w - 1, y + h);

        int curY = y + 20;
        int padX = 20;

        // Logo + Brand Name
        paintLogo(g, x + padX, curY);
        curY += 46;

      

        // Nav Menu Items
        int itemH = 38;
        int itemW = w - padX * 2;
        StudentDashboard.NavItem[] items = StudentDashboard.NavItem.values();

        for (StudentDashboard.NavItem item : items) {
            boolean isSel = (item == dashboard.getActiveNav());
            boolean isHov = (item == dashboard.getHoveredNav());
            int itemX = x + padX;
            int itemY = curY;

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemX, itemY, itemW, itemH, 10, 10);
            if (isSel) {
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(new Color(0xF5, 0xF3, 0xEF));
                g.fill(itemShape);
            }

            Color iconCol = isSel ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED;
            paintNavIcon(g, item, itemX + 12, itemY + 11, 16, iconCol);

            g.setFont(isSel ? DashboardTheme.FONT_BOLD_13 : DashboardTheme.FONT_REG_13);
            g.setColor(isSel ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
            g.drawString(item.label, itemX + 38, itemY + 24);

            if (item.badge > 0) {
                int bw = 20;
                int bh = 18;
                int bx = itemX + itemW - bw - 10;
                int by = itemY + (itemH - bh) / 2;
                RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(bShape);
                g.setFont(DashboardTheme.FONT_BOLD_10);
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawString(String.valueOf(item.badge), bx + 7, by + 13);
            }

            curY += itemH + 2;
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
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(retShape);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("← Back to Landing Page", returnBtnX + 16, returnBtnY + 21);

        // Bottom User Profile Card
        int bottomH = 64;
        int bottomY = h - bottomH;
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.drawLine(x, bottomY, x + w, bottomY);

        int avX = x + padX;
        int avY = bottomY + 14;
        int avSize = 36;
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("AM", avX + 9, avY + 23);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(dashboard.getProfileName(), avX + 44, avY + 16);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("Student · Free plan", avX + 44, avY + 31);

        Draw.drawChevronDown(g, x + w - padX - 10, avY + 14, 10, DashboardTheme.TEXT_LIGHT, 0f);
    }

    private void paintLogo(Graphics2D g, int x, int y) {
        int iconSize = 28;
        RoundRectangle2D logoBg = new RoundRectangle2D.Float(x, y, iconSize, iconSize, 8, 8);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.fill(logoBg);

        g.setColor(Color.WHITE);
        int d = 6;
        g.fillRoundRect(x + 6, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 6, y + 16, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 16, d, d, 2, 2);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("TeamUp", x + 38, y + 21);
    }

    private void paintNavIcon(Graphics2D g, StudentDashboard.NavItem item, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (item) {
            case OVERVIEW:
                int s = 5;
                g.drawRoundRect(x, y, s, s, 2, 2);
                g.drawRoundRect(x + 9, y, s, s, 2, 2);
                g.drawRoundRect(x, y + 9, s, s, 2, 2);
                g.drawRoundRect(x + 9, y + 9, s, s, 2, 2);
                break;
            case DISCOVER_PROJECTS:
                g.drawOval(x, y, size, size);
                g.drawLine(x + 5, y + 11, x + 11, y + 5);
                break;
            case FIND_TEAMMATES:
                g.drawOval(x + 3, y, 6, 6);
                g.drawArc(x, y + 8, 12, 8, 0, 180);
                g.drawOval(x + 10, y + 2, 4, 4);
                break;
            case MY_PROJECTS:
                g.drawRoundRect(x, y + 3, size - 2, size - 5, 3, 3);
                g.drawLine(x, y + 6, x + 6, y + 6);
                break;
            case MY_TEAMS:
                g.drawOval(x + 5, y + 1, 6, 6);
                g.drawArc(x + 2, y + 9, 12, 7, 0, 180);
                break;
            case MESSAGES:
                g.drawRoundRect(x, y + 1, size - 2, size - 4, 4, 4);
                g.drawLine(x + 4, y + size - 3, x + 2, y + size);
                break;
            case MY_PROFILE:
                g.drawOval(x + 4, y + 1, 8, 8);
                g.drawArc(x + 1, y + 10, 14, 6, 0, 180);
                break;
            case SETTINGS:
                g.drawOval(x + 3, y + 3, size - 6, size - 6);
                g.drawOval(x + 6, y + 6, size - 12, size - 12);
                break;
        }
    }

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // Return to Landing Page
        if (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH) {
            dashboard.returnToLanding();
            return true;
        }

        // Sidebar Navigation click
        int sideW = 230;
        int padX = 20;
        int curY = 20 + 46;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (StudentDashboard.NavItem item : StudentDashboard.NavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                dashboard.setActiveNav(item);
                if (item == StudentDashboard.NavItem.MESSAGES) {
                    dashboard.openModal(StudentDashboard.ActiveModal.MESSAGES);
                } else if (item == StudentDashboard.NavItem.MY_PROFILE) {
                    dashboard.openModal(StudentDashboard.ActiveModal.COMPLETE_PROFILE);
                } else {
                    dashboard.showToast("Switched to " + item.label);
                }
                return true;
            }
            curY += itemH + 2;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my, StudentDashboard dashboard) {
        boolean changed = false;

        boolean p1 = returnBtnHovered;
        returnBtnHovered = (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH);
        if (returnBtnHovered != p1) changed = true;

        StudentDashboard.NavItem oldHover = dashboard.getHoveredNav();
        dashboard.setHoveredNav(null);

        int sideW = 230;
        int padX = 20;
        int curY = 20 + 46;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (StudentDashboard.NavItem item : StudentDashboard.NavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                dashboard.setHoveredNav(item);
                break;
            }
            curY += itemH + 2;
        }
        if (dashboard.getHoveredNav() != oldHover) changed = true;

        return changed || returnBtnHovered;
    }
}

