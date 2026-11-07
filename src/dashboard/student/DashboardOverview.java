import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Handles the main scrollable overview content for TeamUp Student Dashboard.
 * Includes Hero welcome, profile completion banner, 4 stat cards, recommended projects,
 * suggested teammates, recent activity, and upcoming deadlines.
 */
public class DashboardOverview {

    // Hitboxes
    private int createProjBtnX, createProjBtnY, createProjBtnW, createProjBtnH;
    private boolean createProjBtnHovered = false;

    private int compProfileBtnX, compProfileBtnY, compProfileBtnW, compProfileBtnH;
    private boolean compProfileBtnHovered = false;

    private int findTeammatesBtnX, findTeammatesBtnY, findTeammatesBtnW, findTeammatesBtnH;
    private boolean findTeammatesBtnHovered = false;

    private int exploreAllBtnX, exploreAllBtnY, exploreAllBtnW, exploreAllBtnH;
    private int findMoreBtnX, findMoreBtnY, findMoreBtnW, findMoreBtnH;
    private int viewAllBtnX, viewAllBtnY, viewAllBtnW, viewAllBtnH;
    private int allTasksBtnX, allTasksBtnY, allTasksBtnW, allTasksBtnH;
    private int viewMessagesBtnX, viewMessagesBtnY, viewMessagesBtnW, viewMessagesBtnH;
    private int unreadMsgBtnX, unreadMsgBtnY, unreadMsgBtnW, unreadMsgBtnH;

    private int totalContentHeight = 1100;

    public int getTotalContentHeight() {
        return totalContentHeight;
    }

    public void paint(Graphics2D g, int startX, int startY, int width, int height, StudentDashboard dashboard) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Welcome Title & Subtitle + "+ Create Project" button
        g.setFont(DashboardTheme.FONT_HEADING);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Welcome back, Alex!", startX + padX, curY + 26);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("A little progress, a great team. Here's what's happening today.", startX + padX, curY + 48);

