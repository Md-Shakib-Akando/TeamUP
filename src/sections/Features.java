import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/** Bento grid of capabilities — every tile has its own live micro-animation. */
public class Features extends LpSection {

    private static class Tile {
        final String title, desc;
        final int type;
        final Color accent;
        int x, y, w, h;
        boolean hovered;
        final Elastic hov = new Elastic(0f, 220f, 20f);
        Tile(String t, String d, int type, Color a) { title = t; desc = d; this.type = type; accent = a; }
    }

    private final List<Tile> tiles = new ArrayList<>();
    private static final int VIS_H = 168;

    public Features() {
        tiles.add(new Tile("Skill-based matching", "Compatibility scores computed from complementary skills, roles and availability - not guesswork.", 0, Fx.PINK));
        tiles.add(new Tile("Smart search & filters", "Filter by stack, category and availability with instant results.", 1, Fx.VIOLET));
        tiles.add(new Tile("Projects & Kanban", "Milestones and tasks that move with your team.", 2, Fx.CYAN));
        tiles.add(new Tile("Team messaging", "Chat and alerts without switching apps.", 3, Fx.MINT));
        tiles.add(new Tile("Ratings & reviews", "Verified peer reviews build real reputation.", 4, Fx.AMBER));
        tiles.add(new Tile("Admin control panel", "Monitor platform health, roles and moderation in real time.", 5, Fx.INDIGO));
        tiles.add(new Tile("Secure by default", "Salted PBKDF2 password hashing and role-based access control keep every account safe.", 6, new Color(0x6E, 0xC1, 0xFF)));
    }

    @Override public String getId() { return "features"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (Tile t : tiles) { t.hov.setTarget(t.hovered ? 1f : 0f); t.hov.update(dt); }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "FEATURES", Fx.CYAN,
                "Everything you need to ", "ship together.",
                "Matching, project management, messaging and reputation - unified in one fast, focused workspace.");
        cur += width < 700 ? 36 : 56;

        int[] c = Fx.container(width);
        int gap = 18;
        int[][] rows;      // tile indices per row
        float[][] spans;   // relative widths per row
        if (width >= 1100) {
            rows = new int[][]{{0, 1}, {2, 3, 4}, {5, 6}};
            spans = new float[][]{{7, 5}, {1, 1, 1}, {5, 7}};
        } else if (width >= 700) {
            rows = new int[][]{{0}, {1, 2}, {3, 4}, {5, 6}};
            spans = new float[][]{{1}, {1, 1}, {1, 1}, {1, 1}};
        } else {
            rows = new int[][]{{0}, {1}, {2}, {3}, {4}, {5}, {6}};
            spans = new float[][]{{1}, {1}, {1}, {1}, {1}, {1}, {1}};
        }

