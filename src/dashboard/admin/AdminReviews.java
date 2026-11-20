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

    private final TabHitbox[] tabBoxes = new TabHitbox[5];
    private final List<ReviewCardHitbox> cardHitboxes = new ArrayList<>();
    private final List<DropdownItemHitbox> popoverHitboxes = new ArrayList<>();
    private final List<PageBtnHitbox> pageHitboxes = new ArrayList<>();

    private int selectedTab = 0; // 0=All, 1=Team Reviews, 2=User Ratings, 3=Reported, 4=Pending approval
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

    private final String[] typeOptions = { "All types", "Team Member Review", "User Rating", "Peer Collaboration", "Community Voice" };
    private int typeFilterIdx = 0;
    private int typeDropX, typeDropY, typeDropW = 145, typeDropH = 36;
    private boolean typeDropHovered = false;

    private final String[] statusOptions = { "All statuses", "Published", "Pending", "Reported", "Under review", "Hidden" };
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

        // 2. Metrics Summary Cards (4 KPI Cards) - Deleted as requested
        // curY = paintMetricsSummary(g, x + padX, curY, contentW);
        // curY += 22;

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
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Reviews & Feedback", x, y + 24);

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

    private static final int TAB_ROW_STEP = 42;

    private String[] tabLabels() {
        AdminDataStore ds = AdminDataStore.getInstance();
        return new String[] {
                "All Reviews (" + ds.getAllReviews().size() + ")",
                "Team Member Reviews (" + ds.getTeamReviewsCount() + ")",
                "User Ratings (" + ds.getUserRatingsCount() + ")",
                "Reported Feedback (" + ds.getReportedReviewsCount() + ")",
                "Pending approval (" + ds.getPendingReviewsCount() + ")"
        };
    }

    /** Extra height when the tab row wraps (same rule as paintTabsAndControls). */
    private int extraTabRowsHeight(int w) {
        String[] labels = tabLabels();
        Graphics2D g = scratch();
        int curX = 0, rows = 1;
        for (int i = 0; i < labels.length; i++) {
            g.setFont(i == selectedTab ? AdminTheme.FONT_BOLD_12 : AdminTheme.FONT_REG_12);
            int tabW = g.getFontMetrics().stringWidth(labels[i]) + 24;
            if (curX > 0 && curX + tabW > w) { curX = 0; rows++; }
            curX += tabW + 8;
        }
        return (rows - 1) * TAB_ROW_STEP;
    }

    private int paintTabsAndControls(Graphics2D g, int x, int y, int w) {
        AdminDataStore ds = AdminDataStore.getInstance();
        int reportedCount = ds.getReportedReviewsCount();
        int pendingCount = ds.getPendingReviewsCount();
        String[] tabLabels = tabLabels();

        // Render Tabs (wrap onto another row when the page is narrow)
        int curX = x;
        int rowY = y;
        for (int i = 0; i < tabLabels.length; i++) {
            boolean isSel = (i == selectedTab);
            g.setFont(isSel ? AdminTheme.FONT_BOLD_12 : AdminTheme.FONT_REG_12);
            FontMetrics tfm = g.getFontMetrics();
            int tabW = tfm.stringWidth(tabLabels[i]) + 24;
            int tabH = 34;
            if (curX > x && curX + tabW > x + w) {
                curX = x;
                rowY += TAB_ROW_STEP;
            }

            RoundRectangle2D tabShape = new RoundRectangle2D.Float(curX, rowY, tabW, tabH, 8, 8);
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
                } else if (i == 4 && pendingCount > 0) {
                    g.setColor(new Color(0xB4, 0x53, 0x09));
                } else {
                    g.setColor(AdminTheme.TEXT_DARK);
                }
            }

            g.drawString(tabLabels[i], curX + 12, rowY + 21);

            tabBoxes[i].x = curX;
            tabBoxes[i].y = rowY;
            tabBoxes[i].w = tabW;
            tabBoxes[i].h = tabH;
            tabBoxes[i].label = tabLabels[i];

            curX += tabW + 8;
        }

        // Controls Row
        int filterY = rowY + 44;
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

    // ================================================================ review card

    /** Hover state survives repaints (hitboxes are rebuilt every frame). */
    private String hoveredCardAction = "";
    /** True while a card is laid out only to learn its height. */
    private boolean measuring = false;
    private Graphics2D scratchG;

    private static final int CARD_PAD = 20;
    private static final int AV_SIZE = 40;
    private static final int BTN_H = 32;
    private static final int BTN_GAP = 8;
    private static final Color GREEN_SOLID = new Color(0x05, 0x96, 0x69);
    private static final Color GREEN_HOVER = new Color(0x04, 0x78, 0x57);
    private static final Color AMBER_TEXT = new Color(0xB4, 0x53, 0x09);
    private static final Color AMBER_BORDER = new Color(0xF5, 0x9E, 0x0B);

    private Graphics2D scratch() {
        if (scratchG == null) {
            scratchG = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
            scratchG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        return scratchG;
    }

    private static boolean isPending(AdminDataStore.ReviewModel r) {
        return "Pending".equalsIgnoreCase(r.status);
    }

    private static boolean isCommunity(AdminDataStore.ReviewModel r) {
        return "Community Voice".equalsIgnoreCase(r.reviewType);
    }

    private static boolean isReported(AdminDataStore.ReviewModel r) {
        return "Reported".equalsIgnoreCase(r.status) || "Under review".equalsIgnoreCase(r.status);
    }

    /** Height a card will take at this width (lays it out without drawing anything visible). */
    private int measureCardHeight(AdminDataStore.ReviewModel rev, int cw) {
        boolean was = measuring;
        measuring = true;
        int h = paintCardBody(scratch(), 0, 0, cw, rev);
        measuring = was;
        return h;
    }

    private int paintReviewCard(Graphics2D g, int cx, int cy, int cw, AdminDataStore.ReviewModel rev,
            AdminDashboard dashboard) {
        int cardH = measureCardHeight(rev, cw);
        boolean reported = isReported(rev);
        boolean hidden = "Hidden".equalsIgnoreCase(rev.status);
        boolean underReview = "Under review".equalsIgnoreCase(rev.status);
        boolean pending = isPending(rev);

        Graphics2D c = (Graphics2D) g.create();
        c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D box = new RoundRectangle2D.Float(cx, cy, cw, cardH, 14, 14);
        // soft shadow
        c.setColor(new Color(15, 23, 42, 10));
        c.fill(new RoundRectangle2D.Float(cx, cy + 2, cw, cardH, 14, 14));
        c.setColor(hidden ? new Color(0xFA, 0xFA, 0xFB) : Color.WHITE);
        c.fill(box);

        // status accent along the left edge
        if (reported || hidden || pending) {
            Color accent = hidden ? new Color(0xCB, 0xD5, 0xE1)
                    : (underReview || pending) ? AMBER_BORDER : AdminTheme.ACCENT_RED;
            Shape old = c.getClip();
            c.clip(box);
            c.setColor(accent);
            c.fillRect(cx, cy, 4, cardH);
            c.setClip(old);
        }
        c.setColor(pending || underReview ? new Color(0xFD, 0xE6, 0x8A)
                : reported ? new Color(0xFE, 0xCD, 0xD3) : AdminTheme.BORDER_COLOR);
        c.setStroke(new BasicStroke(1f));
        c.draw(box);

        paintCardBody(c, cx, cy, cw, rev);
        c.dispose();
        return cy + cardH;
    }

    /** Lays out (and, unless measuring, draws) one card's content. Returns its height. */
    private int paintCardBody(Graphics2D g, int cx, int cy, int cw, AdminDataStore.ReviewModel rev) {
        boolean reported = isReported(rev);
        boolean pending = isPending(rev);
        boolean community = isCommunity(rev);
        boolean hidden = "Hidden".equalsIgnoreCase(rev.status);
        boolean draw = !measuring;
        int pad = CARD_PAD;
        int innerX = cx + pad;
        int innerW = cw - pad * 2;
        boolean compact = innerW < 560;

        // ---- right side of the header: rating + status
        String statusLabel = statusLabel(rev.status);
        g.setFont(AdminTheme.FONT_BOLD_11);
        int pillW = g.getFontMetrics().stringWidth(statusLabel) + 32;
        int pillH = 24;
        int starsW = 5 * 15 + 26; // five stars + "4.0"
        int rightW = compact ? pillW : pillW + 16 + starsW;

        // ---- avatar
        int avX = innerX, avY = cy + pad;
        if (draw) {
            g.setColor(hidden ? AdminTheme.STATUS_GRAY_BG : AdminTheme.BRAND_TINT);
            g.fillOval(avX, avY, AV_SIZE, AV_SIZE);
            g.setFont(AdminTheme.FONT_BOLD_13);
            g.setColor(hidden ? AdminTheme.STATUS_GRAY_TEXT : AdminTheme.BRAND_PRIMARY);
            String inits = safe(rev.reviewerInitials, "U");
            FontMetrics ifm = g.getFontMetrics();
            g.drawString(inits, avX + (AV_SIZE - ifm.stringWidth(inits)) / 2,
                    avY + (AV_SIZE + ifm.getAscent() - ifm.getDescent()) / 2);
        }

        // ---- names: Reviewer -> Reviewee, then type · date
        int textX = avX + AV_SIZE + 14;
        int nameMaxW = Math.max(80, innerX + innerW - textX - rightW - 16);
        String reviewer = safe(rev.reviewerName, "Unknown");
        String reviewee = safe(rev.revieweeName, "-");
        g.setFont(AdminTheme.FONT_BOLD_14);
        FontMetrics nfm = g.getFontMetrics();
        int arrowW = 28;
        int half = Math.max(40, (nameMaxW - arrowW) / 2);
        String rName = fit(nfm, reviewer, Math.max(half, nameMaxW - arrowW - nfm.stringWidth(reviewee)));
        int rW = nfm.stringWidth(rName);
        String eName = fit(nfm, reviewee, Math.max(40, nameMaxW - arrowW - rW));
        int nameY = avY + 16;
        if (draw) {
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(rName, textX, nameY);
            drawArrow(g, textX + rW + 8, nameY - 5, AdminTheme.TEXT_LIGHT);
            g.setFont(AdminTheme.FONT_BOLD_14);
            g.setColor(hidden ? AdminTheme.TEXT_MUTED : AdminTheme.BRAND_PRIMARY);
            g.drawString(eName, textX + rW + arrowW, nameY);
        }
        g.setFont(AdminTheme.FONT_REG_12);
        String sub = safe(rev.reviewType, "Review") + "  ·  " + safe(rev.date, "");
        if (draw) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(fit(g.getFontMetrics(), sub, nameMaxW), textX, nameY + 19);
        }

        // status pill (top-right) and stars
        int pillX = innerX + innerW - pillW;
        int pillY = avY + 2;
        if (draw) {
            paintStatusPill(g, pillX, pillY, pillW, pillH, rev.status, statusLabel);
            if (!compact) paintStarRating(g, pillX - 16 - starsW, pillY + 4, rev.rating);
        }

        int y = avY + AV_SIZE;
        if (compact) { // stars get their own line under the names on narrow cards
            y += 8;
            if (draw) paintStarRating(g, textX, y, rev.rating);
            y += 16;
        }

        // ---- review text (quote bar + up to 4 wrapped lines)
        y += 16;
        String text = safe(rev.reviewText, "");
        g.setFont(AdminTheme.FONT_REG_13);
        FontMetrics tfm = g.getFontMetrics();
        int bodyX = innerX + 14;
        List<String> lines = wrap(tfm, text.isEmpty() ? "No written feedback." : text, innerX + innerW - bodyX, 4);
        int lineH = 21;
        int textTop = y;
        if (draw) {
            g.setColor(hidden ? AdminTheme.BORDER_COLOR : new Color(0xF9, 0xC6, 0xDA));
            g.fillRoundRect(innerX, textTop, 3, lines.size() * lineH - 4, 3, 3);
            g.setColor(text.isEmpty() || hidden ? AdminTheme.TEXT_MUTED : new Color(0x33, 0x41, 0x55));
            for (int i = 0; i < lines.size(); i++)
                g.drawString(lines.get(i), bodyX, textTop + 15 + i * lineH);
        }
        y = textTop + lines.size() * lineH - 4;

        // ---- context tags: project + role
        boolean hasProject = rev.projectName != null && !rev.projectName.trim().isEmpty();
        boolean hasRole = rev.roleOrContext != null && !rev.roleOrContext.trim().isEmpty();
        if (hasProject || hasRole) {
            y += 14;
            int tx = innerX;
            if (hasProject) {
                g.setFont(AdminTheme.FONT_BOLD_11);
                FontMetrics pfm = g.getFontMetrics();
                String p = fit(pfm, rev.projectName.trim(), Math.min(320, innerW - 34));
                int pw = pfm.stringWidth(p) + 34;
                if (draw) {
                    g.setColor(new Color(0xFD, 0xF2, 0xF8));
                    g.fillRoundRect(tx, y, pw, 24, 12, 12);
                    drawFolder(g, tx + 10, y + 7, AdminTheme.BRAND_PRIMARY);
                    g.setFont(AdminTheme.FONT_BOLD_11);
                    g.setColor(AdminTheme.BRAND_PRIMARY);
                    g.drawString(p, tx + 26, y + 16);
                }
                tx += pw + 8;
            }
            if (hasRole) {
                g.setFont(AdminTheme.FONT_REG_11);
                FontMetrics rfm = g.getFontMetrics();
                int room = innerX + innerW - tx;
                if (room < 90) { tx = innerX; y += 30; room = innerW; }
                String roleText = community ? "Who it's for \u00B7 " + rev.roleOrContext.trim() : rev.roleOrContext.trim();
                String r = fit(rfm, roleText, room - 20);
                int rw = rfm.stringWidth(r) + 20;
                if (draw) {
                    g.setColor(AdminTheme.STATUS_GRAY_BG);
                    g.fillRoundRect(tx, y, rw, 24, 12, 12);
                    g.setColor(AdminTheme.STATUS_GRAY_TEXT);
                    g.drawString(r, tx + 10, y + 16);
                }
            }
            y += 24;
        }

        // ---- waiting for approval (Community Voice from the landing page)
        if (pending) {
            y += 14;
            String tab = safe(rev.roleOrContext, "Students");
            String msg = "Once approved it appears on the landing page under \u201C" + tab
                    + "\u201D in Who it's for. Reject keeps it hidden.";
            g.setFont(AdminTheme.FONT_REG_12);
            FontMetrics pfm = g.getFontMetrics();
            int pTextX = innerX + 36;
            int pTextW = innerX + innerW - 14 - pTextX;
            List<String> pl = wrap(pfm, msg, pTextW, 3);
            int pH = 34 + pl.size() * 18 + 4;
            if (draw) {
                g.setColor(new Color(0xFF, 0xFB, 0xEB));
                g.fillRoundRect(innerX, y, innerW, pH, 10, 10);
                g.setColor(new Color(0xFD, 0xE6, 0x8A));
                g.drawRoundRect(innerX, y, innerW, pH, 10, 10);
                drawClock(g, innerX + 14, y + 13, AMBER_TEXT);
                g.setFont(AdminTheme.FONT_BOLD_12);
                g.setColor(AMBER_TEXT);
                g.drawString("Waiting for your approval", pTextX, y + 26);
                g.setFont(AdminTheme.FONT_REG_12);
                g.setColor(new Color(0x92, 0x40, 0x0E));
                for (int i = 0; i < pl.size(); i++) g.drawString(pl.get(i), pTextX, y + 45 + i * 18);
            }
            y += pH;
        }

        // ---- moderation alert
        if (reported) {
            y += 14;
            boolean underReview = "Under review".equalsIgnoreCase(rev.status);
            Color bg = underReview ? new Color(0xFF, 0xFB, 0xEB) : new Color(0xFF, 0xF1, 0xF2);
            Color border = underReview ? new Color(0xFD, 0xE6, 0x8A) : new Color(0xFE, 0xCD, 0xD3);
            Color strong = underReview ? AMBER_TEXT : AdminTheme.ACCENT_RED;
            Color soft = underReview ? new Color(0x92, 0x40, 0x0E) : new Color(0x9F, 0x12, 0x39);
            String reason = safe(rev.reportReason, "Reported as inappropriate content");
            g.setFont(AdminTheme.FONT_REG_12);
            FontMetrics afm = g.getFontMetrics();
            int aTextX = innerX + 36;
            int aTextW = innerX + innerW - 14 - aTextX;
            List<String> rl = wrap(afm, "“" + reason + "”", aTextW, 2);
            String by = rev.reportedBy != null && !rev.reportedBy.trim().isEmpty()
                    ? "Reported by " + rev.reportedBy.trim() : "";
            int aH = 34 + rl.size() * 18 + (by.isEmpty() ? 0 : 18) + 4;
            if (draw) {
                g.setColor(bg);
                g.fillRoundRect(innerX, y, innerW, aH, 10, 10);
                g.setColor(border);
                g.drawRoundRect(innerX, y, innerW, aH, 10, 10);
                drawFlag(g, innerX + 14, y + 14, strong);
                g.setFont(AdminTheme.FONT_BOLD_12);
                g.setColor(strong);
                g.drawString(underReview ? "Under moderation review" : "Flagged for moderation", aTextX, y + 26);
                g.setFont(AdminTheme.FONT_REG_12);
                g.setColor(soft);
                for (int i = 0; i < rl.size(); i++) g.drawString(rl.get(i), aTextX, y + 45 + i * 18);
                if (!by.isEmpty()) {
                    g.setFont(AdminTheme.FONT_REG_11);
                    g.setColor(AdminTheme.TEXT_MUTED);
                    g.drawString(fit(g.getFontMetrics(), by, aTextW), aTextX, y + 45 + rl.size() * 18);
                }
            }
            y += aH;
        }

        // ---- divider + footer (meta left, actions right)
        y += 16;
        if (draw) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.drawLine(innerX, y, innerX + innerW, y);
        }
        y += 14;

        String[] ids = reported ? new String[] { "inspect", "hide", "warn", "delete", "approve" }
                : pending ? new String[] { "inspect", "hide", "delete", "approve" }
                : new String[] { "inspect", "hide", "delete" };
        String[] labels = new String[ids.length];
        int[] widths = new int[ids.length];
        g.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics bfm = g.getFontMetrics();
        int btnTotal = 0;
        for (int i = 0; i < ids.length; i++) {
            labels[i] = buttonLabel(ids[i], hidden, pending);
            int icon = "delete".equals(ids[i]) || "approve".equals(ids[i]) ? 18 : 0;
            widths[i] = bfm.stringWidth(labels[i]) + 28 + icon;
            btnTotal += widths[i] + (i > 0 ? BTN_GAP : 0);
        }

        // meta: verified collaboration · N found this helpful
        g.setFont(AdminTheme.FONT_REG_12);
        FontMetrics m2 = g.getFontMetrics();
        String verified = "Verified collaboration";
        String helpful = rev.helpfulVotes > 0
                ? rev.helpfulVotes + (rev.helpfulVotes == 1 ? " person" : " people") + " found this helpful" : "";
        int metaW = (rev.verifiedCollaboration ? 18 + m2.stringWidth(verified) : 0)
                + (rev.verifiedCollaboration && !helpful.isEmpty() ? 24 : 0)
                + (helpful.isEmpty() ? 0 : m2.stringWidth(helpful));
        String fallbackMeta = community ? "Submitted from the landing page  \u00B7  " + rev.id : "Review ID " + rev.id;
        if (metaW == 0) metaW = m2.stringWidth(fallbackMeta);

        boolean stacked = metaW + 24 + btnTotal > innerW;
        int metaY = stacked ? y + 12 : y + BTN_H / 2 + 4;
        int btnY = stacked ? y + 26 : y;
        if (draw) {
            g.setFont(AdminTheme.FONT_REG_12);
            int mx = innerX;
            if (rev.verifiedCollaboration) {
                drawCheck(g, mx, metaY - 10, GREEN_SOLID);
                g.setFont(AdminTheme.FONT_REG_12);
                g.setColor(GREEN_SOLID);
                g.drawString(verified, mx + 18, metaY);
                mx += 18 + m2.stringWidth(verified);
                if (!helpful.isEmpty()) {
                    g.setColor(new Color(0xCB, 0xD5, 0xE1));
                    g.fillOval(mx + 10, metaY - 6, 4, 4);
                    mx += 24;
                }
            }
            if (!helpful.isEmpty()) {
                g.setColor(AdminTheme.TEXT_MUTED);
                g.drawString(helpful, mx, metaY);
            } else if (!rev.verifiedCollaboration) {
                g.setColor(AdminTheme.TEXT_LIGHT);
                g.drawString(fallbackMeta, mx, metaY);
            }
        }

        // buttons: right-aligned on one row; when stacked they start at the left and wrap as needed
        ReviewCardHitbox hb = new ReviewCardHitbox();
        hb.review = rev;
        int rowStart = 0;
        int rowY = btnY;
        while (rowStart < ids.length) {
            int used = 0, end = rowStart;
            while (end < ids.length && used + widths[end] + (end > rowStart ? BTN_GAP : 0) <= innerW) {
                used += widths[end] + (end > rowStart ? BTN_GAP : 0);
                end++;
            }
            if (end == rowStart) { used = widths[end]; end++; } // single over-wide button
            int bx = stacked ? innerX : innerX + innerW - used;
            for (int i = rowStart; i < end; i++) {
                paintActionButton(g, bx, rowY, widths[i], ids[i], labels[i], rev, hb, draw);
                bx += widths[i] + BTN_GAP;
            }
            rowStart = end;
            if (rowStart < ids.length) rowY += BTN_H + BTN_GAP;
        }
        y = rowY + BTN_H + pad - 2;

        int cardH = y - cy;
        if (draw) {
            hb.cardX = cx;
            hb.cardY = cy;
            hb.cardW = cw;
            hb.cardH = cardH;
            cardHitboxes.add(hb);
        }
        return cardH;
    }

    private static String buttonLabel(String id, boolean hidden, boolean pending) {
        switch (id) {
            case "inspect": return "View details";
            case "hide": return pending ? "Reject" : hidden ? "Unhide" : "Hide";
            case "warn": return "Warn author";
            case "delete": return "Delete";
            default: return pending ? "Approve & publish" : "Approve & keep";
        }
    }

    private void paintActionButton(Graphics2D g, int bx, int by, int bw, String id, String label,
            AdminDataStore.ReviewModel rev, ReviewCardHitbox hb, boolean draw) {
        switch (id) {
            case "inspect": hb.inspectX = bx; hb.inspectY = by; hb.inspectW = bw; hb.inspectH = BTN_H; break;
            case "hide": hb.hideX = bx; hb.hideY = by; hb.hideW = bw; hb.hideH = BTN_H; break;
            case "warn": hb.warnX = bx; hb.warnY = by; hb.warnW = bw; hb.warnH = BTN_H; break;
            case "delete": hb.deleteX = bx; hb.deleteY = by; hb.deleteW = bw; hb.deleteH = BTN_H; break;
            default: hb.approveX = bx; hb.approveY = by; hb.approveW = bw; hb.approveH = BTN_H; break;
        }
        if (!draw) return;
        boolean hov = (rev.id + ":" + id).equals(hoveredCardAction);
        Color fill, border, text;
        switch (id) {
            case "approve":
                fill = hov ? GREEN_HOVER : GREEN_SOLID; border = fill; text = Color.WHITE; break;
            case "delete":
                fill = hov ? new Color(0xFF, 0xE4, 0xE6) : Color.WHITE; border = new Color(0xFE, 0xCD, 0xD3);
                text = AdminTheme.ACCENT_RED; break;
            case "warn":
                fill = hov ? new Color(0xFE, 0xF3, 0xC7) : Color.WHITE; border = new Color(0xFD, 0xE6, 0x8A);
                text = AMBER_TEXT; break;
            case "inspect":
                fill = hov ? AdminTheme.BRAND_TINT : Color.WHITE; border = new Color(0xF9, 0xC6, 0xDA);
                text = AdminTheme.BRAND_PRIMARY; break;
            default:
                fill = hov ? AdminTheme.STATUS_GRAY_BG : Color.WHITE; border = AdminTheme.BORDER_COLOR;
                text = AdminTheme.TEXT_MUTED; break;
        }
        g.setColor(fill);
        g.fillRoundRect(bx, by, bw, BTN_H, 9, 9);
        g.setColor(border);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, by, bw, BTN_H, 9, 9);
        int tx = bx + 14;
        if ("delete".equals(id)) { drawTrash(g, tx, by + 9, text); tx += 18; }
        if ("approve".equals(id)) { drawCheck(g, tx, by + 10, text); tx += 18; }
        g.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(text);
        g.drawString(label, tx, by + (BTN_H + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static String statusLabel(String status) {
        if ("Published".equalsIgnoreCase(status)) return "Published";
        if ("Reported".equalsIgnoreCase(status)) return "Reported";
        if ("Under review".equalsIgnoreCase(status)) return "Under review";
        if ("Pending".equalsIgnoreCase(status)) return "Pending approval";
        if ("Hidden".equalsIgnoreCase(status)) return "Hidden";
        return status == null || status.isEmpty() ? "Unknown" : status;
    }

    private void paintStatusPill(Graphics2D g, int x, int y, int w, int h, String status, String label) {
        Color bg, fg;
        if ("Published".equalsIgnoreCase(status)) { bg = AdminTheme.STATUS_GREEN_BG; fg = AdminTheme.STATUS_GREEN_TEXT; }
        else if ("Reported".equalsIgnoreCase(status)) { bg = AdminTheme.STATUS_RED_BG; fg = AdminTheme.STATUS_RED_TEXT; }
        else if ("Under review".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
            bg = AdminTheme.STATUS_PENDING_BG; fg = AdminTheme.STATUS_PENDING_TXT;
        }
        else { bg = AdminTheme.STATUS_GRAY_BG; fg = AdminTheme.STATUS_GRAY_TEXT; }
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(fg);
        g.fillOval(x + 11, y + h / 2 - 3, 6, 6);
        g.setFont(AdminTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + 22, y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Five drawn stars plus the numeric score (e.g. "4.0"). */
    private void paintStarRating(Graphics2D g, int x, int y, int rating) {
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < 5; i++) {
            g.setColor(i < rating ? AdminTheme.BRAND_AMBER : new Color(0xE2, 0xE8, 0xF0));
            g.fill(starShape(x + i * 15 + 6.5, y + 7.5, 7, 3));
        }
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(Math.max(0, Math.min(5, rating)) + ".0", x + 5 * 15 + 4, y + 12);
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    private static Shape starShape(double cx, double cy, double outer, double inner) {
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? outer : inner;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + Math.cos(a) * r, py = cy + Math.sin(a) * r;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    // ---- tiny vector icons (emoji glyphs render inconsistently in Java2D)

    private static void drawArrow(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x, y, x + 11, y);
        g.drawLine(x + 7, y - 4, x + 11, y);
        g.drawLine(x + 7, y + 4, x + 11, y);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawCheck(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 1, y + 6, x + 5, y + 10);
        g.drawLine(x + 5, y + 10, x + 12, y + 2);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawTrash(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x, y + 3, x + 12, y + 3);
        g.drawLine(x + 4, y + 1, x + 8, y + 1);
        g.drawRoundRect(x + 2, y + 3, 8, 11, 2, 2);
        g.drawLine(x + 5, y + 6, x + 5, y + 11);
        g.drawLine(x + 7, y + 6, x + 7, y + 11);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawClock(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(x, y, 14, 14);
        g.drawLine(x + 7, y + 3, x + 7, y + 7);
        g.drawLine(x + 7, y + 7, x + 10, y + 9);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawFlag(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 1, y, x + 1, y + 15);
        g.fillPolygon(new int[] { x + 2, x + 13, x + 10, x + 13, x + 2 }, new int[] { y, y, y + 4, y + 8, y + 8 }, 5);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawFolder(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x, y + 2, 11, 8, 2, 2);
        g.drawLine(x, y + 2, x + 4, y + 2);
        g.drawLine(x + 4, y + 2, x + 5, y);
        g.drawLine(x + 5, y, x + 1, y);
        g.setStroke(new BasicStroke(1f));
    }

    private static String safe(String s, String def) {
        return s == null || s.trim().isEmpty() ? def : s.trim();
    }

    private static String fit(FontMetrics fm, String s, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    /** Word-wraps to at most maxLines; the last line ends with an ellipsis if text was cut. */
    private static List<String> wrap(FontMetrics fm, String text, int maxW, int maxLines) {
        List<String> out = new ArrayList<>();
        String[] words = text.replace('\n', ' ').trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        boolean cut = false;
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            String cand = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(cand) <= maxW) {
                line.setLength(0);
                line.append(cand);
                continue;
            }
            if (line.length() == 0) { // one very long word
                out.add(fit(fm, w, maxW));
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(w);
            }
            if (out.size() == maxLines) {
                cut = line.length() > 0 || i < words.length - 1;
                line.setLength(0);
                break;
            }
            if (line.length() == 0) continue;
        }
        if (line.length() > 0) out.add(line.toString());
        if (out.isEmpty()) out.add("");
        if (cut) {
            int last = out.size() - 1;
            String l = out.get(last);
            while (l.length() > 1 && fm.stringWidth(l + "…") > maxW) l = l.substring(0, l.length() - 1);
            out.set(last, l.replaceAll("[\\s.,;:!?]+$", "") + "…");
        }
        return out;
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
                boolean wasPending = isPending(ch.review);
                AdminDataStore.getInstance().approveReportedReview(ch.review.id);
                dashboard.showToast(wasPending ? "Review #" + ch.review.id + " approved \u2014 now live on the landing page"
                        : "Feedback #" + ch.review.id + " approved & published");
                return true;
            }

            // Hide / Unhide Button
            if (mx >= ch.hideX && mx <= ch.hideX + ch.hideW && my >= ch.hideY && my <= ch.hideY + ch.hideH) {
                boolean wasPending = isPending(ch.review);
                AdminDataStore.getInstance().hideReview(ch.review.id);
                boolean isNowHidden = "Hidden".equalsIgnoreCase(ch.review.status);
                dashboard.showToast(wasPending ? "Review #" + ch.review.id + " rejected \u2014 it won't be shown"
                        : isNowHidden ? "Feedback #" + ch.review.id + " hidden"
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

        String prevCardHover = hoveredCardAction;
        hoveredCardAction = "";
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
            String id = ch.review.id + ":";
            if (ch.deleteHovered) hoveredCardAction = id + "delete";
            else if (ch.approveHovered) hoveredCardAction = id + "approve";
            else if (ch.hideHovered) hoveredCardAction = id + "hide";
            else if (ch.warnHovered) hoveredCardAction = id + "warn";
            else if (ch.inspectHovered) hoveredCardAction = id + "inspect";
        }
        if (!hoveredCardAction.equals(prevCardHover))
            changed = true;

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
        int cardW = contentW - 48; // paint() uses 24px side padding
        int tabsExtra = extraTabRowsHeight(cardW);
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        int page = Math.min(currentPage, totalPages);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());
        int listH = 0;
        for (int i = start; i < end; i++) {
            listH += measureCardHeight(filtered.get(i), cardW) + 14;
        }
        return 170 + tabsExtra + listH + 100;
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
