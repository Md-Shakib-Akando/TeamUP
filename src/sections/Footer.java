import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Footer — brand block, link columns, status pill and a giant gradient wordmark. */
public class Footer extends LpSection {

    private final NavBar.NavListener listener;

    private static class Link {
        final String label, target;
        int x, y, w, h;
        boolean hovered;
        final Elastic hov = new Elastic(0f, 260f, 20f);
        Link(String l, String t) { label = l; target = t; }
    }

    private final String[] colTitles = {"Product", "Explore", "Workspace"};
    private final List<List<Link>> columns = new ArrayList<>();

    public Footer(NavBar.NavListener listener) {
        this.listener = listener;
        List<Link> a = new ArrayList<>();
        a.add(new Link("Features", "features"));
        a.add(new Link("How it works", "how-it-works"));
        a.add(new Link("Live demo", "demo"));
        List<Link> b = new ArrayList<>();
        b.add(new Link("Who it's for", "audience"));
        b.add(new Link("FAQ", "faq"));
        b.add(new Link("Back to top", "hero"));
        List<Link> c = new ArrayList<>();
        c.add(new Link("Student dashboard", "@dashboard"));
        c.add(new Link("Admin panel", "@admin"));
        c.add(new Link("Log in", "@login"));
        c.add(new Link("Create account", "@register"));
        columns.add(a);
        columns.add(b);
        columns.add(c);
    }

