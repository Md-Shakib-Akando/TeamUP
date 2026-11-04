import java.awt.Graphics2D;

public class Chip {
    public int x, y, width, height;
    public String label;
    public boolean selected = false;
    public boolean hovered = false;
    public Runnable onToggle;

    public Chip(String label, boolean initialSelected, Runnable onToggle) {
        this.label = label;
        this.selected = initialSelected;
        this.onToggle = onToggle;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void paint(Graphics2D g) {
        Draw.drawChip(g, x, y, width, height, label, selected, hovered);
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean prev = hovered;
        hovered = contains(mx, my);
        return hovered != prev;
    }

    public boolean onClick(int mx, int my) {
        if (contains(mx, my)) {
            selected = !selected;
            if (onToggle != null) onToggle.run();
            return true;
        }
        return false;
    }
}

