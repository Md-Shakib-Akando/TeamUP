import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard - Users Management View.
 * Displays user list, status filters, search, role management, suspend/reactivate actions.
 * Wired directly to AdminDataStore for full real-time reactivity.
 * Matches Figma reference Image 2 with exact precision.
 */
public class AdminUsers {

    public static class UserRowHitbox {
        public String name;
        public String role;
        public String status;
        public int actionX, actionY, actionW, actionH;
        public boolean viewHovered = false;
        public boolean suspendHovered = false;
        public boolean editRoleHovered = false;
    }

    private final List<UserRowHitbox> hitboxes = new ArrayList<>();
    private String searchQuery = "";
    private boolean searchFocused = false;

    // Filter dropdown bounds & state
    private final String[] roleOptions = {"All roles", "Member", "Moderator", "Admin"};
    private int roleFilterIndex = 0;
    private int roleDropX, roleDropY, roleDropW = 100, roleDropH = 34;

    private final String[] statusOptions = {"All statuses", "Active", "Suspended"};
    private int statusFilterIndex = 0;
    private int statusDropX, statusDropY, statusDropW = 110, statusDropH = 34;

    private final String[] joinedOptions = {"Joined: Any time", "Last 7 days", "Last 30 days", "This year"};
    private int joinedFilterIndex = 0;
    private int joinedDropX, joinedDropY, joinedDropW = 130, joinedDropH = 34;

    private int exportBtnX, exportBtnY, exportBtnW = 120, exportBtnH = 36;
    private boolean exportHovered = false;

    // Search input bounds
    private int searchX, searchY, searchW = 320, searchH = 34;

    // Pagination bounds & state
    private int currentPage = 1;
    private int prevBtnX, prevBtnY, prevBtnW = 72, prevBtnH = 32;
    private int p1BtnX, p1BtnY, p1BtnW = 32, p1BtnH = 32;
    private int p2BtnX, p2BtnY, p2BtnW = 32, p2BtnH = 32;
    private int p3BtnX, p3BtnY, p3BtnW = 32, p3BtnH = 32;
    private int nextBtnX, nextBtnY, nextBtnW = 54, nextBtnH = 32;

    public AdminUsers() {
    }

    public int getComputedHeight(int width) {
        return 920;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header
        paintHeader(g, x + padX, curY, contentW);
        curY += 76;

        // 2. Metrics summary pills
        paintSummaryPills(g, x + padX, curY);
        curY += 38;

        // 3. Filter Bar (Search + Dropdowns)
        paintFilterBar(g, x + padX, curY, contentW);
        curY += 50;

        // 4. Users Table
        paintUsersTable(g, x + padX, curY, contentW, dashboard);
        curY += 500;

        // 5. Pagination Bar
        paintPagination(g, x + padX, curY, contentW);
        curY += 48;

        // 6. Bottom Notice Banner
        paintNoticeBanner(g, x + padX, curY, contentW);
    }