        // "+ Create Project" Primary Pill Button
        createProjBtnW = 136;
        createProjBtnH = 38;
        createProjBtnX = startX + padX + contentW - createProjBtnW;
        createProjBtnY = curY;
        RoundRectangle2D primBtnShape = new RoundRectangle2D.Float(createProjBtnX, createProjBtnY, createProjBtnW, createProjBtnH, 10, 10);
        g.setColor(createProjBtnHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(primBtnShape);

        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        g.drawString("+ Create Project", createProjBtnX + 16, createProjBtnY + 24);

        curY += 72;

        // 2. Profile Completion Banner Card
        int bannerH = 72;
        RoundRectangle2D bannerShape = new RoundRectangle2D.Float(startX + padX, curY, contentW, bannerH, 14, 14);
        GradientPaint bannerGrad = new GradientPaint(
                startX + padX, curY, new Color(0xFF, 0xF5, 0xF8),
                startX + padX + contentW, curY, new Color(0xFD, 0xEE, 0xF4)
        );
        g.setPaint(bannerGrad);
        g.fill(bannerShape);
        g.setColor(new Color(0xFC, 0xD6, 0xE6));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(bannerShape);

        // Sparkle icon
        int spX = startX + padX + 16;
        int spY = curY + 16;
        int spSize = 38;
        g.setColor(Color.WHITE);
        g.fillOval(spX, spY, spSize, spSize);
        g.setColor(new Color(0xFC, 0xD6, 0xE6));
        g.drawOval(spX, spY, spSize, spSize);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        drawSparkle(g, spX + 13, spY + 13, 12);

        // Banner text
        int pct = dashboard.getProfileCompletePercent();
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        String bTitle = (pct >= 100) ? "✓ Great profile! You're ready to get discovered." : "Great teammates start with a great profile.";
        g.drawString(bTitle, spX + 50, curY + 30);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String bSub = (pct >= 100) ? "Your portfolio and skills are live. Recruiters can view your card." : "Add your portfolio to help people see what you can build.";
        g.drawString(bSub, spX + 50, curY + 48);

        // Progress bar
        int progW = 100;
        int progH = 6;
        int progX = startX + padX + contentW - 270;
        int progY = curY + 36;

        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString(pct + "% complete", progX, curY + 26);

        RoundRectangle2D progTrack = new RoundRectangle2D.Float(progX, progY, progW, progH, progH, progH);
        g.setColor(new Color(0xFC, 0xD6, 0xE6));
        g.fill(progTrack);
        RoundRectangle2D progFill = new RoundRectangle2D.Float(progX, progY, Math.round(progW * (pct / 100f)), progH, progH, progH);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.fill(progFill);

        // "Complete profile ->" button
        compProfileBtnW = 142;
        compProfileBtnH = 34;
        compProfileBtnX = startX + padX + contentW - compProfileBtnW - 16;
        compProfileBtnY = curY + (bannerH - compProfileBtnH) / 2;
        RoundRectangle2D cpBtnShape = new RoundRectangle2D.Float(compProfileBtnX, compProfileBtnY, compProfileBtnW, compProfileBtnH, compProfileBtnH, compProfileBtnH);
        g.setColor(compProfileBtnHovered ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(cpBtnShape);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cpBtnShape);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        String cpText = (pct >= 100) ? "Edit profile →" : "Complete profile →";
        g.drawString(cpText, compProfileBtnX + 16, compProfileBtnY + 21);

        curY += bannerH + 20;

        // 3. Stat Cards Row (4 cards with real counts)
        int statGap = 16;
        int statW = (contentW - statGap * 3) / 4;
        int statH = 116;

        paintStatCard(g, startX + padX, curY, statW, statH, "Active Projects", String.valueOf(dashboard.getActiveProjectsCount()), "Across " + dashboard.getActiveProjectsCount() + " collaborative teams", 0);
        paintStatCard(g, startX + padX + (statW + statGap), curY, statW, statH, "My Teams", String.valueOf(dashboard.getMyTeamsCount()), (dashboard.getMyTeamsCount() - 1) + " active · 1 completed", 1);
        paintStatCard(g, startX + padX + (statW + statGap) * 2, curY, statW, statH, "Pending Requests", String.valueOf(dashboard.getPendingRequestsCount()), "3 received · " + Math.max(0, dashboard.getPendingRequestsCount() - 3) + " sent", 2);
        paintStatCard(g, startX + padX + (statW + statGap) * 3, curY, statW, statH, "Tasks Due Soon", String.valueOf(dashboard.getTasksDueCount()), "Next 7 days · 1 overdue", 3);

        curY += statH + 24;

        // 4. Two-Column Lower Content
        int colGap = 20;
        int leftColW = Math.round((contentW - colGap) * 0.63f);
        int rightColW = contentW - leftColW - colGap;
        int leftX = startX + padX;
        int rightX = leftX + leftColW + colGap;

        // LEFT COLUMN:
        int leftCurY = curY;
        paintRecommendedProjects(g, leftX, leftCurY, leftColW, dashboard.getProjects());
        leftCurY += 276;

        paintRecentActivity(g, leftX, leftCurY, leftColW, dashboard.getActivities());
        leftCurY += 250;

        // RIGHT COLUMN:
        int rightCurY = curY;
        paintSuggestedTeammates(g, rightX, rightCurY, rightColW, dashboard.getTeammates());
        rightCurY += 250;

        paintUpcomingDeadlines(g, rightX, rightCurY, rightColW, dashboard.getTasks());
        rightCurY += 276;

        totalContentHeight = Math.max(leftCurY, rightCurY) - startY + 40;
    }

    private void paintStatCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub, int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(title, x + 16, y + 26);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(val, x + 16, y + 68);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(sub, x + 16, y + 96);

