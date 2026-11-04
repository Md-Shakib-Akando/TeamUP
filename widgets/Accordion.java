import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class Accordion {
    public int x, y, width;
    public String question;
    public String answer;
    public boolean expanded = false;
    public float expandProgress = 0f; // 0 to 1
    public boolean hovered = false;

    public Accordion(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public void update(float dt) {
        float target = expanded ? 1f : 0f;
        expandProgress = Anim.approach(expandProgress, target, dt, 8.0f);
    }

    public int getHeight(int w, FontMetrics titleFm, FontMetrics bodyFm) {
        int headerH = 64;
        if (expandProgress <= 0.001f) {
            return headerH;
        }
        int textMaxW = w - 48;
        List<String> lines = Draw.wrapText(answer, bodyFm, textMaxW);
        int contentH = lines.size() * Math.round(bodyFm.getHeight() * 1.5f) + 24;
        return headerH + Math.round(contentH * expandProgress);
    }

    public void paint(Graphics2D g, int x, int y, int w) {
        this.x = x;
        this.y = y;
        this.width = w;

        FontMetrics titleFm = g.getFontMetrics(Theme.fontCardTitle());
        FontMetrics bodyFm = g.getFontMetrics(Theme.fontBody());
        int currentH = getHeight(w, titleFm, bodyFm);
        int radius = 16;

        // Card surface
        Draw.drawGlassCard(g, x, y, w, currentH, radius, hovered ? 2f : 0f, expanded);

        // Header
        int headerH = 64;
        g.setFont(Theme.fontCardTitle());
        g.setColor(expanded ? Theme.ACCENT_CYAN : (hovered ? Theme.textPrimary() : Theme.textPrimary()));
        int qY = y + (headerH - titleFm.getHeight()) / 2 + titleFm.getAscent();
        g.drawString(question, x + 24, qY);

        // Chevron icon
        int iconSize = 16;
        int iconX = x + w - 24 - iconSize;
        int iconY = y + (headerH - iconSize) / 2;
        float angle = expandProgress * (float) Math.PI;
        Draw.drawChevronDown(g, iconX, iconY, iconSize, Theme.textSecondary(), angle);

        // Answer content
        if (expandProgress > 0.02f) {
            Shape oldClip = g.getClip();
            g.setClip(new Rectangle(x, y + headerH, w, currentH - headerH));

            g.setFont(Theme.fontBody());
            g.setColor(Theme.textSecondary());
            int textMaxW = w - 48;
            List<String> lines = Draw.wrapText(answer, bodyFm, textMaxW);
            int lineH = Math.round(bodyFm.getHeight() * 1.5f);
            int ty = y + headerH + 6 + bodyFm.getAscent();

            for (String line : lines) {
                g.drawString(line, x + 24, ty);
                ty += lineH;
            }

            g.setClip(oldClip);
        }
    }

    public boolean containsHeader(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + 64;
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

