import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Dashboard - Reports Moderation Center.
 * 60/40 Split View: Left interactive reports table + Right rich inspection panel with working actions.
 * Pixel-accurate hitboxes for row selection, working live search & filters, and instant reactive moderation.
 * Matches Figma reference Image 4 with exact precision.
 */
public class AdminReports {

    public static class ReportHitbox {
        int x, y, w, h;
        String id;
    }

    private final List<ReportHitbox> rowHitboxes = new ArrayList<>();
    private String selectedReportId = "R-1048";

    private String noteInput = "";
    private boolean noteFocused = false;
    private int noteBoxX, noteBoxY, noteBoxW, noteBoxH = 34;

    // Search and filter dropdowns
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 320, searchH = 34;

    private final String[] severityOptions = {"All severities", "High", "Medium", "Low"};
    private int sevFilterIdx = 0;
    private int sevDropX, sevDropY, sevDropW = 116, sevDropH = 34;

    private final String[] typeOptions = {"All content types", "Message", "Project", "Profile", "Comment"};
    private int typeFilterIdx = 0;
    private int typeDropX, typeDropY, typeDropW = 136, typeDropH = 34;

    private final String[] statusOptions = {"All statuses", "Pending", "In review", "Resolved"};
    private int statusFilterIdx = 0;
    private int statusDropX, statusDropY, statusDropW = 110, statusDropH = 34;

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
        return 960;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header
        paintHeader(g, x + padX, curY, contentW);
        curY += 76;

        // 2. Status Summary Pills
        paintSummaryPills(g, x + padX, curY);
        curY += 38;

        // 3. Filter Bar
        paintFilterBar(g, x + padX, curY, contentW);
        curY += 48;

        // 4. Split Layout: Left Table (60%) + Right Detail Panel (40%)
        int splitGap = 20;
        int leftW = Math.round(contentW * 0.60f) - splitGap / 2;
        int rightW = contentW - leftW - splitGap;
        int panelH = 580;

