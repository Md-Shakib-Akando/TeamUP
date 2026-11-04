import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class HowItWorks implements Section {

    private static class StepItem {
        int num;
        String title;
        String desc;
        int x, y, w, h;
        boolean hovered = false;
        float hoverLift = 0f;

        StepItem(int num, String title, String desc) {
            this.num = num;
            this.title = title;
            this.desc = desc;
        }
    }

    private final List<StepItem> steps = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public HowItWorks() {
        steps.add(new StepItem(1, "Create your profile", "Highlight verified skills, bio, project history, and GitHub/portfolio links."));
        steps.add(new StepItem(2, "Post or find a project", "Explore open initiatives or publish your own requirements, role openings, and stack."));
        steps.add(new StepItem(3, "Match and invite", "Receive algorithmic member matches, inspect compatibility score, and send requests."));
        steps.add(new StepItem(4, "Collaborate and rate", "Coordinate Kanban tasks, instant team messages, and exchange feedback upon completion."));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "how-it-works";
    }

    @Override
    public int getHeight(int width) {
        if (width < 880) {
            return 820;
        } else {
            return 440;
        }
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (StepItem s : steps) {
            float target = s.hovered ? 4f : 0f;
            s.hoverLift = Anim.approach(s.hoverLift, target, dt, 12f);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        // Header
        int topY = y + 20;
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.BRAND_PINK);
        g2.drawString("HOW IT WORKS", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Four steps from idea to dream team", padX, topY + 14 + tfm.getAscent() + 10);

        boolean isHorizontal = width >= 880;
        int stepsTop = topY + 90;

        if (isHorizontal) {
            // Horizontal layout
            int gap = 20;
            int stepW = (width - padX * 2 - gap * 3) / 4;
            int stepH = 240;

            // Connecting timeline line behind step badges
            int lineY = stepsTop + 24;
            g2.setColor(Theme.cardBorder());
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(padX + 24, lineY, width - padX - 24, lineY);

            for (int i = 0; i < steps.size(); i++) {
                StepItem s = steps.get(i);
                s.x = padX + i * (stepW + gap);
                s.y = stepsTop;
                s.w = stepW;
                s.h = stepH;
                paintStepCard(g2, s, true);
            }
        } else {
            // Vertical timeline
            int stepH = 140;
            int gap = 16;
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

        // Step number badge (gradient circle)
        int badgeSize = 40;
        int badgeX = s.x + 20;
        int badgeY = actualY + 20;

        LinearGradientPaint p = new LinearGradientPaint(
                badgeX, badgeY, badgeX + badgeSize, badgeY + badgeSize,
                new float[]{0f, 1f},
                new Color[]{Theme.BRAND_PINK, new Color(0xFF, 0x8A, 0x5B)}
        );
        Paint old = g.getPaint();
        g.setPaint(p);
        g.fill(new Ellipse2D.Float(badgeX, badgeY, badgeSize, badgeSize));
        g.setPaint(old);

        g.setColor(Color.WHITE);
        g.setFont(Theme.fontCardTitle());
        FontMetrics nfm = g.getFontMetrics();
        String numStr = String.valueOf(s.num);
        int nx = badgeX + (badgeSize - nfm.stringWidth(numStr)) / 2;
        int ny = badgeY + (badgeSize - nfm.getHeight()) / 2 + nfm.getAscent();
        g.drawString(numStr, nx, ny);

        // Title
        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int titleX = isHorizontal ? (s.x + 20) : (badgeX + badgeSize + 18);
        int titleY = isHorizontal ? (badgeY + badgeSize + 22 + tfm.getAscent()) : (badgeY + (badgeSize - tfm.getHeight()) / 2 + tfm.getAscent());
        g.drawString(s.title, titleX, titleY);

        // Description
        g.setFont(Theme.fontBody());
        FontMetrics bfm = g.getFontMetrics();
        g.setColor(Theme.textSecondary());
        int maxW = isHorizontal ? (s.w - 40) : (s.w - (titleX - s.x) - 20);
        List<String> lines = Draw.wrapText(s.desc, bfm, maxW);
        int curY = isHorizontal ? (titleY + 12 + bfm.getAscent()) : (titleY + 18 + bfm.getAscent());
        int lineGap = Math.round(bfm.getHeight() * 1.35f);
        for (String l : lines) {
            g.drawString(l, titleX, curY);
            curY += lineGap;
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

