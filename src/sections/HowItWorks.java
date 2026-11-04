import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class HowItWorks implements Section {

    private static class StepItem {
        int num;
        String tag;
        String title;
        String desc;
        Color accent;
        int x, y, w, h;
        boolean hovered = false;
        float hoverLift = 0f;

        StepItem(int num, String tag, String title, String desc, Color accent) {
            this.num = num;
            this.tag = tag;
            this.title = title;
            this.desc = desc;
            this.accent = accent;
        }
    }

    private final List<StepItem> steps = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public HowItWorks() {
        steps.add(new StepItem(
                1,
                "STEP 01",
                "Create your profile",
                "Highlight verified skills, bio, project history, and GitHub or portfolio links.",
                Theme.ACCENT_CYAN
        ));
        steps.add(new StepItem(
                2,
                "STEP 02",
                "Publish or discover",
                "Explore open initiatives or publish your own requirements, role openings, and technical stack.",
                Theme.ACCENT_VIOLET
        ));
        steps.add(new StepItem(
                3,
                "STEP 03",
                "Match and invite",
                "Receive algorithmic compatibility scores, inspect complementary skill sets, and send invitations.",
                Theme.BRAND_PINK
        ));
        steps.add(new StepItem(
                4,
                "STEP 04",
                "Collaborate and rate",
                "Coordinate Kanban milestones, chat instantly, and exchange verifiable peer reputation upon completion.",
                Theme.SUCCESS_GREEN
        ));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "how-it-works";
    }

    @Override
    public int getHeight(int width) {
        if (width < 880) {
            return 980;
        } else {
            return 540;
        }
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (StepItem s : steps) {
            float target = s.hovered ? 6f : 0f;
            s.hoverLift = Anim.approach(s.hoverLift, target, dt, 14f);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        // Header Badge
        int topY = y + 24;
        Draw.drawSectionBadge(g2, "FOUR SIMPLE STEPS", padX, topY, Theme.ACCENT_VIOLET);

        // Title & Subtitle
        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int titleY = topY + 44 + tfm.getAscent();
        g2.drawString("Four steps from idea to dream team", padX, titleY);

        g2.setFont(Theme.fontBody());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(Theme.textSecondary());
        g2.drawString("A transparent, milestone-driven workflow designed to eliminate freeloading and surface the right talent immediately.", padX, titleY + 22);

        boolean isHorizontal = width >= 880;
        int stepsTop = titleY + 48;

        if (isHorizontal) {
            int gap = 20;
            int stepW = (width - padX * 2 - gap * 3) / 4;
            int stepH = 280;

            // Connecting timeline line behind step badges
            int lineY = stepsTop + 24;
            g2.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 6}, 0));
            g2.drawLine(padX + 30, lineY, width - padX - 30, lineY);

            for (int i = 0; i < steps.size(); i++) {
                StepItem s = steps.get(i);
                s.x = padX + i * (stepW + gap);
                s.y = stepsTop;
                s.w = stepW;
                s.h = stepH;
                paintStepCard(g2, s, true);
            }
        } else {
            int stepH = 180;
            int gap = 18;
            int stepW = width - padX * 2;

            for (int i = 0; i < steps.size(); i++) {
                StepItem s = steps.get(i);
                s.x = padX;
                s.y = stepsTop + i * (stepH + gap);
                s.w = stepW;
                s.h = stepH;
                paintStepCard(g2, s, false);
            }
        }

        g2.dispose();
    }

    private void paintStepCard(Graphics2D g, StepItem s, boolean isHorizontal) {
        Draw.drawGlassCard(g, s.x, s.y, s.w, s.h, 20, s.hoverLift, s.hovered);

        int actualY = s.y - Math.round(s.hoverLift);
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(s.x, actualY, s.w, s.h, 20, 20);

        // Top accent highlight on hover
        if (s.hoverLift > 0.5f) {
            Graphics2D gClip = (Graphics2D) g.create();
            gClip.setClip(cardShape);
            gClip.setColor(s.accent);
            gClip.fillRect(s.x, actualY, s.w, 4);
            gClip.dispose();
        }

        // Step number badge (gradient circle)
        int badgeSize = 42;
        int badgeX = s.x + 20;
        int badgeY = actualY + 20;

        LinearGradientPaint p = new LinearGradientPaint(
                badgeX, badgeY, badgeX + badgeSize, badgeY + badgeSize,
                new float[]{0f, 1f},
                new Color[]{s.accent, s.accent.darker()}
        );
        Paint old = g.getPaint();
        g.setPaint(p);
        g.fill(new Ellipse2D.Float(badgeX, badgeY, badgeSize, badgeSize));
        g.setPaint(old);

        // Glowing outer ring on hover
        if (s.hovered) {
            g.setColor(new Color(s.accent.getRed(), s.accent.getGreen(), s.accent.getBlue(), 80));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Ellipse2D.Float(badgeX - 3, badgeY - 3, badgeSize + 6, badgeSize + 6));
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics nfm = g.getFontMetrics();
        String numStr = "0" + s.num;
        int nx = badgeX + (badgeSize - nfm.stringWidth(numStr)) / 2;
        int ny = badgeY + (badgeSize - nfm.getHeight()) / 2 + nfm.getAscent();
        g.drawString(numStr, nx, ny);

        // Tag
        g.setFont(Theme.fontSmallBold());
        g.setColor(s.accent);
        if (isHorizontal) {
            g.drawString(s.tag, badgeX + badgeSize + 14, badgeY + 25);
        } else {
            g.drawString(s.tag, badgeX + badgeSize + 16, badgeY + 16);
        }

        // Title
        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int titleX = isHorizontal ? (s.x + 20) : (badgeX + badgeSize + 16);
        int titleY = isHorizontal ? (badgeY + badgeSize + 22 + tfm.getAscent()) : (badgeY + 38);
        g.drawString(s.title, titleX, titleY);

        // Description
        g.setFont(Theme.fontBody());
        FontMetrics bfm = g.getFontMetrics();
        g.setColor(Theme.textSecondary());
        int maxW = isHorizontal ? (s.w - 40) : (s.w - (titleX - s.x) - 20);
        List<String> lines = Draw.wrapText(s.desc, bfm, maxW);
        int curY = isHorizontal ? (titleY + 10 + bfm.getAscent()) : (titleY + 16 + bfm.getAscent());
        int lineGap = Math.round(bfm.getHeight() * 1.3f);
        for (String l : lines) {
            g.drawString(l, titleX, curY);
            curY += lineGap;
        }

        // Mini preview widget at the bottom of the card
        if (isHorizontal) {
            int widgetY = actualY + s.h - 48;
            int widgetX = s.x + 20;
            int widgetW = s.w - 40;
            int widgetH = 30;

            RoundRectangle2D wBox = new RoundRectangle2D.Float(widgetX, widgetY, widgetW, widgetH, 8, 8);
            g.setColor(Theme.isDark ? new Color(255, 255, 255, 8) : new Color(0, 0, 0, 6));
            g.fill(wBox);

            g.setFont(Theme.fontSmallBold());
            FontMetrics wfm = g.getFontMetrics();

            if (s.num == 1) {
                g.setColor(Theme.SUCCESS_GREEN);
                g.drawString("✓ Verified GitHub", widgetX + 10, widgetY + 19);
                g.setColor(Theme.textMuted());
                g.drawString("· Java, UI/UX", widgetX + 115, widgetY + 19);
            } else if (s.num == 2) {
                g.setColor(Theme.ACCENT_VIOLET);
                g.drawString("⚡ Open Project", widgetX + 10, widgetY + 19);
                g.setColor(Theme.textMuted());
                g.drawString("· 2 Roles Open", widgetX + 105, widgetY + 19);
            } else if (s.num == 3) {
                g.setColor(Theme.BRAND_PINK);
                g.drawString("★ 96% Match", widgetX + 10, widgetY + 19);
                g.setColor(Theme.textMuted());
                g.drawString("· Optimal Team", widgetX + 98, widgetY + 19);
            } else {
                g.setColor(new Color(0xFF, 0xC1, 0x07));
                g.drawString("★★★★★ 4.9", widgetX + 10, widgetY + 19);
                g.setColor(Theme.textMuted());
                g.drawString("· Sprint Done", widgetX + 96, widgetY + 19);
            }
        }
    }

    @Override
    public boolean onClick(int mx, int my) { return false; }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (StepItem s : steps) {
            boolean was = s.hovered;
            s.hovered = (mx >= s.x && mx <= s.x + s.w && my >= s.y && my <= s.y + s.h);
            if (s.hovered != was) changed = true;
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
