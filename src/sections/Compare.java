import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Comparison matrix — highlighted TeamUp column, staggered tick pop-ins and row hover. */
public class Compare extends LpSection {

    private static class Row {
        final String feature;
        final boolean[] has; // TeamUp, CampLink, CollabConnect, Devpost
        int y;
        Row(String f, boolean... h) { feature = f; has = h; }
    }

    private final List<Row> rows = new ArrayList<>();
    private final String[] cols = {"TeamUp", "CampLink", "CollabConnect", "Devpost"};
    private int hoverRow = -1;
    private int tableX, tableW, rowH = 58, firstRowY;

    public Compare() {
        rows.add(new Row("Skill-based member matching", true, false, true, false));
        rows.add(new Row("Full project lifecycle tracking", true, false, false, true));
        rows.add(new Row("Built-in Kanban task management", true, false, false, false));
        rows.add(new Row("Post-project peer ratings & reviews", true, false, false, false));
        rows.add(new Row("Direct messaging with notifications", true, false, false, false));
        rows.add(new Row("Admin moderation & security panel", true, false, false, true));
    }

    @Override public String getId() { return "compare"; }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 70 : 110);
        cur = Fx.header(g, rv, width, cur, "COMPARE", Fx.VIOLET,
                "Why teams choose ", "TeamUp.",
                "Purpose-built for builder teams - with lifecycle tracking, chat and verified peer reviews in one place.");
        cur += width < 700 ? 36 : 56;

        int[] c = Fx.container(width);
        int tw = Math.min(1000, c[1]);
        int tx = Math.round(width / 2f - tw / 2f);
        boolean narrow = tw < 720;
        int nCols = narrow ? 2 : 4;
        int headH = 84;
        int th = headH + rows.size() * rowH + 16;
        if (!measuring) { tableX = tx; tableW = tw; firstRowY = cur + headH; }
        if (measuring) return cur + th - top + 40;

        Graphics2D t = Fx.fade(g, rv.p(0.2f, 0.9f), 40);
        Fx.card(t, tx, cur, tw, th, 24, 0.2f, Fx.VIOLET);

        int featW = narrow ? Math.round(tw * 0.5f) : Math.round(tw * 0.38f);
        int colW = (tw - featW) / nCols;
        int tuX = tx + featW;

        // Highlight column for TeamUp
        RoundRectangle2D.Float hl = Fx.rr(tuX + 6, cur + 10, colW - 12, th - 20, 18);
        Fx.glowOval(t, tuX + colW / 2f, cur + th / 2f, colW * 0.9f, th * 0.55f, Fx.PINK, Fx.dark() ? 0.18f : 0.1f);
        t.setPaint(new GradientPaint(0, cur, Fx.a(Fx.PINK, Fx.dark() ? 36 : 22), 0, cur + th, Fx.a(Fx.VIOLET, Fx.dark() ? 18 : 10)));
        t.fill(hl);
        Fx.gradientBorder(t, hl, 1.4f, 0.9f, Fx.time() * 0.5f);

        // Header
        t.setFont(Fx.label(11));
        t.setColor(Fx.text3());
        Fx.textMid(t, "CAPABILITY", tx + 28, cur + headH / 2f + 6);
        Draw.drawLogo(t, Math.round(tuX + colW / 2f - 13), cur + 18, 26);
        t.setFont(Fx.heading(15));
        t.setColor(Fx.text());
        Fx.textCenter(t, "TeamUp", tuX + colW / 2f, cur + 60);
        t.setFont(Fx.bold(14));
        if (narrow) {
            t.setColor(Fx.text2());
            Fx.textCenter(t, "Others", tuX + colW + colW / 2f, cur + headH / 2f + 6);
        } else {
            for (int i = 1; i < 4; i++) {
                t.setColor(Fx.text2());
                Fx.textCenter(t, cols[i], tuX + i * colW + colW / 2f, cur + headH / 2f + 6);
            }
        }
        t.setColor(Fx.border());
        t.drawLine(tx + 20, cur + headH, tx + tw - 20, cur + headH);

        // Rows
        int ry = cur + headH;
        for (int r = 0; r < rows.size(); r++) {
            Row row = rows.get(r);
            row.y = ry;
            if (r == hoverRow) {
                t.setColor(Fx.subtle());
                t.fill(Fx.rr(tx + 10, ry + 4, tw - 20, rowH - 8, 12));
            }
            t.setFont(Fx.body(narrow ? 14 : 15));
            t.setColor(r == hoverRow ? Fx.text() : Fx.text2());
            String label = row.feature;
            FontMetrics fm = t.getFontMetrics();
            while (fm.stringWidth(label) > featW - 40 && label.length() > 6) label = label.substring(0, label.length() - 2);
            if (!label.equals(row.feature)) label = label.trim() + "...";
            Fx.textMid(t, label, tx + 28, ry + rowH / 2f);

            for (int k = 0; k < nCols; k++) {
                boolean has;
                if (narrow && k == 1) has = row.has[1] || row.has[2] || row.has[3];
                else has = row.has[k];
                float ccx = tuX + k * colW + colW / 2f;
                float p = rv.p(0.45f + r * 0.08f + k * 0.05f, 0.6f);
                if (has) {
                    if (k == 0) Fx.glow(t, ccx, ry + rowH / 2f, 22, Fx.PINK, 0.4f * p);
                    Fx.checkCircle(t, ccx - 13, ry + rowH / 2f - 13, 26, k == 0 ? Fx.PINK : Fx.text3(), Fx.easeOutBack(p));
                } else {
                    Graphics2D d = Fx.fade(t, p, 0);
                    d.setColor(Fx.text3());
                    d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    d.drawLine(Math.round(ccx - 6), Math.round(ry + rowH / 2f), Math.round(ccx + 6), Math.round(ry + rowH / 2f));
                    d.dispose();
                }
            }
            ry += rowH;
            if (r < rows.size() - 1) {
                t.setColor(Fx.border());
                t.drawLine(tx + 20, ry, tuX + 6, ry);
                t.drawLine(tuX + colW - 6, ry, tx + tw - 20, ry);
            }
        }
        t.dispose();
        return cur + th - top + 40;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        hoverRow = -1;
        if (mx >= tableX && mx <= tableX + tableW) {
            for (int i = 0; i < rows.size(); i++) {
                int ry = firstRowY + i * rowH;
                if (my >= ry && my < ry + rowH) hoverRow = i;
            }
        }
        return false;
    }
}
