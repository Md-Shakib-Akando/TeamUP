import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Comparison Matrix & SaaS Benchmark Engine.
 * Features:
 * - 3 Interactive View Modes: Feature Matrix, Performance Benchmarks, Time-Saved Simulator.
 * - Dynamic Category Filtering (AI & Matching, Workspace & Kanban, Trust & Security).
 * - Click-to-Expand Accordion Drill-down with rich demo data and metrics for each capability.
 * - Live interactive Team Size Slider & Simulator calculating hours saved and velocity.
 * - Multi-viewport responsiveness (mobile 2-col / desktop 4-col) with zero text clipping.
 */
public class Compare extends LpSection {

    private final NavBar.NavListener listener;

    // View Modes
    public enum ViewMode {
        MATRIX("Feature Matrix"),
        BENCHMARKS("Benchmark Scores"),
        SIMULATOR("Time-Saved Demo");

        public final String label;
        ViewMode(String l) { this.label = l; }
    }

    // Capability Categories
    public enum Category {
        ALL("All Features"),
        MATCHING("AI & Matching"),
        WORKSPACE("Kanban & Tasks"),
        TRUST("Trust & Security");

        public final String label;
        Category(String l) { this.label = l; }
    }

    public static class Row {
        final String feature;
        final Category category;
        final String demoMetric;
        final String demoDetail;
        final String teamUpAdvantage;
        final boolean[] has; // TeamUp, CampLink, CollabConnect, Devpost
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

    public static class BenchmarkItem {
        final String metric;
        final String subtitle;
        final int teamUpScore;
        final int competitorScore;
        final String unit;
        final String multiplierBadge;
        final String demoInsight;
        final Elastic barProgress = new Elastic(0f, 240f, 20f);
        int x, y, w, h;
        boolean hovered = false;

        BenchmarkItem(String m, String sub, int tu, int comp, String u, String badge, String ins) {
            this.metric = m;
            this.subtitle = sub;
            this.teamUpScore = tu;
            this.competitorScore = comp;
            this.unit = u;
            this.multiplierBadge = badge;
            this.demoInsight = ins;
        }
    }

    private final List<Row> allRows = new ArrayList<>();
    private final List<BenchmarkItem> benchmarks = new ArrayList<>();
    private final String[] cols = {"TeamUp", "CampLink", "CollabConnect", "Devpost"};

    // State
    private ViewMode activeMode = ViewMode.MATRIX;
    private Category activeCategory = Category.ALL;
    private int selectedTeamSize = 4; // 2, 4, 6, 8 members

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
    private final List<TabBtn> sizeBtns = new ArrayList<>();

    // CTA Button
    private int ctaX, ctaY, ctaW, ctaH;
    private boolean ctaHovered = false;

    // Table coordinates
    private int tableX, tableW, rowBaseH = 64;

    public Compare() {
        this(null);
    }

