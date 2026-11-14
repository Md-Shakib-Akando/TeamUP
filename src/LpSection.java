import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Base class for landing-page sections.
 *
 * - Height is measured from the real layout code (a dry-run render into a 1x1
 *   scratch surface), so text wrapping / font scaling never clips content.
 * - Owns a scroll-triggered {@link Fx.Reveal} that arms when the section scrolls into view.
 * - onMouseMove() implementations return true while the pointer is over
 *   something clickable, which drives the hand cursor.
 */
public abstract class LpSection implements Section {

    protected final Fx.Reveal rv = new Fx.Reveal();

    /** True while the section is being measured (skip side effects). */
    protected boolean measuring = false;

    private int cachedW = -1;
    private float cachedScale = -1f;
    private int cachedH = 600;

    private static BufferedImage scratch;

    @Override
    public int getHeight(int width) {
        if (width != cachedW || cachedScale != Theme.fontScale) {
            if (scratch == null) scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scratch.createGraphics();
            Draw.setQualityHints(g);
            g.setClip(0, 0, 1, 1);
            measuring = true;
            try {
                cachedH = Math.max(1, render(g, 0, width, 0, -1));
            } finally {
                measuring = false;
                g.dispose();
            }
            cachedW = width;
            cachedScale = Theme.fontScale;
        }
        return cachedH;
    }

    /** Forces the next getHeight() to re-measure (e.g. after content changes). */
    protected void invalidateHeight() {
        cachedW = -1;
    }

    @Override
    public final void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        getHeight(width);
        rv.check(y + 80, viewportH);
        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);
        render(g2, y, width, viewportY, viewportH);
        g2.dispose();
    }

    /**
     * Lays out and draws the section starting at screen y. Returns the section height.
     * viewportH is -1 while measuring.
     */
    protected abstract int render(Graphics2D g, int y, int width, int viewportY, int viewportH);

    @Override
    public void update(float dt) {
        rv.update(dt);
    }

    @Override public boolean onClick(int x, int y) { return false; }
    @Override public boolean onMouseMove(int x, int y) { return false; }
    @Override public boolean onMouseDown(int x, int y) { return false; }
    @Override public boolean onMouseUp(int x, int y) { return false; }
    @Override public boolean onKey(KeyEvent e) { return false; }
}
