import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Dashboard - Reports Moderation Center.
 * 60/40 Split View: Left interactive reports table + Right rich inspection panel with working actions.
 * Features:
 * 1. Fully interactive dropdown popovers for Severity, Content Type, and Status filters.
 * 2. Real-time search with clear ('✕') button and empty-state filter reset.
 * 3. Dynamic KPI summary pills showing live counts with click-to-filter shortcuts.
 * 4. Interactive report rows with hover highlights and instant right-panel inspection.
 * 5. Multi-line note input with blinking cursor.
 * 6. Four functional moderation actions: Dismiss, Warn User, Remove Content, and Escalate.
 */
public class AdminReports {

    public static class ReportHitbox {
        public int x, y, w, h;
        public String id;
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

    private final List<ReportHitbox> rowHitboxes = new ArrayList<>();
    private final List<DropdownItemHitbox> popoverHitboxes = new ArrayList<>();
    private String selectedReportId = "R-1048";
    private String hoveredReportId = null;

    private String noteInput = "";
    private boolean noteFocused = false;
    private int noteBoxX, noteBoxY, noteBoxW, noteBoxH = 34;

    // Search input
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 320, searchH = 34;
    private int searchClearX = -9999, searchClearY = -9999, searchClearSize = 18;
    private boolean searchClearHovered = false;

    // Filter Dropdowns
    private final String[] severityOptions = {"All severities", "High", "Medium", "Low"};
    private int sevFilterIdx = 0;
    private int sevDropX, sevDropY, sevDropW = 120, sevDropH = 34;
    private boolean sevDropHovered = false;

    private final String[] typeOptions = {"All content types", "Message", "Project", "Profile", "Comment"};
    private int typeFilterIdx = 0;
    private int typeDropX, typeDropY, typeDropW = 140, typeDropH = 34;
    private boolean typeDropHovered = false;

    private final String[] statusOptions = {"All statuses", "Pending", "In review", "Resolved"};
    private int statusFilterIdx = 0;
    private int statusDropX, statusDropY, statusDropW = 114, statusDropH = 34;
    private boolean statusDropHovered = false;

    // 0 = none, 1 = severity, 2 = content type, 3 = status
    private int activePopover = 0;

    // KPI Summary Pills (clickable filter shortcuts)
    private int pendingPillX, pendingPillY, pendingPillW, pendingPillH;
    private int highPillX, highPillY, highPillW, highPillH;
    private int resolvedPillX, resolvedPillY, resolvedPillW, resolvedPillH;
    private boolean pendingHovered = false;
    private boolean highHovered = false;
    private boolean resolvedHovered = false;

    // Empty state Reset Filters button
    private int resetBtnX = -9999, resetBtnY = -9999, resetBtnW = 104, resetBtnH = 30;
    private boolean resetBtnHovered = false;

    // Action buttons bounds in Right Panel
    private int dismissBtnX, dismissBtnY, dismissBtnW = 90, dismissBtnH = 34;
    private int warnBtnX, warnBtnY, warnBtnW = 96, warnBtnH = 34;
    private int removeBtnX, removeBtnY, removeBtnW = 124, removeBtnH = 36;
    private int escalateBtnX, escalateBtnY, escalateBtnW = 90, escalateBtnH = 36;

    private boolean dismissHovered = false;
    private boolean warnHovered = false;
    private boolean removeHovered = false;
    private boolean escalateHovered = false;

    public AdminReports() {
    }

    public int getComputedHeight(int width) {
        List<AdminDataStore.ReportModel> reports = getFilteredReports();
        int tableH = Math.max(430, 38 + reports.size() * 68 + 8);
        return Math.max(960, 28 + 76 + 38 + 48 + Math.max(tableH + 80, 580) + 40);
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header
        paintHeader(g, x + padX, curY, contentW);
        curY += 76;

        // 2. Dynamic Status Summary Pills
        paintSummaryPills(g, x + padX, curY);
        curY += 38;

        // 3. Filter Bar (Search + 3 Dropdown Buttons)
        paintFilterBar(g, x + padX, curY, contentW);
        curY += 48;

        // 4. Split Layout: Left Table (60%) + Right Detail Panel (40%)
        int splitGap = 20;
        int leftW = Math.round(contentW * 0.60f) - splitGap / 2;
        int rightW = contentW - leftW - splitGap;
        int panelH = 580;

        paintReportsTable(g, x + padX, curY, leftW, panelH);
        paintDetailPanel(g, x + padX + leftW + splitGap, curY, rightW, panelH, dashboard);

        // 5. Floating Dropdown Popover (Painted LAST for Top Z-Index overlay)
        if (activePopover != 0) {
            paintActivePopover(g);
        }
    }

    private void paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Reports", x, y + 24);

        int titleW = hfm.stringWidth("Reports");

        int pillX = x + titleW + 12;
        int pillY = y + 8;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, 76, 20, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(pill);
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("Live Queue", pillX + 9, pillY + 14);

        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Investigate user reports, review flagged content, and resolve moderation actions in real-time.", x, y + 48);
    }

