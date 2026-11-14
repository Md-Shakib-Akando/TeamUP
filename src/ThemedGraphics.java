import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

/**
 * ThemedGraphics — paints the dashboards with a restrained, professional palette.
 *
 * The dashboards were written against a light palette with ~600 hard-coded colours.
 * Instead of rewriting every call site, this Graphics2D decorator remaps colours at
 * draw time, following {@link Theme#themeTransition} (so the landing page's
 * dark/light toggle also applies to the dashboards):
 *
 *  - both modes: every chromatic colour is folded onto one of four hues — accent
 *    blue (#2563EB), success green, warning amber, danger red — keeping its lightness, so
 *    cards don't mix violet, cyan, pink, teal, etc. Tinted neutrals (warm stone,
 *    violet ink) are put on one cool slate hue;
 *  - dark mode: neutrals map onto one cool-grey ramp (white cards → #1D1F23,
 *    ink text → near-white, light borders → hairlines), pastel tints become
 *    muted deep tints;
 *  - the page canvas and {@link #SURFACE_GLASS} (sidebar/header) are flat,
 *    opaque surfaces — no aurora glow behind the dashboards.
 *
 * getColor()/getPaint() return what the caller set, so existing code that reads
 * colours back keeps working. Colour mapping depends on the operation: text,
 * fills and strokes are remapped differently (white text on a coloured button
 * stays white, a white card becomes dark).
 */
public class ThemedGraphics extends Graphics2D {

    /** Sentinel for sidebar/header backgrounds (visually identical to white). */
    public static final Color SURFACE_GLASS = new Color(0xFF, 0xFF, 0xFE);

    private static final int CANVAS_A = 0xF8F7F4, CANVAS_B = 0xF8FAFC;
    /** Dashboard accent (buttons, links, active states): professional blue. Change here to re-brand. */
    private static final int ACCENT = 0x2563EB;

    private static final int FILL = 0, TEXT = 1, STROKE = 2;

    private final Graphics2D g;
    private Color color = Color.BLACK;   // caller's colour
    private Paint paint = Color.BLACK;   // caller's paint
    private boolean paintIsColor = true;
    private int applied = -1;            // mode the delegate currently holds

    private static final Map<Long, Color> cache = new HashMap<>();

    public ThemedGraphics(Graphics2D delegate) {
        this.g = delegate;
        Paint p = delegate.getPaint();
        if (p instanceof Color) { color = (Color) p; paint = p; }
    }

    private ThemedGraphics(Graphics2D delegate, Color c, Paint p, boolean isColor) {
        this.g = delegate;
        this.color = c;
        this.paint = p;
        this.paintIsColor = isColor;
    }

    // =====================================================================
    // Colour mapping
    // =====================================================================

    static Color map(Color c, int mode) {
        float t = Theme.themeTransition; // 0 dark, 1 light
        int tq = Math.round(t * 32f);
        long key = ((long) c.getRGB() << 8) ^ (mode << 6) ^ tq;
        synchronized (cache) {
            Color m = cache.get(key);
            if (m != null) return m;
            m = compute(c, mode, tq / 32f);
            if (cache.size() > 20000) cache.clear();
            cache.put(key, m);
            return m;
        }
    }

    // ---------------------------------------------------------------------
    // Restrained palette: one brand accent + three semantic colours.
    // Every hard-coded hue in the dashboards is folded onto one of these, so
    // a card never shows violet, cyan, indigo, sky, teal and pink side by side.
    // ---------------------------------------------------------------------
    private static final float BRAND_HUE   = hsl(new Color(ACCENT))[0]; // #2563EB blue
    private static final float SUCCESS_HUE = 152f / 360f;
    private static final float WARNING_HUE = 38f / 360f;
    private static final float DANGER_HUE  = 0f;

    /** Cool neutral grey used for every neutral (no warm/violet cast). */
    private static final float NEUTRAL_HUE = 220f / 360f, NEUTRAL_SAT = 0.10f;

    /** Flat dark canvas / chrome — replaces the aurora glow behind the dashboards. */
    private static final Color DARK_CANVAS  = new Color(0x0D, 0x0F, 0x13);
    private static final Color DARK_CHROME  = new Color(0x13, 0x15, 0x1A);
    private static final Color LIGHT_CANVAS = new Color(0xF5, 0xF7, 0xFA);

