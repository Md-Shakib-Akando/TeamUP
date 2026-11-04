import java.awt.Graphics2D;

public class Button {
    public int x, y, width, height;
    public String text;
    public boolean isPrimary = true;
    public boolean isWarm = false;
    public Runnable action;

    public boolean hovered = false;
    public boolean pressed = false;
    public boolean focused = false;

    public Button(String text, boolean isPrimary, Runnable action) {
        this.text = text;
        this.isPrimary = isPrimary;
        this.action = action;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void paint(Graphics2D g) {
        if (isPrimary) {
            Draw.drawGradientButton(g, x, y, width, height, text, hovered, pressed, isWarm, focused);
        } else {
            Draw.drawGhostButton(g, x, y, width, height, text, hovered, pressed, focused);
        }
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean prev = hovered;
        hovered = contains(mx, my);
        return hovered != prev;
    }

    public boolean onMouseDown(int mx, int my) {
        if (contains(mx, my)) {
            pressed = true;
            focused = true;
            return true;
        }
        focused = false;
        return false;
    }

    public boolean onMouseUp(int mx, int my) {
        boolean wasPressed = pressed;
        pressed = false;
        if (wasPressed && contains(mx, my)) {
            if (action != null) action.run();
            return true;
        }
        return false;
    }
}
