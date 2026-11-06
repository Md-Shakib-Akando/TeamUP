import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard - Users Management View.
 * Displays user list, interactive search, multi-criteria filter dropdowns,
 * full responsive table layout, dynamic pagination, and live store actions.
 * Wired directly to AdminDataStore for full real-time reactivity.
 */
public class AdminUsers {

    public static class DropdownItemHitbox {
        public int index;
        public String label;
        public int x, y, w, h;
        public boolean hovered = false;
        public boolean selected = false;

        public DropdownItemHitbox(int index, String label, int x, int y, int w, int h, boolean hovered, boolean selected) {
            this.index = index;
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.hovered = hovered;
            this.selected = selected;
        }
    }

    public static class PageBtnHitbox {
        public int pageNumber;
        public int x, y, w, h;
        public boolean active = false;
        public boolean hovered = false;

        public PageBtnHitbox(int pageNumber, int x, int y, int w, int h, boolean active) {
            this.pageNumber = pageNumber;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.active = active;
        }
    }

    public static class UserRowHitbox {
        public AdminDataStore.UserModel user;
        public int rowX, rowY, rowW, rowH;
        public boolean rowHovered = false;

        public int viewBtnX, viewBtnY, viewBtnW, viewBtnH;
        public boolean viewHovered = false;

        public int suspendBtnX, suspendBtnY, suspendBtnW, suspendBtnH;
        public boolean suspendHovered = false;

        public int editRoleBtnX, editRoleBtnY, editRoleBtnW, editRoleBtnH;
        public boolean editRoleHovered = false;

        public int deleteBtnX, deleteBtnY, deleteBtnW, deleteBtnH;
        public boolean deleteHovered = false;
    }

    private final List<UserRowHitbox> hitboxes = new ArrayList<UserRowHitbox>();
    private final List<DropdownItemHitbox> popoverHitboxes = new ArrayList<DropdownItemHitbox>();
    private final List<PageBtnHitbox> pageHitboxes = new ArrayList<PageBtnHitbox>();

    // Search state
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 320, searchH = 36;
    private int clearSearchX, clearSearchY, clearSearchSize = 20;

    // Filter dropdown bounds & state
    private final String[] roleOptions = {"All roles", "Member", "Moderator", "Admin"};
    private int roleFilterIndex = 0;
    private int roleDropX, roleDropY, roleDropW = 105, roleDropH = 36;
    private boolean roleDropHovered = false;

    private final String[] statusOptions = {"All statuses", "Active", "Suspended"};
    private int statusFilterIndex = 0;
    private int statusDropX, statusDropY, statusDropW = 115, statusDropH = 36;
    private boolean statusDropHovered = false;

    private final String[] joinedOptions = {"Joined: Any time", "Last 7 days", "Last 30 days", "This year"};
    private int joinedFilterIndex = 0;
    private int joinedDropX, joinedDropY, joinedDropW = 140, joinedDropH = 36;
    private boolean joinedDropHovered = false;

    // Active open popover: 0 = None, 1 = Role, 2 = Status, 3 = Joined
    private int activePopover = 0;

    // Reset filters button
    private int resetBtnX, resetBtnY, resetBtnW = 92, resetBtnH = 26;
    private boolean resetBtnHovered = false;

    // Header buttons
    private int addUserBtnX, addUserBtnY, addUserBtnW = 112, addUserBtnH = 36;
    private boolean addUserHovered = false;

    private int exportBtnX, exportBtnY, exportBtnW = 124, exportBtnH = 36;
    private boolean exportHovered = false;

    // Pagination bounds & state
    private int currentPage = 1;
    private final int pageSize = 8;
    private int prevBtnX, prevBtnY, prevBtnW = 76, prevBtnH = 32;
    private boolean prevHovered = false;
    private boolean prevEnabled = false;

    private int nextBtnX, nextBtnY, nextBtnW = 58, nextBtnH = 32;
    private boolean nextHovered = false;
    private boolean nextEnabled = false;

    // Empty state reset button
    private int emptyResetBtnX, emptyResetBtnY, emptyResetBtnW = 130, emptyResetBtnH = 34;
    private boolean emptyResetHovered = false;

    public AdminUsers() {
    }

    public int getComputedHeight(int width) {
        int padX = width < 600 ? 16 : 36;
        int contentW = width - padX * 2;

        int h = 32; // top pad
        // Header
        h += (contentW < 680) ? 116 : 74;
        // Summary pills
        h += (contentW < 560) ? 58 : 38;
        // Filter bar
        h += (contentW < 760) ? 92 : 52;
        // Users Table
        List<AdminDataStore.UserModel> list = getFilteredUsers();
        int rows = Math.min(pageSize, list.size());
        if (rows == 0) {
            h += 240; // empty state height
        } else {
            h += 42 + rows * 55; // table header + rows
        }
        // Pagination
        h += 52;
        // Notice Banner
        h += 68;
        // Bottom pad
        h += 40;

        return Math.max(900, h);
    }