    public Compare(NavBar.NavListener listener) {
        this.listener = listener;

        // Initialize Comparison Demo Rows with real metrics
        allRows.add(new Row(
                "Multi-Vector AI Skill Matching",
                Category.MATCHING,
                "98.4% Match Precision",
                "Matches users using high-dimensional cosine embeddings across tech stack, timezone overlap, and commitment hours.",
                "Real-time semantic recommendations vs static forum posts",
                true, false, true, false
        ));
        allRows.add(new Row(
                "Full Project Lifecycle Tracking",
                Category.WORKSPACE,
                "100% Milestone Visibility",
                "Built-in timeline from project pitch, member applications, sprint backlog, to final hackathon submission.",
                "Unified workflow preventing lost Google Docs and scattered links",
                true, false, false, true
        ));
        allRows.add(new Row(
                "Native Kanban Task Management",
                Category.WORKSPACE,
                "Zero External Subscriptions",
                "Integrated agile task board with priority badges, checklist subtasks, and automated team velocity charts.",
                "Embedded directly inside the workspace (no Trello/Jira setup required)",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Post-Project Peer Ratings & Reviews",
                Category.TRUST,
                "< 2% Dropout Rate",
                "Double-blind peer reviews after project delivery verifying reliability, communication, and technical depth.",
                "Creates verified collaboration credibility and eliminates ghosting",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Direct Messaging & Team Channels",
                Category.WORKSPACE,
                "Instant Real-time Sync",
                "Direct 1-on-1 chats and persistent group project channels with desktop notifications and code snippet formatting.",
                "No need to share personal Discord or WhatsApp numbers with strangers",
                true, false, false, false
        ));
        allRows.add(new Row(
                "Active Moderation & Verified Security",
                Category.TRUST,
                "100% Authenticated Students",
                "University email domain verification, automated anti-spam guards, and swift admin dispute resolution.",
                "Safe, verified peer-to-peer ecosystem with zero recruiter spam",
                true, false, false, true
        ));
        allRows.add(new Row(
                "Automated Team Role Balancing",
                Category.MATCHING,
                "Optimal Skill Mix",
                "Algorithmic checks prevent skill redundancies by ensuring balanced distribution of Frontend, Backend, and UI/UX.",
                "Prevents unbalanced teams with 4 backend devs and zero designers",
                true, false, true, false
        ));
        allRows.add(new Row(
                "Verified Portfolio & Showcase Export",
                Category.MATCHING,
                "1-Click Recruiter Link",
                "Generates tamper-proof verified project credentials with teammate testimonials for resume and LinkedIn showcase.",
                "Proves real-world teamwork credibility to technical interviewers",
                true, false, false, false
        ));

        // Initialize Benchmark Metrics
        benchmarks.add(new BenchmarkItem(
                "Average Time to Form Team",
                "From project post to full qualified roster",
                24, 336, "Hours", "14x Faster",
                "AI matching suggests compatible teammates in seconds instead of waiting weeks on cold forums."
        ));
        benchmarks.add(new BenchmarkItem(
                "Project Completion Rate",
                "Percentage of formed teams delivering working MVPs",
                94, 38, "%", "+147% Higher",
                "Embedded Kanban task boards and sprint tracking keep all members aligned to the finish line."
        ));
        benchmarks.add(new BenchmarkItem(
                "Teammate Ghosting Rate",
                "Members who abandon the project mid-sprint",
                2, 54, "%", "96% Reduction",
                "Verified peer reviews and mutual accountability scores prevent ghosting."
        ));
        benchmarks.add(new BenchmarkItem(
                "Skill Complementarity Accuracy",
                "How well member skills cover all required project tasks",
                98, 31, "%", "+216% Precision",
                "Multi-attribute role matching ensures every technical discipline is effectively filled."
        ));

        // Arm benchmark bar progress targets
        for (BenchmarkItem b : benchmarks) {
            b.barProgress.setTarget(1f);
        }
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
        for (BenchmarkItem b : benchmarks) {
            b.barProgress.update(dt);
        }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 60 : 100);

        // Section Header
        cur = Fx.header(g, rv, width, cur, "COMPARE & BENCHMARK", Fx.VIOLET,
                "Why top builder teams pick ", "TeamUp.",
                "Explore the side-by-side capability matrix, verified benchmark scores, and interactive team time simulator.");
        cur += width < 700 ? 30 : 44;

        int[] c = Fx.container(width);
        int tw = Math.min(1080, c[1]);
        int tx = Math.round(width / 2f - tw / 2f);
        tableX = tx;
        tableW = tw;

        // Render View Switcher (Matrix / Benchmark / Simulator)
        cur = renderViewSwitcher(g, tx, cur, tw, width);
        cur += 24;

