import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Pixel-perfect Student Workspace Overview Dashboard for TeamUp.
 * Matches the uploaded reference design down to layout, colors, icons, badges,
 * and typography. Pure core Java with vector-drawn graphics and smooth interactions.
 */
public class StudentDashboard {

    public interface DashboardListener {
        void returnToLanding();
        void openModal(String title, String message, boolean isRegister);
    }

    private final DashboardListener listener;

    // Viewport & scrolling
    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private int totalContentHeight = 1050;

    // Sidebar navigation state
    public enum NavItem {
        OVERVIEW("Overview", 0),
        DISCOVER_PROJECTS("Discover Projects", 0),
        FIND_TEAMMATES("Find Teammates", 0),
        MY_PROJECTS("My Projects", 0),
        MY_TEAMS("My Teams", 0),
        MESSAGES("Messages", 3), // badge 3
        MY_PROFILE("My Profile", 0),
        SETTINGS("Settings", 0);

        final String label;
        final int badge;
        NavItem(String label, int badge) {
            this.label = label;
            this.badge = badge;
        }
    }

    private NavItem activeNav = NavItem.OVERVIEW;
    private NavItem hoveredNav = null;

    // Interactive elements hitboxes
    private int returnBtnX, returnBtnY, returnBtnW, returnBtnH;
    private boolean returnBtnHovered = false;

    private int createProjBtnX, createProjBtnY, createProjBtnW, createProjBtnH;
    private boolean createProjBtnHovered = false;

    private int topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH;
    private boolean topCreateBtnHovered = false;

    private int compProfileBtnX, compProfileBtnY, compProfileBtnW, compProfileBtnH;
    private boolean compProfileBtnHovered = false;

    private int findTeammatesBtnX, findTeammatesBtnY, findTeammatesBtnW, findTeammatesBtnH;
    private boolean findTeammatesBtnHovered = false;

    // Bookmarks state
    private boolean greenRouteBookmarked = false;
    private boolean openLabBookmarked = false;
    private int bm1X, bm1Y, bm1Size = 22;
    private int bm2X, bm2Y, bm2Size = 22;

    // Search box
    private String searchText = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW, searchH;

    // Notification toast
    private String toastMessage = "";
    private float toastTimer = 0f;

