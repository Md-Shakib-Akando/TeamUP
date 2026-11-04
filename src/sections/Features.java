import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Features implements Section {

    private static class FeatureCard {
        String title;
        String desc;
        int type;
        Color bgTint;
        Color accentColor;
        boolean isDarkCard;
        int x, y, w, h;
        boolean hovered = false;
        final Elastic hoverLift = new Elastic(0f, 160f, 12f);

        FeatureCard(String title, String desc, int type, Color bgTint, Color accentColor, boolean isDarkCard) {
            this.title = title;
            this.desc = desc;
            this.type = type;
            this.bgTint = bgTint;
            this.accentColor = accentColor;
            this.isDarkCard = isDarkCard;
        }
    }

    private final List<FeatureCard> cards = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Features() {
        cards.add(new FeatureCard("Skill-Based Matching",
                "Instant algorithmic matching that snaps skill chips toward candidate profiles and computes compatibility rings.",
                0, Theme.PASTEL_PINK, Theme.BRAND_PINK, false));

        cards.add(new FeatureCard("Smart Search & Filters",
                "Filter pills toggle with an elastic pop across competencies, project categories, and availability.",
                1, Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, false));

        cards.add(new FeatureCard("Project & Task Management",
                "Task cards drift across an interactive mini Kanban board with live milestone tracking.",
                2, Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, false));

        cards.add(new FeatureCard("Team Messaging & Alerts",
                "Typing bubbles and an animated notification bell keep collaborators in sync without context switching.",
                3, Theme.PASTEL_MINT, Theme.ACCENT_MINT, false));

        cards.add(new FeatureCard("Ratings & Reviews",
                "Stars fill one by one after project wrap-up, building a verifiable reputation score.",
                4, Theme.PASTEL_SUNNY, Theme.ACCENT_SUNNY, false));

        cards.add(new FeatureCard("Admin Management Panel",
                "Live performance bars animate up to monitor platform health, roles, and safety moderation.",
                5, Theme.INK_DARK, Color.WHITE, true));

        cards.add(new FeatureCard("Secure by Default",
                "Animated closing cryptographic lock: PBKDF2 salted password hashing and role-based access control.",
                6, Theme.PASTEL_SKY, Theme.ACCENT_SKY, false));

        reveal.trigger();
    }

    @Override
    public String getId() {
        return "features";
    }

    @Override
    public int getHeight(int width) {
        if (width < 700) {
            return 1780;
        } else if (width < 1100) {
            return 1160;
        } else {
            return 820;
        }
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (FeatureCard c : cards) {
            c.hoverLift.setTarget(c.hovered ? 6f : 0f);
            c.hoverLift.update(dt);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int topY = y + 24;
        Draw.drawSectionBadge(g2, "CORE CAPABILITIES", padX, topY, Theme.BRAND_PINK);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int titleY = topY + 44 + tfm.getAscent();
        g2.drawString("Everything you need to ship together", padX, titleY);

        g2.setFont(Theme.fontBody());
        g2.setColor(Theme.textSecondary());
        g2.drawString("Built-in intelligent matching, Kanban task workflows, team messaging, and immutable peer reviews in one unified workspace.", padX, titleY + 22);

        int gridTop = titleY + 48;
        int gap = 20;
        int totalW = width - padX * 2;

        if (width >= 1100) {
            // Bento Grid: 3 rows
            int row1H = 260;
            int card1W = Math.round(totalW * 0.58f) - gap / 2;
            int card2W = totalW - card1W - gap;

            FeatureCard c0 = cards.get(0);
            c0.x = padX; c0.y = gridTop; c0.w = card1W; c0.h = row1H;
            paintBentoCard(g2, c0);

            FeatureCard c1 = cards.get(1);
            c1.x = padX + card1W + gap; c1.y = gridTop; c1.w = card2W; c1.h = row1H;
            paintBentoCard(g2, c1);

            int row2Top = gridTop + row1H + gap;
            int row2H = 210;
            int col3W = (totalW - gap * 2) / 3;

            FeatureCard c2 = cards.get(2);
            c2.x = padX; c2.y = row2Top; c2.w = col3W; c2.h = row2H;
            paintBentoCard(g2, c2);

            FeatureCard c3 = cards.get(3);
            c3.x = padX + col3W + gap; c3.y = row2Top; c3.w = col3W; c3.h = row2H;
            paintBentoCard(g2, c3);

            FeatureCard c4 = cards.get(4);
            c4.x = padX + (col3W + gap) * 2; c4.y = row2Top; c4.w = col3W; c4.h = row2H;
            paintBentoCard(g2, c4);

            int row3Top = row2Top + row2H + gap;
            int row3H = 170;
            int card5W = Math.round(totalW * 0.38f);
            int card6W = totalW - card5W - gap;

            FeatureCard c5 = cards.get(5);
            c5.x = padX; c5.y = row3Top; c5.w = card5W; c5.h = row3H;
            paintBentoCard(g2, c5);

            FeatureCard c6 = cards.get(6);
            c6.x = padX + card5W + gap; c6.y = row3Top; c6.w = card6W; c6.h = row3H;
            paintBentoCard(g2, c6);

        } else if (width >= 700) {
            int colW = (totalW - gap) / 2;
            int cardH = 220;
            int curY = gridTop;

            for (int i = 0; i < cards.size(); i++) {
                FeatureCard c = cards.get(i);
                if (i == 0) {
                    c.x = padX; c.y = curY; c.w = totalW; c.h = 250;
                    curY += 250 + gap;
                } else {
                    int idx = i - 1;
                    int col = idx % 2;
                    int row = idx / 2;
                    c.x = padX + col * (colW + gap);
                    c.y = curY + row * (cardH + gap);
                    c.w = colW;
                    c.h = cardH;
                }
                paintBentoCard(g2, c);
            }
        } else {
            int curY = gridTop;
            int cardH = 200;
            for (int i = 0; i < cards.size(); i++) {
                FeatureCard c = cards.get(i);
                c.x = padX;
                c.y = curY;
                c.w = totalW;
                c.h = (i == 0) ? 250 : cardH;
                paintBentoCard(g2, c);
                curY += c.h + gap;
            }
        }

        g2.dispose();
    }

    private void paintBentoCard(Graphics2D g, FeatureCard c) {
        int actualY = Math.round(c.y - c.hoverLift.getValue());
        int radius = 24;
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(c.x, actualY, c.w, c.h, radius, radius);

        // Soft layered shadow
        int shadowOffset = c.hovered ? 12 : 5;
        g.setColor(new Color(0, 0, 0, c.hovered ? 25 : 12));
        g.fill(new RoundRectangle2D.Float(c.x, actualY + shadowOffset, c.w, c.h, radius, radius));

        // Pastel Tint background (PRD Section 6.5)
        if (c.isDarkCard) {
            g.setColor(Theme.INK_DARK);
        } else {
            g.setColor(Theme.isDark ? Theme.cardBg() : c.bgTint);
        }
        g.fill(cardShape);

        // 1px subtle border
        g.setColor(c.isDarkCard ? new Color(255, 255, 255, 30) : Theme.cardBorder());
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cardShape);

        int pad = 24;

        // Custom micro-animations in feature cards (Motion Spec #11)
        float time = Anim.globalTime;

        if (c.type == 0) {
            // Large Pink Card: Skill chips snap toward a profile and match ring fills
            Draw.drawSparkleIcon(g, c.x + pad, actualY + pad, 36, Theme.BRAND_PINK);

            // Animated match ring
            int ringSize = 48;
            int ringX = c.x + c.w - ringSize - pad;
            int ringY = actualY + pad;
            float fillPct = Anim.pulse(0.6f, 0.96f, 2.0f);
            g.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 35));
            g.setStroke(new BasicStroke(4.0f));
            g.draw(new Ellipse2D.Float(ringX, ringY, ringSize, ringSize));
            g.setColor(Theme.BRAND_PINK);
            g.draw(new Arc2D.Float(ringX, ringY, ringSize, ringSize, 90, -360 * fillPct, Arc2D.OPEN));

        } else if (c.type == 1) {
            // Search card: filter pills toggle with elastic pop
            Draw.drawSearchIcon(g, c.x + pad, actualY + pad, 34, Theme.ACCENT_VIOLET);

        } else if (c.type == 2) {
            // Violet card: Task cards drift across a mini board
            Draw.drawTaskIcon(g, c.x + pad, actualY + pad, 34, Theme.ACCENT_VIOLET);
            float taskDrift = (float) Math.sin(time * 2.0) * 8f;
            g.setColor(new Color(Theme.ACCENT_VIOLET.getRed(), Theme.ACCENT_VIOLET.getGreen(), Theme.ACCENT_VIOLET.getBlue(), 40));
            g.fill(new RoundRectangle2D.Float(c.x + c.w - 80 + taskDrift, actualY + pad + 6, 56, 24, 8, 8));

        } else if (c.type == 3) {
            // Mint card: typing bubbles and a bell that shakes
            Draw.drawChatIcon(g, c.x + pad, actualY + pad, 34, Theme.ACCENT_MINT);
            // Bell shake
            float shake = (float) Math.sin(time * 8.0) * 3f;
            g.setColor(Theme.ACCENT_MINT);
            g.fillOval(Math.round(c.x + c.w - pad - 20 + shake), actualY + pad + 10, 10, 10);

        } else if (c.type == 4) {
            // Sunny card: stars fill one by one
            Draw.drawStarIcon(g, c.x + pad, actualY + pad, 34, new Color(0xFF, 0xC1, 0x07));
            int starsActive = (int) ((time * 1.5) % 5) + 1;
            for (int s = 0; s < 5; s++) {
                Color sc = (s < starsActive) ? new Color(0xFF, 0xC1, 0x07) : new Color(0, 0, 0, 30);
                Draw.drawStarIcon(g, c.x + c.w - 110 + s * 18, actualY + pad + 10, 14, sc);
            }

        } else if (c.type == 5) {
            // Small dark card: admin bars animate up
            Draw.drawUsersIcon(g, c.x + pad, actualY + pad, 34, Color.WHITE);
            // Animated rising bars
            int barBaseX = c.x + c.w - pad - 60;
            for (int b = 0; b < 3; b++) {
                float barH = 12f + (float) Math.sin(time * 3f + b) * 10f;
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Float(barBaseX + b * 16, actualY + pad + 30 - barH, 10, barH, 4, 4));
            }

        } else {
            // Sky card: lock closes
            Draw.drawShieldIcon(g, c.x + pad, actualY + pad, 34, Theme.ACCENT_SKY);
        }

        // Title
        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(c.isDarkCard ? Color.WHITE : Theme.textPrimary());
        int titleY = actualY + pad + 38 + 14 + tfm.getAscent();
        g.drawString(c.title, c.x + pad, titleY);

        // Description
        g.setFont(Theme.fontBody());
        FontMetrics bfm = g.getFontMetrics();
        g.setColor(c.isDarkCard ? new Color(0x94, 0xA3, 0xB8) : Theme.textSecondary());
        List<String> lines = Draw.wrapText(c.desc, bfm, c.w - pad * 2);
        int curY = titleY + 8 + bfm.getAscent();
        int lineGap = Math.round(bfm.getHeight() * 1.35f);
        for (String l : lines) {
            g.drawString(l, c.x + pad, curY);
            curY += lineGap;
        }
    }

    @Override
    public boolean onClick(int mx, int my) { return false; }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (FeatureCard c : cards) {
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
