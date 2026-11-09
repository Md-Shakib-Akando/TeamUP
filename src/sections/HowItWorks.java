import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Four-step timeline. A gradient progress line draws in on reveal, then the
 * active step auto-advances (hover or click takes over).
 */
public class HowItWorks extends LpSection {

    private static class Step {
        final String title, desc, icon;
        final Color accent;
        int x, y, w, h;
        boolean hovered;
        final Elastic act = new Elastic(0f, 200f, 20f);
        Step(String t, String d, String i, Color a) { title = t; desc = d; icon = i; accent = a; }
    }

    private final List<Step> steps = new ArrayList<>();
    private int active = 0;
    private float autoT = 0f;
    private int hoverIdx = -1;
    private final Elastic lineFill = new Elastic(0f, 40f, 12f);

    public HowItWorks() {
        steps.add(new Step("Create your profile", "Showcase verified skills, project history and your GitHub or portfolio links.", "users", Fx.CYAN));
        steps.add(new Step("Publish or discover", "Browse open projects or post your own with roles, stack and timeline.", "rocket", Fx.VIOLET));
        steps.add(new Step("Match and invite", "Get compatibility scores, compare complementary skills and send invites.", "spark", Fx.PINK));
        steps.add(new Step("Build and rate", "Ship with Kanban milestones and chat, then exchange verified peer reviews.", "star", Fx.AMBER));
    }

