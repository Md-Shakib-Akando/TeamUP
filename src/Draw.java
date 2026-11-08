import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw contains graphics rendering utilities matching Dribbble PRD spec:
 * soft cursor pink glow with lag, self-drawing pink squiggle, generated cover art,
 * pastel surfaces with layered soft shadows, magnetic buttons, and vector shapes.
 */
public class Draw {

    public static void setQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Motion Spec #4: Soft pink glow follows mouse with lag (RadialGradientPaint, lerp 0.12 per frame)
     */
    public static void drawCursorGlow(Graphics2D g) {
        if (!Theme.motionEnabled) return;
        float cx = Anim.lagCursorX;
        float cy = Anim.lagCursorY;
        float radius = 220f;

        Point2D center = new Point2D.Float(cx, cy);
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color c0 = new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 28);
        Color c1 = new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 12);
        Color c2 = new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 0);
        Color[] colors = {c0, c1, c2};

        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        Paint oldPaint = g.getPaint();
        g.setPaint(p);
        g.fill(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
        g.setPaint(oldPaint);
    }

    /**
     * Base canvas background with subtle warm off-white and faint grid.
     */
    public static void drawBackground(Graphics2D g, int width, int height) {
        g.setColor(Theme.bg());
        g.fillRect(0, 0, width, height);

        // Faint grid
        int gridSize = 56;
        Color gridColor = Theme.isDark ? new Color(255, 255, 255, 5) : new Color(0, 0, 0, 6);
        g.setColor(gridColor);
        g.setStroke(new BasicStroke(1.0f));
        for (int x = 0; x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }

        // Dynamic floating ambient gradient orbs with harmonic motion
        float t = Anim.globalTime;
        if (!Theme.isDark) {
            float b1X = width * 0.82f + (float) Math.sin(t * 0.45) * 55f;
            float b1Y = 220f + (float) Math.cos(t * 0.35) * 45f;
            float b1R = 380f + (float) Math.sin(t * 0.7) * 25f;
            drawRadialBlob(g, b1X, b1Y, b1R, new Color(0xFF, 0xE4, 0xED), 0.45f);

            float b2X = width * 0.16f + (float) Math.cos(t * 0.4) * 45f;
            float b2Y = 560f + (float) Math.sin(t * 0.5) * 40f;
            float b2R = 400f + (float) Math.cos(t * 0.65) * 30f;
            drawRadialBlob(g, b2X, b2Y, b2R, new Color(0xEB, 0xFB, 0xF5), 0.38f);

            float b3X = width * 0.52f + (float) Math.sin(t * 0.3) * 60f;
            float b3Y = 950f + (float) Math.cos(t * 0.4) * 40f;
            drawRadialBlob(g, b3X, b3Y, 360f, new Color(0xF3, 0xF0, 0xFF), 0.35f);
        } else {
            // Dark mode dynamic neon atmospheric glow orbs
            float b1X = width * 0.82f + (float) Math.sin(t * 0.4) * 50f;
            float b1Y = 240f + (float) Math.cos(t * 0.3) * 40f;
            drawRadialBlob(g, b1X, b1Y, 400f, new Color(0x7C, 0x5C, 0xFF), 0.14f);

            float b2X = width * 0.15f + (float) Math.cos(t * 0.35) * 45f;
            float b2Y = 580f + (float) Math.sin(t * 0.45) * 35f;
            drawRadialBlob(g, b2X, b2Y, 380f, new Color(0xEA, 0x4C, 0x89), 0.12f);
        }

        // Atmospheric drifting ambient micro-particles
        if (Theme.motionEnabled) {
            drawAmbientParticles(g, width, height);
        }
    }

    /**
     * Atmospheric drifting particles / sparkles that float smoothly upward.
     */
    public static void drawAmbientParticles(Graphics2D g, int width, int height) {
        if (!Theme.motionEnabled || width <= 0 || height <= 0) return;
        float t = Anim.globalTime;
        int particleCount = 26;

        for (int i = 0; i < particleCount; i++) {
            float seedX = (float) (Math.sin(i * 127.1f) * 0.5f + 0.5f);
            float speed = 25f + ((i * 31) % 25);
            float loopDist = height + 200f;
            float progress = ((t * speed + i * 45f) % loopDist) / loopDist;
            float py = height + 50f - progress * loopDist;
            float px = (seedX * width) + (float) Math.sin(t * 0.7f + i * 1.5f) * 30f;

            // Fading alpha envelope
            float alpha = (float) Math.sin(progress * Math.PI) * 0.45f;
            if (alpha <= 0.01f) continue;

            int pSize = (i % 3 == 0) ? 4 : (i % 2 == 0 ? 3 : 2);
            Color pColor = (i % 3 == 0) ? Theme.BRAND_PINK : ((i % 3 == 1) ? Theme.ACCENT_CYAN : Theme.ACCENT_VIOLET);

            g.setColor(new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), Math.round(255 * alpha)));
            g.fill(new Ellipse2D.Float(px, py, pSize, pSize));

            if (pSize >= 4) {
                g.setColor(new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), Math.round(65 * alpha)));
                g.fill(new Ellipse2D.Float(px - 2, py - 2, pSize + 4, pSize + 4));
            }
        }
    }

    public static void drawRadialBlob(Graphics2D g, float cx, float cy, float radius, Color color, float alpha) {
        Point2D center = new Point2D.Float(cx, cy);
        float[] dist = {0.0f, 1.0f};
        Color c0 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
        Color c1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, new Color[]{c0, c1});
        Paint old = g.getPaint();
        g.setPaint(p);
        g.fill(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
        g.setPaint(old);
    }

    /**
     * Motion Spec #3: Self-drawing pink squiggle under the key word "teammates"
     */
    public static void drawPinkSquiggle(Graphics2D g, float x, float y, float w, float progress) {
        if (progress <= 0.01f) return;
        progress = Math.min(1.0f, progress);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BRAND_PINK);
        g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D.Float path = new Path2D.Float();
        path.moveTo(x, y);

        // Multi-curve wavy squiggle
        float seg = w / 4f;
        path.curveTo(x + seg * 0.5f, y + 8, x + seg * 0.7f, y - 4, x + seg, y + 4);
        path.curveTo(x + seg * 1.5f, y + 10, x + seg * 1.7f, y - 4, x + seg * 2, y + 4);
        path.curveTo(x + seg * 2.5f, y + 10, x + seg * 2.7f, y - 4, x + seg * 3, y + 4);
        path.curveTo(x + seg * 3.5f, y + 10, x + seg * 3.8f, y - 2, x + w, y + 2);

        // Partial stroke clipping by progress width
        Shape oldClip = g2.getClip();
        g2.clip(new Rectangle2D.Float(x - 4, y - 10, w * progress + 8, 30));
        g2.draw(path);
        g2.setClip(oldClip);
        g2.dispose();
    }

    /**
     * Motion Spec #6: Card Tilt and Lift with generated cover art
     */
    public static void drawMemberCard(Graphics2D g, int x, int y, int w, int h,
                                      String name, String role, String initials,
                                      String[] skills, int matchPct, double rating,
                                      int styleIdx, Color c1, Color c2,
                                      float tiltX, float tiltY, float scale,
                                      boolean hovered, boolean invited, boolean liked,
                                      float hoverAlpha, Runnable onInvite, Runnable onLike) {

        Graphics2D g2 = (Graphics2D) g.create();
        setQualityHints(g2);

        float cx = x + w / 2f;
        float cy = y + h / 2f;

        // Apply 3D-feel AffineTransform (Tilt and Scale)
        AffineTransform at = new AffineTransform();
        at.translate(cx, cy);
        at.scale(scale, scale);
        at.shear(tiltX * 0.0008, tiltY * 0.0008);
        at.translate(-cx, -cy);
        g2.transform(at);

        int radius = 24;
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, y, w, h, radius, radius);

        // Soft layered shadow
        int shadowOffset = hovered ? 12 : 5;
        int shadowAlpha = hovered ? (Theme.isDark ? 65 : 30) : (Theme.isDark ? 30 : 15);
        g2.setColor(new Color(0, 0, 0, shadowAlpha));
        g2.fill(new RoundRectangle2D.Float(x, y + shadowOffset, w, h, radius, radius));

        // Card surface (White in light mode, dark card in dark mode)
        g2.setColor(Theme.cardBg());
        g2.fill(cardShape);

        // Top half: Generated cover art in Java2D
        int coverH = Math.round(h * 0.48f);
        Shape oldClip = g2.getClip();
        g2.clip(cardShape);
        drawCoverArt(g2, x, y, w, coverH, styleIdx, c1, c2);
        g2.setClip(oldClip);

        // Initials Avatar overlapping cover art
        int avSize = 44;
        int avX = x + 18;
        int avY = y + coverH - 22;
        drawAvatar(g2, avX, avY, avSize, initials, c1, c2);

        // White border around avatar
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3.0f));
        g2.draw(new Ellipse2D.Float(avX, avY, avSize, avSize));

        // Candidate Name & Match Badge
        g2.setFont(Theme.fontCardTitle());
        FontMetrics nfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int textY = avY + avSize + 14;
        g2.drawString(name, x + 18, textY);

        // Match pill on right
        int matchBadgeW = 56;
        int matchBadgeH = 22;
        int matchBadgeX = x + w - matchBadgeW - 18;
        int matchBadgeY = textY - 16;
        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 28));
        g2.fill(new RoundRectangle2D.Float(matchBadgeX, matchBadgeY, matchBadgeW, matchBadgeH, matchBadgeH, matchBadgeH));
        g2.setColor(Theme.BRAND_PINK);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics mfm = g2.getFontMetrics();
        String mStr = matchPct + "%";
        g2.drawString(mStr, matchBadgeX + (matchBadgeW - mfm.stringWidth(mStr)) / 2, matchBadgeY + 15);

        // Role & Rating
        g2.setFont(Theme.fontSmall());
        g2.setColor(Theme.textSecondary());
        g2.drawString(role, x + 18, textY + 18);

        // Stars & rating score
        int starY = textY + 36;
        drawStarIcon(g2, x + 18, starY - 11, 14, new Color(0xFF, 0xC1, 0x07));
        g2.setFont(Theme.fontSmallBold());
        g2.drawString(String.format("%.1f", rating), x + 36, starY);

        // Skill pills
        int chipY = starY + 14;
        int chipX = x + 18;
        for (int i = 0; i < Math.min(skills.length, 3); i++) {
            String sk = skills[i];
            int chipW = g2.getFontMetrics(Theme.fontSmallBold()).stringWidth(sk) + 16;
            drawChip(g2, chipX, chipY, chipW, 22, sk, false, false);
            chipX += chipW + 6;
        }

        // Motion Spec #7: Hover Overlay (bottom gradient fades in with "Invite" button & heart that pops)
        if (hoverAlpha > 0.01f) {
            g2.setClip(cardShape);

            // Translucent gradient overlay
            int overH = 70;
            int overY = y + h - overH;
            LinearGradientPaint ogp = new LinearGradientPaint(
                    x, overY, x, y + h,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(13, 12, 34, 0),
                            new Color(13, 12, 34, Math.round(220 * hoverAlpha))
                    }
            );
            g2.setPaint(ogp);
            g2.fillRect(x, overY, w, overH);

            // "Invite" button
            int invW = 90;
            int invH = 34;
            int invX = x + 18;
            int invY = y + h - invH - 16;
            drawGradientButton(g2, invX, invY, invW, invH, invited ? "Invited ✓" : "Invite", false, false, false, false);

            // Pop Heart Icon on the right
            int heartSize = 24;
            int heartX = x + w - heartSize - 22;
            int heartY = invY + (invH - heartSize) / 2;
            float heartScale = liked ? 1.25f : 1.0f;
            drawHeart(g2, heartX, heartY, heartSize, liked, Theme.BRAND_PINK, heartScale);

            g2.setClip(oldClip);
        }

        // 1px Border
        g2.setColor(Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(cardShape);

        g2.dispose();
    }

    /**
     * Modern sleek pill badge with glowing dot (e.g. "● THE CHALLENGE")
     */
    public static void drawSectionBadge(Graphics2D g, String text, int x, int y, Color dotColor) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(Theme.fontSmallBold());
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int badgeH = 28;
        int badgeW = textW + 30;

        RoundRectangle2D pill = new RoundRectangle2D.Float(x, y, badgeW, badgeH, badgeH, badgeH);
        g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), 20));
        g2.fill(pill);

        g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), 55));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(pill);

        // Dot
        int dotSize = 6;
        g2.setColor(dotColor);
        g2.fillOval(x + 11, y + (badgeH - dotSize) / 2, dotSize, dotSize);

        // Text
        g2.setColor(Theme.isDark ? new Color(0xEE, 0xF2, 0xF6) : dotColor.darker());
        g2.drawString(text, x + 22, y + (badgeH - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    /**
     * Generates beautiful modern abstract cover art in pure Java2D
     */
    private static void drawCoverArt(Graphics2D g, int x, int y, int w, int h, int styleIdx, Color c1, Color c2) {
        // Background gradient
        LinearGradientPaint p = new LinearGradientPaint(
                x, y, x + w, y + h,
                new float[]{0f, 1f},
                new Color[]{c1, c2}
        );
        Paint old = g.getPaint();
        g.setPaint(p);
        g.fillRect(x, y, w, h);

        g.setColor(new Color(255, 255, 255, 45));

        if (styleIdx % 3 == 0) {
            // Overlapping geometric circles
            g.fill(new Ellipse2D.Float(x + w * 0.4f, y - h * 0.2f, w * 0.8f, w * 0.8f));
            g.setColor(new Color(255, 255, 255, 25));
            g.fill(new Ellipse2D.Float(x - w * 0.2f, y + h * 0.3f, w * 0.6f, w * 0.6f));
        } else if (styleIdx % 3 == 1) {
            // Wave blob shape
            Path2D.Float blob = new Path2D.Float();
            blob.moveTo(x, y + h);
            blob.curveTo(x + w * 0.3f, y + h * 0.2f, x + w * 0.7f, y + h * 0.9f, x + w, y + h * 0.3f);
            blob.lineTo(x + w, y + h);
            blob.closePath();
            g.fill(blob);
        } else {
            // Modern angled stripes
            g.setStroke(new BasicStroke(24f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 30));
            g.drawLine(x - 20, y + h, Math.round(x + w * 0.6f), y - 20);
            g.drawLine(Math.round(x + w * 0.3f), y + h + 20, x + w + 20, y);
        }

        g.setPaint(old);
    }

    /**
     * Heart icon that pops with bounce scale
     */
    public static void drawHeart(Graphics2D g, int x, int y, int size, boolean filled, Color color, float scale) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + size / 2f, y + size / 2f);
        g2.scale(scale, scale);
        g2.translate(-size / 2f, -size / 2f);

        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float s = size;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(s * 0.5f, s * 0.8f);
        p.curveTo(s * 0.1f, s * 0.5f, 0, s * 0.25f, s * 0.25f, s * 0.1f);
        p.curveTo(s * 0.45f, 0, s * 0.5f, s * 0.2f, s * 0.5f, s * 0.25f);
        p.curveTo(s * 0.5f, s * 0.2f, s * 0.55f, 0, s * 0.75f, s * 0.1f);
        p.curveTo(s, s * 0.25f, s * 0.9f, s * 0.5f, s * 0.5f, s * 0.8f);
        p.closePath();

        if (filled) {
            g2.fill(p);
        } else {
            g2.draw(p);
        }
        g2.dispose();
    }

    /**
     * Dribbble Pink Pill Button with magnetic offset support (Motion Spec #8)
     */
    public static void drawPinkPillButton(Graphics2D g, int x, int y, int w, int h, String text,
                                          boolean isHovered, boolean isPressed, boolean focused,
                                          float magnetX, float magnetY) {
        int actualX = Math.round(x + magnetX);
        int actualY = Math.round(y + magnetY);
        int radius = h;
        RoundRectangle2D shape = new RoundRectangle2D.Float(actualX, actualY, w, h, radius, radius);

        // Soft pink drop shadow
        int shadowOffset = isHovered ? 8 : 4;
        g.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), isHovered ? 85 : 45));
        g.fill(new RoundRectangle2D.Float(actualX, actualY + shadowOffset, w, h, radius, radius));

        // Hot pink fill
        Color c = isHovered ? brighter(Theme.BRAND_PINK, 1.1f) : Theme.BRAND_PINK;
        if (isPressed) c = darker(Theme.BRAND_PINK, 0.9f);
        g.setColor(c);
        g.fill(shape);

        // Animated light shine / shimmer sweep (periodic every 3.5s)
        if (Theme.motionEnabled) {
            float cycle = (Anim.globalTime % 3.5f) / 1.0f;
            if (cycle <= 1.0f) {
                Graphics2D gClip = (Graphics2D) g.create();
                gClip.setClip(shape);
                float shineX = actualX - 60f + cycle * (w + 120f);
                LinearGradientPaint shineGrad = new LinearGradientPaint(
                        shineX - 25, actualY, shineX + 25, actualY + h,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{
                                new Color(255, 255, 255, 0),
                                new Color(255, 255, 255, 110),
                                new Color(255, 255, 255, 0)
                        }
                );
                gClip.setPaint(shineGrad);
                gClip.fill(shape);
                gClip.dispose();
            }
        }

        // Label
        g.setFont(Theme.fontButton());
        FontMetrics fm = g.getFontMetrics();
        int textX = actualX + (w - fm.stringWidth(text)) / 2;
        int textY = actualY + (h - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(text, textX, textY);

        if (focused) {
            drawFocusRing(g, actualX - 3, actualY - 3, w + 6, h + 6, radius + 4);
        }
    }

    /**
     * Draws standard gradient button
     */
    public static void drawGradientButton(Graphics2D g, int x, int y, int w, int h, String text,
                                          boolean isHovered, boolean isPressed, boolean isWarm, boolean focused) {
        drawPinkPillButton(g, x, y, w, h, text, isHovered, isPressed, focused, 0f, 0f);
    }

    /**
     * Draws ghost/outline button
     */
    public static void drawGhostButton(Graphics2D g, int x, int y, int w, int h, String text,
                                       boolean isHovered, boolean isPressed, boolean focused) {
        int radius = h;
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, radius, radius);

        Color fill = isHovered ? new Color(0, 0, 0, 10) : new Color(0, 0, 0, 0);
        g.setColor(fill);
        g.fill(shape);

        Color border = isHovered ? Theme.INK_DARK : Theme.cardBorder();
        g.setColor(border);
        g.setStroke(new BasicStroke(isHovered ? 1.5f : 1.2f));
        g.draw(shape);

        g.setFont(Theme.fontButton());
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h - fm.getHeight()) / 2 + fm.getAscent();

        g.setColor(Theme.textPrimary());
        g.drawString(text, textX, textY);

        if (focused) {
            drawFocusRing(g, x - 3, y - 3, w + 6, h + 6, radius + 4);
        }
    }

    /**
     * Draws interactive skill/badge chip
     */
    public static void drawChip(Graphics2D g, int x, int y, int w, int h, String text, boolean selected, boolean hovered) {
        int radius = h;
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, radius, radius);

        if (selected) {
            g.setColor(Theme.BRAND_PINK);
            g.fill(shape);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(hovered ? new Color(0, 0, 0, 12) : new Color(0, 0, 0, 5));
            g.fill(shape);
            g.setColor(hovered ? Theme.INK_DARK : Theme.cardBorder());
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
            g.setColor(hovered ? Theme.INK_DARK : Theme.textSecondary());
        }

        g.setFont(Theme.fontSmallBold());
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, textX, textY);
    }

    public static void drawGlassCard(Graphics2D g, int x, int y, int w, int h, int radius, float hoverLift, boolean glow) {
        int actualY = y - Math.round(hoverLift);

        int shadowOffset = 6 + Math.round(hoverLift * 1.5f);
        int shadowAlpha = Theme.isDark ? (glow ? 50 : 25) : (glow ? 25 : 12);
        g.setColor(new Color(0, 0, 0, shadowAlpha));
        g.fill(new RoundRectangle2D.Float(x, actualY + shadowOffset, w, h, radius, radius));

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, actualY, w, h, radius, radius);
        g.setColor(Theme.cardBg());
        g.fill(cardShape);

        Color borderColor = glow ? new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 120) : Theme.cardBorder();
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cardShape);
    }

    public static void drawRadarBeacon(Graphics2D g, int cx, int cy, Color color) {
        float phase = Anim.loop(2.0f, 0f);
        float r = 5f + phase * 16f;
        int alpha = (int) (180 * (1f - phase));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha)));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));

        g.setColor(color);
        g.fill(new Ellipse2D.Float(cx - 4, cy - 4, 8, 8));
    }

    public static void drawFocusRing(Graphics2D g, int x, int y, int w, int h, int radius) {
        g.setColor(Theme.BRAND_PINK);
        float[] dash = {4f, 3f};
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g.draw(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
    }

    public static void drawLogo(Graphics2D g, int x, int y, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        setQualityHints(g2);

        RoundRectangle2D badge = new RoundRectangle2D.Float(x, y, size, size, size * 0.35f, size * 0.35f);
        g2.setColor(Theme.BRAND_PINK);
        g2.fill(badge);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(size * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float cx = x + size * 0.5f;
        float cy = y + size * 0.5f;

        Path2D.Float path = new Path2D.Float();
        path.moveTo(cx - size * 0.24f, cy - size * 0.18f);
        path.lineTo(cx + size * 0.24f, cy - size * 0.18f);
        path.moveTo(cx, cy - size * 0.18f);
        path.lineTo(cx, cy + size * 0.08f);
        path.curveTo(cx, cy + size * 0.25f, cx + size * 0.22f, cy + size * 0.25f, cx + size * 0.22f, cy + size * 0.10f);
        g2.draw(path);

        float dotR = size * 0.06f;
        g2.fill(new Ellipse2D.Float(cx - size * 0.24f - dotR, cy - size * 0.18f - dotR, dotR * 2, dotR * 2));
        g2.fill(new Ellipse2D.Float(cx + size * 0.24f - dotR, cy - size * 0.18f - dotR, dotR * 2, dotR * 2));
        g2.fill(new Ellipse2D.Float(cx + size * 0.22f - dotR, cy + size * 0.10f - dotR, dotR * 2, dotR * 2));

        g2.dispose();
    }

    public static void drawSparkleIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        float cx = x + size / 2f;
        float cy = y + size / 2f;
        float r = size * 0.45f;

        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx, cy - r);
        p.quadTo(cx, cy, cx + r, cy);
        p.quadTo(cx, cy, cx, cy + r);
        p.quadTo(cx, cy, cx - r, cy);
        p.quadTo(cx, cy, cx, cy - r);
        p.closePath();
        g2.fill(p);
        g2.dispose();
    }

    public static void drawSearchIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float r = size * 0.32f;
        g2.draw(new Ellipse2D.Float(x + size * 0.15f, y + size * 0.15f, r * 2, r * 2));
        g2.draw(new Line2D.Float(x + size * 0.58f, y + size * 0.58f, x + size * 0.85f, y + size * 0.85f));
        g2.dispose();
    }

    public static void drawUsersIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float cx = x + size * 0.5f;
        g2.draw(new Ellipse2D.Float(cx - size * 0.15f, y + size * 0.15f, size * 0.3f, size * 0.3f));
        Path2D.Float body = new Path2D.Float();
        body.moveTo(cx - size * 0.3f, y + size * 0.82f);
        body.curveTo(cx - size * 0.3f, y + size * 0.58f, cx + size * 0.3f, y + size * 0.58f, cx + size * 0.3f, y + size * 0.82f);
        g2.draw(body);
        g2.dispose();
    }

    public static void drawTaskIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(x + size * 0.15f, y + size * 0.15f, size * 0.7f, size * 0.7f, size * 0.2f, size * 0.2f));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + size * 0.3f, y + size * 0.5f);
        p.lineTo(x + size * 0.45f, y + size * 0.65f);
        p.lineTo(x + size * 0.7f, y + size * 0.38f);
        g2.draw(p);
        g2.dispose();
    }

    public static void drawChatIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + size * 0.2f, y + size * 0.2f);
        p.lineTo(x + size * 0.8f, y + size * 0.2f);
        p.quadTo(x + size * 0.85f, y + size * 0.2f, x + size * 0.85f, y + size * 0.3f);
        p.lineTo(x + size * 0.85f, y + size * 0.6f);
        p.quadTo(x + size * 0.85f, y + size * 0.7f, x + size * 0.75f, y + size * 0.7f);
        p.lineTo(x + size * 0.45f, y + size * 0.7f);
        p.lineTo(x + size * 0.25f, y + size * 0.85f);
        p.lineTo(x + size * 0.25f, y + size * 0.7f);
        p.lineTo(x + size * 0.2f, y + size * 0.7f);
        p.quadTo(x + size * 0.15f, y + size * 0.7f, x + size * 0.15f, y + size * 0.6f);
        p.lineTo(x + size * 0.15f, y + size * 0.3f);
        p.quadTo(x + size * 0.15f, y + size * 0.2f, x + size * 0.2f, y + size * 0.2f);
        g2.draw(p);
        g2.dispose();
    }

    public static void drawStarIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        float cx = x + size / 2f;
        float cy = y + size / 2f;
        float rOuter = size * 0.45f;
        float rInner = rOuter * 0.45f;

        Path2D.Float p = new Path2D.Float();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float r = (i % 2 == 0) ? rOuter : rInner;
            float px = (float) (cx + Math.cos(angle) * r);
            float py = (float) (cy + Math.sin(angle) * r);
            if (i == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        p.closePath();
        g2.fill(p);
        g2.dispose();
    }

    public static void drawShieldIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float cx = x + size * 0.5f;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx, y + size * 0.15f);
        p.lineTo(x + size * 0.8f, y + size * 0.25f);
        p.quadTo(x + size * 0.8f, y + size * 0.65f, cx, y + size * 0.85f);
        p.quadTo(x + size * 0.2f, y + size * 0.65f, x + size * 0.2f, y + size * 0.25f);
        p.closePath();
        g2.draw(p);
        g2.dispose();
    }

    public static void drawThemeIcon(Graphics2D g, int x, int y, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.textPrimary());
        float cx = x + size / 2f;
        float cy = y + size / 2f;

        if (Theme.isDark) {
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float r = size * 0.22f;
            g2.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                float x1 = (float) (cx + Math.cos(a) * (r + 3));
                float y1 = (float) (cy + Math.sin(a) * (r + 3));
                float x2 = (float) (cx + Math.cos(a) * (r + 6));
                float y2 = (float) (cy + Math.sin(a) * (r + 6));
                g2.draw(new Line2D.Float(x1, y1, x2, y2));
            }
        } else {
            Path2D.Float moon = new Path2D.Float();
            float r = size * 0.35f;
            moon.moveTo(cx, cy - r);
            moon.curveTo(cx + r * 1.2f, cy - r * 0.5f, cx + r * 1.2f, cy + r * 0.5f, cx, cy + r);
            moon.curveTo(cx + r * 0.6f, cy + r * 0.4f, cx + r * 0.6f, cy - r * 0.4f, cx, cy - r);
            moon.closePath();
            g2.fill(moon);
        }
        g2.dispose();
    }

    public static void drawCheckIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + size * 0.2f, y + size * 0.52f);
        p.lineTo(x + size * 0.42f, y + size * 0.74f);
        p.lineTo(x + size * 0.8f, y + size * 0.28f);
        g2.draw(p);
        g2.dispose();
    }

    public static void drawDashIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(x + size * 0.3f, y + size * 0.5f, x + size * 0.7f, y + size * 0.5f));
        g2.dispose();
    }

    public static void drawChevronDown(Graphics2D g, int x, int y, int size, Color color, float angle) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + size / 2.0, y + size / 2.0);
        g2.rotate(angle);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(-size * 0.3f, -size * 0.15f);
        p.lineTo(0, size * 0.15f);
        p.lineTo(size * 0.3f, -size * 0.15f);
        g2.draw(p);
        g2.dispose();
    }

    public static void drawHamburgerIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(x + size * 0.2f, y + size * 0.3f, x + size * 0.8f, y + size * 0.3f));
        g2.draw(new Line2D.Float(x + size * 0.2f, y + size * 0.5f, x + size * 0.8f, y + size * 0.5f));
        g2.draw(new Line2D.Float(x + size * 0.2f, y + size * 0.7f, x + size * 0.8f, y + size * 0.7f));
        g2.dispose();
    }

    public static void drawCloseIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(x + size * 0.25f, y + size * 0.25f, x + size * 0.75f, y + size * 0.75f));
        g2.draw(new Line2D.Float(x + size * 0.75f, y + size * 0.25f, x + size * 0.25f, y + size * 0.75f));
        g2.dispose();
    }

    public static void drawAvatar(Graphics2D g, int x, int y, int size, String initials, Color c1, Color c2) {
        Graphics2D g2 = (Graphics2D) g.create();
        setQualityHints(g2);

        LinearGradientPaint p = new LinearGradientPaint(x, y, x + size, y + size,
                new float[]{0f, 1f},
                new Color[]{c1, c2});
        g2.setPaint(p);
        g2.fill(new Ellipse2D.Float(x, y, size, size));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size * 0.42f)));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (size - fm.stringWidth(initials)) / 2;
        int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, tx, ty);
        g2.dispose();
    }

    public static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] paragraphs = text.split("\n");
        for (String p : paragraphs) {
            String[] words = p.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String w : words) {
                if (currentLine.length() == 0) {
                    currentLine.append(w);
                } else {
                    String test = currentLine + " " + w;
                    if (fm.stringWidth(test) <= maxWidth) {
                        currentLine.append(" ").append(w);
                    } else {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(w);
                    }
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    private static Color brighter(Color c, float factor) {
        int r = Math.min(255, Math.round(c.getRed() * factor));
        int g = Math.min(255, Math.round(c.getGreen() * factor));
        int b = Math.min(255, Math.round(c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }

    private static Color darker(Color c, float factor) {
        int r = Math.max(0, Math.round(c.getRed() * factor));
        int g = Math.max(0, Math.round(c.getGreen() * factor));
        int b = Math.max(0, Math.round(c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }
}