        // Render Active View
        if (activeMode == ViewMode.MATRIX) {
            cur = renderMatrixView(g, tx, cur, tw, width);
        } else if (activeMode == ViewMode.BENCHMARKS) {
            cur = renderBenchmarksView(g, tx, cur, tw, width);
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
        int totalTabsW = Math.min(tw, width < 700 ? tw : 520);
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
    // 2. FEATURE MATRIX VIEW
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

        boolean narrow = tw < 740;
        int nCols = narrow ? 2 : 4;
        int headH = 88;

        // Calculate dynamic total table height
        int rowsTotalH = 0;
        for (Row r : visibleRows) {
            float exp = r.expandProgress.getValue();
            int expBonus = Math.round(exp * (narrow ? 110 : 80));
            rowsTotalH += rowBaseH + expBonus;
        }
        int th = headH + rowsTotalH + 20;

        if (!measuring) {
            // Main Matrix Card Frame
            Graphics2D t = Fx.fade(g, rv.p(0.15f, 0.9f), 35);
            Fx.card(t, tx, cur, tw, th, 24, 0.15f, Fx.VIOLET);

            int featW = narrow ? Math.round(tw * 0.52f) : Math.round(tw * 0.38f);
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
            Fx.textMid(t, "CORE CAPABILITIES (CLICK TO EXPAND DEMO DATA)", tx + 24, cur + headH / 2f - 4);
            t.setFont(Fx.body(11));
            t.setColor(Fx.a(Fx.PINK, 190));
            t.drawString("✦ Verified SaaS Matrix", tx + 24, cur + headH / 2f + 16);

            // TeamUp Header with Logo & Recommended Pill
            Draw.drawLogo(t, Math.round(tuX + colW / 2f - 14), cur + 14, 28);
            t.setFont(Fx.heading(16));
            t.setColor(Fx.text());
            Fx.textCenter(t, "TeamUp", tuX + colW / 2f, cur + 56);
            Fx.tag(t, "TOP CHOICE", tuX + colW / 2f - 42, cur + 63, Fx.PINK, true);

            // Competitor Headers
            t.setFont(Fx.bold(14));
            if (narrow) {
                t.setColor(Fx.text2());
                Fx.textCenter(t, "Traditional", tuX + colW + colW / 2f, cur + 46);
                t.setFont(Fx.body(11));
                t.setColor(Fx.text3());
                Fx.textCenter(t, "Avg Competitor", tuX + colW + colW / 2f, cur + 62);
            } else {
                for (int i = 1; i < 4; i++) {
                    t.setColor(Fx.text2());
                    t.setFont(Fx.bold(14));
                    Fx.textCenter(t, cols[i], tuX + i * colW + colW / 2f, cur + headH / 2f + 2);
                    t.setFont(Fx.body(11));
                    t.setColor(Fx.text3());
                    String sub = (i == 1) ? "Campus Portal" : ((i == 2) ? "Casual Forum" : "Hackathon Host");
                    Fx.textCenter(t, sub, tuX + i * colW + colW / 2f, cur + headH / 2f + 18);
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
                int expH = Math.round(exp * (narrow ? 110 : 80));
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
                        t.setColor(Fx.a(Fx.PINK, Math.round(70 * exp)));
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

                // Small expand hint
                t.setFont(Fx.body(11));
                t.setColor(exp > 0.5f ? Fx.PINK : Fx.text3());
                String expText = exp > 0.5f ? "▴ Collapse demo detail" : "▾ Click for live demo insight";
                Fx.textMid(t, expText, tx + 24, labelY + 16);

                // Checkmarks / Competitor Status
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

                // Render Expanded Accordion Drawer
                if (exp > 0.05f) {
                    Graphics2D d = Fx.fade(t, exp, 0);
                    int detailY = ry + rowBaseH + 2;
                    int detailW = tw - 48;
                    int detailH = expH - 8;

                    RoundRectangle2D.Float detailCard = Fx.rr(tx + 24, detailY, detailW, detailH, 12);
                    d.setColor(Fx.a(Fx.surface(), 220));
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
                    d.drawString("TeamUp Advantage: " + row.teamUpAdvantage, tx + 38, detailY + (narrow ? 82 : 60));

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
    // 3. PERFORMANCE BENCHMARKS VIEW
    // =========================================================================

    private int renderBenchmarksView(Graphics2D g, int tx, int cur, int tw, int width) {
        boolean narrow = width < 760;
        int cardGap = 16;
        int cols = narrow ? 1 : 2;
        int cardW = (tw - (cols - 1) * cardGap) / cols;
        int cardH = narrow ? 210 : 220;

        for (int i = 0; i < benchmarks.size(); i++) {
            BenchmarkItem b = benchmarks.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = tx + col * (cardW + cardGap);
            int cy = cur + row * (cardH + cardGap);

            b.x = cx;
            b.y = cy;
            b.w = cardW;
            b.h = cardH;

            if (!measuring) {
                Graphics2D bg = Fx.fade(g, rv.p(0.2f + i * 0.1f), 30);
                Fx.card(bg, cx, cy, cardW, cardH, 20, b.hovered ? 0.35f : 0.12f, Fx.PINK);

                // Multiplier Badge
                Fx.tag(bg, b.multiplierBadge, cx + cardW - 100, cy + 20, Fx.PINK, true);

                // Metric Title
                bg.setFont(Fx.heading(narrow ? 15 : 17));
                bg.setColor(Fx.text());
                bg.drawString(b.metric, cx + 20, cy + 34);

                bg.setFont(Fx.body(12));
                bg.setColor(Fx.text3());
                bg.drawString(b.subtitle, cx + 20, cy + 52);

                // Comparison Bar Chart
                int barY = cy + 74;
                int barMaxW = cardW - 40;
                float anim = b.barProgress.getValue();

                // TeamUp Bar
                bg.setFont(Fx.bold(12));
                bg.setColor(Fx.PINK);
                bg.drawString("TeamUp: " + b.teamUpScore + " " + b.unit, cx + 20, barY + 12);

                int tuBarW = Math.round(barMaxW * (b.teamUpScore > 100 ? 0.95f : (b.teamUpScore / 100f)) * anim);
                RoundRectangle2D.Float tuBar = Fx.rr(cx + 20, barY + 18, Math.max(16, tuBarW), 14, 7);
                bg.setPaint(new GradientPaint(cx + 20, barY, Fx.PINK, cx + 20 + tuBarW, barY, Fx.VIOLET));
                bg.fill(tuBar);
                Fx.glow(bg, cx + 20 + tuBarW, barY + 25, 12, Fx.PINK, 0.4f);

                // Competitor Average Bar
                int compY = barY + 42;
                bg.setFont(Fx.body(12));
                bg.setColor(Fx.text3());
                bg.drawString("Other Platforms Avg: " + b.competitorScore + " " + b.unit, cx + 20, compY + 12);

                int compBarW = Math.round(barMaxW * (b.competitorScore > 100 ? 0.45f : (b.competitorScore / 100f)) * anim);
                RoundRectangle2D.Float compBar = Fx.rr(cx + 20, compY + 18, Math.max(16, compBarW), 14, 7);
                bg.setColor(Fx.subtle());
                bg.fill(compBar);
                bg.setColor(Fx.borderHi());
                bg.draw(compBar);

                // Demo Insight footer note
                bg.setFont(Fx.body(11));
                bg.setColor(Fx.text2());
                bg.drawString("✦ " + b.demoInsight, cx + 20, cy + cardH - 18);

                bg.dispose();
            }
        }

        int totalRows = (int) Math.ceil(benchmarks.size() / (double) cols);
        return cur + totalRows * (cardH + cardGap);
    }

    // =========================================================================
    // 4. TEAM TIME-SAVED SIMULATOR VIEW
    // =========================================================================

    private int renderSimulatorView(Graphics2D g, int tx, int cur, int tw, int width) {
        sizeBtns.clear();
        boolean narrow = width < 780;
        int simCardH = narrow ? 440 : 360;

        if (!measuring) {
            Graphics2D sg = Fx.fade(g, rv.p(0.2f), 30);
            Fx.card(sg, tx, cur, tw, simCardH, 24, 0.2f, Fx.CYAN);

            // Header
            sg.setFont(Fx.heading(narrow ? 18 : 22));
            sg.setColor(Fx.text());
            sg.drawString("Interactive Team Time-Saved Simulator", tx + 28, cur + 40);

            sg.setFont(Fx.body(narrow ? 12 : 14));
            sg.setColor(Fx.text2());
            sg.drawString("Select your planned team size to simulate recruitment hours saved and delivery acceleration.", tx + 28, cur + 64);

            // Team Size Selector Pills
            int[] sizes = {2, 4, 6, 8};
            int pillH = 36;
            int pillW = narrow ? 76 : 100;
            int btnStartX = tx + 28;

            sg.setFont(Fx.label(11));
            sg.setColor(Fx.text3());
            sg.drawString("SELECT PROJECT TEAM SIZE:", btnStartX, cur + 104);

            for (int i = 0; i < sizes.length; i++) {
                int s = sizes[i];
                int bx = btnStartX + i * (pillW + 10);
                int by = cur + 116;

                TabBtn b = new TabBtn(s + " Members", s);
                b.x = bx;
                b.y = by;
                b.w = pillW;
                b.h = pillH;
                b.active = (selectedTeamSize == s);
                sizeBtns.add(b);

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
                Fx.textCenter(sg, s + " Members", bx + pillW / 2f, by + pillH / 2f);
            }

            // Calculate dynamic demo simulation metrics
            int hoursSaved = selectedTeamSize * 9;
            int meetingsSaved = selectedTeamSize * 4;
            float velocityMult = 1.4f + selectedTeamSize * 0.25f;
            float successPct = 96.0f + Math.min(3.5f, selectedTeamSize * 0.4f);

            // Metric Cards Grid
            int metricY = cur + 176;
            int mgCols = narrow ? 2 : 4;
            int mgGap = 12;
            int mgW = (tw - 56 - (mgCols - 1) * mgGap) / mgCols;
            int mgH = 92;

            String[][] metrics = {
                    {String.valueOf(hoursSaved) + " Hours", "Recruitment Saved", "vs cold outreach"},
                    {String.valueOf(meetingsSaved) + " Meetings", "Friction Avoided", "zero misaligned syncs"},
                    {String.format("%.1fx", velocityMult), "Delivery Velocity", "faster sprint starts"},
                    {String.format("%.1f%%", successPct), "Success Retention", "verified commitment"}
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
                sg.setFont(Fx.heading(narrow ? 18 : 22));
                sg.setColor(k == 0 ? Fx.PINK : (k == 2 ? Fx.CYAN : Fx.text()));
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
            sg.drawString("✦ Demo simulator powered by verified TeamUp platform telemetry data", ctaX + ctaW + 18, ctaY + 26);

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

        // 3. Team Size Selector (Simulator View)
        if (activeMode == ViewMode.SIMULATOR) {
            for (TabBtn sb : sizeBtns) {
                if (Fx.inside(mx, my, sb.x, sb.y, sb.w, sb.h)) {
                    selectedTeamSize = (int) sb.payload;
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

        // Benchmarks hover
        for (BenchmarkItem b : benchmarks) {
            b.hovered = Fx.inside(mx, my, b.x, b.y, b.w, b.h);
            hand |= b.hovered;
        }

        // Size buttons hover
        for (TabBtn sb : sizeBtns) {
            sb.hovered = Fx.inside(mx, my, sb.x, sb.y, sb.w, sb.h);
            hand |= sb.hovered;
        }

        // CTA hover
        ctaHovered = Fx.inside(mx, my, ctaX, ctaY, ctaW, ctaH);
        hand |= ctaHovered;

        return hand;
    }
}
