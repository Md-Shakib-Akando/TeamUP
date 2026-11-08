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
 * ThemedGraphics — paints the dashboards in the landing page's "Aurora Glass" theme.
 *
 * The dashboards were written against a light palette with ~600 hard-coded colours.
 * Instead of rewriting every call site, this Graphics2D decorator remaps colours at
 * draw time, following {@link Theme#themeTransition} (so the landing page's
 * dark/light toggle also applies to the dashboards):
 *
 *  - dark mode: white surfaces become dark glass, ink text becomes light,
 *    pastel tints become deep tints, light borders become subtle hairlines;
 *  - the page canvas colour becomes translucent so the aurora background shows;
 *  - {@link #SURFACE_GLASS} (sidebar/header) becomes frosted glass;
 *  - solid brand-pink fills (buttons, active pills, progress) get the landing
 *    pink → violet gradient.
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
    private static final int BRAND = 0xEA4C89, BRAND_HOVER = 0xD83A77;

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

    private static Color compute(Color c, int mode, float light) {
        int rgb = c.getRGB() & 0xFFFFFF;
        int a = c.getAlpha();
        if (mode == FILL && (rgb == CANVAS_A || rgb == CANVAS_B)) {
            Color dark = new Color(255, 255, 255, 10 * a / 255);
            return Theme.lerp(dark, c, light);
        }
        if (mode == FILL && rgb == (SURFACE_GLASS.getRGB() & 0xFFFFFF)) {
            return Theme.lerp(new Color(10, 10, 24, 212), new Color(255, 255, 255, 240), light);
        }
        if (light >= 0.999f) return c;
        Color d = darkVariant(c, mode);
        return light <= 0.001f ? d : Theme.lerp(d, c, light);
    }

    private static Color darkVariant(Color c, int mode) {
        float[] hsl = hsl(c);
        float h = hsl[0], s = hsl[1], l = hsl[2];
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        boolean neutral = (max - min) < 18;
        int a = c.getAlpha();

        if (mode == TEXT) {
            if (neutral) {
                if (l > 0.85f) return c;                       // white text on colour stays white
                return fromHsl(h, s, 0.97f - l * 0.7f, a);     // ink -> light, grey -> mid
            }
            if (l < 0.55f) return fromHsl(h, Math.min(1f, s * 1.05f), 0.70f, a);
            return c;
        }
        if (mode == STROKE) {
            if (neutral) {
                if (l >= 0.8f) return fromHsl(h, s, 0.24f, a); // light borders -> hairlines
                return fromHsl(h, s, 0.97f - l * 0.7f, a);
            }
            if (l >= 0.85f) return fromHsl(h, Math.min(s, 0.6f), 0.32f, a);
            if (l < 0.55f) return fromHsl(h, s, 0.68f, a);
            return c;
        }
        // FILL
        if (neutral) {
            if (l >= 0.86f) return fromHsl(h, s * 0.6f, 0.045f + (l - 0.86f) * 0.6f, a);   // white -> ~#1C1C2A
            if (l >= 0.55f) return fromHsl(h, s, 0.24f, a);
            if (a > 200) return fromHsl(h, s, Math.max(l, 0.16f), a);                      // dark buttons stay visible
            return c;                                                                     // shadows, scrims
        }
        if (l >= 0.85f) return fromHsl(h, Math.min(s, 0.55f), 0.16f + (l - 0.85f) * 0.5f, a); // pastel -> deep tint
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

    /** Fill with brand-gradient upgrade for solid brand-pink shapes. */
    private boolean brandFill(Shape s) {
        if (!paintIsColor || color.getAlpha() != 255) return false;
        int rgb = color.getRGB() & 0xFFFFFF;
        if (rgb != BRAND && rgb != BRAND_HOVER && rgb != 0x8B5CF6 && rgb != 0x7C5CFF) return false;
        Rectangle2D b = s.getBounds2D();
        if (b.getWidth() < 6 || b.getHeight() < 3) return false;
        Color a, z;
        if (rgb == BRAND || rgb == BRAND_HOVER) {
            a = rgb == BRAND ? Fx.PINK : new Color(0xD8, 0x3A, 0x77);
            z = rgb == BRAND ? Fx.VIOLET : new Color(0x7C, 0x3A, 0xED);
        } else {
            a = Fx.VIOLET;
            z = Fx.CYAN;
        }
        Paint old = g.getPaint();
        g.setPaint(new GradientPaint((float) b.getX(), (float) b.getY(), a,
                (float) b.getMaxX(), (float) b.getMaxY(), z));
        g.fill(s);
        g.setPaint(old);
        return true;
    }

    /** Small white fills (toggle knobs, dots, icon cut-outs) keep their colour. */
    private boolean keepSmallWhite(double w, double h) {
        if (!paintIsColor) return false;
        int rgb = color.getRGB() & 0xFFFFFF;
        return rgb == 0xFFFFFF && w <= 30 && h <= 30;
    }

    private void fillShape(Shape s) {
        if (brandFill(s)) return;
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
        if (brandFill(new Rectangle(x, y, w, h))) return;
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
