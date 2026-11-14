import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Section interface implemented by all page sections.
 * Supports responsive height calculation, viewport culling,
 * input handling, and animation updates.
 */
public interface Section {
    String getId();
    int getHeight(int width);
    void update(float dt);
    void paint(Graphics2D g, int y, int width, int viewportY, int viewportH);
    boolean onClick(int x, int y);
    boolean onMouseMove(int x, int y);
    boolean onMouseDown(int x, int y);
    boolean onMouseUp(int x, int y);
    boolean onKey(KeyEvent e);
}

