import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class Dropdown {
    public int x, y, width, height;
    private final List<String> options;
    private int selectedIndex = 0;
    public boolean open = false;
    private int hoveredIndex = -1;

    public Dropdown(List<String> options) {
        this.options = options;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getSelected() {
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            return options.get(selectedIndex);
        }
        return options.get(0);
    }

    public void paint(Graphics2D g) {
        int radius = Math.min(14, height / 2);
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, width, height, radius, radius);

        // Header Background (Glassmorphic)
        if (Theme.isDark) {
            g.setColor(open ? new Color(255, 255, 255, 28) : new Color(255, 255, 255, 16));
        } else {
            g.setColor(open ? new Color(255, 255, 255, 240) : new Color(255, 255, 255, 180));
        }
        g.fill(shape);

        // Header Border
        g.setColor(open ? Theme.BRAND_PINK : Theme.inputBorder());
        g.setStroke(new BasicStroke(open ? 1.6f : 1.2f));
        g.draw(shape);

        // Selected Text
        g.setFont(Theme.fontSmallBold());
        FontMetrics fm = g.getFontMetrics();
        int padX = 14;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Theme.textPrimary());
        g.drawString(getSelected(), x + padX, textY);

        // Chevron Icon
        int iconSize = 12;
        int iconX = x + width - padX - iconSize;
        int iconY = y + (height - iconSize) / 2;
        Draw.drawChevronDown(g, iconX, iconY, iconSize, Theme.textSecondary(), open ? (float) Math.PI : 0f);

        // If open, draw popup list below
        if (open) {
            paintMenu(g);
        }
    }

    public void paintMenu(Graphics2D g) {
        int itemH = 34;
        int menuY = y + height + 6;
        int menuH = options.size() * itemH + 8;
        int radius = 12;

        RoundRectangle2D menuShape = new RoundRectangle2D.Float(x, menuY, width, menuH, radius, radius);

        // Soft drop shadow
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new RoundRectangle2D.Float(x, menuY + 4, width, menuH, radius, radius));

        // Frosted Glass Background
        if (Theme.isDark) {
            g.setColor(new Color(22, 30, 52, 248));
        } else {
            g.setColor(new Color(255, 255, 255, 248));
        }
        g.fill(menuShape);

        // Glass Border
        g.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 90));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(menuShape);

        // Items
        g.setFont(Theme.fontSmall());
        FontMetrics fm = g.getFontMetrics();
        int padX = 14;

        for (int i = 0; i < options.size(); i++) {
            int itemTop = menuY + 4 + i * itemH;
            if (i == hoveredIndex) {
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 25) : new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 25));
                g.fill(new RoundRectangle2D.Float(x + 4, itemTop, width - 8, itemH, 6, 6));
            }
            if (i == selectedIndex) {
                g.setColor(Theme.BRAND_PINK);
            } else {
                g.setColor(Theme.textPrimary());
            }
            int ty = itemTop + (itemH - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(options.get(i), x + padX, ty);
        }
    }

    public boolean contains(int mx, int my) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            return true;
        }
        if (open) {
            int itemH = 34;
            int menuY = y + height + 6;
            int menuH = options.size() * itemH + 8;
            return mx >= x && mx <= x + width && my >= menuY && my <= menuY + menuH;
        }
        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        if (!open) return false;
        int itemH = 34;
        int menuY = y + height + 6;
        int prev = hoveredIndex;
        hoveredIndex = -1;
        for (int i = 0; i < options.size(); i++) {
            int top = menuY + 4 + i * itemH;
            if (mx >= x && mx <= x + width && my >= top && my < top + itemH) {
                hoveredIndex = i;
                break;
            }
        }
        return hoveredIndex != prev;
    }

    public boolean onClick(int mx, int my) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            open = !open;
            return true;
        }
        if (open) {
            int itemH = 34;
            int menuY = y + height + 6;
            for (int i = 0; i < options.size(); i++) {
                int top = menuY + 4 + i * itemH;
                if (mx >= x && mx <= x + width && my >= top && my < top + itemH) {
                    selectedIndex = i;
                    open = false;
                    return true;
                }
            }
            open = false;
            return true;
        }
        return false;
    }
}
