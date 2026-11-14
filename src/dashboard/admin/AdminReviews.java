import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard - Reviews & Feedback Management View.
 * Displays all peer ratings, team member evaluations, reported reviews, and
 * content moderation queues.
 * Fully dynamic and reactive:
 * - 4 KPI Metrics cards (Average Rating, Team Member Reviews, Reported Reviews
 * Queue, Positive Satisfaction Rate)
 * - Live filter tabs with real-time counters (All, Team Member Reviews, User
 * Ratings, Reported Reviews)
 * - Interactive filter dropdowns (Rating 1★-5★, Review Type, Moderation Status)
 * with floating popovers
 * - Instant search by reviewer, student, project name, or feedback text with ✕
 * clear button
 * - Filter reset button when any filter is active
 * - "+ Add review" modal integration for recording new peer evaluations
 * - Real CSV export ("Export feedback") with live data
 * - Interactive moderation actions directly on cards:
 * - "Delete Inappropriate Feedback" (instant permanent purge & audit log)
 * - "Approve Review" (clears reported flag & publishes)
 * - "Hide / Unhide" (toggles visibility)
 * - "Warn Author" (logs official moderation warning)
 * - "Inspect Details" (opens full modal inspector)
 * - Dynamic pagination (Previous, Page 1..N, Next) with true page slicing
 * - Multi-viewport responsiveness with dynamic computed height.
 */
public class AdminReviews {

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

        public DropdownItemHitbox(int index, String label, int x, int y, int w, int h, boolean hovered,
                boolean selected) {
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

    public static class ReviewCardHitbox {
        public AdminDataStore.ReviewModel review;
        public int cardX, cardY, cardW, cardH;
        public boolean cardHovered = false;

        public int deleteX, deleteY, deleteW, deleteH;
        public boolean deleteHovered = false;

        public int approveX, approveY, approveW, approveH;
        public boolean approveHovered = false;

        public int hideX, hideY, hideW, hideH;
        public boolean hideHovered = false;

        public int warnX, warnY, warnW, warnH;
        public boolean warnHovered = false;

        public int inspectX, inspectY, inspectW, inspectH;
        public boolean inspectHovered = false;
    }

    private final TabHitbox[] tabBoxes = new TabHitbox[4];
    private final List<ReviewCardHitbox> cardHitboxes = new ArrayList<>();
    private final List<DropdownItemHitbox> popoverHitboxes = new ArrayList<>();
    private final List<PageBtnHitbox> pageHitboxes = new ArrayList<>();

    private int selectedTab = 0; // 0=All, 1=Team Reviews, 2=User Ratings, 3=Reported
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 320, searchH = 36;
    private int clearSearchX, clearSearchY, clearSearchSize = 18;
    private boolean clearSearchHovered = false;

    // Filter dropdowns
    private final String[] starOptions = { "All ratings", "5 Stars ★★★★★", "4 Stars ★★★★☆", "3 Stars ★★★☆☆",
            "2 Stars ★★☆☆☆", "1 Star ★☆☆☆☆" };
    private int starFilterIdx = 0;
    private int starDropX, starDropY, starDropW = 120, starDropH = 36;
    private boolean starDropHovered = false;

    private final String[] typeOptions = { "All types", "Team Member Review", "User Rating", "Peer Collaboration" };
    private int typeFilterIdx = 0;
    private int typeDropX, typeDropY, typeDropW = 145, typeDropH = 36;
    private boolean typeDropHovered = false;

    private final String[] statusOptions = { "All statuses", "Published", "Reported", "Under review", "Hidden" };
    private int statusFilterIdx = 0;
    private int statusDropX, statusDropY, statusDropW = 120, statusDropH = 36;
    private boolean statusDropHovered = false;

    // Active popover: 0=None, 1=Rating, 2=Type, 3=Status
    private int activePopover = 0;

    // Reset filters button
    private int resetBtnX, resetBtnY, resetBtnW = 96, resetBtnH = 26;
    private boolean resetBtnHovered = false;

    // Header buttons
    private int addReviewBtnX, addReviewBtnY, addReviewBtnW = 120, addReviewBtnH = 36;
    private boolean addReviewHovered = false;

    // Pagination
    private int currentPage = 1;
    private final int pageSize = 6;
    private int prevBtnX, prevBtnY, prevBtnW = 76, prevBtnH = 32;
    private boolean prevHovered = false;
    private boolean prevEnabled = false;

    private int nextBtnX, nextBtnY, nextBtnW = 58, nextBtnH = 32;
    private boolean nextHovered = false;
    private boolean nextEnabled = false;

    // Empty state reset button
    private int emptyResetBtnX, emptyResetBtnY, emptyResetBtnW = 140, emptyResetBtnH = 34;
    private boolean emptyResetHovered = false;

    public AdminReviews() {
        for (int i = 0; i < tabBoxes.length; i++) {
            tabBoxes[i] = new TabHitbox();
        }
    }

    public void paint(Graphics2D g, int x, int y, int w, int h, AdminDashboard dashboard) {
        cardHitboxes.clear();
        int curY = y + 24;
        int padX = 24;
        int contentW = w - padX * 2;

        // 1. Header Section
        curY = paintHeader(g, x + padX, curY, contentW, dashboard);
        curY += 20;

        // 2. Metrics Summary Cards (4 KPI Cards)
        curY = paintMetricsSummary(g, x + padX, curY, contentW);
        curY += 22;

        // 3. Filter Tabs & Controls Bar
        curY = paintTabsAndControls(g, x + padX, curY, contentW);
        curY += 16;

        // 4. Feedback Cards Stream
        curY = paintReviewsStream(g, x + padX, curY, contentW, dashboard);

        // 5. Pagination Bar
        curY = paintPagination(g, x + padX, curY, contentW);

        // 6. Floating Dropdown Popover (drawn on top of all layers)
        if (activePopover != 0) {
            paintActivePopover(g);
        }
    }

    private int paintHeader(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        g.setFont(AdminTheme.FONT_HEADING);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Reviews & Feedback", x, y + 24);

        int titleW = hfm.stringWidth("Reviews & Feedback");

        // Total count pill
        int pillX = x + titleW + 12;
        int pillY = y + 7;
        int totalRev = AdminDataStore.getInstance().getTotalReviewsCount();
        String totalLabel = String.format("%,d evaluations", totalRev);
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
        g.drawString("Monitor user ratings, peer collaboration feedback, reported reviews, and content moderation.", x,
                y + 48);

        // Action Buttons - Add review button removed per user request
        addReviewBtnX = -9999;
        addReviewBtnY = -9999;
        return y + 60;
    }

    private int paintMetricsSummary(Graphics2D g, int x, int y, int w) {
        int cardGap = 14;
        int cardCount = 4;
        int cardW = Math.max(160, (w - (cardCount - 1) * cardGap) / cardCount);
        int cardH = 88;

        AdminDataStore store = AdminDataStore.getInstance();
        double avgRating = store.getAverageRating();
        int teamReviews = store.getTeamReviewsCount();
        int reportedQueue = store.getReportedReviewsCount();
        int positiveRate = store.getPositiveRatingPercentage();

        // 1. Average Rating Card
        paintKpiCard(g, x, y, cardW, cardH, "AVERAGE RATING", String.format("%.1f", avgRating) + " ★",
                "Based on peer ratings", "★ Top Tier", new Color(0xFE, 0xF3, 0xC7), AdminTheme.BRAND_AMBER);

        // 2. Team Member Reviews Card
        paintKpiCard(g, x + (cardW + cardGap), y, cardW, cardH, "TEAM MEMBER REVIEWS", String.valueOf(teamReviews),
                "Verified collaborations", "🤝 Peer Sprints", new Color(0xEE, 0xF2, 0xFF), AdminTheme.BRAND_PRIMARY);

        // 3. Reported Reviews Card
        Color repBg = reportedQueue > 0 ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xDC, 0xFC, 0xE7);
        Color repFg = reportedQueue > 0 ? AdminTheme.ACCENT_RED : AdminTheme.BRAND_GREEN_DARK;
        String repBadge = reportedQueue > 0 ? "⚠️ Moderation" : "✓ All Clear";
        paintKpiCard(g, x + (cardW + cardGap) * 2, y, cardW, cardH, "REPORTED REVIEWS", String.valueOf(reportedQueue),
                reportedQueue > 0 ? "Flagged feedback pending" : "No pending reports", repBadge, repBg, repFg);

        // 4. Positive Satisfaction Card
        paintKpiCard(g, x + (cardW + cardGap) * 3, y, cardW, cardH, "SATISFACTION RATE", positiveRate + "%",
                "Rated 4★ and 5★ stars", "✓ High Trust", new Color(0xDC, 0xFC, 0xE7), AdminTheme.BRAND_GREEN_DARK);

        return y + cardH;
    }