    /** Slate / grey scale values whose HSL saturation looks "colourful" but are neutral. */
    private static final java.util.Set<Integer> NEUTRALS = new java.util.HashSet<>(java.util.Arrays.asList(
            0xF8FAFC, 0xF1F5F9, 0xE2E8F0, 0xCBD5E1, 0x94A3B8, 0x64748B, 0x475569, 0x334155, 0x1E293B, 0x0F172A, 0x020617,
            0xF9FAFB, 0xF3F4F6, 0xE5E7EB, 0xD1D5DB, 0x9CA3AF, 0x6B7280, 0x4B5563, 0x374151, 0x1F2937, 0x111827,
            0x0D0C22));

    private static boolean isNeutral(Color c, float s) {
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        return (max - min) < 10 || s < 0.30f || NEUTRALS.contains(c.getRGB() & 0xFFFFFF);
    }

    /** Folds a chromatic colour onto the brand / success / warning / danger hue, keeping its lightness. */
    private static Color normalize(Color c, float[] hsl) {
        float deg = hsl[0] * 360f;
        float hue;
        float maxSat;
        if (deg >= 341f || deg < 15f)  { hue = DANGER_HUE;  maxSat = 0.80f; }
        else if (deg < 70f)            { hue = WARNING_HUE; maxSat = 0.90f; }
        else if (deg < 175f)           { hue = SUCCESS_HUE; maxSat = 0.75f; }
        else                           { hue = BRAND_HUE;   maxSat = 0.80f; }
        float l = hsl[2];
        // The old pink sat at L 0.61; a blue carrying white button text needs to sit a little deeper.
        if (hue == BRAND_HUE && l >= 0.35f && l <= 0.80f) l -= 0.08f;
        return fromHsl(hue, Math.min(hsl[1], maxSat), l, c.getAlpha());
    }

    /** Puts every tinted neutral (warm stone, violet ink) on one cool slate hue, in both themes. */
    private static Color coolNeutral(Color c, float[] hsl) {
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        if (max - min < 3) return c; // pure white / grey
        return fromHsl(NEUTRAL_HUE, Math.min(hsl[1], 0.25f), hsl[2], c.getAlpha());
    }

    /** Paints the dashboard backdrop: a flat canvas in both themes. */
    public static void paintCanvas(Graphics2D g, int w, int h) {
        g.setColor(Theme.lerp(DARK_CANVAS, LIGHT_CANVAS, Theme.themeTransition));
        g.fillRect(0, 0, w, h);
    }

    private static Color compute(Color c, int mode, float light) {
        int rgb = c.getRGB() & 0xFFFFFF;
        int a = c.getAlpha();
        if (mode == FILL && (rgb == CANVAS_A || rgb == CANVAS_B)) {
            Color dark = new Color(DARK_CANVAS.getRed(), DARK_CANVAS.getGreen(), DARK_CANVAS.getBlue(), a);
            Color lite = new Color(LIGHT_CANVAS.getRed(), LIGHT_CANVAS.getGreen(), LIGHT_CANVAS.getBlue(), a);
            return Theme.lerp(dark, lite, light);
        }
        if (mode == FILL && rgb == (SURFACE_GLASS.getRGB() & 0xFFFFFF)) {
            return Theme.lerp(DARK_CHROME, Color.WHITE, light);
        }
        float[] hsl = hsl(c);
        boolean neutral = isNeutral(c, hsl[1]);
        Color base = neutral ? coolNeutral(c, hsl) : normalize(c, hsl);
        if (light >= 0.999f) return base;
        Color d = darkVariant(base, mode, neutral);
        return light <= 0.001f ? d : Theme.lerp(d, base, light);
    }