    @Override public String getId() { return "footer"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (List<Link> col : columns) for (Link l : col) { l.hov.setTarget(l.hovered ? 1f : 0f); l.hov.update(dt); }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int[] c = Fx.container(width);
        boolean stacked = c[1] < 860;

        int cur = y + 30;
        if (!measuring) {
            // top hairline with glow
            g.setPaint(new GradientPaint(c[0], 0, Fx.a(Fx.VIOLET, 0), c[0] + c[1] / 2f, 0, Fx.a(Fx.VIOLET, 140), true));
            g.fillRect(c[0], cur, c[1], 1);
        }
        cur += 64;

        // Brand block
        int bx = c[0];
        int by = cur;
        if (!measuring) {
            Graphics2D b = Fx.fade(g, rv.p(0f), 20);
            Draw.drawLogo(b, bx, by, 34);
            b.setFont(Fx.heading(22));
            b.setColor(Fx.text());
            Fx.textMid(b, "TeamUp", bx + 46, by + 17);
            int py = Fx.paragraph(b, "Skill-based team formation and milestone collaboration for students, developers and researchers.",
                    Fx.body(14.5f), Fx.text2(), bx, by + 52, Math.min(340, c[1]), 1.6f, false);
            // status pill
            int sy = Math.max(by + 132, py + 18);
            b.setFont(Fx.bold(12));
            String st = "All systems normal";
            int sw = b.getFontMetrics().stringWidth(st) + 36;
            RoundRectangle2D.Float pill = Fx.rr(bx, sy, sw, 28, 14);
            b.setColor(Fx.a(Fx.MINT, Fx.dark() ? 22 : 26));
            b.fill(pill);
            b.setColor(Fx.a(Fx.MINT, 90));
            b.draw(pill);
            float pz = (Fx.time() % 1.8f) / 1.8f;
            Fx.glow(b, bx + 15, sy + 14, 6 + pz * 8, Fx.MINT, 0.8f * (1 - pz));
            b.setColor(Fx.MINT);
            b.fill(new java.awt.geom.Ellipse2D.Float(bx + 12, sy + 11, 6, 6));
            b.setColor(Fx.ink(Fx.MINT));
            Fx.textMid(b, st, bx + 26, sy + 14);
            b.dispose();
        }
        int brandH = 132 + 28;

        // Link columns
        int colsX = stacked ? c[0] : c[0] + Math.max(420, c[1] - 3 * 190);
        int colsY = stacked ? cur + brandH + 44 : cur;
        int colW = stacked ? Math.min(190, c[1] / 3) : 190;
        int maxRows = 0;
        for (int i = 0; i < columns.size(); i++) {
            int x = colsX + i * colW;
            int ly = colsY;
            if (!measuring) {
                Graphics2D h = Fx.fade(g, rv.p(0.1f + i * 0.06f), 20);
                h.setFont(Fx.label(11));
                h.setColor(Fx.text3());
                h.drawString(colTitles[i].toUpperCase(), x, ly + 10);
                h.dispose();
            }
            ly += 34;
            for (Link l : columns.get(i)) {
                if (!measuring) {
                    Graphics2D lg = Fx.fade(g, rv.p(0.15f + i * 0.06f), 20);
                    lg.setFont(Fx.body(14.5f));
                    FontMetrics fm = lg.getFontMetrics();
                    l.x = x; l.y = ly - 4; l.w = fm.stringWidth(l.label) + 22; l.h = 28;
                    float hv = l.hov.getValue();
                    lg.setColor(Fx.mix(Fx.text2(), Fx.text(), hv));
                    Fx.textMid(lg, l.label, x + hv * 4, ly + 10);
                    if (hv > 0.02f) {
                        Graphics2D ar = (Graphics2D) lg.create();
                        Fx.fadeBy(ar, hv);
                        Fx.arrow(ar, x + fm.stringWidth(l.label) + 10 + hv * 4, ly + 10, 9, Fx.ink(Fx.PINK), 1.6f);
                        ar.dispose();
                    }
                    lg.dispose();
                }
                ly += 36;
            }
            maxRows = Math.max(maxRows, columns.get(i).size());
        }
        int colsBottom = colsY + 34 + maxRows * 36;
        cur = Math.max(cur + brandH, colsBottom) + 48;

        // bottom bar
        if (!measuring) {
            g.setColor(Fx.border());
            g.drawLine(c[0], cur, c[0] + c[1], cur);
            g.setFont(Fx.body(13));
            g.setColor(Fx.text3());
            g.drawString("(c) 2026 TeamUp. Designed & developed by Sadia Rimi, Sanjida Mostak and Md. Shakib Akando.", c[0], cur + 32);
            if (!stacked) {
                String sc = "M  motion   -   Ctrl +/-  zoom   -   F2  dashboard";
                g.drawString(sc, c[0] + c[1] - g.getFontMetrics().stringWidth(sc), cur + 32);
            }
        }
        cur += stacked ? 44 : 36;

        // giant wordmark, clipped at the bottom edge
        float ws = Math.min(240, c[1] / 4.2f);
        Font wf = Fx.display(ws / Theme.fontScale);
        FontMetrics wfm = g.getFontMetrics(wf);
        int cap = Math.round(wfm.getAscent() * 0.72f);
        int wordH = Math.round(cap * 0.92f);
        if (!measuring) {
            Graphics2D w = (Graphics2D) g.create();
            w.clipRect(0, cur, width, wordH);
            w.setFont(wf);
            String word = "TeamUp";
            float wx = width / 2f - wfm.stringWidth(word) / 2f;
            float base = cur + cap;
            Color a1 = Fx.a(Fx.dark() ? Color.WHITE : new Color(30, 20, 80), Fx.dark() ? 46 : 40);
            w.setPaint(new GradientPaint(0, cur, a1, 0, cur + wordH, Fx.a(a1, 0)));
            w.drawString(word, wx, base);
            // cursor-tracked shine across the wordmark
            if (Theme.motionEnabled) {
                Graphics2D sh = (Graphics2D) w.create();
                sh.setPaint(new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(Anim.mouseX, Anim.mouseY), 260f,
                        new float[]{0f, 1f}, new Color[]{Fx.a(Fx.PINK, 120), Fx.a(Fx.VIOLET, 0)}));
                sh.drawString(word, wx, base);
                sh.dispose();
            }
            w.dispose();
        }
        cur += wordH;
        return cur - top;
    }

    private void go(Link l) {
        if (listener == null) return;
        switch (l.target) {
            case "@dashboard": listener.openDashboard(); break;
            case "@admin": listener.openAdminDashboard(); break;
            case "@login": listener.openModal("TeamUp Account Login", "Enter your credentials to access your projects, messages, and saved teams.", false); break;
            case "@register": listener.openModal("Create your TeamUp Account", "Join students, developers, and researchers building great projects.", true); break;
            default: listener.scrollToSection(l.target);
        }
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (List<Link> col : columns) for (Link l : col) {
            if (Fx.inside(mx, my, l.x, l.y, l.w, l.h)) { go(l); return true; }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;
        for (List<Link> col : columns) for (Link l : col) {
            l.hovered = Fx.inside(mx, my, l.x, l.y, l.w, l.h);
            hand |= l.hovered;
        }
        return hand;
    }
}
