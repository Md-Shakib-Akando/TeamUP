import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** "Who it's for" — segmented role tabs with a spring indicator and a cross-fading story card. */
public class Audience extends LpSection {

    private static class Role {
        final String name, person, title, initials, benefit, quote;
        final String[] team;
        final Color c1, c2;
        int x, y, w, h;
        boolean hovered;
        Role(String name, String person, String title, String initials, Color c1, Color c2,
             String benefit, String quote, String... team) {
            this.name = name; this.person = person; this.title = title; this.initials = initials;
            this.c1 = c1; this.c2 = c2; this.benefit = benefit; this.quote = quote; this.team = team;
        }
    }

    private final List<Role> roles = new ArrayList<>();
    private int sel = 0;
    private final Elastic indX = new Elastic(0f, 280f, 24f);
    private final Elastic indW = new Elastic(0f, 280f, 24f);
    private boolean indInit = false;
    private float switchT = 10f;
    private float autoT = 0f;
    private boolean userPicked = false;
    private boolean cardHovered = false;
    private int cardX, cardY, cardW, cardH;

    public Audience() {
        roles.add(new Role("Students", "Rafi Karim", "CS Undergraduate", "RK", Fx.CYAN, Fx.VIOLET,
                "Find classmates who actually ship - instead of carrying group assignments alone.",
                "I posted our software engineering project needing a Java backend lead and a UI designer. Two hours later, our team was formed.",
                "Backend lead", "UI designer", "QA tester"));
        roles.add(new Role("Freelancers", "Nila Rahman", "Independent Designer", "NR", Fx.PINK, Fx.VIOLET,
                "Form reliable pods and build a verified rating history that wins bigger contracts.",
                "Instead of turning down full-stack inquiries, I pair with vetted Java and database developers and deliver turn-key projects.",
                "Full-stack dev", "DB engineer", "Copywriter"));
        roles.add(new Role("Developers", "Tanvir Ahmed", "Full-Stack Builder", "TA", Fx.MINT, Fx.CYAN,
                "Join hackathons and open-source ventures matched to your exact engineering strengths.",
                "I filter by the stacks I want to work with. No time wasted on mismatched skill sets or ghosted chats.",
                "ML engineer", "Designer", "PM"));
        roles.add(new Role("Researchers", "Dr. Hasan", "Lab Director", "DH", Fx.AMBER, Fx.ORANGE,
                "Recruit specialised contributors for prototypes, simulations and computational modelling.",
                "We needed deep algorithmic optimisation experience for a bioinformatics simulation. TeamUp matched us instantly.",
                "Algorithms", "Data viz", "HPC"));
        roles.add(new Role("Startups", "Maya & Leo", "MVP Founders", "ML", Fx.VIOLET, Fx.PINK,
                "Assemble multi-disciplinary founding crews without expensive agency retainers.",
                "We validated our prototype architecture with a designer and systems architect recruited through verified skill reviews.",
                "Architect", "Brand designer", "Growth"));
    }

    @Override public String getId() { return "audience"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        switchT += dt;
        if (rv.started() && !userPicked && !cardHovered && Theme.motionEnabled) {
            autoT += dt;
            if (autoT > 6f) { autoT = 0f; select((sel + 1) % roles.size(), false); }
        }
        Role r = roles.get(sel);
        if (r.w > 0) {
            if (!indInit) { indX.setValue(r.x); indW.setValue(r.w); indInit = true; }
            indX.setTarget(r.x);
            indW.setTarget(r.w);
        }
        indX.update(dt);
        indW.update(dt);
    }

    private void select(int i, boolean user) {
        if (i == sel) return;
        sel = i;
        switchT = 0f;
        if (user) userPicked = true;
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "WHO IT'S FOR", Fx.AMBER,
                "Built for every kind of ", "builder.",
                "Coursework, client pods, research labs or MVPs - TeamUp adapts to your stage.");
        cur += width < 700 ? 32 : 48;

        int[] c = Fx.container(width);

