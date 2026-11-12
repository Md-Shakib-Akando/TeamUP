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

    // Chart month points coordinates for tooltip
    private final int[] chartMonthPointsX = new int[7];
    private final int[] chartMonthPointsY = new int[7];

    public AdminOverview() {
        // Sample Moderation Queue
        moderationList.add(new ModerationItem("R-1048", "StudyBuddy AI conversation", "Message", "Priya Shah", "Oct 16, 2026", "Pending"));
        moderationList.add(new ModerationItem("R-1047", "SkillSwap Marketplace", "Project", "Maya Patel", "Oct 15, 2026", "In review"));
        moderationList.add(new ModerationItem("R-1046", "Unsolicited project invite", "Message", "Alex Morgan", "Oct 15, 2026", "Pending"));
        moderationList.add(new ModerationItem("R-1045", "User profile biography", "Profile", "Sam Chen", "Oct 14, 2026", "Pending"));

        // Recent Platform Activity
        activityList.add(new ActivityItem("Maya Patel", "created Local Food Network", "12 minutes ago", 0));
        activityList.add(new ActivityItem("Jordan Lee", "suspended Noah Williams", "38 minutes ago", 1));
        activityList.add(new ActivityItem("Alex Morgan", "joined Research Commons", "1 hour ago", 2));
        activityList.add(new ActivityItem("Sam Chen", "resolved report R-1044", "2 hours ago", 3));
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
        g.drawString("TeamUp Analytics Suite · Live synchronization with AdminDataStore · Real-time university metrics.", x + padX, curY + 12);
    }

    private int paintHeaderSection(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Overview & Analytics", x, y + 24);

        int titleW = hfm.stringWidth("Overview & Analytics");

        // Live status pill badge
        int pillX = x + titleW + 12;
        int pillY = y + 7;
        int pillW = 104;
        int pillH = 22;
        RoundRectangle2D livePill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 11, 11);
        g.setColor(new Color(0xDC, 0xFC, 0xE7));
        g.fill(livePill);

        // Green dot
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.fillOval(pillX + 9, pillY + 7, 7, 7);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.drawString("LIVE METRICS", pillX + 22, pillY + 15);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        String sub = "Real-time platform performance, user acquisition, project delivery velocity, skill distribution, and active team health.";
        if (w < 700) {
            sub = "Real-time performance, user acquisition, project success & team health.";
        }
        g.drawString(sub, x, y + 48);

        return y + 56;
    }

    private int paintKpiCardsRow(Graphics2D g, int x, int y, int w) {
        int kpiH = 124;
        int kpiGap = 16;

        AdminDataStore store = AdminDataStore.getInstance();
        int totalUsers = store.getTotalUsersCount();
        int totalProjects = store.getTotalProjectsCount();
        double successRate = store.getProjectSuccessRate();
        int activeTeams = store.getActiveTeamsCount();

        if (w >= 1050) {
            // 4 Cards across
            int kpiW = (w - kpiGap * 3) / 4;
            paintElevatedKpiCard(g, x, y, kpiW, kpiH, "TOTAL PLATFORM USERS", String.format("%,d", totalUsers), "↑ +14.8% MoM", "+1,620 new · 98.4% verified", AdminTheme.BRAND_PRIMARY, new Color(0xFD, 0xEE, 0xF4), 0);
            paintElevatedKpiCard(g, x + (kpiW + kpiGap), y, kpiW, kpiH, "TOTAL PROJECTS & INITIATIVES", String.format("%,d", totalProjects), "↑ +12.6% vs term", store.getActiveProjectsCount() + " Active · " + store.getCompletedProjectsCount() + " Completed", AdminTheme.BRAND_SECONDARY, new Color(0xEE, 0xF2, 0xFF), 1);
            paintElevatedKpiCard(g, x + (kpiW + kpiGap) * 2, y, kpiW, kpiH, "PROJECT SUCCESS RATE", String.format("%.1f%%", successRate), "★ Top Benchmark", "96 delivered on schedule", AdminTheme.BRAND_AMBER, new Color(0xFE, 0xF3, 0xC7), 2);
            paintElevatedKpiCard(g, x + (kpiW + kpiGap) * 3, y, kpiW, kpiH, "ACTIVE TEAMS COLLABORATING", String.format("%,d Teams", activeTeams), "⚡ 84% Fill Ratio", "4.6 avg size · " + store.collaborationRequests + " requests", AdminTheme.BRAND_ACCENT, new Color(0xEC, 0xFE, 0xFF), 3);
            return y + kpiH;
        } else if (w >= 620) {
            // 2 Columns x 2 Rows
            int kpiW = (w - kpiGap) / 2;
            paintElevatedKpiCard(g, x, y, kpiW, kpiH, "TOTAL PLATFORM USERS", String.format("%,d", totalUsers), "↑ +14.8% MoM", "+1,620 new · 98.4% verified", AdminTheme.BRAND_PRIMARY, new Color(0xFD, 0xEE, 0xF4), 0);
            paintElevatedKpiCard(g, x + kpiW + kpiGap, y, kpiW, kpiH, "TOTAL PROJECTS & INITIATIVES", String.format("%,d", totalProjects), "↑ +12.6% vs term", store.getActiveProjectsCount() + " Active · " + store.getCompletedProjectsCount() + " Completed", AdminTheme.BRAND_SECONDARY, new Color(0xEE, 0xF2, 0xFF), 1);

            int row2Y = y + kpiH + kpiGap;
            paintElevatedKpiCard(g, x, row2Y, kpiW, kpiH, "PROJECT SUCCESS RATE", String.format("%.1f%%", successRate), "★ Top Benchmark", "96 delivered on schedule", AdminTheme.BRAND_AMBER, new Color(0xFE, 0xF3, 0xC7), 2);
            paintElevatedKpiCard(g, x + kpiW + kpiGap, row2Y, kpiW, kpiH, "ACTIVE TEAMS COLLABORATING", String.format("%,d Teams", activeTeams), "⚡ 84% Fill Ratio", "4.6 avg size · " + store.collaborationRequests + " requests", AdminTheme.BRAND_ACCENT, new Color(0xEC, 0xFE, 0xFF), 3);
            return row2Y + kpiH;
        } else {
            // 1 Column x 4 Rows
            int kpiW = w;
            int curCardY = y;
            paintElevatedKpiCard(g, x, curCardY, kpiW, kpiH, "TOTAL PLATFORM USERS", String.format("%,d", totalUsers), "↑ +14.8% MoM", "+1,620 new · 98.4% verified", AdminTheme.BRAND_PRIMARY, new Color(0xFD, 0xEE, 0xF4), 0);
            curCardY += kpiH + kpiGap;
            paintElevatedKpiCard(g, x, curCardY, kpiW, kpiH, "TOTAL PROJECTS & INITIATIVES", String.format("%,d", totalProjects), "↑ +12.6% vs term", store.getActiveProjectsCount() + " Active · " + store.getCompletedProjectsCount() + " Completed", AdminTheme.BRAND_SECONDARY, new Color(0xEE, 0xF2, 0xFF), 1);
            curCardY += kpiH + kpiGap;
            paintElevatedKpiCard(g, x, curCardY, kpiW, kpiH, "PROJECT SUCCESS RATE", String.format("%.1f%%", successRate), "★ Top Benchmark", "96 delivered on schedule", AdminTheme.BRAND_AMBER, new Color(0xFE, 0xF3, 0xC7), 2);
            curCardY += kpiH + kpiGap;
            paintElevatedKpiCard(g, x, curCardY, kpiW, kpiH, "ACTIVE TEAMS COLLABORATING", String.format("%,d Teams", activeTeams), "⚡ 84% Fill Ratio", "4.6 avg size · " + store.collaborationRequests + " requests", AdminTheme.BRAND_ACCENT, new Color(0xEC, 0xFE, 0xFF), 3);
            return curCardY + kpiH;
        }
    }

    private void paintElevatedKpiCard(Graphics2D g, int cx, int cy, int cw, int ch, String title, String val, String badge, String subtext, Color accent, Color tint, int type) {
        RoundRectangle2D card = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Top left title
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String drawTitle = title;
        if (cw < 200 && title.length() > 16) {
            drawTitle = title.substring(0, 15) + "…";
        }
        g.drawString(drawTitle, cx + 16, cy + 24);

        // Top right trend pill badge
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics bfm = g.getFontMetrics();
        int bw = bfm.stringWidth(badge) + 14;
        int bx = cx + cw - bw - 14;
        int by = cy + 12;
        RoundRectangle2D bpill = new RoundRectangle2D.Float(bx, by, bw, 20, 10, 10);
        g.setColor(tint);
        g.fill(bpill);
        g.setColor(accent);
        g.drawString(badge, bx + 7, by + 14);

        // Big value
        g.setFont(AdminTheme.FONT_METRIC);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(val, cx + 16, cy + 68);

        // Bottom subtext
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String drawSub = subtext;
        if (cw < 240 && subtext.length() > 24) {
            drawSub = subtext.substring(0, 22) + "…";
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
        g.drawString("Cumulative student enrollment & active collaborative members", x + 20, y + 46);

        // Plot area
        int plotX = x + 48;
        int plotY = y + 74;
        int plotW = Math.max(80, w - 68);
        int plotH = 150;

        // Data arrays (May to Nov)
        String[] months = {"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov"};
        int[] userCounts = {7400, 8250, 9100, 10400, 11350, 12480, 13200};
        int[] newUsers = {650, 850, 850, 1300, 950, 1620, 720};
        int maxUserVal = 14000;

        // Y-axis gridlines & labels
        String[] yLabels = {"14k", "10.5k", "7k", "3.5k", "0"};
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
        for (int i = 0; i < numPoints; i++) {
            int bx = chartMonthPointsX[i] - barW / 2;
            float newFrac = newUsers[i] / 2000f;
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
        if (w >= 440) {
            g.setColor(AdminTheme.BRAND_PRIMARY);
            g.drawString("● 13,200 Total Users", x + 20, footY);
            g.setColor(AdminTheme.BRAND_SECONDARY);
            g.drawString("■ +1,620 New in Oct", x + 155, footY);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("↑ 89.4% Retention", x + Math.min(w - 120, 280), footY);
        } else {
            g.setColor(AdminTheme.BRAND_PRIMARY);
            g.drawString("● 13,200 Total", x + 20, footY);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("↑ 89.4% Retention", x + w - 120, footY);
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
        g.drawString("Milestone completion health & student team performance", x + 20, y + 46);

        AdminDataStore store = AdminDataStore.getInstance();
        int completed = store.getCompletedProjectsCount();
        int active = store.getActiveProjectsCount();
        int recruiting = store.getRecruitingProjectsCount();
        int review = store.getUnderReviewProjectsCount();
        int total = completed + active + recruiting + review;
        if (total == 0) total = 482;

        int curY = y + 78;
        int gaugeD = (w < 460) ? 84 : 100;
        int gaugeX = x + 24;
        int gaugeY = curY + 6;

        // Background track
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawArc(gaugeX, gaugeY, gaugeD, gaugeD, -40, 260);

        // Success arc (94.6% of 260 deg = ~246 deg)
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.drawArc(gaugeX, gaugeY, gaugeD, gaugeD, -40, 246);

        // Inside gauge text
        g.setFont((w < 460) ? AdminTheme.FONT_BOLD_16 : AdminTheme.FONT_BOLD_20);
        g.setColor(AdminTheme.TEXT_DARK);
        String rateStr = "94.6%";
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

        paintPipelineRow(g, pipeX, curY + 4, pipeW, "Delivered", completed + " projects", "96.2% on-time", new Color(0x10, 0xB9, 0x81));
        paintPipelineRow(g, pipeX, curY + 34, pipeW, "Active Sprints", active + " in-flight", "286 active", AdminTheme.BRAND_PRIMARY);
        paintPipelineRow(g, pipeX, curY + 64, pipeW, "Recruiting", recruiting + " open teams", "92 recruiting", AdminTheme.BRAND_SECONDARY);
        paintPipelineRow(g, pipeX, curY + 94, pipeW, "In Review", review + " in review", "8 pending", AdminTheme.BRAND_AMBER);

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
        int rvW = Math.max(4, Math.round(segW * revFrac));

        int barCurX = x + 20;
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.fillRoundRect(barCurX, segY, cW, segH, 4, 4);
        barCurX += cW;

        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.fillRoundRect(barCurX, segY, aW, segH, 4, 4);
        barCurX += aW;

        g.setColor(AdminTheme.BRAND_SECONDARY);
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
        g.drawString("Top talent demand & skills distribution across student profiles", x + 20, y + 46);

        List<AdminDataStore.SkillModel> allSkills = AdminDataStore.getInstance().getAllSkills();
        int curY = y + 76;
        int rowH = 44;
        int maxUsers = 1400;

        Color[] barColors = {
                AdminTheme.BRAND_PRIMARY,
                AdminTheme.BRAND_SECONDARY,
                new Color(0x10, 0xB9, 0x81),
                AdminTheme.BRAND_ACCENT,
                AdminTheme.BRAND_AMBER
        };

        for (int i = 0; i < Math.min(5, allSkills.size()); i++) {
            AdminDataStore.SkillModel skill = allSkills.get(i);

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
            int barWidth = Math.max(12, Math.min(trackW, Math.round(trackW * (skill.memberCount / (float) maxUsers))));

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
            String uStr = String.format("%,d", skill.memberCount);
            g.drawString(uStr, x + w - 65, curY + 16);

            // Interactive hitbox
            skillHitboxes.add(new SkillRowHitbox(skill.name, skill.memberCount, x + 16, curY - 2, w - 32, 26));

            curY += rowH;
        }

        // Footer Note
        int footY = y + h - 18;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Real-time data from Categories & Skills registry · Click any skill for insights.", x + 20, footY);
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
        g.drawString("Sprint velocity, composition fill rates & matching dynamics", x + 20, y + 46);

        // 4 Tiles
        int gridX = x + 20;
        int gridY = y + 72;
        int tileGap = 10;
        boolean stackTiles = (w < 440);
        AdminDataStore store = AdminDataStore.getInstance();

        if (!stackTiles) {
            int tileW = (w - 40 - tileGap) / 2;
            int tileH = 68;

            paintMicroTile(g, gridX, gridY, tileW, tileH, "ACTIVE TEAMS", store.getActiveTeamsCount() + " Teams", "In-flight sprint squads", AdminTheme.BRAND_PRIMARY);
            paintMicroTile(g, gridX + tileW + tileGap, gridY, tileW, tileH, "AVG COMPOSITION", "4.6 / 6 Members", "84% optimal fill", AdminTheme.BRAND_SECONDARY);
            paintMicroTile(g, gridX, gridY + tileH + tileGap, tileW, tileH, "MATCHING REQUESTS", String.format("%,d Requests", store.collaborationRequests), "+18.4% this term", AdminTheme.BRAND_AMBER);
            paintMicroTile(g, gridX + tileW + tileGap, gridY + tileH + tileGap, tileW, tileH, "SPRINT VELOCITY", "92.4%", "Milestones on track", new Color(0x10, 0xB9, 0x81));
        } else {
            int tileW = w - 40;
            int tileH = 46;
            paintMicroTileCompact(g, gridX, gridY, tileW, tileH, "ACTIVE TEAMS", store.getActiveTeamsCount() + " Teams", AdminTheme.BRAND_PRIMARY);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6), tileW, tileH, "AVG COMPOSITION", "4.6 / 6 Members", AdminTheme.BRAND_SECONDARY);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6) * 2, tileW, tileH, "MATCHING REQUESTS", String.format("%,d Requests", store.collaborationRequests), AdminTheme.BRAND_AMBER);
            paintMicroTileCompact(g, gridX, gridY + (tileH + 6) * 3, tileW, tileH, "SPRINT VELOCITY", "92.4%", new Color(0x10, 0xB9, 0x81));
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

        // Segmented Capacity Bar
        int barY = capY + 26;
        int barW = w - 40;
        int barH = 10;
        RoundRectangle2D b1 = new RoundRectangle2D.Float(gridX, barY, Math.round(barW * 0.68f), barH, 4, 4);
        g.setColor(new Color(0x10, 0xB9, 0x81)); // 68% optimal
        g.fill(b1);

        RoundRectangle2D b2 = new RoundRectangle2D.Float(gridX + Math.round(barW * 0.68f), barY, Math.round(barW * 0.24f), barH, 4, 4);
        g.setColor(AdminTheme.BRAND_SECONDARY); // 24% recruiting
        g.fill(b2);

        RoundRectangle2D b3 = new RoundRectangle2D.Float(gridX + Math.round(barW * 0.92f), barY, Math.round(barW * 0.08f), barH, 4, 4);
        g.setColor(AdminTheme.BRAND_AMBER); // 8% heavy
        g.fill(b3);

        int footY = y + h - 18;
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String capLegend = "■ Optimal (4-6 · 68%)   ■ Recruiting (1-3 · 24%)   ■ Heavy (>6 · 8%)";
        if (w < 440) {
            capLegend = "■ Opt 68%   ■ Rec 24%   ■ Hvy 8%";
        }
        g.drawString(capLegend, gridX, footY);
    }

    private void paintMicroTile(Graphics2D g, int tx, int ty, int tw, int th, String title, String val, String sub, Color c) {
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
        // 1. View all reports button
        if (mx >= viewAllReportsX && mx <= viewAllReportsX + viewAllReportsW && my >= viewAllReportsY && my <= viewAllReportsY + viewAllReportsH) {
            dashboard.openReportsQueue();
            return true;
        }

        // 3. Popular Skills row clicks
        for (SkillRowHitbox sh : skillHitboxes) {
            if (mx >= sh.x && mx <= sh.x + sh.w && my >= sh.y && my <= sh.y + sh.h) {
                dashboard.showToast("Skill: " + sh.name + " (" + String.format("%,d", sh.count) + " enrolled students)");
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
}
