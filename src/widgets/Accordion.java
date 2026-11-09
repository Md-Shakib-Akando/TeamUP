import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Glass accordion row: plus icon morphs into a cross, answer slides and fades in. */
public class Accordion {
    public int x, y, width;
    public String question;
    public String answer;
    public boolean expanded = false;
    public float expandProgress = 0f; // 0..1
    public boolean hovered = false;
    private final Elastic hov = new Elastic(0f, 240f, 22f);

    public static final int HEADER_H = 68;

    public Accordion(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public void update(float dt) {
        expandProgress = Anim.approach(expandProgress, expanded ? 1f : 0f, dt, 9.0f);
        hov.setTarget(hovered ? 1f : 0f);
        hov.update(dt);
    }

    /** True while the open/close animation is running. */
    public boolean animating() {
        float t = expanded ? 1f : 0f;
        return Math.abs(expandProgress - t) > 0.001f;
    }

    private static Font qFont() { return Fx.bold(16.5f); }
    private static Font aFont() { return Fx.body(15); }

    public int getHeight(int w, FontMetrics ignored1, FontMetrics ignored2) {
        return getHeight(w);
    }

    public int getHeight(int w) {
        if (expandProgress <= 0.001f) return HEADER_H;
        FontMetrics fm = Fx.metrics(aFont());
        return HEADER_H + Math.round(contentH(fm, w) * Fx.easeInOutCubic(expandProgress));
    }

    private int contentH(FontMetrics fm, int w) {
        List<String> lines = Draw.wrapText(answer, fm, w - 76);
        return lines.size() * Math.round(fm.getHeight() * 1.6f) + 22;
    }

    public void paint(Graphics2D g, int x, int y, int w) {
        this.x = x;
        this.y = y;
        this.width = w;
        int h = getHeight(w);
        float hv = hov.getValue();
        float e = Fx.easeInOutCubic(expandProgress);

        Fx.card(g, x, y, w, h, 18, Math.max(hv * 0.6f, e), Fx.VIOLET);
        RoundRectangle2D.Float shape = Fx.rr(x, y, w, h, 18);
        if (e > 0.01f) {
            Graphics2D c = (Graphics2D) g.create();
            c.clip(shape);
            c.setPaint(new GradientPaint(0, y, Fx.PINK, 0, y + h, Fx.VIOLET));
            Fx.fadeBy(c, e);
            c.fillRect(x, y + 14, 3, h - 28);
            c.dispose();
        }

        // Question
        g.setFont(qFont());
        g.setColor(Fx.text());
        FontMetrics qfm = g.getFontMetrics();
        String q = question;
        while (qfm.stringWidth(q) > w - 100 && q.length() > 8) q = q.substring(0, q.length() - 2);
        if (!q.equals(question)) q = q.trim() + "...";
        Fx.textMid(g, q, x + 28, y + HEADER_H / 2f);

        // Plus -> cross icon in a circle
        float icx = x + w - 40, icy = y + HEADER_H / 2f;
        float r = 15;
        g.setColor(e > 0.5f ? Fx.a(Fx.PINK, Fx.dark() ? 40 : 30) : Fx.subtle());
        g.fill(new java.awt.geom.Ellipse2D.Float(icx - r, icy - r, r * 2, r * 2));
        Graphics2D ic = (Graphics2D) g.create();
        ic.translate(icx, icy);
        ic.rotate(e * Math.PI / 4 * 3);
        ic.setColor(e > 0.5f ? Fx.ink(Fx.PINK) : Fx.text2());
        ic.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        ic.drawLine(-6, 0, 6, 0);
        ic.drawLine(0, -6, 0, 6);
        ic.dispose();

        // Answer
        if (e > 0.02f) {
            Graphics2D a = (Graphics2D) g.create();
            a.clipRect(x, y + HEADER_H - 6, w, h - HEADER_H + 6);
            Fx.fadeBy(a, e);
            a.translate(0, (1 - e) * -8);
            Fx.paragraph(a, answer, aFont(), Fx.text2(), x + 28, y + HEADER_H - 6, w - 76, 1.6f, false);
            a.dispose();
        }
    }

    public boolean containsHeader(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + HEADER_H;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean prev = hovered;
        hovered = containsHeader(mx, my);
        return hovered != prev;
    }

    public boolean onClick(int mx, int my) {
        if (containsHeader(mx, my)) {
            expanded = !expanded;
            return true;
        }
        return false;
    }
}
