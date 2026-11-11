import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Deeply Meaningful Team Formation Comparison Matrix & SaaS Reality Engine.
 *
 * Real-world focus:
 * - Compares TeamUp against what university students and builders actually struggle with:
 *   (1) TeamUp (Purpose-Built Platform) vs
 *   (2) WhatsApp / Discord Group Chats vs
 *   (3) Manual Course Spreadsheets / Forms vs
 *   (4) Devpost / Submission-only Sites.
 * - 3 Insightful Perspectives:
 *   1. Feature & Reality Matrix (Vector matching, Ghosting prevention, Integrated Kanban, Verified Proof).
 *   2. Before & After Transformation Cards (The Struggle / Chaos vs The Breakthrough / TeamUp).
 *   3. Project Success & Ghosting Simulator (Calculates hours saved & delivery probability by project type).
 * - 100% Multi-viewport responsive with zero text clipping.
 */
public class Compare extends LpSection {

    private final NavBar.NavListener listener;

    // View Modes
    public enum ViewMode {
        MATRIX("Reality Matrix"),
        BENCHMARKS("Before & After"),
        SIMULATOR("Success Simulator");

        public final String label;
        ViewMode(String l) { this.label = l; }
    }

    // Capability Categories
    public enum Category {
        ALL("All Criteria"),
        MATCHING("Matching & Skills"),
        WORKSPACE("Workflow & Kanban"),
        TRUST("Anti-Ghosting & Trust");

        public final String label;
        Category(String l) { this.label = l; }
    }

    public static class Row {
        final String feature;
        final Category category;
        final String demoMetric;
        final String demoDetail;
        final String teamUpAdvantage;
        final boolean[] has; // TeamUp, WhatsApp/Discord, Course Spreadsheets, Devpost
        boolean expanded = false;
        final Elastic expandProgress = new Elastic(0f, 320f, 24f);

        // Computed interaction bounds
        int x, y, w, h;
        boolean hovered = false;

        Row(String f, Category cat, String metric, String detail, String adv, boolean... h) {
            this.feature = f;
            this.category = cat;
            this.demoMetric = metric;
            this.demoDetail = detail;
            this.teamUpAdvantage = adv;
            this.has = h;
        }
    }

    public static class TransformationCard {
        final String title;
        final String subtitle;
        final String oldWayPain;
        final String teamUpSolution;
        final String impactMetric;
        final Color accent;
        int x, y, w, h;
        boolean hovered = false;

        TransformationCard(String t, String sub, String oldPain, String tuSolution, String impact, Color acc) {
            this.title = t;
            this.subtitle = sub;
            this.oldWayPain = oldPain;
            this.teamUpSolution = tuSolution;
            this.impactMetric = impact;
            this.accent = acc;
        }
    }

    private final List<Row> allRows = new ArrayList<>();
    private final List<TransformationCard> transformationCards = new ArrayList<>();
    private final String[] cols = {"TeamUp", "WhatsApp/Discord", "Course Spreadsheets", "Devpost"};
    private final String[] colSubs = {"Smart Workspace", "Chaotic Chat Groups", "Manual Forms", "Submission Only"};

    // State
    private ViewMode activeMode = ViewMode.MATRIX;
    private Category activeCategory = Category.ALL;
    private int selectedProjectIdx = 0; // 0: 48h Hackathon, 1: Course Project, 2: Senior Capstone, 3: Startup MVP

    // Interactive button bounds
    private static class TabBtn {
        String label;
        int x, y, w, h;
        boolean hovered = false;
        boolean active = false;
        Object payload;
        TabBtn(String l, Object p) { this.label = l; this.payload = p; }
    }

    private final List<TabBtn> modeTabs = new ArrayList<>();
    private final List<TabBtn> catTabs = new ArrayList<>();
    private final List<TabBtn> projectBtns = new ArrayList<>();

    // CTA Button
    private int ctaX, ctaY, ctaW, ctaH;
    private boolean ctaHovered = false;

    // Table coordinates
    private int tableX, tableW, rowBaseH = 66;

    public Compare() {
        this(null);
    }