        int order = 0;
        for (int r = 0; r < rows.length; r++) {
            float sum = 0;
            for (float s : spans[r]) sum += s;
            int avail = c[1] - gap * (rows[r].length - 1);
            int rowH = 0;
            int[] ws = new int[rows[r].length];
            for (int k = 0; k < rows[r].length; k++) {
                ws[k] = k == rows[r].length - 1 ? avail - sumOf(ws, k) : Math.round(avail * spans[r][k] / sum);
                rowH = Math.max(rowH, tileH(g, tiles.get(rows[r][k]), ws[k]));
            }
            int x = c[0];
            for (int k = 0; k < rows[r].length; k++) {
                Tile t = tiles.get(rows[r][k]);
                t.x = x; t.y = cur; t.w = ws[k]; t.h = rowH;
                x += ws[k] + gap;
                if (measuring) continue;
                Graphics2D tg = Fx.fade(g, rv.p(0.2f + order * 0.08f), 36);
                paintTile(tg, t);
                tg.dispose();
                order++;
            }
            cur += rowH + gap;
        }
        cur -= gap;
        return cur - top + 40;
    }

    private static int sumOf(int[] a, int n) { int s = 0; for (int i = 0; i < n; i++) s += a[i]; return s; }

    private int tileH(Graphics2D g, Tile t, int w) {
        return 22 + VIS_H + 22 + 28 + Fx.paragraphHeight(g, t.desc, Fx.body(14.5f), w - 52, 1.5f) + 26;
    }

    private void paintTile(Graphics2D g, Tile t) {
        float hv = t.hov.getValue();
        float y = t.y - hv * 6f;
        Fx.card(g, t.x, y, t.w, t.h, 24, hv, t.accent);

        // visual stage
        float vx = t.x + 14, vy = y + 14, vw = t.w - 28, vh = VIS_H + 8;
        RoundRectangle2D.Float stage = Fx.rr(vx, vy, vw, vh, 16);
        Graphics2D s = (Graphics2D) g.create();
        s.clip(new java.awt.geom.Rectangle2D.Float(vx + 4, vy + 4, vw - 8, vh - 8));
        s.setPaint(new GradientPaint(0, vy, Fx.a(t.accent, Fx.dark() ? 26 : 22), 0, vy + vh, Fx.a(t.accent, 0)));
        s.fill(stage);
        Fx.glow(s, vx + vw / 2f, vy + vh * 0.55f, Math.min(vw, 360) * 0.6f, t.accent, 0.10f + 0.12f * hv);
        paintVisual(s, t, vx, vy, vw, vh, hv);
        s.dispose();
        g.setColor(Fx.border());
        g.draw(stage);

        int px = t.x + 26;
        float ty = y + 22 + VIS_H + 22;
        g.setFont(Fx.heading(20));
        g.setColor(Fx.text());
        g.drawString(t.title, px, ty + 18);
        Fx.paragraph(g, t.desc, Fx.body(14.5f), Fx.text2(), px, Math.round(ty + 28), t.w - 52, 1.5f, false);
    }

    // ------------------------------------------------------------------ visuals

    private void paintVisual(Graphics2D g, Tile t, float x, float y, float w, float h, float hv) {
        float time = Fx.time();
        float cx = x + w / 2f, cy = y + h / 2f;
        switch (t.type) {
            case 0: visMatch(g, x, y, w, h, time); break;
            case 1: visSearch(g, x, y, w, h, time); break;
            case 2: visKanban(g, x, y, w, h, time); break;
            case 3: visChat(g, x, y, w, h, time); break;
            case 4: visRating(g, cx, cy, time); break;
            case 5: visAdmin(g, x, y, w, h, time); break;
            default: visSecure(g, x, y, w, h, time);
        }
    }

    private void visMatch(Graphics2D g, float x, float y, float w, float h, float time) {
        String[] skills = {"Java", "React", "Figma", "SQL"};
        Color[] cols = {Fx.ORANGE, Fx.CYAN, Fx.PINK, Fx.VIOLET};
        float leftX = x + Math.max(20, w * 0.08f);
        float ringR = Math.min(52, h * 0.32f);
        float rcx = x + w - Math.max(70, w * 0.2f), rcy = y + h / 2f;
        float rowH = (h - 30) / skills.length;
        g.setFont(Fx.bold(13));
        for (int i = 0; i < skills.length; i++) {
            float sy = y + 15 + i * rowH + rowH / 2f;
            int tw = g.getFontMetrics().stringWidth(skills[i]) + 36;
            // flowing connector
            Path2D.Float p = new Path2D.Float();
            p.moveTo(leftX + tw, sy);
            p.curveTo(leftX + tw + (rcx - leftX - tw) * 0.5f, sy, rcx - ringR - 60, rcy, rcx - ringR - 6, rcy);
            g.setColor(Fx.a(cols[i], 60));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(p);
            g.setColor(Fx.a(cols[i], 220));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 14f}, 18f - ((time * 40f + i * 6f) % 18f)));
            g.draw(p);
            // chip
            g.setStroke(new BasicStroke(1f));
            g.setColor(Fx.dark() ? new Color(0x16, 0x16, 0x2C) : Color.WHITE);
            g.fill(Fx.rr(leftX, sy - 14, tw, 28, 14));
            g.setColor(Fx.a(cols[i], 120));
            g.draw(Fx.rr(leftX, sy - 14, tw, 28, 14));
            g.setColor(cols[i]);
            g.fill(new Ellipse2D.Float(leftX + 11, sy - 3, 6, 6));
            g.setColor(Fx.text());
            Fx.textMid(g, skills[i], leftX + 22, sy);
        }
        // ring
        float v = 0.6f + 0.34f * (0.5f + 0.5f * (float) Math.sin(time * 0.9f));
        Fx.glow(g, rcx, rcy, ringR * 2.2f, Fx.PINK, 0.35f);
        g.setColor(Fx.dark() ? new Color(0x12, 0x12, 0x26) : Color.WHITE);
        g.fill(new Ellipse2D.Float(rcx - ringR, rcy - ringR, ringR * 2, ringR * 2));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Fx.subtle());
        g.draw(new Ellipse2D.Float(rcx - ringR + 6, rcy - ringR + 6, ringR * 2 - 12, ringR * 2 - 12));
        g.setPaint(new GradientPaint(rcx - ringR, rcy - ringR, Fx.PINK, rcx + ringR, rcy + ringR, Fx.VIOLET));
        g.draw(new Arc2D.Float(rcx - ringR + 6, rcy - ringR + 6, ringR * 2 - 12, ringR * 2 - 12, 90, -360 * v, Arc2D.OPEN));
        g.setFont(Fx.display(22));
        g.setColor(Fx.text());
        Fx.textCenter(g, Math.round(v * 100) + "%", rcx, rcy - 4);
        g.setFont(Fx.body(10.5f));
        g.setColor(Fx.text3());
        Fx.textCenter(g, "match", rcx, rcy + 16);
    }

    private void visSearch(Graphics2D g, float x, float y, float w, float h, float time) {
        String[] queries = {"Java backend", "UI/UX designer", "ML engineer"};
        float cyc = Theme.motionEnabled ? time % 9f : 2.5f;
        int qi = (int) (cyc / 3f);
        float lt = cyc - qi * 3f;
        String q = queries[qi];
        int n = Math.min(q.length(), (int) (lt / 0.08f));
        if (lt > 2.4f) n = Math.max(0, q.length() - (int) ((lt - 2.4f) / 0.03f));
        float fx = x + 18, fw = w - 36, fy = y + 16;
        g.setColor(Fx.dark() ? new Color(0x14, 0x14, 0x2A) : Color.WHITE);
        g.fill(Fx.rr(fx, fy, fw, 36, 18));
        g.setColor(Fx.a(Fx.VIOLET, 120));
        g.draw(Fx.rr(fx, fy, fw, 36, 18));
        Draw.drawSearchIcon(g, Math.round(fx + 11), Math.round(fy + 8), 20, Fx.text3());
        g.setFont(Fx.body(14));
        g.setColor(Fx.text());
        String shown = q.substring(0, n);
        Fx.textMid(g, shown, fx + 38, fy + 18);
        if (((int) (time * 2.5f)) % 2 == 0) {
            int cw = g.getFontMetrics().stringWidth(shown);
            g.setColor(Fx.VIOLET);
            g.fill(new Rectangle2D.Float(fx + 39 + cw, fy + 9, 2, 18));
        }
        String[] f = {"Remote", "Full-stack", "Available", "Senior", "Design"};
        float px = fx, py = fy + 46;
        int hi = Theme.motionEnabled ? ((int) (time * 1.2f)) % f.length : 1;
        g.setFont(Fx.bold(12));
        for (int i = 0; i < f.length; i++) {
            int pw = g.getFontMetrics().stringWidth(f[i]) + 24;
            if (px + pw > fx + fw) { px = fx; py += 36; }
            if (py + 28 > y + h - 6) break;
            boolean on = i == hi || i == (hi + 2) % f.length;
            RoundRectangle2D.Float pill = Fx.rr(px, py, pw, 28, 14);
            if (on) {
                g.setPaint(new GradientPaint(px, py, Fx.VIOLET, px + pw, py, Fx.PINK));
                g.fill(pill);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Fx.subtle());
                g.fill(pill);
                g.setColor(Fx.border());
                g.draw(pill);
                g.setColor(Fx.text2());
            }
            Fx.textCenter(g, f[i], px + pw / 2f, py + 14);
            px += pw + 8;
        }
        // result rows
        float ry = py + 38;
        String[][] res = {{"SL", "Sarah Lin", "96%"}, {"PP", "Priya Patel", "91%"}};
        Color[][] rc = {{Fx.ORANGE, Fx.PINK}, {Fx.MINT, Fx.CYAN}};
        for (int i = 0; i < res.length; i++) {
            float rowY = ry + i * 36;
            if (rowY + 30 > y + h - 2) break;
            float appear = Theme.motionEnabled ? Fx.easeOutQuart(Fx.clamp((lt - 0.9f - i * 0.15f) / 0.4f)) * (lt > 2.4f ? Fx.clamp(1f - (lt - 2.4f) / 0.3f) : 1f) : 1f;
            Graphics2D r = Fx.fade(g, appear, 8);
            r.setColor(Fx.dark() ? new Color(0x16, 0x16, 0x2C) : Color.WHITE);
            r.fill(Fx.rr(fx, rowY, fw, 30, 10));
            Draw.drawAvatar(r, Math.round(fx + 6), Math.round(rowY + 4), 22, res[i][0], rc[i][0], rc[i][1]);
            r.setFont(Fx.bold(12.5f));
            r.setColor(Fx.text());
            Fx.textMid(r, res[i][1], fx + 36, rowY + 15);
            r.setColor(Fx.ink(Fx.VIOLET));
            Fx.textMid(r, res[i][2], fx + fw - 12 - r.getFontMetrics().stringWidth(res[i][2]), rowY + 15);
            r.dispose();
        }
    }

    private void visKanban(Graphics2D g, float x, float y, float w, float h, float time) {
        String[] cols = {"To do", "Doing", "Done"};
        float gap = 8;
        float cw = (w - 28 - gap * 2) / 3f;
        float top = y + 16;
        g.setFont(Fx.label(9.5f));
        for (int i = 0; i < 3; i++) {
            float cx = x + 14 + i * (cw + gap);
            g.setColor(Fx.subtle());
            g.fill(Fx.rr(cx, top, cw, h - 30, 10));
            g.setColor(Fx.text3());
            g.drawString(cols[i].toUpperCase(), cx + 10, top + 18);
            // static cards
            int count = i == 0 ? 2 : (i == 1 ? 1 : 2);
            for (int k = 0; k < count; k++) {
                float ky = top + 28 + k * 38 + (i == 1 ? 38 : 0);
                if (ky + 30 > y + h - 18) break;
                miniCard(g, cx + 6, ky, cw - 12, k + i);
            }
        }
        // moving card: To do -> Doing -> Done
        float cyc = Theme.motionEnabled ? (time % 4.5f) / 4.5f : 0.5f;
        float seg = Fx.easeInOutCubic(Fx.clamp((cyc % 0.5f) / 0.32f));
        int from = cyc < 0.5f ? 0 : 1;
        float x0 = x + 14 + from * (cw + gap) + 6;
        float mx = x0 + (cw + gap) * seg;
        float my = top + 28 + 76 - (float) Math.sin(seg * Math.PI) * 14;
        if (my + 30 <= y + h - 18) {
            Fx.glow(g, mx + (cw - 12) / 2f, my + 15, cw * 0.7f, Fx.CYAN, 0.35f);
            Graphics2D r = (Graphics2D) g.create();
            r.rotate((float) Math.sin(seg * Math.PI) * 0.06f, mx + cw / 2f, my + 15);
            miniCard(r, mx, my, cw - 12, 7);
            r.setColor(Fx.a(Fx.CYAN, 160));
            r.draw(Fx.rr(mx, my, cw - 12, 30, 7));
            r.dispose();
        }
    }

    private void miniCard(Graphics2D g, float x, float y, float w, int seed) {
        g.setColor(Fx.dark() ? new Color(0x1A, 0x1A, 0x32) : Color.WHITE);
        g.fill(Fx.rr(x, y, w, 30, 7));
        Color[] c = {Fx.PINK, Fx.VIOLET, Fx.CYAN, Fx.MINT, Fx.AMBER};
        g.setColor(c[seed % c.length]);
        g.fill(Fx.rr(x + 8, y + 8, 18, 4, 2));
        g.setColor(Fx.border());
        g.fill(Fx.rr(x + 8, y + 17, w * 0.6f, 4, 2));
    }

    private void visChat(Graphics2D g, float x, float y, float w, float h, float time) {
        float bx = x + 18;
        g.setFont(Fx.body(12.5f));
        // incoming
        bubble(g, bx, y + 18, "Can you take the API auth?", false, w);
        // outgoing
        String out = "On it - PR by tonight";
        float ow = g.getFontMetrics().stringWidth(out) + 26;
        bubble(g, x + w - 18 - ow, y + 62, out, true, w);
        // typing indicator
        float ty = y + 108;
        g.setColor(Fx.dark() ? new Color(0x1A, 0x1A, 0x32) : Color.WHITE);
        g.fill(Fx.rr(bx, ty, 62, 32, 16));
        for (int i = 0; i < 3; i++) {
            float bounce = Theme.motionEnabled ? (float) Math.max(0, Math.sin(time * 6f - i * 0.7f)) * 4f : 0f;
            g.setColor(Fx.a(Fx.MINT, 220));
            g.fill(new Ellipse2D.Float(bx + 14 + i * 13, ty + 13 - bounce, 7, 7));
        }
        // bell
        float bcx = x + w - 40, bcy = y + 128;
        float shake = Theme.motionEnabled ? (float) Math.sin(time * 18f) * 0.25f * Math.max(0, (float) Math.sin(time * 1.4f)) : 0f;
        Graphics2D b = (Graphics2D) g.create();
        b.rotate(shake, bcx, bcy - 12);
        b.setColor(Fx.text2());
        Path2D.Float bell = new Path2D.Float();
        bell.moveTo(bcx - 11, bcy + 6);
        bell.curveTo(bcx - 9, bcy - 2, bcx - 10, bcy - 16, bcx, bcy - 16);
        bell.curveTo(bcx + 10, bcy - 16, bcx + 9, bcy - 2, bcx + 11, bcy + 6);
        bell.closePath();
        b.fill(bell);
        b.fill(new Ellipse2D.Float(bcx - 3, bcy + 6, 6, 5));
        b.dispose();
        g.setColor(Fx.PINK);
        g.fill(new Ellipse2D.Float(bcx + 4, bcy - 20, 12, 12));
        g.setColor(Color.WHITE);
        g.setFont(Fx.bold(8.5f));
        Fx.textCenter(g, "3", bcx + 10, bcy - 14);
    }

    private void bubble(Graphics2D g, float x, float y, String text, boolean mine, float maxW) {
        g.setFont(Fx.body(12.5f));
        float bw = g.getFontMetrics().stringWidth(text) + 26;
        RoundRectangle2D.Float b = Fx.rr(x, y, bw, 34, 14);
        if (mine) {
            g.setPaint(new GradientPaint(x, y, Fx.MINT, x + bw, y, Fx.CYAN));
            g.fill(b);
            g.setColor(new Color(0x05, 0x2E, 0x26));
        } else {
            g.setColor(Fx.dark() ? new Color(0x1A, 0x1A, 0x32) : Color.WHITE);
            g.fill(b);
            g.setColor(Fx.text());
        }
        Fx.textMid(g, text, x + 13, y + 17);
    }

    private void visRating(Graphics2D g, float cx, float cy, float time) {
        float ph = time % 5f;
        int filled = Theme.motionEnabled ? Math.min(5, 1 + (int) (ph * 2.2f)) : 5;
        g.setFont(Fx.display(44));
        Fx.gradientText(g, "4.9", cx - g.getFontMetrics().stringWidth("4.9") / 2f, cy + 4, new Color[]{Fx.AMBER, Fx.ORANGE}, 0f);
        for (int i = 0; i < 5; i++) {
            float sx = cx - 60 + i * 24;
            float pop = i < filled ? 1f : 0f;
            int size = i < filled ? 22 : 20;
            if (pop > 0) Fx.glow(g, sx + 10, cy + 34, 18, Fx.AMBER, 0.35f);
            Draw.drawStarIcon(g, Math.round(sx), Math.round(cy + 24 - (size - 20) / 2f), size, i < filled ? Fx.AMBER : Fx.border());
        }
        g.setFont(Fx.body(11.5f));
        g.setColor(Fx.text3());
        Fx.textCenter(g, "from 128 peer reviews", cx, cy + 66);
    }

    private void visAdmin(Graphics2D g, float x, float y, float w, float h, float time) {
        int bars = Math.max(6, Math.min(12, (int) ((w - 40) / 26)));
        float bw = (w - 40) / bars;
        float base = y + h - 22;
        Path2D.Float line = new Path2D.Float();
        for (int i = 0; i < bars; i++) {
            float v = 0.35f + 0.25f * (float) Math.sin(i * 0.9f) + 0.2f * (float) Math.sin(time * 1.6f + i * 0.6f);
            v = Fx.clamp(v + 0.15f, 0.1f, 0.95f);
            float bh = (h - 52) * v;
            float bx = x + 20 + i * bw + bw * 0.2f;
            g.setPaint(new GradientPaint(0, base - bh, Fx.a(Fx.INDIGO, 230), 0, base, Fx.a(Fx.INDIGO, 40)));
            g.fill(Fx.rr(bx, base - bh, bw * 0.6f, bh, 4));
            float lx = bx + bw * 0.3f, ly = base - bh - 10;
            if (i == 0) line.moveTo(lx, ly); else line.lineTo(lx, ly);
        }
        g.setColor(Fx.a(Fx.CYAN, 200));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(line);
        g.setStroke(new BasicStroke(1f));
        g.setFont(Fx.bold(11));
        g.setColor(Fx.text2());
        g.drawString("Active users", x + 20, y + 22);
        g.setColor(Fx.ink(Fx.MINT));
        g.drawString("+24%", x + w - 20 - g.getFontMetrics().stringWidth("+24%"), y + 22);
    }

    private void visSecure(Graphics2D g, float x, float y, float w, float h, float time) {
        float cx = x + w / 2f, cy = y + h / 2f - 6;
        // concentric pulse rings
        for (int i = 0; i < 3; i++) {
            float p = Theme.motionEnabled ? ((time * 0.5f + i / 3f) % 1f) : 0.4f;
            float r = 30 + p * 70;
            g.setColor(Fx.a(new Color(0x6E, 0xC1, 0xFF), Math.round(120 * (1 - p))));
            g.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }
        Fx.glow(g, cx, cy, 80, new Color(0x6E, 0xC1, 0xFF), 0.4f);
        g.setColor(Fx.dark() ? new Color(0x12, 0x16, 0x2C) : Color.WHITE);
        g.fill(new Ellipse2D.Float(cx - 34, cy - 34, 68, 68));
        g.setColor(Fx.a(new Color(0x6E, 0xC1, 0xFF), 150));
        g.draw(new Ellipse2D.Float(cx - 34, cy - 34, 68, 68));
        float closed = Theme.motionEnabled ? Fx.easeOutBack(Fx.clamp(((time % 3f) - 0.6f) / 0.5f)) : 1f;
        Fx.lock(g, cx - 18, cy - 20, 36, Fx.ink(new Color(0x6E, 0xC1, 0xFF)), closed);
        String[] chips = {"PBKDF2", "Salted hashes", "RBAC"};
        g.setFont(Fx.bold(11));
        float total = 0;
        for (String s : chips) total += g.getFontMetrics().stringWidth(s) + 18 + 8;
        float px = cx - (total - 8) / 2f;
        for (String s : chips) {
            px += Fx.tag(g, s, px, y + h - 32, new Color(0x6E, 0xC1, 0xFF), false) + 8;
        }
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        for (Tile t : tiles) t.hovered = Fx.inside(mx, my, t.x, t.y - 6, t.w, t.h + 6);
        return false;
    }
}
