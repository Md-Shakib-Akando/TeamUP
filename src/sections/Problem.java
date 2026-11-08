import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** "The challenge" — three pain-point cards with spotlight hover and gradient metrics. */
public class Problem extends LpSection {

    private static class Card {
        final String tag, title, desc, metric, metricLabel, icon;
        final Color accent;
        int x, y, w, h;
        boolean hovered;
        final Elastic hov = new Elastic(0f, 220f, 20f);

        Card(String tag, String title, String desc, String metric, String metricLabel, String icon, Color accent) {
            this.tag = tag; this.title = title; this.desc = desc; this.metric = metric;
            this.metricLabel = metricLabel; this.icon = icon; this.accent = accent;
        }
    }

    private final List<Card> cards = new ArrayList<>();

    public Problem() {
        cards.add(new Card("SKILL VISIBILITY", "Skills stay invisible",
                "Talent hides behind silent group chats. You waste hours guessing who actually knows Java, UI/UX or systems design.",
                "3.4 days", "lost asking around", "search", Fx.CYAN));
        cards.add(new Card("CHAOTIC WORKFLOW", "Recruiting is scattered",
                "Teams form across WhatsApp, Discord and campus boards with no structured pipeline, roles or criteria.",
                "5+ apps", "juggled per project", "users", Fx.VIOLET));
        cards.add(new Card("ZERO PROOF", "No accountability",
                "Projects collapse without consequences. Freeloaders slip by while dedicated builders earn no verifiable record.",
                "68%", "of teams report unbalanced work", "shield", Fx.PINK));
    }

    @Override public String getId() { return "problem"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (Card c : cards) { c.hov.setTarget(c.hovered ? 1f : 0f); c.hov.update(dt); }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "THE CHALLENGE", Fx.PINK,
                "Team building shouldn't be ", "this hard.",
                "Group chats, cold outreach and scattered boards lead to ghosted projects and broken promises.");
        cur += width < 700 ? 36 : 56;

        int[] c = Fx.container(width);
        int cols = width >= 980 ? 3 : 1;
        int gap = 20;
        int cw = (c[1] - gap * (cols - 1)) / cols;
        if (cols == 1) cw = Math.min(cw, 620);
        int startX = cols == 1 ? Math.round(width / 2f - cw / 2f) : c[0];

        int ch = 0;
        for (Card k : cards) ch = Math.max(ch, cardHeight(g, k, cw));

        for (int i = 0; i < cards.size(); i++) {
            Card k = cards.get(i);
            int col = i % cols, row = i / cols;
            k.x = startX + col * (cw + gap);
            k.y = cur + row * (ch + gap);
            k.w = cw;
            k.h = ch;
            if (measuring) continue;
            Graphics2D cg = Fx.fade(g, rv.p(0.25f + i * 0.12f), 40);
            paintCard(cg, k);
            cg.dispose();
        }
        int rows = (cards.size() + cols - 1) / cols;
        cur += rows * ch + (rows - 1) * gap;
        return cur - top + 40;
    }

    private int cardHeight(Graphics2D g, Card k, int w) {
        return 28 + 52 + 22 + 30 + Fx.paragraphHeight(g, k.desc, Fx.body(15), w - 56, 1.55f) + 28 + 64 + 24;
    }

    private void paintCard(Graphics2D g, Card k) {
        float hv = k.hov.getValue();
        float y = k.y - hv * 8f;
        Fx.card(g, k.x, y, k.w, k.h, 22, hv, k.accent);

        // giant faded index number
        g.setFont(Fx.display(96));
        g.setColor(Fx.a(k.accent, Math.round((Fx.dark() ? 10 : 14) + 14 * hv)));
        String num = "0" + (cards.indexOf(k) + 1);
        g.drawString(num, k.x + k.w - g.getFontMetrics().stringWidth(num) - 18, y + 92);

        int px = k.x + 28;
        float cy = y + 28;
        Fx.iconTile(g, px, cy, 52, k.accent, k.icon, hv);
        cy += 52 + 22;
        g.setFont(Fx.label(11));
        g.setColor(Fx.ink(k.accent));
        g.drawString(k.tag, px, cy + 10);
        cy += 22;
        g.setFont(Fx.heading(22));
        g.setColor(Fx.text());
        g.drawString(k.title, px, cy + 20);
        cy += 30;
        cy = Fx.paragraph(g, k.desc, Fx.body(15), Fx.text2(), px, Math.round(cy), k.w - 56, 1.55f, false);

        // metric footer
        float fy = y + k.h - 24 - 64;
        g.setColor(Fx.border());
        g.drawLine(px, Math.round(fy), k.x + k.w - 28, Math.round(fy));
        g.setFont(Fx.display(30));
        FontMetrics fm = g.getFontMetrics();
        Fx.gradientText(g, k.metric, px, fy + 20 + fm.getAscent() * 0.82f, new Color[]{k.accent, Fx.mix(k.accent, Fx.VIOLET, 0.6f)}, 0f);
        int mw = fm.stringWidth(k.metric);
        g.setFont(Fx.body(13.5f));
        g.setColor(Fx.text3());
        g.drawString(k.metricLabel, px + mw + 12, fy + 20 + fm.getAscent() * 0.78f);
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        for (Card c : cards) c.hovered = Fx.inside(mx, my, c.x, c.y - 8, c.w, c.h + 8);
        return false;
    }
}