    private void paintSummaryPills(Graphics2D g, int x, int y) {
        List<AdminDataStore.ReportModel> all = AdminDataStore.getInstance().getAllReports();
        long pendingCount = all.stream().filter(r -> "Pending".equalsIgnoreCase(r.status) || "In review".equalsIgnoreCase(r.status)).count();
        long highCount = all.stream().filter(r -> "High".equalsIgnoreCase(r.severity) && !"Resolved".equalsIgnoreCase(r.status)).count();
        long resolvedCount = all.stream().filter(r -> "Resolved".equalsIgnoreCase(r.status)).count();

        // 1. Pending Pill (Click to filter pending)
        pendingPillX = x;
        pendingPillY = y;
        pendingPillW = 130;
        pendingPillH = 26;
        boolean isPendingActive = (statusFilterIdx == 1);
        RoundRectangle2D pPill = new RoundRectangle2D.Float(pendingPillX, pendingPillY, pendingPillW, pendingPillH, 13, 13);
        g.setColor(pendingHovered || isPendingActive ? new Color(0xFD, 0xE6, 0x8A) : new Color(0xFE, 0xF3, 0xC7));
        g.fill(pPill);
        g.setColor(isPendingActive ? new Color(0x92, 0x40, 0x0E) : new Color(0xB4, 0x53, 0x09));
        g.setStroke(new BasicStroke(isPendingActive ? 1.5f : 1.0f));
        g.draw(pPill);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(pendingCount + " pending reports", pendingPillX + 10, pendingPillY + 17);

        // 2. High Priority Pill (Click to filter High severity)
        highPillX = x + pendingPillW + 10;
        highPillY = y;
        highPillW = 114;
        highPillH = 26;
        boolean isHighActive = (sevFilterIdx == 1);
        RoundRectangle2D hPill = new RoundRectangle2D.Float(highPillX, highPillY, highPillW, highPillH, 13, 13);
        g.setColor(highHovered || isHighActive ? new Color(0xFE, 0xCD, 0xCD) : new Color(0xFE, 0xE2, 0xE2));
        g.fill(hPill);
        g.setColor(isHighActive ? new Color(0x99, 0x1B, 0x1B) : new Color(0xDC, 0x26, 0x26));
        g.setStroke(new BasicStroke(isHighActive ? 1.5f : 1.0f));
        g.draw(hPill);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(highCount + " high priority", highPillX + 10, highPillY + 17);

        // 3. Resolved Pill (Click to filter Resolved)
        resolvedPillX = highPillX + highPillW + 10;
        resolvedPillY = y;
        resolvedPillW = 100;
        resolvedPillH = 26;
        boolean isResolvedActive = (statusFilterIdx == 3);
        RoundRectangle2D rPill = new RoundRectangle2D.Float(resolvedPillX, resolvedPillY, resolvedPillW, resolvedPillH, 13, 13);
        g.setColor(resolvedHovered || isResolvedActive ? new Color(0xC6, 0xF6, 0xD5) : new Color(0xE6, 0xF4, 0xEA));
        g.fill(rPill);
        g.setColor(isResolvedActive ? new Color(0x06, 0x4E, 0x3B) : new Color(0x06, 0x5F, 0x46));
        g.setStroke(new BasicStroke(isResolvedActive ? 1.5f : 1.0f));
        g.draw(rPill);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(resolvedCount + " resolved", resolvedPillX + 10, resolvedPillY + 17);

        // Subtitle hint
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Click any status pill to filter quickly", resolvedPillX + resolvedPillW + 14, y + 17);
    }

