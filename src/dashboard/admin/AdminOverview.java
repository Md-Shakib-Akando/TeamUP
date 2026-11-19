import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Overview Page for TeamUp Admin Dashboard.
 * Fully responsive across all viewports (Mobile 360px up to 4K 2560px).
 * Includes complete Platform Analytics & Statistics suite:
 * 1. Total Users & Projects (Real-time KPI metrics, growth trends, active ratios)
 * 2. Monthly User Growth (Interactive area curve chart, acquisition velocity, net new registrations)
 * 3. Project Success Rate (Success gauge, milestone delivery breakdown, in-flight pipeline)
 * 4. Most Popular Skills (Ranked skill demand bars, student counts, category tags)
 * 5. Active Teams Statistics (Team fill ratio, collaboration requests, capacity distribution)
 * 6. Pending Moderation Queue & Recent Platform Activity Feed.
 *
 * Fully dynamic, interactive, and reactive with AdminDataStore.
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

    public static class SkillRowHitbox {
        public String name;
        public int count;
        public int x, y, w, h;
        public boolean hovered = false;

        public SkillRowHitbox(String name, int count, int x, int y, int w, int h) {
            this.name = name;
            this.count = count;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private final List<ModerationItem> moderationList = new ArrayList<>();
    private final List<ActivityItem> activityList = new ArrayList<>();
    private final List<SkillRowHitbox> skillHitboxes = new ArrayList<>();

    // Active teams explore button
    private int exploreTeamsBtnX, exploreTeamsBtnY, exploreTeamsBtnW = 155, exploreTeamsBtnH = 28;
    private boolean exploreTeamsHovered = false;

    // View all reports button
    private int viewAllReportsX, viewAllReportsY, viewAllReportsW = 145, viewAllReportsH = 34;
    private boolean viewAllReportsHovered = false;

    // Refresh live button
    private int refreshBtnX, refreshBtnY, refreshBtnW = 120, refreshBtnH = 30;
    private boolean refreshHovered = false;

    // Chart month points coordinates for tooltip
    private final int[] chartMonthPointsX = new int[AdminDataStore.GROWTH_MONTHS];
    private final int[] chartMonthPointsY = new int[AdminDataStore.GROWTH_MONTHS];

    public AdminOverview() {
        syncFromStore();
    }

    public void syncFromStore() {
        moderationList.clear();
        for (AdminDataStore.ReportModel r : AdminDataStore.getInstance().getAllReports()) {
            if (!"Resolved".equalsIgnoreCase(r.status)) {
                moderationList.add(new ModerationItem(r.id, r.title, r.type, r.reporter, r.date, r.status));
                if (moderationList.size() >= 4) break;
            }
        }

        activityList.clear();
        List<AdminDataStore.AuditLogModel> logs = AdminDataStore.getInstance().getAllAuditLogs();
        for (int i = 0; i < Math.min(4, logs.size()); i++) {
            AdminDataStore.AuditLogModel l = logs.get(i);
            int iconType = 0;
            String actLower = l.action.toLowerCase();
            if (actLower.contains("suspend") || actLower.contains("security")) iconType = 1;
            else if (actLower.contains("join") || actLower.contains("role") || actLower.contains("user")) iconType = 2;
            else if (actLower.contains("resolve") || actLower.contains("removed") || actLower.contains("created") || actLower.contains("feedback") || actLower.contains("review")) iconType = 3;
            activityList.add(new ActivityItem(l.actorName, l.action + " " + l.target, l.timestampLine1, iconType));
        }
    }

    public List<ModerationItem> getModerationList() {
        return moderationList;
    }

    public int getComputedHeight(int width) {
        int padX = (width < 600) ? 16 : 28;
        int w = width - padX * 2;
        int totalH = 24; // top padding

        // 1. Header Section
        totalH += 56;
        totalH += 20; // gap

        // 2. KPI Cards Row
        int kpiH = 124;
        int kpiGap = 16;
        if (w >= 1050) {
            totalH += kpiH;
        } else if (w >= 620) {
            totalH += kpiH * 2 + kpiGap;
        } else {
            totalH += kpiH * 4 + kpiGap * 3;
        }
        totalH += 22; // gap

        // 3. Grid Row 1 (User Growth & Project Success)
        int row1H = 310;
        int chartGap = 18;
        if (w >= 920) {
            totalH += row1H;
        } else {
            totalH += row1H * 2 + chartGap;
        }
        totalH += 22; // gap

        // 4. Grid Row 2 (Popular Skills & Active Teams)
        int row2H = 320;
        if (w >= 920) {
            totalH += row2H;
        } else {
            int teamsH = (w < 440) ? 410 : row2H;
            totalH += row2H + teamsH + chartGap;
        }
        totalH += 24; // gap

        // 5. Grid Row 3 (Pending Moderation & Recent Activity)
        int bottomH = 340;
        int splitGap = 18;
        if (w >= 920) {
            totalH += bottomH;
        } else {
            totalH += bottomH + 310 + splitGap;
        }
        totalH += 24; // gap

        // 6. Footer
        totalH += 36;
        return totalH;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = (width < 600) ? 16 : 28;
        int curY = y + 24;
        int contentW = width - padX * 2;
        skillHitboxes.clear();
        syncFromStore();

        // 1. Header Section with Time Range & Export
        curY = paintHeaderSection(g, x + padX, curY, contentW, dashboard);
        curY += 20;

        // 2. Row of 4 KPI Metric Cards (Total Users & Projects, Success Rate, Active Teams)
        curY = paintKpiCardsRow(g, x + padX, curY, contentW);
        curY += 22;

        // 3. Grid Row 1: Monthly User Growth & Project Success Rate
        int chartGap = 18;
        if (contentW >= 920) {
            int halfW = (contentW - chartGap) / 2;
            int row1H = 310;
            paintUserGrowthCard(g, x + padX, curY, halfW, row1H);
            paintProjectSuccessCard(g, x + padX + halfW + chartGap, curY, halfW, row1H);
            curY += row1H + 22;
        } else {
            int row1H = 310;
            paintUserGrowthCard(g, x + padX, curY, contentW, row1H);
            curY += row1H + chartGap;
            paintProjectSuccessCard(g, x + padX, curY, contentW, row1H);
            curY += row1H + 22;
        }

        // 4. Grid Row 2: Most Popular Skills & Active Teams Statistics
        if (contentW >= 920) {
            int halfW = (contentW - chartGap) / 2;
            int row2H = 320;
            paintPopularSkillsCard(g, x + padX, curY, halfW, row2H);
            paintActiveTeamsCard(g, x + padX + halfW + chartGap, curY, halfW, row2H, dashboard);
            curY += row2H + 24;
        } else {
            int row2H = 320;
            paintPopularSkillsCard(g, x + padX, curY, contentW, row2H);
            curY += row2H + chartGap;
            int teamsH = (contentW < 440) ? 410 : row2H;
            paintActiveTeamsCard(g, x + padX, curY, contentW, teamsH, dashboard);
            curY += teamsH + 24;
        }

        // 5. Grid Row 3: Pending Moderation & Recent Activity
        int splitGap = 18;
        if (contentW >= 920) {
            int modW = Math.round(contentW * 0.63f) - splitGap / 2;
            int actW = contentW - modW - splitGap;
            int bottomH = 340;
            paintPendingModeration(g, x + padX, curY, modW, bottomH, dashboard);
            paintRecentActivity(g, x + padX + modW + splitGap, curY, actW, bottomH);
            curY += bottomH + 24;
        } else {
            int bottomH = 340;
            paintPendingModeration(g, x + padX, curY, contentW, bottomH, dashboard);
            curY += bottomH + splitGap;
            paintRecentActivity(g, x + padX, curY, contentW, 310);
            curY += 310 + 24;
        }

        // 6. Footer Note
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString(AdminDataStore.getInstance().hasLiveStats()
                ? "TeamUp Analytics · Computed from MongoDB · Refreshes every 45s and after each admin action."
                : "TeamUp Analytics · Offline demo data — log in with the backend running for live metrics.", x + padX, curY + 12);
    }

    private int paintHeaderSection(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Overview & Analytics", x, y + 24);

        int titleW = hfm.stringWidth("Overview & Analytics");

        // Live / offline status pill badge
        AdminDataStore store = AdminDataStore.getInstance();
        boolean live = store.hasLiveStats();
        String pillText = live ? "LIVE METRICS" : "OFFLINE DEMO";
        int pillX = x + titleW + 12;
        int pillY = y + 7;
        int pillW = 104;
        int pillH = 22;
        RoundRectangle2D livePill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 11, 11);
        g.setColor(live ? new Color(0xDC, 0xFC, 0xE7) : AdminTheme.STATUS_GRAY_BG);
        g.fill(livePill);

        Color dot = live ? new Color(0x10, 0xB9, 0x81) : AdminTheme.TEXT_MUTED;
        g.setColor(dot);
        g.fillOval(pillX + 9, pillY + 7, 7, 7);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.drawString(pillText, pillX + 22, pillY + 15);

        // Sync / Refresh Live button on the right
        int refW = 124;
        int refH = 30;
        refreshBtnX = x + w - refW;
        refreshBtnY = y + 4;
        refreshBtnW = refW;
        refreshBtnH = refH;

        if (w >= 540) {
            RoundRectangle2D refBtn = new RoundRectangle2D.Float(refreshBtnX, refreshBtnY, refreshBtnW, refreshBtnH, 8, 8);
            g.setColor(refreshHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(refBtn);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(refBtn);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_DARK);
            String refText = "↻ Refresh Live";
            FontMetrics rfm = g.getFontMetrics();
            g.drawString(refText, refreshBtnX + (refreshBtnW - rfm.stringWidth(refText)) / 2, refreshBtnY + 19);

            // Last sync time, left of the refresh button
            String synced = store.getLastStatsSyncLabel();
            if (synced != null && w >= 760) {
                g.setFont(AdminTheme.FONT_REG_11);
                g.setColor(AdminTheme.TEXT_LIGHT);
                String syncText = "Synced " + synced;
                g.drawString(syncText, refreshBtnX - g.getFontMetrics().stringWidth(syncText) - 12, refreshBtnY + 19);
            }
        } else {
            refreshBtnX = -9999;
            refreshBtnY = -9999;
        }

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        String sub = "Platform performance, user growth, project delivery, skill distribution and team health at a glance.";
        if (w < 900) {
            sub = "User growth, project success & team health at a glance.";
        }
        g.drawString(sub, x, y + 48);

        return y + 56;
    }

    private int paintKpiCardsRow(Graphics2D g, int x, int y, int w) {
        int kpiH = 124;
        int kpiGap = 16;

        AdminDataStore store = AdminDataStore.getInstance();
        int totalUsers = store.getTotalUsersCount();
        int newUsers = store.getNewUsersThisMonth();
        int totalProjects = store.getTotalProjectsCount();
        int newProjects = store.getNewProjectsThisMonth();
        double successRate = store.getProjectSuccessRate();
        int activeTeams = store.getActiveTeamsCount();
        int activeProj = store.getActiveProjectsCount();
        int compProj = store.getCompletedProjectsCount();
        int started = store.getStartedProjectsCount();

        String[] titles = { "TOTAL USERS", "TOTAL PROJECTS", "PROJECT SUCCESS RATE", "ACTIVE TEAMS" };
        String[] values = {
                String.format("%,d", totalUsers),
                String.format("%,d", totalProjects),
                String.format("%.1f%%", successRate),
                String.format("%,d Teams", activeTeams) };
        String[] badges = {
                newUsers > 0 ? "↑ +" + newUsers + " this month" : "No new this month",
                newProjects > 0 ? "↑ +" + newProjects + " this month" : "No new this month",
                compProj + " of " + started + " delivered",
                store.getTeamFillRatio() + "% seats filled" };
        String[] subs = {
                String.format("%,d active · %,d suspended", store.getActiveUsersCount(), store.getSuspendedUsersCount()),
                activeProj + " Active · " + store.getRecruitingProjectsCount() + " Recruiting · " + compProj + " Done",
                "Completed vs. projects past recruiting",
                String.format("%.1f avg size · %,d requests", store.getAverageTeamSize(), store.collaborationRequests) };

        int cols = (w >= 1050) ? 4 : (w >= 620) ? 2 : 1;
        int kpiW = (w - kpiGap * (cols - 1)) / cols;
        int bottom = y;
        for (int i = 0; i < 4; i++) {
            int cx = x + (i % cols) * (kpiW + kpiGap);
            int cy = y + (i / cols) * (kpiH + kpiGap);
            paintElevatedKpiCard(g, cx, cy, kpiW, kpiH, titles[i], values[i], badges[i], subs[i], null, null, i);
            bottom = cy + kpiH;
        }
        return bottom;
    }

    private void paintElevatedKpiCard(Graphics2D g, int cx, int cy, int cw, int ch, String title, String val, String badge, String subtext, Color accent, Color tint, int type) {
        // One accent per card set: brand for the indicator line, green only for an upward trend.
        boolean upTrend = badge.startsWith("↑");
        tint = upTrend ? AdminTheme.STATUS_GREEN_BG : AdminTheme.STATUS_GRAY_BG;
        Color badgeText = upTrend ? AdminTheme.STATUS_GREEN_TEXT : AdminTheme.TEXT_MUTED;
        accent = AdminTheme.BRAND_PRIMARY;

        RoundRectangle2D card = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Top right trend pill badge (measured first so the title can make room for it)
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics bfm = g.getFontMetrics();
        int bw = bfm.stringWidth(badge) + 14;
        int bx = cx + cw - bw - 14;

        // Top left title, shortened with an ellipsis when it would run into the badge
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        FontMetrics tfm = g.getFontMetrics();
        int maxTitleW = bx - (cx + 16) - 8;
        String drawTitle = title;
        if (tfm.stringWidth(drawTitle) > maxTitleW) {
            while (drawTitle.length() > 4 && tfm.stringWidth(drawTitle + "…") > maxTitleW) {
                drawTitle = drawTitle.substring(0, drawTitle.length() - 1);
            }
            drawTitle = drawTitle.trim() + "…";
        }
        g.drawString(drawTitle, cx + 16, cy + 24);

        g.setFont(AdminTheme.FONT_BOLD_10);
        int by = cy + 12;
        RoundRectangle2D bpill = new RoundRectangle2D.Float(bx, by, bw, 20, 10, 10);
        g.setColor(tint);
        g.fill(bpill);
        g.setColor(badgeText);
        g.drawString(badge, bx + 7, by + 14);

        // Big value
        g.setFont(AdminTheme.FONT_METRIC);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(val, cx + 16, cy + 68);

        // Bottom subtext
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        FontMetrics sfm = g.getFontMetrics();
        int maxSubW = cw - 32;
        String drawSub = subtext;
        if (sfm.stringWidth(drawSub) > maxSubW) {
            while (drawSub.length() > 4 && sfm.stringWidth(drawSub + "…") > maxSubW) {
                drawSub = drawSub.substring(0, drawSub.length() - 1);
            }
            drawSub = drawSub.trim() + "…";
        }
        g.drawString(drawSub, cx + 16, cy + 96);

        // Micro accent indicator line
        g.setColor(accent);
        g.fillRoundRect(cx + 16, cy + 108, cw - 32, 3, 2, 2);
    }

    private void paintUserGrowthCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Title & Subtitle
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Monthly User Growth & Trajectory", x + 20, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Registered users (line) and new sign-ups per month (bars)", x + 20, y + 46);

        // Plot area
        int plotX = x + 48;
        int plotY = y + 74;
        int plotW = Math.max(80, w - 68);
        int plotH = 150;

        AdminDataStore store = AdminDataStore.getInstance();

        // Last 7 months of registrations, bucketed from each user's join date
        List<AdminDataStore.MonthPoint> growth = store.getMonthlyUserGrowth();
        String[] months = new String[growth.size()];
        int[] userCounts = new int[growth.size()];
        int[] newUsers = new int[growth.size()];
        int peakTotal = 0;
        int peakNew = 0;
        for (int i = 0; i < growth.size(); i++) {
            AdminDataStore.MonthPoint mp = growth.get(i);
            months[i] = mp.label;
            userCounts[i] = mp.total;
            newUsers[i] = mp.newUsers;
            peakTotal = Math.max(peakTotal, mp.total);
            peakNew = Math.max(peakNew, mp.newUsers);
        }
        int maxUserVal = niceAxisMax(peakTotal);

        // Y-axis gridlines & labels
        String[] yLabels = {
            formatMetricVal(maxUserVal),
            formatMetricVal(Math.round(maxUserVal * 0.75f)),
            formatMetricVal(Math.round(maxUserVal * 0.50f)),
            formatMetricVal(Math.round(maxUserVal * 0.25f)),
            "0"
        };
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

        // X points calculation
        int numPoints = months.length;
        for (int i = 0; i < numPoints; i++) {
            chartMonthPointsX[i] = plotX + Math.round(i * (plotW / (float) (numPoints - 1)));
            float frac = userCounts[i] / (float) maxUserVal;
            chartMonthPointsY[i] = plotY + plotH - Math.round(plotH * frac);
        }

        // Vertical bars for monthly new additions
        int barW = Math.max(6, Math.min(12, plotW / 24));
        int maxNewUser = Math.max(1, peakNew);
        for (int i = 0; i < numPoints; i++) {
            if (newUsers[i] == 0) continue;
            int bx = chartMonthPointsX[i] - barW / 2;
            float newFrac = Math.min(1.0f, newUsers[i] / (float) maxNewUser);
            int bH = Math.round(plotH * 0.45f * newFrac);
            int by = plotY + plotH - bH;

            RoundRectangle2D bar = new RoundRectangle2D.Float(bx, by, barW, bH, 4, 4);
            g.setColor(new Color(0x8B, 0x5C, 0xF6, 50));
            g.fill(bar);
        }

        // Smooth area curve underneath
        GeneralPath areaPath = new GeneralPath();
        areaPath.moveTo(chartMonthPointsX[0], plotY + plotH);
        areaPath.lineTo(chartMonthPointsX[0], chartMonthPointsY[0]);

        for (int i = 0; i < numPoints - 1; i++) {
            float x1 = chartMonthPointsX[i];
            float y1 = chartMonthPointsY[i];
            float x2 = chartMonthPointsX[i + 1];
            float y2 = chartMonthPointsY[i + 1];

            float cx1 = x1 + (x2 - x1) * 0.5f;
            float cy1 = y1;
            float cx2 = x1 + (x2 - x1) * 0.5f;
            float cy2 = y2;
            areaPath.curveTo(cx1, cy1, cx2, cy2, x2, y2);
        }
        areaPath.lineTo(chartMonthPointsX[numPoints - 1], plotY + plotH);
        areaPath.closePath();

        GradientPaint grad = new GradientPaint(
                0, plotY, new Color(0xEA, 0x4C, 0x89, 70),
                0, plotY + plotH, new Color(0xEA, 0x4C, 0x89, 5)
        );
        g.setPaint(grad);
        g.fill(areaPath);

        // Smooth curve stroke
        GeneralPath strokePath = new GeneralPath();
        strokePath.moveTo(chartMonthPointsX[0], chartMonthPointsY[0]);
        for (int i = 0; i < numPoints - 1; i++) {
            float x1 = chartMonthPointsX[i];
            float y1 = chartMonthPointsY[i];
            float x2 = chartMonthPointsX[i + 1];
            float y2 = chartMonthPointsY[i + 1];

            float cx1 = x1 + (x2 - x1) * 0.5f;
            float cy1 = y1;
            float cx2 = x1 + (x2 - x1) * 0.5f;
            float cy2 = y2;
            strokePath.curveTo(cx1, cy1, cx2, cy2, x2, y2);
        }
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(strokePath);

        // Points & Labels
        for (int i = 0; i < numPoints; i++) {
            int px = chartMonthPointsX[i];
            int py = chartMonthPointsY[i];

            g.setColor(Color.WHITE);
            g.fillOval(px - 5, py - 5, 10, 10);
            g.setColor(AdminTheme.BRAND_PRIMARY);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(px - 5, py - 5, 10, 10);

            g.setFont(AdminTheme.FONT_REG_10);
            g.setColor(AdminTheme.TEXT_LIGHT);
            int mw = yfm.stringWidth(months[i]);
            g.drawString(months[i], px - mw / 2, plotY + plotH + 20);
        }

        // Summary footer inside card
        int footY = y + h - 18;
        g.setFont(AdminTheme.FONT_BOLD_11);
        FontMetrics ffm = g.getFontMetrics();
        double engagement = store.getEngagementRate();
        String totalText = "● " + String.format("%,d Total Users", store.getTotalUsersCount());
        String newText = "■ +" + newUsers[numPoints - 1] + " this month";
        String engText = engagement >= 0 ? String.format("↑ %.1f%% in a team", engagement) : null;
        int fx = x + 20;
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString(totalText, fx, footY);
        fx += ffm.stringWidth(totalText) + 18;
        if (w >= 440) {
            g.setColor(AdminTheme.BRAND_SECONDARY);
            g.drawString(newText, fx, footY);
            fx += ffm.stringWidth(newText) + 18;
        }
        if (engText != null && fx + ffm.stringWidth(engText) <= x + w - 16) {
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString(engText, fx, footY);
        }
    }

    private void paintProjectSuccessCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Project Success Rate & Lifecycle", x + 20, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Delivered projects vs. everything past recruiting", x + 20, y + 46);

        AdminDataStore store = AdminDataStore.getInstance();
        int completed = store.getCompletedProjectsCount();
        int active = store.getActiveProjectsCount();
        int recruiting = store.getRecruitingProjectsCount();
        int review = store.getUnderReviewProjectsCount();
        int total = completed + active + recruiting + review;
        boolean noProjects = (total == 0);
        if (noProjects) total = 1;
        int msDone = store.getMilestonesCompleted();
        int msTotal = store.getMilestonesTotal();

        double rate = store.getProjectSuccessRate();
        int arcDeg = Math.min(260, (int) Math.round(260.0 * (rate / 100.0)));

        int curY = y + 78;
        int gaugeD = (w < 460) ? 84 : 100;
        int gaugeX = x + 24;
        int gaugeY = curY + 6;

        // Background track
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawArc(gaugeX, gaugeY, gaugeD, gaugeD, -40, 260);

        // Success arc (Dynamic)
        if (arcDeg > 0) {
            g.setColor(new Color(0x10, 0xB9, 0x81));
            g.drawArc(gaugeX, gaugeY, gaugeD, gaugeD, 220 - arcDeg, arcDeg);
        }

        // Inside gauge text
        g.setFont((w < 460) ? AdminTheme.FONT_BOLD_16 : AdminTheme.FONT_BOLD_20);
        g.setColor(AdminTheme.TEXT_DARK);
        String rateStr = String.format("%.1f%%", rate);
        FontMetrics rfm = g.getFontMetrics();
        g.drawString(rateStr, gaugeX + (gaugeD - rfm.stringWidth(rateStr)) / 2, gaugeY + (gaugeD / 2) - 2);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_MUTED);
        String subStr = "SUCCESS";
        FontMetrics sfm = g.getFontMetrics();
        g.drawString(subStr, gaugeX + (gaugeD - sfm.stringWidth(subStr)) / 2, gaugeY + (gaugeD / 2) + 14);

        // Pipeline layout
        int pipeX = gaugeX + gaugeD + ((w < 460) ? 16 : 28);
        int pipeW = w - (pipeX - x) - 18;

        paintPipelineRow(g, pipeX, curY + 4, pipeW, "Delivered", completed + (completed == 1 ? " project" : " projects"), sharePct(completed, total), new Color(0x10, 0xB9, 0x81));
        paintPipelineRow(g, pipeX, curY + 34, pipeW, "Active", active + " in progress", sharePct(active, total), AdminTheme.BRAND_PRIMARY);
        paintPipelineRow(g, pipeX, curY + 64, pipeW, "Recruiting", recruiting + " open teams", sharePct(recruiting, total), AdminTheme.CHART_MUTED);
        paintPipelineRow(g, pipeX, curY + 94, pipeW, "In Review", review + " in review", sharePct(review, total), AdminTheme.BRAND_AMBER);
        if (msTotal > 0) {
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(msDone + " of " + msTotal + " milestones completed", pipeX + 14, curY + 136);
        }

        // Segmented Progress Bar
        int segY = y + h - 56;
        int segW = w - 40;
        int segH = 12;
        RoundRectangle2D segBg = new RoundRectangle2D.Float(x + 20, segY, segW, segH, 6, 6);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.fill(segBg);

        float compFrac = (float) completed / total;
        float actFrac = (float) active / total;
        float recFrac = (float) recruiting / total;
        float revFrac = (float) review / total;

        int cW = Math.round(segW * compFrac);
        int aW = Math.round(segW * actFrac);
        int rW = Math.round(segW * recFrac);
        int rvW = noProjects ? 0 : Math.max(0, segW - cW - aW - rW); // remainder, so the bar fills exactly

        int barCurX = x + 20;
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.fillRoundRect(barCurX, segY, cW, segH, 4, 4);
        barCurX += cW;

        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.fillRoundRect(barCurX, segY, aW, segH, 4, 4);
        barCurX += aW;

        g.setColor(AdminTheme.CHART_MUTED);
        g.fillRoundRect(barCurX, segY, rW, segH, 4, 4);
        barCurX += rW;

        g.setColor(AdminTheme.BRAND_AMBER);
        g.fillRoundRect(barCurX, segY, rvW, segH, 4, 4);

        // Footnote below progress bar
        int legY = y + h - 22;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String legText = "■ Delivered (" + completed + ")   ■ Active (" + active + ")   ■ Recruiting (" + recruiting + ")   ■ Review (" + review + ")";
        if (w < 440) {
            legText = "■ Del (" + completed + ")  ■ Act (" + active + ")  ■ Rec (" + recruiting + ")";
        }
        g.drawString(legText, x + 20, legY);
    }

    private static String sharePct(int part, int total) {
        return total <= 0 ? "0%" : Math.round(part * 100.0 / total) + "% of all";
    }

    private void paintPipelineRow(Graphics2D g, int px, int py, int pw, String label, String count, String badge, Color c) {
        g.setColor(c);
        g.fillOval(px, py + 3, 8, 8);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(label, px + 14, py + 11);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        int countX = (pw < 240) ? px + 85 : px + 110;
        g.drawString(count, countX, py + 11);

        if (pw >= 220) {
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(c);
            FontMetrics bfm = g.getFontMetrics();
            g.drawString(badge, px + pw - bfm.stringWidth(badge) - 4, py + 11);
        }
    }

    private void paintPopularSkillsCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Most Popular Skills & Competencies", x + 20, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Skills most often listed on student profiles", x + 20, y + 46);

        List<AdminDataStore.SkillStat> allSkills = AdminDataStore.getInstance().getTopSkills(5);
        int curY = y + 76;
        int rowH = 44;
        int maxUsers = 1;
        for (AdminDataStore.SkillStat s : allSkills) {
            if (s.count > maxUsers) maxUsers = s.count;
        }

        if (allSkills.isEmpty()) {
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No skills listed on any student profile yet.", x + 20, y + 100);
        }

        Color[] barColors = { AdminTheme.BRAND_PRIMARY }; // single series: one colour

        for (int i = 0; i < Math.min(5, allSkills.size()); i++) {
            AdminDataStore.SkillStat skill = allSkills.get(i);

            // Rank #
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("#" + (i + 1), x + 20, curY + 16);

            // Skill Name
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(skill.name, x + 44, curY + 16);

            // Progress Bar Track
            int trackX = (w < 400) ? x + 125 : x + 150;
            int trackW = Math.max(40, w - (trackX - x) - 75);
            int barWidth = Math.max(12, Math.min(trackW, Math.round(trackW * (skill.count / (float) maxUsers))));

            RoundRectangle2D trk = new RoundRectangle2D.Float(trackX, curY + 6, trackW, 12, 6, 6);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(trk);

            // Progress Fill
            RoundRectangle2D fill = new RoundRectangle2D.Float(trackX, curY + 6, barWidth, 12, 6, 6);
            g.setColor(barColors[i % barColors.length]);
            g.fill(fill);

            // User Count
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_DARK);
            String uStr = String.format("%,d", skill.count);
            g.drawString(uStr, x + w - 65, curY + 16);

            // Interactive hitbox
            skillHitboxes.add(new SkillRowHitbox(skill.name, skill.count, x + 16, curY - 2, w - 32, 26));

            curY += rowH;
        }

        // Footer Note
        int footY = y + h - 18;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Counted from student profiles · Click a skill for details.", x + 20, footY);
    }

    private void paintActiveTeamsCard(Graphics2D g, int x, int y, int w, int h, AdminDashboard dashboard) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Active Teams Collaboration Statistics", x + 20, y + 28);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("Team sizes, open seats, matching requests & task progress", x + 20, y + 46);

        // 4 Tiles
        int gridX = x + 20;
        int gridY = y + 72;
        int tileGap = 10;
        boolean stackTiles = (w < 440);
        AdminDataStore store = AdminDataStore.getInstance();

        int teamCount = store.getActiveTeamsCount();
        String teamsStr = teamCount + (teamCount == 1 ? " Team" : " Teams");
        String compStr = String.format("%.1f / %.1f Members", store.getAverageTeamSize(), store.getAverageTeamCapacity());
        String reqStr = String.format("%,d Requests", store.collaborationRequests);
        String velStr = store.getSprintVelocity() + "%";
        String teamsSub = "2+ members, project not done";
        String compSub = store.getTeamFillRatio() + "% of seats filled";
        String reqSub = String.format("%,d pending · %.0f%% accepted", store.getPendingCollaborationRequests(), store.getRequestAcceptanceRate());
        String velSub = String.format("%,d of %,d tasks done", store.getTasksDone(), store.getTasksTotal());

        if (!stackTiles) {
            int tileW = (w - 40 - tileGap) / 2;
            int tileH = 68;

            paintMicroTile(g, gridX, gridY, tileW, tileH, "ACTIVE TEAMS", teamsStr, teamsSub, AdminTheme.BRAND_PRIMARY);
            paintMicroTile(g, gridX + tileW + tileGap, gridY, tileW, tileH, "AVG COMPOSITION", compStr, compSub, AdminTheme.BRAND_SECONDARY);
            paintMicroTile(g, gridX, gridY + tileH + tileGap, tileW, tileH, "MATCHING REQUESTS", reqStr, reqSub, AdminTheme.BRAND_AMBER);
            paintMicroTile(g, gridX + tileW + tileGap, gridY + tileH + tileGap, tileW, tileH, "TASK COMPLETION", velStr, velSub, new Color(0x10, 0xB9, 0x81));
        } else {
            int tileW = w - 40;
            int tileH = 46;
            paintMicroTileCompact(g, gridX, gridY, tileW, tileH, "ACTIVE TEAMS", teamsStr, AdminTheme.BRAND_PRIMARY);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6), tileW, tileH, "AVG COMPOSITION", compStr, AdminTheme.BRAND_SECONDARY);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6) * 2, tileW, tileH, "MATCHING REQUESTS", reqStr, AdminTheme.BRAND_AMBER);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6) * 3, tileW, tileH, "TASK COMPLETION", velStr, new Color(0x10, 0xB9, 0x81));
        }

        // Capacity section
        int capY = stackTiles ? gridY + (46 + 6) * 4 + 8 : gridY + (68 + tileGap) * 2 + 10;
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Team Capacity Distribution:", gridX, capY + 12);

        // Explore teams button removed as requested
        exploreTeamsBtnX = -9999;
        exploreTeamsBtnY = -9999;
        exploreTeamsBtnW = 0;
        exploreTeamsBtnH = 0;

        // Dynamic Segmented Capacity Bar
        int barY = capY + 26;
        int barW = w - 40;
        int barH = 10;

        int[] dist = store.getTeamCapacityDistribution(); // {full, open seats, over limit}
        int teamTotal = dist[0] + dist[1] + dist[2];
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.fill(new RoundRectangle2D.Float(gridX, barY, barW, barH, 4, 4));

        Color[] segColors = { new Color(0x10, 0xB9, 0x81), AdminTheme.CHART_MUTED, AdminTheme.BRAND_AMBER };
        int[] pcts = new int[3];
        int segX = gridX;
        for (int i = 0; i < 3; i++) {
            pcts[i] = teamTotal == 0 ? 0 : (int) Math.round(dist[i] * 100.0 / teamTotal);
            if (dist[i] == 0) continue;
            int segW = Math.round(barW * dist[i] / (float) teamTotal);
            boolean lastNonEmpty = true;
            for (int k = i + 1; k < 3; k++) if (dist[k] > 0) lastNonEmpty = false;
            if (lastNonEmpty) segW = gridX + barW - segX; // absorb rounding
            g.setColor(segColors[i]);
            g.fill(new RoundRectangle2D.Float(segX, barY, segW, barH, 4, 4));
            segX += segW;
        }

        int footY = y + h - 18;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String capLegend = "■ Full (" + dist[0] + " · " + pcts[0] + "%)   ■ Open seats (" + dist[1] + " · " + pcts[1]
                + "%)   ■ Over " + store.maxTeamSize + " members (" + dist[2] + ")";
        if (w < 440) {
            capLegend = "■ Full " + dist[0] + "   ■ Open " + dist[1] + "   ■ Over " + dist[2];
        }
        if (teamTotal == 0) {
            capLegend = "No active teams yet.";
        }
        g.drawString(capLegend, gridX, footY);
    }

    private void paintMicroTile(Graphics2D g, int tx, int ty, int tw, int th, String title, String val, String sub, Color c) {
        c = AdminTheme.BRAND_PRIMARY; // uniform accent stripe across tiles
        RoundRectangle2D tile = new RoundRectangle2D.Float(tx, ty, tw, th, 8, 8);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(tile);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(tile);

        g.setColor(c);
        g.fillRect(tx, ty, 4, th);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(title, tx + 12, ty + 18);

        g.setFont(AdminTheme.FONT_BOLD_15);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(val, tx + 12, ty + 40);

        g.setFont(AdminTheme.FONT_REG_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString(sub, tx + 12, ty + 56);
    }

    private void paintMicroTileCompact(Graphics2D g, int tx, int ty, int tw, int th, String title, String val, Color c) {
        c = AdminTheme.BRAND_PRIMARY; // uniform accent stripe across tiles
        RoundRectangle2D tile = new RoundRectangle2D.Float(tx, ty, tw, th, 8, 8);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(tile);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(tile);

        g.setColor(c);
        g.fillRect(tx, ty, 4, th);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(title, tx + 12, ty + 18);

        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(val, tx + 12, ty + 36);
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
        g.drawString("Pending moderation queue", x + 20, y + 30);

        // "View all reports" Button
        int pendingCount = AdminDataStore.getInstance().getPendingReportsCount();
        String viewAllBtnText = "View all " + pendingCount + " reports";
        viewAllReportsW = Math.min(145, Math.max(120, w / 3));
        viewAllReportsH = 32;
        viewAllReportsX = x + w - viewAllReportsW - 20;
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

        boolean compactCols = (w < 580);
        int colContent = x + 20;
        int colReporter = compactCols ? x + Math.round(w * 0.45f) : x + Math.round(w * 0.42f);
        int colDate = compactCols ? -1 : x + Math.round(w * 0.60f);
        int colStatus = compactCols ? x + Math.round(w * 0.68f) : x + Math.round(w * 0.75f);
        int colAction = compactCols ? x + Math.round(w * 0.84f) : x + Math.round(w * 0.88f);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Reported content", colContent, tableTopY + 22);
        g.drawString("Reporter", colReporter, tableTopY + 22);
        if (!compactCols) g.drawString("Date", colDate, tableTopY + 22);
        g.drawString("Status", colStatus, tableTopY + 22);
        g.drawString("Action", colAction, tableTopY + 22);

        // Rows
        int rowH = 54;
        int curRowY = tableTopY + 34;

        if (moderationList.isEmpty()) {
            g.setFont(AdminTheme.FONT_REG_13);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("✓ All reports resolved. Moderation queue is clear!", x + 24, tableTopY + 68);
        }

        for (int i = 0; i < moderationList.size(); i++) {
            ModerationItem item = moderationList.get(i);

            // Title
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            String tStr = item.title;
            int maxTW = colReporter - colContent - 14;
            FontMetrics tfm = g.getFontMetrics();
            if (tfm.stringWidth(tStr) > maxTW && tStr.length() > 8) {
                while (tStr.length() > 6 && tfm.stringWidth(tStr + "…") > maxTW) {
                    tStr = tStr.substring(0, tStr.length() - 1);
                }
                tStr += "…";
            }
            g.drawString(tStr, colContent, curRowY + 22);

            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(item.id + " · " + item.type, colContent, curRowY + 38);

            // Reporter
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(item.reporter, colReporter, curRowY + 30);

            // Date (if wide)
            if (!compactCols && colDate > 0) {
                g.setFont(AdminTheme.FONT_REG_11);
                g.setColor(AdminTheme.TEXT_LIGHT);
                g.drawString(item.date, colDate, curRowY + 30);
            }

            // Status Pill
            int pillBadgeW = item.status.equals("In review") ? 66 : 60;
            int pillBadgeH = 22;
            int pillBadgeY = curRowY + 16;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(colStatus, pillBadgeY, pillBadgeW, pillBadgeH, 10, 10);
            boolean isReview = item.status.equals("In review");
            g.setColor(isReview ? AdminTheme.STATUS_REVIEW_BG : AdminTheme.STATUS_PENDING_BG);
            g.fill(sPill);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(isReview ? AdminTheme.STATUS_REVIEW_TXT : AdminTheme.STATUS_PENDING_TXT);
            g.drawString(item.status, colStatus + (isReview ? 9 : 11), pillBadgeY + 15);

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
        g.drawString("Recent platform activity", x + 20, y + 30);

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
            else if (actLower.contains("resolve") || actLower.contains("removed") || actLower.contains("created") || actLower.contains("feedback") || actLower.contains("review")) iconType = 3;
            activityList.add(new ActivityItem(l.actorName, l.action + " " + l.target, l.timestampLine1, iconType));
        }

        if (activityList.isEmpty()) {
            g.setFont(AdminTheme.FONT_REG_13);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No recent platform activity recorded yet.", x + 24, y + 80);
        }

        for (ActivityItem act : activityList) {
            paintActivityIcon(g, x + 20, curY + 2, act.iconType);

            // Actor + Action
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            FontMetrics afm = g.getFontMetrics();
            g.drawString(act.actor, x + 44, curY + 14);

            int actorW = afm.stringWidth(act.actor);
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);

            String actText = " " + act.action;
            int maxActW = w - (44 + actorW) - 16;
            FontMetrics mfm = g.getFontMetrics();
            if (mfm.stringWidth(actText) > maxActW && actText.length() > 8) {
                while (actText.length() > 6 && mfm.stringWidth(actText + "…") > maxActW) {
                    actText = actText.substring(0, actText.length() - 1);
                }
                actText += "…";
            }
            g.drawString(actText, x + 44 + actorW, curY + 14);

            // Time
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(act.time, x + 44, curY + 30);

            curY += rowH;
        }
    }

    private void paintActivityIcon(Graphics2D g, int ix, int iy, int type) {
        g.setStroke(new BasicStroke(1.5f));

        if (type == 0) {
            g.setColor(AdminTheme.BRAND_PRIMARY); // Pink pulse wave
            g.drawLine(ix, iy + 6, ix + 4, iy + 6);
            g.drawLine(ix + 4, iy + 6, ix + 7, iy);
            g.drawLine(ix + 7, iy, ix + 9, iy + 12);
            g.drawLine(ix + 9, iy + 12, ix + 12, iy + 6);
            g.drawLine(ix + 12, iy + 6, ix + 15, iy + 6);
        } else if (type == 1) {
            g.setColor(AdminTheme.BRAND_SECONDARY); // Violet shield
            g.drawRect(ix + 1, iy + 1, 12, 12);
            g.drawLine(ix + 7, iy + 1, ix + 7, iy + 13);
        } else if (type == 2) {
            g.setColor(AdminTheme.BRAND_ACCENT); // Cyan join
            g.drawOval(ix + 2, iy + 1, 6, 6);
            g.drawArc(ix, iy + 7, 10, 6, 0, 180);
        } else {
            g.setColor(AdminTheme.BRAND_MINT); // Mint checkmark
            g.drawLine(ix + 2, iy + 7, ix + 6, iy + 11);
            g.drawLine(ix + 6, iy + 11, ix + 13, iy + 3);
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 0. Refresh live button
        if (mx >= refreshBtnX && mx <= refreshBtnX + refreshBtnW && my >= refreshBtnY && my <= refreshBtnY + refreshBtnH) {
            if (dashboard != null) {
                dashboard.loadFromServer();
                dashboard.showToast("Synchronizing live metrics from backend...");
            }
            return true;
        }

        // 1. View all reports button
        if (mx >= viewAllReportsX && mx <= viewAllReportsX + viewAllReportsW && my >= viewAllReportsY && my <= viewAllReportsY + viewAllReportsH) {
            dashboard.openReportsQueue();
            return true;
        }

        // 3. Popular Skills row clicks
        for (SkillRowHitbox sh : skillHitboxes) {
            if (mx >= sh.x && mx <= sh.x + sh.w && my >= sh.y && my <= sh.y + sh.h) {
                dashboard.showToast("Skill: " + sh.name + " (" + String.format("%,d", sh.count) + (sh.count == 1 ? " student)" : " students)"));
                return true;
            }
        }

        // 4. Moderation review actions
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

        // Refresh button hover
        boolean pRef = refreshHovered;
        refreshHovered = (mx >= refreshBtnX && mx <= refreshBtnX + refreshBtnW && my >= refreshBtnY && my <= refreshBtnY + refreshBtnH);
        if (refreshHovered != pRef) changed = true;

        // View all reports hover
        boolean p2 = viewAllReportsHovered;
        viewAllReportsHovered = (mx >= viewAllReportsX && mx <= viewAllReportsX + viewAllReportsW && my >= viewAllReportsY && my <= viewAllReportsY + viewAllReportsH);
        if (viewAllReportsHovered != p2) changed = true;

        // Moderation items hover
        for (ModerationItem item : moderationList) {
            boolean was = item.actionHovered;
            item.actionHovered = (mx >= item.actionX && mx <= item.actionX + item.actionW && my >= item.actionY && my <= item.actionY + item.actionH);
            if (item.actionHovered != was) changed = true;
        }

        // Skills hover
        for (SkillRowHitbox sh : skillHitboxes) {
            boolean was = sh.hovered;
            sh.hovered = (mx >= sh.x && mx <= sh.x + sh.w && my >= sh.y && my <= sh.y + sh.h);
            if (sh.hovered != was) changed = true;
        }

        return changed;
    }

    /** Smallest "round" axis maximum (divisible into 4 integer ticks) that leaves headroom above v. */
    static int niceAxisMax(int v) {
        int target = Math.max(4, (int) Math.ceil(v * 1.1));
        int[] ladderTimesTen = { 10, 12, 15, 20, 25, 30, 40, 50, 60, 80 }; // 1, 1.2, 1.5 … 8 (× magnitude)
        long mag = 1;
        while (true) {
            for (int m : ladderTimesTen) {
                if ((m * mag) % 10 != 0) continue; // tick step must be a whole number
                long max = (m * mag / 10) * 4;
                if (max >= target) return (int) max;
            }
            mag *= 10;
        }
    }

    private String formatMetricVal(int v) {
        if (v >= 1000000) return String.format("%.1fM", v / 1000000.0);
        if (v >= 1000) {
            double k = v / 1000.0;
            if (k == Math.floor(k)) return String.format("%dk", (int) k);
            return String.format("%.1fk", k);
        }
        return String.valueOf(v);
    }
}