        paintReportsTable(g, x + padX, curY, leftW, panelH);
        paintDetailPanel(g, x + padX + leftW + splitGap, curY, rightW, panelH, dashboard);
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
        g.drawString("Sample data", pillX + 8, pillY + 14);

        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Investigate user reports, flagged content, and resolve moderation actions.", x, y + 48);
    }

    private void paintSummaryPills(Graphics2D g, int x, int y) {
        int pending = AdminDataStore.getInstance().getPendingReportsCount();

        // Pending Pill
        RoundRectangle2D pPill = new RoundRectangle2D.Float(x, y, 120, 24, 12, 12);
        g.setColor(new Color(0xFE, 0xF3, 0xC7));
        g.fill(pPill);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(new Color(0xB4, 0x53, 0x09));
        g.drawString(pending + " pending reports", x + 10, y + 16);

        // High priority Pill
        int highX = x + 130;
        RoundRectangle2D hPill = new RoundRectangle2D.Float(highX, y, 96, 24, 12, 12);
        g.setColor(new Color(0xFE, 0xE2, 0xE2));
        g.fill(hPill);
        g.setColor(new Color(0xDC, 0x26, 0x26));
        g.drawString("5 high priority", highX + 10, y + 16);

        // Resolved this month
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("42 resolved this month", highX + 112, y + 16);
    }

    private void paintFilterBar(Graphics2D g, int x, int y, int w) {
        // Search Box
        searchX = x;
        searchY = y;
        searchW = Math.min(320, w - 400);
        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(sBox);

        AdminTheme.drawSearchIcon(g, searchX + 10, searchY + 9, 16, AdminTheme.TEXT_LIGHT);
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(searchQuery.isEmpty() ? AdminTheme.TEXT_LIGHT : AdminTheme.TEXT_DARK);
        g.drawString(searchQuery.isEmpty() ? "Search report ID or content..." : searchQuery, searchX + 34, searchY + 22);

        // Right side dropdowns
        statusDropW = 110;
        statusDropX = x + w - statusDropW;
        statusDropY = y;
        paintDropdown(g, statusDropX, y, statusDropW, searchH, statusOptions[statusFilterIdx]);

        typeDropW = 136;
        typeDropX = statusDropX - typeDropW - 10;
        typeDropY = y;
        paintDropdown(g, typeDropX, y, typeDropW, searchH, typeOptions[typeFilterIdx]);

        sevDropW = 116;
        sevDropX = typeDropX - sevDropW - 10;
        sevDropY = y;
        paintDropdown(g, sevDropX, y, sevDropW, searchH, severityOptions[sevFilterIdx]);
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

        AdminTheme.drawChevronDown(g, dx + dw - 18, dy + 14, 8, AdminTheme.TEXT_LIGHT, 0f);
    }

    private void paintReportsTable(Graphics2D g, int x, int y, int w, int h) {
        int tableH = 430;
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

        int colRep = x + 20;
        int colSev = x + Math.round(w * 0.44f);
        int colDate = x + Math.round(w * 0.60f);
        int colStat = x + Math.round(w * 0.83f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Report / Content", colRep, thY + 24);
        g.drawString("Severity", colSev, thY + 24);
        g.drawString("Reporter / Date", colDate, thY + 24);
        g.drawString("Status", colStat, thY + 24);

        int rowH = 68;
        int curRowY = thY + 38;

        List<AdminDataStore.ReportModel> reports = getFilteredReports();
        rowHitboxes.clear();

        for (int i = 0; i < Math.min(5, reports.size()); i++) {
            AdminDataStore.ReportModel r = reports.get(i);
            boolean isSel = r.id.equalsIgnoreCase(selectedReportId);

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
            }

            // Report ID & Title
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString(r.id + " · " + r.type, colRep, curRowY + 20);

            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(r.title, colRep, curRowY + 38);

            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(r.category, colRep, curRowY + 54);

            // Severity
            g.setFont(AdminTheme.FONT_BOLD_11);
            if ("High".equalsIgnoreCase(r.severity)) g.setColor(new Color(0xDC, 0x26, 0x26));
            else if ("Medium".equalsIgnoreCase(r.severity)) g.setColor(new Color(0xD9, 0x77, 0x06));
            else g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(r.severity, colSev, curRowY + 36);

            // Reporter & Date
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(r.reporter, colDate, curRowY + 26);
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(r.date, colDate, curRowY + 44);

            // Status Pill
            paintStatusBadge(g, colStat, curRowY + 22, r.status);

            curRowY += rowH;
            if (i < Math.min(5, reports.size()) - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, curRowY, x + w, curRowY);
            }
        }

        // Selected Banner below table
        paintSelectedBanner(g, x, y + tableH + 16, w);
    }

    private List<AdminDataStore.ReportModel> getFilteredReports() {
        String q = searchQuery.trim().toLowerCase();
        return AdminDataStore.getInstance().getAllReports().stream().filter(r -> {
            boolean matchQ = q.isEmpty() || r.id.toLowerCase().contains(q) || r.title.toLowerCase().contains(q) || r.reporter.toLowerCase().contains(q);
            boolean matchSev = sevFilterIdx == 0 || r.severity.equalsIgnoreCase(severityOptions[sevFilterIdx]);
            boolean matchType = typeFilterIdx == 0 || r.type.equalsIgnoreCase(typeOptions[typeFilterIdx]);
            boolean matchStat = statusFilterIdx == 0 || r.status.equalsIgnoreCase(statusOptions[statusFilterIdx]);
            return matchQ && matchSev && matchType && matchStat;
        }).collect(Collectors.toList());
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
        g.drawString(selectedReportId + " selected", x + 44, y + 24);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Review the report and surrounding context before taking action.", x + 44, y + 42);
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
        int p1W = 86;
        int pH = 22;
        RoundRectangle2D sp = new RoundRectangle2D.Float(x + padX, curY, p1W, pH, 10, 10);
        g.setColor(new Color(0xFE, 0xE2, 0xE2));
        g.fill(sp);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(new Color(0xDC, 0x26, 0x26));
        g.drawString(cur.severity + " severity", x + padX + 10, curY + 15);

        int p2W = 68;
        RoundRectangle2D stp = new RoundRectangle2D.Float(x + padX + p1W + 10, curY, p2W, pH, 10, 10);
        paintStatusBadge(g, x + padX + p1W + 10, curY, cur.status);

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
        g.drawString("Context: 3 related messages · No previous warnings for this member.", x + padX, ctxY);

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
        g.setStroke(new BasicStroke(1.2f));
        g.draw(nBox);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(noteInput.isEmpty() ? AdminTheme.TEXT_LIGHT : AdminTheme.TEXT_DARK);
        g.drawString(noteInput.isEmpty() ? "Add an internal note for the moderation team..." : noteInput, x + padX + 10, noteBoxY + 21);

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
        // Search focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            noteFocused = false;
            return true;
        } else {
            searchFocused = false;
        }

        // Dropdown clicks
        if (mx >= sevDropX && mx <= sevDropX + sevDropW && my >= sevDropY && my <= sevDropY + sevDropH) {
            sevFilterIdx = (sevFilterIdx + 1) % severityOptions.length;
            dashboard.showToast("Filter severity: " + severityOptions[sevFilterIdx]);
            return true;
        }
        if (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY && my <= typeDropY + typeDropH) {
            typeFilterIdx = (typeFilterIdx + 1) % typeOptions.length;
            dashboard.showToast("Filter content type: " + typeOptions[typeFilterIdx]);
            return true;
        }
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            statusFilterIdx = (statusFilterIdx + 1) % statusOptions.length;
            dashboard.showToast("Filter report status: " + statusOptions[statusFilterIdx]);
            return true;
        }

        // Note box focus
        if (mx >= noteBoxX && mx <= noteBoxX + noteBoxW && my >= noteBoxY && my <= noteBoxY + noteBoxH) {
            noteFocused = true;
            searchFocused = false;
            return true;
        } else {
            noteFocused = false;
        }

        // Table row selection with exact hitboxes
        for (ReportHitbox hb : rowHitboxes) {
            if (mx >= hb.x && mx <= hb.x + hb.w && my >= hb.y && my <= hb.y + hb.h) {
                selectedReportId = hb.id;
                dashboard.showToast("Inspecting Report " + selectedReportId);
                return true;
            }
        }

        // Action Buttons in Right Panel
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
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
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
}