    private void paintKpiCard(Graphics2D g, int cx, int cy, int cw, int ch, String title, String val, String sub,
            String badge, Color badgeBg, Color badgeFg) {
        RoundRectangle2D box = new RoundRectangle2D.Float(cx, cy, cw, ch, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        // Title
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(title, cx + 14, cy + 22);

        // Badge pill on top right
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics bfm = g.getFontMetrics();
        int bw = bfm.stringWidth(badge) + 12;
        int bx = cx + cw - bw - 12;
        int by = cy + 11;
        RoundRectangle2D bpill = new RoundRectangle2D.Float(bx, by, bw, 18, 9, 9);
        g.setColor(badgeBg);
        g.fill(bpill);
        g.setColor(badgeFg);
        g.drawString(badge, bx + 6, by + 13);

        // Value
        g.setFont(AdminTheme.FONT_BOLD_20);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(val, cx + 14, cy + 54);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(sub, cx + 14, cy + 74);
    }

    private int paintTabsAndControls(Graphics2D g, int x, int y, int w) {
        AdminDataStore ds = AdminDataStore.getInstance();
        int totalCount = ds.getAllReviews().size();
        int teamCount = ds.getTeamReviewsCount();
        int userCount = ds.getUserRatingsCount();
        int reportedCount = ds.getReportedReviewsCount();

        String[] tabLabels = {
                "All Reviews (" + totalCount + ")",
                "Team Member Reviews (" + teamCount + ")",
                "User Ratings (" + userCount + ")",
                "Reported Feedback (" + reportedCount + ")"
        };

        // Render Tabs
        int curX = x;
        for (int i = 0; i < 4; i++) {
            boolean isSel = (i == selectedTab);
            g.setFont(isSel ? AdminTheme.FONT_BOLD_12 : AdminTheme.FONT_REG_12);
            FontMetrics tfm = g.getFontMetrics();
            int tabW = tfm.stringWidth(tabLabels[i]) + 24;
            int tabH = 34;

            RoundRectangle2D tabShape = new RoundRectangle2D.Float(curX, y, tabW, tabH, 8, 8);
            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.fill(tabShape);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.WHITE);
                g.fill(tabShape);
                g.setColor(AdminTheme.BORDER_COLOR);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(tabShape);

                // If reported tab has items, highlight text red/amber
                if (i == 3 && reportedCount > 0) {
                    g.setColor(AdminTheme.ACCENT_RED);
                } else {
                    g.setColor(AdminTheme.TEXT_DARK);
                }
            }

            g.drawString(tabLabels[i], curX + 12, y + 21);

            tabBoxes[i].x = curX;
            tabBoxes[i].y = y;
            tabBoxes[i].w = tabW;
            tabBoxes[i].h = tabH;
            tabBoxes[i].label = tabLabels[i];

            curX += tabW + 8;
        }