    private void paintHeader(Graphics2D g, int x, int y, int w) {
        // Title: "Users"
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Users", x, y + 24);

        int titleW = hfm.stringWidth("Users");

        // "Sample data" Pill
        int pillX = x + titleW + 12;
        int pillY = y + 8;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, 76, 20, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(pill);
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("Sample data", pillX + 8, pillY + 14);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Manage community members, account status and platform roles.", x, y + 48);

        // Top Right: "Export users" Button
        exportBtnX = x + w - exportBtnW;
        exportBtnY = y + 8;
        RoundRectangle2D expBox = new RoundRectangle2D.Float(exportBtnX, exportBtnY, exportBtnW, exportBtnH, 8, 8);
        g.setColor(exportHovered ? new Color(0xF5, 0xF3, 0xEF) : Color.WHITE);
        g.fill(expBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(expBox);

        // Download icon
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawRect(exportBtnX + 12, exportBtnY + 12, 12, 10);
        g.drawLine(exportBtnX + 18, exportBtnY + 8, exportBtnX + 18, exportBtnY + 16);
        g.drawLine(exportBtnX + 15, exportBtnY + 13, exportBtnX + 18, exportBtnY + 16);
        g.drawLine(exportBtnX + 21, exportBtnY + 13, exportBtnX + 18, exportBtnY + 16);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Export users", exportBtnX + 32, exportBtnY + 22);
    }

    private void paintSummaryPills(Graphics2D g, int x, int y) {
        int total = AdminDataStore.getInstance().baseTotalUsers;
        int active = AdminDataStore.getInstance().getActiveUsersCount();
        int suspended = AdminDataStore.getInstance().getSuspendedUsersCount();

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(String.format("%,d total members", total), x, y + 16);

        // Active pill
        int actX = x + 150;
        RoundRectangle2D actPill = new RoundRectangle2D.Float(actX, y, 92, 24, 12, 12);
        g.setColor(new Color(0xE6, 0xF4, 0xEA));
        g.fill(actPill);
        g.setColor(new Color(0x06, 0x5F, 0x46));
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(String.format("%,d active", active), actX + 10, y + 16);

        // Suspended pill
        int susX = actX + 102;
        RoundRectangle2D susPill = new RoundRectangle2D.Float(susX, y, 98, 24, 12, 12);
        g.setColor(new Color(0xFE, 0xE2, 0xE2));
        g.fill(susPill);
        g.setColor(new Color(0x99, 0x1B, 0x1B));
        g.drawString(suspended + " suspended", susX + 10, y + 16);
    }

    private void paintFilterBar(Graphics2D g, int x, int y, int w) {
        // Search Box
        searchX = x;
        searchY = y;
        searchW = Math.min(380, w - 380);
        searchH = 34;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(sBox);

        Draw.drawSearchIcon(g, searchX + 10, searchY + 9, 16, AdminTheme.TEXT_LIGHT);
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(searchQuery.isEmpty() ? AdminTheme.TEXT_LIGHT : AdminTheme.TEXT_DARK);
        g.drawString(searchQuery.isEmpty() ? "Search by name or email..." : searchQuery, searchX + 34, searchY + 22);

        // Dropdowns on the right
        joinedDropW = 136;
        joinedDropX = x + w - joinedDropW;
        joinedDropY = y;
        paintDropdown(g, joinedDropX, joinedDropY, joinedDropW, searchH, joinedOptions[joinedFilterIndex]);

        statusDropW = 106;
        statusDropX = joinedDropX - statusDropW - 10;
        statusDropY = y;
        paintDropdown(g, statusDropX, statusDropY, statusDropW, searchH, statusOptions[statusFilterIndex]);

        roleDropW = 96;
        roleDropX = statusDropX - roleDropW - 10;
        roleDropY = y;
        paintDropdown(g, roleDropX, roleDropY, roleDropW, searchH, roleOptions[roleFilterIndex]);
    }

    private void paintDropdown(Graphics2D g, int dx, int dy, int dw, int dh, String text) {
        RoundRectangle2D box = new RoundRectangle2D.Float(dx, dy, dw, dh, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(text, dx + 12, dy + 21);

        Draw.drawChevronDown(g, dx + dw - 18, dy + 14, 8, AdminTheme.TEXT_LIGHT, 0f);
    }

    private void paintUsersTable(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        int tableH = 480;
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header
        int thY = y;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fillRect(x + 1, thY + 1, w - 2, 38);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, thY + 38, x + w, thY + 38);

        int colUser = x + 24;
        int colEmail = x + Math.round(w * 0.28f);
        int colRole = x + Math.round(w * 0.52f);
        int colJoined = x + Math.round(w * 0.63f);
        int colStatus = x + Math.round(w * 0.76f);
        int colAction = x + Math.round(w * 0.86f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("User", colUser, thY + 24);
        g.drawString("Email", colEmail, thY + 24);
        g.drawString("Role", colRole, thY + 24);
        g.drawString("Joined", colJoined, thY + 24);
        g.drawString("Status", colStatus, thY + 24);
        g.drawString("Actions", colAction, thY + 24);

        int rowH = 55;
        int curRowY = thY + 38;

        List<AdminDataStore.UserModel> userList = AdminDataStore.getInstance().filterUsers(
                searchQuery, roleOptions[roleFilterIndex], statusOptions[statusFilterIndex]
        );

        hitboxes.clear();

        int pageSize = 8;
        int fromIndex = Math.min((currentPage - 1) * pageSize, Math.max(0, userList.size() - 1));
        if (userList.isEmpty()) fromIndex = 0;
        int toIndex = Math.min(fromIndex + pageSize, userList.size());

        for (int i = fromIndex; i < toIndex; i++) {
            AdminDataStore.UserModel u = userList.get(i);
            UserRowHitbox hb = new UserRowHitbox();
            hb.name = u.name;
            hb.role = u.role;
            hb.status = u.status;

            // Avatar circle
            int avSize = 28;
            int avY = curRowY + (rowH - avSize) / 2;
            g.setColor(AdminTheme.BRAND_TINT);
            g.fillOval(colUser, avY, avSize, avSize);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString(u.initials, colUser + 6, avY + 18);

            // User Name
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(u.name, colUser + 36, curRowY + 32);

            // Email
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(u.email, colEmail, curRowY + 32);

            // Role
            if ("Admin".equalsIgnoreCase(u.role)) {
                RoundRectangle2D rolePill = new RoundRectangle2D.Float(colRole, curRowY + 18, 54, 20, 10, 10);
                g.setColor(AdminTheme.BRAND_TINT);
                g.fill(rolePill);
                g.setFont(AdminTheme.FONT_BOLD_10);
                g.setColor(AdminTheme.BRAND_PRIMARY);
                g.drawString("Admin", colRole + 8, curRowY + 32);
            } else if ("Moderator".equalsIgnoreCase(u.role)) {
                RoundRectangle2D rolePill = new RoundRectangle2D.Float(colRole, curRowY + 18, 70, 20, 10, 10);
                g.setColor(new Color(0xE0, 0xF2, 0xFE));
                g.fill(rolePill);
                g.setFont(AdminTheme.FONT_BOLD_10);
                g.setColor(new Color(0x03, 0x69, 0xA1));
                g.drawString("Moderator", colRole + 8, curRowY + 32);
            } else {
                g.setFont(AdminTheme.FONT_REG_12);
                g.setColor(AdminTheme.TEXT_MUTED);
                g.drawString(u.role, colRole, curRowY + 32);
            }

            // Joined date
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(u.joined, colJoined, curRowY + 32);

            // Status pill
            boolean isActive = "Active".equalsIgnoreCase(u.status);
            int pillW = isActive ? 52 : 72;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(colStatus, curRowY + 18, pillW, 20, 10, 10);
            g.setColor(isActive ? new Color(0xE6, 0xF4, 0xEA) : new Color(0xFE, 0xE2, 0xE2));
            g.fill(sPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(isActive ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
            g.drawString(u.status, colStatus + 8, curRowY + 32);

            // Action links: "View" · "Suspend/Reactivate" · "Edit Role"
            int ax = colAction;
            g.setFont(AdminTheme.FONT_BOLD_11);

            hb.actionX = ax;
            hb.actionY = curRowY + 12;
            hb.actionH = 26;

            // 1. View
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString("View", ax, curRowY + 32);
            ax += 36;

            // 2. Suspend / Reactivate
            String susAction = isActive ? "Suspend" : "Reactivate";
            g.setColor(isActive ? new Color(0x99, 0x1B, 0x1B) : AdminTheme.BRAND_GREEN_DARK);
            g.drawString(susAction, ax, curRowY + 32);
            FontMetrics afm = g.getFontMetrics();
            ax += afm.stringWidth(susAction) + 12;

            // 3. Edit Role
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString("Edit Role", ax, curRowY + 32);
            ax += 60;

            hb.actionW = ax - hb.actionX;
            hitboxes.add(hb);

            // Row divider line
            curRowY += rowH;
            if (i < Math.min(8, userList.size()) - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, curRowY, x + w, curRowY);
            }
        }
    }

    private void paintPagination(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        int startU = (currentPage - 1) * 8 + 1;
        int endU = Math.min(currentPage * 8, 12480);
        g.drawString("Showing " + startU + "-" + endU + " of 12,480 users", x, y + 20);

        int curX = x + w;

        // Next button
        nextBtnX = curX - nextBtnW;
        nextBtnY = y + 2;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next", false);

        // Page 3
        p3BtnX = nextBtnX - 8 - p3BtnW;
        p3BtnY = y + 2;
        paintPageBtn(g, p3BtnX, p3BtnY, p3BtnW, p3BtnH, "3", currentPage == 3);

        // Page 2
        p2BtnX = p3BtnX - 6 - p2BtnW;
        p2BtnY = y + 2;
        paintPageBtn(g, p2BtnX, p2BtnY, p2BtnW, p2BtnH, "2", currentPage == 2);

        // Page 1
        p1BtnX = p2BtnX - 6 - p1BtnW;
        p1BtnY = y + 2;
        paintPageBtn(g, p1BtnX, p1BtnY, p1BtnW, p1BtnH, "1", currentPage == 1);

        // Previous button
        prevBtnX = p1BtnX - 8 - prevBtnW;
        prevBtnY = y + 2;
        paintPageBtn(g, prevBtnX, prevBtnY, prevBtnW, prevBtnH, "Previous", false);
    }

    private void paintPageBtn(Graphics2D g, int bx, int by, int bw, int bh, String text, boolean active) {
        RoundRectangle2D b = new RoundRectangle2D.Float(bx, by, bw, bh, 6, 6);
        if (active) {
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.fill(b);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.WHITE);
            g.fill(b);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(b);
            g.setColor(AdminTheme.TEXT_DARK);
        }
        g.setFont(AdminTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + (bh - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void paintNoticeBanner(Graphics2D g, int x, int y, int w) {
        int banH = 56;
        RoundRectangle2D banner = new RoundRectangle2D.Float(x, y, w, banH, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(banner);
        g.setColor(new Color(0xFD, 0xD7, 0xE4));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(banner);

        // Checkmark circle
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawOval(x + 18, y + 18, 18, 18);
        g.drawLine(x + 22, y + 27, x + 25, y + 31);
        g.drawLine(x + 25, y + 31, x + 31, y + 23);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Role changes affect platform access", x + 46, y + 24);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Review a member's responsibilities before editing their role. Administrative actions are recorded in Activity Logs.", x + 46, y + 42);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        if (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH) {
            dashboard.showToast("Exporting user records to CSV (12,480 total)...");
            AdminDataStore.getInstance().logActivity("Jordan Lee", "JL", "Exported user list", "Users · October 2026", true);
            return true;
        }

        // Search Box Focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Role Dropdown click (Cycles through: All roles -> Member -> Moderator -> Admin)
        if (mx >= roleDropX && mx <= roleDropX + roleDropW && my >= roleDropY && my <= roleDropY + roleDropH) {
            roleFilterIndex = (roleFilterIndex + 1) % roleOptions.length;
            dashboard.showToast("Filtered by: " + roleOptions[roleFilterIndex]);
            return true;
        }

        // Status Dropdown click (Cycles through: All statuses -> Active -> Suspended)
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            statusFilterIndex = (statusFilterIndex + 1) % statusOptions.length;
            currentPage = 1;
            dashboard.showToast("Filtered by: " + statusOptions[statusFilterIndex]);
            return true;
        }

        // Joined Dropdown click
        if (mx >= joinedDropX && mx <= joinedDropX + joinedDropW && my >= joinedDropY && my <= joinedDropY + joinedDropH) {
            joinedFilterIndex = (joinedFilterIndex + 1) % joinedOptions.length;
            currentPage = 1;
            dashboard.showToast("Filtered by: " + joinedOptions[joinedFilterIndex]);
            return true;
        }

        // Pagination Clicks
        if (mx >= p1BtnX && mx <= p1BtnX + p1BtnW && my >= p1BtnY && my <= p1BtnY + p1BtnH) {
            currentPage = 1;
            dashboard.showToast("Page 1 of users");
            return true;
        }
        if (mx >= p2BtnX && mx <= p2BtnX + p2BtnW && my >= p2BtnY && my <= p2BtnY + p2BtnH) {
            currentPage = 2;
            dashboard.showToast("Page 2 of users");
            return true;
        }
        if (mx >= p3BtnX && mx <= p3BtnX + p3BtnW && my >= p3BtnY && my <= p3BtnY + p3BtnH) {
            currentPage = 3;
            dashboard.showToast("Page 3 of users");
            return true;
        }
        if (mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            currentPage = Math.min(3, currentPage + 1);
            dashboard.showToast("Page " + currentPage + " of users");
            return true;
        }
        if (mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            currentPage = Math.max(1, currentPage - 1);
            dashboard.showToast("Page " + currentPage + " of users");
            return true;
        }

        // Table actions
        for (UserRowHitbox u : hitboxes) {
            if (mx >= u.actionX && mx <= u.actionX + u.actionW && my >= u.actionY && my <= u.actionY + u.actionH) {
                int localX = mx - u.actionX;
                if (localX < 36) {
                    dashboard.showToast("Opening profile details for " + u.name);
                } else if (localX < 100) {
                    if ("Active".equalsIgnoreCase(u.status)) {
                        AdminDataStore.getInstance().suspendUser(u.name);
                        dashboard.showToast("Suspended account for " + u.name);
                    } else {
                        AdminDataStore.getInstance().reactivateUser(u.name);
                        dashboard.showToast("Reactivated account for " + u.name);
                    }
                } else {
                    dashboard.openEditRoleModal(u.name, u.role);
                }
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean pExp = exportHovered;
        exportHovered = (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH);
        if (exportHovered != pExp) changed = true;

        for (UserRowHitbox u : hitboxes) {
            boolean inRow = (mx >= u.actionX && mx <= u.actionX + u.actionW && my >= u.actionY && my <= u.actionY + u.actionH);
            if (inRow) {
                int lx = mx - u.actionX;
                u.viewHovered = lx < 36;
                u.suspendHovered = lx >= 36 && lx < 100;
                u.editRoleHovered = lx >= 100;
                changed = true;
            } else {
                if (u.viewHovered || u.suspendHovered || u.editRoleHovered) {
                    u.viewHovered = false;
                    u.suspendHovered = false;
                    u.editRoleHovered = false;
                    changed = true;
                }
            }
        }

        return changed || exportHovered;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (!searchFocused) return false;
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (searchQuery.length() > 0) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                currentPage = 1;
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            searchFocused = false;
            return true;
        } else if (ch >= 32 && ch <= 126) {
            searchQuery += ch;
            currentPage = 1;
            return true;
        }
        return false;
    }
}
