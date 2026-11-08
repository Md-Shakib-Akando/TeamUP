import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fx — the visual toolkit behind the redesigned landing page.
 *
 * "Aurora Glass" design language: deep ink canvas, drifting pink / violet / cyan
 * aurora light, frosted glass surfaces with cursor-tracking spotlight borders,
 * gradient typography, and scroll-triggered staggered reveals.
 *
 * Every colour token is interpolated by {@link Theme#themeTransition}, so the
 * light/dark toggle still animates smoothly across the whole page.
 */
public final class Fx {

    private Fx() {}

    // =====================================================================
    // PALETTE
    // =====================================================================

    public static final Color PINK   = Theme.BRAND_PINK;               // #EA4C89 brand
    public static final Color VIOLET = new Color(0x8B, 0x5C, 0xF6);
    public static final Color INDIGO = new Color(0x63, 0x66, 0xF1);
    public static final Color CYAN   = new Color(0x22, 0xD3, 0xEE);
    public static final Color MINT   = new Color(0x34, 0xD3, 0x99);
    public static final Color AMBER  = new Color(0xFB, 0xBF, 0x24);
    public static final Color ORANGE = new Color(0xFF, 0x8A, 0x5B);
    public static final Color RED    = new Color(0xF4, 0x3F, 0x5E);

    /** Brand gradient stops used for headlines, buttons and highlights. */
    public static final Color[] BRAND = {PINK, VIOLET, CYAN};

    private static Color dl(Color dark, Color light) {
        return Theme.lerp(dark, light, Theme.themeTransition);
    }

    public static boolean dark() { return Theme.themeTransition < 0.5f; }

    public static Color bg()        { return dl(new Color(0x07, 0x07, 0x14), new Color(0xF7, 0xF6, 0xFB)); }
    public static Color bgDeep()    { return dl(new Color(0x04, 0x04, 0x0C), new Color(0xEE, 0xEC, 0xF5)); }
    public static Color text()      { return dl(new Color(0xF4, 0xF4, 0xF8), new Color(0x0D, 0x0C, 0x22)); }
    public static Color text2()     { return dl(new Color(0xA6, 0xA6, 0xBA), new Color(0x4A, 0x49, 0x5E)); }
    public static Color text3()     { return dl(new Color(0x6E, 0x6E, 0x86), new Color(0x8C, 0x8B, 0x9E)); }
    public static Color border()    { return dl(new Color(255, 255, 255, 22), new Color(20, 18, 50, 24)); }
    public static Color borderHi()  { return dl(new Color(255, 255, 255, 46), new Color(20, 18, 50, 50)); }
    public static Color surface()   { return dl(new Color(255, 255, 255, 9), new Color(255, 255, 255, 215)); }
    public static Color surfaceHi() { return dl(new Color(255, 255, 255, 16), new Color(255, 255, 255, 245)); }
    public static Color solid()     { return dl(new Color(0x10, 0x10, 0x22), Color.WHITE); }
    public static Color subtle()    { return dl(new Color(255, 255, 255, 12), new Color(20, 18, 50, 9)); }
    /** Field fill for inputs that sit on frosted glass (keeps text readable). */
    public static Color glassField(boolean active) {
        return active ? dl(new Color(8, 6, 26, 150), new Color(255, 255, 255, 235))
                      : dl(new Color(8, 6, 26, 115), new Color(255, 255, 255, 190));
    }

    public static Color a(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clampI(alpha, 0, 255));
    }

    public static Color mix(Color a, Color b, float t) { return Theme.lerp(a, b, t); }

    /** Accent tinted so it stays readable on light backgrounds. */
    public static Color ink(Color accent) {
        return dl(accent, mix(accent, new Color(0x1A, 0x10, 0x3A), 0.28f));
    }

    // =====================================================================
    // MATH & EASING
    // =====================================================================

    public static float clamp(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    public static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }
    public static int clampI(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }

    public static float easeOutQuart(float t) { t = clamp(t); float f = 1f - t; return 1f - f * f * f * f; }
    public static float easeOutExpo(float t) { t = clamp(t); return t >= 1f ? 1f : 1f - (float) Math.pow(2, -10 * t); }
    public static float easeInOutCubic(float t) {
        t = clamp(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }
    public static float easeOutBack(float t) { return Anim.easeOutBack(t); }

    public static float time() { return Anim.globalTime; }

    public static float wave(float speed, float phase) {
        if (!Theme.motionEnabled) return 0f;
        return (float) Math.sin(Anim.globalTime * speed + phase);
    }

    // =====================================================================
    // TYPOGRAPHY
    // =====================================================================

    private static String family;
    private static final Map<String, Font> fontCache = new HashMap<>();

    public static String family() {
        if (family == null) {
            String[] preferred = {"Inter", "Segoe UI", "SF Pro Display", "Helvetica Neue", "Roboto", "Noto Sans", "DejaVu Sans"};
            family = Font.SANS_SERIF;
            try {
                String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                outer:
                for (String p : preferred) {
                    for (String f : installed) {
                        if (f.equalsIgnoreCase(p)) {
                            family = f;
                            break outer;
                        }
                    }
                }
            } catch (Throwable ignored) {
                family = Font.SANS_SERIF;
            }
        }
        return family;
    }

    /** Font in the resolved UI family. size is pre-zoom; tracking is in em (e.g. -0.02f). */
    public static Font font(int style, float size, float tracking) {
        int px = Math.max(8, Math.round(size * Theme.fontScale));
        String key = style + ":" + px + ":" + tracking;
        Font f = fontCache.get(key);
        if (f == null) {
            f = new Font(family(), style, px);
            if (tracking != 0f) {
                Map<TextAttribute, Object> attrs = new HashMap<>();
                attrs.put(TextAttribute.TRACKING, tracking);
                f = f.deriveFont(attrs);
            }
            fontCache.put(key, f);
        }
        return f;
    }

    public static Font font(int style, float size) { return font(style, size, 0f); }
    public static Font display(float size) { return font(Font.BOLD, size, 0f); }
    public static Font heading(float size) { return font(Font.BOLD, size, 0f); }
    public static Font body(float size)    { return font(Font.PLAIN, size, 0f); }
    public static Font bold(float size)    { return font(Font.BOLD, size, 0f); }
    public static Font label(float size)   { return font(Font.BOLD, size, 0.12f); }

    public static int textW(Graphics2D g, Font f, String s) { return g.getFontMetrics(f).stringWidth(s); }

    private static Graphics2D metricsG;

    /** FontMetrics with the same rendering hints used for painting (for layout outside paint). */
    public static synchronized FontMetrics metrics(Font f) {
        if (metricsG == null) {
            metricsG = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
            Draw.setQualityHints(metricsG);
        }
        return metricsG.getFontMetrics(f);
    }

    /** Draws text so that its vertical centre sits on cy. */
    public static void textMid(Graphics2D g, String s, float x, float cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, x, cy - fm.getHeight() / 2f + fm.getAscent());
    }

    public static void textCenter(Graphics2D g, String s, float cx, float cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2f, cy - fm.getHeight() / 2f + fm.getAscent());
    }

    /** Animated gradient text. shift slides the gradient (pass time for shimmer). */
    public static void gradientText(Graphics2D g, String s, float x, float baseline, Color[] cols, float shift) {
        FontMetrics fm = g.getFontMetrics();
        float w = Math.max(1, fm.stringWidth(s));
        float off = shift * w;
        float[] stops = new float[cols.length];
        for (int i = 0; i < cols.length; i++) stops[i] = cols.length == 1 ? 0f : i / (float) (cols.length - 1);
        Paint old = g.getPaint();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(x - off, baseline - fm.getAscent()),
                new Point2D.Float(x - off + w, baseline),
                stops, cols, MultipleGradientPaint.CycleMethod.REFLECT));
        g.drawString(s, x, baseline);
        g.setPaint(old);
    }

    /** Wrapped paragraph. Returns the y below the last line. */
    public static int paragraph(Graphics2D g, String text, Font f, Color c, int x, int y, int maxW, float lineH, boolean center) {
        g.setFont(f);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        List<String> lines = Draw.wrapText(text, fm, maxW);
        int step = Math.round(f.getSize2D() * lineH);
        // centre the glyph box inside the first line box (like CSS line-height)
        int baseline = y + Math.round((step - fm.getHeight()) / 2f) + fm.getAscent();
        for (String l : lines) {
            int lx = center ? x + (maxW - fm.stringWidth(l)) / 2 : x;
            g.drawString(l, lx, baseline);
            baseline += step;
        }
        return y + lines.size() * step;
    }

    public static int paragraphHeight(Graphics2D g, String text, Font f, int maxW, float lineH) {
        FontMetrics fm = g.getFontMetrics(f);
        return Draw.wrapText(text, fm, maxW).size() * Math.round(f.getSize2D() * lineH);
    }

    // =====================================================================
    // LAYOUT
    // =====================================================================

    public static final int MAX_W = 1180;

    public static int pad(int width) { return width > 1100 ? 48 : (width > 600 ? 28 : 18); }

    /** Content column: {x, width}. */
    public static int[] container(int width) {
        int p = pad(width);
        int w = Math.min(MAX_W, width - p * 2);
        return new int[]{(width - w) / 2, w};
    }

    public static RoundRectangle2D.Float rr(float x, float y, float w, float h, float r) {
        return new RoundRectangle2D.Float(x, y, w, h, r * 2, r * 2);
    }

    public static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // =====================================================================
    // COMPOSITING
    // =====================================================================

    public static float alphaOf(Graphics2D g) {
        Composite c = g.getComposite();
        if (c instanceof AlphaComposite) return ((AlphaComposite) c).getAlpha();
        return 1f;
    }

    /** Multiplies the current alpha of g. */
    public static void fadeBy(Graphics2D g, float a) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alphaOf(g) * a)));
    }

    /**
     * Copy of g faded to p and lifted by (1-p)*dy. Callers must dispose().
     */
    public static Graphics2D fade(Graphics2D g, float p, float dy) {
        Graphics2D c = (Graphics2D) g.create();
        if (p < 0.999f) {
            fadeBy(c, p);
            c.translate(0, (1f - p) * dy);
        }
        return c;
    }

    /** Same as fade(), plus a subtle scale-in around (cx, cy). */
    public static Graphics2D pop(Graphics2D g, float p, float cx, float cy) {
        Graphics2D c = (Graphics2D) g.create();
        if (p < 0.999f) {
            fadeBy(c, p);
            float s = 0.92f + 0.08f * p;
            c.translate(cx, cy + (1f - p) * 16f);
            c.scale(s, s);
            c.translate(-cx, -cy);
        }
        return c;
    }

    // =====================================================================
    // GLOW SPRITES (cached radial light — far cheaper than per-frame gradients)
    // =====================================================================

    private static final Map<Integer, BufferedImage> glowCache = new HashMap<>();
    private static final int SPRITE = 256;

    private static BufferedImage glowSprite(Color c) {
        int key = c.getRGB() | 0xFF000000;
        BufferedImage img = glowCache.get(key);
        if (img != null) return img;
        img = new BufferedImage(SPRITE, SPRITE, BufferedImage.TYPE_INT_ARGB);
        float half = SPRITE / 2f;
        int rgb = c.getRGB() & 0x00FFFFFF;
        for (int y = 0; y < SPRITE; y++) {
            for (int x = 0; x < SPRITE; x++) {
                float dx = (x + 0.5f - half) / half;
                float dy = (y + 0.5f - half) / half;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d >= 1f) continue;
                // Gaussian-like falloff, zero at the rim (no hard edge)
                float a = (float) Math.exp(-d * d * 3.2f) * (1f - d * d);
                img.setRGB(x, y, (Math.round(a * 255) << 24) | rgb);
            }
        }
        glowCache.put(key, img);
        return img;
    }

    /*
     * Pre-scaled glow cache. Scaling a sprite on every frame is the most expensive
     * operation in software Java2D (~20ns/pixel), so each (colour, size) pair is
     * scaled once and afterwards only blitted. Sizes are quantised on a ~4% log
     * scale so animated radii reuse entries; an LRU caps memory (~64 MB).
     */
    private static final long MAX_CACHE_PIXELS = 16_000_000L;
    private static long cachePixels = 0;
    private static final java.util.LinkedHashMap<Long, BufferedImage> scaled =
            new java.util.LinkedHashMap<Long, BufferedImage>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, BufferedImage> e) {
                    if (cachePixels > MAX_CACHE_PIXELS && size() > 1) {
                        BufferedImage v = e.getValue();
                        cachePixels -= (long) v.getWidth() * v.getHeight();
                        return true;
                    }
                    return false;
                }
            };

    private static int quantize(float v) {
        if (v <= 24) return Math.max(2, Math.round(v / 2f) * 2);
        double step = Math.log(1.04);
        return (int) Math.round(Math.exp(Math.round(Math.log(v) / step) * step));
    }

    private static synchronized BufferedImage scaledGlow(Color c, int w, int h) {
        long key = ((long) (c.getRGB() & 0xFFFFFF) << 32) ^ ((long) w << 16) ^ h;
        BufferedImage img = scaled.get(key);
        if (img != null) return img;
        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(glowSprite(c), 0, 0, w, h, null);
        g.dispose();
        cachePixels += (long) w * h;
        scaled.put(key, img);
        return img;
    }

    public static void glow(Graphics2D g, float cx, float cy, float radius, Color c, float alpha) {
        glowOval(g, cx, cy, radius, radius, c, alpha);
    }

    /** Elliptical glow (cached, blitted unscaled). */
    public static void glowOval(Graphics2D g, float cx, float cy, float rx, float ry, Color c, float alpha) {
        if (alpha <= 0.003f || rx < 1 || ry < 1) return;
        Rectangle clip = g.getClipBounds();
        if (clip != null && !clip.intersects(cx - rx, cy - ry, rx * 2, ry * 2)) return;
        int w = quantize(rx * 2), h = quantize(ry * 2);
        BufferedImage img = scaledGlow(c, w, h);
        Composite old = g.getComposite();
        fadeBy(g, alpha);
        g.drawImage(img, Math.round(cx - w / 2f), Math.round(cy - h / 2f), null);
        g.setComposite(old);
    }

    // =====================================================================
    // PAGE BACKGROUND
    // =====================================================================

    /*
     * The aurora drifts slowly, so it is rendered into an off-screen buffer that is
     * rebuilt incrementally (one layer per frame) and swapped when complete.
     * Each frame then costs one opaque blit instead of four huge glows + a grid.
     */
    private static BufferedImage bgFront, bgBack;
    private static int bgStep = 0;
    private static boolean bgValid = false;
    private static float bgTheme = -1f;
    private static final int BG_STEPS = 5;

    private static void bgLayer(Graphics2D g, int step, int w, int h, float t) {
        float dk = 1f - Theme.themeTransition; // 1 = dark
        float base = 0.16f + 0.16f * dk;
        float big = Math.max(w, h);
        switch (step) {
            case 0:
                g.setColor(bg());
                g.fillRect(0, 0, w, h);
                break;
            case 1:
                glow(g, w * 0.12f + 60 * (float) Math.sin(t * 0.13), -h * 0.05f + 40 * (float) Math.cos(t * 0.11), big * 0.55f, PINK, base * 0.9f);
                break;
            case 2:
                glow(g, w * 0.88f + 70 * (float) Math.cos(t * 0.09), h * 0.18f + 50 * (float) Math.sin(t * 0.15), big * 0.55f, VIOLET, base);
                break;
            case 3:
                glow(g, w * 0.55f + 90 * (float) Math.sin(t * 0.07 + 2), h * 1.05f, big * 0.6f, INDIGO, base * 0.75f);
                glow(g, w * 0.05f, h * 0.85f + 40 * (float) Math.sin(t * 0.1 + 1), w * 0.35f, CYAN, base * 0.45f);
                break;
            default: {
                // fixed dot grid: content scrolls over it, which reads as depth
                BufferedImage tile = gridTile(dark());
                int ts = tile.getWidth();
                Composite oc = g.getComposite();
                fadeBy(g, 0.7f);
                for (int y = 0; y < h; y += ts)
                    for (int x = 0; x < w; x += ts)
                        g.drawImage(tile, x, y, null);
                g.setComposite(oc);
            }
        }
    }

    private static BufferedImage gridTileDark, gridTileLight;

    private static BufferedImage gridTile(boolean darkMode) {
        BufferedImage t = darkMode ? gridTileDark : gridTileLight;
        if (t != null) return t;
        int s = 32;
        t = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = t.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(darkMode ? new Color(255, 255, 255, 26) : new Color(30, 20, 80, 26));
        g.fill(new Ellipse2D.Float(s / 2f - 0.9f, s / 2f - 0.9f, 1.8f, 1.8f));
        g.dispose();
        if (darkMode) gridTileDark = t; else gridTileLight = t;
        return t;
    }

    /** Full-window aurora background with dot grid (cached, refreshed incrementally). */
    public static void background(Graphics2D g, int w, int h, float scrollY) {
        if (w <= 0 || h <= 0) return;
        boolean resized = bgFront == null || bgFront.getWidth() != w || bgFront.getHeight() != h;
        boolean themeMoving = Math.abs(bgTheme - Theme.themeTransition) > 0.001f;
        if (resized) {
            bgFront = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            bgBack = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            bgValid = false;
        }
        float t = Anim.globalTime;
        if (!bgValid || themeMoving) {
            // full rebuild: first frame, resize, or while the theme cross-fades
            Graphics2D b = bgFront.createGraphics();
            Draw.setQualityHints(b);
            for (int i = 0; i < BG_STEPS; i++) bgLayer(b, i, w, h, t);
            b.dispose();
            bgValid = true;
            bgStep = 0;
            bgTheme = Theme.themeTransition;
        } else if (Theme.motionEnabled) {
            Graphics2D b = bgBack.createGraphics();
            Draw.setQualityHints(b);
            bgLayer(b, bgStep, w, h, t);
            b.dispose();
            if (++bgStep >= BG_STEPS) {
                BufferedImage tmp = bgFront;
                bgFront = bgBack;
                bgBack = tmp;
                bgStep = 0;
            }
        }
        g.drawImage(bgFront, 0, 0, null);
    }

    /** Lagging cursor light. */
    public static void cursorLight(Graphics2D g) {
        if (!Theme.motionEnabled) return;
        float dk = 1f - Theme.themeTransition;
        glow(g, Anim.lagCursorX, Anim.lagCursorY, 340, VIOLET, 0.07f + 0.08f * dk);
        glow(g, Anim.lagCursorX, Anim.lagCursorY, 140, PINK, 0.05f + 0.05f * dk);
    }

    // =====================================================================
    // SURFACES
    // =====================================================================

    // ---- cached static layers (large anti-aliased fills are the main per-frame cost)

    private static final long MAX_LAYER_PIXELS = 24_000_000L;
    private static long layerPixels = 0;
    private static final java.util.LinkedHashMap<String, BufferedImage> layers =
            new java.util.LinkedHashMap<String, BufferedImage>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) {
                    if (layerPixels > MAX_LAYER_PIXELS && size() > 1) {
                        BufferedImage v = e.getValue();
                        layerPixels -= (long) v.getWidth() * v.getHeight();
                        return true;
                    }
                    return false;
                }
            };

    private interface LayerPainter { void paint(Graphics2D g); }

    private static synchronized BufferedImage layer(String key, int w, int h, LayerPainter p) {
        BufferedImage img = layers.get(key);
        if (img != null) return img;
        img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        Draw.setQualityHints(g);
        p.paint(g);
        g.dispose();
        layerPixels += (long) img.getWidth() * img.getHeight();
        layers.put(key, img);
        return img;
    }

    private static void rawShadow(Graphics2D g, float x, float y, float w, float h, float r, float strength, float lift, boolean darkMode) {
        if (strength <= 0.01f) return;
        Color sc = darkMode ? new Color(0, 0, 0) : new Color(40, 30, 90);
        int n = 5;
        for (int i = 0; i < n; i++) {
            float k = (i + 1) / (float) n;
            float grow = k * lift * 0.9f;
            int alpha = Math.round(255 * strength * (1f - k) * 0.22f);
            if (alpha <= 0) continue;
            g.setColor(a(sc, alpha));
            g.fill(rr(x - grow * 0.5f, y + lift * 0.45f * k, w + grow, h + grow * 0.4f, r + grow * 0.5f));
        }
    }

    /** Soft layered drop shadow (cached per size / strength). */
    public static void softShadow(Graphics2D g, float x, float y, float w, float h, float r, float strength, float lift) {
        if (strength <= 0.01f) return;
        final int iw = Math.round(w), ih = Math.round(h), ir = Math.round(r);
        final float st = Math.round(strength * 40f) / 40f;
        final int lf = Math.round(lift);
        final boolean dm = dark();
        final int m = lf + 4;
        String key = "sh:" + iw + "x" + ih + ":" + ir + ":" + st + ":" + lf + ":" + dm;
        BufferedImage img = layer(key, iw + m * 2, ih + m * 2 + lf, gg -> rawShadow(gg, m, m, iw, ih, ir, st, lf, dm));
        g.drawImage(img, Math.round(x) - m, Math.round(y) - m, null);
    }

    /**
     * Frosted glass card with cursor spotlight.
     * hover: 0..1, accent may be null (neutral).
     * The static body (shadow, glass fill, border, sheen) is cached; only the
     * cursor-driven light is drawn live.
     */
    public static void card(Graphics2D g, float x, float y, float w, float h, float r, float hover, Color accent) {
        final float dk = 1f - Theme.themeTransition;
        final int iw = Math.round(w), ih = Math.round(h), ir = Math.round(r);
        final int hq = Math.round(clamp(hover) * 8f);
        final int tq = Math.round(Theme.themeTransition * 16f);
        final float hv = hq / 8f;
        final int m = 40;
        String key = "card:" + iw + "x" + ih + ":" + ir + ":" + hq + ":" + tq;
        BufferedImage body = layer(key, iw + m * 2, ih + m * 2, gg -> {
            float d = 1f - tq / 16f;
            float bx = m, by = m;
            RoundRectangle2D.Float sh = rr(bx, by, iw, ih, ir);
            rawShadow(gg, bx, by, iw, ih, ir, (0.35f * d + 0.12f * (1 - d)) + hv * 0.25f, 14 + hv * 18, d > 0.5f);
            gg.setColor(Theme.lerp(new Color(0x0C, 0x0C, 0x1C, 200), new Color(255, 255, 255, 230), 1 - d));
            gg.fill(sh);
            gg.setPaint(new GradientPaint(0, by, Theme.lerp(new Color(255, 255, 255, 14), new Color(255, 255, 255, 120), 1 - d),
                    0, by + ih, Theme.lerp(new Color(255, 255, 255, 4), new Color(250, 249, 255, 60), 1 - d)));
            gg.fill(sh);
            gg.setStroke(new BasicStroke(1f));
            gg.setColor(Theme.lerp(new Color(255, 255, 255, 22), new Color(20, 18, 50, 24), 1 - d));
            gg.draw(sh);
            if (d > 0.3f) {
                gg.setPaint(new LinearGradientPaint(bx, by, bx + iw, by, new float[]{0f, 0.5f, 1f},
                        new Color[]{a(Color.WHITE, 0), a(Color.WHITE, Math.round(60 * d)), a(Color.WHITE, 0)}));
                gg.draw(new Line2D.Float(bx + ir, by + 0.5f, bx + iw - ir, by + 0.5f));
            }
        });
        int px = Math.round(x), py = Math.round(y);
        g.drawImage(body, px - m, py - m, null);

        // Inner spotlight that follows the cursor
        if (accent != null && hover > 0.01f) {
            Graphics2D c = (Graphics2D) g.create();
            float in = r * 0.3f;
            c.clip(new Rectangle2D.Float(px + in, py + in, iw - in * 2, ih - in * 2));
            glow(c, Anim.mouseX, Anim.mouseY, Math.max(w, h) * 0.75f, accent, (0.10f + 0.08f * dk) * hover);
            c.dispose();
        }
        if (accent != null) spotlightBorder(g, rr(px, py, iw, ih, r), accent, 0.55f + 0.45f * hover);
    }

    /** Border that lights up near the cursor. */
    public static void spotlightBorder(Graphics2D g, Shape s, Color accent, float strength) {
        if (!Theme.motionEnabled) return;
        Rectangle b = s.getBounds();
        float mx = Anim.mouseX, my = Anim.mouseY;
        float range = 240f;
        float dx = Math.max(Math.max(b.x - mx, 0), mx - (b.x + b.width));
        float dy = Math.max(Math.max(b.y - my, 0), my - (b.y + b.height));
        if (dx * dx + dy * dy > range * range) return;
        Paint old = g.getPaint();
        Stroke os = g.getStroke();
        g.setPaint(new RadialGradientPaint(new Point2D.Float(mx, my), range, new float[]{0f, 1f},
                new Color[]{a(accent, Math.round(230 * clamp(strength))), a(accent, 0)}));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(s);
        g.setStroke(os);
        g.setPaint(old);
    }

    /** Rotating conic-ish gradient border (used on hero CTA panel & highlighted cards). */
    public static void gradientBorder(Graphics2D g, Shape s, float width, float alpha, float angleShift) {
        Rectangle b = s.getBounds();
        float cx = b.x + b.width / 2f, cy = b.y + b.height / 2f;
        float ang = angleShift;
        float rx = (float) Math.cos(ang) * b.width / 2f, ry = (float) Math.sin(ang) * b.height / 2f;
        Paint old = g.getPaint();
        Stroke os = g.getStroke();
        Composite oc = g.getComposite();
        fadeBy(g, alpha);
        g.setPaint(new LinearGradientPaint(new Point2D.Float(cx - rx, cy - ry), new Point2D.Float(cx + rx + 0.5f, cy + ry + 0.5f),
                new float[]{0f, 0.5f, 1f}, BRAND, MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.setStroke(new BasicStroke(width));
        g.draw(s);
        g.setComposite(oc);
        g.setStroke(os);
        g.setPaint(old);
    }

    // =====================================================================
    // BUTTONS
    // =====================================================================

    public static void arrow(Graphics2D g, float x, float cy, float size, Color c, float strokeW) {
        Graphics2D a = (Graphics2D) g.create();
        a.setColor(c);
        a.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x, cy);
        p.lineTo(x + size, cy);
        p.moveTo(x + size * 0.55f, cy - size * 0.42f);
        p.lineTo(x + size, cy);
        p.lineTo(x + size * 0.55f, cy + size * 0.42f);
        a.draw(p);
        a.dispose();
    }

    /** Primary gradient pill with glow, shine sweep and sliding arrow. */
    public static void primaryButton(Graphics2D g, float x, float y, float w, float h, String text,
                                     float hover, float press, boolean withArrow) {
        Graphics2D b = (Graphics2D) g.create();
        float s = 1f - press * 0.03f;
        b.translate(x + w / 2f, y + h / 2f);
        b.scale(s, s);
        b.translate(-(x + w / 2f), -(y + h / 2f));

        RoundRectangle2D.Float shape = rr(x, y, w, h, h / 2f);

        // Under-glow
        glowOval(b, x + w / 2f, y + h * 0.75f, w * 0.62f, h * 1.1f, PINK, 0.25f + 0.35f * hover);

        // Gradient fill (slides on hover)
        float sh = hover * w * 0.35f;
        b.setPaint(new LinearGradientPaint(new Point2D.Float(x - sh, y), new Point2D.Float(x + w * 1.35f - sh, y + h),
                new float[]{0f, 0.55f, 1f}, new Color[]{PINK, mix(PINK, VIOLET, 0.6f), VIOLET}));
        b.fill(shape);

        // Inner top highlight
        b.setPaint(new GradientPaint(0, y, a(Color.WHITE, 60), 0, y + h * 0.55f, a(Color.WHITE, 0)));
        b.fill(rr(x + 1, y + 1, w - 2, h * 0.55f, h / 2f - 1));

        // Shine sweep every few seconds
        if (Theme.motionEnabled) {
            float cyc = (Anim.globalTime % 3.6f) / 3.6f;
            float sx = x - w * 0.4f + cyc * w * 2.2f;
            if (sx < x + w + 40) {
                Paint op = b.getPaint();
                b.setPaint(new LinearGradientPaint(sx, y, sx + 50, y, new float[]{0f, 0.5f, 1f},
                        new Color[]{a(Color.WHITE, 0), a(Color.WHITE, 70), a(Color.WHITE, 0)}));
                b.fill(shape);
                b.setPaint(op);
            }
        }

        b.setColor(a(Color.WHITE, 50));
        b.setStroke(new BasicStroke(1f));
        b.draw(shape);

        // Label
        b.setFont(bold(15));
        FontMetrics fm = b.getFontMetrics();
        float arrowW = withArrow ? 14 + 10 : 0;
        float tw = fm.stringWidth(text) + arrowW;
        float tx = x + (w - tw) / 2f;
        b.setColor(Color.WHITE);
        b.drawString(text, tx, y + (h - fm.getHeight()) / 2f + fm.getAscent());
        if (withArrow) {
            arrow(b, tx + fm.stringWidth(text) + 10 + hover * 4f, y + h / 2f, 12, Color.WHITE, 2f);
        }
        b.dispose();
    }

    /** Secondary glass pill. */
    public static void ghostButton(Graphics2D g, float x, float y, float w, float h, String text, float hover, boolean withArrow) {
        RoundRectangle2D.Float shape = rr(x, y, w, h, h / 2f);
        g.setColor(mix(surface(), surfaceHi(), hover));
        g.fill(shape);
        g.setColor(mix(border(), borderHi(), hover));
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        spotlightBorder(g, shape, VIOLET, 0.6f * hover);

        g.setFont(bold(15));
        FontMetrics fm = g.getFontMetrics();
        float arrowW = withArrow ? 22 : 0;
        float tw = fm.stringWidth(text) + arrowW;
        float tx = x + (w - tw) / 2f;
        g.setColor(text());
        g.drawString(text, tx, y + (h - fm.getHeight()) / 2f + fm.getAscent());
        if (withArrow) arrow(g, tx + fm.stringWidth(text) + 10 + hover * 4f, y + h / 2f, 11, text(), 1.8f);
    }

    // =====================================================================
    // SECTION CHROME
    // =====================================================================

    /** Eyebrow pill centred on cx. Returns its height. */
    public static int eyebrow(Graphics2D g, String text, float cx, float y, Color accent) {
        Font f = label(11.5f);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int h = 30;
        int w = fm.stringWidth(text) + 44;
        float x = cx - w / 2f;
        RoundRectangle2D.Float pill = rr(x, y, w, h, h / 2f);
        g.setColor(a(accent, dark() ? 22 : 18));
        g.fill(pill);
        g.setColor(a(accent, dark() ? 70 : 80));
        g.setStroke(new BasicStroke(1f));
        g.draw(pill);
        // pulsing dot
        float pulse = Theme.motionEnabled ? (Anim.globalTime % 2f) / 2f : 0f;
        glow(g, x + 16, y + h / 2f, 10 + pulse * 6, accent, 0.6f * (1 - pulse));
        g.setColor(accent);
        g.fill(new Ellipse2D.Float(x + 13, y + h / 2f - 3, 6, 6));
        g.setColor(ink(accent));
        g.drawString(text, x + 27, y + (h - fm.getHeight()) / 2f + fm.getAscent());
        return h;
    }

    /**
     * Centred section header: eyebrow, two-tone headline (second part gradient), subtitle.
     * Returns the y beneath the header.
     */
    public static int header(Graphics2D g, Reveal rv, int width, int y,
                             String eyebrow, Color accent, String titleA, String titleB, String sub) {
        int[] c = container(width);
        float cx = width / 2f;

        Graphics2D e = fade(g, rv.p(0f), 18);
        eyebrow(e, eyebrow, cx, y, accent);
        e.dispose();
        y += 30 + 22;

        // Fit headline
        float size = width < 700 ? 32 : (width < 1100 ? 40 : 48);
        Font tf = heading(size);
        int full = textW(g, tf, titleA + titleB);
        boolean split = full > c[1];
        if (split) {
            while (size > 26 && Math.max(textW(g, heading(size), titleA), textW(g, heading(size), titleB)) > c[1]) size -= 2;
            tf = heading(size);
        }
        g.setFont(tf);
        FontMetrics fm = g.getFontMetrics();
        int lineH = Math.round(fm.getHeight() * 1.04f);

        Graphics2D t = fade(g, rv.p(0.08f), 26);
        t.setFont(tf);
        if (!split) {
            float x = cx - full / 2f;
            t.setColor(text());
            t.drawString(titleA, x, y + fm.getAscent());
            gradientText(t, titleB, x + fm.stringWidth(titleA), y + fm.getAscent(), BRAND, wave(0.6f, 0) * 0.25f);
            y += lineH;
        } else {
            t.setColor(text());
            t.drawString(titleA.trim(), cx - fm.stringWidth(titleA.trim()) / 2f, y + fm.getAscent());
            y += lineH;
            gradientText(t, titleB.trim(), cx - fm.stringWidth(titleB.trim()) / 2f, y + fm.getAscent(), BRAND, wave(0.6f, 0) * 0.25f);
            y += lineH;
        }
        t.dispose();

        if (sub != null && !sub.isEmpty()) {
            y += 14;
            int maxW = Math.min(660, c[1]);
            Graphics2D s = fade(g, rv.p(0.16f), 26);
            y = paragraph(s, sub, body(width < 700 ? 15.5f : 17.5f), text2(), Math.round(cx - maxW / 2f), y, maxW, 1.45f, true);
            s.dispose();
        }
        return y;
    }

    // =====================================================================
    // SMALL VISUALS
    // =====================================================================

    /** Gradient icon tile with a Draw.* glyph inside. kind: search, users, shield, task, chat, star, spark, bolt, layers, lock. */
    public static void iconTile(Graphics2D g, float x, float y, float size, Color accent, String kind, float hover) {
        RoundRectangle2D.Float tile = rr(x, y, size, size, size * 0.28f);
        glow(g, x + size / 2f, y + size / 2f, size * (0.9f + hover * 0.4f), accent, 0.18f + 0.22f * hover);
        Paint old = g.getPaint();
        g.setPaint(new GradientPaint(x, y, a(accent, dark() ? 60 : 40), x + size, y + size, a(accent, dark() ? 18 : 14)));
        g.fill(tile);
        g.setPaint(old);
        g.setColor(a(accent, dark() ? 110 : 90));
        g.setStroke(new BasicStroke(1f));
        g.draw(tile);
        icon(g, kind, x + size * 0.22f, y + size * 0.22f, size * 0.56f, ink(accent));
    }

    public static void icon(Graphics2D g, String kind, float x, float y, float s, Color c) {
        int ix = Math.round(x), iy = Math.round(y), is = Math.round(s);
        switch (kind) {
            case "search": Draw.drawSearchIcon(g, ix, iy, is, c); break;
            case "users":  Draw.drawUsersIcon(g, ix, iy, is, c); break;
            case "shield": Draw.drawShieldIcon(g, ix, iy, is, c); break;
            case "task":   Draw.drawTaskIcon(g, ix, iy, is, c); break;
            case "chat":   Draw.drawChatIcon(g, ix, iy, is, c); break;
            case "star":   Draw.drawStarIcon(g, ix, iy, is, c); break;
            case "spark":  Draw.drawSparkleIcon(g, ix, iy, is, c); break;
            case "check":  Draw.drawCheckIcon(g, ix, iy, is, c); break;
            case "bolt":   bolt(g, x, y, s, c); break;
            case "lock":   lock(g, x, y, s, c, 1f); break;
            case "chart":  chart(g, x, y, s, c); break;
            case "rocket": rocket(g, x, y, s, c); break;
            default:       Draw.drawSparkleIcon(g, ix, iy, is, c);
        }
    }

    public static void bolt(Graphics2D g, float x, float y, float s, Color c) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + s * 0.58f, y + s * 0.05f);
        p.lineTo(x + s * 0.18f, y + s * 0.56f);
        p.lineTo(x + s * 0.48f, y + s * 0.56f);
        p.lineTo(x + s * 0.40f, y + s * 0.95f);
        p.lineTo(x + s * 0.82f, y + s * 0.42f);
        p.lineTo(x + s * 0.52f, y + s * 0.42f);
        p.closePath();
        g.setColor(c);
        g.fill(p);
    }

    /** Padlock; closed: 0 = open shackle, 1 = closed. */
    public static void lock(Graphics2D g, float x, float y, float s, Color c, float closed) {
        Graphics2D l = (Graphics2D) g.create();
        l.setColor(c);
        l.setStroke(new BasicStroke(Math.max(1.6f, s * 0.1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        l.draw(rr(x + s * 0.18f, y + s * 0.45f, s * 0.64f, s * 0.48f, s * 0.1f));
        float lift = (1f - clamp(closed)) * s * 0.16f;
        Path2D.Float sh = new Path2D.Float();
        sh.moveTo(x + s * 0.32f, y + s * 0.45f - lift * 0.2f);
        sh.lineTo(x + s * 0.32f, y + s * 0.3f - lift);
        sh.curveTo(x + s * 0.32f, y + s * 0.05f - lift, x + s * 0.68f, y + s * 0.05f - lift, x + s * 0.68f, y + s * 0.3f - lift);
        sh.lineTo(x + s * 0.68f, y + s * 0.45f - lift * 1.6f);
        l.draw(sh);
        l.fill(new Ellipse2D.Float(x + s * 0.45f, y + s * 0.6f, s * 0.1f, s * 0.1f));
        l.dispose();
    }

    public static void chart(Graphics2D g, float x, float y, float s, Color c) {
        g.setColor(c);
        float bw = s * 0.16f;
        float[] hs = {0.35f, 0.6f, 0.85f};
        for (int i = 0; i < 3; i++) {
            float bh = s * hs[i];
            g.fill(rr(x + s * 0.12f + i * s * 0.28f, y + s * 0.92f - bh, bw, bh, bw / 2f));
        }
    }

    public static void rocket(Graphics2D g, float x, float y, float s, Color c) {
        Graphics2D r = (Graphics2D) g.create();
        r.setColor(c);
        r.setStroke(new BasicStroke(Math.max(1.6f, s * 0.09f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + s * 0.5f, y + s * 0.05f);
        p.curveTo(x + s * 0.78f, y + s * 0.25f, x + s * 0.72f, y + s * 0.6f, x + s * 0.64f, y + s * 0.72f);
        p.lineTo(x + s * 0.36f, y + s * 0.72f);
        p.curveTo(x + s * 0.28f, y + s * 0.6f, x + s * 0.22f, y + s * 0.25f, x + s * 0.5f, y + s * 0.05f);
        r.draw(p);
        r.fill(new Ellipse2D.Float(x + s * 0.43f, y + s * 0.3f, s * 0.14f, s * 0.14f));
        r.draw(new Line2D.Float(x + s * 0.42f, y + s * 0.84f, x + s * 0.42f, y + s * 0.95f));
        r.draw(new Line2D.Float(x + s * 0.58f, y + s * 0.84f, x + s * 0.58f, y + s * 0.95f));
        r.dispose();
    }

    public static void checkCircle(Graphics2D g, float x, float y, float s, Color c, float p) {
        if (p <= 0.01f) return;
        Graphics2D k = pop(g, p, x + s / 2f, y + s / 2f);
        k.setColor(a(c, dark() ? 40 : 30));
        k.fill(new Ellipse2D.Float(x, y, s, s));
        k.setColor(a(c, 120));
        k.setStroke(new BasicStroke(1f));
        k.draw(new Ellipse2D.Float(x, y, s, s));
        Draw.drawCheckIcon(k, Math.round(x + s * 0.12f), Math.round(y + s * 0.12f), Math.round(s * 0.76f), ink(c));
        k.dispose();
    }

    /** Avatar with gradient ring. */
    public static void avatar(Graphics2D g, float x, float y, float s, String initials, Color c1, Color c2, boolean ring) {
        if (ring) {
            g.setPaint(new GradientPaint(x, y, c1, x + s, y + s, c2));
            g.fill(new Ellipse2D.Float(x - 2.5f, y - 2.5f, s + 5, s + 5));
            g.setColor(bg());
            g.fill(new Ellipse2D.Float(x - 1, y - 1, s + 2, s + 2));
        }
        Draw.drawAvatar(g, Math.round(x), Math.round(y), Math.round(s), initials, c1, c2);
    }

    /** Small pill tag. Returns its width. */
    public static int tag(Graphics2D g, String text, float x, float y, Color accent, boolean filled) {
        Font f = bold(11.5f);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int h = 22;
        int w = fm.stringWidth(text) + 18;
        RoundRectangle2D.Float pill = rr(x, y, w, h, h / 2f);
        if (filled) {
            g.setPaint(new GradientPaint(x, y, accent, x + w, y, mix(accent, VIOLET, 0.5f)));
            g.fill(pill);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(a(accent, dark() ? 26 : 20));
            g.fill(pill);
            g.setColor(a(accent, dark() ? 60 : 70));
            g.setStroke(new BasicStroke(1f));
            g.draw(pill);
            g.setColor(ink(accent));
        }
        g.drawString(text, x + 9, y + (h - fm.getHeight()) / 2f + fm.getAscent());
        return w;
    }

    // =====================================================================
    // REVEAL — scroll-triggered, staggered entrance animations
    // =====================================================================

    // =====================================================================
    // BLUR (for frosted glass backdrops)
    // =====================================================================

    /** In-place separable box blur; 3 passes approximate a Gaussian. */
    public static void blur(BufferedImage img, int radius, int passes) {
        int w = img.getWidth(), h = img.getHeight();
        if (radius < 1 || w < 2 || h < 2) return;
        int[] a = img.getRGB(0, 0, w, h, null, 0, w);
        int[] b = new int[a.length];
        for (int p = 0; p < passes; p++) {
            boxPass(a, b, w, h, radius, true);
            boxPass(b, a, w, h, radius, false);
        }
        img.setRGB(0, 0, w, h, a, 0, w);
    }

    private static void boxPass(int[] src, int[] dst, int w, int h, int r, boolean horizontal) {
        int lines = horizontal ? h : w, len = horizontal ? w : h;
        int div = r * 2 + 1;
        for (int l = 0; l < lines; l++) {
            int base = horizontal ? l * w : l;
            int step = horizontal ? 1 : w;
            int sr = 0, sg = 0, sb = 0;
            for (int k = -r; k <= r; k++) {
                int c = src[base + clampI(k, 0, len - 1) * step];
                sr += (c >> 16) & 255; sg += (c >> 8) & 255; sb += c & 255;
            }
            for (int i = 0; i < len; i++) {
                dst[base + i * step] = 0xFF000000 | ((sr / div) << 16) | ((sg / div) << 8) | (sb / div);
                int out = src[base + clampI(i - r, 0, len - 1) * step];
                int in = src[base + clampI(i + r + 1, 0, len - 1) * step];
                sr += ((in >> 16) & 255) - ((out >> 16) & 255);
                sg += ((in >> 8) & 255) - ((out >> 8) & 255);
                sb += (in & 255) - (out & 255);
            }
        }
    }

    /** Smooth (bilinear) upscale into a new opaque image. */
    public static BufferedImage upscale(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    public static BufferedImage copy(BufferedImage src) {
        BufferedImage c = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = c.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return c;
    }

    private static BufferedImage grain;

    /** Subtle film-grain tile that makes glass read as a physical material. */
    public static BufferedImage grainTile() {
        if (grain == null) {
            java.util.Random r = new java.util.Random(11);
            grain = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    int v = r.nextBoolean() ? 255 : 0;
                    int al = r.nextInt(14);
                    grain.setRGB(x, y, (al << 24) | (v << 16) | (v << 8) | v);
                }
            }
        }
        return grain;
    }

    public static class Reveal {
        private float t = -1f;

        /** Arms the reveal once the element's top crosses 88% of the viewport. */
        public void check(int screenTop, int viewportH) {
            if (t < 0f && viewportH > 0 && screenTop < viewportH * 0.88f) t = 0f;
        }

        public void update(float dt) {
            if (t >= 0f) t += dt;
            if (!Theme.motionEnabled && t >= 0f) t = 100f;
        }

        public boolean started() { return t >= 0f; }
        public float elapsed() { return Math.max(0f, t); }

        public float p(float delay) { return p(delay, 0.75f); }

        public float p(float delay, float dur) {
            if (t < 0f) return 0f;
            return easeOutQuart((t - delay) / dur);
        }

        /** Raw linear progress (for counters etc). */
        public float lin(float delay, float dur) {
            if (t < 0f) return 0f;
            return clamp((t - delay) / dur);
        }
    }
}