    private List<AdminDataStore.UserModel> getFilteredUsers() {
        return AdminDataStore.getInstance().filterUsers(
                searchQuery,
                roleOptions[roleFilterIndex],
                statusOptions[statusFilterIndex],
                joinedOptions[joinedFilterIndex]
        );
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = width < 600 ? 16 : 36;
        int curY = y + 26;
        int contentW = width - padX * 2;

        // 1. Header (Users title + Add user + Export users)
        curY = paintHeader(g, x + padX, curY, contentW, dashboard);

        // 2. Metrics summary pills
        curY = paintSummaryPills(g, x + padX, curY, contentW);

        // 3. Filter Bar (Search + Dropdowns)
        curY = paintFilterBar(g, x + padX, curY, contentW);

        // 4. Users Table
        curY = paintUsersTable(g, x + padX, curY, contentW, dashboard);

        // 5. Pagination Bar
        curY = paintPagination(g, x + padX, curY, contentW);

        // 6. Bottom Notice Banner
        paintNoticeBanner(g, x + padX, curY, contentW);

        // 7. Floating Dropdown Popover (drawn on top of all layers)
        if (activePopover != 0) {
            paintActivePopover(g);
        }
    }

    private int paintHeader(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        // Title: "Users"
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Users", x, y + 24);

        int titleW = hfm.stringWidth("Users");

        // "Directory" Pill
        int pillX = x + titleW + 12;
        int pillY = y + 7;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, 78, 22, 11, 11);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.fill(pill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Directory", pillX + 13, pillY + 15);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Manage community members, account status and platform roles.", x, y + 48);

        // Header Action Buttons
        boolean wrapButtons = (w < 680);
        int btnY = wrapButtons ? y + 68 : y + 8;
        int rightX = wrapButtons ? (x + w) : (x + w);

        // "Export users" Button (Secondary Outline)
        exportBtnW = 120;
        exportBtnH = 36;
        exportBtnX = rightX - exportBtnW;
        exportBtnY = btnY;

