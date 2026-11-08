import java.awt.*;
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
        public float currentMatch = 0.85f, targetMatch = 0.85f;
        public final Elastic rowY = new Elastic(0f, 160f, 17f);

        public Candidate(String name, String role, String initials, Color c1, Color c2, double rating, String... sk) {
            this.name = name; this.role = role; this.initials = initials; this.c1 = c1; this.c2 = c2; this.rating = rating;
            Collections.addAll(skills, sk);
        }
    }

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
    private static final int ROW_H = 84, ROW_GAP = 10;

    private int resetX, resetY, resetW, resetH;
    private boolean resetHover;
    private float pulse = 0f; // flash when ranking changes

    public MatchDemo() {
        candidates.add(new Candidate("Sarah Lin", "Backend & Systems", "SL", Fx.ORANGE, Fx.PINK, 4.9, "Java", "MySQL", "Testing", "Cloud"));
        candidates.add(new Candidate("David Chen", "Product Designer", "DC", Fx.VIOLET, new Color(0x6E, 0xC1, 0xFF), 5.0, "Design", "React"));
        candidates.add(new Candidate("Priya Patel", "Full-Stack Engineer", "PP", Fx.MINT, Fx.CYAN, 4.8, "Java", "Python", "Cloud", "Testing"));
        candidates.add(new Candidate("Marcus Vance", "Frontend & Mobile", "MV", Fx.AMBER, Fx.ORANGE, 4.7, "Design", "React", "Java"));

        String[] all = {"Java", "Design", "MySQL", "Testing", "React", "Python", "Cloud"};
        selected.add("Java");
        selected.add("MySQL");
        for (String s : all) chips.add(new SkillChip(s, selected.contains(s)));
        recompute(false);
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

        // left panel height (chips wrap)
        int chipsH = layoutChips(g, px + 28, 0, leftW - 56, true);
        int leftH = 28 + 22 + 26 + 18 + chipsH + 26 + 96 + 28;
        int rightH = 28 + 22 + 22 + listH + 28;
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
        int rw = side ? pw - leftW : pw;
        if (side) {
            pg.setColor(Fx.border());
            pg.drawLine(rx, cur + 20, rx, cur + ph - 20);
        } else {
            pg.setColor(Fx.border());
            pg.drawLine(px + 20, ry, px + pw - 20, ry);
        }
        int listX = rx + 24;
        int listW = rw - 48;
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
        pg.dispose();

        return cur + ph - top + 40;
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
        RoundRectangle2D.Float row = Fx.rr(x, y, w, ROW_H, 16);
        if (topMatch) {
            Fx.glow(g, x + w * 0.3f, y + ROW_H / 2f, w * 0.4f, Fx.PINK, 0.12f + 0.2f * pulse);
            g.setPaint(new GradientPaint(x, y, Fx.a(Fx.PINK, Fx.dark() ? 34 : 22), x + w, y, Fx.a(Fx.VIOLET, 8)));
            g.fill(row);
            Fx.gradientBorder(g, row, 1.3f, 0.9f, Fx.time() * 0.6f);
        } else {
            g.setColor(Fx.subtle());
            g.fill(row);
            g.setColor(Fx.border());
            g.draw(row);
        }

        boolean compact = w < 520;
        int ix = x + 18;
        // rank
        g.setFont(Fx.bold(13));
        g.setColor(topMatch ? Fx.ink(Fx.PINK) : Fx.text3());
        Fx.textCenter(g, "#" + (rank + 1), ix + 8, y + ROW_H / 2f);
        ix += 30;
        Fx.avatar(g, ix, y + ROW_H / 2f - 22, 44, c.initials, c.c1, c.c2, topMatch);
        ix += 58;

        g.setFont(Fx.bold(15.5f));
        g.setColor(Fx.text());
        g.drawString(c.name, ix, y + 31);
        int nw = g.getFontMetrics().stringWidth(c.name);
        if (topMatch && !compact) Fx.tag(g, "TOP MATCH", ix + nw + 10, y + 14, Fx.PINK, true);

        // skills (matched highlighted)
        float sx = ix;
        g.setFont(Fx.font(Font.PLAIN, 11.5f));
        FontMetrics fm = g.getFontMetrics();
        int barArea = compact ? 70 : Math.min(230, w / 3);
        for (String s : c.skills) {
            int sw = fm.stringWidth(s) + 16;
            if (sx + sw > x + w - barArea - 20) break;
            boolean hit = selected.contains(s);
            RoundRectangle2D.Float p = Fx.rr(sx, y + 44, sw, 22, 11);
            g.setColor(hit ? Fx.a(Fx.MINT, Fx.dark() ? 40 : 34) : Fx.subtle());
            g.fill(p);
            if (hit) { g.setColor(Fx.a(Fx.MINT, 110)); g.draw(p); }
            g.setColor(hit ? Fx.ink(Fx.MINT) : Fx.text3());
            Fx.textMid(g, s, sx + 8, y + 55);
            sx += sw + 6;
        }

        // match bar + pct
        int pct = Math.round(c.currentMatch * 100);
        g.setFont(Fx.display(22));
        String ps = pct + "%";
        int psw = g.getFontMetrics().stringWidth(ps);
        float pxp = x + w - 18 - psw;
        if (pct >= 80) Fx.gradientText(g, ps, pxp, y + ROW_H / 2f + 8, Fx.BRAND, 0f);
        else { g.setColor(Fx.text()); g.drawString(ps, pxp, y + ROW_H / 2f + 8); }
        if (!compact) {
            int bw = barArea - psw - 34;
            float bx = pxp - 16 - bw, by = y + ROW_H / 2f - 3;
            g.setColor(Fx.border());
            g.fill(Fx.rr(bx, by, bw, 6, 3));
            float fw = bw * c.currentMatch;
            g.setPaint(new GradientPaint(bx, 0, c.c1, bx + bw, 0, c.c2));
            g.fill(Fx.rr(bx, by, fw, 6, 3));
            Fx.glow(g, bx + fw, by + 3, 12, c.c2, 0.7f);
        }
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
        return hand || resetHover;
    }
}