        // Icon
        int iconX = x + w - 32;
        int iconY = y + 16;
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (type == 0) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawRoundRect(iconX, iconY + 2, 16, 12, 3, 3);
            g.drawLine(iconX, iconY + 5, iconX + 6, iconY + 5);
        } else if (type == 1) {
            g.setColor(new Color(0x0D, 0x94, 0x88));
            g.drawOval(iconX + 4, iconY, 6, 6);
            g.drawArc(iconX + 1, iconY + 7, 12, 7, 0, 180);
        } else if (type == 2) {
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawOval(iconX + 3, iconY, 6, 6);
            g.drawArc(iconX, iconY + 7, 10, 7, 0, 180);
            g.drawLine(iconX + 13, iconY + 4, iconX + 13, iconY + 8);
            g.drawLine(iconX + 11, iconY + 6, iconX + 15, iconY + 6);
        } else {
            g.setColor(new Color(0xD9, 0x77, 0x06));
            g.drawRoundRect(iconX, iconY + 2, 14, 13, 3, 3);
            g.drawLine(iconX + 3, iconY, iconX + 3, iconY + 2);
            g.drawLine(iconX + 11, iconY + 11, iconX + 11, iconY + 2);
            g.drawLine(iconX, iconY + 6, iconX + 14, iconY + 6);
        }
    }

    private void paintRecommendedProjects(Graphics2D g, int x, int y, int w, List<StudentDashboard.ProjectItem> projects) {
        int cardH = 260;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        // Header: "Recommended projects" ... "Explore all ->"
        g.setFont(DashboardTheme.FONT_TITLE);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Recommended projects", x + 20, y + 30);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String exp = "Explore all →";
        int expW = g.getFontMetrics().stringWidth(exp);
        exploreAllBtnX = x + w - expW - 20;
        exploreAllBtnY = y + 16;
        exploreAllBtnW = expW;
        exploreAllBtnH = 20;
        g.drawString(exp, exploreAllBtnX, y + 30);

        int pad = 20;
        int gap = 16;
        int pCardW = (w - pad * 2 - gap) / 2;
        int pCardH = 190;
        int pCardY = y + 48;

        for (int i = 0; i < Math.min(2, projects.size()); i++) {
            StudentDashboard.ProjectItem p = projects.get(i);
            int px = x + pad + i * (pCardW + gap);
            p.hitX = px;
            p.hitY = pCardY;
            p.hitW = pCardW;
            p.hitH = pCardH;
            p.bmX = px + pCardW - 28;
            p.bmY = pCardY + 16;

            paintSingleProjectCard(g, p);
        }
    }

    private void paintSingleProjectCard(Graphics2D g, StudentDashboard.ProjectItem p) {
        int x = p.hitX;
        int y = p.hitY;
        int w = p.hitW;
        int h = p.hitH;

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(new Color(0xFA, 0xF9, 0xF6));
        g.fill(cardShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(cardShape);

        // Icon
        int iconSize = 26;
        int iconX = x + 16;
        int iconY = y + 16;
        g.setColor(p.isGreen ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xE0, 0xF2, 0xFE));
        g.fillOval(iconX, iconY, iconSize, iconSize);

        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (p.isGreen) {
            g.setColor(new Color(0x16, 0xA3, 0x4A));
            g.drawArc(iconX + 6, iconY + 6, 12, 14, 0, 180);
            g.drawLine(iconX + 6, iconY + 16, iconX + 16, iconY + 10);
        } else {
            g.setColor(new Color(0x02, 0x84, 0xC7));
            g.drawPolygon(
                    new int[]{iconX + 10, iconX + 15, iconX + 19, iconX + 6},
                    new int[]{iconY + 6, iconY + 6, iconY + 19, iconY + 19}, 4
            );
        }

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.title, iconX + 34, iconY + 18);

        // Bookmark Icon
        int bmx = p.bmX;
        int bmy = p.bmY;
        g.setColor(p.bookmarked ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.3f));
        if (p.bookmarked) {
            g.fillPolygon(new int[]{bmx, bmx + 10, bmx + 10, bmx + 5, bmx}, new int[]{bmy, bmy, bmy + 14, bmy + 10, bmy + 14}, 5);
        } else {
            g.drawPolygon(new int[]{bmx, bmx + 10, bmx + 10, bmx + 5, bmx}, new int[]{bmy, bmy, bmy + 14, bmy + 10, bmy + 14}, 5);
        }

        // Author
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(p.author, x + 16, y + 54);

        // Description
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(new Color(0x47, 0x55, 0x69));
        FontMetrics dfm = g.getFontMetrics();
        List<String> dLines = Draw.wrapText(p.desc, dfm, w - 32);
        int dy = y + 74;
        for (String dl : dLines) {
            g.drawString(dl, x + 16, dy);
            dy += 16;
        }

        // Skills
        int sx = x + 16;
        int sy = y + 116;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();
        for (String sk : p.skills) {
            int sw = sfm.stringWidth(sk) + 14;
            int sh = 22;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(sx, sy, sw, sh, 6, 6);
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fill(sPill);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(sk, sx + 7, sy + 15);
            sx += sw + 6;
        }

        // Footer
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(p.footer, x + 16, y + 154);

        // Action Link
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("View project →", x + 16, y + 176);
    }

    private void paintRecentActivity(Graphics2D g, int x, int y, int w, List<StudentDashboard.ActivityItem> activities) {
        int cardH = 220;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        g.setFont(DashboardTheme.FONT_TITLE);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Recent activity", x + 20, y + 30);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String va = "View all →";
        int vaW = g.getFontMetrics().stringWidth(va);
        viewAllBtnX = x + w - vaW - 20;
        viewAllBtnY = y + 16;
        viewAllBtnW = vaW;
        viewAllBtnH = 20;
        g.drawString(va, viewAllBtnX, y + 30);

        int curY = y + 50;
        for (int i = 0; i < Math.min(3, activities.size()); i++) {
            StudentDashboard.ActivityItem act = activities.get(i);
            paintActivityItem(g, x + 20, curY, w - 40, act);
            curY += 52;
        }
    }

    private void paintActivityItem(Graphics2D g, int x, int y, int w, StudentDashboard.ActivityItem act) {
        int avSize = 34;
        g.setColor(act.avBg);
        g.fillOval(x, y, avSize, avSize);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(act.avText);
        g.drawString(act.initials, x + 9, y + 22);

        int textX = x + 44;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        if (act.subtitle.isEmpty()) {
            g.drawString(act.title, textX, y + 21);
        } else {
            g.drawString(act.title, textX, y + 16);
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(act.subtitle, textX, y + 31);
        }

        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics bfm = g.getFontMetrics();
        int bw = bfm.stringWidth(act.badgeText) + 16;
        int bh = 22;
        int bx = x + w - bw;
        int by = y + (avSize - bh) / 2;
        RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
        g.setColor(act.badgeBg);
        g.fill(bShape);
        g.setColor(act.badgeTextCol);
        g.drawString(act.badgeText, bx + 8, by + 15);
    }

    private void paintSuggestedTeammates(Graphics2D g, int x, int y, int w, List<StudentDashboard.TeammateItem> teammates) {
        int cardH = 230;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        g.setFont(DashboardTheme.FONT_TITLE);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Suggested teammates", x + 20, y + 30);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String fm = "Find more →";
        int fmW = g.getFontMetrics().stringWidth(fm);
        findMoreBtnX = x + w - fmW - 20;
        findMoreBtnY = y + 16;
        findMoreBtnW = fmW;
        findMoreBtnH = 20;
        g.drawString(fm, findMoreBtnX, y + 30);

        int curY = y + 50;
        for (int i = 0; i < Math.min(2, teammates.size()); i++) {
            StudentDashboard.TeammateItem t = teammates.get(i);
            t.hitX = x + 20;
            t.hitY = curY;
            t.hitW = w - 40;
            t.hitH = 44;

            paintTeammateRow(g, t);
            curY += 48;
        }

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("Illustrative matches based on complementary skills.", x + 20, curY + 6);
        curY += 16;

        findTeammatesBtnX = x + 20;
        findTeammatesBtnY = curY;
        findTeammatesBtnW = w - 40;
        findTeammatesBtnH = 36;
        RoundRectangle2D btnShape = new RoundRectangle2D.Float(findTeammatesBtnX, findTeammatesBtnY, findTeammatesBtnW, findTeammatesBtnH, 10, 10);
        g.setColor(findTeammatesBtnHovered ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(btnShape);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(btnShape);

        int iconX = findTeammatesBtnX + (findTeammatesBtnW - 130) / 2;
        int iconY = findTeammatesBtnY + 11;
        g.setColor(DashboardTheme.TEXT_DARK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(iconX + 2, iconY, 5, 5);
        g.drawArc(iconX, iconY + 7, 9, 6, 0, 180);
        g.drawOval(iconX + 9, iconY + 2, 4, 4);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Find Teammates", iconX + 20, findTeammatesBtnY + 23);
    }

    private void paintTeammateRow(Graphics2D g, StudentDashboard.TeammateItem t) {
        int x = t.hitX;
        int y = t.hitY;
        int w = t.hitW;

        int avSize = 34;
        g.setColor(t.avBg);
        g.fillOval(x, y, avSize, avSize);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(t.avText);
        g.drawString(t.initials, x + 9, y + 22);

        int textX = x + 44;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(t.name, textX, y + 16);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(t.role, textX, y + 30);

        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics mfm = g.getFontMetrics();
        g.setColor(new Color(0x05, 0x96, 0x69));
        g.drawString(t.matchScore, x + w - mfm.stringWidth(t.matchScore), y + 22);
    }

    private void paintUpcomingDeadlines(Graphics2D g, int x, int y, int w, List<StudentDashboard.TaskItem> tasks) {
        int cardH = 250;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        g.setFont(DashboardTheme.FONT_TITLE);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Upcoming deadlines", x + 20, y + 30);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String at = "All tasks →";
        int atW = g.getFontMetrics().stringWidth(at);
        allTasksBtnX = x + w - atW - 20;
        allTasksBtnY = y + 16;
        allTasksBtnW = atW;
        allTasksBtnH = 20;
        g.drawString(at, allTasksBtnX, y + 30);

        int curY = y + 54;
        for (int i = 0; i < Math.min(3, tasks.size()); i++) {
            StudentDashboard.TaskItem t = tasks.get(i);
            t.hitX = x + 20;
            t.hitY = curY;
            t.hitW = w - 40;
            t.hitH = 38;

            paintTaskItem(g, t);
            curY += 46;
        }

        curY += 4;
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String unreadText = "3 unread conversations";
        g.drawString(unreadText, x + 20, curY);
        unreadMsgBtnX = x + 20;
        unreadMsgBtnY = curY - 14;
        unreadMsgBtnW = g.getFontMetrics().stringWidth(unreadText);
        unreadMsgBtnH = 20;

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String vm = "View Messages →";
        int vmW = g.getFontMetrics().stringWidth(vm);
        viewMessagesBtnX = x + w - vmW - 20;
        viewMessagesBtnY = curY - 14;
        viewMessagesBtnW = vmW;
        viewMessagesBtnH = 20;
        g.drawString(vm, viewMessagesBtnX, curY);
    }

    private void paintTaskItem(Graphics2D g, StudentDashboard.TaskItem t) {
        int x = t.hitX;
        int y = t.hitY;

        int iconX = x;
        int iconY = y + 2;
        g.setColor(t.completed ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(iconX, iconY, 13, 12, 2, 2);
        g.drawLine(iconX + 3, iconY - 2, iconX + 3, iconY);
        g.drawLine(iconX + 10, iconY - 2, iconX + 10, iconY);
        g.drawLine(iconX, iconY + 4, iconX + 13, iconY + 4);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(t.completed ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
        String displayTitle = t.completed ? t.title + " (Done ✓)" : t.title;
        g.drawString(displayTitle, x + 22, y + 12);

        g.setFont(DashboardTheme.FONT_REG_11);
        if (t.statusType == 0) {
            g.setColor(t.completed ? DashboardTheme.TEXT_LIGHT : new Color(0xEF, 0x44, 0x44));
            g.drawString("Overdue", x + 22, y + 26);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(" · Oct 15 · CampusConnect", x + 68, y + 26);
        } else if (t.statusType == 1) {
            g.setColor(t.completed ? DashboardTheme.TEXT_LIGHT : new Color(0xF5, 0x9E, 0x0B));
            g.drawString("Today", x + 22, y + 26);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(" · Oct 16 · CampusConnect", x + 56, y + 26);
        } else {
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(t.subtitle, x + 22, y + 26);
        }
    }

    private void drawSparkle(Graphics2D g, int cx, int cy, int size) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx, cy - size);
        p.quadTo(cx, cy, cx + size, cy);
        p.quadTo(cx, cy, cx, cy + size);
        p.quadTo(cx, cy, cx - size, cy);
        p.quadTo(cx, cy, cx, cy - size);
        g.fill(p);
    }

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // "+ Create Project" button (Primary)
        if (mx >= createProjBtnX && mx <= createProjBtnX + createProjBtnW &&
                my >= createProjBtnY && my <= createProjBtnY + createProjBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.CREATE_PROJECT);
            return true;
        }

        // "Complete profile ->" banner button
        if (mx >= compProfileBtnX && mx <= compProfileBtnX + compProfileBtnW &&
                my >= compProfileBtnY && my <= compProfileBtnY + compProfileBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.COMPLETE_PROFILE);
            return true;
        }

        // "Find Teammates" right column button
        if (mx >= findTeammatesBtnX && mx <= findTeammatesBtnX + findTeammatesBtnW &&
                my >= findTeammatesBtnY && my <= findTeammatesBtnY + findTeammatesBtnH) {
            dashboard.setActiveNav(StudentDashboard.NavItem.FIND_TEAMMATES);
            dashboard.showToast("Opening Find Teammates discovery...");
            return true;
        }

        // "Explore all ->"
        if (mx >= exploreAllBtnX && mx <= exploreAllBtnX + exploreAllBtnW &&
                my >= exploreAllBtnY && my <= exploreAllBtnY + exploreAllBtnH) {
            dashboard.setActiveNav(StudentDashboard.NavItem.DISCOVER_PROJECTS);
            dashboard.showToast("Browsing projects looking for teammates...");
            return true;
        }

        // "Find more ->"
        if (mx >= findMoreBtnX && mx <= findMoreBtnX + findMoreBtnW &&
                my >= findMoreBtnY && my <= findMoreBtnY + findMoreBtnH) {
            dashboard.setActiveNav(StudentDashboard.NavItem.FIND_TEAMMATES);
            dashboard.showToast("Opening Find Teammates discovery...");
            return true;
        }

        // "View all ->" activity
        if (mx >= viewAllBtnX && mx <= viewAllBtnX + viewAllBtnW &&
                my >= viewAllBtnY && my <= viewAllBtnY + viewAllBtnH) {
            dashboard.showToast("Activity history loaded.");
            return true;
        }

        // "All tasks ->"
        if (mx >= allTasksBtnX && mx <= allTasksBtnX + allTasksBtnW &&
                my >= allTasksBtnY && my <= allTasksBtnY + allTasksBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.ALL_TASKS);
            return true;
        }

        // "View Messages ->" and "3 unread conversations" -> Navigate to Messages Page
        boolean clickedMessages = (mx >= viewMessagesBtnX && mx <= viewMessagesBtnX + viewMessagesBtnW &&
                my >= viewMessagesBtnY && my <= viewMessagesBtnY + viewMessagesBtnH) ||
                (mx >= unreadMsgBtnX && mx <= unreadMsgBtnX + unreadMsgBtnW &&
                my >= unreadMsgBtnY && my <= unreadMsgBtnY + unreadMsgBtnH);
        if (clickedMessages) {
            dashboard.setActiveNav(StudentDashboard.NavItem.MESSAGES);
            dashboard.showToast("Opening Messages...");
            return true;
        }

        // Project Cards interaction: Bookmarks & "View project ->"
        for (StudentDashboard.ProjectItem p : dashboard.getProjects()) {
            // Bookmark click
            if (mx >= p.bmX - 6 && mx <= p.bmX + 18 && my >= p.bmY - 6 && my <= p.bmY + 18) {
                p.bookmarked = !p.bookmarked;
                dashboard.showToast(p.bookmarked ? p.title + " added to Bookmarks ★" : p.title + " removed from Bookmarks");
                return true;
            }
            // Card or "View project" click
            if (mx >= p.hitX && mx <= p.hitX + p.hitW && my >= p.hitY && my <= p.hitY + p.hitH) {
                dashboard.setSelectedProject(p);
                dashboard.openModal(StudentDashboard.ActiveModal.PROJECT_DETAILS);
                return true;
            }
        }

        // Teammates row click
        for (StudentDashboard.TeammateItem t : dashboard.getTeammates()) {
            if (mx >= t.hitX && mx <= t.hitX + t.hitW && my >= t.hitY && my <= t.hitY + t.hitH) {
                dashboard.setSelectedTeammate(t);
                dashboard.openModal(StudentDashboard.ActiveModal.TEAMMATE_DETAILS);
                return true;
            }
        }

        // Deadlines task click: toggle task completed
        for (StudentDashboard.TaskItem t : dashboard.getTasks()) {
            if (mx >= t.hitX && mx <= t.hitX + t.hitW && my >= t.hitY && my <= t.hitY + t.hitH) {
                t.completed = !t.completed;
                if (t.completed) {
                    dashboard.setTasksDueCount(Math.max(0, dashboard.getTasksDueCount() - 1));
                    dashboard.showToast("Completed: " + t.title + " ✓");
                } else {
                    dashboard.setTasksDueCount(dashboard.getTasksDueCount() + 1);
                }
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p2 = createProjBtnHovered;
        createProjBtnHovered = (mx >= createProjBtnX && mx <= createProjBtnX + createProjBtnW &&
                my >= createProjBtnY && my <= createProjBtnY + createProjBtnH);
        if (createProjBtnHovered != p2) changed = true;

        boolean p4 = compProfileBtnHovered;
        compProfileBtnHovered = (mx >= compProfileBtnX && mx <= compProfileBtnX + compProfileBtnW &&
                my >= compProfileBtnY && my <= compProfileBtnY + compProfileBtnH);
        if (compProfileBtnHovered != p4) changed = true;

        boolean p5 = findTeammatesBtnHovered;
        findTeammatesBtnHovered = (mx >= findTeammatesBtnX && mx <= findTeammatesBtnX + findTeammatesBtnW &&
                my >= findTeammatesBtnY && my <= findTeammatesBtnY + findTeammatesBtnH);
        if (findTeammatesBtnHovered != p5) changed = true;

        return changed || createProjBtnHovered || compProfileBtnHovered || findTeammatesBtnHovered;
    }
}

