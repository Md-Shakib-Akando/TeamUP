import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Problem implements Section {

    private static class ProblemCard {
        String title;
        String desc;
        int iconType; // 0=Search, 1=Users, 2=Shield
        float hoverLift = 0f;
        boolean hovered = false;
        int x, y, w, h;

        ProblemCard(String title, String desc, int iconType) {
            this.title = title;
            this.desc = desc;
            this.iconType = iconType;
        }
    }

    private final List<ProblemCard> cards = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Problem() {
        cards.add(new ProblemCard(
                "Can't see who has the skills",
                "Talent is hidden behind silent group chats. You waste days asking who knows Java, UI/UX, or database design without verified profiles.",
                0
        ));
        cards.add(new ProblemCard(
                "Hours lost asking around",
                "Recruiting happens across scattered Discord channels, WhatsApp groups, and campus boards instead of one streamlined pipeline.",
                1
        ));
        cards.add(new ProblemCard(
                "No proof of who is reliable",
                "Projects end with zero accountability. Freeloaders get away unnoticed while dedicated teammates receive no reputation or rating proof.",
                2
        ));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "problem";
    }

    @Override
    public int getHeight(int width) {
        if (width < 700) {
            return 720;
        } else if (width < 1100) {
            return 560;
        } else {
            return 440;
        }
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (ProblemCard card : cards) {
            float target = card.hovered ? 4f : 0f;
            card.hoverLift = Anim.approach(card.hoverLift, target, dt, 12f);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        // Section Eyebrow & Title
        int topY = y + 20;
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.BRAND_PINK);
        g2.drawString("THE CHALLENGE", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Team building shouldn't be this hard", padX, topY + 14 + tfm.getAscent() + 10);

        // Cards layout
        int cardsTop = topY + 80;
        int gap = 20;

        if (width < 700) {
            // 1 column
            int cardW = width - padX * 2;
            int cardH = 170;
            for (int i = 0; i < cards.size(); i++) {
                ProblemCard c = cards.get(i);
                c.x = padX;
                c.y = cardsTop + i * (cardH + gap);
                c.w = cardW;
                c.h = cardH;
                paintCard(g2, c);
            }
        } else if (width < 1100) {
            // 2 columns (2 on row 1, 1 on row 2)
            int cardW = (width - padX * 2 - gap) / 2;
            int cardH = 180;
            for (int i = 0; i < cards.size(); i++) {
                ProblemCard c = cards.get(i);
                int col = i % 2;
                int row = i / 2;
                c.x = padX + col * (cardW + gap);
                c.y = cardsTop + row * (cardH + gap);
                c.w = (i == 2) ? (width - padX * 2) : cardW;
                c.h = cardH;
                paintCard(g2, c);
            }
        } else {
            // 3 columns
            int cardW = (width - padX * 2 - gap * 2) / 3;
            int cardH = 220;
            for (int i = 0; i < cards.size(); i++) {
                ProblemCard c = cards.get(i);
                c.x = padX + i * (cardW + gap);
                c.y = cardsTop;
                c.w = cardW;
                c.h = cardH;
                paintCard(g2, c);
            }
        }

        g2.dispose();
    }

    private void paintCard(Graphics2D g, ProblemCard c) {
        Draw.drawGlassCard(g, c.x, c.y, c.w, c.h, 20, c.hoverLift, c.hovered);

        int iconSize = 38;
        int iconX = c.x + 24;
        int iconY = c.y - Math.round(c.hoverLift) + 24;

        // Icon circular badge
        g.setColor(Theme.isDark ? new Color(255, 255, 255, 12) : new Color(0, 0, 0, 8));
        g.fillOval(iconX, iconY, iconSize, iconSize);

        if (c.iconType == 0) {
            Draw.drawSearchIcon(g, iconX + 8, iconY + 8, 22, Theme.ACCENT_CYAN);
        } else if (c.iconType == 1) {
            Draw.drawUsersIcon(g, iconX + 8, iconY + 8, 22, Theme.ACCENT_VIOLET);
        } else {
            Draw.drawShieldIcon(g, iconX + 8, iconY + 8, 22, Theme.HIGHLIGHT_WARM);
        }

        // Title
        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int titleY = iconY + iconSize + 18 + tfm.getAscent();
        g.drawString(c.title, c.x + 24, titleY);

        // Description
        g.setFont(Theme.fontBody());
        FontMetrics bfm = g.getFontMetrics();
        g.setColor(Theme.textSecondary());
        List<String> lines = Draw.wrapText(c.desc, bfm, c.w - 48);
        int curY = titleY + 12 + bfm.getAscent();
        int lineGap = Math.round(bfm.getHeight() * 1.35f);
        for (String l : lines) {
            g.drawString(l, c.x + 24, curY);
            curY += lineGap;
        }
    }

    @Override
    public boolean onClick(int mx, int my) { return false; }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (ProblemCard c : cards) {
            boolean was = c.hovered;
            c.hovered = (mx >= c.x && mx <= c.x + c.w && my >= c.y && my <= c.y + c.h);
            if (c.hovered != was) changed = true;
        }
        return changed;
    }

    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }
    @Override
    public boolean onKey(KeyEvent e) { return false; }
}