    private void paintFilterBar(Graphics2D g, int x, int y, int w) {
        // Search Box
        searchX = x;
        searchY = y;
        searchW = Math.min(320, w - 420);
        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1.2f));
        g.draw(sBox);

        AdminTheme.drawSearchIcon(g, searchX + 10, searchY + 9, 16, searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_LIGHT);
        g.setFont(AdminTheme.FONT_REG_12);
        if (searchQuery.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Search report ID, title, or user...", searchX + 34, searchY + 22);
            searchClearX = -9999;
            searchClearY = -9999;
        } else {
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(searchQuery, searchX + 34, searchY + 22);

            // Clear '✕' button
            searchClearSize = 18;
            searchClearX = searchX + searchW - searchClearSize - 8;
            searchClearY = searchY + (searchH - searchClearSize) / 2;
            g.setColor(searchClearHovered ? new Color(0xE2, 0xE8, 0xF0) : new Color(0xF1, 0xF5, 0xF9));
            g.fillOval(searchClearX, searchClearY, searchClearSize, searchClearSize);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(searchClearHovered ? AdminTheme.TEXT_DARK : AdminTheme.TEXT_MUTED);
            FontMetrics xfm = g.getFontMetrics();
            g.drawString("✕", searchClearX + (searchClearSize - xfm.stringWidth("✕")) / 2, searchClearY + 13);
        }

        // Dropdown 3: Status
        statusDropW = 114;
        statusDropX = x + w - statusDropW;
        statusDropY = y;
        paintDropdownButton(g, statusDropX, y, statusDropW, searchH, statusOptions[statusFilterIdx], statusDropHovered, activePopover == 3);

        // Dropdown 2: Content Type
        typeDropW = 140;
        typeDropX = statusDropX - typeDropW - 10;
        typeDropY = y;
        paintDropdownButton(g, typeDropX, y, typeDropW, searchH, typeOptions[typeFilterIdx], typeDropHovered, activePopover == 2);

        // Dropdown 1: Severity
        sevDropW = 120;
        sevDropX = typeDropX - sevDropW - 10;
        sevDropY = y;
        paintDropdownButton(g, sevDropX, y, sevDropW, searchH, severityOptions[sevFilterIdx], sevDropHovered, activePopover == 1);
    }

    private void paintDropdownButton(Graphics2D g, int dx, int dy, int dw, int dh, String text, boolean hovered, boolean isOpen) {
        RoundRectangle2D box = new RoundRectangle2D.Float(dx, dy, dw, dh, 8, 8);
        g.setColor(hovered || isOpen ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(box);
        g.setColor(isOpen ? AdminTheme.BRAND_GREEN_DARK : (hovered ? AdminTheme.TEXT_MUTED : AdminTheme.BORDER_COLOR));
        g.setStroke(new BasicStroke(isOpen ? 1.5f : 1.2f));
        g.draw(box);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(isOpen ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
        g.drawString(text, dx + 12, dy + 21);

        AdminTheme.drawChevronDown(g, dx + dw - 18, dy + 14, 8, isOpen ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_LIGHT, isOpen ? 180f : 0f);
    }

    private void paintActivePopover(Graphics2D g) {
        String[] opts;
        int activeIdx;
        int popX, popY, popW;

        if (activePopover == 1) {
            opts = severityOptions;
            activeIdx = sevFilterIdx;
            popX = sevDropX;
            popY = sevDropY + sevDropH + 4;
            popW = Math.max(140, sevDropW + 20);
        } else if (activePopover == 2) {
            opts = typeOptions;
            activeIdx = typeFilterIdx;
            popX = typeDropX;
            popY = typeDropY + typeDropH + 4;
            popW = Math.max(160, typeDropW + 20);
        } else if (activePopover == 3) {
            opts = statusOptions;
            activeIdx = statusFilterIdx;
            popX = statusDropX;
            popY = statusDropY + statusDropH + 4;
            popW = Math.max(130, statusDropW + 20);
        } else {
            return;
        }

        int itemH = 34;
        int popH = opts.length * itemH + 8;

        // Shadow
        g.setColor(new Color(0, 0, 0, 22));
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
            popoverHitboxes.add(dh);

            if (dh.hovered) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fillRoundRect(dh.x, dh.y, dh.w, dh.h, 6, 6);
            }

            g.setFont(isSel ? AdminTheme.FONT_BOLD_11 : AdminTheme.FONT_REG_11);
            g.setColor(isSel ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
            g.drawString(opts[i], dh.x + 10, dh.y + 21);

            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.setFont(AdminTheme.FONT_BOLD_12);
                g.drawString("✓", dh.x + dh.w - 18, dh.y + 21);
            }

            itemY += itemH;
        }
    }

    private void paintReportsTable(Graphics2D g, int x, int y, int w, int h) {
        List<AdminDataStore.ReportModel> reports = getFilteredReports();
        int rowH = 68;
        int tableH = Math.max(380, 42 + Math.max(1, reports.size()) * rowH);

        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Table Header
        int thY = y;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fillRect(x + 1, thY + 1, w - 2, 38);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, thY + 38, x + w, thY + 38);

        int colRep = x + 20;
        int colSev = x + Math.round(w * 0.44f);
        int colDate = x + Math.round(w * 0.60f);
        int colStat = x + Math.round(w * 0.83f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("REPORT / CONTENT", colRep, thY + 24);
        g.drawString("SEVERITY", colSev, thY + 24);
        g.drawString("REPORTER / DATE", colDate, thY + 24);
        g.drawString("STATUS", colStat, thY + 24);

        int curRowY = thY + 38;
        rowHitboxes.clear();

        if (reports.isEmpty()) {
            resetBtnX = x + 20;
            resetBtnY = curRowY + 70;
            resetBtnW = 104;
            resetBtnH = 30;

            g.setFont(AdminTheme.FONT_BOLD_13);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString("No reports found", x + 20, curRowY + 36);

            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No reports match your current filters or search query.", x + 20, curRowY + 56);

            RoundRectangle2D rBtn = new RoundRectangle2D.Float(resetBtnX, resetBtnY, resetBtnW, resetBtnH, 6, 6);
            g.setColor(resetBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(rBtn);
            g.setColor(resetBtnHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(rBtn);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(resetBtnHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
            g.drawString("Reset filters", resetBtnX + 16, resetBtnY + 19);
        } else {
            resetBtnX = -9999;
            resetBtnY = -9999;

            for (int i = 0; i < reports.size(); i++) {
                AdminDataStore.ReportModel r = reports.get(i);
                boolean isSel = r.id.equalsIgnoreCase(selectedReportId);
                boolean isHovered = r.id.equalsIgnoreCase(hoveredReportId);

                ReportHitbox hb = new ReportHitbox();
                hb.x = x;
                hb.y = curRowY;
                hb.w = w;
                hb.h = rowH;
                hb.id = r.id;
                rowHitboxes.add(hb);

                if (isSel) {
                    g.setColor(AdminTheme.BRAND_TINT);
                    g.fillRect(x + 1, curRowY, w - 2, rowH);
                    g.setColor(AdminTheme.BRAND_PRIMARY);
                    g.fillRect(x + 1, curRowY, 3, rowH);
                } else if (isHovered) {
                    g.setColor(new Color(0xF8, 0xFA, 0xFC));
                    g.fillRect(x + 1, curRowY, w - 2, rowH);
                }

                // Report ID & Type
                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.drawString(r.id + " · " + r.type, colRep, curRowY + 20);

                // Title
                g.setFont(AdminTheme.FONT_BOLD_12);
                g.setColor(AdminTheme.TEXT_DARK);
                g.drawString(r.title, colRep, curRowY + 38);

                // Category
                g.setFont(AdminTheme.FONT_REG_11);
                g.setColor(AdminTheme.TEXT_LIGHT);
                g.drawString(r.category, colRep, curRowY + 54);

                // Severity Badge
                paintSeverityBadge(g, colSev, curRowY + 24, r.severity);

                // Reporter & Date
                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(AdminTheme.TEXT_DARK);
                g.drawString(r.reporter, colDate, curRowY + 26);
                g.setFont(AdminTheme.FONT_REG_11);
                g.setColor(AdminTheme.TEXT_LIGHT);
                g.drawString(r.date, colDate, curRowY + 44);

                // Status Badge
                paintStatusBadge(g, colStat, curRowY + 22, r.status);

                curRowY += rowH;
                if (i < reports.size() - 1) {
                    g.setColor(new Color(0xF1, 0xF5, 0xF9));
                    g.drawLine(x, curRowY, x + w, curRowY);
                }
            }
        }

        // Table Footer: Showing X of Y reports
        int footY = y + tableH + 16;
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String footText = "Showing " + reports.size() + " of " + AdminDataStore.getInstance().getAllReports().size() + " reports";
        g.drawString(footText, x + 4, footY + 4);

        // Selected Status Banner below table
        paintSelectedBanner(g, x, footY + 16, w);
    }

    private List<AdminDataStore.ReportModel> getFilteredReports() {
        String q = searchQuery.trim().toLowerCase();
        return AdminDataStore.getInstance().getAllReports().stream().filter(r -> {
            boolean matchQ = q.isEmpty()
                    || r.id.toLowerCase().contains(q)
                    || r.title.toLowerCase().contains(q)
                    || r.reporter.toLowerCase().contains(q)
                    || r.flaggedAuthor.toLowerCase().contains(q)
                    || r.category.toLowerCase().contains(q);
            boolean matchSev = (sevFilterIdx == 0) || r.severity.equalsIgnoreCase(severityOptions[sevFilterIdx]);
            boolean matchType = (typeFilterIdx == 0) || r.type.equalsIgnoreCase(typeOptions[typeFilterIdx]);
            boolean matchStat = (statusFilterIdx == 0) || r.status.equalsIgnoreCase(statusOptions[statusFilterIdx]);
            return matchQ && matchSev && matchType && matchStat;
        }).collect(Collectors.toList());
    }

    private void paintSeverityBadge(Graphics2D g, int x, int y, String severity) {
        int bw = 62;
        int bh = 22;
        RoundRectangle2D pill = new RoundRectangle2D.Float(x, y, bw, bh, 6, 6);
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics fm = g.getFontMetrics();

        if ("High".equalsIgnoreCase(severity)) {
            g.setColor(new Color(0xFE, 0xE2, 0xE2));
            g.fill(pill);
            g.setColor(new Color(0xDC, 0x26, 0x26));
            g.drawString("High", x + (bw - fm.stringWidth("High")) / 2, y + 15);
        } else if ("Medium".equalsIgnoreCase(severity)) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(pill);
            g.setColor(new Color(0xD9, 0x77, 0x06));
            g.drawString("Medium", x + (bw - fm.stringWidth("Medium")) / 2, y + 15);
        } else {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(pill);
            g.setColor(new Color(0x64, 0x74, 0x8B));
            g.drawString("Low", x + (bw - fm.stringWidth("Low")) / 2, y + 15);
        }
    }

    private void paintStatusBadge(Graphics2D g, int x, int y, String status) {
        int bw = 68;
        int bh = 22;
        RoundRectangle2D pill = new RoundRectangle2D.Float(x, y, bw, bh, 10, 10);
        g.setFont(AdminTheme.FONT_BOLD_10);

        if ("Pending".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(pill);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.drawString("Pending", x + 13, y + 15);
        } else if ("In review".equalsIgnoreCase(status)) {
            g.setColor(new Color(0xE0, 0xF2, 0xFE));
            g.fill(pill);
            g.setColor(new Color(0x03, 0x69, 0xA1));
            g.drawString("In review", x + 10, y + 15);
        } else {
            g.setColor(new Color(0xE6, 0xF4, 0xEA));
            g.fill(pill);
            g.setColor(new Color(0x06, 0x5F, 0x46));
            g.drawString("Resolved", x + 10, y + 15);
        }
    }

    private void paintSelectedBanner(Graphics2D g, int x, int y, int w) {
        int h = 54;
        RoundRectangle2D b = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(b);
        g.setColor(new Color(0xFD, 0xD7, 0xE4));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(b);

        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawOval(x + 16, y + 16, 18, 18);
        g.drawLine(x + 20, y + 25, x + 23, y + 29);
        g.drawLine(x + 23, y + 29, x + 29, y + 21);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(selectedReportId + " selected for review", x + 44, y + 24);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Review the report context on the right panel before executing moderation actions.", x + 44, y + 42);
    }

    private void paintDetailPanel(Graphics2D g, int x, int y, int w, int h, AdminDashboard dashboard) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        AdminDataStore.ReportModel cur = AdminDataStore.getInstance().getAllReports().stream()
                .filter(r -> r.id.equalsIgnoreCase(selectedReportId))
                .findFirst()
                .orElse(AdminDataStore.getInstance().getAllReports().get(0));

        int padX = 24;
        int curY = y + 28;

        // Header: "Report R-1048"
        g.setFont(AdminTheme.FONT_HEADING);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Report " + cur.id, x + padX, curY);

        curY += 22;
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Submitted " + cur.date + " · " + cur.timeUtc, x + padX, curY);

        // Pills: Severity & Status
        curY += 16;
        paintSeverityBadge(g, x + padX, curY, cur.severity);
        paintStatusBadge(g, x + padX + 72, curY, cur.status);

        // Reporter Profile
        curY += 36;
        int avSize = 32;
        g.setColor(AdminTheme.BRAND_TINT);
        g.fillOval(x + padX, curY, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString(cur.reporterInitials, x + padX + 8, curY + 20);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(cur.reporter, x + padX + avSize + 12, curY + 14);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Reported a " + cur.type.toLowerCase() + " · " + cur.category, x + padX + avSize + 12, curY + 28);

        // Reporter's Description
        curY += 46;
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("REPORTER'S DESCRIPTION", x + padX, curY);

        curY += 16;
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_DARK);
        drawStringMultiLine(g, "\"" + cur.reporterDesc + "\"", x + padX, curY, w - padX * 2);

        // Flagged Content Box
        curY += 56;
        int boxH = 88;
        RoundRectangle2D qBox = new RoundRectangle2D.Float(x + padX, curY, w - padX * 2, boxH, 8, 8);
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(qBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(qBox);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(cur.flaggedAuthor, x + padX + 12, curY + 20);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        drawStringMultiLine(g, cur.flaggedContent, x + padX + 12, curY + 40, w - padX * 2 - 24);

        g.setFont(AdminTheme.FONT_REG_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString(cur.flaggedTime, x + padX + 12, curY + boxH - 10);

        // Context note
        int ctxY = curY + boxH + 16;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Context: Related messages recorded · Safety check clean.", x + padX, ctxY);

        // Moderation Note Field
        int noteY = ctxY + 18;
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.drawString("Moderation note", x + padX, noteY);

        noteBoxX = x + padX;
        noteBoxY = noteY + 8;
        noteBoxW = w - padX * 2;
        noteBoxH = 34;
        RoundRectangle2D nBox = new RoundRectangle2D.Float(noteBoxX, noteBoxY, noteBoxW, noteBoxH, 6, 6);
        g.setColor(Color.WHITE);
        g.fill(nBox);
        g.setColor(noteFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(noteFocused ? 1.5f : 1.2f));
        g.draw(nBox);

        g.setFont(AdminTheme.FONT_REG_11);
        if (noteInput.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Add an internal note for the moderation team...", x + padX + 10, noteBoxY + 21);
        } else {
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(noteInput, x + padX + 10, noteBoxY + 21);
        }

        // Blinking cursor when focused
        if (noteFocused && (System.currentTimeMillis() % 1000 < 500)) {
            FontMetrics nfm = g.getFontMetrics();
            int curPos = x + padX + 10 + (noteInput.isEmpty() ? 0 : nfm.stringWidth(noteInput));
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawLine(curPos, noteBoxY + 8, curPos, noteBoxY + noteBoxH - 8);
        }

        // Row 1 Action Buttons: Dismiss | Warn User
        int b1Y = noteBoxY + noteBoxH + 16;
        dismissBtnX = x + padX;
        dismissBtnY = b1Y;
        dismissBtnW = 90;
        RoundRectangle2D disBtn = new RoundRectangle2D.Float(dismissBtnX, dismissBtnY, dismissBtnW, dismissBtnH, 8, 8);
        g.setColor(dismissHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(disBtn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(disBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Dismiss", dismissBtnX + 22, dismissBtnY + 22);

        warnBtnX = dismissBtnX + dismissBtnW + 10;
        warnBtnY = b1Y;
        warnBtnW = 96;
        RoundRectangle2D warnBtn = new RoundRectangle2D.Float(warnBtnX, warnBtnY, warnBtnW, warnBtnH, 8, 8);
        g.setColor(warnHovered ? new Color(0xFE, 0xF3, 0xC7) : Color.WHITE);
        g.fill(warnBtn);
        g.setColor(new Color(0xD9, 0x77, 0x06));
        g.draw(warnBtn);
        g.setColor(new Color(0xB4, 0x53, 0x09));
        g.drawString("Warn User", warnBtnX + 18, warnBtnY + 22);

        // Row 2 Action Buttons: Remove Content (Red) | Escalate
        int b2Y = b1Y + dismissBtnH + 12;
        removeBtnX = x + padX;
        removeBtnY = b2Y;
        removeBtnW = 124;
        RoundRectangle2D rmBtn = new RoundRectangle2D.Float(removeBtnX, removeBtnY, removeBtnW, removeBtnH, 8, 8);
        g.setColor(removeHovered ? new Color(0x99, 0x1B, 0x1B) : new Color(0xDC, 0x26, 0x26));
        g.fill(rmBtn);
        g.setColor(Color.WHITE);
        g.drawString("Remove Content", removeBtnX + 14, removeBtnY + 23);

        escalateBtnX = removeBtnX + removeBtnW + 10;
        escalateBtnY = b2Y;
        escalateBtnW = 90;
        RoundRectangle2D escBtn = new RoundRectangle2D.Float(escalateBtnX, escalateBtnY, escalateBtnW, escalateBtnH, 8, 8);
        g.setColor(escalateHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(escBtn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(escBtn);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Escalate", escalateBtnX + 20, escalateBtnY + 23);
    }

    private void drawStringMultiLine(Graphics2D g, String text, int x, int y, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        int cy = y;

        for (String w : words) {
            if (fm.stringWidth(cur + " " + w) > maxW) {
                g.drawString(cur.toString(), x, cy);
                cur = new StringBuilder(w);
                cy += fm.getHeight();
            } else {
                if (cur.length() > 0) cur.append(" ");
                cur.append(w);
            }
        }
        if (cur.length() > 0) {
            g.drawString(cur.toString(), x, cy);
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Dropdown popover item clicks (if any dropdown is open)
        if (activePopover != 0) {
            for (DropdownItemHitbox dh : popoverHitboxes) {
                if (mx >= dh.x && mx <= dh.x + dh.w && my >= dh.y && my <= dh.y + dh.h) {
                    if (activePopover == 1) {
                        sevFilterIdx = dh.index;
                        dashboard.showToast("Filter severity: " + severityOptions[sevFilterIdx]);
                    } else if (activePopover == 2) {
                        typeFilterIdx = dh.index;
                        dashboard.showToast("Filter content type: " + typeOptions[typeFilterIdx]);
                    } else if (activePopover == 3) {
                        statusFilterIdx = dh.index;
                        dashboard.showToast("Filter report status: " + statusOptions[statusFilterIdx]);
                    }
                    activePopover = 0;
                    return true;
                }
            }

            // Check if clicking dropdown button itself to toggle it
            boolean clickedDropdownBtn = (mx >= sevDropX && mx <= sevDropX + sevDropW && my >= sevDropY && my <= sevDropY + sevDropH)
                    || (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY && my <= typeDropY + typeDropH)
                    || (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH);

            if (!clickedDropdownBtn) {
                // Click outside closes popover
                activePopover = 0;
                return true;
            }
        }

        // 2. Dropdown button clicks (toggle popover)
        if (mx >= sevDropX && mx <= sevDropX + sevDropW && my >= sevDropY && my <= sevDropY + sevDropH) {
            activePopover = (activePopover == 1) ? 0 : 1;
            return true;
        }
        if (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY && my <= typeDropY + typeDropH) {
            activePopover = (activePopover == 2) ? 0 : 2;
            return true;
        }
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            activePopover = (activePopover == 3) ? 0 : 3;
            return true;
        }

        // 3. Search Clear Button ('✕')
        if (!searchQuery.isEmpty() && searchClearX > 0 && mx >= searchClearX && mx <= searchClearX + searchClearSize && my >= searchClearY && my <= searchClearY + searchClearSize) {
            searchQuery = "";
            searchFocused = false;
            dashboard.showToast("Report search cleared");
            return true;
        }

        // 4. Search input focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            noteFocused = false;
            return true;
        } else {
            searchFocused = false;
        }

        // 5. Summary pills shortcuts
        if (mx >= pendingPillX && mx <= pendingPillX + pendingPillW && my >= pendingPillY && my <= pendingPillY + pendingPillH) {
            statusFilterIdx = (statusFilterIdx == 1) ? 0 : 1;
            dashboard.showToast(statusFilterIdx == 1 ? "Filtering: Pending reports" : "Showing all statuses");
            return true;
        }
        if (mx >= highPillX && mx <= highPillX + highPillW && my >= highPillY && my <= highPillY + highPillH) {
            sevFilterIdx = (sevFilterIdx == 1) ? 0 : 1;
            dashboard.showToast(sevFilterIdx == 1 ? "Filtering: High priority reports" : "Showing all severities");
            return true;
        }
        if (mx >= resolvedPillX && mx <= resolvedPillX + resolvedPillW && my >= resolvedPillY && my <= resolvedPillY + resolvedPillH) {
            statusFilterIdx = (statusFilterIdx == 3) ? 0 : 3;
            dashboard.showToast(statusFilterIdx == 3 ? "Filtering: Resolved reports" : "Showing all statuses");
            return true;
        }

        // 6. Empty state Reset filters button
        if (resetBtnX > 0 && mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH) {
            searchQuery = "";
            sevFilterIdx = 0;
            typeFilterIdx = 0;
            statusFilterIdx = 0;
            activePopover = 0;
            dashboard.showToast("All report filters reset");
            return true;
        }

        // 7. Moderation Note box focus
        if (mx >= noteBoxX && mx <= noteBoxX + noteBoxW && my >= noteBoxY && my <= noteBoxY + noteBoxH) {
            noteFocused = true;
            searchFocused = false;
            return true;
        } else {
            noteFocused = false;
        }

        // 8. Table row selection with hitboxes
        for (ReportHitbox hb : rowHitboxes) {
            if (mx >= hb.x && mx <= hb.x + hb.w && my >= hb.y && my <= hb.y + hb.h) {
                selectedReportId = hb.id;
                dashboard.showToast("Inspecting Report " + selectedReportId);
                return true;
            }
        }

        // 9. Action Buttons in Right Panel
        if (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY && my <= dismissBtnY + dismissBtnH) {
            AdminDataStore.getInstance().resolveReport(selectedReportId, noteInput);
            dashboard.showToast("Report " + selectedReportId + " dismissed as non-violating.");
            noteInput = "";
            return true;
        }

        if (mx >= warnBtnX && mx <= warnBtnX + warnBtnW && my >= warnBtnY && my <= warnBtnY + warnBtnH) {
            AdminDataStore.getInstance().warnUserReport(selectedReportId, noteInput);
            dashboard.showToast("Official warning issued for report " + selectedReportId);
            noteInput = "";
            return true;
        }

        if (mx >= removeBtnX && mx <= removeBtnX + removeBtnW && my >= removeBtnY && my <= removeBtnY + removeBtnH) {
            AdminDataStore.getInstance().removeContentReport(selectedReportId, noteInput);
            dashboard.showToast("Flagged content for " + selectedReportId + " removed and purged.");
            noteInput = "";
            return true;
        }

        if (mx >= escalateBtnX && mx <= escalateBtnX + escalateBtnW && my >= escalateBtnY && my <= escalateBtnY + escalateBtnH) {
            AdminDataStore.getInstance().escalateReport(selectedReportId, noteInput);
            dashboard.showToast("Report " + selectedReportId + " escalated to safety council.");
            noteInput = "";
            return true;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        // Dropdown popover item hovers
        if (activePopover != 0) {
            for (DropdownItemHitbox dh : popoverHitboxes) {
                boolean pHov = dh.hovered;
                dh.hovered = (mx >= dh.x && mx <= dh.x + dh.w && my >= dh.y && my <= dh.y + dh.h);
                if (dh.hovered != pHov) changed = true;
            }
        }

        // Dropdown buttons hover
        boolean pSev = sevDropHovered;
        sevDropHovered = (mx >= sevDropX && mx <= sevDropX + sevDropW && my >= sevDropY && my <= sevDropY + sevDropH);
        if (pSev != sevDropHovered) changed = true;

        boolean pType = typeDropHovered;
        typeDropHovered = (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY && my <= typeDropY + typeDropH);
        if (pType != typeDropHovered) changed = true;

        boolean pStat = statusDropHovered;
        statusDropHovered = (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH);
        if (pStat != statusDropHovered) changed = true;

        // Search clear hover
        boolean pClr = searchClearHovered;
        searchClearHovered = (!searchQuery.isEmpty() && searchClearX > 0 && mx >= searchClearX && mx <= searchClearX + searchClearSize && my >= searchClearY && my <= searchClearY + searchClearSize);
        if (pClr != searchClearHovered) changed = true;

        // Summary pills hover
        boolean pPend = pendingHovered;
        pendingHovered = (mx >= pendingPillX && mx <= pendingPillX + pendingPillW && my >= pendingPillY && my <= pendingPillY + pendingPillH);
        if (pPend != pendingHovered) changed = true;

        boolean pHgh = highHovered;
        highHovered = (mx >= highPillX && mx <= highPillX + highPillW && my >= highPillY && my <= highPillY + highPillH);
        if (pHgh != highHovered) changed = true;

        boolean pRes = resolvedHovered;
        resolvedHovered = (mx >= resolvedPillX && mx <= resolvedPillX + resolvedPillW && my >= resolvedPillY && my <= resolvedPillY + resolvedPillH);
        if (pRes != resolvedHovered) changed = true;

        // Reset button hover
        boolean pRst = resetBtnHovered;
        resetBtnHovered = (resetBtnX > 0 && mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY && my <= resetBtnY + resetBtnH);
        if (pRst != resetBtnHovered) changed = true;

        // Table rows hover
        String prevHoveredId = hoveredReportId;
        hoveredReportId = null;
        for (ReportHitbox hb : rowHitboxes) {
            if (mx >= hb.x && mx <= hb.x + hb.w && my >= hb.y && my <= hb.y + hb.h) {
                hoveredReportId = hb.id;
                break;
            }
        }
        if ((prevHoveredId == null && hoveredReportId != null) || (prevHoveredId != null && !prevHoveredId.equals(hoveredReportId))) {
            changed = true;
        }

        // Action buttons hover
        boolean pDis = dismissHovered;
        dismissHovered = (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY && my <= dismissBtnY + dismissBtnH);
        if (dismissHovered != pDis) changed = true;

        boolean pWarn = warnHovered;
        warnHovered = (mx >= warnBtnX && mx <= warnBtnX + warnBtnW && my >= warnBtnY && my <= warnBtnY + warnBtnH);
        if (warnHovered != pWarn) changed = true;

        boolean pRm = removeHovered;
        removeHovered = (mx >= removeBtnX && mx <= removeBtnX + removeBtnW && my >= removeBtnY && my <= removeBtnY + removeBtnH);
        if (removeHovered != pRm) changed = true;

        boolean pEsc = escalateHovered;
        escalateHovered = (mx >= escalateBtnX && mx <= escalateBtnX + escalateBtnW && my >= escalateBtnY && my <= escalateBtnY + escalateBtnH);
        if (escalateHovered != pEsc) changed = true;

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (searchFocused) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (searchQuery.length() > 0) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    return true;
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                searchFocused = false;
                return true;
            } else if (ch >= 32 && ch <= 126) {
                searchQuery += ch;
                return true;
            }
        } else if (noteFocused) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (noteInput.length() > 0) {
                    noteInput = noteInput.substring(0, noteInput.length() - 1);
                    return true;
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                noteFocused = false;
                return true;
            } else if (ch >= 32 && ch <= 126) {
                noteInput += ch;
                return true;
            }
        }
        return false;
    }

    // Getters and Setters for Testing & Automation
    public String getSelectedReportId() { return selectedReportId; }
    public void setSelectedReportId(String id) { this.selectedReportId = id; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String q) { this.searchQuery = (q == null) ? "" : q; }

    public String getNoteInput() { return noteInput; }
    public void setNoteInput(String note) { this.noteInput = (note == null) ? "" : note; }

    public int getSevFilterIdx() { return sevFilterIdx; }
    public void setSevFilterIdx(int idx) { this.sevFilterIdx = idx; }

    public int getTypeFilterIdx() { return typeFilterIdx; }
    public void setTypeFilterIdx(int idx) { this.typeFilterIdx = idx; }

    public int getStatusFilterIdx() { return statusFilterIdx; }
    public void setStatusFilterIdx(int idx) { this.statusFilterIdx = idx; }

    public int getActivePopover() { return activePopover; }
    public void setActivePopover(int p) { this.activePopover = p; }

    public int getSevDropX() { return sevDropX; }
    public int getSevDropY() { return sevDropY; }
    public int getSevDropW() { return sevDropW; }
    public int getSevDropH() { return sevDropH; }

    public int getTypeDropX() { return typeDropX; }
    public int getTypeDropY() { return typeDropY; }
    public int getTypeDropW() { return typeDropW; }
    public int getTypeDropH() { return typeDropH; }

    public int getStatusDropX() { return statusDropX; }
    public int getStatusDropY() { return statusDropY; }
    public int getStatusDropW() { return statusDropW; }
    public int getStatusDropH() { return statusDropH; }

    public int getSearchX() { return searchX; }
    public int getSearchY() { return searchY; }
    public int getSearchClearX() { return searchClearX; }
    public int getSearchClearY() { return searchClearY; }

    public int getDismissBtnX() { return dismissBtnX; }
    public int getDismissBtnY() { return dismissBtnY; }
    public int getWarnBtnX() { return warnBtnX; }
    public int getWarnBtnY() { return warnBtnY; }
    public int getRemoveBtnX() { return removeBtnX; }
    public int getRemoveBtnY() { return removeBtnY; }
    public int getEscalateBtnX() { return escalateBtnX; }
    public int getEscalateBtnY() { return escalateBtnY; }

    public int getResetBtnX() { return resetBtnX; }
    public int getResetBtnY() { return resetBtnY; }

    public int getPendingPillX() { return pendingPillX; }
    public int getPendingPillY() { return pendingPillY; }
    public int getHighPillX() { return highPillX; }
    public int getHighPillY() { return highPillY; }
    public int getResolvedPillX() { return resolvedPillX; }
    public int getResolvedPillY() { return resolvedPillY; }

    public List<ReportHitbox> getRowHitboxes() { return rowHitboxes; }
    public List<DropdownItemHitbox> getPopoverHitboxes() { return popoverHitboxes; }
}
