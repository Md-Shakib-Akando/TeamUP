import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class MatchDemo implements Section {

    public static class Candidate {
        public String name;
        public String role;
        public String initials;
        public Color c1, c2;
        public Set<String> skills = new HashSet<>();
        public float currentMatch = 0.85f;
        public float targetMatch = 0.85f;
        public float displayY = 0f;
        public float targetY = 0f;
        public double rating;

        public Candidate(String name, String role, String initials, Color c1, Color c2, double rating, String... sk) {
            this.name = name;
            this.role = role;
            this.initials = initials;
            this.c1 = c1;
            this.c2 = c2;
            this.rating = rating;
            Collections.addAll(skills, sk);
        }
    }

    private final List<Chip> skillChips = new ArrayList<>();
    private final List<Candidate> candidates = new ArrayList<>();
    private final Set<String> selectedSkills = new HashSet<>();

    private final Anim.RevealState reveal = new Anim.RevealState();

    public MatchDemo() {
        candidates.add(new Candidate("Sarah Lin", "Backend & Systems", "SL",
                Theme.ACCENT_VIOLET, Theme.ACCENT_CYAN, 4.9, "Java", "MySQL", "Testing", "Cloud"));

        candidates.add(new Candidate("David Chen", "UI/UX & Product Design", "DC",
                new Color(0xF4, 0x3F, 0x5E), Theme.HIGHLIGHT_WARM, 5.0, "Design", "React"));

        candidates.add(new Candidate("Priya Patel", "Full Stack Engineer", "PP",
                new Color(0x10, 0xB9, 0x81), Theme.ACCENT_CYAN, 4.8, "Java", "Python", "Cloud", "Testing"));

        candidates.add(new Candidate("Marcus Vance", "Frontend & Mobile", "MV",
                new Color(0x8B, 0x5C, 0xF6), new Color(0xEC, 0x48, 0x99), 4.7, "Design", "React", "Java"));

        String[] allSkills = {"Java", "Design", "MySQL", "Testing", "React", "Python", "Cloud"};
        selectedSkills.add("Java");
        selectedSkills.add("MySQL");

        for (String s : allSkills) {
            boolean init = selectedSkills.contains(s);
            skillChips.add(new Chip(s, init, () -> toggleSkill(s)));
        }

        recomputeMatches(false);
        reveal.trigger();
    }

    private void toggleSkill(String skill) {
        if (selectedSkills.contains(skill)) {
            selectedSkills.remove(skill);
        } else {
            selectedSkills.add(skill);
        }
        recomputeMatches(true);
    }

    private void recomputeMatches(boolean animate) {
        if (selectedSkills.isEmpty()) {
            for (Candidate c : candidates) {
                c.targetMatch = 0.45f;
            }
        } else {
            for (Candidate c : candidates) {
                int matched = 0;
                for (String sk : selectedSkills) {
                    if (c.skills.contains(sk)) {
                        matched++;
                    }
                }
                float ratio = (float) matched / (float) selectedSkills.size();
                float match = Math.min(0.99f, Math.max(0.25f, ratio * 0.82f + 0.15f));
                c.targetMatch = match;
            }
        }

        candidates.sort((a, b) -> Float.compare(b.targetMatch, a.targetMatch));

        if (!animate) {
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                c.currentMatch = c.targetMatch;
                c.displayY = i * 90f;
                c.targetY = i * 90f;
            }
        } else {
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                c.targetY = i * 90f;
            }
        }
    }

    @Override
    public String getId() {
        return "demo";
    }

    @Override
    public int getHeight(int width) {
        return width < 700 ? 760 : 660;
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (Candidate c : candidates) {
            c.currentMatch = Anim.approach(c.currentMatch, c.targetMatch, dt, 8.0f);
            c.displayY = Anim.approach(c.displayY, c.targetY, dt, 10.0f);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int topY = y + 20;

        // Eyebrow & Title
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.ACCENT_CYAN);
        g2.drawString("INTERACTIVE DEMO", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Experience real-time skill matching", padX, topY + 14 + tfm.getAscent() + 10);

        g2.setFont(Theme.fontBody());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(Theme.textSecondary());
        g2.drawString("Click skill chips below to simulate a live project search and observe candidate re-ranking (Sample Data).",
                padX, topY + 80);

        // Skill Chips Bar
        int chipsY = topY + 108;
        int chipX = padX;
        for (Chip chip : skillChips) {
            chip.setBounds(chipX, chipsY, 84, 32);
            chip.paint(g2);
            chipX += 92;
        }

        // Main Demo Container Card
        int containerY = chipsY + 54;
        int containerW = width - padX * 2;
        int containerH = (candidates.size() * 90) + 40;
        Draw.drawGlassCard(g2, padX, containerY, containerW, containerH, 24, 0f, true);

        // Sample Data Badge
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.textMuted());
        String badge = "LIVE ALGORITHM PREVIEW  ·  SAMPLE DATA";
        g2.drawString(badge, padX + 24, containerY + 28);

        // Candidates list with animated positions
        int listTop = containerY + 44;
        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);
            int cardY = listTop + Math.round(c.displayY);
            paintCandidateRow(g2, padX + 20, cardY, containerW - 40, c, i == 0);
        }

        g2.dispose();
    }

    private void paintCandidateRow(Graphics2D g, int x, int y, int w, Candidate c, boolean isTopMatch) {
        int rowH = 76;
        RoundRectangle2D rowShape = new RoundRectangle2D.Float(x, y, w, rowH, 16, 16);

        // Subtle row surface with dynamic highlight for top candidate
        if (isTopMatch) {
            g.setColor(Theme.isDark ? new Color(124, 92, 255, 18) : new Color(124, 92, 255, 12));
            g.fill(rowShape);
            g.setColor(new Color(Theme.ACCENT_VIOLET.getRed(), Theme.ACCENT_VIOLET.getGreen(), Theme.ACCENT_VIOLET.getBlue(), 90));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(rowShape);
        } else {
            g.setColor(Theme.isDark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 6));
            g.fill(rowShape);
            g.setColor(Theme.cardBorder());
            g.setStroke(new BasicStroke(1.0f));
            g.draw(rowShape);
        }

        // Avatar
        int avSize = 44;
        int avX = x + 16;
        int avY = y + (rowH - avSize) / 2;
        Draw.drawAvatar(g, avX, avY, avSize, c.initials, c.c1, c.c2);

        // Name & Rating
        g.setFont(Theme.fontCardTitle());
        FontMetrics nfm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int tx = avX + avSize + 16;
        int ty = y + 26;
        g.drawString(c.name, tx, ty);

        // Top match pill badge
        if (isTopMatch) {
            int tagX = tx + nfm.stringWidth(c.name) + 12;
            int tagH = 18;
            int tagW = 76;
            g.setColor(Theme.ACCENT_VIOLET);
            g.fill(new RoundRectangle2D.Float(tagX, ty - 14, tagW, tagH, tagH, tagH));
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.drawString("TOP MATCH", tagX + 8, ty - 1);
        }

        // Rating
        int starX = isTopMatch ? (tx + nfm.stringWidth(c.name) + 96) : (tx + nfm.stringWidth(c.name) + 12);
        Draw.drawStarIcon(g, starX, ty - 13, 14, new Color(0xFF, 0xC1, 0x07));
        g.setFont(Theme.fontSmallBold());
        g.setColor(Theme.textSecondary());
        g.drawString(String.format(Locale.US, "%.1f", c.rating), starX + 18, ty);

        // Role & Matched Skills
        g.setFont(Theme.fontSmall());
        g.setColor(Theme.textSecondary());
        StringBuilder skStr = new StringBuilder(c.role).append("  ·  Skills: ");
        for (String s : c.skills) {
            skStr.append(s).append(" ");
        }
        g.drawString(skStr.toString(), tx, y + 52);

        // Animated Match Bar with Dribbble Glowing Tip
        int barW = Math.min(160, Math.max(90, w / 4));
        int barH = 10;
        int barX = x + w - barW - 90;
        int barY = y + (rowH - barH) / 2;

        // Track
        g.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15));
        g.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH));

        // Fill progress
        int fillW = Math.round(barW * c.currentMatch);
        LinearGradientPaint fillGrad = new LinearGradientPaint(
                barX, barY, barX + barW, barY,
                new float[]{0f, 1f},
                new Color[]{Theme.ACCENT_VIOLET, Theme.ACCENT_CYAN}
        );
        Paint old = g.getPaint();
        g.setPaint(fillGrad);
        g.fill(new RoundRectangle2D.Float(barX, barY, fillW, barH, barH, barH));
        g.setPaint(old);

        // Glowing leading dot at end of bar (Dribbble micro-interaction)
        if (fillW > 6) {
            float pulseGlow = Anim.pulse(0.6f, 1.0f, 4f);
            g.setColor(new Color(Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(), Theme.ACCENT_CYAN.getBlue(), (int) (220 * pulseGlow)));
            g.fillOval(barX + fillW - 5, barY - 1, 12, 12);
        }

        // Percentage label with color tiering
        int pct = Math.round(c.currentMatch * 100);
        g.setFont(Theme.fontBodyBold());
        FontMetrics pfm = g.getFontMetrics();
        g.setColor(pct > 80 ? Theme.ACCENT_CYAN : Theme.textPrimary());
        String pctStr = pct + "%";
        g.drawString(pctStr, barX + barW + 16, y + (rowH - pfm.getHeight()) / 2 + pfm.getAscent());
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (Chip chip : skillChips) {
            if (chip.onClick(mx, my)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (Chip chip : skillChips) {
            if (chip.onMouseMove(mx, my)) {
                changed = true;
            }
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