    // Colors matching reference design
    private static final Color BG_CANVAS = new Color(0xF8, 0xF9, 0xFD);
    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xEA, 0xEF, 0xF5);
    private static final Color BRAND_PURPLE = new Color(0x4F, 0x46, 0xE5); // #4F46E5
    private static final Color BRAND_PURPLE_HOVER = new Color(0x43, 0x38, 0xCA);
    private static final Color PURPLE_TINT_BG = new Color(0xEE, 0xF2, 0xFF); // #EEF2FF
    private static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A); // #0F172A
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B); // #64748B
    private static final Color TEXT_LIGHT = new Color(0x94, 0xA3, 0xB8); // #94A3B8

    public StudentDashboard(DashboardListener listener) {
        this.listener = listener;
    }

    public void showToast(String message) {
        this.toastMessage = message;
        this.toastTimer = 3.5f;
    }

    public void update(float dt) {
        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14f);
        if (toastTimer > 0f) {
            toastTimer -= dt;
            if (toastTimer <= 0f) toastMessage = "";
        }
    }

    public void paint(Graphics2D g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // 1. Overall Background Canvas
        g2.setColor(BG_CANVAS);
        g2.fillRect(0, 0, width, height);

        int sideW = 230;
        int headerH = 60;

        // 2. Main Scrollable Content Area (Clipped under header & right of sidebar)
        Shape oldClip = g2.getClip();
        g2.setClip(sideW, headerH, width - sideW, height - headerH);

        Graphics2D cg = (Graphics2D) g2.create();
        int contentY = headerH - Math.round(scrollY);
        paintOverviewContent(cg, sideW, contentY, width - sideW, height);
        cg.dispose();

        g2.setClip(oldClip);

        // 3. Left Fixed Sidebar
        paintSidebar(g2, 0, 0, sideW, height);

        // 4. Top Fixed Header
        paintHeader(g2, sideW, 0, width - sideW, headerH);

        // 5. Scrollbar for Content
        paintContentScrollbar(g2, width - 8, headerH, 6, height - headerH);

        // 6. Toast Notification (if active)
        if (!toastMessage.isEmpty()) {
            paintToast(g2, width, height);
        }

        g2.dispose();
    }

    // =========================================================================
    // SIDEBAR RENDERING
    // =========================================================================
    private void paintSidebar(Graphics2D g, int x, int y, int w, int h) {
        // Background & Right Border
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(BORDER_COLOR);
        g.drawLine(x + w - 1, y, x + w - 1, y + h);

        int curY = y + 20;
        int padX = 20;

        // Logo + Brand Name
        paintLogo(g, x + padX, curY);
        curY += 46;

        // "YOUR WORKSPACE" Card
        int wsCardW = w - padX * 2;
        int wsCardH = 68;
        RoundRectangle2D wsCard = new RoundRectangle2D.Float(x + padX, curY, wsCardW, wsCardH, 12, 12);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(wsCard);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("YOUR WORKSPACE", x + padX + 12, curY + 20);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Student workspace", x + padX + 12, curY + 38);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("University of Washington", x + padX + 12, curY + 54);

        curY += wsCardH + 16;

        // Nav Menu Items
        int itemH = 38;
        int itemW = w - padX * 2;
        NavItem[] items = NavItem.values();

        for (NavItem item : items) {
            boolean isSel = (item == activeNav);
            boolean isHov = (item == hoveredNav);
            int itemX = x + padX;
            int itemY = curY;

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemX, itemY, itemW, itemH, 10, 10);
            if (isSel) {
                g.setColor(PURPLE_TINT_BG);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fill(itemShape);
            }

            // Icon
            Color iconCol = isSel ? BRAND_PURPLE : TEXT_MUTED;
            paintNavIcon(g, item, itemX + 12, itemY + 11, 16, iconCol);

            // Label
            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 13));
            g.setColor(isSel ? BRAND_PURPLE : TEXT_MUTED);
            g.drawString(item.label, itemX + 38, itemY + 24);

            // Badge (for Messages)
            if (item.badge > 0) {
                int bw = 20;
                int bh = 18;
                int bx = itemX + itemW - bw - 10;
                int by = itemY + (itemH - bh) / 2;
                RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
                g.setColor(new Color(0xEE, 0xF2, 0xFF));
                g.fill(bShape);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                g.setColor(BRAND_PURPLE);
                g.drawString(String.valueOf(item.badge), bx + 7, by + 13);
            }

            curY += itemH + 2;
        }

        // Return to Landing Page Pill (Helper navigation)
        curY += 12;
        returnBtnX = x + padX;
        returnBtnY = curY;
        returnBtnW = w - padX * 2;
        returnBtnH = 34;
        RoundRectangle2D retShape = new RoundRectangle2D.Float(returnBtnX, returnBtnY, returnBtnW, returnBtnH, 10, 10);
        g.setColor(returnBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(retShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(retShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("← Back to Landing Page", returnBtnX + 16, returnBtnY + 21);

        // Bottom User Profile Card
        int bottomH = 64;
        int bottomY = h - bottomH;
        g.setColor(BORDER_COLOR);
        g.drawLine(x, bottomY, x + w, bottomY);

        // Avatar "AM"
        int avX = x + padX;
        int avY = bottomY + 14;
        int avSize = 36;
        g.setColor(new Color(0xED, 0xE9, 0xFE));
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        g.drawString("AM", avX + 9, avY + 23);

        // User info
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Alex Morgan", avX + 44, avY + 16);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString("Student · Free plan", avX + 44, avY + 31);

        // Chevron
        Draw.drawChevronDown(g, x + w - padX - 10, avY + 14, 10, TEXT_LIGHT, 0f);
    }

    private void paintLogo(Graphics2D g, int x, int y) {
        int iconSize = 28;
        RoundRectangle2D logoBg = new RoundRectangle2D.Float(x, y, iconSize, iconSize, 8, 8);
        g.setColor(BRAND_PURPLE);
        g.fill(logoBg);

        // 4 dots/squares
        g.setColor(Color.WHITE);
        int d = 6;
        g.fillRoundRect(x + 6, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 6, y + 16, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 16, d, d, 2, 2);

        // Brand Name
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(TEXT_DARK);
        g.drawString("TeamUp", x + 38, y + 21);
    }

    private void paintNavIcon(Graphics2D g, NavItem item, int x, int y, int size, Color c) {
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

    // =========================================================================
    // TOP HEADER RENDERING
    // =========================================================================
    private void paintHeader(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        int padX = 28;
        int curX = x + padX;

        // Breadcrumb: "Workspace >"
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("Workspace", curX, y + 35);
        curX += 75;
        g.setColor(TEXT_LIGHT);
        g.drawString("›", curX, y + 35);
        curX += 20;

        // Search Pill Input
        searchX = curX;
        searchY = y + 12;
        searchW = Math.min(320, w - 460);
        searchH = 36;
        RoundRectangle2D searchPill = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, searchH, searchH);
        g.setColor(searchFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(searchPill);
        g.setColor(searchFocused ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(searchPill);

        // Search Magnifier Icon
        g.setColor(TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 11, 10, 10);
        g.drawLine(searchX + 22, searchY + 19, searchX + 26, searchY + 23);

        // Placeholder or entered text
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (searchText.isEmpty() && !searchFocused) {
            g.setColor(TEXT_LIGHT);
            g.drawString("Search projects, people and teams...", searchX + 34, searchY + 22);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(searchText, searchX + 34, searchY + 22);
        }

        // Right side controls
        int rightX = x + w - padX;

        // User Avatar Circle "AM"
        int avSize = 34;
        rightX -= avSize;
        int avY = y + (h - avSize) / 2;
        g.setColor(new Color(0xED, 0xE9, 0xFE));
        g.fillOval(rightX, avY, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        g.drawString("AM", rightX + 8, avY + 22);

        // Bell Icon
        rightX -= 36;
        int bellX = rightX;
        int bellY = y + 21;
        g.setColor(TEXT_MUTED);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(bellX, bellY, 14, 14, 0, 180);
        g.drawLine(bellX - 2, bellY + 14, bellX + 16, bellY + 14);
        g.drawOval(bellX + 5, bellY + 14, 4, 3);
        // Notification dot
        g.setColor(BRAND_PURPLE);
        g.fillOval(bellX + 12, bellY - 1, 5, 5);

        // "+ Create project" Ghost Pill Button
        rightX -= 140;
        topCreateBtnX = rightX;
        topCreateBtnY = y + 13;
        topCreateBtnW = 126;
        topCreateBtnH = 34;
        RoundRectangle2D topBtnShape = new RoundRectangle2D.Float(topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH, topCreateBtnH, topCreateBtnH);
        g.setColor(topCreateBtnHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(topBtnShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(topBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("+ Create project", topCreateBtnX + 14, topCreateBtnY + 21);
    }

    // =========================================================================
    // MAIN CONTENT RENDERING ("OVERVIEW")
    // =========================================================================
    private void paintOverviewContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Welcome Title & Subtitle + "+ Create Project" button
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString("Welcome back, Alex!", startX + padX, curY + 26);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("A little progress, a great team. Here's what's happening today.", startX + padX, curY + 48);

        // "+ Create Project" Primary Pill Button (top-right)
        createProjBtnW = 136;
        createProjBtnH = 38;
        createProjBtnX = startX + padX + contentW - createProjBtnW;
        createProjBtnY = curY;
        RoundRectangle2D primBtnShape = new RoundRectangle2D.Float(createProjBtnX, createProjBtnY, createProjBtnW, createProjBtnH, 10, 10);
        g.setColor(createProjBtnHovered ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        g.fill(primBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("+ Create Project", createProjBtnX + 16, createProjBtnY + 24);

        curY += 72;

        // 2. Profile Completion Banner Card
        int bannerH = 72;
        RoundRectangle2D bannerShape = new RoundRectangle2D.Float(startX + padX, curY, contentW, bannerH, 14, 14);
        // Soft gradient fill
        GradientPaint bannerGrad = new GradientPaint(
                startX + padX, curY, new Color(0xF8, 0xFA, 0xFD),
                startX + padX + contentW, curY, new Color(0xEE, 0xF2, 0xFF)
        );
        g.setPaint(bannerGrad);
        g.fill(bannerShape);
        g.setColor(new Color(0xE0, 0xE7, 0xFF));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(bannerShape);

        // Sparkle / Star Icon in circle
        int spX = startX + padX + 16;
        int spY = curY + 16;
        int spSize = 38;
        g.setColor(Color.WHITE);
        g.fillOval(spX, spY, spSize, spSize);
        g.setColor(new Color(0xE0, 0xE7, 0xFF));
        g.drawOval(spX, spY, spSize, spSize);
        // Sparkle stars
        g.setColor(BRAND_PURPLE);
        drawSparkle(g, spX + 13, spY + 13, 12);

        // Banner text
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Great teammates start with a great profile.", spX + 50, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Add your portfolio to help people see what you can build.", spX + 50, curY + 48);

        // Progress bar (middle right)
        int progW = 100;
        int progH = 6;
        int progX = startX + padX + contentW - 270;
        int progY = curY + 36;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(BRAND_PURPLE);
        g.drawString("80% complete", progX, curY + 26);

        RoundRectangle2D progTrack = new RoundRectangle2D.Float(progX, progY, progW, progH, progH, progH);
        g.setColor(new Color(0xE0, 0xE7, 0xFF));
        g.fill(progTrack);
        RoundRectangle2D progFill = new RoundRectangle2D.Float(progX, progY, Math.round(progW * 0.8f), progH, progH, progH);
        g.setColor(BRAND_PURPLE);
        g.fill(progFill);

        // "Complete profile ->" button
        compProfileBtnW = 138;
        compProfileBtnH = 34;
        compProfileBtnX = startX + padX + contentW - compProfileBtnW - 16;
        compProfileBtnY = curY + (bannerH - compProfileBtnH) / 2;
        RoundRectangle2D cpBtnShape = new RoundRectangle2D.Float(compProfileBtnX, compProfileBtnY, compProfileBtnW, compProfileBtnH, compProfileBtnH, compProfileBtnH);
        g.setColor(compProfileBtnHovered ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(cpBtnShape);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cpBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Complete profile →", compProfileBtnX + 14, compProfileBtnY + 21);

        curY += bannerH + 20;

        // 3. Stat Cards Row (4 cards)
        int statGap = 16;
        int statW = (contentW - statGap * 3) / 4;
        int statH = 116;

        paintStatCard(g, startX + padX, curY, statW, statH, "Active Projects", "3", "Across 3 collaborative teams", 0);
        paintStatCard(g, startX + padX + (statW + statGap), curY, statW, statH, "My Teams", "4", "3 active · 1 completed", 1);
        paintStatCard(g, startX + padX + (statW + statGap) * 2, curY, statW, statH, "Pending Requests", "5", "3 received · 2 sent", 2);
        paintStatCard(g, startX + padX + (statW + statGap) * 3, curY, statW, statH, "Tasks Due Soon", "6", "Next 7 days · 1 overdue", 3);

        curY += statH + 24;

        // 4. Two-Column Lower Content
        int colGap = 20;
        int leftColW = Math.round((contentW - colGap) * 0.63f);
        int rightColW = contentW - leftColW - colGap;
        int leftX = startX + padX;
        int rightX = leftX + leftColW + colGap;

        // LEFT COLUMN:
        int leftCurY = curY;
        paintRecommendedProjects(g, leftX, leftCurY, leftColW);
        leftCurY += 276;

        paintRecentActivity(g, leftX, leftCurY, leftColW);
        leftCurY += 250;

        // RIGHT COLUMN:
        int rightCurY = curY;
        paintSuggestedTeammates(g, rightX, rightCurY, rightColW);
        rightCurY += 250;

        paintUpcomingDeadlines(g, rightX, rightCurY, rightColW);
        rightCurY += 276;

        totalContentHeight = Math.max(leftCurY, rightCurY) - startY + 40;
    }

    // =========================================================================
    // STAT CARD
    // =========================================================================
    private void paintStatCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub, int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString(title, x + 16, y + 26);

        // Value
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.setColor(TEXT_DARK);
        g.drawString(val, x + 16, y + 68);

        // Subtitle
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(sub, x + 16, y + 96);

        // Top right icon
        int iconX = x + w - 32;
        int iconY = y + 16;
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (type == 0) {
            // Folder (Active Projects)
            g.setColor(TEXT_LIGHT);
            g.drawRoundRect(iconX, iconY + 2, 16, 12, 3, 3);
            g.drawLine(iconX, iconY + 5, iconX + 6, iconY + 5);
        } else if (type == 1) {
            // People / Teams (Teal)
            g.setColor(new Color(0x0D, 0x94, 0x88));
            g.drawOval(iconX + 4, iconY, 6, 6);
            g.drawArc(iconX + 1, iconY + 7, 12, 7, 0, 180);
        } else if (type == 2) {
            // Pending Requests (Purple)
            g.setColor(BRAND_PURPLE);
            g.drawOval(iconX + 3, iconY, 6, 6);
            g.drawArc(iconX, iconY + 7, 10, 7, 0, 180);
            g.drawLine(iconX + 13, iconY + 4, iconX + 13, iconY + 8);
            g.drawLine(iconX + 11, iconY + 6, iconX + 15, iconY + 6);
        } else {
            // Tasks Due (Amber)
            g.setColor(new Color(0xD9, 0x77, 0x06));
            g.drawRoundRect(iconX, iconY + 2, 14, 13, 3, 3);
            g.drawLine(iconX + 3, iconY, iconX + 3, iconY + 2);
            g.drawLine(iconX + 11, iconY, iconX + 11, iconY + 2);
            g.drawLine(iconX, iconY + 6, iconX + 14, iconY + 6);
        }
    }

    // =========================================================================
    // RECOMMENDED PROJECTS SECTION
    // =========================================================================
    private void paintRecommendedProjects(Graphics2D g, int x, int y, int w) {
        int cardH = 260;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        // Header: "Recommended projects" ... "Explore all ->"
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Recommended projects", x + 20, y + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        String exp = "Explore all →";
        g.drawString(exp, x + w - g.getFontMetrics().stringWidth(exp) - 20, y + 30);

        // 2 Side-by-side Project Cards
        int pad = 20;
        int gap = 16;
        int pCardW = (w - pad * 2 - gap) / 2;
        int pCardH = 190;
        int pCardY = y + 48;

        // Card 1: GreenRoute
        paintSingleProjectCard(g, x + pad, pCardY, pCardW, pCardH,
                "GreenRoute", "Maya Patel · Sustainability",
                "Help students choose cleaner ways to get around campus.",
                new String[]{"React", "Maps API", "Node.js"},
                "2 open roles · 4 / 6 members", true, greenRouteBookmarked, 0);

        bm1X = x + pad + pCardW - 28;
        bm1Y = pCardY + 16;

        // Card 2: OpenLab
        paintSingleProjectCard(g, x + pad + pCardW + gap, pCardY, pCardW, pCardH,
                "OpenLab", "Noah Williams · Research",
                "Connect student researchers with labs and shared knowledge.",
                new String[]{"TypeScript", "PostgreSQL"},
                "2 open roles · 3 / 5 members", false, openLabBookmarked, 1);

        bm2X = x + pad + pCardW + gap + pCardW - 28;
        bm2Y = pCardY + 16;
    }

    private void paintSingleProjectCard(Graphics2D g, int x, int y, int w, int h,
                                        String title, String author, String desc,
                                        String[] skills, String footer, boolean isGreen, boolean bookmarked, int type) {
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(new Color(0xFA, 0xFB, 0xFD));
        g.fill(cardShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(cardShape);

        // Icon (Leaf or Beaker)
        int iconSize = 26;
        int iconX = x + 16;
        int iconY = y + 16;
        g.setColor(isGreen ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xE0, 0xF2, 0xFE));
        g.fillOval(iconX, iconY, iconSize, iconSize);

        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (isGreen) {
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
        g.setColor(TEXT_DARK);
        g.drawString(title, iconX + 34, iconY + 18);

        // Bookmark Icon (top right)
        int bmx = x + w - 26;
        int bmy = y + 18;
        g.setColor(bookmarked ? BRAND_PURPLE : TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.3f));
        if (bookmarked) {
            g.fillPolygon(new int[]{bmx, bmx + 10, bmx + 10, bmx + 5, bmx}, new int[]{bmy, bmy, bmy + 14, bmy + 10, bmy + 14}, 5);
        } else {
            g.drawPolygon(new int[]{bmx, bmx + 10, bmx + 10, bmx + 5, bmx}, new int[]{bmy, bmy, bmy + 14, bmy + 10, bmy + 14}, 5);
        }

        // Author / Category
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(author, x + 16, y + 54);

        // Description
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(0x47, 0x55, 0x69));
        FontMetrics dfm = g.getFontMetrics();
        List<String> dLines = Draw.wrapText(desc, dfm, w - 32);
        int dy = y + 74;
        for (String dl : dLines) {
            g.drawString(dl, x + 16, dy);
            dy += 16;
        }

        // Skill Pills
        int sx = x + 16;
        int sy = y + 116;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics sfm = g.getFontMetrics();
        for (String sk : skills) {
            int sw = sfm.stringWidth(sk) + 14;
            int sh = 22;
            RoundRectangle2D sPill = new RoundRectangle2D.Float(sx, sy, sw, sh, 6, 6);
            g.setColor(new Color(0xEE, 0xF2, 0xF6));
            g.fill(sPill);
            g.setColor(new Color(0x33, 0x41, 0x55));
            g.drawString(sk, sx + 7, sy + 15);
            sx += sw + 6;
        }

        // Footer details
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(footer, x + 16, y + 154);

        // "View project ->" Link
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        g.drawString("View project →", x + 16, y + 176);
    }

    // =========================================================================
    // RECENT ACTIVITY SECTION
    // =========================================================================
    private void paintRecentActivity(Graphics2D g, int x, int y, int w) {
        int cardH = 220;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        // Header: "Recent activity" ... "View all ->"
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Recent activity", x + 20, y + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        String va = "View all →";
        g.drawString(va, x + w - g.getFontMetrics().stringWidth(va) - 20, y + 30);

        // Activity Items
        int curY = y + 50;

        // Item 1: Priya Shah
        paintActivityItem(g, x + 20, curY, w - 40, "PS", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9),
                "Priya Shah", "", "Design", new Color(0xF3, 0xE8, 0xFF), new Color(0x7E, 0x22, 0xCE));
        curY += 52;

        // Item 2: Daniel Kim
        paintActivityItem(g, x + 20, curY, w - 40, "DK", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Daniel Kim", "Completed the event API schema · 42 min ago", "Completed", new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0xA3, 0x4A));
        curY += 52;

        // Item 3: Sofia Martinez
        paintActivityItem(g, x + 20, curY, w - 40, "SM", new Color(0xFF, 0xE4, 0xE6), new Color(0xBE, 0x12, 0x3C),
                "Sofia Martinez", "Mentioned you in \"Build event cards\" · 1 hour ago", "Task", new Color(0xF1, 0xF5, 0xF9), new Color(0x47, 0x55, 0x69));
    }

    private void paintActivityItem(Graphics2D g, int x, int y, int w,
                                   String initials, Color avBg, Color avText,
                                   String title, String subtitle,
                                   String badgeText, Color badgeBg, Color badgeTextCol) {
        // Avatar circle
        int avSize = 34;
        g.setColor(avBg);
        g.fillOval(x, y, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(avText);
        g.drawString(initials, x + 9, y + 22);

        // Texts
        int textX = x + 44;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        if (subtitle.isEmpty()) {
            g.drawString(title, textX, y + 21);
        } else {
            g.drawString(title, textX, y + 16);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(subtitle, textX, y + 31);
        }

        // Pill Badge (right side)
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics bfm = g.getFontMetrics();
        int bw = bfm.stringWidth(badgeText) + 16;
        int bh = 22;
        int bx = x + w - bw;
        int by = y + (avSize - bh) / 2;
        RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
        g.setColor(badgeBg);
        g.fill(bShape);
        g.setColor(badgeTextCol);
        g.drawString(badgeText, bx + 8, by + 15);
    }

    // =========================================================================
    // SUGGESTED TEAMMATES SECTION
    // =========================================================================
    private void paintSuggestedTeammates(Graphics2D g, int x, int y, int w) {
        int cardH = 230;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        // Header: "Suggested teammates" ... "Find more ->"
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Suggested teammates", x + 20, y + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        String fm = "Find more →";
        g.drawString(fm, x + w - g.getFontMetrics().stringWidth(fm) - 20, y + 30);

        int curY = y + 50;

        // Teammate 1: Priya Shah
        paintTeammateRow(g, x + 20, curY, w - 40, "PS", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9),
                "Priya Shah", "UX/UI designer · UW", "96% sample");
        curY += 48;

        // Teammate 2: Daniel Kim
        paintTeammateRow(g, x + 20, curY, w - 40, "DK", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Daniel Kim", "Backend developer · UW", "92% sample");
        curY += 46;

        // Illustrative note
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("Illustrative matches based on complementary skills.", x + 20, curY + 6);
        curY += 16;

        // "Find Teammates" Pill Button
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

        // 2 people icon
        int iconX = findTeammatesBtnX + (findTeammatesBtnW - 130) / 2;
        int iconY = findTeammatesBtnY + 11;
        g.setColor(TEXT_DARK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(iconX + 2, iconY, 5, 5);
        g.drawArc(iconX, iconY + 7, 9, 6, 0, 180);
        g.drawOval(iconX + 9, iconY + 2, 4, 4);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Find Teammates", iconX + 20, findTeammatesBtnY + 23);
    }

    private void paintTeammateRow(Graphics2D g, int x, int y, int w,
                                  String initials, Color avBg, Color avText,
                                  String name, String role, String matchScore) {
        // Avatar circle
        int avSize = 34;
        g.setColor(avBg);
        g.fillOval(x, y, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(avText);
        g.drawString(initials, x + 9, y + 22);

        // Name & Role
        int textX = x + 44;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString(name, textX, y + 16);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(role, textX, y + 30);

        // Match badge (green text)
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics mfm = g.getFontMetrics();
        g.setColor(new Color(0x05, 0x96, 0x69)); // #059669
        g.drawString(matchScore, x + w - mfm.stringWidth(matchScore), y + 22);
    }

    // =========================================================================
    // UPCOMING DEADLINES SECTION
    // =========================================================================
    private void paintUpcomingDeadlines(Graphics2D g, int x, int y, int w) {
        int cardH = 250;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(container);

        // Header: "Upcoming deadlines" ... "All tasks ->"
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Upcoming deadlines", x + 20, y + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        String at = "All tasks →";
        g.drawString(at, x + w - g.getFontMetrics().stringWidth(at) - 20, y + 30);

        int curY = y + 54;

        // Task 1: Finalize event API contract (Overdue)
        paintTaskItem(g, x + 20, curY, w - 40, "Finalize event API contract", "Overdue · Oct 15 · CampusConnect", 0);
        curY += 46;

        // Task 2: Build event discovery cards (Today)
        paintTaskItem(g, x + 20, curY, w - 40, "Build event discovery cards", "Today · Oct 16 · CampusConnect", 1);
        curY += 46;

        // Task 3: Accessibility review (Normal)
        paintTaskItem(g, x + 20, curY, w - 40, "Accessibility review", "Mon, Oct 19 · CampusConnect", 2);
        curY += 50;

        // Bottom Strip: "3 unread conversations" ... "View Messages ->"
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("3 unread conversations", x + 20, curY);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        String vm = "View Messages →";
        g.drawString(vm, x + w - g.getFontMetrics().stringWidth(vm) - 20, curY);
    }

    private void paintTaskItem(Graphics2D g, int x, int y, int w, String title, String subtitle, int statusType) {
        // Calendar icon
        int iconX = x;
        int iconY = y + 2;
        g.setColor(TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(iconX, iconY, 13, 12, 2, 2);
        g.drawLine(iconX + 3, iconY - 2, iconX + 3, iconY);
        g.drawLine(iconX + 10, iconY - 2, iconX + 10, iconY);
        g.drawLine(iconX, iconY + 4, iconX + 13, iconY + 4);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString(title, x + 22, y + 12);

        // Subtitle with color-coded status
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        if (statusType == 0) {
            // Overdue in red
            g.setColor(new Color(0xEF, 0x44, 0x44));
            g.drawString("Overdue", x + 22, y + 26);
            g.setColor(TEXT_MUTED);
            g.drawString(" · Oct 15 · CampusConnect", x + 68, y + 26);
        } else if (statusType == 1) {
            // Today in amber
            g.setColor(new Color(0xF5, 0x9E, 0x0B));
            g.drawString("Today", x + 22, y + 26);
            g.setColor(TEXT_MUTED);
            g.drawString(" · Oct 16 · CampusConnect", x + 56, y + 26);
        } else {
            g.setColor(TEXT_MUTED);
            g.drawString(subtitle, x + 22, y + 26);
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

    private void paintContentScrollbar(Graphics2D g, int x, int y, int w, int h) {
        if (totalContentHeight <= h) return;
        float ratio = (float) h / (float) totalContentHeight;
        int thumbH = Math.max(36, Math.round(h * ratio));
        int maxScroll = totalContentHeight - h;
        float pct = scrollY / (float) maxScroll;
        int thumbY = y + Math.round((h - thumbH) * pct);

        RoundRectangle2D thumb = new RoundRectangle2D.Float(x, thumbY, w, thumbH, w, w);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.fill(thumb);
    }

    private void paintToast(Graphics2D g, int width, int height) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(toastMessage) + 36;
        int th = 36;
        int tx = (width - tw) / 2;
        int ty = height - 60;

        RoundRectangle2D tShape = new RoundRectangle2D.Float(tx, ty, tw, th, 18, 18);
        g.setColor(new Color(0x1E, 0x29, 0x3B));
        g.fill(tShape);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.setStroke(new BasicStroke(1f));
        g.draw(tShape);

        g.setColor(Color.WHITE);
        g.drawString("✓  " + toastMessage, tx + 18, ty + 22);
    }

    // =========================================================================
    // EVENT HANDLING
    // =========================================================================
    public boolean onClick(int mx, int my) {
        // Return to Landing Page
        if (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH) {
            if (listener != null) listener.returnToLanding();
            return true;
        }

        // Sidebar Navigation click
        int sideW = 230;
        int padX = 20;
        int curY = 20 + 46 + 68 + 16;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (NavItem item : NavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                activeNav = item;
                showToast("Switched to " + item.label);
                return true;
            }
            curY += itemH + 2;
        }

        // Search bar focus
        searchFocused = (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH);

        // "+ Create Project" primary button
        if (mx >= createProjBtnX && mx <= createProjBtnX + createProjBtnW &&
                my >= createProjBtnY && my <= createProjBtnY + createProjBtnH) {
            if (listener != null) {
                listener.openModal("Create New Project", "Publish a project, define required roles, and recruit teammates.", true);
            }
            return true;
        }

        // Top "+ Create project" ghost button
        if (mx >= topCreateBtnX && mx <= topCreateBtnX + topCreateBtnW &&
                my >= topCreateBtnY && my <= topCreateBtnY + topCreateBtnH) {
            if (listener != null) {
                listener.openModal("Create New Project", "Publish a project, define required roles, and recruit teammates.", true);
            }
            return true;
        }

        // "Complete profile ->" banner button
        if (mx >= compProfileBtnX && mx <= compProfileBtnX + compProfileBtnW &&
                my >= compProfileBtnY && my <= compProfileBtnY + compProfileBtnH) {
            activeNav = NavItem.MY_PROFILE;
            showToast("Opening Alex Morgan's profile editor...");
            return true;
        }

        // "Find Teammates" right column button
        if (mx >= findTeammatesBtnX && mx <= findTeammatesBtnX + findTeammatesBtnW &&
                my >= findTeammatesBtnY && my <= findTeammatesBtnY + findTeammatesBtnH) {
            activeNav = NavItem.FIND_TEAMMATES;
            showToast("Searching verified peers at University of Washington...");
            return true;
        }

        // Bookmarks toggles
        if (mx >= bm1X - 6 && mx <= bm1X + bm1Size + 6 && my >= bm1Y - 6 && my <= bm1Y + bm1Size + 6) {
            greenRouteBookmarked = !greenRouteBookmarked;
            showToast(greenRouteBookmarked ? "GreenRoute saved to Bookmarks" : "Removed GreenRoute from Bookmarks");
            return true;
        }

        if (mx >= bm2X - 6 && mx <= bm2X + bm2Size + 6 && my >= bm2Y - 6 && my <= bm2Y + bm2Size + 6) {
            openLabBookmarked = !openLabBookmarked;
            showToast(openLabBookmarked ? "OpenLab saved to Bookmarks" : "Removed OpenLab from Bookmarks");
            return true;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p1 = returnBtnHovered;
        returnBtnHovered = (mx >= returnBtnX && mx <= returnBtnX + returnBtnW && my >= returnBtnY && my <= returnBtnY + returnBtnH);
        if (returnBtnHovered != p1) changed = true;

        boolean p2 = createProjBtnHovered;
        createProjBtnHovered = (mx >= createProjBtnX && mx <= createProjBtnX + createProjBtnW &&
                my >= createProjBtnY && my <= createProjBtnY + createProjBtnH);
        if (createProjBtnHovered != p2) changed = true;

        boolean p3 = topCreateBtnHovered;
        topCreateBtnHovered = (mx >= topCreateBtnX && mx <= topCreateBtnX + topCreateBtnW &&
                my >= topCreateBtnY && my <= topCreateBtnY + topCreateBtnH);
        if (topCreateBtnHovered != p3) changed = true;

        boolean p4 = compProfileBtnHovered;
        compProfileBtnHovered = (mx >= compProfileBtnX && mx <= compProfileBtnX + compProfileBtnW &&
                my >= compProfileBtnY && my <= compProfileBtnY + compProfileBtnH);
        if (compProfileBtnHovered != p4) changed = true;

        boolean p5 = findTeammatesBtnHovered;
        findTeammatesBtnHovered = (mx >= findTeammatesBtnX && mx <= findTeammatesBtnX + findTeammatesBtnW &&
                my >= findTeammatesBtnY && my <= findTeammatesBtnY + findTeammatesBtnH);
        if (findTeammatesBtnHovered != p5) changed = true;

        // Sidebar Hover
        NavItem oldHover = hoveredNav;
        hoveredNav = null;
        int sideW = 230;
        int padX = 20;
        int curY = 20 + 46 + 68 + 16;
        int itemH = 38;
        int itemW = sideW - padX * 2;

        for (NavItem item : NavItem.values()) {
            if (mx >= padX && mx <= padX + itemW && my >= curY && my <= curY + itemH) {
                hoveredNav = item;
                break;
            }
            curY += itemH + 2;
        }
        if (hoveredNav != oldHover) changed = true;

        return changed || returnBtnHovered || createProjBtnHovered || topCreateBtnHovered || compProfileBtnHovered || findTeammatesBtnHovered;
    }

    public void onMouseWheel(int wheelRotation, int screenH) {
        int maxScroll = Math.max(0, totalContentHeight - (screenH - 60));
        targetScrollY = Math.max(0, Math.min(targetScrollY + wheelRotation * 45, maxScroll));
    }

    public boolean onKey(KeyEvent e) {
        if (!searchFocused) return false;

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
            }
            return true;
        }

        char c = e.getKeyChar();
        if (c >= 32 && c <= 126 && searchText.length() < 50) {
            searchText += c;
            return true;
        }

        return false;
    }
}