        RoundRectangle2D expBox = new RoundRectangle2D.Float(exportBtnX, exportBtnY, exportBtnW, exportBtnH, 8, 8);
        g.setColor(exportHovered ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(expBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(expBox);

        // Download icon
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawRect(exportBtnX + 12, exportBtnY + 13, 11, 9);
        g.drawLine(exportBtnX + 17, exportBtnY + 9, exportBtnX + 17, exportBtnY + 16);
        g.drawLine(exportBtnX + 14, exportBtnY + 13, exportBtnX + 17, exportBtnY + 16);
        g.drawLine(exportBtnX + 20, exportBtnY + 13, exportBtnX + 17, exportBtnY + 16);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Export users", exportBtnX + 31, exportBtnY + 22);

        // "+ Add user" Button (Primary Forest Green)
        addUserBtnW = 110;
        addUserBtnH = 36;
        addUserBtnX = exportBtnX - addUserBtnW - 10;
        addUserBtnY = btnY;

        RoundRectangle2D addBox = new RoundRectangle2D.Float(addUserBtnX, addUserBtnY, addUserBtnW, addUserBtnH, 8, 8);
        g.setColor(addUserHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBox);

        // Plus icon
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(addUserBtnX + 16, addUserBtnY + 18, addUserBtnX + 24, addUserBtnY + 18);
        g.drawLine(addUserBtnX + 20, addUserBtnY + 14, addUserBtnX + 20, addUserBtnY + 22);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("Add user", addUserBtnX + 32, addUserBtnY + 22);

        return wrapButtons ? (btnY + addUserBtnH + 18) : (y + 74);
    }

    private int paintSummaryPills(Graphics2D g, int x, int y, int w) {
        int total = AdminDataStore.getInstance().baseTotalUsers;
        int active = AdminDataStore.getInstance().getActiveUsersCount();
        int suspended = AdminDataStore.getInstance().getSuspendedUsersCount();
        List<AdminDataStore.UserModel> filtered = getFilteredUsers();

        int curX = x;

        // Total members text
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        String totStr = String.format("%,d total members", total);
        g.drawString(totStr, curX, y + 16);
        FontMetrics fm = g.getFontMetrics();
        curX += fm.stringWidth(totStr) + 16;

        // Active pill
        int actW = 94;
        RoundRectangle2D actPill = new RoundRectangle2D.Float(curX, y, actW, 24, 12, 12);
        g.setColor(new Color(0xE6, 0xF4, 0xEA));
        g.fill(actPill);
        // Green dot
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.fillOval(curX + 10, y + 8, 8, 8);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(new Color(0x06, 0x5F, 0x46));
        g.drawString(String.format("%,d active", active), curX + 24, y + 16);
        curX += actW + 10;

        // Suspended pill
        int susW = 100;
        RoundRectangle2D susPill = new RoundRectangle2D.Float(curX, y, susW, 24, 12, 12);
        g.setColor(new Color(0xFE, 0xE2, 0xE2));
        g.fill(susPill);
        // Red dot
        g.setColor(new Color(0xEF, 0x44, 0x44));
        g.fillOval(curX + 10, y + 8, 8, 8);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(new Color(0x99, 0x1B, 0x1B));
        g.drawString(suspended + " suspended", curX + 24, y + 16);
        curX += susW + 12;

        // Filter status / Reset button
        boolean hasFilters = !searchQuery.isEmpty() || roleFilterIndex != 0 || statusFilterIndex != 0 || joinedFilterIndex != 0;
        if (hasFilters) {
            String filterMatchStr = filtered.size() + " matching";
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(filterMatchStr, curX, y + 16);
            curX += g.getFontMetrics().stringWidth(filterMatchStr) + 12;

            resetBtnX = curX;
            resetBtnY = y;
            resetBtnW = 88;
            resetBtnH = 24;
            RoundRectangle2D rBox = new RoundRectangle2D.Float(resetBtnX, resetBtnY, resetBtnW, resetBtnH, 12, 12);
            g.setColor(resetBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(rBox);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(rBox);

            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("✕ Reset filters", resetBtnX + 10, resetBtnY + 16);
        } else {
            resetBtnX = -100;
        }

        return y + 36;
    }

    private int paintFilterBar(Graphics2D g, int x, int y, int w) {
        boolean wrapFilters = (w < 760);

        // Search Input
        searchX = x;
        searchY = y;
        searchH = 36;

        if (wrapFilters) {
            searchW = w;
        } else {
            int rightPads = joinedDropW + statusDropW + roleDropW + 36;
            searchW = Math.max(220, w - rightPads);
        }

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(sBox);

        Draw.drawSearchIcon(g, searchX + 12, searchY + 10, 16, AdminTheme.TEXT_LIGHT);
        g.setFont(AdminTheme.FONT_REG_12);

        if (searchQuery.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Search by name or email...", searchX + 36, searchY + 23);
        } else {
            g.setColor(AdminTheme.TEXT_DARK);
            String cursor = (searchFocused && (System.currentTimeMillis() % 1000 < 500)) ? "|" : "";
            g.drawString(searchQuery + cursor, searchX + 36, searchY + 23);

            // Clear search button (✕)
            clearSearchSize = 18;
            clearSearchX = searchX + searchW - clearSearchSize - 10;
            clearSearchY = searchY + (searchH - clearSearchSize) / 2;
            g.setColor(new Color(0x94, 0xA3, 0xB8));
            g.fillOval(clearSearchX, clearSearchY, clearSearchSize, clearSearchSize);
            g.setColor(Color.WHITE);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.drawString("✕", clearSearchX + 5, clearSearchY + 13);
        }

        // Dropdowns row
        int dropY = wrapFilters ? (searchY + searchH + 10) : y;
        int rightAnchor = x + w;

        // Joined Dropdown
        joinedDropW = 140;
        joinedDropH = 36;
        joinedDropX = rightAnchor - joinedDropW;
        joinedDropY = dropY;
        paintDropdownButton(g, joinedDropX, joinedDropY, joinedDropW, joinedDropH, joinedOptions[joinedFilterIndex], joinedDropHovered, activePopover == 3);

        // Status Dropdown
        statusDropW = 115;
        statusDropH = 36;
        statusDropX = joinedDropX - statusDropW - 10;
        statusDropY = dropY;
        paintDropdownButton(g, statusDropX, statusDropY, statusDropW, statusDropH, statusOptions[statusFilterIndex], statusDropHovered, activePopover == 2);

        // Role Dropdown
        roleDropW = 105;
        roleDropH = 36;
        roleDropX = statusDropX - roleDropW - 10;
        roleDropY = dropY;
        paintDropdownButton(g, roleDropX, roleDropY, roleDropW, roleDropH, roleOptions[roleFilterIndex], roleDropHovered, activePopover == 1);

        return wrapFilters ? (dropY + joinedDropH + 16) : (y + searchH + 16);
    }

    private void paintDropdownButton(Graphics2D g, int dx, int dy, int dw, int dh, String text, boolean hovered, boolean isOpen) {
        RoundRectangle2D box = new RoundRectangle2D.Float(dx, dy, dw, dh, 8, 8);
        g.setColor(hovered || isOpen ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(box);
        g.setColor(isOpen ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(text, dx + 12, dy + 22);

        Draw.drawChevronDown(g, dx + dw - 18, dy + 14, 8, AdminTheme.TEXT_LIGHT, isOpen ? 180f : 0f);
    }

    private int paintUsersTable(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.UserModel> userList = getFilteredUsers();
        hitboxes.clear();

        // Calculate pagination bounds
        int totalFiltered = userList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalFiltered / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalFiltered);
        int rowsCount = Math.max(0, toIndex - fromIndex);

        int tableH = (rowsCount == 0) ? 230 : (42 + rowsCount * 55);

        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Row
        int thY = y;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fillRect(x + 1, thY + 1, w - 2, 40);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, thY + 41, x + w, thY + 41);

        // Responsive Column Positions
        boolean isCompact = (w < 720);
        int colUser = x + 20;
        int colEmail = x + (int) (w * (isCompact ? 0.35f : 0.28f));
        int colRole = x + (int) (w * (isCompact ? 0.65f : 0.50f));
        int colJoined = isCompact ? -1 : x + (int) (w * 0.63f);
        int colStatus = x + (int) (w * (isCompact ? 0.80f : 0.74f));
        int colAction = x + (int) (w * (isCompact ? 0.88f : 0.84f));

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("User", colUser, thY + 25);
        g.drawString("Email", colEmail, thY + 25);
        g.drawString("Role", colRole, thY + 25);
        if (!isCompact) {
            g.drawString("Joined", colJoined, thY + 25);
        }
        g.drawString("Status", colStatus, thY + 25);
        g.drawString("Actions", colAction, thY + 25);

        // Empty State Check
        if (rowsCount == 0) {
            paintEmptyState(g, x, thY + 42, w, tableH - 42);
            return y + tableH + 16;
        }

        int rowH = 55;
        int curRowY = thY + 42;

        for (int i = fromIndex; i < toIndex; i++) {
            AdminDataStore.UserModel u = userList.get(i);
            UserRowHitbox hb = new UserRowHitbox();
            hb.user = u;
            hb.rowX = x;
            hb.rowY = curRowY;
            hb.rowW = w;
            hb.rowH = rowH;

            // Hover row highlight
            if (hb.rowHovered) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRect(x + 1, curRowY, w - 2, rowH);
            }

            // Avatar circle with initials
            int avSize = 28;
            int avY = curRowY + (rowH - avSize) / 2;
            g.setColor(new Color(0xE2, 0xEE, 0xE7));
            g.fillOval(colUser, avY, avSize, avSize);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            FontMetrics afm = g.getFontMetrics();
            int initX = colUser + (avSize - afm.stringWidth(u.initials)) / 2;
            g.drawString(u.initials, initX, avY + 18);

            // User Name
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            int maxNameW = Math.max(60, colEmail - colUser - 44);
            g.drawString(truncateWithEllipsis(g, u.name, maxNameW), colUser + 36, curRowY + 32);

            // User Email
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            int maxEmailW = Math.max(60, colRole - colEmail - 16);
            g.drawString(truncateWithEllipsis(g, u.email, maxEmailW), colEmail, curRowY + 32);

            // Role Badge
            if ("Admin".equalsIgnoreCase(u.role)) {
                RoundRectangle2D rolePill = new RoundRectangle2D.Float(colRole, curRowY + 18, 54, 20, 10, 10);
                g.setColor(new Color(0xE6, 0xF4, 0xEA));
                g.fill(rolePill);
                g.setFont(AdminTheme.FONT_BOLD_10);
                g.setColor(new Color(0x06, 0x5F, 0x46));
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

            // Joined date (omitted on compact)
            if (!isCompact && colJoined > 0) {
                g.setFont(AdminTheme.FONT_REG_11);
                g.setColor(AdminTheme.TEXT_LIGHT);
                g.drawString(u.joined, colJoined, curRowY + 32);
            }

            // Status pill
            boolean isActive = "Active".equalsIgnoreCase(u.status);
            int pillW = isActive ? 52 : 72;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(colStatus, curRowY + 18, pillW, 20, 10, 10);
            g.setColor(isActive ? new Color(0xE6, 0xF4, 0xEA) : new Color(0xFE, 0xE2, 0xE2));
            g.fill(sPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(isActive ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
            g.drawString(u.status, colStatus + 8, curRowY + 32);

            // Action links: "View" · "Suspend/Reactivate" · "Edit" · "Delete"
            int ax = colAction;
            g.setFont(AdminTheme.FONT_BOLD_11);

            // 1. View Button
            hb.viewBtnX = ax;
            hb.viewBtnY = curRowY + 14;
            hb.viewBtnW = 32;
            hb.viewBtnH = 24;
            g.setColor(hb.viewHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.TEXT_DARK);
            g.drawString("View", ax, curRowY + 32);
            ax += hb.viewBtnW + 8;

            // 2. Suspend / Reactivate Button
            String susAction = isActive ? "Suspend" : "Reactivate";
            FontMetrics safm = g.getFontMetrics();
            int susW = safm.stringWidth(susAction);
            hb.suspendBtnX = ax;
            hb.suspendBtnY = curRowY + 14;
            hb.suspendBtnW = susW;
            hb.suspendBtnH = 24;
            g.setColor(isActive ? (hb.suspendHovered ? new Color(0x7F, 0x1D, 0x1D) : new Color(0xDC, 0x26, 0x26))
                                : (hb.suspendHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK));
            g.drawString(susAction, ax, curRowY + 32);
            ax += susW + 10;

            // 3. Edit Role Button
            hb.editRoleBtnX = ax;
            hb.editRoleBtnY = curRowY + 14;
            hb.editRoleBtnW = 28;
            hb.editRoleBtnH = 24;
            g.setColor(hb.editRoleHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.TEXT_DARK);
            g.drawString("Edit", ax, curRowY + 32);
            ax += hb.editRoleBtnW + 8;

            // 4. Delete Action
            hb.deleteBtnX = ax;
            hb.deleteBtnY = curRowY + 14;
            hb.deleteBtnW = 22;
            hb.deleteBtnH = 24;
            g.setColor(hb.deleteHovered ? new Color(0x99, 0x1B, 0x1B) : new Color(0xEF, 0x44, 0x44));
            g.drawString("✕", ax + 4, curRowY + 31);

            hitboxes.add(hb);

            // Row divider line
            curRowY += rowH;
            if (i < toIndex - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, curRowY, x + w, curRowY);
            }
        }

        return y + tableH + 16;
    }

    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        int midX = x + w / 2;
        int midY = y + h / 2 - 20;

        // Search icon circle
        int iconCircle = 44;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.fillOval(midX - iconCircle / 2, midY - 24, iconCircle, iconCircle);
        Draw.drawSearchIcon(g, midX - 9, midY - 15, 18, AdminTheme.TEXT_LIGHT);

        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_DARK);
        String msg = searchQuery.isEmpty() ? "No users matching active filters" : "No users found matching \"" + searchQuery + "\"";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, midX - fm.stringWidth(msg) / 2, midY + 34);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String sub = "Try searching for a different name, email, or reset your filters.";
        FontMetrics sfm = g.getFontMetrics();
        g.drawString(sub, midX - sfm.stringWidth(sub) / 2, midY + 52);

        // Reset button
        emptyResetBtnW = 124;
        emptyResetBtnH = 32;
        emptyResetBtnX = midX - emptyResetBtnW / 2;
        emptyResetBtnY = midY + 68;

        RoundRectangle2D rBox = new RoundRectangle2D.Float(emptyResetBtnX, emptyResetBtnY, emptyResetBtnW, emptyResetBtnH, 8, 8);
        g.setColor(emptyResetHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(rBox);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        FontMetrics rfm = g.getFontMetrics();
        g.drawString("Reset All Filters", emptyResetBtnX + (emptyResetBtnW - rfm.stringWidth("Reset All Filters")) / 2, emptyResetBtnY + 20);
    }

    private int paintPagination(Graphics2D g, int x, int y, int w) {
        List<AdminDataStore.UserModel> userList = getFilteredUsers();
        int totalFiltered = userList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalFiltered / pageSize));