        // Controls Row
        int filterY = y + 44;
        searchX = x;
        searchY = filterY;
        searchW = Math.min(300, Math.max(220, w / 3));
        searchH = 36;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.8f : 1.2f));
        g.draw(sBox);

        AdminTheme.drawSearchIcon(g, searchX + 11, searchY + 11, 14, AdminTheme.TEXT_MUTED);

        // Search text / placeholder
        g.setFont(AdminTheme.FONT_REG_12);
        if (searchQuery.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Search feedback, student, project...", searchX + 32, searchY + 22);
        } else {
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(searchQuery, searchX + 32, searchY + 22);

            // ✕ Clear search button
            clearSearchX = searchX + searchW - 24;
            clearSearchY = searchY + 9;
            clearSearchSize = 18;
            if (clearSearchHovered) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fillOval(clearSearchX, clearSearchY, clearSearchSize, clearSearchSize);
            }
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("✕", clearSearchX + 4, clearSearchY + 13);
        }

        // Dropdown 1: Rating filter
        int dropX = searchX + searchW + 12;
        starDropX = dropX;
        starDropY = filterY;
        starDropW = 120;
        starDropH = 36;
        paintDropdownBtn(g, starDropX, starDropY, starDropW, starDropH, starOptions[starFilterIdx],
                (activePopover == 1), starDropHovered);

        // Dropdown 2: Review Type
        dropX += starDropW + 10;
        typeDropX = dropX;
        typeDropY = filterY;
        typeDropW = 145;
        typeDropH = 36;
        paintDropdownBtn(g, typeDropX, typeDropY, typeDropW, typeDropH, typeOptions[typeFilterIdx],
                (activePopover == 2), typeDropHovered);

        // Dropdown 3: Status
        dropX += typeDropW + 10;
        statusDropX = dropX;
        statusDropY = filterY;
        statusDropW = 120;
        statusDropH = 36;
        paintDropdownBtn(g, statusDropX, statusDropY, statusDropW, statusDropH, statusOptions[statusFilterIdx],
                (activePopover == 3), statusDropHovered);

        // Reset Filters Button
        boolean filtersActive = !searchQuery.isEmpty() || selectedTab != 0 || starFilterIdx != 0 || typeFilterIdx != 0
                || statusFilterIdx != 0;
        if (filtersActive) {
            dropX += statusDropW + 12;
            resetBtnX = dropX;
            resetBtnY = filterY + 5;
            resetBtnW = 96;
            resetBtnH = 26;

            RoundRectangle2D rBox = new RoundRectangle2D.Float(resetBtnX, resetBtnY, resetBtnW, resetBtnH, 6, 6);
            g.setColor(resetBtnHovered ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xFF, 0xF1, 0xF2));
            g.fill(rBox);
            g.setColor(AdminTheme.ACCENT_RED);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.drawString("✕ Reset filters", resetBtnX + 10, resetBtnY + 17);
        }

        return filterY + searchH;
    }

    private void paintDropdownBtn(Graphics2D g, int bx, int by, int bw, int bh, String label, boolean open,
            boolean hovered) {
        RoundRectangle2D box = new RoundRectangle2D.Float(bx, by, bw, bh, 8, 8);
        g.setColor(open ? new Color(0xDC, 0xFC, 0xE7) : (hovered ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE));
        g.fill(box);
        g.setColor(open ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(open ? 1.6f : 1.2f));
        g.draw(box);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_DARK);
        // Truncate label if too wide
        String drawLbl = label;
        if (drawLbl.length() > 14 && !drawLbl.startsWith("All")) {
            drawLbl = drawLbl.substring(0, 13) + "…";
        }
        g.drawString(drawLbl, bx + 12, by + 22);

        AdminTheme.drawChevronDown(g, bx + bw - 18, by + 14, 10, AdminTheme.TEXT_MUTED, open ? 180f : 0f);
    }

    private List<AdminDataStore.ReviewModel> getFilteredReviews() {
        return AdminDataStore.getInstance().filterReviews(
                searchQuery,
                selectedTab,
                starOptions[starFilterIdx],
                typeOptions[typeFilterIdx],
                statusOptions[statusFilterIdx]);
    }

    private int paintReviewsStream(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.ReviewModel> filtered = getFilteredReviews();

        if (filtered.isEmpty()) {
            return paintEmptyState(g, x, y, w);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        if (currentPage > totalPages)
            currentPage = totalPages;

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        int curY = y;

        for (int i = startIndex; i < endIndex; i++) {
            AdminDataStore.ReviewModel rev = filtered.get(i);
            curY = paintReviewCard(g, x, curY, w, rev, dashboard);
            curY += 14;
        }

        return curY;
    }

    private int paintReviewCard(Graphics2D g, int cx, int cy, int cw, AdminDataStore.ReviewModel rev,
            AdminDashboard dashboard) {
        boolean isReported = "Reported".equalsIgnoreCase(rev.status) || "Under review".equalsIgnoreCase(rev.status);
        boolean isHidden = "Hidden".equalsIgnoreCase(rev.status);

        // Compute card height dynamically: reported cards have an alert box (+42px)
        int cardH = isReported ? 172 : 136;

        RoundRectangle2D cardBox = new RoundRectangle2D.Float(cx, cy, cw, cardH, 12, 12);
        g.setColor(isHidden ? new Color(0xFA, 0xFA, 0xFA) : Color.WHITE);
        g.fill(cardBox);

        // Border: highlight red/amber if reported
        if (isReported) {
            g.setColor(new Color(0xFE, 0xCA, 0xCA));
            g.setStroke(new BasicStroke(1.5f));
        } else {
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
        }
        g.draw(cardBox);

        ReviewCardHitbox hb = new ReviewCardHitbox();
        hb.review = rev;
        hb.cardX = cx;
        hb.cardY = cy;
        hb.cardW = cw;
        hb.cardH = cardH;

        int row1Y = cy + 18;

        // 1. Reviewer Avatar (Circle with initials)
        int avSize = 34;
        int avX = cx + 18;
        int avY = cy + 14;
        g.setColor(AdminTheme.BRAND_TINT);
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        String inits = rev.reviewerInitials != null ? rev.reviewerInitials : "U";
        FontMetrics ifm = g.getFontMetrics();
        g.drawString(inits, avX + (avSize - ifm.stringWidth(inits)) / 2, avY + 22);

        // Reviewer Name & Context
        int textX = avX + avSize + 12;
        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(rev.reviewerName, textX, row1Y + 4);

        int rwNameW = g.getFontMetrics().stringWidth(rev.reviewerName);
        int tagX = textX + rwNameW + 8;

        // Arrow and Reviewee
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("evaluated", tagX, row1Y + 3);
        tagX += 54;

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString(rev.revieweeName, tagX, row1Y + 3);
        tagX += g.getFontMetrics().stringWidth(rev.revieweeName) + 12;

        // Project Pill
        if (rev.projectName != null && !rev.projectName.isEmpty()) {
            g.setFont(AdminTheme.FONT_BOLD_10);
            FontMetrics pfm = g.getFontMetrics();
            int pw = pfm.stringWidth(rev.projectName) + 16;
            RoundRectangle2D ppill = new RoundRectangle2D.Float(tagX, row1Y - 11, pw, 20, 10, 10);
            g.setColor(new Color(0xEE, 0xF2, 0xFF));
            g.fill(ppill);
            g.setColor(AdminTheme.BRAND_PRIMARY);
            g.drawString(rev.projectName, tagX + 8, row1Y + 3);
            tagX += pw + 8;
        }

        // Role / Context Pill
        if (rev.roleOrContext != null && !rev.roleOrContext.isEmpty() && tagX < cx + cw - 280) {
            g.setFont(AdminTheme.FONT_REG_10);
            FontMetrics rfm = g.getFontMetrics();
            int rw = rfm.stringWidth(rev.roleOrContext) + 14;
            RoundRectangle2D rpill = new RoundRectangle2D.Float(tagX, row1Y - 11, rw, 20, 10, 10);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(rpill);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(rev.roleOrContext, tagX + 7, row1Y + 3);
        }

        // Right side badges: Star Rating + Status Pill
        int rightBadgeX = cx + cw - 18;

        // Status Pill
        int sW = 84;
        int sH = 22;
        int sX = rightBadgeX - sW;
        int sY = row1Y - 12;
        RoundRectangle2D sp = new RoundRectangle2D.Float(sX, sY, sW, sH, 11, 11);

        if ("Published".equalsIgnoreCase(rev.status)) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("✓ Published", sX + 11, sY + 15);
        } else if ("Reported".equalsIgnoreCase(rev.status)) {
            g.setColor(new Color(0xFE, 0xE2, 0xE2));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.ACCENT_RED);
            g.drawString("⚠️ Reported", sX + 11, sY + 15);
        } else if ("Under review".equalsIgnoreCase(rev.status)) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.drawString("⏳ Under review", sX + 7, sY + 15);
        } else {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("👁️ Hidden", sX + 16, sY + 15);
        }

        // Stars display (Gold stars)
        int starEndX = sX - 16;
        paintStarRating(g, starEndX - 88, sY + 3, rev.rating);

        // Second Row: Review Content Text
        int contentY = cy + 56;
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(isHidden ? AdminTheme.TEXT_MUTED : AdminTheme.TEXT_DARK);

        String text = (rev.reviewText != null) ? rev.reviewText : "";
        // Fit text within width
        FontMetrics tfm = g.getFontMetrics();
        int maxTextW = cw - 36;
        if (tfm.stringWidth("\"" + text + "\"") > maxTextW) {
            String truncated = text;
            while (!truncated.isEmpty() && tfm.stringWidth("\"" + truncated + "…\"") > maxTextW) {
                truncated = truncated.substring(0, truncated.length() - 1);
            }
            text = truncated + "…";
        }
        g.drawString("\"" + text + "\"", cx + 18, contentY);

        int alertOffset = 0;

        // Third Row: If Reported Alert Callout Box
        if (isReported) {
            int alertY = cy + 74;
            int alertW = cw - 36;
            int alertH = 32;
            RoundRectangle2D alertBox = new RoundRectangle2D.Float(cx + 18, alertY, alertW, alertH, 6, 6);
            g.setColor(new Color(0xFF, 0xF1, 0xF2));
            g.fill(alertBox);
            g.setColor(new Color(0xFE, 0xCD, 0xD3));
            g.setStroke(new BasicStroke(1.0f));
            g.draw(alertBox);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.ACCENT_RED);
            g.drawString("⚠️ Flagged for moderation:", cx + 28, alertY + 20);

            String reason = (rev.reportReason != null) ? rev.reportReason : "Reported as inappropriate content";
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(new Color(0x9F, 0x12, 0x39));
            g.drawString("\"" + reason + "\"" + (rev.reportedBy != null ? " · by " + rev.reportedBy : ""), cx + 182,
                    alertY + 20);

            alertOffset = 38;
        }

        // Bottom Row: Date / Verification / Helpful Votes + Action Buttons
        int bottomY = cy + 78 + alertOffset;

        // Meta info
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String meta = rev.date + "  ·  " + rev.reviewType;
        if (rev.verifiedCollaboration)
            meta += "  ·  ✓ Verified Sprint";
        if (rev.helpfulVotes > 0)
            meta += "  ·  👍 " + rev.helpfulVotes + " helpful";
        g.drawString(meta, cx + 18, bottomY + 16);

        // Action Buttons on Right
        int btnRight = cx + cw - 18;

        // 1. "Delete Feedback" / "Delete Inappropriate Feedback" (Crimson Button)
        hb.deleteW = 124;
        hb.deleteH = 28;
        hb.deleteX = btnRight - hb.deleteW;
        hb.deleteY = bottomY;

        RoundRectangle2D delBox = new RoundRectangle2D.Float(hb.deleteX, hb.deleteY, hb.deleteW, hb.deleteH, 6, 6);
        g.setColor(hb.deleteHovered ? new Color(0xFE, 0xE2, 0xE2) : Color.WHITE);
        g.fill(delBox);
        g.setColor(AdminTheme.ACCENT_RED);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(delBox);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("🗑 Delete feedback", hb.deleteX + 11, hb.deleteY + 18);

        int prevBtnX = hb.deleteX;

        // 2. "Approve Review" Button (if reported)
        if (isReported) {
            hb.approveW = 105;
            hb.approveH = 28;
            hb.approveX = prevBtnX - 8 - hb.approveW;
            hb.approveY = bottomY;

            RoundRectangle2D appBox = new RoundRectangle2D.Float(hb.approveX, hb.approveY, hb.approveW, hb.approveH, 6,
                    6);
            g.setColor(hb.approveHovered ? AdminTheme.BRAND_GREEN_DARK.darker() : AdminTheme.BRAND_GREEN_DARK);
            g.fill(appBox);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(Color.WHITE);
            g.drawString("✓ Approve & keep", hb.approveX + 8, hb.approveY + 18);

            prevBtnX = hb.approveX;
        }

        // 3. "Hide / Unhide" Button
        String hideLabel = isHidden ? "👁 Unhide" : "Hide";
        g.setFont(AdminTheme.FONT_REG_11);
        hb.hideW = isHidden ? 78 : 58;
        hb.hideH = 28;
        hb.hideX = prevBtnX - 8 - hb.hideW;
        hb.hideY = bottomY;

        RoundRectangle2D hideBox = new RoundRectangle2D.Float(hb.hideX, hb.hideY, hb.hideW, hb.hideH, 6, 6);
        g.setColor(hb.hideHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(hideBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(hideBox);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(hideLabel, hb.hideX + 14, hb.hideY + 18);

        prevBtnX = hb.hideX;

        // 4. "Warn Author" Button (if reported)
        if (isReported) {
            hb.warnW = 90;
            hb.warnH = 28;
            hb.warnX = prevBtnX - 8 - hb.warnW;
            hb.warnY = bottomY;

            RoundRectangle2D warnBox = new RoundRectangle2D.Float(hb.warnX, hb.warnY, hb.warnW, hb.warnH, 6, 6);
            g.setColor(hb.warnHovered ? new Color(0xFE, 0xF3, 0xC7) : Color.WHITE);
            g.fill(warnBox);
            g.setColor(new Color(0xD9, 0x77, 0x06));
            g.setStroke(new BasicStroke(1.0f));
            g.draw(warnBox);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.drawString("⚠️ Warn author", hb.warnX + 8, hb.warnY + 18);

            prevBtnX = hb.warnX;
        }

        // 5. "Inspect Details" Button
        hb.inspectW = 72;
        hb.inspectH = 28;
        hb.inspectX = prevBtnX - 8 - hb.inspectW;
        hb.inspectY = bottomY;

        RoundRectangle2D insBox = new RoundRectangle2D.Float(hb.inspectX, hb.inspectY, hb.inspectW, hb.inspectH, 6, 6);
        g.setColor(hb.inspectHovered ? new Color(0xEE, 0xF2, 0xFF) : Color.WHITE);
        g.fill(insBox);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(insBox);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("Inspect →", hb.inspectX + 11, hb.inspectY + 18);

        cardHitboxes.add(hb);
        return cy + cardH;
    }

    private void paintStarRating(Graphics2D g, int x, int y, int rating) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        int starSpacing = 16;
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                g.setColor(AdminTheme.BRAND_AMBER);
            } else {
                g.setColor(new Color(0xE2, 0xE8, 0xF0));
            }
            g.drawString("★", x + (i - 1) * starSpacing, y + 12);
        }
    }

    private int paintEmptyState(Graphics2D g, int x, int y, int w) {
        int emptyH = 220;
        RoundRectangle2D box = new RoundRectangle2D.Float(x, y, w, emptyH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        int midX = x + w / 2;
        int curY = y + 42;

        // Large star outline icon
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        FontMetrics ifm = g.getFontMetrics();
        g.drawString("★", midX - ifm.stringWidth("★") / 2, curY + 28);
        curY += 56;

        // Headline
        g.setFont(AdminTheme.FONT_BOLD_15);
        g.setColor(AdminTheme.TEXT_DARK);
        String msg = "No reviews found matching criteria";
        FontMetrics mfm = g.getFontMetrics();
        g.drawString(msg, midX - mfm.stringWidth(msg) / 2, curY);
        curY += 22;

        // Hint
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String hint = "Try adjusting your search query, star rating, or moderation filters.";
        FontMetrics hfm = g.getFontMetrics();
        g.drawString(hint, midX - hfm.stringWidth(hint) / 2, curY);
        curY += 28;

        // "Reset all filters" button
        emptyResetBtnW = 140;
        emptyResetBtnH = 34;
        emptyResetBtnX = midX - emptyResetBtnW / 2;
        emptyResetBtnY = curY;

        RoundRectangle2D rBtn = new RoundRectangle2D.Float(emptyResetBtnX, emptyResetBtnY, emptyResetBtnW,
                emptyResetBtnH, 8, 8);
        g.setColor(emptyResetHovered ? AdminTheme.BRAND_GREEN_DARK.darker() : AdminTheme.BRAND_GREEN_DARK);
        g.fill(rBtn);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        String btnText = "Reset all filters";
        FontMetrics bfm = g.getFontMetrics();
        g.drawString(btnText, emptyResetBtnX + (emptyResetBtnW - bfm.stringWidth(btnText)) / 2, emptyResetBtnY + 21);

        return y + emptyH;
    }

    private int paintPagination(Graphics2D g, int x, int y, int w) {
        List<AdminDataStore.ReviewModel> filtered = getFilteredReviews();
        int totalItems = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages)
            currentPage = totalPages;

        int fromIdx = (totalItems == 0) ? 0 : ((currentPage - 1) * pageSize + 1);
        int toIdx = Math.min(currentPage * pageSize, totalItems);

        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Showing " + fromIdx + "-" + toIdx + " of " + totalItems + " reviews", x, y + 20);

        int curX = x + w;

        // Next button
        nextEnabled = (currentPage < totalPages);
        nextBtnX = curX - nextBtnW;
        nextBtnY = y + 2;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next", false, nextEnabled, nextHovered);

        curX = nextBtnX - 8;
        pageHitboxes.clear();

        // Dynamic page buttons
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

    private void paintPageBtn(Graphics2D g, int bx, int by, int bw, int bh, String text, boolean active,
            boolean enabled, boolean hovered) {
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

    private void paintActivePopover(Graphics2D g) {
        popoverHitboxes.clear();

        int popX = 0, popY = 0, popW = 160;
        String[] options = new String[0];
        int selectedIndex = 0;

        if (activePopover == 1) { // Rating popover
            popX = starDropX;
            popY = starDropY + starDropH + 4;
            popW = 150;
            options = starOptions;
            selectedIndex = starFilterIdx;
        } else if (activePopover == 2) { // Type popover
            popX = typeDropX;
            popY = typeDropY + typeDropH + 4;
            popW = 175;
            options = typeOptions;
            selectedIndex = typeFilterIdx;
        } else if (activePopover == 3) { // Status popover
            popX = statusDropX;
            popY = statusDropY + statusDropH + 4;
            popW = 140;
            options = statusOptions;
            selectedIndex = statusFilterIdx;
        }

        int itemH = 32;
        int popH = options.length * itemH + 8;

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
        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
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
        // 1. Popover consumption
        if (activePopover != 0) {
            for (DropdownItemHitbox dh : popoverHitboxes) {
                if (mx >= dh.x && mx <= dh.x + dh.w && my >= dh.y && my <= dh.y + dh.h) {
                    if (activePopover == 1) {
                        starFilterIdx = dh.index;
                    } else if (activePopover == 2) {
                        typeFilterIdx = dh.index;
                    } else if (activePopover == 3) {
                        statusFilterIdx = dh.index;
                    }
                    activePopover = 0;
                    currentPage = 1;
                    dashboard.showToast("Filter updated: " + dh.label);
                    return true;
                }
            }
            activePopover = 0;
            return true;
        }

        // 2. Header Buttons - Add review button removed per user request

        // 3. Tabs Click
        for (int i = 0; i < tabBoxes.length; i++) {
            TabHitbox th = tabBoxes[i];
            if (mx >= th.x && mx <= th.x + th.w && my >= th.y && my <= th.y + th.h) {
                selectedTab = i;
                currentPage = 1;
                activePopover = 0;
                dashboard.showToast("Switched to " + th.label);
                return true;
            }
        }

        // 4. Search & Clear Search
        if (!searchQuery.isEmpty() && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize &&
                my >= clearSearchY && my <= clearSearchY + clearSearchSize) {
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

        // 5. Dropdown buttons toggle
        if (mx >= starDropX && mx <= starDropX + starDropW && my >= starDropY && my <= starDropY + starDropH) {
            activePopover = (activePopover == 1) ? 0 : 1;
            return true;
        }
        if (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY && my <= typeDropY + typeDropH) {
            activePopover = (activePopover == 2) ? 0 : 2;
            return true;
        }
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY
                && my <= statusDropY + statusDropH) {
            activePopover = (activePopover == 3) ? 0 : 3;
            return true;
        }

        // 6. Reset Filters Button
        boolean filtersActive = !searchQuery.isEmpty() || selectedTab != 0 || starFilterIdx != 0 || typeFilterIdx != 0
                || statusFilterIdx != 0;
        if (filtersActive && mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY
                && my <= resetBtnY + resetBtnH) {
            resetAllFilters();
            dashboard.showToast("All feedback filters reset");
            return true;
        }

        // 7. Empty State Reset Button
        List<AdminDataStore.ReviewModel> filtered = getFilteredReviews();
        if (filtered.isEmpty()) {
            if (mx >= emptyResetBtnX && mx <= emptyResetBtnX + emptyResetBtnW && my >= emptyResetBtnY
                    && my <= emptyResetBtnY + emptyResetBtnH) {
                resetAllFilters();
                dashboard.showToast("Filters reset to default view");
                return true;
            }
        }

        // 8. Review Cards Moderation Actions
        for (ReviewCardHitbox ch : cardHitboxes) {
            // Delete Inappropriate Feedback Button
            if (mx >= ch.deleteX && mx <= ch.deleteX + ch.deleteW && my >= ch.deleteY
                    && my <= ch.deleteY + ch.deleteH) {
                AdminDataStore.getInstance().deleteReview(ch.review.id);
                dashboard.showToast("Feedback #" + ch.review.id + " deleted permanently");
                return true;
            }

            // Approve & Keep Button
            if (ch.approveW > 0 && mx >= ch.approveX && mx <= ch.approveX + ch.approveW && my >= ch.approveY
                    && my <= ch.approveY + ch.approveH) {
                AdminDataStore.getInstance().approveReportedReview(ch.review.id);
                dashboard.showToast("Feedback #" + ch.review.id + " approved & published");
                return true;
            }

            // Hide / Unhide Button
            if (mx >= ch.hideX && mx <= ch.hideX + ch.hideW && my >= ch.hideY && my <= ch.hideY + ch.hideH) {
                AdminDataStore.getInstance().hideReview(ch.review.id);
                boolean isNowHidden = "Hidden".equalsIgnoreCase(ch.review.status);
                dashboard.showToast(isNowHidden ? "Feedback #" + ch.review.id + " hidden"
                        : "Feedback #" + ch.review.id + " published");
                return true;
            }

            // Warn Author Button
            if (ch.warnW > 0 && mx >= ch.warnX && mx <= ch.warnX + ch.warnW && my >= ch.warnY
                    && my <= ch.warnY + ch.warnH) {
                AdminDataStore.getInstance().warnAuthorReview(ch.review.id);
                dashboard.showToast("Warning issued to author " + ch.review.reviewerName);
                return true;
            }

            // Inspect Details Button
            if (mx >= ch.inspectX && mx <= ch.inspectX + ch.inspectW && my >= ch.inspectY
                    && my <= ch.inspectY + ch.inspectH) {
                dashboard.openReviewDetailsModal(ch.review);
                return true;
            }
        }

        // 9. Pagination
        if (prevEnabled && mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            if (currentPage > 1) {
                currentPage--;
                return true;
            }
        }
        if (nextEnabled && mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            currentPage++;
            return true;
        }
        for (PageBtnHitbox pb : pageHitboxes) {
            if (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h) {
                if (pb.pageNumber != currentPage) {
                    currentPage = pb.pageNumber;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p3 = clearSearchHovered;
        clearSearchHovered = (!searchQuery.isEmpty() && mx >= clearSearchX && mx <= clearSearchX + clearSearchSize
                && my >= clearSearchY && my <= clearSearchY + clearSearchSize);
        if (clearSearchHovered != p3)
            changed = true;

        boolean p4 = starDropHovered;
        starDropHovered = (mx >= starDropX && mx <= starDropX + starDropW && my >= starDropY
                && my <= starDropY + starDropH);
        if (starDropHovered != p4)
            changed = true;

        boolean p5 = typeDropHovered;
        typeDropHovered = (mx >= typeDropX && mx <= typeDropX + typeDropW && my >= typeDropY
                && my <= typeDropY + typeDropH);
        if (typeDropHovered != p5)
            changed = true;

        boolean p6 = statusDropHovered;
        statusDropHovered = (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY
                && my <= statusDropY + statusDropH);
        if (statusDropHovered != p6)
            changed = true;

        boolean p7 = resetBtnHovered;
        resetBtnHovered = (mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= resetBtnY
                && my <= resetBtnY + resetBtnH);
        if (resetBtnHovered != p7)
            changed = true;

        for (ReviewCardHitbox ch : cardHitboxes) {
            ch.deleteHovered = (mx >= ch.deleteX && mx <= ch.deleteX + ch.deleteW && my >= ch.deleteY
                    && my <= ch.deleteY + ch.deleteH);
            ch.approveHovered = (ch.approveW > 0 && mx >= ch.approveX && mx <= ch.approveX + ch.approveW
                    && my >= ch.approveY && my <= ch.approveY + ch.approveH);
            ch.hideHovered = (mx >= ch.hideX && mx <= ch.hideX + ch.hideW && my >= ch.hideY
                    && my <= ch.hideY + ch.hideH);
            ch.warnHovered = (ch.warnW > 0 && mx >= ch.warnX && mx <= ch.warnX + ch.warnW && my >= ch.warnY
                    && my <= ch.warnY + ch.warnH);
            ch.inspectHovered = (mx >= ch.inspectX && mx <= ch.inspectX + ch.inspectW && my >= ch.inspectY
                    && my <= ch.inspectY + ch.inspectH);
        }

        boolean pPrev = prevHovered;
        prevHovered = (prevEnabled && mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY
                && my <= prevBtnY + prevBtnH);
        if (prevHovered != pPrev)
            changed = true;

        boolean pNext = nextHovered;
        nextHovered = (nextEnabled && mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY
                && my <= nextBtnY + nextBtnH);
        if (nextHovered != pNext)
            changed = true;

        for (PageBtnHitbox pb : pageHitboxes) {
            boolean oldHov = pb.hovered;
            pb.hovered = (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h);
            if (pb.hovered != oldHov)
                changed = true;
        }

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (!searchFocused)
            return false;

        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                currentPage = 1;
                return true;
            }
            return false;
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            searchFocused = false;
            return true;
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            searchFocused = false;
            return true;
        } else if (!Character.isISOControl(ch)) {
            searchQuery += ch;
            currentPage = 1;
            return true;
        }

        return false;
    }

    public void resetAllFilters() {
        selectedTab = 0;
        searchQuery = "";
        starFilterIdx = 0;
        typeFilterIdx = 0;
        statusFilterIdx = 0;
        activePopover = 0;
        currentPage = 1;
    }

    public int getComputedHeight(int contentW) {
        List<AdminDataStore.ReviewModel> filtered = getFilteredReviews();
        if (filtered.isEmpty()) {
            return 640;
        }
        int pageItems = Math.min(pageSize, filtered.size());
        int estimatedListH = 0;
        for (int i = 0; i < pageItems; i++) {
            AdminDataStore.ReviewModel r = filtered.get(i);
            boolean isReported = "Reported".equalsIgnoreCase(r.status) || "Under review".equalsIgnoreCase(r.status);
            estimatedListH += (isReported ? 172 : 136) + 14;
        }
        return 280 + estimatedListH + 100;
    }

    // Getters for automated tests
    public int getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(int tab) {
        this.selectedTab = tab;
        this.currentPage = 1;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
        this.currentPage = 1;
    }

    public int getStarFilterIdx() {
        return starFilterIdx;
    }

    public void setStarFilterIdx(int idx) {
        this.starFilterIdx = idx;
        this.currentPage = 1;
    }

    public int getTypeFilterIdx() {
        return typeFilterIdx;
    }

    public void setTypeFilterIdx(int idx) {
        this.typeFilterIdx = idx;
        this.currentPage = 1;
    }

    public int getStatusFilterIdx() {
        return statusFilterIdx;
    }

    public void setStatusFilterIdx(int idx) {
        this.statusFilterIdx = idx;
        this.currentPage = 1;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int p) {
        this.currentPage = p;
    }

    public int getActivePopover() {
        return activePopover;
    }

    public List<ReviewCardHitbox> getCardHitboxes() {
        return cardHitboxes;
    }
}
