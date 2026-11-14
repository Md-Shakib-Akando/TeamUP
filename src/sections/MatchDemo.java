import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Live matching playground: toggle required skills and watch candidates
 * re-rank with spring-animated rows. Also receives queries from the hero search.
 */
public class MatchDemo extends LpSection {

    public static class Candidate {
        public final String name, role, initials;
        public final Color c1, c2;
        public final Set<String> skills = new LinkedHashSet<>();
        public final double rating;
        public final String university, availability, bio;
        public final int projectsDone;
        public final List<String> messages = new ArrayList<>();
        public float currentMatch = 0.85f, targetMatch = 0.85f;
        public final Elastic rowY = new Elastic(0f, 160f, 17f);

        int profX, profY, profW, profH;
        boolean profHover;
        int msgX, msgY, msgW, msgH;
        boolean msgHover;

        public Candidate(String name, String role, String initials, Color c1, Color c2, double rating, String... sk) {
            this(name, role, initials, c1, c2, rating, "University Member", "Flexible · Weekly sprints", 4,
                    "Verified student collaborator ready for capstone and hackathon projects.", sk);
        }

        public Candidate(String name, String role, String initials, Color c1, Color c2, double rating,
                         String university, String availability, int projectsDone, String bio, String... sk) {
            this.name = name; this.role = role; this.initials = initials;
            this.c1 = c1; this.c2 = c2; this.rating = rating;
            this.university = university; this.availability = availability;
            this.projectsDone = projectsDone; this.bio = bio;
            Collections.addAll(skills, sk);
        }
    }

    private enum DrawerMode { NONE, PROFILE, MESSAGE }

    private static class SkillChip {
        final String label;
        boolean on, hovered;
        int x, y, w, h;
        final Elastic sel = new Elastic(0f, 260f, 18f);
        SkillChip(String l, boolean on) { label = l; this.on = on; sel.setValue(on ? 1f : 0f); }
    }

    private final List<Candidate> candidates = new ArrayList<>();
    private final List<SkillChip> chips = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private static final int ROW_H = 104, ROW_GAP = 10;

    private int resetX, resetY, resetW, resetH;
    private boolean resetHover;
    private float pulse = 0f; // flash when ranking changes

    private DrawerMode drawerMode = DrawerMode.NONE;
    private Candidate activeCandidate = null;
    private final TextBox msgBox = new TextBox("Write a quick intro message...");
    private final String[] quickReplies = {"Join Campus Connect?", "Free for a quick sync?", "Love your skill stack!"};
    private final int[][] quickBounds = new int[quickReplies.length][4];
    private int quickHoverIdx = -1;
    private int tabProfX, tabProfY, tabProfW, tabProfH;
    private int tabMsgX, tabMsgY, tabMsgW, tabMsgH;
    private int closeX, closeY, closeW, closeH;
    private int drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH;
    private boolean tabProfHover, tabMsgHover, closeHover, drawerBtnHover;
    private String sendStatus = "";
    private float sendStatusT = 0f;

    public MatchDemo() {
        candidates.add(new Candidate("Sarah Lin", "Backend & Systems", "SL", Fx.ORANGE, Fx.PINK, 4.9,
                "Stanford CS · Senior", "15 hrs/wk · Evenings", 6,
                "Architects resilient Java/MySQL APIs and loves clean database schemas and sprint demos.",
                "Java", "MySQL", "Testing", "Cloud"));
        candidates.add(new Candidate("David Chen", "Product Designer", "DC", Fx.VIOLET, new Color(0x6E, 0xC1, 0xFF), 5.0,
                "MIT Media Lab · Junior", "12 hrs/wk · Flexible", 5,
                "Crafts accessible design systems and rapid React UI prototypes for student product teams.",
                "Design", "React"));
        candidates.add(new Candidate("Priya Patel", "Full-Stack Engineer", "PP", Fx.MINT, Fx.CYAN, 4.8,
                "UC Berkeley EECS · Senior", "18 hrs/wk · Weekdays", 7,
                "Ships end-to-end Python/Java web services with CI testing and cloud deployment.",
                "Java", "Python", "Cloud", "Testing"));
        candidates.add(new Candidate("Marcus Vance", "Frontend & Mobile", "MV", Fx.AMBER, Fx.ORANGE, 4.7,
                "CMU HCI · Sophomore", "10 hrs/wk · Weekends", 4,
                "Bridges motion UI polish with Java & React frontends. Fast async communicator.",
                "Design", "React", "Java"));

        String[] all = {"Java", "Design", "MySQL", "Testing", "React", "Python", "Cloud"};
        selected.add("Java");
        selected.add("MySQL");
        for (String s : all) chips.add(new SkillChip(s, selected.contains(s)));
        recompute(false);
    }

    /** True while the inline message composer input has focus. */
    public boolean hasFocus() {
        return drawerMode == DrawerMode.MESSAGE && activeCandidate != null && msgBox.focused;
    }

