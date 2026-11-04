import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Problem implements Section {

    private static class ProblemCard {
        String num;
        String tag;
        String title;
        String desc;
        String metric;
        String metricLabel;
        int iconType; // 0=Search, 1=Users, 2=Shield
        Color accent;
        float hoverLift = 0f;
        boolean hovered = false;
        int x, y, w, h;

        ProblemCard(String num, String tag, String title, String desc, String metric, String metricLabel, int iconType, Color accent) {
            this.num = num;
            this.tag = tag;
            this.title = title;
            this.desc = desc;
            this.metric = metric;
            this.metricLabel = metricLabel;
            this.iconType = iconType;
            this.accent = accent;
        }
    }

    private final List<ProblemCard> cards = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Problem() {
        cards.add(new ProblemCard(
                "01",
                "SKILL VISIBILITY",
                "Can't see verified skills",
                "Talent stays hidden behind silent group chats. You waste hours guessing who actually knows Java, UI/UX, or systems architecture.",
                "3.4 Days",
                "wasted asking around",
                0,
                Theme.ACCENT_CYAN
        ));
        cards.add(new ProblemCard(
                "02",
                "CHAOTIC WORKFLOW",
                "Hours lost in noisy channels",
                "Team recruiting happens across scattered WhatsApp, Discord, and campus boards without a structured pipeline or role criteria.",
                "5+ Channels",
                "juggled per project",
                1,
                Theme.ACCENT_VIOLET
        ));
        cards.add(new ProblemCard(
                "03",
                "ZERO PROOF",
                "No accountability or reputation",
                "Projects collapse with zero consequences. Freeloaders get away unnoticed while dedicated builders earn no verifiable track record.",
                "68% Teams",
                "suffer unbalanced workloads",
                2,
                Theme.BRAND_PINK
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
            return 820;
        } else if (width < 1100) {
            return 600;
        } else {
            return 490;
        }
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (ProblemCard card : cards) {
            float target = card.hovered ? 6f : 0f;
            card.hoverLift = Anim.approach(card.hoverLift, target, dt, 14f);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        // Section Eyebrow Badge
        int topY = y + 24;
        Draw.drawSectionBadge(g2, "THE CHALLENGE", padX, topY, Theme.BRAND_PINK);

        // Section Title & Subtitle
        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int titleY = topY + 44 + tfm.getAscent();
        g2.drawString("Team building shouldn't be this hard", padX, titleY);

        g2.setFont(Theme.fontBody());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(Theme.textSecondary());
        g2.drawString("Traditional group chats, cold outreach, and scattered boards lead to ghosted projects and broken promises.", padX, titleY + 22);

        // Cards layout
        int cardsTop = titleY + 46;
        int gap = 20;

        if (width < 700) {
            int cardW = width - padX * 2;
            int cardH = 200;
            for (int i = 0; i < cards.size(); i++) {
                ProblemCard c = cards.get(i);
                c.x = padX;
                c.y = cardsTop + i * (cardH + gap);
                c.w = cardW;
                c.h = cardH;
                paintCard(g2, c);
            }
        } else if (width < 1100) {
            int cardW = (width - padX * 2 - gap) / 2;
            int cardH = 210;
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
            int cardW = (width - padX * 2 - gap * 2) / 3;
            int cardH = 245;
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
        int drawY = c.y - Math.round(c.hoverLift);
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(c.x, drawY, c.w, c.h, 20, 20);

        // Glass card base with soft elevation shadow
        Draw.drawGlassCard(g, c.x, c.y, c.w, c.h, 20, c.hoverLift, c.hovered);

        // Top accent line on hover
        if (c.hoverLift > 0.5f) {
            Graphics2D gClip = (Graphics2D) g.create();
            gClip.setClip(cardShape);
            int barH = 4;
            gClip.setColor(c.accent);
            gClip.fillRect(c.x, drawY, c.w, barH);
            gClip.dispose();
        }

        // Watermark Number in top right
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        FontMetrics nfm = g.getFontMetrics();
        g.setColor(Theme.isDark ? new Color(255, 255, 255, 18) : new Color(0, 0, 0, 14));
        g.drawString(c.num, c.x + c.w - nfm.stringWidth(c.num) - 22, drawY + 32);

        // Icon Box (rounded square with soft tint)
        int iconSize = 42;
        int iconX = c.x + 22;
        int iconY = drawY + 22;
        RoundRectangle2D iconBox = new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, 12, 12);
        g.setColor(new Color(c.accent.getRed(), c.accent.getGreen(), c.accent.getBlue(), 22));
        g.fill(iconBox);
        g.setColor(new Color(c.accent.getRed(), c.accent.getGreen(), c.accent.getBlue(), 60));
        g.setStroke(new BasicStroke(1.0f));
        g.draw(iconBox);

        if (c.iconType == 0) {
            Draw.drawSearchIcon(g, iconX + 10, iconY + 10, 22, c.accent);
        } else if (c.iconType == 1) {
            Draw.drawUsersIcon(g, iconX + 10, iconY + 10, 22, c.accent);
        } else {
            Draw.drawShieldIcon(g, iconX + 10, iconY + 10, 22, c.accent);
        }

        // Micro Category Tag
        int tagX = iconX + iconSize + 14;
        g.setFont(Theme.fontSmallBold());
        g.setColor(c.accent);
        g.drawString(c.tag, tagX, iconY + 16);

        // Card Title
        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int titleY = iconY + iconSize + 22;
        g.drawString(c.title, c.x + 22, titleY);

        // Card Description
        g.setFont(Theme.fontBody());
        FontMetrics bfm = g.getFontMetrics();
        g.setColor(Theme.textSecondary());
        List<String> lines = Draw.wrapText(c.desc, bfm, c.w - 44);
        int curY = titleY + 8 + bfm.getAscent();
        int lineGap = Math.round(bfm.getHeight() * 1.3f);
        for (String l : lines) {
            g.drawString(l, c.x + 22, curY);
            curY += lineGap;
        }

        // Bottom Metric Ribbon
        int ribbonY = drawY + c.h - 36;
        g.setColor(Theme.isDark ? new Color(255, 255, 255, 12) : new Color(0, 0, 0, 10));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(c.x + 22, ribbonY - 8, c.x + c.w - 22, ribbonY - 8);

        g.setFont(Theme.fontSmallBold());
        g.setColor(c.accent);
        g.drawString(c.metric, c.x + 22, ribbonY + 12);
        FontMetrics mfm = g.getFontMetrics();
        int metW = mfm.stringWidth(c.metric);

        g.setFont(Theme.fontSmall());
        g.setColor(Theme.textMuted());
        g.drawString(" — " + c.metricLabel, c.x + 22 + metW, ribbonY + 12);
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
