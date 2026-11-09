import java.awt.*;

/** Stat band — numbers count up when scrolled into view. */
public class NumbersStrip extends LpSection {

    private final String[] prefix = {"", "", "< ", ""};
    private final float[] value = {8, 4, 2, 100};
    private final String[] suffix = {"", "", "s", "%"};
    private final String[] label = {"Architected modules", "Distinct user roles", "Target search speed", "Pure core Java"};
    private final String[] sub = {"End-to-end modular design", "Leader, Member, Admin, Visitor", "Indexed, low-latency queries", "No heavy frameworks"};
    private final String[] icons = {"layers", "users", "bolt", "spark"};
    private final Color[] accents = {Fx.PINK, Fx.VIOLET, Fx.CYAN, Fx.MINT};

    @Override public String getId() { return "numbers"; }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 60 : 100);
        int[] c = Fx.container(width);
        int cols = width >= 980 ? 4 : (width >= 600 ? 2 : 1);
        int rows = (4 + cols - 1) / cols;
        int cellH = 150;
        int bh = rows * cellH;
        if (measuring) return cur + bh - top + 20;

        Graphics2D b = Fx.fade(g, rv.p(0f, 0.9f), 30);
        Fx.glowOval(b, width / 2f, cur + bh / 2f, c[1] * 0.5f, bh * 0.8f, Fx.VIOLET, Fx.dark() ? 0.14f : 0.08f);
        Fx.card(b, c[0], cur, c[1], bh, 26, 0f, Fx.PINK);

        int cw = c[1] / cols;
        for (int i = 0; i < 4; i++) {
            int col = i % cols, row = i / cols;
            int x = c[0] + col * cw;
            int cy = cur + row * cellH;
            if (col > 0) {
                b.setPaint(new GradientPaint(0, cy + 20, Fx.a(Fx.border(), 0), 0, cy + cellH / 2f, Fx.borderHi(), true));
                b.fillRect(x, cy + 24, 1, cellH - 48);
            }
            if (row > 0 && col == 0) {
                b.setColor(Fx.border());
                b.drawLine(c[0] + 24, cy, c[0] + c[1] - 24, cy);
            }
            float p = rv.lin(0.2f + i * 0.12f, 1.6f);
            float e = Fx.easeOutExpo(p);
            String num;
            if (value[i] >= 10) num = String.valueOf(Math.round(value[i] * e));
            else num = String.valueOf(Math.max(e > 0 ? 1 : 0, Math.round(value[i] * e)));
            num = prefix[i] + num + suffix[i];

            int px = x + 32;
            Fx.iconTile(b, px, cy + 32, 40, accents[i], icons[i].equals("layers") ? "task" : icons[i], 0f);
            b.setFont(Fx.display(40));
            Fx.gradientText(b, num, px + 56, cy + 32 + 34, new Color[]{Fx.text(), Fx.mix(Fx.text(), accents[i], 0.6f)}, 0f);
            b.setFont(Fx.bold(15));
            b.setColor(Fx.text());
            b.drawString(label[i], px, cy + 98);
            b.setFont(Fx.body(13));
            b.setColor(Fx.text3());
            b.drawString(sub[i], px, cy + 119);
        }
        b.dispose();
        return cur + bh - top + 20;
    }
}
