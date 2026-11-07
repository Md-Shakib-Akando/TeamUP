import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Overview Page for TeamUp Admin Dashboard.
 * Renders KPI cards, user growth curve chart, project activity dual-bar chart,
 * pending moderation table, and recent platform activity feed.
 */
public class AdminOverview {

    public static class ModerationItem {
        public String id;
        public String title;
        public String type; // "Message", "Project", "Profile"
        public String reporter;
        public String date;
        public String status; // "Pending", "In review"
        public int actionX, actionY, actionW, actionH;
        public boolean actionHovered = false;

        public ModerationItem(String id, String title, String type, String reporter, String date, String status) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.reporter = reporter;
            this.date = date;
            this.status = status;
        }
    }

    public static class ActivityItem {
        public String actor;
        public String action;
        public String time;
        public int iconType; // 0=Activity, 1=Shield, 2=Join, 3=Check

        public ActivityItem(String actor, String action, String time, int iconType) {
            this.actor = actor;
            this.action = action;
            this.time = time;
            this.iconType = iconType;
        }
    }

    private final List<ModerationItem> moderationList = new ArrayList<>();
    private final List<ActivityItem> activityList = new ArrayList<>();

    // Action button bounds
    private int viewAllReportsX, viewAllReportsY, viewAllReportsW = 145, viewAllReportsH = 34;
    private boolean viewAllReportsHovered = false;

    private int datePickerX, datePickerY, datePickerW = 150, datePickerH = 36;
    private boolean datePickerHovered = false;

    public AdminOverview() {
        // Initialize Sample Moderation Queue from Figma
        moderationList.add(new ModerationItem("R-1048", "StudyBuddy AI conversation", "Message", "Priya Shah", "Oct 16, 2026", "Pending"));
        moderationList.add(new ModerationItem("R-1047", "SkillSwap Marketplace", "Project", "Maya Patel", "Oct 15, 2026", "In review"));
        moderationList.add(new ModerationItem("R-1046", "Unsolicited project invite", "Message", "Alex Morgan", "Oct 15, 2026", "Pending"));
        moderationList.add(new ModerationItem("R-1045", "User profile biography", "Profile", "Sam Chen", "Oct 14, 2026", "Pending"));

        // Initialize Recent Platform Activity
        activityList.add(new ActivityItem("Maya Patel", "created Local Food Network", "12 minutes ago", 0));
        activityList.add(new ActivityItem("Jordan Lee", "suspended Noah Williams", "38 minutes ago", 1));
        activityList.add(new ActivityItem("Alex Morgan", "joined Research Commons", "1 hour ago", 2));
        activityList.add(new ActivityItem("Sam Chen", "resolved report R-1044", "2 hours ago", 3));
    }

    public List<ModerationItem> getModerationList() {
        return moderationList;
    }

    public int getComputedHeight(int width) {
        return 1020;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header Section
        paintHeaderSection(g, x + padX, curY, contentW);
        curY += 76;

        // 2. Row of 4 KPI Metric Cards
        int kpiGap = 18;
        int kpiW = (contentW - kpiGap * 3) / 4;
        int kpiH = 120;

        int totalUsers = AdminDataStore.getInstance().baseTotalUsers;
        int activeProjects = AdminDataStore.getInstance().getActiveProjectsCount();
        int pendingReports = AdminDataStore.getInstance().getPendingReportsCount();
        int collabRequests = AdminDataStore.getInstance().collaborationRequests;

        paintKpiCard(g, x + padX, curY, kpiW, kpiH, "Total Users", String.format("%,d", totalUsers), "↑ 8.2% vs. previous month", AdminTheme.BRAND_GREEN_MID, 0);
        paintKpiCard(g, x + padX + (kpiW + kpiGap), curY, kpiW, kpiH, "Active Projects", String.valueOf(activeProjects), "↑ 12.6% vs. previous month", AdminTheme.BRAND_GREEN_MID, 1);
        paintKpiCard(g, x + padX + (kpiW + kpiGap) * 2, curY, kpiW, kpiH, "Pending Reports", String.valueOf(pendingReports), "5 high priority · Review queue", new Color(0xC2, 0x41, 0x0C), 2);
        paintKpiCard(g, x + padX + (kpiW + kpiGap) * 3, curY, kpiW, kpiH, "Collaboration Requests", String.format("%,d", collabRequests), "↑ 18.4% this month", AdminTheme.BRAND_GREEN_MID, 3);

        curY += kpiH + 24;

        // 3. Charts Row (User Growth & Project Activity)
        int chartGap = 20;
        int chartW = (contentW - chartGap) / 2;
        int chartH = 290;

        paintUserGrowthChart(g, x + padX, curY, chartW, chartH);
        paintProjectActivityChart(g, x + padX + chartW + chartGap, curY, chartW, chartH);

        curY += chartH + 28;

        // 4. Bottom Row: Pending Moderation (Left 64%) + Recent Activity (Right 36%)
        int splitGap = 20;
        int modW = Math.round(contentW * 0.65f) - splitGap / 2;
        int actW = contentW - modW - splitGap;
        int bottomH = 340;

        paintPendingModeration(g, x + padX, curY, modW, bottomH, dashboard);
        paintRecentActivity(g, x + padX + modW + splitGap, curY, actW, bottomH);

        curY += bottomH + 24;

        // 5. Footer Note
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Snapshot: October 16, 2026, 10:30 AM UTC · All metrics are sample data.", x + padX, curY + 12);
    }

    private void paintHeaderSection(Graphics2D g, int x, int y, int w) {
        // Title: "Admin Overview"
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Admin Overview", x, y + 24);

        int titleW = hfm.stringWidth("Admin Overview");

        // "Sample data" Pill Badge
        int pillX = x + titleW + 12;
        int pillY = y + 8;
        int pillW = 76;
        int pillH = 20;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(pill);
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("Sample data", pillX + 8, pillY + 14);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("A pulse on your community and what needs your attention.", x, y + 48);

        // Date Range Picker Button on the right
        datePickerW = 145;
        datePickerH = 36;
        datePickerX = x + w - datePickerW;
        datePickerY = y + 8;

        RoundRectangle2D dateBox = new RoundRectangle2D.Float(datePickerX, datePickerY, datePickerW, datePickerH, 10, 10);
        g.setColor(datePickerHovered ? new Color(0xF5, 0xF3, 0xEF) : Color.WHITE);
        g.fill(dateBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(dateBox);

        // Calendar Icon
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawRect(datePickerX + 12, datePickerY + 10, 14, 13);
        g.drawLine(datePickerX + 12, datePickerY + 14, datePickerX + 26, datePickerY + 14);
        g.fillRect(datePickerX + 15, datePickerY + 8, 2, 3);
        g.fillRect(datePickerX + 21, datePickerY + 8, 2, 3);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Oct 1 – 16, 2026", datePickerX + 34, datePickerY + 22);
    }

    private void paintKpiCard(Graphics2D g, int x, int y, int w, int h, String title, String value, String subtext, Color trendCol, int iconType) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Title
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(title, x + 18, y + 26);

        // Top right icon
        paintKpiIcon(g, x + w - 34, y + 14, iconType);

        // Big value
        g.setFont(AdminTheme.FONT_METRIC);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(value, x + 18, y + 68);

        // Trend / subtext
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(trendCol);
        g.drawString(subtext, x + 18, y + 96);
    }

    private void paintKpiIcon(Graphics2D g, int ix, int iy, int type) {
        g.setColor(AdminTheme.BRAND_GREEN_MID);
        g.setStroke(new BasicStroke(1.5f));

        if (type == 0) {
            // Users icon
            g.drawOval(ix + 3, iy, 7, 7);
            g.drawArc(ix, iy + 9, 13, 8, 0, 180);
        } else if (type == 1) {
            // Folder icon
            g.drawRoundRect(ix, iy + 2, 16, 12, 3, 3);
            g.drawLine(ix, iy + 5, ix + 6, iy + 5);
        } else if (type == 2) {
            // Flag icon
            g.setColor(new Color(0xC2, 0x41, 0x0C));
            g.drawLine(ix + 2, iy, ix + 2, iy + 16);
            g.drawRect(ix + 2, iy + 1, 10, 8);
        } else {
            // Handshake / heart icon
            g.drawOval(ix + 2, iy + 2, 6, 6);
            g.drawOval(ix + 8, iy + 2, 6, 6);
            g.drawLine(ix + 2, iy + 7, ix + 8, iy + 14);
            g.drawLine(ix + 14, iy + 7, ix + 8, iy + 14);
        }
    }

    private void paintUserGrowthChart(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Title and subtitle
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("User growth", x + 22, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Total registered members · last 6 months", x + 22, y + 46);

        // Chart plot area
        int plotX = x + 50;
        int plotY = y + 74;
        int plotW = w - 74;
        int plotH = 145;

        // Horizontal gridlines & Y labels (12.5k, 10k, 7.5k, 5k)
        String[] yLabels = {"12.5k", "10k", "7.5k", "5k"};
        g.setFont(AdminTheme.FONT_REG_10);
        FontMetrics yfm = g.getFontMetrics();

        for (int i = 0; i < 4; i++) {
            int lineY = plotY + Math.round(i * (plotH / 3.0f));
            g.setColor(AdminTheme.CHART_GRID);
            g.setStroke(new BasicStroke(1f));
            g.drawLine(plotX, lineY, plotX + plotW, lineY);

            g.setColor(AdminTheme.TEXT_LIGHT);
            String lbl = yLabels[i];
            g.drawString(lbl, plotX - yfm.stringWidth(lbl) - 10, lineY + 4);
        }

        // X labels (May, Jun, Jul, Aug, Sep, Oct)
        String[] xLabels = {"May", "Jun", "Jul", "Aug", "Sep", "Oct"};
        float[] pointsY = {plotY + plotH, plotY + plotH * 0.76f, plotY + plotH * 0.58f, plotY + plotH * 0.35f, plotY + plotH * 0.16f, plotY + 4};

        for (int i = 0; i < xLabels.length; i++) {
            int lx = plotX + Math.round(i * (plotW / 5.0f));
            g.setColor(AdminTheme.TEXT_LIGHT);
            int lw = yfm.stringWidth(xLabels[i]);
            g.drawString(xLabels[i], lx - lw / 2, plotY + plotH + 20);
        }

        // Curved line path
        GeneralPath path = new GeneralPath();
        float startX = plotX;
        float startY = pointsY[0];
        path.moveTo(startX, startY);

        for (int i = 0; i < 5; i++) {
            float x1 = plotX + i * (plotW / 5.0f);
            float y1 = pointsY[i];
            float x2 = plotX + (i + 1) * (plotW / 5.0f);
            float y2 = pointsY[i + 1];

            float cx1 = x1 + (x2 - x1) * 0.5f;
            float cy1 = y1;
            float cx2 = x1 + (x2 - x1) * 0.5f;
            float cy2 = y2;
            path.curveTo(cx1, cy1, cx2, cy2, x2, y2);
        }

        g.setColor(AdminTheme.CHART_LINE);
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(path);

        // Chart Legend at bottom
        int legY = y + h - 22;
        g.setColor(AdminTheme.CHART_LINE);
        g.fillRect(x + 22, legY - 9, 10, 10);
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Registered users – 12,480 total", x + 38, legY);
    }

    private void paintProjectActivityChart(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Title and subtitle
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Project activity", x + 22, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Projects created and completed · last 6 months", x + 22, y + 46);

        // Plot area
        int plotX = x + 44;
        int plotY = y + 74;
        int plotW = w - 68;
        int plotH = 145;

        // Y gridlines & labels (80, 60, 40, 20, 0)
        String[] yLabels = {"80", "60", "40", "20", "0"};
        g.setFont(AdminTheme.FONT_REG_10);
        FontMetrics yfm = g.getFontMetrics();

        for (int i = 0; i < 5; i++) {
            int lineY = plotY + Math.round(i * (plotH / 4.0f));
            g.setColor(AdminTheme.CHART_GRID);
            g.setStroke(new BasicStroke(1f));
            g.drawLine(plotX, lineY, plotX + plotW, lineY);

            g.setColor(AdminTheme.TEXT_LIGHT);
            String lbl = yLabels[i];
            g.drawString(lbl, plotX - yfm.stringWidth(lbl) - 8, lineY + 4);
        }

        // 6 Months Bars: Created (dark green) & Completed (sage teal)
        int[] createdVals = {38, 47, 58, 50, 67, 75};
        int[] completedVals = {18, 25, 33, 43, 50, 57};
        String[] xLabels = {"May", "Jun", "Jul", "Aug", "Sep", "Oct"};

        int groupW = plotW / 6;
        int barW = 14;

        for (int i = 0; i < 6; i++) {
            int cx = plotX + i * groupW + groupW / 2;

            // Created Bar
            float cFrac = createdVals[i] / 80f;
            int cH = Math.round(plotH * cFrac);
            int cY = plotY + plotH - cH;
            int cX = cx - barW - 2;
            RoundRectangle2D b1 = new RoundRectangle2D.Float(cX, cY, barW, cH, 4, 4);
            g.setColor(AdminTheme.CHART_BAR_DARK);
            g.fill(b1);

            // Completed Bar
            float comFrac = completedVals[i] / 80f;
            int comH = Math.round(plotH * comFrac);
            int comY = plotY + plotH - comH;
            int comX = cx + 2;
            RoundRectangle2D b2 = new RoundRectangle2D.Float(comX, comY, barW, comH, 4, 4);
            g.setColor(AdminTheme.CHART_BAR_TEAL);
            g.fill(b2);

            // X label
            g.setFont(AdminTheme.FONT_REG_10);
            g.setColor(AdminTheme.TEXT_LIGHT);
            int lw = yfm.stringWidth(xLabels[i]);
            g.drawString(xLabels[i], cx - lw / 2, plotY + plotH + 18);
        }

        // Legend at bottom
        int legY = y + h - 22;
        g.setColor(AdminTheme.CHART_BAR_DARK);
        g.fillRect(x + 22, legY - 9, 10, 10);
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Created", x + 38, legY);

        g.setColor(AdminTheme.CHART_BAR_TEAL);
        g.fillRect(x + 105, legY - 9, 10, 10);
        g.drawString("Completed", x + 121, legY);
    }

    private void paintPendingModeration(Graphics2D g, int x, int y, int w, int h, AdminDashboard dashboard) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Title
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Pending moderation", x + 24, y + 30);

        // "View all reports" Pill Button
        int pendingCount = AdminDataStore.getInstance().getPendingReportsCount();
        String viewAllBtnText = "View all " + pendingCount + " reports";
        viewAllReportsW = 145;
        viewAllReportsH = 32;
        viewAllReportsX = x + w - viewAllReportsW - 24;
        viewAllReportsY = y + 14;

        RoundRectangle2D btn = new RoundRectangle2D.Float(viewAllReportsX, viewAllReportsY, viewAllReportsW, viewAllReportsH, 8, 8);
        g.setColor(viewAllReportsHovered ? new Color(0xF5, 0xF3, 0xEF) : Color.WHITE);
        g.fill(btn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(btn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        FontMetrics vfm = g.getFontMetrics();
        g.drawString(viewAllBtnText, viewAllReportsX + (viewAllReportsW - vfm.stringWidth(viewAllBtnText)) / 2, viewAllReportsY + 20);

        // Dynamic sync from AdminDataStore
        moderationList.clear();
        for (AdminDataStore.ReportModel r : AdminDataStore.getInstance().getAllReports()) {
            if (!"Resolved".equalsIgnoreCase(r.status)) {
                moderationList.add(new ModerationItem(r.id, r.title, r.type, r.reporter, r.date, r.status));
                if (moderationList.size() >= 4) break;
            }
        }

        // Table Header
        int tableTopY = y + 62;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fillRect(x + 1, tableTopY, w - 2, 34);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, tableTopY + 34, x + w, tableTopY + 34);

        int colContent = x + 24;
        int colReporter = x + Math.round(w * 0.44f);
        int colDate = x + Math.round(w * 0.62f);
        int colStatus = x + Math.round(w * 0.77f);
        int colAction = x + Math.round(w * 0.89f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Reported content / Type", colContent, tableTopY + 22);
        g.drawString("Reporter", colReporter, tableTopY + 22);
        g.drawString("Date", colDate, tableTopY + 22);
        g.drawString("Status", colStatus, tableTopY + 22);
        g.drawString("Action", colAction, tableTopY + 22);

        // Rows
        int rowH = 54;
        int curRowY = tableTopY + 34;

        for (int i = 0; i < moderationList.size(); i++) {
            ModerationItem item = moderationList.get(i);

            // Content title and sub
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(item.title, colContent, curRowY + 22);

            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(item.id + " · " + item.type, colContent, curRowY + 38);

            // Reporter
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(item.reporter, colReporter, curRowY + 30);

            // Date
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(item.date, colDate, curRowY + 30);

            // Status Pill
            int pillBadgeW = item.status.equals("In review") ? 68 : 62;
            int pillBadgeH = 22;
            int pillBadgeY = curRowY + 16;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(colStatus, pillBadgeY, pillBadgeW, pillBadgeH, 10, 10);
            boolean isReview = item.status.equals("In review");
            g.setColor(isReview ? AdminTheme.STATUS_REVIEW_BG : AdminTheme.STATUS_PENDING_BG);
            g.fill(sPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(isReview ? AdminTheme.STATUS_REVIEW_TXT : AdminTheme.STATUS_PENDING_TXT);
            g.drawString(item.status, colStatus + (isReview ? 10 : 12), pillBadgeY + 15);

            // Action: "Review →"
            item.actionX = colAction;
            item.actionY = curRowY + 14;
            item.actionW = 68;
            item.actionH = 26;

            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(item.actionHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
            g.drawString("Review →", colAction + (item.actionHovered ? 2 : 0), curRowY + 30);

            curRowY += rowH;
            if (i < moderationList.size() - 1) {
                g.setColor(AdminTheme.BORDER_COLOR);
                g.drawLine(x + 20, curRowY, x + w - 20, curRowY);
            }
        }
    }

    private void paintRecentActivity(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Title
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Recent platform activity", x + 24, y + 30);

        int curY = y + 68;
        int rowH = 62;

        activityList.clear();
        List<AdminDataStore.AuditLogModel> logs = AdminDataStore.getInstance().getAllAuditLogs();
        for (int i = 0; i < Math.min(4, logs.size()); i++) {
            AdminDataStore.AuditLogModel l = logs.get(i);
            int iconType = 0;
            String actLower = l.action.toLowerCase();
            if (actLower.contains("suspend") || actLower.contains("security")) iconType = 1;
            else if (actLower.contains("join") || actLower.contains("role") || actLower.contains("user")) iconType = 2;
            else if (actLower.contains("resolve") || actLower.contains("removed") || actLower.contains("created")) iconType = 3;
            activityList.add(new ActivityItem(l.actorName, l.action + " " + l.target, l.timestampLine1, iconType));
        }

        for (ActivityItem act : activityList) {
            // Icon
            paintActivityIcon(g, x + 24, curY + 2, act.iconType);

            // Actor (Bold) + Action
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            FontMetrics afm = g.getFontMetrics();
            g.drawString(act.actor, x + 48, curY + 14);

            int actorW = afm.stringWidth(act.actor);
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(" " + act.action, x + 48 + actorW, curY + 14);

            // Time
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(act.time, x + 48, curY + 30);

            curY += rowH;
        }
    }

    private void paintActivityIcon(Graphics2D g, int ix, int iy, int type) {
        g.setColor(AdminTheme.BRAND_GREEN_MID);
        g.setStroke(new BasicStroke(1.5f));

        if (type == 0) {
            // Pulse wave
            g.drawLine(ix, iy + 6, ix + 4, iy + 6);
            g.drawLine(ix + 4, iy + 6, ix + 7, iy);
            g.drawLine(ix + 7, iy, ix + 9, iy + 12);
            g.drawLine(ix + 9, iy + 12, ix + 12, iy + 6);
            g.drawLine(ix + 12, iy + 6, ix + 15, iy + 6);
        } else if (type == 1) {
            // Shield
            g.drawRect(ix + 1, iy + 1, 12, 12);
            g.drawLine(ix + 7, iy + 1, ix + 7, iy + 13);
        } else if (type == 2) {
            // Join
            g.drawOval(ix + 2, iy + 1, 6, 6);
            g.drawArc(ix, iy + 7, 10, 6, 0, 180);
        } else {
            // Checkmark
            g.drawLine(ix + 2, iy + 7, ix + 6, iy + 11);
            g.drawLine(ix + 6, iy + 11, ix + 13, iy + 3);
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // Date range picker click
        if (mx >= datePickerX && mx <= datePickerX + datePickerW && my >= datePickerY && my <= datePickerY + datePickerH) {
            dashboard.showToast("Date range selected: Oct 1 – 16, 2026");
            return true;
        }

        // View all reports button
        if (mx >= viewAllReportsX && mx <= viewAllReportsX + viewAllReportsW && my >= viewAllReportsY && my <= viewAllReportsY + viewAllReportsH) {
            dashboard.openReportsQueue();
            return true;
        }

        // Moderation review actions
        for (ModerationItem item : moderationList) {
            if (mx >= item.actionX && mx <= item.actionX + item.actionW && my >= item.actionY && my <= item.actionY + item.actionH) {
                dashboard.openReviewModal(item);
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p1 = datePickerHovered;
        datePickerHovered = (mx >= datePickerX && mx <= datePickerX + datePickerW && my >= datePickerY && my <= datePickerY + datePickerH);
        if (datePickerHovered != p1) changed = true;

        boolean p2 = viewAllReportsHovered;
        viewAllReportsHovered = (mx >= viewAllReportsX && mx <= viewAllReportsX + viewAllReportsW && my >= viewAllReportsY && my <= viewAllReportsY + viewAllReportsH);
        if (viewAllReportsHovered != p2) changed = true;

        for (ModerationItem item : moderationList) {
            boolean was = item.actionHovered;
            item.actionHovered = (mx >= item.actionX && mx <= item.actionX + item.actionW && my >= item.actionY && my <= item.actionY + item.actionH);
            if (item.actionHovered != was) changed = true;
        }

        return changed;
    }
}

