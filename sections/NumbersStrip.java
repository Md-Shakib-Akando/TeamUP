import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class NumbersStrip implements Section {

    private static class StatItem {
        String number;
        String label;
        String subtext;

        StatItem(String number, String label, String subtext) {
            this.number = number;
            this.label = label;
            this.subtext = subtext;
        }
    }

    private final List<StatItem> stats = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public NumbersStrip() {
        stats.add(new StatItem("8", "Architected Modules", "End-to-end modular platform design"));
        stats.add(new StatItem("4", "Distinct User Roles", "Leader, Member, Admin, Visitor"));
        stats.add(new StatItem("< 2s", "Target Search Speed", "Ultra-fast indexed query latency"));
        stats.add(new StatItem("100%", "Pure Core Java", "High performance without heavy frameworks"));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "numbers";
    }

    @Override
    public int getHeight(int width) {
        return width < 700 ? 480 : (width < 1100 ? 280 : 180);
    }

    private float countProgress = 0f;

    @Override
    public void update(float dt) {
        reveal.update(dt);
        countProgress = Anim.approach(countProgress, 1.0f, dt, 2.5f);
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int totalW = width - padX * 2;
        int gap = 20;

        if (width >= 1100) {
            // 4 columns
            int colW = (totalW - gap * 3) / 4;
            int cardH = 140;
            int topY = y + 20;

            for (int i = 0; i < stats.size(); i++) {
                StatItem s = stats.get(i);
                int cx = padX + i * (colW + gap);
                paintStatCard(g2, cx, topY, colW, cardH, s);
            }
        } else if (width >= 700) {
            // 2x2 grid
            int colW = (totalW - gap) / 2;
            int cardH = 120;
            int topY = y + 10;

            for (int i = 0; i < stats.size(); i++) {
                StatItem s = stats.get(i);
                int col = i % 2;
                int row = i / 2;
                int cx = padX + col * (colW + gap);
                int cy = topY + row * (cardH + gap);
                paintStatCard(g2, cx, cy, colW, cardH, s);
            }
        } else {
            // 1 column
            int cardH = 100;
            int topY = y + 10;
            for (int i = 0; i < stats.size(); i++) {
                StatItem s = stats.get(i);
                int cy = topY + i * (cardH + 12);
                paintStatCard(g2, padX, cy, totalW, cardH, s);
            }
        }

        g2.dispose();
    }

    private void paintStatCard(Graphics2D g, int x, int y, int w, int h, StatItem s) {
        Draw.drawGlassCard(g, x, y, w, h, 18, 0f, false);

        // Big metric number with count-up animation (Motion Spec #12)
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(38 * Theme.fontScale)));
        FontMetrics nfm = g.getFontMetrics();

        String displayNum = s.number;
        if (Theme.motionEnabled) {
            if ("8".equals(s.number)) {
                displayNum = String.valueOf(Math.round(8 * countProgress));
            } else if ("4".equals(s.number)) {
                displayNum = String.valueOf(Math.round(4 * countProgress));
            } else if ("100%".equals(s.number)) {
                displayNum = Math.round(100 * countProgress) + "%";
            }
        }

        int numW = nfm.stringWidth(displayNum);
        LinearGradientPaint p = new LinearGradientPaint(
                x + 24, y + 20, x + 24 + numW + 10, y + 20,
                new float[]{0f, 1f},
                new Color[]{Theme.BRAND_PINK, new Color(0xFF, 0x8A, 0x5B)}
        );
        Paint old = g.getPaint();
        g.setPaint(p);
        int numY = y + 20 + nfm.getAscent();
        g.drawString(displayNum, x + 24, numY);
        g.setPaint(old);

        // Label
        g.setFont(Theme.fontBodyBold());
        FontMetrics lfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int ly = numY + 10 + lfm.getAscent();
        g.drawString(s.label, x + 24, ly);

        // Subtext
        g.setFont(Theme.fontSmall());
        g.setColor(Theme.textMuted());
        g.drawString(s.subtext, x + 24, ly + 18);
    }

    @Override
    public boolean onClick(int mx, int my) { return false; }
    @Override
    public boolean onMouseMove(int mx, int my) { return false; }
    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }
    @Override
    public boolean onKey(KeyEvent e) { return false; }
}