    /** Maps a free-text search to demo skills and re-ranks. */
    public void applyQuery(String q) {
        if (q == null) return;
        String s = q.toLowerCase(Locale.ROOT);
        Set<String> found = new LinkedHashSet<>();
        if (s.contains("java") && !s.contains("javascript")) found.add("Java");
        if (s.contains("ui") || s.contains("ux") || s.contains("design") || s.contains("figma")) found.add("Design");
        if (s.contains("sql") || s.contains("database") || s.contains("postgres")) found.add("MySQL");
        if (s.contains("test") || s.contains("qa")) found.add("Testing");
        if (s.contains("react") || s.contains("frontend") || s.contains("javascript")) found.add("React");
        if (s.contains("python") || s.contains("ml") || s.contains("ai") || s.contains("data")) found.add("Python");
        if (s.contains("cloud") || s.contains("aws") || s.contains("devops") || s.contains("docker")) found.add("Cloud");
        if (found.isEmpty()) return;
        selected.clear();
        selected.addAll(found);
        for (SkillChip c : chips) c.on = selected.contains(c.label);
        recompute(true);
    }

    private void toggle(SkillChip c) {
        c.on = !c.on;
        if (c.on) selected.add(c.label); else selected.remove(c.label);
        recompute(true);
    }

    private void recompute(boolean animate) {
        for (Candidate c : candidates) {
            if (selected.isEmpty()) {
                c.targetMatch = 0.45f;
            } else {
                int m = 0;
                for (String s : selected) if (c.skills.contains(s)) m++;
                float ratio = m / (float) selected.size();
                c.targetMatch = Math.min(0.99f, Math.max(0.25f, ratio * 0.82f + 0.15f + (float) (c.rating - 4.7) * 0.05f));
            }
        }
        candidates.sort((a, b) -> Float.compare(b.targetMatch, a.targetMatch));
        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);
            c.rowY.setTarget(i * (ROW_H + ROW_GAP));
            if (!animate) { c.rowY.setValue(i * (ROW_H + ROW_GAP)); c.currentMatch = c.targetMatch; }
        }
        if (animate) pulse = 1f;
    }

    @Override public String getId() { return "demo"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (Candidate c : candidates) {
            c.currentMatch = Anim.approach(c.currentMatch, c.targetMatch, dt, 6f);
            c.rowY.update(dt);
        }
        for (SkillChip c : chips) { c.sel.setTarget(c.on ? 1f : 0f); c.sel.update(dt); }
        pulse = Math.max(0f, pulse - dt * 1.4f);
        msgBox.update(dt);
        if (sendStatusT > 0f) sendStatusT = Math.max(0f, sendStatusT - dt);
    }

    private void openDrawer(Candidate c, DrawerMode mode) {
        if (c == null) return;
        if (activeCandidate == c && drawerMode == mode) {
            closeDrawer();
            return;
        }
        activeCandidate = c;
        drawerMode = mode;
        sendStatus = "";
        msgBox.hasError = false;
        if (mode == DrawerMode.MESSAGE) {
            msgBox.placeholder = "Write a message to " + c.name + "...";
            msgBox.focused = true;
        } else {
            msgBox.focused = false;
        }
        invalidateHeight();
    }

    private void closeDrawer() {
        drawerMode = DrawerMode.NONE;
        activeCandidate = null;
        msgBox.focused = false;
        msgBox.hasError = false;
        sendStatus = "";
        invalidateHeight();
    }

    private void sendCurrentMessage() {
        if (activeCandidate == null) return;
        String txt = msgBox.getText().trim();
        if (txt.isEmpty()) {
            msgBox.hasError = true;
            sendStatus = "Please type or pick a quick message first.";
            return;
        }
        activeCandidate.messages.add(txt);
        msgBox.setText("");
        msgBox.hasError = false;
        sendStatus = "Message sent to " + activeCandidate.name + " ✓";
        sendStatusT = 4.0f;
        invalidateHeight();
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "LIVE DEMO", Fx.PINK,
                "See matching in ", "real time.",
                "Pick the skills your project needs - candidates re-rank instantly. Try searching from the top of the page too.");
        cur += width < 700 ? 36 : 56;

        int[] c = Fx.container(width);
        int pw = Math.min(1080, c[1]);
        int px = Math.round(width / 2f - pw / 2f);
        boolean side = pw >= 860;
        int leftW = side ? 300 : pw;
        int listH = candidates.size() * (ROW_H + ROW_GAP) - ROW_GAP;
        int rw = side ? pw - leftW : pw;
        int listW = rw - 48;

        int drawerH = (drawerMode != DrawerMode.NONE && activeCandidate != null)
                ? 16 + layoutAndPaintDrawer(g, 0, 0, listW, true)
                : 0;

        // left panel height (chips wrap)
        int chipsH = layoutChips(g, px + 28, 0, leftW - 56, true);
        int leftH = 28 + 22 + 26 + 18 + chipsH + 26 + 96 + 28;
        int rightH = 28 + 22 + 22 + listH + drawerH + 28;
        int ph = side ? Math.max(leftH, rightH) : leftH + rightH;

        if (measuring) return cur + ph - top + 40;

        Graphics2D pg = Fx.fade(g, rv.p(0.25f, 0.9f), 40);
        Fx.glowOval(pg, width / 2f, cur + ph / 2f, pw * 0.55f, ph * 0.6f, Fx.VIOLET, Fx.dark() ? 0.18f : 0.1f);
        Fx.card(pg, px, cur, pw, ph, 26, 0.3f, Fx.VIOLET);

        // ---- left: requirements
        int lx = px + 28;
        int ly = cur + 28;
        pg.setFont(Fx.label(11));
        pg.setColor(Fx.text3());
        pg.drawString("PROJECT NEEDS", lx, ly + 10);
        ly += 22;
        pg.setFont(Fx.heading(20));
        pg.setColor(Fx.text());
        pg.drawString("Campus Connect", lx, ly + 18);
        ly += 26 + 18;
        layoutChips(pg, lx, ly, leftW - 56, false);
        ly += chipsH + 26;

        // summary box
        RoundRectangle2D.Float sum = Fx.rr(lx, ly, leftW - 56, 96, 14);
        pg.setColor(Fx.subtle());
        pg.fill(sum);
        pg.setColor(Fx.border());
        pg.draw(sum);
        pg.setFont(Fx.display(30));
        String n = String.valueOf(selected.size());
        Fx.gradientText(pg, n, lx + 18, ly + 44, Fx.BRAND, 0f);
        int nw = pg.getFontMetrics().stringWidth(n);
        pg.setFont(Fx.body(13.5f));
        pg.setColor(Fx.text2());
        pg.drawString(selected.size() == 1 ? "skill required" : "skills required", lx + 18 + nw + 10, ly + 40);
        pg.setFont(Fx.body(12.5f));
        pg.setColor(Fx.text3());
        Candidate best = candidates.get(0);
        pg.drawString("Best fit: " + best.name + " (" + Math.round(best.targetMatch * 100) + "%)", lx + 18, ly + 70);
        // reset link
        pg.setFont(Fx.bold(12.5f));
        String rs = "Reset";
        resetW = pg.getFontMetrics().stringWidth(rs) + 16;
        resetH = 26;
        resetX = lx + leftW - 56 - resetW - 10;
        resetY = ly + 12;
        pg.setColor(resetHover ? Fx.text() : Fx.ink(Fx.PINK));
        Fx.textCenter(pg, rs, resetX + resetW / 2f, resetY + resetH / 2f);

        // ---- right: ranked list
        int rx = side ? px + leftW : px;
        int ry = side ? cur : cur + leftH;
        if (side) {
            pg.setColor(Fx.border());
            pg.drawLine(rx, cur + 20, rx, cur + ph - 20);
        } else {
            pg.setColor(Fx.border());
            pg.drawLine(px + 20, ry, px + pw - 20, ry);
        }
        int listX = rx + 24;
        int hy = ry + 28;
        pg.setFont(Fx.label(11));
        pg.setColor(Fx.text3());
        pg.drawString("RANKED CANDIDATES", listX, hy + 10);
        String live = "LIVE";
        int lw = pg.getFontMetrics().stringWidth(live);
        float pz = (Fx.time() % 1.4f) / 1.4f;
        Fx.glow(pg, listX + listW - lw - 14, hy + 6, 6 + pz * 10, Fx.MINT, 0.7f * (1 - pz));
        pg.setColor(Fx.MINT);
        pg.fill(new Ellipse2D.Float(listX + listW - lw - 18, hy + 2, 8, 8));
        pg.setColor(Fx.ink(Fx.MINT));
        pg.drawString(live, listX + listW - lw, hy + 10);

        int listTop = hy + 22 + 22;
        // paint lower-ranked first so the leader slides over others
        for (int i = candidates.size() - 1; i >= 0; i--) {
            Candidate cd = candidates.get(i);
            int rowY = listTop + Math.round(cd.rowY.getValue());
            Graphics2D rg = Fx.fade(pg, rv.p(0.45f + i * 0.1f), 24);
            paintRow(rg, listX, rowY, listW, cd, i);
            rg.dispose();
        }

        if (drawerMode != DrawerMode.NONE && activeCandidate != null) {
            layoutAndPaintDrawer(pg, listX, listTop + listH + 16, listW, false);
        }
        pg.dispose();

        return cur + ph - top + 40;
    }

    /** Lays out (and optionally paints) the inline Profile / Message drawer. Returns used height. */
    private int layoutAndPaintDrawer(Graphics2D g, int dx, int dy, int dw, boolean measureOnly) {
        Candidate cd = activeCandidate;
        boolean compact = dw < 460;
        int pad = compact ? 16 : 20;
        int innerW = dw - pad * 2;

        // Calculate dynamic height first
        int y = dy + pad;
        int headerH = 40;
        y += headerH + 12;

        int contentH;
        if (drawerMode == DrawerMode.PROFILE) {
            int metaH = compact ? 52 : 28;
            int bioH = Fx.paragraphHeight(g, cd.bio, Fx.body(13f), innerW, 1.45f);
            int skillsH = 26;
            int footerH = 34;
            contentH = metaH + 10 + bioH + 12 + skillsH + 14 + footerH;
        } else {
            int threadH = cd.messages.isEmpty() ? 0 : Math.min(2, cd.messages.size()) * 26 + 8;
            int quickH = layoutQuickReplies(g, dx + pad, 0, innerW, true);
            int inputH = compact ? 84 : 38;
            int statusH = !sendStatus.isEmpty() ? 22 : 0;
            contentH = threadH + quickH + 12 + inputH + statusH;
        }

        int totalH = (y - dy) + contentH + pad;
        if (measureOnly) return totalH;

        RoundRectangle2D.Float box = Fx.rr(dx, dy, dw, totalH, 18);
        g.setColor(Fx.surface());
        g.fill(box);
        Fx.gradientBorder(g, box, 1.2f, 0.7f, Fx.time() * 0.5f);

        // Header row: avatar + name + role + tabs + close
        int cx = dx + pad;
        int cy = dy + pad;
        Fx.avatar(g, cx, cy + 2, 36, cd.initials, cd.c1, cd.c2, true);
        int tx = cx + 48;
        g.setFont(Fx.bold(15));
        g.setColor(Fx.text());
        g.drawString(cd.name, tx, cy + 17);
        g.setFont(Fx.body(12));
        g.setColor(Fx.text3());
        int pct = Math.round(cd.currentMatch * 100);
        String sub = compact ? (pct + "% match") : (cd.role + "  ·  ★ " + cd.rating + "  ·  " + pct + "% match");
        g.drawString(sub, tx, cy + 34);

        // Right-aligned controls: [Profile] [Message] [x]
        closeW = 28; closeH = 28;
        closeX = dx + dw - pad - closeW;
        closeY = cy + 6;
        RoundRectangle2D.Float closeBtn = Fx.rr(closeX, closeY, closeW, closeH, 14);
        g.setColor(closeHover ? Fx.surfaceHi() : Fx.subtle());
        g.fill(closeBtn);
        g.setColor(closeHover ? Fx.borderHi() : Fx.border());
        g.draw(closeBtn);
        g.setFont(Fx.bold(13));
        g.setColor(closeHover ? Fx.text() : Fx.text2());
        Fx.textCenter(g, "×", closeX + closeW / 2f, closeY + closeH / 2f);

        tabMsgW = compact ? 68 : 80; tabMsgH = 28;
        tabMsgX = closeX - 8 - tabMsgW;
        tabMsgY = cy + 6;
        paintPillBtn(g, tabMsgX, tabMsgY, tabMsgW, tabMsgH, "Message", drawerMode == DrawerMode.MESSAGE, tabMsgHover, true);

        tabProfW = compact ? 60 : 72; tabProfH = 28;
        tabProfX = tabMsgX - 6 - tabProfW;
        tabProfY = cy + 6;
        paintPillBtn(g, tabProfX, tabProfY, tabProfW, tabProfH, "Profile", drawerMode == DrawerMode.PROFILE, tabProfHover, false);

        int bodyY = cy + headerH + 10;
        g.setColor(Fx.border());
        g.drawLine(dx + pad, bodyY - 5, dx + dw - pad, bodyY - 5);

        if (drawerMode == DrawerMode.PROFILE) {
            // Metadata strip
            g.setFont(Fx.bold(12));
            g.setColor(Fx.ink(Fx.VIOLET));
            String m1 = cd.university;
            String m2 = cd.availability + "  ·  " + cd.projectsDone + " shipped projects";
            if (compact) {
                g.drawString(m1, cx, bodyY + 14);
                g.setFont(Fx.body(12));
                g.setColor(Fx.text2());
                g.drawString(m2, cx, bodyY + 34);
                bodyY += 52;
            } else {
                g.drawString(m1, cx, bodyY + 16);
                int m1w = g.getFontMetrics().stringWidth(m1);
                g.setFont(Fx.body(12));
                g.setColor(Fx.text2());
                g.drawString("  ·  " + m2, cx + m1w + 4, bodyY + 16);
                bodyY += 28;
            }

            // Bio paragraph
            bodyY = Fx.paragraph(g, cd.bio, Fx.body(13f), Fx.text2(), cx, bodyY + 4, innerW, 1.45f, false);
            bodyY += 10;

            // Full skill pills
            int sx = cx;
            g.setFont(Fx.bold(11.5f));
            FontMetrics fm = g.getFontMetrics();
            for (String s : cd.skills) {
                int sw = fm.stringWidth(s) + 18;
                if (sx + sw > dx + dw - pad) break;
                boolean hit = selected.contains(s);
                RoundRectangle2D.Float sp = Fx.rr(sx, bodyY, sw, 24, 12);
                g.setColor(hit ? Fx.a(Fx.MINT, Fx.dark() ? 40 : 32) : Fx.subtle());
                g.fill(sp);
                g.setColor(hit ? Fx.a(Fx.MINT, 120) : Fx.border());
                g.draw(sp);
                g.setColor(hit ? Fx.ink(Fx.MINT) : Fx.text2());
                Fx.textCenter(g, s, sx + sw / 2f, bodyY + 12f);
                sx += sw + 6;
            }
            bodyY += 36;

            // Profile CTA: Send Message + recent sent count
            drawerBtnW = 144; drawerBtnH = 32;
            drawerBtnX = cx;
            drawerBtnY = bodyY;
            paintPillBtn(g, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH, "Send Message ->", true, drawerBtnHover, true);

            if (!cd.messages.isEmpty()) {
                g.setFont(Fx.body(12));
                g.setColor(Fx.ink(Fx.MINT));
                g.drawString(cd.messages.size() + (cd.messages.size() == 1 ? " message sent ✓" : " messages sent ✓"),
                        drawerBtnX + drawerBtnW + 14, drawerBtnY + 20);
            }
        } else {
            // MESSAGE MODE
            if (!cd.messages.isEmpty()) {
                int start = Math.max(0, cd.messages.size() - 2);
                for (int i = start; i < cd.messages.size(); i++) {
                    String m = "You: " + cd.messages.get(i);
                    RoundRectangle2D.Float mb = Fx.rr(cx, bodyY, innerW, 22, 8);
                    g.setColor(Fx.a(Fx.MINT, Fx.dark() ? 24 : 18));
                    g.fill(mb);
                    g.setFont(Fx.body(11.5f));
                    g.setColor(Fx.text2());
                    Fx.textMid(g, m, cx + 10, bodyY + 11f);
                    bodyY += 26;
                }
                bodyY += 8;
            }

            int quickUsed = layoutQuickReplies(g, cx, bodyY, innerW, false);
            bodyY += quickUsed + 12;

            int sendBtnW = 84;
            int sendBtnH = 38;
            if (compact) {
                msgBox.setBounds(cx, bodyY, innerW, sendBtnH);
                msgBox.paint(g);
                drawerBtnX = cx;
                drawerBtnY = bodyY + sendBtnH + 8;
                drawerBtnW = innerW;
                drawerBtnH = sendBtnH;
                paintPillBtn(g, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH, "Send Msg", true, drawerBtnHover, true);
                bodyY += sendBtnH * 2 + 12;
            } else {
                msgBox.setBounds(cx, bodyY, innerW - sendBtnW - 10, sendBtnH);
                msgBox.paint(g);
                drawerBtnX = cx + innerW - sendBtnW;
                drawerBtnY = bodyY;
                drawerBtnW = sendBtnW;
                drawerBtnH = sendBtnH;
                paintPillBtn(g, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH, "Send Msg", true, drawerBtnHover, true);
                bodyY += sendBtnH + 6;
            }

            if (!sendStatus.isEmpty()) {
                g.setFont(Fx.bold(12));
                g.setColor(msgBox.hasError ? Fx.ink(Fx.ORANGE) : Fx.ink(Fx.MINT));
                g.drawString(sendStatus, cx + 2, bodyY + 14);
            }
        }
        return totalH;
    }

    private int layoutQuickReplies(Graphics2D g, int x, int y, int maxW, boolean measureOnly) {
        g.setFont(Fx.bold(11.5f));
        FontMetrics fm = g.getFontMetrics();
        int cx = x, cy = y, h = 26, gap = 8;
        for (int i = 0; i < quickReplies.length; i++) {
            String label = quickReplies[i];
            int w = fm.stringWidth(label) + 20;
            if (cx + w > x + maxW && cx > x) { cx = x; cy += h + gap; }
            if (!measureOnly) {
                quickBounds[i][0] = cx; quickBounds[i][1] = cy;
                quickBounds[i][2] = w;  quickBounds[i][3] = h;
                boolean hov = quickHoverIdx == i;
                RoundRectangle2D.Float chip = Fx.rr(cx, cy, w, h, h / 2f);
                g.setColor(hov ? Fx.surfaceHi() : Fx.subtle());
                g.fill(chip);
                g.setColor(hov ? Fx.PINK : Fx.border());
                g.draw(chip);
                g.setColor(hov ? Fx.text() : Fx.text2());
                Fx.textCenter(g, label, cx + w / 2f, cy + h / 2f);
            }
            cx += w + gap;
        }
        return cy - y + h;
    }

    private void paintPillBtn(Graphics2D g, int x, int y, int w, int h, String label, boolean active, boolean hovered, boolean primary) {
        RoundRectangle2D.Float r = Fx.rr(x, y, w, h, h / 2f);
        if (primary) {
            if (active || hovered) {
                g.setPaint(new GradientPaint(x, y, hovered ? Fx.PINK : Fx.a(Fx.PINK, 225),
                        x + w, y + h, hovered ? Fx.VIOLET : Fx.a(Fx.VIOLET, 225)));
                g.fill(r);
                g.setColor(Fx.a(Color.WHITE, 70));
                g.draw(r);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Fx.a(Fx.PINK, Fx.dark() ? 32 : 24));
                g.fill(r);
                g.setColor(Fx.a(Fx.PINK, 130));
                g.draw(r);
                g.setColor(Fx.ink(Fx.PINK));
            }
        } else {
            if (active) {
                g.setColor(Fx.a(Fx.VIOLET, Fx.dark() ? 45 : 28));
                g.fill(r);
                g.setColor(Fx.VIOLET);
            } else {
                g.setColor(hovered ? Fx.surfaceHi() : Fx.subtle());
                g.fill(r);
                g.setColor(hovered ? Fx.borderHi() : Fx.border());
            }
            g.draw(r);
            g.setColor(active || hovered ? Fx.text() : Fx.text2());
        }
        g.setFont(Fx.bold(11.5f));
        Fx.textCenter(g, label, x + w / 2f, y + h / 2f);
    }

    /** Lays out (and optionally paints) the chips. Returns used height. */
    private int layoutChips(Graphics2D g, int x, int y, int maxW, boolean measureOnly) {
        g.setFont(Fx.bold(13));
        FontMetrics fm = g.getFontMetrics();
        int cx = x, cy = y, h = 36, gap = 8;
        for (SkillChip c : chips) {
            int w = fm.stringWidth(c.label) + 44;
            if (cx + w > x + maxW) { cx = x; cy += h + gap; }
            if (!measureOnly) {
                c.x = cx; c.y = cy; c.w = w; c.h = h;
                paintChip(g, c);
            }
            cx += w + gap;
        }
        return cy - y + h;
    }

    private void paintChip(Graphics2D g, SkillChip c) {
        float s = c.sel.getValue();
        RoundRectangle2D.Float shape = Fx.rr(c.x, c.y, c.w, c.h, c.h / 2f);
        if (s > 0.01f) Fx.glow(g, c.x + c.w / 2f, c.y + c.h / 2f, c.w * 0.7f, Fx.PINK, 0.22f * s);
        g.setColor(c.hovered ? Fx.surfaceHi() : Fx.subtle());
        g.fill(shape);
        if (s > 0.01f) {
            Graphics2D f = (Graphics2D) g.create();
            Fx.fadeBy(f, s);
            f.setPaint(new GradientPaint(c.x, c.y, Fx.PINK, c.x + c.w, c.y + c.h, Fx.VIOLET));
            f.fill(shape);
            f.dispose();
        }
        g.setColor(s > 0.5f ? Fx.a(Color.WHITE, 60) : (c.hovered ? Fx.borderHi() : Fx.border()));
        g.draw(shape);
        // check / plus glyph
        float gx = c.x + 14, gy = c.y + c.h / 2f;
        Graphics2D k = (Graphics2D) g.create();
        k.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        k.setColor(s > 0.5f ? Color.WHITE : Fx.text3());
        if (s > 0.5f) {
            k.drawLine(Math.round(gx - 4), Math.round(gy), Math.round(gx - 1), Math.round(gy + 3));
            k.drawLine(Math.round(gx - 1), Math.round(gy + 3), Math.round(gx + 5), Math.round(gy - 4));
        } else {
            k.drawLine(Math.round(gx - 4), Math.round(gy), Math.round(gx + 4), Math.round(gy));
            k.drawLine(Math.round(gx), Math.round(gy - 4), Math.round(gx), Math.round(gy + 4));
        }
        k.dispose();
        g.setFont(Fx.bold(13));
        g.setColor(s > 0.5f ? Color.WHITE : (c.hovered ? Fx.text() : Fx.text2()));
        Fx.textMid(g, c.label, c.x + 28, c.y + c.h / 2f);
    }

    private void paintRow(Graphics2D g, int x, int y, int w, Candidate c, int rank) {
        boolean topMatch = rank == 0;
        boolean selectedRow = activeCandidate == c && drawerMode != DrawerMode.NONE;
        RoundRectangle2D.Float row = Fx.rr(x, y, w, ROW_H, 16);
        if (topMatch) {
            Fx.glow(g, x + w * 0.3f, y + ROW_H / 2f, w * 0.4f, Fx.PINK, 0.12f + 0.2f * pulse);
            g.setPaint(new GradientPaint(x, y, Fx.a(Fx.PINK, Fx.dark() ? 34 : 22), x + w, y, Fx.a(Fx.VIOLET, 8)));
            g.fill(row);
            Fx.gradientBorder(g, row, selectedRow ? 1.8f : 1.3f, 0.9f, Fx.time() * 0.6f);
        } else {
            g.setColor(selectedRow ? Fx.surface() : Fx.subtle());
            g.fill(row);
            g.setColor(selectedRow ? Fx.VIOLET : Fx.border());
            g.draw(row);
        }

        boolean compact = w < 520;
        int ix = x + 18;
        // rank + avatar
        g.setFont(Fx.bold(13));
        g.setColor(topMatch ? Fx.ink(Fx.PINK) : Fx.text3());
        Fx.textCenter(g, "#" + (rank + 1), ix + 8, compact ? y + 32f : y + ROW_H / 2f);
        ix += compact ? 26 : 30;
        int avSize = compact ? 38 : 44;
        int avY = compact ? y + 13 : y + (ROW_H - avSize) / 2;
        Fx.avatar(g, ix, avY, avSize, c.initials, c.c1, c.c2, topMatch);
        ix += avSize + (compact ? 12 : 14);

        // Name + optional TOP MATCH badge
        g.setFont(Fx.bold(compact ? 14.5f : 15.5f));
        g.setColor(Fx.text());
        g.drawString(c.name, ix, y + 29);
        int nw = g.getFontMetrics().stringWidth(c.name);
        if (topMatch && !compact) Fx.tag(g, "TOP MATCH", ix + nw + 10, y + 12, Fx.PINK, true);

        // Match % + optional progress bar at top-right
        int pct = Math.round(c.currentMatch * 100);
        g.setFont(Fx.display(compact ? 18 : 21));
        String ps = pct + "%";
        int psw = g.getFontMetrics().stringWidth(ps);
        float pxp = x + w - 18 - psw;
        float matchBaseY = y + 32f;
        if (pct >= 80) Fx.gradientText(g, ps, pxp, matchBaseY, Fx.BRAND, 0f);
        else { g.setColor(Fx.text()); g.drawString(ps, pxp, matchBaseY); }
        int barArea = compact ? 64 : Math.min(220, w / 3);
        if (!compact) {
            int bw = barArea - psw - 30;
            float bx = pxp - 14 - bw, by = y + 22f;
            g.setColor(Fx.border());
            g.fill(Fx.rr(bx, by, bw, 6, 3));
            float fw = bw * c.currentMatch;
            g.setPaint(new GradientPaint(bx, 0, c.c1, bx + bw, 0, c.c2));
            g.fill(Fx.rr(bx, by, fw, 6, 3));
            Fx.glow(g, bx + fw, by + 3, 12, c.c2, 0.7f);
        }

        // Action buttons: View Profile & Send Msg
        boolean profActive = activeCandidate == c && drawerMode == DrawerMode.PROFILE;
        boolean msgActive = activeCandidate == c && drawerMode == DrawerMode.MESSAGE;
        if (compact) {
            c.profW = 92; c.profH = 26;
            c.msgW = 84; c.msgH = 26;
            c.profX = x + 18;
            c.profY = y + 66;
            c.msgX = c.profX + c.profW + 8;
            c.msgY = y + 66;
        } else {
            c.msgW = 86; c.msgH = 26;
            c.profW = 96; c.profH = 26;
            c.msgX = x + w - 18 - c.msgW;
            c.msgY = y + 62;
            c.profX = c.msgX - 8 - c.profW;
            c.profY = y + 62;

            // Subtle role line under name
            g.setFont(Fx.body(11.5f));
            g.setColor(Fx.text3());
            g.drawString(c.role + "  ·  ★ " + c.rating, ix, y + 46);
        }

        // Skills (matched highlighted) positioned so they never overlap buttons
        float sx = ix;
        int skillY = compact ? y + 38 : y + 64;
        int skillH = compact ? 20 : 22;
        int skillMaxX = compact ? (x + w - 18) : (c.profX - 12);
        g.setFont(Fx.font(Font.PLAIN, 11.5f));
        FontMetrics fm = g.getFontMetrics();
        for (String s : c.skills) {
            int sw = fm.stringWidth(s) + 16;
            if (sx + sw > skillMaxX) break;
            boolean hit = selected.contains(s);
            RoundRectangle2D.Float p = Fx.rr(sx, skillY, sw, skillH, skillH / 2f);
            g.setColor(hit ? Fx.a(Fx.MINT, Fx.dark() ? 40 : 34) : Fx.subtle());
            g.fill(p);
            if (hit) { g.setColor(Fx.a(Fx.MINT, 110)); g.draw(p); }
            g.setColor(hit ? Fx.ink(Fx.MINT) : Fx.text3());
            Fx.textMid(g, s, sx + 8, skillY + skillH / 2f);
            sx += sw + 6;
        }

        paintPillBtn(g, c.profX, c.profY, c.profW, c.profH, "View Profile", profActive, c.profHover, false);
        paintPillBtn(g, c.msgX, c.msgY, c.msgW, c.msgH, "Send Msg", msgActive, c.msgHover, true);
    }

    @Override
    public boolean onMouseDown(int mx, int my) {
        if (drawerMode == DrawerMode.MESSAGE && activeCandidate != null) {
            return msgBox.onMouseDown(mx, my);
        }
        return false;
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (SkillChip c : chips) {
            if (Fx.inside(mx, my, c.x, c.y, c.w, c.h)) { toggle(c); return true; }
        }
        if (Fx.inside(mx, my, resetX, resetY, resetW, resetH)) {
            selected.clear();
            selected.add("Java");
            selected.add("MySQL");
            for (SkillChip c : chips) c.on = selected.contains(c.label);
            recompute(true);
            return true;
        }
        for (Candidate cd : candidates) {
            if (Fx.inside(mx, my, cd.profX, cd.profY, cd.profW, cd.profH)) {
                openDrawer(cd, DrawerMode.PROFILE);
                return true;
            }
            if (Fx.inside(mx, my, cd.msgX, cd.msgY, cd.msgW, cd.msgH)) {
                openDrawer(cd, DrawerMode.MESSAGE);
                return true;
            }
        }
        if (drawerMode != DrawerMode.NONE && activeCandidate != null) {
            if (Fx.inside(mx, my, closeX, closeY, closeW, closeH)) {
                closeDrawer();
                return true;
            }
            if (Fx.inside(mx, my, tabProfX, tabProfY, tabProfW, tabProfH)) {
                if (drawerMode != DrawerMode.PROFILE) openDrawer(activeCandidate, DrawerMode.PROFILE);
                return true;
            }
            if (Fx.inside(mx, my, tabMsgX, tabMsgY, tabMsgW, tabMsgH)) {
                if (drawerMode != DrawerMode.MESSAGE) openDrawer(activeCandidate, DrawerMode.MESSAGE);
                return true;
            }
            if (drawerMode == DrawerMode.PROFILE) {
                if (Fx.inside(mx, my, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH)) {
                    openDrawer(activeCandidate, DrawerMode.MESSAGE);
                    return true;
                }
            } else {
                for (int i = 0; i < quickReplies.length; i++) {
                    int[] qb = quickBounds[i];
                    if (Fx.inside(mx, my, qb[0], qb[1], qb[2], qb[3])) {
                        msgBox.setText(quickReplies[i]);
                        msgBox.focused = true;
                        msgBox.hasError = false;
                        sendStatus = "";
                        return true;
                    }
                }
                if (msgBox.contains(mx, my)) {
                    msgBox.focused = true;
                    msgBox.hasError = false;
                    return true;
                }
                if (Fx.inside(mx, my, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH)) {
                    sendCurrentMessage();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;
        for (SkillChip c : chips) {
            c.hovered = Fx.inside(mx, my, c.x, c.y, c.w, c.h);
            hand |= c.hovered;
        }
        resetHover = Fx.inside(mx, my, resetX, resetY, resetW, resetH);
        hand |= resetHover;
        for (Candidate cd : candidates) {
            cd.profHover = Fx.inside(mx, my, cd.profX, cd.profY, cd.profW, cd.profH);
            cd.msgHover = Fx.inside(mx, my, cd.msgX, cd.msgY, cd.msgW, cd.msgH);
            hand |= cd.profHover || cd.msgHover;
        }
        quickHoverIdx = -1;
        if (drawerMode != DrawerMode.NONE && activeCandidate != null) {
            closeHover = Fx.inside(mx, my, closeX, closeY, closeW, closeH);
            tabProfHover = Fx.inside(mx, my, tabProfX, tabProfY, tabProfW, tabProfH);
            tabMsgHover = Fx.inside(mx, my, tabMsgX, tabMsgY, tabMsgW, tabMsgH);
            drawerBtnHover = Fx.inside(mx, my, drawerBtnX, drawerBtnY, drawerBtnW, drawerBtnH);
            hand |= closeHover || tabProfHover || tabMsgHover || drawerBtnHover;
            if (drawerMode == DrawerMode.MESSAGE) {
                for (int i = 0; i < quickReplies.length; i++) {
                    int[] qb = quickBounds[i];
                    if (Fx.inside(mx, my, qb[0], qb[1], qb[2], qb[3])) {
                        quickHoverIdx = i;
                        hand = true;
                    }
                }
            }
        } else {
            closeHover = tabProfHover = tabMsgHover = drawerBtnHover = false;
        }
        return hand;
    }

    @Override
    public boolean onKey(KeyEvent e) {
        if (drawerMode != DrawerMode.MESSAGE || activeCandidate == null || !msgBox.focused) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                sendCurrentMessage();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                closeDrawer();
                return true;
            }
            if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(e.getKeyChar()) || e.isControlDown()) {
                return msgBox.onKey(e);
            }
            return true;
        }
        if (e.getID() == KeyEvent.KEY_TYPED) {
            return msgBox.onKey(e);
        }
        return false;
    }
}