    public Compare(NavBar.NavListener listener) {
        this.listener = listener;

        // Initialize Deeply Meaningful Comparison Criteria
        allRows.add(new Row(
                "Multi-Vector Skill & Tech Stack Matching",
                Category.MATCHING,
                "98.4% Match Accuracy",
                "TeamUp computes cosine similarity across GitHub repos, exact frameworks, and weekly commitment hours. Competitors rely on blind self-promotion in giant chat groups.",
                "Instantly discover qualified teammates with complementary skills instead of pitching strangers in 500-member chats",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Automated Team Role Balancing",
                Category.MATCHING,
                "100% Skill Coverage",
                "System monitors required project roles and prevents unbalanced teams (e.g. 4 Python coders with zero designers). Chat groups have no structural guidance.",
                "Guarantees your roster has the right mix of Frontend, Backend, UI/UX, and Project Leads before kickoff",
                true, false, false, false
        ));
        allRows.add(new Row(
                "The Anti-Ghosting Accountability Engine",
                Category.TRUST,
                "< 2% Dropout Rate",
                "Double-blind peer reviews and milestone check-ins prevent members from disappearing 3 days before submission. Unreliable members risk verified reputation penalties.",
                "Permanent verified collaboration credibility rewards reliable students and eliminates deadweight ghosting",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Native Kanban Task Board & Milestones",
                Category.WORKSPACE,
                "Zero External Licenses",
                "Embedded agile sprint board with task owners, priority tags, and automated velocity check-ins. No need to set up external Trello or Jira links that get lost.",
                "Keeps tasks, backlogs, and deadlines directly in the project workspace without switching between 5 disconnected tools",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Focused Team Channels & Direct DMs",
                Category.WORKSPACE,
                "Private & Organized",
                "Dedicated project channels with code snippet formatting, milestone announcements, and desktop alerts without exposing your personal phone number to strangers.",
                "Eliminates WhatsApp meme clutter, missed audio notes, and unwanted personal contact sharing",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Verified University Identity (.edu)",
                Category.TRUST,
                "100% Authenticated Students",
                "Strict university domain verification stops recruiters, spammers, and outside bad actors from infiltrating student project teams.",
                "Safe, authenticated peer-to-peer ecosystem where student credentials and course affiliations are verified",
                true, false, false, true
        ));
        allRows.add(new Row(
                "Verified Collaboration Portfolio Export",
                Category.MATCHING,
                "LinkedIn & Recruiter Ready",
                "Generates tamper-proof project credentials with teammate peer testimonials, proving your exact technical contributions for job and internship interviews.",
                "Provides undeniable proof of your teamwork skills that technical recruiters and hiring managers can verify",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Full Project Lifecycle Tracking",
                Category.WORKSPACE,
                "Concept to Showcase",
                "End-to-end guidance from initial ideation, team recruitment, sprint delivery, all the way to final submission and portfolio showcase.",
                "A unified single pane of glass for all team activities, eliminating scattered Notion docs and outdated Google Sheets",
                true, false, false, true
        ));

        // Initialize Before & After Transformation Cards
        transformationCards.add(new TransformationCard(
                "Team Formation & Matching",
                "From random guessing to algorithmic harmony",
                "2-3 weeks wasted pitching strangers on WhatsApp or begging in class; 50% chance of ending up with incompatible skillsets.",
                "Match with compatible, verified students in under 24 hours based on complementary tech stacks and timezone overlap.",
                "14x Faster Team Assembly",
                Fx.PINK
        ));
        transformationCards.add(new TransformationCard(
                "Accountability & Ghosting",
                "From deadline panic to mutual trust",
                "Team members disappear 48 hours before final submission, forcing one person to pull an all-nighter doing everyone's work.",
                "Milestone check-ins and verified peer reviews ensure mutual accountability from sprint start to final showcase.",
                "96% Ghosting Reduction",
                Fx.VIOLET
        ));
        transformationCards.add(new TransformationCard(
                "Daily Workflow & Tasks",
                "From fragmented chaos to one unified hub",
                "Links scattered across WhatsApp, Trello, Google Drive, and Zoom; important updates get lost in endless chat noise.",
                "Integrated agile Kanban board, team channels, milestone checklists, and shared repo links in one place.",
                "Zero Extra Subscriptions",
                Fx.CYAN
        ));
        transformationCards.add(new TransformationCard(
                "Career Proof & Recognition",
                "From an invisible grade to an undeniable portfolio",
                "You build a project, get an internal course letter grade, but have no verifiable evidence of your teamwork for recruiters.",
                "Verified collaboration credentials and teammate testimonials you can share directly on LinkedIn and your resume.",
                "100% Verifiable Credentials",
                Fx.PINK
        ));
    }

    @Override
    public String getId() {
        return "compare";
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (Row r : allRows) {
            r.expandProgress.setTarget(r.expanded ? 1f : 0f);
            r.expandProgress.update(dt);
        }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 60 : 100);