    @Override public String getId() { return "how-it-works"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (rv.started() && hoverIdx < 0 && Theme.motionEnabled) {
            autoT += dt;
            if (autoT > 3.4f) { autoT = 0f; active = (active + 1) % steps.size(); }
        }
        int cur = hoverIdx >= 0 ? hoverIdx : active;
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).act.setTarget(i == cur ? 1f : 0f);
            steps.get(i).act.update(dt);
        }
        lineFill.setTarget(rv.started() ? cur / (float) (steps.size() - 1) : 0f);
        lineFill.update(dt);
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "HOW IT WORKS", Fx.VIOLET,
                "From idea to ", "dream team.",
                "A transparent, milestone-driven workflow that surfaces the right talent and keeps everyone accountable.");
        cur += width < 700 ? 40 : 64;

        int[] c = Fx.container(width);
        boolean horiz = width >= 980;

        if (horiz) {
            int gap = 20;
            int sw = (c[1] - gap * 3) / 4;
            int nodeY = cur + 22;
            int cardY = cur + 70;
            int ch = 0;
            for (Step s : steps) ch = Math.max(ch, cardH(g, s, sw));

            if (!measuring) {
                // track
                int x0 = c[0] + sw / 2, x1 = c[0] + c[1] - sw / 2;
                float draw = rv.p(0.3f, 1.2f);
                g.setColor(Fx.border());
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f, 7f}, 0f));
                g.drawLine(x0, nodeY, Math.round(x0 + (x1 - x0) * draw), nodeY);
                float fx = x0 + (x1 - x0) * lineFill.getValue() * draw;
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setPaint(new GradientPaint(x0, 0, Fx.CYAN, x1, 0, Fx.PINK));
                g.drawLine(x0, nodeY, Math.round(fx), nodeY);
                Fx.glow(g, fx, nodeY, 34, Fx.PINK, 0.6f * draw);
            }

            for (int i = 0; i < steps.size(); i++) {
                Step s = steps.get(i);
                s.x = c[0] + i * (sw + gap);
                s.y = cardY;
                s.w = sw;
                s.h = ch;
                if (measuring) continue;
                float p = rv.p(0.35f + i * 0.14f);
                Graphics2D ng = Fx.pop(g, p, s.x + sw / 2f, nodeY);
                paintNode(ng, s, i, s.x + sw / 2f, nodeY);
                ng.dispose();
                Graphics2D cg = Fx.fade(g, p, 36);
                paintCard(cg, s, i);
                cg.dispose();
            }
            cur = cardY + ch;
        } else {
            int cw = Math.min(620, c[1]) - 56;
            int x = Math.round(width / 2f - (cw + 56) / 2f);
            int lineX = x + 22;
            int cy = cur;
            int startY = cy;
            for (int i = 0; i < steps.size(); i++) {
                Step s = steps.get(i);
                int ch = cardH(g, s, cw);
                s.x = x + 56; s.y = cy; s.w = cw; s.h = ch;
                if (!measuring) {
                    float p = rv.p(0.3f + i * 0.12f);
                    Graphics2D ng = Fx.pop(g, p, lineX, cy + 24);
                    paintNode(ng, s, i, lineX, cy + 24);
                    ng.dispose();
                    Graphics2D cg = Fx.fade(g, p, 30);
                    paintCard(cg, s, i);
                    cg.dispose();
                }
                cy += ch + 18;
            }
            if (!measuring) {
                g.setColor(Fx.border());
                g.setStroke(new BasicStroke(2f));
                g.drawLine(lineX, startY + 48, lineX, cy - 40);
            }
            cur = cy - 18;
        }
        return cur - top + 40;
    }

    private int cardH(Graphics2D g, Step s, int w) {
        return 24 + 18 + 30 + Fx.paragraphHeight(g, s.desc, Fx.body(14.5f), w - 48, 1.55f) + 18 + 58 + 22;
    }

    private void paintNode(Graphics2D g, Step s, int i, float cx, float cy) {
        float a = s.act.getValue();
        float r = 20 + a * 3;
        Fx.glow(g, cx, cy, 40 + a * 30, s.accent, 0.2f + 0.45f * a);
        g.setColor(Fx.dark() ? new Color(0x0E, 0x0E, 0x20) : Color.WHITE);
        g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        g.setStroke(new BasicStroke(2f));
        g.setColor(Fx.mix(Fx.borderHi(), s.accent, a));
        g.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        if (a > 0.05f && Theme.motionEnabled) {
            // orbiting arc on the active node
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Fx.a(s.accent, Math.round(220 * a)));
            float start = -Fx.time() * 220f;
            g.draw(new Arc2D.Float(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2, start, 90, Arc2D.OPEN));
        }
        g.setFont(Fx.bold(14));
        g.setColor(Fx.mix(Fx.text2(), Fx.text(), a));
        Fx.textCenter(g, "0" + (i + 1), cx, cy);
    }

    private void paintCard(Graphics2D g, Step s, int i) {
        float a = s.act.getValue();
        float y = s.y - a * 6f;
        Fx.card(g, s.x, y, s.w, s.h, 20, a, s.accent);
        if (a > 0.02f) Fx.gradientBorder(g, Fx.rr(s.x, y, s.w, s.h, 20), 1.3f, a * 0.9f, Fx.time() * 0.5f);

        int px = s.x + 24;
        float cy = y + 24;
        Fx.icon(g, s.icon, px, cy, 18, Fx.ink(s.accent));
        g.setFont(Fx.label(10.5f));
        g.setColor(Fx.ink(s.accent));
        g.drawString("STEP 0" + (i + 1), px + 28, cy + 13);
        cy += 18 + 14;
        g.setFont(Fx.heading(19));
        g.setColor(Fx.text());
        g.drawString(s.title, px, cy + 16);
        cy += 30;
        Fx.paragraph(g, s.desc, Fx.body(14.5f), Fx.text2(), px, Math.round(cy), s.w - 48, 1.55f, false);

        // mini widget
        float wy = y + s.h - 22 - 58;
        int ww = s.w - 48;
        g.setColor(Fx.subtle());
        g.fill(Fx.rr(px, wy, ww, 58, 12));
        g.setColor(Fx.border());
        g.draw(Fx.rr(px, wy, ww, 58, 12));
        float mid = wy + 29;
        switch (i) {
            case 0: {
                Draw.drawAvatar(g, px + 12, Math.round(mid - 15), 30, "RK", Fx.CYAN, Fx.VIOLET);
                g.setFont(Fx.bold(12.5f));
                g.setColor(Fx.text());
                g.drawString("Rafi K.", px + 52, mid - 2);
                Fx.checkCircle(g, px + 52 + g.getFontMetrics().stringWidth("Rafi K.") + 6, mid - 14, 14, Fx.MINT, 1f);
                g.setFont(Fx.body(11.5f));
                g.setColor(Fx.text3());
                g.drawString("Java  -  UI/UX  -  SQL", px + 52, mid + 15);
                break;
            }
            case 1: {
                g.setFont(Fx.bold(12));
                g.setColor(Fx.text2());
                g.drawString("Open roles", px + 14, mid - 4);
                float tx = px + 14;
                int w1 = Fx.tag(g, "Backend", tx, mid + 2, Fx.VIOLET, false);
                if (tx + w1 + 6 + 60 < px + ww) Fx.tag(g, "Design", tx + w1 + 6, mid + 2, Fx.PINK, false);
                break;
            }
            case 2: {
                g.setFont(Fx.bold(12.5f));
                g.setColor(Fx.text());
                g.drawString("Skill match", px + 14, mid - 5);
                float v = 0.96f * (0.35f + 0.65f * a);
                String pct = "96%";
                g.setColor(Fx.ink(Fx.PINK));
                g.drawString(pct, px + ww - 14 - g.getFontMetrics().stringWidth(pct), mid - 5);
                g.setColor(Fx.border());
                g.fill(Fx.rr(px + 14, mid + 6, ww - 28, 7, 3.5f));
                g.setPaint(new GradientPaint(px + 14, 0, Fx.PINK, px + ww - 14, 0, Fx.VIOLET));
                g.fill(Fx.rr(px + 14, mid + 6, (ww - 28) * v, 7, 3.5f));
                break;
            }
            default: {
                int filled = a > 0.5f && Theme.motionEnabled ? Math.min(5, 1 + (int) ((Fx.time() * 3f) % 6)) : 5;
                for (int k = 0; k < 5; k++) {
                    Draw.drawStarIcon(g, px + 14 + k * 20, Math.round(mid - 9), 18, k < filled ? Fx.AMBER : Fx.border());
                }
                g.setFont(Fx.bold(13));
                g.setColor(Fx.text());
                g.drawString("4.9", px + 14 + 5 * 20 + 8, mid + 5);
            }
        }
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            if (Fx.inside(mx, my, s.x, s.y, s.w, s.h)) { active = i; autoT = 0f; return true; }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        hoverIdx = -1;
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            s.hovered = Fx.inside(mx, my, s.x, s.y - 6, s.w, s.h + 6);
            if (s.hovered) hoverIdx = i;
        }
        if (hoverIdx >= 0) { active = hoverIdx; autoT = 0f; }
        return hoverIdx >= 0;
    }
}