        int startU = totalFiltered == 0 ? 0 : (currentPage - 1) * pageSize + 1;
        int endU = Math.min(currentPage * pageSize, totalFiltered);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Showing " + startU + "-" + endU + " of " + String.format("%,d", totalFiltered) + " users", x, y + 21);

        int curX = x + w;

        // Next button
        nextBtnW = 58;
        nextBtnH = 32;
        nextBtnX = curX - nextBtnW;
        nextBtnY = y + 2;
        nextEnabled = (currentPage < totalPages);
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next", false, nextEnabled, nextHovered);

        curX = nextBtnX - 8;

        // Dynamic page numbers (shows up to 4 page numbers around currentPage)
        pageHitboxes.clear();
        int startP = Math.max(1, Math.min(currentPage - 1, totalPages - 3));
        int endP = Math.min(totalPages, startP + 3);

        for (int p = endP; p >= startP; p--) {
            int pBtnW = 32;
            int pBtnH = 32;
            int pBtnX = curX - pBtnW;
            int pBtnY = y + 2;
            boolean isActive = (p == currentPage);

            PageBtnHitbox ph = new PageBtnHitbox(p, pBtnX, pBtnY, pBtnW, pBtnH, isActive);
            pageHitboxes.add(ph);

            paintPageBtn(g, pBtnX, pBtnY, pBtnW, pBtnH, String.valueOf(p), isActive, true, ph.hovered);
            curX = pBtnX - 6;
        }