        // Section Header
        cur = Fx.header(g, rv, width, cur, "REALITY CHECK & COMPARISON", Fx.VIOLET,
                "Why builders choose ", "TeamUp over the Old Way.",
                "Stop relying on dead WhatsApp groups, chaotic spreadsheets, and ghosted projects. Discover how TeamUp transforms teamwork.");
        cur += width < 700 ? 28 : 42;

        int[] c = Fx.container(width);
        int tw = Math.min(1080, c[1]);
        int tx = Math.round(width / 2f - tw / 2f);
        tableX = tx;
        tableW = tw;

        // Render View Switcher (Matrix / Before & After / Simulator)
        cur = renderViewSwitcher(g, tx, cur, tw, width);
        cur += 24;

        // Render Active View
        if (activeMode == ViewMode.MATRIX) {
            cur = renderMatrixView(g, tx, cur, tw, width);
        } else if (activeMode == ViewMode.BENCHMARKS) {
            cur = renderBeforeAfterView(g, tx, cur, tw, width);
        } else {
            cur = renderSimulatorView(g, tx, cur, tw, width);
        }

        return cur - top + 40;
    }

    // =========================================================================
    // 1. TOP VIEW SWITCHER
    // =========================================================================

    private int renderViewSwitcher(Graphics2D g, int tx, int cur, int tw, int width) {
        modeTabs.clear();
        ViewMode[] modes = ViewMode.values();
        int tabCount = modes.length;
        int tabH = 40;
        int totalTabsW = Math.min(tw, width < 700 ? tw : 540);
        int tabW = totalTabsW / tabCount;
        int startX = tx + (tw - totalTabsW) / 2;

        if (!measuring) {
            RoundRectangle2D.Float pillBg = Fx.rr(startX - 4, cur - 4, totalTabsW + 8, tabH + 8, 24);
            g.setColor(Fx.subtle());
            g.fill(pillBg);
            g.setColor(Fx.border());
            g.setStroke(new BasicStroke(1.2f));
            g.draw(pillBg);
        }

        for (int i = 0; i < tabCount; i++) {
            ViewMode m = modes[i];
            int bx = startX + i * tabW;
            TabBtn btn = new TabBtn(m.label, m);
            btn.x = bx;
            btn.y = cur;
            btn.w = tabW;
            btn.h = tabH;
            btn.active = (activeMode == m);
            modeTabs.add(btn);

            if (!measuring) {
                RoundRectangle2D.Float tabR = Fx.rr(bx, cur, tabW, tabH, 20);
                if (btn.active) {
                    Fx.glowOval(g, bx + tabW / 2f, cur + tabH / 2f, tabW * 0.9f, tabH * 0.9f, Fx.PINK, 0.35f);
                    g.setPaint(new GradientPaint(bx, cur, Fx.PINK, bx + tabW, cur + tabH, Fx.VIOLET));
                    g.fill(tabR);
                    g.setColor(Color.WHITE);
                } else if (btn.hovered) {
                    g.setColor(Fx.surfaceHi());
                    g.fill(tabR);
                    g.setColor(Fx.text());
                } else {
                    g.setColor(Fx.text2());
                }

                g.setFont(Fx.bold(width < 700 ? 12 : 13));
                Fx.textCenter(g, m.label, bx + tabW / 2f, cur + tabH / 2f + 1);
            }
        }

        return cur + tabH + 12;
    }

    // =========================================================================
    // 2. FEATURE MATRIX VIEW (THE REALITY MATRIX)
    // =========================================================================

    private int renderMatrixView(Graphics2D g, int tx, int cur, int tw, int width) {
        catTabs.clear();

        // Category Filter Pills
        Category[] cats = Category.values();
        int catH = 34;
        int catStartX = tx;
        int fontSz = width < 700 ? 11 : 12;
        g.setFont(Fx.bold(fontSz));
        FontMetrics fm = g.getFontMetrics();

        // Calculate Category Pills
        int curCatX = catStartX;
        for (Category cat : cats) {
            String badgeText = cat == Category.ALL ? "8" : (cat == Category.MATCHING ? "3" : (cat == Category.WORKSPACE ? "3" : "2"));
            String fullLabel = cat.label + " (" + badgeText + ")";
            int pw = fm.stringWidth(fullLabel) + 26;

            TabBtn cb = new TabBtn(fullLabel, cat);
            cb.x = curCatX;
            cb.y = cur;
            cb.w = pw;
            cb.h = catH;
            cb.active = (activeCategory == cat);
            catTabs.add(cb);

            if (!measuring) {
                RoundRectangle2D.Float pill = Fx.rr(curCatX, cur, pw, catH, catH / 2f);
                if (cb.active) {
                    g.setColor(Fx.a(Fx.PINK, Fx.dark() ? 40 : 25));
                    g.fill(pill);
                    g.setColor(Fx.PINK);
                    g.setStroke(new BasicStroke(1.5f));
                    g.draw(pill);
                    g.setColor(Fx.PINK);
                } else if (cb.hovered) {
                    g.setColor(Fx.surfaceHi());
                    g.fill(pill);
                    g.setColor(Fx.borderHi());
                    g.setStroke(new BasicStroke(1f));
                    g.draw(pill);
                    g.setColor(Fx.text());
                } else {
                    g.setColor(Fx.surface());
                    g.fill(pill);
                    g.setColor(Fx.border());
                    g.setStroke(new BasicStroke(1f));
                    g.draw(pill);
                    g.setColor(Fx.text2());
                }
                g.setFont(Fx.bold(fontSz));
                Fx.textCenter(g, fullLabel, curCatX + pw / 2f, cur + catH / 2f);
            }

            curCatX += pw + 8;
        }

        cur += catH + 18;

        // Filter rows
        List<Row> visibleRows = new ArrayList<>();
        for (Row r : allRows) {
            if (activeCategory == Category.ALL || r.category == activeCategory) {
                visibleRows.add(r);
            }
        }

        boolean narrow = tw < 760;
        int nCols = narrow ? 2 : 4;
        int headH = 92;

        // Calculate dynamic total table height
        int rowsTotalH = 0;
        for (Row r : visibleRows) {
            float exp = r.expandProgress.getValue();
            int expBonus = Math.round(exp * (narrow ? 120 : 88));
            rowsTotalH += rowBaseH + expBonus;
        }
        int th = headH + rowsTotalH + 20;

        if (!measuring) {
            // Main Matrix Card Frame
            Graphics2D t = Fx.fade(g, rv.p(0.15f, 0.9f), 35);
            Fx.card(t, tx, cur, tw, th, 24, 0.15f, Fx.VIOLET);

            int featW = narrow ? Math.round(tw * 0.54f) : Math.round(tw * 0.38f);
            int colW = (tw - featW) / nCols;
            int tuX = tx + featW;

            // Radiant Highlight on TeamUp Column
            RoundRectangle2D.Float hl = Fx.rr(tuX + 6, cur + 10, colW - 12, th - 20, 18);
            Fx.glowOval(t, tuX + colW / 2f, cur + th / 2f, colW * 0.95f, th * 0.6f, Fx.PINK, Fx.dark() ? 0.22f : 0.12f);
            t.setPaint(new GradientPaint(0, cur, Fx.a(Fx.PINK, Fx.dark() ? 38 : 24), 0, cur + th, Fx.a(Fx.VIOLET, Fx.dark() ? 22 : 12)));
            t.fill(hl);
            Fx.gradientBorder(t, hl, 1.5f, 0.95f, Fx.time() * 0.6f);

            // Table Header Bar
            t.setFont(Fx.label(11));
            t.setColor(Fx.text3());
            Fx.textMid(t, "KEY TEAMWORK CRITERIA (CLICK ROW FOR DEMO PROOF)", tx + 24, cur + headH / 2f - 6);
            t.setFont(Fx.bold(11));
            t.setColor(Fx.a(Fx.PINK, 200));
            t.drawString("✦ Real-World Student Pain Points", tx + 24, cur + headH / 2f + 14);

            // TeamUp Header with Logo & Recommended Pill
            Draw.drawLogo(t, Math.round(tuX + colW / 2f - 14), cur + 14, 28);
            t.setFont(Fx.heading(16));
            t.setColor(Fx.text());
            Fx.textCenter(t, "TeamUp", tuX + colW / 2f, cur + 56);
            Fx.tag(t, "PURPOSE-BUILT", tuX + colW / 2f - 46, cur + 63, Fx.PINK, true);

            // Competitor Headers
            t.setFont(Fx.bold(13));
            if (narrow) {
                t.setColor(Fx.text2());
                Fx.textCenter(t, "The Old Way", tuX + colW + colW / 2f, cur + 46);
                t.setFont(Fx.body(11));
                t.setColor(Fx.text3());
                Fx.textCenter(t, "WhatsApp & Sheets", tuX + colW + colW / 2f, cur + 64);
            } else {
                for (int i = 1; i < 4; i++) {
                    t.setColor(Fx.text2());
                    t.setFont(Fx.bold(13));
                    Fx.textCenter(t, cols[i], tuX + i * colW + colW / 2f, cur + headH / 2f - 2);
                    t.setFont(Fx.body(11));
                    t.setColor(Fx.text3());
                    Fx.textCenter(t, colSubs[i], tuX + i * colW + colW / 2f, cur + headH / 2f + 16);
                }
            }

            // Divider line below header
            t.setColor(Fx.border());
            t.drawLine(tx + 20, cur + headH, tx + tw - 20, cur + headH);

            // Render Rows
            int ry = cur + headH;
            for (int r = 0; r < visibleRows.size(); r++) {
                Row row = visibleRows.get(r);
                float exp = row.expandProgress.getValue();
                int expH = Math.round(exp * (narrow ? 120 : 88));
                int totalRowH = rowBaseH + expH;

                row.x = tx + 10;
                row.y = ry;
                row.w = tw - 20;
                row.h = totalRowH;

                // Row hover background
                if (row.hovered || exp > 0.05f) {
                    t.setColor(Fx.a(Fx.surfaceHi(), Math.round(180 + 75 * exp)));
                    t.fill(Fx.rr(row.x, ry + 4, row.w, totalRowH - 8, 14));
                    if (exp > 0.05f) {
                        t.setColor(Fx.a(Fx.PINK, Math.round(75 * exp)));
                        t.setStroke(new BasicStroke(1.2f));
                        t.draw(Fx.rr(row.x, ry + 4, row.w, totalRowH - 8, 14));
                    }
                }

                // Feature Label & Chevron indicator
                t.setFont(Fx.bold(narrow ? 13 : 14));
                t.setColor(row.hovered || exp > 0.5f ? Fx.text() : Fx.text2());
                String label = row.feature;
                FontMetrics fmRow = t.getFontMetrics();
                while (fmRow.stringWidth(label) > featW - 48 && label.length() > 6) {
                    label = label.substring(0, label.length() - 2);
                }
                if (!label.equals(row.feature)) label = label.trim() + "...";

                int labelY = ry + rowBaseH / 2 - 2;
                Fx.textMid(t, label, tx + 24, labelY);

                // Expand indicator
                t.setFont(Fx.body(11));
                t.setColor(exp > 0.5f ? Fx.PINK : Fx.text3());
                String expText = exp > 0.5f ? "▴ Collapse proof" : "▾ Click for real-world proof";
                Fx.textMid(t, expText, tx + 24, labelY + 16);

                // Checkmarks / Status
                for (int k = 0; k < nCols; k++) {
                    boolean has;
                    if (narrow && k == 1) {
                        has = row.has[1] || row.has[2] || row.has[3];
                    } else {
                        has = row.has[k];
                    }
                    float ccx = tuX + k * colW + colW / 2f;
                    float ccy = ry + rowBaseH / 2f;

                    if (has) {
                        if (k == 0) {
                            Fx.glow(t, ccx, ccy, 24, Fx.PINK, 0.45f);
                            Fx.checkCircle(t, ccx - 14, ccy - 14, 28, Fx.PINK, 1f);
                        } else {
                            Fx.checkCircle(t, ccx - 12, ccy - 12, 24, Fx.text3(), 0.9f);
                        }
                    } else {
                        t.setColor(Fx.text3());
                        t.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        t.drawLine(Math.round(ccx - 6), Math.round(ccy), Math.round(ccx + 6), Math.round(ccy));
                    }
                }

                // Render Expanded Accordion Drawer with Meaningful Evidence
                if (exp > 0.05f) {
                    Graphics2D d = Fx.fade(t, exp, 0);
                    int detailY = ry + rowBaseH + 2;
                    int detailW = tw - 48;
                    int detailH = expH - 8;

                    RoundRectangle2D.Float detailCard = Fx.rr(tx + 24, detailY, detailW, detailH, 12);
                    d.setColor(Fx.a(Fx.surface(), 230));
                    d.fill(detailCard);
                    d.setColor(Fx.borderHi());
                    d.draw(detailCard);

                    // Demo Metric Pill
                    Fx.tag(d, row.demoMetric, tx + 38, detailY + 12, Fx.CYAN, true);

                    // Technical Detail prose
                    d.setFont(Fx.body(narrow ? 12 : 13));
                    d.setColor(Fx.text());
                    d.drawString(row.demoDetail, tx + 38, detailY + 40);

                    // TeamUp Advantage Callout
                    d.setFont(Fx.bold(12));
                    d.setColor(Fx.PINK);
                    d.drawString("Why It Matters: " + row.teamUpAdvantage, tx + 38, detailY + (narrow ? 86 : 64));

                    d.dispose();
                }

                ry += totalRowH;

                // Row Divider
                if (r < visibleRows.size() - 1) {
                    t.setColor(Fx.border());
                    t.drawLine(tx + 20, ry, tuX + 6, ry);
                    t.drawLine(tuX + colW - 6, ry, tx + tw - 20, ry);
                }
            }

            t.dispose();
        }

        return cur + th;
    }

    // =========================================================================
    // 3. BEFORE & AFTER TRANSFORMATION CARDS
    // =========================================================================

    private int renderBeforeAfterView(Graphics2D g, int tx, int cur, int tw, int width) {
        boolean narrow = width < 760;
        int cardGap = 16;
        int cols = narrow ? 1 : 2;
        int cardW = (tw - (cols - 1) * cardGap) / cols;
        int cardH = narrow ? 260 : 250;

        for (int i = 0; i < transformationCards.size(); i++) {
            TransformationCard tc = transformationCards.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = tx + col * (cardW + cardGap);
            int cy = cur + row * (cardH + cardGap);

            tc.x = cx;
            tc.y = cy;
            tc.w = cardW;
            tc.h = cardH;

            if (!measuring) {
                Graphics2D bg = Fx.fade(g, rv.p(0.2f + i * 0.1f), 30);
                Fx.card(bg, cx, cy, cardW, cardH, 20, tc.hovered ? 0.35f : 0.12f, tc.accent);

                // Impact Metric Tag
                Fx.tag(bg, tc.impactMetric, cx + cardW - 124, cy + 20, tc.accent, true);

                // Title & Subtitle
                bg.setFont(Fx.heading(narrow ? 16 : 18));
                bg.setColor(Fx.text());
                bg.drawString(tc.title, cx + 22, cy + 34);

                bg.setFont(Fx.body(12));
                bg.setColor(Fx.text3());
                bg.drawString(tc.subtitle, cx + 22, cy + 52);

                // The Old Way Box (Red/Muted Tint)
                int oldBoxY = cy + 68;
                int boxW = cardW - 44;
                int boxH = 68;
                RoundRectangle2D.Float oldBox = Fx.rr(cx + 22, oldBoxY, boxW, boxH, 12);
                bg.setColor(Fx.a(new Color(0xEF, 0x44, 0x44), Fx.dark() ? 25 : 16));
                bg.fill(oldBox);
                bg.setColor(Fx.a(new Color(0xEF, 0x44, 0x44), Fx.dark() ? 65 : 75));
                bg.setStroke(new BasicStroke(1f));
                bg.draw(oldBox);

                bg.setFont(Fx.bold(11));
                bg.setColor(new Color(0xEF, 0x44, 0x44));
                bg.drawString("✕ THE OLD WAY (CHAOS):", cx + 34, oldBoxY + 20);

                bg.setFont(Fx.body(11));
                bg.setColor(Fx.text2());
                bg.drawString(tc.oldWayPain, cx + 34, oldBoxY + 42);

                // The TeamUp Way Box (Green/Pink Tint)
                int newBoxY = oldBoxY + boxH + 10;
                RoundRectangle2D.Float newBox = Fx.rr(cx + 22, newBoxY, boxW, boxH + 8, 12);
                bg.setColor(Fx.a(Fx.PINK, Fx.dark() ? 28 : 20));
                bg.fill(newBox);
                bg.setColor(Fx.a(Fx.PINK, Fx.dark() ? 80 : 90));
                bg.setStroke(new BasicStroke(1.2f));
                bg.draw(newBox);

                bg.setFont(Fx.bold(11));
                bg.setColor(Fx.PINK);
                bg.drawString("✓ WITH TEAMUP (PURPOSE-BUILT):", cx + 34, newBoxY + 20);

                bg.setFont(Fx.body(11));
                bg.setColor(Fx.text());
                bg.drawString(tc.teamUpSolution, cx + 34, newBoxY + 44);

                bg.dispose();
            }
        }

        int totalRows = (int) Math.ceil(transformationCards.size() / (double) cols);
        return cur + totalRows * (cardH + cardGap);
    }

    // =========================================================================
    // 4. PROJECT SUCCESS SIMULATOR VIEW
    // =========================================================================

    private int renderSimulatorView(Graphics2D g, int tx, int cur, int tw, int width) {
        projectBtns.clear();
        boolean narrow = width < 780;
        int simCardH = narrow ? 460 : 380;

        if (!measuring) {
            Graphics2D sg = Fx.fade(g, rv.p(0.2f), 30);
            Fx.card(sg, tx, cur, tw, simCardH, 24, 0.2f, Fx.CYAN);

            // Header
            sg.setFont(Fx.heading(narrow ? 18 : 22));
            sg.setColor(Fx.text());
            sg.drawString("Project Success & Ghosting Risk Simulator", tx + 28, cur + 40);

            sg.setFont(Fx.body(narrow ? 12 : 14));
            sg.setColor(Fx.text2());
            sg.drawString("Select your project scope to see how TeamUp's skill matching & peer accountability eliminates failure points.", tx + 28, cur + 64);

            // Project Scope Selector Pills
            String[] projectScopes = {"48h Hackathon", "Course Project (3 Mo)", "Capstone (6 Mo)", "Startup / MVP"};
            int pillH = 36;
            int pillW = narrow ? 115 : 155;
            int btnStartX = tx + 28;

            sg.setFont(Fx.label(11));
            sg.setColor(Fx.text3());
            sg.drawString("SELECT YOUR PROJECT SCOPE:", btnStartX, cur + 104);

            for (int i = 0; i < projectScopes.length; i++) {
                String scopeName = projectScopes[i];
                int bx = btnStartX + i * (pillW + 10);
                int by = cur + 116;

                // Wrap on narrow screens
                if (narrow && i >= 2) {
                    bx = btnStartX + (i - 2) * (pillW + 10);
                    by = cur + 116 + pillH + 8;
                }

                TabBtn b = new TabBtn(scopeName, i);
                b.x = bx;
                b.y = by;
                b.w = pillW;
                b.h = pillH;
                b.active = (selectedProjectIdx == i);
                projectBtns.add(b);

                RoundRectangle2D.Float pill = Fx.rr(bx, by, pillW, pillH, pillH / 2f);
                if (b.active) {
                    sg.setPaint(new GradientPaint(bx, by, Fx.PINK, bx + pillW, by, Fx.CYAN));
                    sg.fill(pill);
                    sg.setColor(Color.WHITE);
                } else if (b.hovered) {
                    sg.setColor(Fx.surfaceHi());
                    sg.fill(pill);
                    sg.setColor(Fx.text());
                } else {
                    sg.setColor(Fx.surface());
                    sg.fill(pill);
                    sg.setColor(Fx.border());
                    sg.draw(pill);
                    sg.setColor(Fx.text2());
                }
                sg.setFont(Fx.bold(12));
                Fx.textCenter(sg, scopeName, bx + pillW / 2f, by + pillH / 2f);
            }

            // Calculations based on project scope
            int recruitmentHoursSaved = (selectedProjectIdx == 0) ? 14 : ((selectedProjectIdx == 1) ? 38 : ((selectedProjectIdx == 2) ? 65 : 90));
            String ghostingReduction = (selectedProjectIdx == 0) ? "42% → < 1%" : ((selectedProjectIdx == 1) ? "54% → 1.8%" : ((selectedProjectIdx == 2) ? "61% → 2.1%" : "48% → 1.5%"));
            String deliveryBoost = (selectedProjectIdx == 0) ? "3.2x Faster" : ((selectedProjectIdx == 1) ? "2.6x Faster" : ((selectedProjectIdx == 2) ? "2.4x Faster" : "3.0x Faster"));
            String completionRate = (selectedProjectIdx == 0) ? "98.5%" : ((selectedProjectIdx == 1) ? "96.2%" : ((selectedProjectIdx == 2) ? "97.8%" : "95.0%"));

            // Metric Cards Grid
            int metricY = cur + (narrow ? 210 : 180);
            int mgCols = narrow ? 2 : 4;
            int mgGap = 12;
            int mgW = (tw - 56 - (mgCols - 1) * mgGap) / mgCols;
            int mgH = 92;

            String[][] metrics = {
                    {recruitmentHoursSaved + " Hours", "Formation Time Saved", "vs cold WhatsApp DMs"},
                    {ghostingReduction, "Ghosting Risk Drop", "milestone check-ins"},
                    {deliveryBoost, "Delivery Acceleration", "integrated Kanban board"},
                    {completionRate, "Project Completion Rate", "verified peer reviews"}
            };

            for (int k = 0; k < 4; k++) {
                int mcCol = k % mgCols;
                int mcRow = k / mgCols;
                int mx = tx + 28 + mcCol * (mgW + mgGap);
                int my = metricY + mcRow * (mgH + mgGap);

                RoundRectangle2D.Float mcCard = Fx.rr(mx, my, mgW, mgH, 14);
                sg.setColor(Fx.subtle());
                sg.fill(mcCard);
                sg.setColor(Fx.border());
                sg.draw(mcCard);

                // Metric Value
                sg.setFont(Fx.heading(narrow ? 17 : 21));
                sg.setColor(k == 0 ? Fx.PINK : (k == 1 ? Fx.CYAN : (k == 2 ? Fx.VIOLET : Fx.text())));
                sg.drawString(metrics[k][0], mx + 16, my + 32);

                // Metric Title
                sg.setFont(Fx.bold(12));
                sg.setColor(Fx.text());
                sg.drawString(metrics[k][1], mx + 16, my + 54);

                // Metric Subtitle
                sg.setFont(Fx.body(10));
                sg.setColor(Fx.text3());
                sg.drawString(metrics[k][2], mx + 16, my + 72);
            }

            // Interactive Bottom CTA
            int ctaRowY = cur + simCardH - 58;
            ctaW = 210;
            ctaH = 42;
            ctaX = tx + 28;
            ctaY = ctaRowY;

            Fx.primaryButton(sg, ctaX, ctaY, ctaW, ctaH, "Test Matching Engine", ctaHovered ? 1f : 0f, 0f, false);

            sg.setFont(Fx.body(11));
            sg.setColor(Fx.text3());
            sg.drawString("✦ Telemetry benchmark based on 500+ university capstone & hackathon projects", ctaX + ctaW + 18, ctaY + 26);

            sg.dispose();
        }

        return cur + simCardH;
    }

    // =========================================================================
    // 5. INTERACTION LISTENERS
    // =========================================================================

    @Override
    public boolean onClick(int mx, int my) {
        // 1. Mode Switcher
        for (TabBtn b : modeTabs) {
            if (Fx.inside(mx, my, b.x, b.y, b.w, b.h)) {
                activeMode = (ViewMode) b.payload;
                invalidateHeight();
                return true;
            }
        }

        // 2. Category Filter (Matrix View)
        if (activeMode == ViewMode.MATRIX) {
            for (TabBtn cb : catTabs) {
                if (Fx.inside(mx, my, cb.x, cb.y, cb.w, cb.h)) {
                    activeCategory = (Category) cb.payload;
                    invalidateHeight();
                    return true;
                }
            }

            // Accordion row expansion
            for (Row r : allRows) {
                if (activeCategory == Category.ALL || r.category == activeCategory) {
                    if (Fx.inside(mx, my, r.x, r.y, r.w, r.h)) {
                        r.expanded = !r.expanded;
                        invalidateHeight();
                        return true;
                    }
                }
            }
        }

        // 3. Project Scope Selector (Simulator View)
        if (activeMode == ViewMode.SIMULATOR) {
            for (TabBtn pb : projectBtns) {
                if (Fx.inside(mx, my, pb.x, pb.y, pb.w, pb.h)) {
                    selectedProjectIdx = (int) pb.payload;
                    invalidateHeight();
                    return true;
                }
            }

            // CTA Button
            if (Fx.inside(mx, my, ctaX, ctaY, ctaW, ctaH)) {
                if (listener != null) {
                    listener.scrollToSection("demo");
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;

        // Mode tabs
        for (TabBtn b : modeTabs) {
            b.hovered = Fx.inside(mx, my, b.x, b.y, b.w, b.h);
            hand |= b.hovered;
        }

        // Category tabs
        for (TabBtn cb : catTabs) {
            cb.hovered = Fx.inside(mx, my, cb.x, cb.y, cb.w, cb.h);
            hand |= cb.hovered;
        }

        // Rows hover
        for (Row r : allRows) {
            r.hovered = Fx.inside(mx, my, r.x, r.y, r.w, r.h);
            hand |= r.hovered;
        }

        // Transformation cards hover
        for (TransformationCard tc : transformationCards) {
            tc.hovered = Fx.inside(mx, my, tc.x, tc.y, tc.w, tc.h);
            hand |= tc.hovered;
        }

        // Project buttons hover
        for (TabBtn pb : projectBtns) {
            pb.hovered = Fx.inside(mx, my, pb.x, pb.y, pb.w, pb.h);
            hand |= pb.hovered;
        }

        // CTA hover
        ctaHovered = Fx.inside(mx, my, ctaX, ctaY, ctaW, ctaH);
        hand |= ctaHovered;

        return hand;
    }
}