    private static Color darkVariant(Color c, int mode, boolean neutral) {
        float[] hsl = hsl(c);
        float h = hsl[0], s = hsl[1], l = hsl[2];
        int a = c.getAlpha();

        if (mode == TEXT) {
            if (neutral) {
                if (l > 0.85f) return c;                                         // white text on colour stays white
                return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, 0.95f - l * 0.70f, a);  // ink -> light, grey -> mid
            }
            if (l < 0.55f) return fromHsl(h, Math.min(s, 0.80f), 0.68f, a);
            return c;
        }
        if (mode == STROKE) {
            if (neutral) {
                if (l >= 0.8f) return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, 0.19f, a); // light borders -> hairlines
                return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, 0.95f - l * 0.70f, a);
            }
            if (l >= 0.85f) return fromHsl(h, Math.min(s, 0.45f), 0.30f, a);
            if (l < 0.55f) return fromHsl(h, Math.min(s, 0.80f), 0.66f, a);
            return c;
        }
        // FILL
        if (neutral) {
            if (l >= 0.86f) return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, 0.075f + (l - 0.86f) * 0.36f, a); // white -> #1D1F23
            if (l >= 0.55f) return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, 0.98f - l * 0.80f, a);           // tracks, controls
            if (a > 200) return fromHsl(NEUTRAL_HUE, NEUTRAL_SAT, Math.max(l, 0.22f), a);             // dark buttons stay visible
            return c;                                                                                 // shadows, scrims
        }
        if (l >= 0.85f) return fromHsl(h, Math.min(s, 0.30f), 0.14f + (l - 0.85f) * 0.3f, a); // pastel -> muted deep tint
        if (l < 0.3f) return fromHsl(h, s, 0.42f, a);
        return c;
    }

    private static float[] hsl(Color c) {
        float r = c.getRed() / 255f, gg = c.getGreen() / 255f, b = c.getBlue() / 255f;
        float max = Math.max(r, Math.max(gg, b)), min = Math.min(r, Math.min(gg, b));
        float l = (max + min) / 2f, h = 0f, s = 0f;
        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == r) h = (gg - b) / d + (gg < b ? 6f : 0f);
            else if (max == gg) h = (b - r) / d + 2f;
            else h = (r - gg) / d + 4f;
            h /= 6f;
        }
        return new float[]{h, s, l};
    }

    private static Color fromHsl(float h, float s, float l, int a) {
        l = Math.max(0f, Math.min(1f, l));
        s = Math.max(0f, Math.min(1f, s));
        float r, gg, b;
        if (s == 0f) {
            r = gg = b = l;
        } else {
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            r = hue(p, q, h + 1f / 3f);
            gg = hue(p, q, h);
            b = hue(p, q, h - 1f / 3f);
        }
        return new Color(Math.round(r * 255), Math.round(gg * 255), Math.round(b * 255), a);
    }

    private static float hue(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    private Paint mapPaint(Paint p) {
        if (p instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint) p;
            return new GradientPaint(gp.getPoint1(), map(gp.getColor1(), FILL), gp.getPoint2(), map(gp.getColor2(), FILL), gp.isCyclic());
        }
        if (p instanceof LinearGradientPaint) {
            LinearGradientPaint lp = (LinearGradientPaint) p;
            return new LinearGradientPaint(lp.getStartPoint(), lp.getEndPoint(), lp.getFractions(), mapAll(lp.getColors()),
                    lp.getCycleMethod(), lp.getColorSpace(), lp.getTransform());
        }
        if (p instanceof RadialGradientPaint) {
            RadialGradientPaint rp = (RadialGradientPaint) p;
            return new RadialGradientPaint(rp.getCenterPoint(), rp.getRadius(), rp.getFocusPoint(), rp.getFractions(),
                    mapAll(rp.getColors()), rp.getCycleMethod(), rp.getColorSpace(), rp.getTransform());
        }
        return p;
    }

    private static Color[] mapAll(Color[] cs) {
        Color[] out = new Color[cs.length];
        for (int i = 0; i < cs.length; i++) out[i] = map(cs[i], FILL);
        return out;
    }

    /** Puts the right mapped colour on the delegate for the coming operation. */
    private void prep(int mode) {
        if (!paintIsColor) return;
        if (applied != mode) {
            g.setColor(map(color, mode));
            applied = mode;
        }
    }

    /** Small white fills (toggle knobs, dots, icon cut-outs) keep their colour. */
    private boolean keepSmallWhite(double w, double h) {
        if (!paintIsColor) return false;
        int rgb = color.getRGB() & 0xFFFFFF;
        return rgb == 0xFFFFFF && w <= 30 && h <= 30;
    }

    private void fillShape(Shape s) {
        Rectangle2D b = s.getBounds2D();
        if (keepSmallWhite(b.getWidth(), b.getHeight())) {
            g.setColor(color);
            applied = -1;
            g.fill(s);
            return;
        }
        prep(FILL);
        g.fill(s);
    }

    // =====================================================================
    // Colour / paint state
    // =====================================================================

    @Override public Color getColor() { return color; }

    @Override
    public void setColor(Color c) {
        if (c == null) return;
        color = c;
        paint = c;
        paintIsColor = true;
        applied = -1;
    }

    @Override public Paint getPaint() { return paint; }

    @Override
    public void setPaint(Paint p) {
        if (p == null) return;
        if (p instanceof Color) { setColor((Color) p); return; }
        paint = p;
        paintIsColor = false;
        applied = -1;
        g.setPaint(mapPaint(p));
    }

    @Override public void setBackground(Color c) { g.setBackground(c == null ? null : map(c, FILL)); }
    @Override public Color getBackground() { return g.getBackground(); }

    // =====================================================================
    // Shapes
    // =====================================================================

    @Override public void fill(Shape s) { fillShape(s); }
    @Override public void draw(Shape s) { prep(STROKE); g.draw(s); }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        if (keepSmallWhite(w, h)) { g.setColor(color); applied = -1; g.fillRect(x, y, w, h); return; }
        prep(FILL);
        g.fillRect(x, y, w, h);
    }

    @Override public void clearRect(int x, int y, int w, int h) { g.clearRect(x, y, w, h); }
    @Override public void drawLine(int x1, int y1, int x2, int y2) { prep(STROKE); g.drawLine(x1, y1, x2, y2); }
    @Override public void drawRect(int x, int y, int w, int h) { prep(STROKE); g.drawRect(x, y, w, h); }
    @Override public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) { prep(STROKE); g.drawRoundRect(x, y, w, h, aw, ah); }

    @Override
    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
        fillShape(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, aw, ah));
    }

    @Override public void drawOval(int x, int y, int w, int h) { prep(STROKE); g.drawOval(x, y, w, h); }

    @Override
    public void fillOval(int x, int y, int w, int h) {
        if (keepSmallWhite(w, h)) { g.setColor(color); applied = -1; g.fillOval(x, y, w, h); return; }
        prep(FILL);
        g.fillOval(x, y, w, h);
    }

    @Override public void drawArc(int x, int y, int w, int h, int sa, int aa) { prep(STROKE); g.drawArc(x, y, w, h, sa, aa); }
    @Override public void fillArc(int x, int y, int w, int h, int sa, int aa) { prep(FILL); g.fillArc(x, y, w, h, sa, aa); }
    @Override public void drawPolyline(int[] xs, int[] ys, int n) { prep(STROKE); g.drawPolyline(xs, ys, n); }
    @Override public void drawPolygon(int[] xs, int[] ys, int n) { prep(STROKE); g.drawPolygon(xs, ys, n); }
    @Override public void fillPolygon(int[] xs, int[] ys, int n) { prep(FILL); g.fillPolygon(xs, ys, n); }

    // =====================================================================
    // Text
    // =====================================================================

    @Override public void drawString(String s, int x, int y) { prep(TEXT); g.drawString(s, x, y); }
    @Override public void drawString(String s, float x, float y) { prep(TEXT); g.drawString(s, x, y); }
    @Override public void drawString(AttributedCharacterIterator it, int x, int y) { prep(TEXT); g.drawString(it, x, y); }
    @Override public void drawString(AttributedCharacterIterator it, float x, float y) { prep(TEXT); g.drawString(it, x, y); }
    @Override public void drawGlyphVector(GlyphVector gv, float x, float y) { prep(TEXT); g.drawGlyphVector(gv, x, y); }
    @Override public void drawChars(char[] data, int off, int len, int x, int y) { prep(TEXT); g.drawChars(data, off, len, x, y); }

    // =====================================================================
    // Images (untouched)
    // =====================================================================

    @Override public boolean drawImage(Image img, AffineTransform xf, ImageObserver obs) { return g.drawImage(img, xf, obs); }
    @Override public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) { g.drawImage(img, op, x, y); }
    @Override public void drawRenderedImage(RenderedImage img, AffineTransform xf) { g.drawRenderedImage(img, xf); }
    @Override public void drawRenderableImage(RenderableImage img, AffineTransform xf) { g.drawRenderableImage(img, xf); }
    @Override public boolean drawImage(Image img, int x, int y, ImageObserver o) { return g.drawImage(img, x, y, o); }
    @Override public boolean drawImage(Image img, int x, int y, int w, int h, ImageObserver o) { return g.drawImage(img, x, y, w, h, o); }
    @Override public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver o) { return g.drawImage(img, x, y, bg, o); }
    @Override public boolean drawImage(Image img, int x, int y, int w, int h, Color bg, ImageObserver o) { return g.drawImage(img, x, y, w, h, bg, o); }
    @Override public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver o) {
        return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, o);
    }
    @Override public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg, ImageObserver o) {
        return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bg, o);
    }

    // =====================================================================
    // Plain delegation
    // =====================================================================

    @Override
    public Graphics create() {
        ThemedGraphics t = new ThemedGraphics((Graphics2D) g.create(), color, paint, paintIsColor);
        return t;
    }

    @Override public void dispose() { g.dispose(); }
    @Override public boolean hit(Rectangle r, Shape s, boolean onStroke) { return g.hit(r, s, onStroke); }
    @Override public GraphicsConfiguration getDeviceConfiguration() { return g.getDeviceConfiguration(); }
    @Override public void setComposite(Composite c) { g.setComposite(c); }
    @Override public Composite getComposite() { return g.getComposite(); }
    @Override public void setStroke(Stroke s) { g.setStroke(s); }
    @Override public Stroke getStroke() { return g.getStroke(); }
    @Override public void setRenderingHint(RenderingHints.Key k, Object v) { g.setRenderingHint(k, v); }
    @Override public Object getRenderingHint(RenderingHints.Key k) { return g.getRenderingHint(k); }
    @Override public void setRenderingHints(Map<?, ?> m) { g.setRenderingHints(m); }
    @Override public void addRenderingHints(Map<?, ?> m) { g.addRenderingHints(m); }
    @Override public RenderingHints getRenderingHints() { return g.getRenderingHints(); }
    @Override public void translate(int x, int y) { g.translate(x, y); }
    @Override public void translate(double x, double y) { g.translate(x, y); }
    @Override public void rotate(double th) { g.rotate(th); }
    @Override public void rotate(double th, double x, double y) { g.rotate(th, x, y); }
    @Override public void scale(double sx, double sy) { g.scale(sx, sy); }
    @Override public void shear(double sx, double sy) { g.shear(sx, sy); }
    @Override public void transform(AffineTransform t) { g.transform(t); }
    @Override public void setTransform(AffineTransform t) { g.setTransform(t); }
    @Override public AffineTransform getTransform() { return g.getTransform(); }
    @Override public void clip(Shape s) { g.clip(s); }
    @Override public FontRenderContext getFontRenderContext() { return g.getFontRenderContext(); }
    @Override public void setPaintMode() { g.setPaintMode(); }
    @Override public void setXORMode(Color c) { g.setXORMode(c); }
    @Override public Font getFont() { return g.getFont(); }
    @Override public void setFont(Font f) { g.setFont(f); }
    @Override public FontMetrics getFontMetrics(Font f) { return g.getFontMetrics(f); }
    @Override public Rectangle getClipBounds() { return g.getClipBounds(); }
    @Override public void clipRect(int x, int y, int w, int h) { g.clipRect(x, y, w, h); }
    @Override public void setClip(int x, int y, int w, int h) { g.setClip(x, y, w, h); }
    @Override public Shape getClip() { return g.getClip(); }
    @Override public void setClip(Shape s) { g.setClip(s); }
    @Override public void copyArea(int x, int y, int w, int h, int dx, int dy) { g.copyArea(x, y, w, h, dx, dy); }
}