        // Previous button
        prevBtnW = 76;
        prevBtnH = 32;
        prevBtnX = curX - 2 - prevBtnW;
        prevBtnY = y + 2;
        prevEnabled = (currentPage > 1);
        paintPageBtn(g, prevBtnX, prevBtnY, prevBtnW, prevBtnH, "Previous", false, prevEnabled, prevHovered);

        return y + 48;
    }

    private void paintPageBtn(Graphics2D g, int bx, int by, int bw, int bh, String text, boolean active, boolean enabled, boolean hovered) {
        RoundRectangle2D b = new RoundRectangle2D.Float(bx, by, bw, bh, 6, 6);
        if (active) {
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.fill(b);
            g.setColor(Color.WHITE);
        } else if (!enabled) {
            g.setColor(new Color(0xF8, 0xFA, 0xFC));
            g.fill(b);
            g.setColor(new Color(0xEA, 0xEE, 0xF4));
            g.setStroke(new BasicStroke(1.0f));
            g.draw(b);
            g.setColor(new Color(0x94, 0xA3, 0xB8));
        } else {
            g.setColor(hovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
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
        g.setColor(new Color(0xF0, 0xFD, 0xF4));
        g.fill(banner);
        g.setColor(new Color(0xBB, 0xF7, 0xD0));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(banner);

        // Checkmark badge
        g.setColor(new Color(0x16, 0xA3, 0x4A));
        g.fillOval(x + 18, y + 18, 20, 20);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.0f));
        g.drawLine(x + 23, y + 28, x + 26, y + 31);
        g.drawLine(x + 26, y + 31, x + 32, y + 24);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(new Color(0x14, 0x53, 0x2D));
        g.drawString("Role changes affect platform access immediately", x + 48, y + 25);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(new Color(0x16, 0x65, 0x34));
        g.drawString("Review member permissions before editing roles. All suspensions and role upgrades are recorded in Activity Logs.", x + 48, y + 43);
    }

    private void paintActivePopover(Graphics2D g) {
        String[] opts;
        int activeIdx;
        int popX, popY, popW;

        if (activePopover == 1) {
            opts = roleOptions;
            activeIdx = roleFilterIndex;
            popX = roleDropX;
            popY = roleDropY + roleDropH + 4;
            popW = Math.max(120, roleDropW);
        } else if (activePopover == 2) {
            opts = statusOptions;
            activeIdx = statusFilterIndex;
            popX = statusDropX;
            popY = statusDropY + statusDropH + 4;
            popW = Math.max(130, statusDropW);
        } else if (activePopover == 3) {
            opts = joinedOptions;
            activeIdx = joinedFilterIndex;
            popX = joinedDropX;
            popY = joinedDropY + joinedDropH + 4;
            popW = Math.max(150, joinedDropW);
        } else {
            return;
        }

        int itemH = 34;
        int popH = opts.length * itemH + 8;

        // Shadow
        g.setColor(new Color(0, 0, 0, 18));
        g.fillRoundRect(popX - 2, popY, popW + 4, popH + 4, 10, 10);

        // Popover Box
        RoundRectangle2D pBox = new RoundRectangle2D.Float(popX, popY, popW, popH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(pBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(pBox);

        popoverHitboxes.clear();
        int itemY = popY + 4;

        for (int i = 0; i < opts.length; i++) {
            boolean isSel = (i == activeIdx);
            DropdownItemHitbox dh = new DropdownItemHitbox(i, opts[i], popX + 4, itemY, popW - 8, itemH, false, isSel);

            if (dh.hovered) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fillRoundRect(dh.x, dh.y, dh.w, dh.h, 6, 6);
            }

            g.setFont(isSel ? AdminTheme.FONT_BOLD_11 : AdminTheme.FONT_REG_11);
            g.setColor(isSel ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
            g.drawString(opts[i], dh.x + 10, dh.y + 21);

            // Checkmark if selected
            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.setFont(AdminTheme.FONT_BOLD_12);
                g.drawString("✓", dh.x + dh.w - 18, dh.y + 22);
            }

            popoverHitboxes.add(dh);
            itemY += itemH;
        }
    }

    private String truncateWithEllipsis(Graphics2D g, String text, int maxW) {
        if (text == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) return text;
        String ell = "...";
        int ellW = fm.stringWidth(ell);
        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) + ellW > maxW) {
            len--;
        }
        return text.substring(0, Math.max(0, len)) + ell;
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Popover click handling (if open)
        if (activePopover != 0) {
            for (DropdownItemHitbox d : popoverHitboxes) {
                if (mx >= d.x && mx <= d.x + d.w && my >= d.y && my <= d.y + d.h) {
                    if (activePopover == 1) {
                        roleFilterIndex = d.index;
                        dashboard.showToast("Filter role: " + roleOptions[roleFilterIndex]);
                    } else if (activePopover == 2) {
                        statusFilterIndex = d.index;
                        dashboard.showToast("Filter status: " + statusOptions[statusFilterIndex]);
                    } else if (activePopover == 3) {
                        joinedFilterIndex = d.index;
                        dashboard.showToast("Filter joined: " + joinedOptions[joinedFilterIndex]);
                    }
                    currentPage = 1;
                    activePopover = 0;
                    return true;
                }
            }
            // Click outside popover closes it
            activePopover = 0;
            return true;
        }

        // 2. Add User Button
        if (mx >= addUserBtnX && mx <= addUserBtnX + addUserBtnW && my >= addUserBtnY && my <= addUserBtnY + addUserBtnH) {
            dashboard.openAddUserModal();
            return true;
        }

        // 3. Export Users Button
        if (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH) {
            String path = AdminDataStore.getInstance().exportUsersToCsv();
            int total = AdminDataStore.getInstance().getAllUsers().size();
            dashboard.showToast("Exported " + total + " users to " + path);
            return true;
        }

        // 4. Reset Filters Buttons
        if (mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH) {
            resetAllFilters(dashboard);
            return true;
        }
        if (mx >= emptyResetBtnX && mx <= emptyResetBtnX + emptyResetBtnW && my >= emptyResetBtnY && my <= emptyResetBtnY + emptyResetBtnH) {
            resetAllFilters(dashboard);
            return true;
        }

        // 5. Search Box & Clear
        if (!searchQuery.isEmpty() && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize && my >= clearSearchY && my <= clearSearchY + clearSearchSize) {
            searchQuery = "";
            currentPage = 1;
            dashboard.showToast("Search cleared");
            return true;
        }
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 6. Dropdown Popover Toggles
        if (mx >= roleDropX && mx <= roleDropX + roleDropW && my >= roleDropY && my <= roleDropY + roleDropH) {
            activePopover = (activePopover == 1) ? 0 : 1;
            return true;
        }
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            activePopover = (activePopover == 2) ? 0 : 2;
            return true;
        }
        if (mx >= joinedDropX && mx <= joinedDropX + joinedDropW && my >= joinedDropY && my <= joinedDropY + joinedDropH) {
            activePopover = (activePopover == 3) ? 0 : 3;
            return true;
        }

        // 7. Table Row Actions
        for (UserRowHitbox u : hitboxes) {
            if (mx >= u.viewBtnX && mx <= u.viewBtnX + u.viewBtnW && my >= u.viewBtnY && my <= u.viewBtnY + u.viewBtnH) {
                dashboard.openUserDetailsModal(u.user);
                return true;
            }
            if (mx >= u.suspendBtnX && mx <= u.suspendBtnX + u.suspendBtnW && my >= u.suspendBtnY && my <= u.suspendBtnY + u.suspendBtnH) {
                if ("Active".equalsIgnoreCase(u.user.status)) {
                    AdminDataStore.getInstance().suspendUser(u.user.name);
                    dashboard.showToast("Account suspended for " + u.user.name);
                } else {
                    AdminDataStore.getInstance().reactivateUser(u.user.name);
                    dashboard.showToast("Account reactivated for " + u.user.name);
                }
                return true;
            }
            if (mx >= u.editRoleBtnX && mx <= u.editRoleBtnX + u.editRoleBtnW && my >= u.editRoleBtnY && my <= u.editRoleBtnY + u.editRoleBtnH) {
                dashboard.openEditRoleModal(u.user.name, u.user.role);
                return true;
            }
            if (mx >= u.deleteBtnX && mx <= u.deleteBtnX + u.deleteBtnW && my >= u.deleteBtnY && my <= u.deleteBtnY + u.deleteBtnH) {
                String dName = u.user.name;
                AdminDataStore.getInstance().deleteUser(dName);
                dashboard.showToast("Deleted user record: " + dName);
                return true;
            }
        }

        // 8. Pagination Clicks
        for (PageBtnHitbox p : pageHitboxes) {
            if (mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + p.h) {
                currentPage = p.pageNumber;
                dashboard.showToast("Page " + currentPage);
                return true;
            }
        }
        if (prevEnabled && mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            currentPage = Math.max(1, currentPage - 1);
            dashboard.showToast("Page " + currentPage);
            return true;
        }
        if (nextEnabled && mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            currentPage++;
            dashboard.showToast("Page " + currentPage);
            return true;
        }

        return false;
    }

    private void resetAllFilters(AdminDashboard dashboard) {
        searchQuery = "";
        roleFilterIndex = 0;
        statusFilterIndex = 0;
        joinedFilterIndex = 0;
        currentPage = 1;
        activePopover = 0;
        dashboard.showToast("All filters reset");
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        // Popover hover
        if (activePopover != 0) {
            for (DropdownItemHitbox d : popoverHitboxes) {
                boolean h = (mx >= d.x && mx <= d.x + d.w && my >= d.y && my <= d.y + d.h);
                if (d.hovered != h) {
                    d.hovered = h;
                    changed = true;
                }
            }
            return changed;
        }

        // Header buttons hover
        boolean pAdd = addUserHovered;
        addUserHovered = (mx >= addUserBtnX && mx <= addUserBtnX + addUserBtnW && my >= addUserBtnY && my <= addUserBtnY + addUserBtnH);
        if (pAdd != addUserHovered) changed = true;

        boolean pExp = exportHovered;
        exportHovered = (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH);
        if (pExp != exportHovered) changed = true;

        // Reset button hover
        boolean pRes = resetBtnHovered;
        resetBtnHovered = (mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH);
        if (pRes != resetBtnHovered) changed = true;

        boolean pEmptyRes = emptyResetHovered;
        emptyResetHovered = (mx >= emptyResetBtnX && mx <= emptyResetBtnX + emptyResetBtnW && my >= emptyResetBtnY && my <= emptyResetBtnY + emptyResetBtnH);
        if (pEmptyRes != emptyResetHovered) changed = true;

        // Dropdown buttons hover
        boolean pR = roleDropHovered;
        roleDropHovered = (mx >= roleDropX && mx <= roleDropX + roleDropW && my >= roleDropY && my <= roleDropY + roleDropH);
        if (pR != roleDropHovered) changed = true;

        boolean pS = statusDropHovered;
        statusDropHovered = (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH);
        if (pS != statusDropHovered) changed = true;

        boolean pJ = joinedDropHovered;
        joinedDropHovered = (mx >= joinedDropX && mx <= joinedDropX + joinedDropW && my >= joinedDropY && my <= joinedDropY + joinedDropH);
        if (pJ != joinedDropHovered) changed = true;

        // Row and Action buttons hover
        for (UserRowHitbox u : hitboxes) {
            boolean rH = (mx >= u.rowX && mx <= u.rowX + u.rowW && my >= u.rowY && my <= u.rowY + u.rowH);
            if (u.rowHovered != rH) {
                u.rowHovered = rH;
                changed = true;
            }

            boolean vH = (mx >= u.viewBtnX && mx <= u.viewBtnX + u.viewBtnW && my >= u.viewBtnY && my <= u.viewBtnY + u.viewBtnH);
            if (u.viewHovered != vH) {
                u.viewHovered = vH;
                changed = true;
            }

            boolean sH = (mx >= u.suspendBtnX && mx <= u.suspendBtnX + u.suspendBtnW && my >= u.suspendBtnY && my <= u.suspendBtnY + u.suspendBtnH);
            if (u.suspendHovered != sH) {
                u.suspendHovered = sH;
                changed = true;
            }

            boolean eH = (mx >= u.editRoleBtnX && mx <= u.editRoleBtnX + u.editRoleBtnW && my >= u.editRoleBtnY && my <= u.editRoleBtnY + u.editRoleBtnH);
            if (u.editRoleHovered != eH) {
                u.editRoleHovered = eH;
                changed = true;
            }

            boolean dH = (mx >= u.deleteBtnX && mx <= u.deleteBtnX + u.deleteBtnW && my >= u.deleteBtnY && my <= u.deleteBtnY + u.deleteBtnH);
            if (u.deleteHovered != dH) {
                u.deleteHovered = dH;
                changed = true;
            }
        }

        // Pagination buttons hover
        boolean prevH = (mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH);
        if (prevHovered != prevH) {
            prevHovered = prevH;
            changed = true;
        }

        boolean nextH = (mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH);
        if (nextHovered != nextH) {
            nextHovered = nextH;
            changed = true;
        }

        for (PageBtnHitbox p : pageHitboxes) {
            boolean ph = (mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + p.h);
            if (p.hovered != ph) {
                p.hovered = ph;
                changed = true;
            }
        }

        return changed;
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
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
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