        // ---- segmented control
        g.setFont(Fx.bold(14));
        FontMetrics fm = g.getFontMetrics();
        int segH = 46, pad = 5;
        int total = pad * 2;
        for (Role r : roles) total += fm.stringWidth(r.name) + 36;
        boolean wrap = total > c[1];
        if (!wrap) {
            int sx = Math.round(width / 2f - total / 2f);
            int x = sx + pad;
            for (Role r : roles) {
                int w = fm.stringWidth(r.name) + 36;
                if (!measuring) { r.x = x; r.y = cur + pad; r.w = w; r.h = segH - pad * 2; }
                x += w;
            }
            if (!measuring) {
                Graphics2D sg = Fx.fade(g, rv.p(0.2f), 20);
                RoundRectangle2D.Float bar = Fx.rr(sx, cur, total, segH, segH / 2f);
                sg.setColor(Fx.surface());
                sg.fill(bar);
                sg.setColor(Fx.border());
                sg.draw(bar);
                // indicator
                float ix = indX.getValue(), iw = indW.getValue();
                Fx.glow(sg, ix + iw / 2f, cur + segH / 2f, iw * 0.7f, Fx.PINK, 0.3f);
                sg.setPaint(new GradientPaint(ix, cur, Fx.PINK, ix + iw, cur + segH, Fx.VIOLET));
                sg.fill(Fx.rr(ix, cur + pad, iw, segH - pad * 2, (segH - pad * 2) / 2f));
                for (int i = 0; i < roles.size(); i++) {
                    Role r = roles.get(i);
                    sg.setFont(Fx.bold(14));
                    float near = 1f - Fx.clamp(Math.abs((r.x + r.w / 2f) - (ix + iw / 2f)) / (r.w * 0.6f));
                    sg.setColor(Fx.mix(r.hovered ? Fx.text() : Fx.text2(), Color.WHITE, near));
                    Fx.textCenter(sg, r.name, r.x + r.w / 2f, r.y + r.h / 2f);
                }
                sg.dispose();
            }
            cur += segH;
        } else {
            // wrap into pill rows
            int x = c[0], rowY = cur;
            int h = 40, gap = 8;
            for (int i = 0; i < roles.size(); i++) {
                Role r = roles.get(i);
                int w = fm.stringWidth(r.name) + 32;
                if (x + w > c[0] + c[1]) { x = c[0]; rowY += h + gap; }
                if (!measuring) {
                    r.x = x; r.y = rowY; r.w = w; r.h = h;
                    RoundRectangle2D.Float p = Fx.rr(x, rowY, w, h, h / 2f);
                    if (i == sel) {
                        g.setPaint(new GradientPaint(x, rowY, Fx.PINK, x + w, rowY + h, Fx.VIOLET));
                        g.fill(p);
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Fx.surface());
                        g.fill(p);
                        g.setColor(Fx.border());
                        g.draw(p);
                        g.setColor(Fx.text2());
                    }
                    g.setFont(Fx.bold(14));
                    Fx.textCenter(g, r.name, x + w / 2f, rowY + h / 2f);
                }
                x += w + gap;
            }
            cur = rowY + h;
        }
        cur += 28;

        // ---- story card
        Role r = roles.get(sel);
        int cw = Math.min(1000, c[1]);
        int cx = Math.round(width / 2f - cw / 2f);
        boolean two = cw >= 760;
        int leftW = two ? Math.round(cw * 0.6f) : cw;
        int rightW = two ? cw - leftW : cw;
        int qH = 0, bH = 0;
        for (Role any : roles) {
            qH = Math.max(qH, Fx.paragraphHeight(g, any.quote, Fx.font(Font.PLAIN, two ? 21 : 18), leftW - 80, 1.45f));
            bH = Math.max(bH, Fx.paragraphHeight(g, any.benefit, Fx.body(15), rightW - 64, 1.5f));
        }
        int leftH = 40 + 40 + qH + 30 + 48 + 40;
        int rightH = 40 + 22 + bH + 26 + 22 + 3 * 42 + 30;
        int ch = two ? Math.max(leftH, rightH) : leftH + rightH;
        if (!measuring) { cardX = cx; cardY = cur; cardW = cw; cardH = ch; }
        if (measuring) return cur + ch - top + 40;

        Graphics2D cg = Fx.fade(g, rv.p(0.35f), 36);
        Fx.glowOval(cg, cx + cw * 0.3f, cur + ch / 2f, cw * 0.4f, ch * 0.6f, r.c1, Fx.dark() ? 0.16f : 0.1f);
        Fx.card(cg, cx, cur, cw, ch, 26, cardHovered ? 1f : 0.2f, r.c1);

        float ap = Fx.easeOutQuart(switchT / 0.55f);
        Graphics2D a = (Graphics2D) cg.create();
        Fx.fadeBy(a, ap);
        a.translate((1f - ap) * 18f, 0);

        // quote side
        int lx = cx + 40;
        int ly = cur + 40;
        paintQuoteMark(a, lx, ly, r);
        ly += 40;
        ly = Fx.paragraph(a, r.quote, Fx.font(Font.PLAIN, two ? 21 : 18), Fx.text(), lx, ly, leftW - 80, 1.45f, false);
        ly += 30;
        Fx.avatar(a, lx, ly, 44, r.initials, r.c1, r.c2, true);
        a.setFont(Fx.bold(15));
        a.setColor(Fx.text());
        a.drawString(r.person, lx + 58, ly + 19);
        a.setFont(Fx.body(13));
        a.setColor(Fx.text3());
        a.drawString(r.title, lx + 58, ly + 38);

        // benefit side
        int rx = two ? cx + leftW : cx;
        int ry = two ? cur : cur + leftH;
        if (two) {
            a.setColor(Fx.border());
            a.drawLine(rx, cur + 28, rx, cur + ch - 28);
        } else {
            a.setColor(Fx.border());
            a.drawLine(cx + 28, ry, cx + cw - 28, ry);
        }
        int bx = rx + 32;
        int by = ry + 40;
        a.setFont(Fx.label(11));
        a.setColor(Fx.ink(r.c1));
        a.drawString("WHY IT WORKS", bx, by + 10);
        by += 22;
        by = Fx.paragraph(a, r.benefit, Fx.body(15), Fx.text2(), bx, by, rightW - 64, 1.5f, false);
        by += 26;
        a.setFont(Fx.label(11));
        a.setColor(Fx.text3());
        a.drawString("TYPICAL TEAM", bx, by + 10);
        by += 22;
        for (int i = 0; i < r.team.length; i++) {
            float ip = Fx.easeOutQuart((switchT - 0.1f - i * 0.08f) / 0.5f);
            Graphics2D t = (Graphics2D) a.create();
            Fx.fadeBy(t, ip);
            t.translate((1 - ip) * 14, 0);
            RoundRectangle2D.Float row = Fx.rr(bx, by, rightW - 64, 34, 10);
            t.setColor(Fx.subtle());
            t.fill(row);
            Fx.checkCircle(t, bx + 9, by + 8, 18, Fx.MINT, 1f);
            t.setFont(Fx.bold(13));
            t.setColor(Fx.text());
            Fx.textMid(t, r.team[i], bx + 36, by + 17);
            t.dispose();
            by += 42;
        }
        a.dispose();

        // progress dots for auto-rotation
        if (!userPicked) {
            float pr = Fx.clamp(autoT / 6f);
            int dw = 64;
            float dx = cx + cw - 40 - dw, dy = cur + ch - 22;
            cg.setColor(Fx.border());
            cg.fill(Fx.rr(dx, dy, dw, 3, 1.5f));
            cg.setPaint(new GradientPaint(dx, 0, Fx.PINK, dx + dw, 0, Fx.VIOLET));
            cg.fill(Fx.rr(dx, dy, dw * pr, 3, 1.5f));
        }
        cg.dispose();

        return cur + ch - top + 40;
    }

    private void paintQuoteMark(Graphics2D g, float x, float y, Role r) {
        Graphics2D q = (Graphics2D) g.create();
        q.setPaint(new GradientPaint(x, y, r.c1, x + 40, y + 30, r.c2));
        for (int i = 0; i < 2; i++) {
            float ox = x + i * 20;
            q.fill(new java.awt.geom.Ellipse2D.Float(ox, y + 6, 14, 14));
            Path2D.Float tail = new Path2D.Float();
            tail.moveTo(ox + 1, y + 14);
            tail.quadTo(ox + 1, y - 2, ox + 14, y - 4);
            tail.lineTo(ox + 13, y + 1);
            tail.quadTo(ox + 6, y + 3, ox + 7, y + 12);
            tail.closePath();
            q.fill(tail);
        }
        q.dispose();
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (int i = 0; i < roles.size(); i++) {
            Role r = roles.get(i);
            if (Fx.inside(mx, my, r.x, r.y, r.w, r.h)) { select(i, true); return true; }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;
        for (Role r : roles) {
            r.hovered = Fx.inside(mx, my, r.x, r.y, r.w, r.h);
            hand |= r.hovered;
        }
        cardHovered = Fx.inside(mx, my, cardX, cardY, cardW, cardH);
        return hand;
    }
}
