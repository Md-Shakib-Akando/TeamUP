import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** FAQ — split layout with a support card on the left and animated accordions on the right. */
public class Faq extends LpSection {

    private final NavBar.NavListener listener;
    private final List<Accordion> items = new ArrayList<>();
    private int btnX, btnY, btnW, btnH;
    private boolean btnHover;
    private final Elastic btnHov = new Elastic(0f, 260f, 20f);

    public Faq(NavBar.NavListener listener) {
        this.listener = listener;
        items.add(new Accordion("Is TeamUp free to use?",
                "Yes. TeamUp is free for students, developers and researchers - no paywalls, hidden fees or limits on team formation during early access."));
        items.add(new Accordion("Who can join and collaborate?",
                "Anyone who builds: students assembling coursework or hackathon squads, freelancers forming client pods, researchers needing technical help, and early-stage founders."));
        items.add(new Accordion("How does the matching engine work?",
                "It scores compatibility across complementary technical skills, availability, desired project roles and verified feedback from previous collaborators."));
        items.add(new Accordion("How are freeloaders prevented?",
                "Accountability is built into the lifecycle. At each milestone and at wrap-up, collaborators leave peer ratings on reliability, communication and quality."));
        items.add(new Accordion("How is my data kept secure?",
                "Passwords are hashed with salted PBKDF2, sessions are scoped, and private contact details stay masked until an invitation is accepted."));
        items.add(new Accordion("Which platforms are supported?",
                "TeamUp is desktop-first for Windows, macOS and Linux, built on high-performance core Java with no heavy browser runtime."));
        items.get(0).expanded = true;
        items.get(0).expandProgress = 1f;
    }

    public Faq() { this(null); }

    @Override public String getId() { return "faq"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        boolean anim = false;
        for (Accordion a : items) {
            a.update(dt);
            anim |= a.animating();
        }
        if (anim) invalidateHeight();
        btnHov.setTarget(btnHover ? 1f : 0f);
        btnHov.update(dt);
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        int[] c = Fx.container(width);
        boolean split = c[1] >= 980;

        int listX, listW, listY;
        int leftBottom;
        if (split) {
            int lw = 380;
            int lx = c[0];
            int ly = cur;
            Graphics2D e = Fx.fade(g, rv.p(0f), 18);
            e.setFont(Fx.label(11.5f));
            int ew = e.getFontMetrics().stringWidth("FAQ") + 44;
            Fx.eyebrow(e, "FAQ", lx + ew / 2f, ly, Fx.MINT);
            e.dispose();
            ly += 52;
            Graphics2D t = Fx.fade(g, rv.p(0.08f), 24);
            t.setFont(Fx.heading(46));
            FontMetrics fm = t.getFontMetrics();
            t.setColor(Fx.text());
            t.drawString("Questions?", lx, ly + fm.getAscent());
            ly += Math.round(fm.getHeight() * 1.02f);
            Fx.gradientText(t, "Answered.", lx, ly + fm.getAscent(), Fx.BRAND, Fx.wave(0.6f, 0) * 0.25f);
            ly += Math.round(fm.getHeight() * 1.02f);
            t.dispose();
            ly += 14;
            Graphics2D s = Fx.fade(g, rv.p(0.16f), 24);
            ly = Fx.paragraph(s, "Everything about access, accountability and how matching works. Can't find what you need? We're happy to help.",
                    Fx.body(16.5f), Fx.text2(), lx, ly, lw - 20, 1.5f, false);
            s.dispose();
            ly += 32;
            ly = paintSupport(g, lx, ly, lw - 20);
            leftBottom = ly;
            listX = lx + lw + 40;
            listW = c[0] + c[1] - listX;
            listY = cur;
        } else {
            cur = Fx.header(g, rv, width, cur, "FAQ", Fx.MINT, "Questions? ", "Answered.",
                    "Everything about access, accountability and how matching works.");
            cur += 40;
            listW = Math.min(820, c[1]);
            listX = Math.round(width / 2f - listW / 2f);
            listY = cur;
            leftBottom = cur;
        }

        int ly = listY;
        for (int i = 0; i < items.size(); i++) {
            Accordion a = items.get(i);
            int h = a.getHeight(listW);
            if (!measuring) {
                Graphics2D ag = Fx.fade(g, rv.p(0.2f + i * 0.07f), 24);
                a.paint(ag, listX, ly, listW);
                ag.dispose();
            }
            ly += h + 12;
        }
        ly -= 12;

        if (!split) {
            ly += 28;
            ly = paintSupport(g, listX, ly, listW);
        }
        return Math.max(ly, leftBottom) - top + 40;
    }

    private int paintSupport(Graphics2D g, int x, int y, int w) {
        int h = 150;
        if (measuring) return y + h;
        Graphics2D s = Fx.fade(g, rv.p(0.3f), 24);
        Fx.card(s, x, y, w, h, 20, btnHov.getValue() * 0.5f, Fx.MINT);
        // avatar stack
        String[] ini = {"SR", "SM", "SA"};
        Color[][] cols = {{Fx.PINK, Fx.VIOLET}, {Fx.MINT, Fx.CYAN}, {Fx.AMBER, Fx.ORANGE}};
        for (int i = 0; i < 3; i++) {
            float ax = x + 22 + i * 25;
            s.setColor(Fx.solid());
            s.fill(new java.awt.geom.Ellipse2D.Float(ax - 2, y + 20, 34, 34));
            Draw.drawAvatar(s, Math.round(ax), y + 22, 30, ini[i], cols[i][0], cols[i][1]);
        }
        s.setFont(Fx.bold(15));
        s.setColor(Fx.text());
        s.drawString("Still curious?", x + 22 + 3 * 25 + 18, y + 35);
        s.setFont(Fx.body(13));
        s.setColor(Fx.text3());
        s.drawString("The team replies within a day.", x + 22 + 3 * 25 + 18, y + 53);
        btnW = Math.min(w - 44, 220);
        btnH = 44;
        btnX = x + 22;
        btnY = y + h - btnH - 22;
        Fx.ghostButton(s, btnX, btnY, btnW, btnH, "Join early access", btnHov.getValue(), true);
        s.dispose();
        return y + h;
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (Accordion a : items) {
            if (a.onClick(mx, my)) { invalidateHeight(); return true; }
        }
        if (Fx.inside(mx, my, btnX, btnY, btnW, btnH)) {
            if (listener != null) listener.openModal("Join Early Access", "Create your TeamUp account or sign in to get started.", true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;
        for (Accordion a : items) {
            a.onMouseMove(mx, my);
            hand |= a.hovered;
        }
        btnHover = Fx.inside(mx, my, btnX, btnY, btnW, btnH);
        return hand || btnHover;
    }
}
