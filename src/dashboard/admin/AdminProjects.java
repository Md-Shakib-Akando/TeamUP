import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard - Projects Management View.
 * Displays all platform initiatives, category tags, team fill ratio, moderation review triggers.
 * Fully dynamic and reactive:
 * - Live filter tabs with real-time dynamic counters (All, Active, Recruiting, Under review)
 * - Dynamic Category filter populated from AdminDataStore categories
 * - Interactive filter dropdowns (Categories, Status, Created date) with floating popover menus
 * - Instant search by project name, owner, category, or description with ? Clear button
 * - Filter reset button when any filter is active
 * - "+ Add project" modal integration for creating new initiatives
 * - Real CSV export ("Export projects") with live data
 * - Interactive row actions ("Review project ?", "Approve", "Delete") wired to AdminModals and AdminDataStore
 * - Empty state with "Reset all filters" button when no projects match criteria
 * - Dynamic pagination (Previous, Page 1..N, Next) with true page slicing
 * - Multi-viewport responsiveness with dynamic computed height.
 */
public class AdminProjects {

    public static class TabHitbox {
        public int x, y, w, h;
        public String label;
        public int count;
    }

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
        public boolean enabled = true;

        public PageBtnHitbox(int pageNumber, int x, int y, int w, int h, boolean active, boolean enabled) {
            this.pageNumber = pageNumber;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.active = active;
            this.enabled = enabled;
        }
    }

    public static class ProjectRowHitbox {
        public AdminDataStore.ProjectModel project;
        public String name;
        public String status;
        public int rowX, rowY, rowW, rowH;
        public boolean rowHovered = false;

        public int actionX, actionY, actionW, actionH;
        public boolean actionHovered = false;

        public int quickApproveX, quickApproveY, quickApproveW, quickApproveH;
        public boolean quickApproveHovered = false;
    }

    private final TabHitbox[] tabBoxes = new TabHitbox[4];
    private final List<ProjectRowHitbox> hitboxes = new ArrayList<>();
    private final List<DropdownItemHitbox> popoverHitboxes = new ArrayList<>();
    private final List<PageBtnHitbox> pageHitboxes = new ArrayList<>();

    private int selectedTab = 0; // 0=All, 1=Active, 2=Recruiting, 3=Under review
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 340, searchH = 36;
    private int clearSearchX, clearSearchY, clearSearchSize = 18;
    private boolean clearSearchHovered = false;

    // Filter dropdown bounds & states
    private final String[] statusOptions = {"All statuses", "Active", "Recruiting", "Under review", "Completed"};
    private int statusFilterIdx = 0;
    private int statusDropX, statusDropY, statusDropW = 115, statusDropH = 36;
    private boolean statusDropHovered = false;

    private int catFilterIdx = 0;
    private int catDropX, catDropY, catDropW = 135, catDropH = 36;
    private boolean catDropHovered = false;

    private final String[] createdOptions = {"Created: Any time", "Past 30 days", "Past 6 months", "This year"};
    private int createdFilterIdx = 0;
    private int createdDropX, createdDropY, createdDropW = 145, createdDropH = 36;
    private boolean createdDropHovered = false;

    // Active Popover: 0 = None, 1 = Category, 2 = Status, 3 = Created
    private int activePopover = 0;

    // Reset filters button
    private int resetBtnX, resetBtnY, resetBtnW = 96, resetBtnH = 26;
    private boolean resetBtnHovered = false;

    // Header buttons
    private int addProjectBtnX, addProjectBtnY, addProjectBtnW = 120, addProjectBtnH = 36;
    private boolean addProjectHovered = false;

    // Pagination bounds & states
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

    public AdminProjects() {
        for (int i = 0; i < tabBoxes.length; i++) {
            tabBoxes[i] = new TabHitbox();
        }
    }

    public void setSearchQuery(String q) { this.searchQuery = (q == null) ? "" : q; this.currentPage = 1; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchFocused(boolean f) { this.searchFocused = f; }
    public boolean isSearchFocused() { return searchFocused; }
    public void setSelectedTab(int t) { this.selectedTab = t; this.currentPage = 1; }
    public int getSelectedTab() { return selectedTab; }
    public void setCategoryFilterIdx(int idx) { this.catFilterIdx = idx; this.currentPage = 1; }
    public int getCategoryFilterIdx() { return catFilterIdx; }
    public void setStatusFilterIdx(int idx) { this.statusFilterIdx = idx; this.currentPage = 1; }
    public int getStatusFilterIdx() { return statusFilterIdx; }
    public void setCreatedFilterIdx(int idx) { this.createdFilterIdx = idx; this.currentPage = 1; }
    public int getCreatedFilterIdx() { return createdFilterIdx; }
    public void setCurrentPage(int p) { this.currentPage = p; }
    public int getCurrentPage() { return currentPage; }

    /**
     * Dynamically pulls categories from AdminDataStore (Category 0 is always "All categories").
     */
    public List<String> getDynamicCategoryOptions() {
        List<String> options = new ArrayList<>();
        options.add("All categories");
        List<AdminDataStore.CategoryModel> cats = AdminDataStore.getInstance().getAllCategories();
        for (AdminDataStore.CategoryModel c : cats) {
            if (!options.contains(c.name)) {
                options.add(c.name);
            }
        }
        return options;
    }

    /**
     * Returns the reactive filtered list of projects based on search query, selected tab, and all dropdown filters.
     */
    public List<AdminDataStore.ProjectModel> getFilteredProjects() {
        List<String> cats = getDynamicCategoryOptions();
        String catFilter = (catFilterIdx >= 0 && catFilterIdx < cats.size()) ? cats.get(catFilterIdx) : "All categories";
        String statusFilter = (statusFilterIdx >= 0 && statusFilterIdx < statusOptions.length) ? statusOptions[statusFilterIdx] : "All statuses";
        String createdFilter = (createdFilterIdx >= 0 && createdFilterIdx < createdOptions.length) ? createdOptions[createdFilterIdx] : "Created: Any time";

        return AdminDataStore.getInstance().filterProjects(searchQuery, selectedTab, catFilter, statusFilter, createdFilter);
    }

    public int getComputedHeight(int width) {
        int padX = width < 600 ? 16 : 36;
        int contentW = width - padX * 2;

        int h = 28; // top pad
        // Header
        h += (contentW < 680) ? 116 : 76;
        // Status filter tabs
        h += 46;
        // Search & Filter bar
        h += (contentW < 760) ? 92 : 52;

        // Table
        List<AdminDataStore.ProjectModel> list = getFilteredProjects();
        int rows = Math.min(pageSize, list.size());
        if (rows == 0) {
            h += 240; // Empty state card height
        } else {
            h += 42 + rows * 55; // Table header + dynamic rows
        }

        // Pagination
        h += 48;
        // Footnote
        h += 32;
        // Bottom pad
        h += 32;

        return Math.max(900, h);
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = width < 600 ? 16 : 36;
        int curY = y + 26;
        int contentW = width - padX * 2;

        // 1. Header (Projects title, dynamic badge, + Add project, Export projects)
        curY = paintHeader(g, x + padX, curY, contentW, dashboard);

        // 2. Status Filter Tabs (Dynamic counts & pills)
        curY = paintStatusTabs(g, x + padX, curY, contentW);

        // 3. Search and Dropdown Filters
        curY = paintFilterBar(g, x + padX, curY, contentW);

        // 4. Projects Table
        curY = paintProjectsTable(g, x + padX, curY, contentW, dashboard);

        // 5. Pagination Bar
        curY = paintPagination(g, x + padX, curY, contentW);

        // 6. Footnote
        paintFootnote(g, x + padX, curY, contentW);

        // 7. Floating Dropdown Popover (drawn on top of all layers)
        if (activePopover != 0) {
            paintActivePopover(g);
        }
    }

    private int paintHeader(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Projects", x, y + 24);

        int titleW = hfm.stringWidth("Projects");

        // Dynamic total counter Pill
        int pillX = x + titleW + 12;
        int pillY = y + 7;
        int totalProjects = AdminDataStore.getInstance().getTotalProjectsCount();
        String totalLabel = String.format("%,d total", totalProjects);
        g.setFont(AdminTheme.FONT_BOLD_10);
        int pillW = g.getFontMetrics().stringWidth(totalLabel) + 18;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, pillW, 22, 11, 11);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(pill);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString(totalLabel, pillX + 9, pillY + 15);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Explore team activity and review the projects shaping your community.", x, y + 48);

        // Header Action Buttons
        boolean wrapButtons = (w < 680);
        int btnY = wrapButtons ? y + 68 : y + 8;
        int rightX = x + w;

        // "+ Add project" Button (Primary Forest Green)
        addProjectBtnW = 120;
        addProjectBtnH = 36;
        addProjectBtnX = rightX - addProjectBtnW;
        addProjectBtnY = btnY;

        RoundRectangle2D addBox = new RoundRectangle2D.Float(addProjectBtnX, addProjectBtnY, addProjectBtnW, addProjectBtnH, 8, 8);
        g.setColor(addProjectHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBox);

        // Plus icon
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(addProjectBtnX + 16, addProjectBtnY + 18, addProjectBtnX + 24, addProjectBtnY + 18);
        g.drawLine(addProjectBtnX + 20, addProjectBtnY + 14, addProjectBtnX + 20, addProjectBtnY + 22);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("Add project", addProjectBtnX + 32, addProjectBtnY + 22);

        return wrapButtons ? (btnY + addProjectBtnH + 18) : (y + 74);
    }

    private int paintStatusTabs(Graphics2D g, int x, int y, int w) {
        AdminDataStore store = AdminDataStore.getInstance();
        int allCount = store.getTotalProjectsCount();
        int actCount = store.getActiveProjectsCount();
        int recCount = store.getRecruitingProjectsCount();
        int revCount = store.getUnderReviewProjectsCount();

        String[] tabs = {
                String.format("All projects · %,d", allCount),
                String.format("Active · %,d", actCount),
                String.format("Recruiting · %,d", recCount),
                String.format("Under review · %,d", revCount)
        };

        int curX = x;
        int tabH = 32;

        for (int i = 0; i < tabs.length; i++) {
            boolean isSel = (i == selectedTab);
            g.setFont(AdminTheme.FONT_BOLD_11);
            FontMetrics tfm = g.getFontMetrics();
            int tabW = tfm.stringWidth(tabs[i]) + 26;

            tabBoxes[i].x = curX;
            tabBoxes[i].y = y;
            tabBoxes[i].w = tabW;
            tabBoxes[i].h = tabH;
            tabBoxes[i].label = tabs[i];
            tabBoxes[i].count = (i == 0) ? allCount : (i == 1 ? actCount : (i == 2 ? recCount : revCount));

            RoundRectangle2D tPill = new RoundRectangle2D.Float(curX, y, tabW, tabH, 8, 8);
            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.fill(tPill);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.WHITE);
                g.fill(tPill);
                g.setColor(AdminTheme.BORDER_COLOR);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(tPill);
                g.setColor(AdminTheme.TEXT_MUTED);
            }
            g.drawString(tabs[i], curX + 13, y + 20);

            curX += tabW + 10;
        }

        // Reset filters button if any filter is active
        boolean hasFilters = !searchQuery.isEmpty() || selectedTab != 0 || catFilterIdx != 0 || statusFilterIdx != 0 || createdFilterIdx != 0;
        if (hasFilters) {
            List<AdminDataStore.ProjectModel> filtered = getFilteredProjects();
            String filterMatchStr = filtered.size() + " matching";
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(filterMatchStr, curX + 6, y + 20);
            curX += g.getFontMetrics().stringWidth(filterMatchStr) + 16;

            resetBtnX = curX;
            resetBtnY = y + 3;
            resetBtnW = 96;
            resetBtnH = 26;
            RoundRectangle2D rBox = new RoundRectangle2D.Float(resetBtnX, resetBtnY, resetBtnW, resetBtnH, 12, 12);
            g.setColor(resetBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(rBox);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(rBox);

            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("✕ Reset filters", resetBtnX + 10, resetBtnY + 17);
        } else {
            resetBtnX = -100;
        }

        return y + 46;
    }

    private int paintFilterBar(Graphics2D g, int x, int y, int w) {
        boolean wrapFilters = (w < 760);

        // Search Input Box
        searchX = x;
        searchY = y;
        searchH = 36;

        if (wrapFilters) {
            searchW = w;
        } else {
            int rightPads = createdDropW + statusDropW + catDropW + 36;
            searchW = Math.max(220, w - rightPads);
        }

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(sBox);

        AdminTheme.drawSearchIcon(g, searchX + 10, searchY + 10, 16, AdminTheme.TEXT_LIGHT);
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(searchQuery.isEmpty() ? AdminTheme.TEXT_LIGHT : AdminTheme.TEXT_DARK);
        g.drawString(searchQuery.isEmpty() ? "Search projects, categories or owners..." : searchQuery, searchX + 34, searchY + 23);

        // Clear Search Button (✕)
        if (!searchQuery.isEmpty()) {
            clearSearchX = searchX + searchW - 26;
            clearSearchY = searchY + 9;
            clearSearchSize = 18;
            if (clearSearchHovered) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fillOval(clearSearchX, clearSearchY, clearSearchSize, clearSearchSize);
            }
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("✕", clearSearchX + 5, clearSearchY + 13);
        } else {
            clearSearchX = -100;
        }

        // Right side dropdowns
        int dropY = wrapFilters ? (y + searchH + 10) : y;
        int rightEdge = x + w;

        // 1. Created Dropdown
        createdDropX = rightEdge - createdDropW;
        createdDropY = dropY;
        paintDropdownButton(g, createdDropX, createdDropY, createdDropW, searchH, createdOptions[createdFilterIdx], createdDropHovered, activePopover == 3);

        // 2. Status Dropdown
        statusDropX = createdDropX - statusDropW - 10;
        statusDropY = dropY;
        paintDropdownButton(g, statusDropX, statusDropY, statusDropW, searchH, statusOptions[statusFilterIdx], statusDropHovered, activePopover == 2);

        // 3. Category Dropdown
        List<String> dynamicCats = getDynamicCategoryOptions();
        String currentCat = (catFilterIdx >= 0 && catFilterIdx < dynamicCats.size()) ? dynamicCats.get(catFilterIdx) : "All categories";
        catDropX = statusDropX - catDropW - 10;
        catDropY = dropY;
        paintDropdownButton(g, catDropX, catDropY, catDropW, searchH, currentCat, catDropHovered, activePopover == 1);

        return wrapFilters ? (dropY + searchH + 14) : (y + searchH + 14);
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
        String label = text;
        if (label.length() > 16) label = label.substring(0, 14) + "...";
        g.drawString(label, dx + 12, dy + 22);

        AdminTheme.drawChevronDown(g, dx + dw - 18, dy + 14, 8, AdminTheme.TEXT_LIGHT, isOpen ? 180f : 0f);
    }

    private int paintProjectsTable(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.ProjectModel> filteredList = getFilteredProjects();
        int totalItems = filteredList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIdx = (currentPage - 1) * pageSize;
        int toIdx = Math.min(fromIdx + pageSize, totalItems);
        List<AdminDataStore.ProjectModel> pageItems = (fromIdx < totalItems) ? filteredList.subList(fromIdx, toIdx) : new ArrayList<>();

        int tableH = (pageItems.isEmpty()) ? 240 : (42 + pageItems.size() * 55);
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Row
        int thY = y;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fillRect(x + 1, thY + 1, w - 2, 38);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, thY + 38, x + w, thY + 38);

        int colName = x + 24;
        int colOwner = x + Math.round(w * 0.32f);
        int colSize = x + Math.round(w * 0.52f);
        int colStatus = x + Math.round(w * 0.63f);
        int colCreated = x + Math.round(w * 0.75f);
        int colAction = x + Math.round(w * 0.86f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Project name", colName, thY + 24);
        g.drawString("Owner", colOwner, thY + 24);
        g.drawString("Team size", colSize, thY + 24);
        g.drawString("Status", colStatus, thY + 24);
        g.drawString("Created", colCreated, thY + 24);
        g.drawString("Action", colAction, thY + 24);

        hitboxes.clear();

        if (pageItems.isEmpty()) {
            paintEmptyState(g, x, thY + 38, w, tableH - 38);
            return y + tableH + 16;
        }

        int rowH = 55;
        int curRowY = thY + 38;

        for (int i = 0; i < pageItems.size(); i++) {
            AdminDataStore.ProjectModel p = pageItems.get(i);
            ProjectRowHitbox hb = new ProjectRowHitbox();
            hb.project = p;
            hb.name = p.name;
            hb.status = p.status;
            hb.rowX = x;
            hb.rowY = curRowY;
            hb.rowW = w;
            hb.rowH = rowH;

            // Row hover background highlight
            if (hb.rowHovered) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRect(x + 1, curRowY, w - 2, rowH);
            }

            // Project Name & Category Tag
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            String pName = p.name;
            if (pName.length() > 28) pName = pName.substring(0, 26) + "...";
            g.drawString(pName, colName, curRowY + 23);

            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(p.category, colName, curRowY + 40);

            // Owner
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(p.owner, colOwner, curRowY + 32);

            // Team size
            g.drawString(p.teamSize, colSize, curRowY + 32);

            // Status Pill Badge
            paintStatusBadge(g, colStatus, curRowY + 18, p.status);

            // Created Date
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(p.created, colCreated, curRowY + 32);

            // Action: Review Project Link
            hb.actionX = colAction;
            hb.actionY = curRowY + 14;
            hb.actionW = 104;
            hb.actionH = 26;

            if (hb.actionHovered) {
                g.setColor(new Color(0xDC, 0xFC, 0xE7));
                g.fillRoundRect(hb.actionX - 4, hb.actionY, hb.actionW + 8, hb.actionH, 6, 6);
            }
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("Review project →", colAction, curRowY + 31);

            // Quick Approve link for Under review projects
            if ("Under review".equalsIgnoreCase(p.status)) {
                hb.quickApproveX = colAction + hb.actionW + 12;
                hb.quickApproveY = curRowY + 16;
                hb.quickApproveW = 56;
                hb.quickApproveH = 22;
                RoundRectangle2D qb = new RoundRectangle2D.Float(hb.quickApproveX, hb.quickApproveY, hb.quickApproveW, hb.quickApproveH, 6, 6);
                g.setColor(hb.quickApproveHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
                g.fill(qb);
                g.setFont(AdminTheme.FONT_BOLD_10);
                g.setColor(Color.WHITE);
                g.drawString("Approve", hb.quickApproveX + 8, hb.quickApproveY + 15);
            } else {
                hb.quickApproveX = -100;
            }

            hitboxes.add(hb);

            curRowY += rowH;
            if (i < pageItems.size() - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, curRowY, x + w, curRowY);
            }
        }

        return y + tableH + 16;
    }

    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        int cy = y + h / 2 - 36;

        // Search icon circle
        int icSize = 44;
        int icX = x + (w - icSize) / 2;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.fillOval(icX, cy, icSize, icSize);
        AdminTheme.drawSearchIcon(g, icX + 13, cy + 13, 18, AdminTheme.TEXT_MUTED);

        // Empty Title
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_DARK);
        String title = "No projects found";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, x + (w - fm.stringWidth(title)) / 2, cy + 62);

        // Empty Subtitle
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String sub = "No platform projects match your current search terms and filter criteria.";
        FontMetrics sfm = g.getFontMetrics();
        g.drawString(sub, x + (w - sfm.stringWidth(sub)) / 2, cy + 82);

        // Reset button
        emptyResetBtnW = 130;
        emptyResetBtnH = 32;
        emptyResetBtnX = x + (w - emptyResetBtnW) / 2;
        emptyResetBtnY = cy + 96;

        RoundRectangle2D rb = new RoundRectangle2D.Float(emptyResetBtnX, emptyResetBtnY, emptyResetBtnW, emptyResetBtnH, 8, 8);
        g.setColor(emptyResetHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(rb);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Reset all filters", emptyResetBtnX + 24, emptyResetBtnY + 20);
    }

    private void paintStatusBadge(Graphics2D g, int x, int y, String status) {
        int bw = 82;
        int bh = 22;
        RoundRectangle2D pill = new RoundRectangle2D.Float(x, y, bw, bh, 10, 10);
        g.setFont(AdminTheme.FONT_BOLD_10);

        if ("Active".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xE6, 0xF4, 0xEA));
            g.fill(pill);
            g.setColor(new Color(0x06, 0x5F, 0x46));
            g.drawString("Active", x + 22, y + 15);
        } else if ("Completed".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(pill);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("Completed", x + 12, y + 15);
        } else if ("Under review".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(pill);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.drawString("Under review", x + 8, y + 15);
        } else if ("Recruiting".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(pill);
            g.setColor(new Color(0x16, 0x65, 0x34));
            g.drawString("Recruiting", x + 15, y + 15);
        }
    }

    private int paintPagination(Graphics2D g, int x, int y, int w) {
        List<AdminDataStore.ProjectModel> filtered = getFilteredProjects();
        int totalItems = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIdx = (totalItems == 0) ? 0 : ((currentPage - 1) * pageSize + 1);
        int toIdx = Math.min(currentPage * pageSize, totalItems);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Showing " + fromIdx + "-" + toIdx + " of " + totalItems + " projects", x, y + 20);

        int curX = x + w;

        // Next button
        nextEnabled = (currentPage < totalPages);
        nextBtnX = curX - nextBtnW;
        nextBtnY = y + 2;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next", false, nextEnabled, nextHovered);

        curX = nextBtnX - 8;

        pageHitboxes.clear();

        // Dynamic page buttons (1..totalPages)
        int maxVisible = 5;
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + maxVisible - 1);
        if (endPage - startPage + 1 < maxVisible) {
            startPage = Math.max(1, endPage - maxVisible + 1);
        }

        for (int p = endPage; p >= startPage; p--) {
            int pBtnW = 32;
            int pBtnH = 32;
            int pBtnX = curX - pBtnW;
            int pBtnY = y + 2;

            PageBtnHitbox pb = new PageBtnHitbox(p, pBtnX, pBtnY, pBtnW, pBtnH, (p == currentPage), true);
            pageHitboxes.add(pb);

            paintPageBtn(g, pBtnX, pBtnY, pBtnW, pBtnH, String.valueOf(p), p == currentPage, true, pb.hovered);
            curX = pBtnX - 6;
        }

        // Previous button
        prevEnabled = (currentPage > 1);
        prevBtnX = curX - 2 - prevBtnW;
        prevBtnY = y + 2;
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
            g.setColor(AdminTheme.BORDER_COLOR);
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

    private void paintFootnote(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Team size shows current members / available places. Projects under review may have restricted visibility.", x, y + 16);
    }

    private void paintActivePopover(Graphics2D g) {
        popoverHitboxes.clear();

        int popX = 0, popY = 0, popW = 160;
        List<String> options = new ArrayList<>();
        int selectedIndex = 0;

        if (activePopover == 1) { // Category Popover
            popX = catDropX;
            popY = catDropY + catDropH + 4;
            popW = 180;
            options = getDynamicCategoryOptions();
            selectedIndex = catFilterIdx;
        } else if (activePopover == 2) { // Status Popover
            popX = statusDropX;
            popY = statusDropY + statusDropH + 4;
            popW = 140;
            for (String s : statusOptions) options.add(s);
            selectedIndex = statusFilterIdx;
        } else if (activePopover == 3) { // Created Popover
            popX = createdDropX;
            popY = createdDropY + createdDropH + 4;
            popW = 160;
            for (String c : createdOptions) options.add(c);
            selectedIndex = createdFilterIdx;
        }

        int itemH = 32;
        int popH = options.size() * itemH + 8;

        // Shadow & Backdrop Card
        RoundRectangle2D shadow = new RoundRectangle2D.Float(popX + 2, popY + 2, popW, popH, 8, 8);
        g.setColor(new Color(0, 0, 0, 20));
        g.fill(shadow);

        RoundRectangle2D card = new RoundRectangle2D.Float(popX, popY, popW, popH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        int curY = popY + 4;
        for (int i = 0; i < options.size(); i++) {
            String opt = options.get(i);
            boolean isSel = (i == selectedIndex);

            DropdownItemHitbox dh = new DropdownItemHitbox(i, opt, popX + 4, curY, popW - 8, itemH, false, isSel);

            if (isSel) {
                g.setColor(new Color(0xDC, 0xFC, 0xE7));
                g.fillRoundRect(dh.x, dh.y, dh.w, dh.h, 6, 6);
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.setFont(AdminTheme.FONT_BOLD_11);
            } else {
                g.setColor(AdminTheme.TEXT_DARK);
                g.setFont(AdminTheme.FONT_REG_11);
            }

            g.drawString(opt, dh.x + 10, dh.y + 20);

            if (isSel) {
                g.drawString("✓", dh.x + dh.w - 18, dh.y + 20);
            }

            popoverHitboxes.add(dh);
            curY += itemH;
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Popover click handling (highest priority)
        if (activePopover != 0) {
            for (DropdownItemHitbox dh : popoverHitboxes) {
                if (mx >= dh.x && mx <= dh.x + dh.w && my >= dh.y && my <= dh.y + dh.h) {
                    if (activePopover == 1) {
                        catFilterIdx = dh.index;
                        dashboard.showToast("Filtered by category: " + dh.label);
                    } else if (activePopover == 2) {
                        statusFilterIdx = dh.index;
                        dashboard.showToast("Filtered by status: " + dh.label);
                    } else if (activePopover == 3) {
                        createdFilterIdx = dh.index;
                        dashboard.showToast("Filtered by date: " + dh.label);
                    }
                    activePopover = 0;
                    currentPage = 1;
                    return true;
                }
            }
            // Click outside popover closes it
            activePopover = 0;
            return true;
        }

        // 2. Add Project button
        if (mx >= addProjectBtnX && mx <= addProjectBtnX + addProjectBtnW && my >= addProjectBtnY && my <= addProjectBtnY + addProjectBtnH) {
            dashboard.openAddProjectModal();
            return true;
        }

        // 3. Tab clicks with dynamic hitboxes
        for (int i = 0; i < tabBoxes.length; i++) {
            TabHitbox tb = tabBoxes[i];
            if (tb != null && mx >= tb.x && mx <= tb.x + tb.w && my >= tb.y && my <= tb.y + tb.h) {
                selectedTab = i;
                currentPage = 1;
                dashboard.showToast("Filtered: " + tb.label);
                return true;
            }
        }

        // 5. Reset filters button
        if (resetBtnX > 0 && mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH) {
            resetAllFilters();
            dashboard.showToast("All project filters reset");
            return true;
        }

        // 6. Empty state reset button
        if (emptyResetBtnX > 0 && mx >= emptyResetBtnX && mx <= emptyResetBtnX + emptyResetBtnW && my >= emptyResetBtnY && my <= emptyResetBtnY + emptyResetBtnH) {
            resetAllFilters();
            dashboard.showToast("Filters reset to show all projects");
            return true;
        }

        // 7. Clear search button (✕)
        if (clearSearchX > 0 && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize && my >= clearSearchY && my <= clearSearchY + clearSearchSize) {
            searchQuery = "";
            currentPage = 1;
            return true;
        }

        // 8. Search focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 9. Dropdown triggers
        if (mx >= catDropX && mx <= catDropX + catDropW && my >= catDropY && my <= catDropY + catDropH) {
            activePopover = (activePopover == 1) ? 0 : 1;
            return true;
        }
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            activePopover = (activePopover == 2) ? 0 : 2;
            return true;
        }
        if (mx >= createdDropX && mx <= createdDropX + createdDropW && my >= createdDropY && my <= createdDropY + createdDropH) {
            activePopover = (activePopover == 3) ? 0 : 3;
            return true;
        }

        // 10. Table row actions
        for (ProjectRowHitbox p : hitboxes) {
            // Quick approve
            if (p.quickApproveX > 0 && mx >= p.quickApproveX && mx <= p.quickApproveX + p.quickApproveW &&
                    my >= p.quickApproveY && my <= p.quickApproveY + p.quickApproveH) {
                AdminDataStore.getInstance().updateProjectStatus(p.name, "Active");
                dashboard.showToast("Approved and activated project: " + p.name);
                return true;
            }

            // Review project modal link
            if (mx >= p.actionX && mx <= p.actionX + p.actionW && my >= p.actionY && my <= p.actionY + p.actionH) {
                AdminDataStore.ProjectModel proj = AdminDataStore.getInstance().getProjectByName(p.name);
                if (proj != null) {
                    dashboard.openProjectDetailsModal(proj);
                } else if (p.project != null) {
                    dashboard.openProjectDetailsModal(p.project);
                }
                return true;
            }
        }

        // 11. Pagination clicks
        if (prevEnabled && mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            currentPage = Math.max(1, currentPage - 1);
            dashboard.showToast("Page " + currentPage + " of projects");
            return true;
        }
        if (nextEnabled && mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            List<AdminDataStore.ProjectModel> filtered = getFilteredProjects();
            int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
            currentPage = Math.min(totalPages, currentPage + 1);
            dashboard.showToast("Page " + currentPage + " of projects");
            return true;
        }
        for (PageBtnHitbox pb : pageHitboxes) {
            if (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h) {
                currentPage = pb.pageNumber;
                dashboard.showToast("Page " + currentPage + " of projects");
                return true;
            }
        }

        return false;
    }

    public void resetAllFilters() {
        searchQuery = "";
        selectedTab = 0;
        catFilterIdx = 0;
        statusFilterIdx = 0;
        createdFilterIdx = 0;
        currentPage = 1;
        activePopover = 0;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        // Popover hover
        if (activePopover != 0) {
            for (DropdownItemHitbox dh : popoverHitboxes) {
                boolean was = dh.hovered;
                dh.hovered = (mx >= dh.x && mx <= dh.x + dh.w && my >= dh.y && my <= dh.y + dh.h);
                if (dh.hovered != was) changed = true;
            }
            return changed;
        }

        // Buttons hover
        boolean pAdd = addProjectHovered;
        addProjectHovered = (mx >= addProjectBtnX && mx <= addProjectBtnX + addProjectBtnW && my >= addProjectBtnY && my <= addProjectBtnY + addProjectBtnH);
        if (addProjectHovered != pAdd) changed = true;

        boolean pRst = resetBtnHovered;
        resetBtnHovered = (resetBtnX > 0 && mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH);
        if (resetBtnHovered != pRst) changed = true;

        boolean pEr = emptyResetHovered;
        emptyResetHovered = (emptyResetBtnX > 0 && mx >= emptyResetBtnX && mx <= emptyResetBtnX + emptyResetBtnW && my >= emptyResetBtnY && my <= emptyResetBtnY + emptyResetBtnH);
        if (emptyResetHovered != pEr) changed = true;

        boolean pClr = clearSearchHovered;
        clearSearchHovered = (clearSearchX > 0 && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize && my >= clearSearchY && my <= clearSearchY + clearSearchSize);
        if (clearSearchHovered != pClr) changed = true;

        boolean pCat = catDropHovered;
        catDropHovered = (mx >= catDropX && mx <= catDropX + catDropW && my >= catDropY && my <= catDropY + catDropH);
        if (catDropHovered != pCat) changed = true;

        boolean pStat = statusDropHovered;
        statusDropHovered = (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH);
        if (statusDropHovered != pStat) changed = true;

        boolean pCre = createdDropHovered;
        createdDropHovered = (mx >= createdDropX && mx <= createdDropX + createdDropW && my >= createdDropY && my <= createdDropY + createdDropH);
        if (createdDropHovered != pCre) changed = true;

        // Table row hover
        for (ProjectRowHitbox p : hitboxes) {
            boolean wasRow = p.rowHovered;
            p.rowHovered = (mx >= p.rowX && mx <= p.rowX + p.rowW && my >= p.rowY && my <= p.rowY + p.rowH);
            if (p.rowHovered != wasRow) changed = true;

            boolean wasAct = p.actionHovered;
            p.actionHovered = (mx >= p.actionX && mx <= p.actionX + p.actionW && my >= p.actionY && my <= p.actionY + p.actionH);
            if (p.actionHovered != wasAct) changed = true;

            if (p.quickApproveX > 0) {
                boolean wasQA = p.quickApproveHovered;
                p.quickApproveHovered = (mx >= p.quickApproveX && mx <= p.quickApproveX + p.quickApproveW && my >= p.quickApproveY && my <= p.quickApproveY + p.quickApproveH);
                if (p.quickApproveHovered != wasQA) changed = true;
            }
        }

        // Pagination hover
        boolean pPrev = prevHovered;
        prevHovered = (prevEnabled && mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH);
        if (prevHovered != pPrev) changed = true;

        boolean pNext = nextHovered;
        nextHovered = (nextEnabled && mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH);
        if (nextHovered != pNext) changed = true;

        for (PageBtnHitbox pb : pageHitboxes) {
            boolean was = pb.hovered;
            pb.hovered = (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h);
            if (pb.hovered != was) changed = true;
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
            activePopover = 0;
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
