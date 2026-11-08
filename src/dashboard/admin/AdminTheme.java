import java.awt.*;

/**
 * Styling constants, color palette, and typography tokens for TeamUp Admin Dashboard.
 * Forest/Emerald green theme matching the administration interface design.
 */
public class AdminTheme {
    public static final Color BG_CANVAS = new Color(0xF8, 0xFA, 0xFC);
    public static final Color SIDEBAR_BG = Color.WHITE;
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0);

    // Green Brand Palette
    public static final Color BRAND_GREEN_DARK = new Color(0x14, 0x53, 0x2D); // #14532D Dark forest green
    public static final Color BRAND_GREEN_MID  = new Color(0x05, 0x96, 0x69); // #059669 Emerald
    public static final Color BRAND_GREEN_TINT = new Color(0xE8, 0xF5, 0xE9); // Soft sage active nav tint
    public static final Color BRAND_GREEN_HOVER = new Color(0x0F, 0x3F, 0x22);

    // Chart Palette
    public static final Color CHART_LINE      = new Color(0x11, 0x5E, 0x3B); // #115E3B Forest green line
    public static final Color CHART_BAR_DARK  = new Color(0x14, 0x53, 0x2D); // #14532D Created bars
    public static final Color CHART_BAR_TEAL  = new Color(0x5E, 0xA8, 0x9B); // #5EA89B Completed bars
    public static final Color CHART_GRID      = new Color(0xF1, 0xF5, 0xF9); // Subtle grid lines

    // Status Pill Colors
    public static final Color STATUS_PENDING_BG = new Color(0xFE, 0xF3, 0xC7); // Soft amber
    public static final Color STATUS_PENDING_TXT = new Color(0xB4, 0x53, 0x09);
    public static final Color STATUS_REVIEW_BG = new Color(0xE0, 0xF2, 0xFE); // Soft sky blue
    public static final Color STATUS_REVIEW_TXT = new Color(0x03, 0x69, 0xA1);
    public static final Color STATUS_GREEN_BG = new Color(0xDC, 0xFC, 0xE7);
    public static final Color STATUS_GREEN_TEXT = new Color(0x16, 0x65, 0x34);
    public static final Color STATUS_RED_BG = new Color(0xFE, 0xE2, 0xE2);
    public static final Color STATUS_RED_TEXT = new Color(0xB9, 0x1C, 0x1C);
    public static final Color STATUS_GRAY_BG = new Color(0xF1, 0xF5, 0xF9);
    public static final Color STATUS_GRAY_TEXT = new Color(0x64, 0x74, 0x8B);

    // Text Palette
    public static final Color TEXT_DARK  = new Color(0x0F, 0x17, 0x2A);
    public static final Color TEXT_MAIN  = TEXT_DARK;
    public static final Color TEXT_MUTED = new Color(0x47, 0x55, 0x69);
    public static final Color TEXT_LIGHT = new Color(0x94, 0xA3, 0xB8);

    // Typography
    public static final Font FONT_HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    public static final Font FONT_BOLD_24 = FONT_HEADING;
    public static final Font FONT_TITLE   = new Font(Font.SANS_SERIF, Font.BOLD, 17);
    public static final Font FONT_BOLD_16 = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONT_SECTION = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font FONT_BOLD_14 = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FONT_METRIC  = new Font(Font.SANS_SERIF, Font.BOLD, 28);
    public static final Font FONT_BOLD_13 = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_BOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font FONT_SEMIBOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font FONT_BOLD_11 = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    public static final Font FONT_SEMIBOLD_11 = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    public static final Font FONT_BOLD_10 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    public static final Font FONT_REG_13  = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_REGULAR_13 = FONT_REG_13;
    public static final Font FONT_REG_12  = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_REGULAR_12 = FONT_REG_12;
    public static final Font FONT_REG_11  = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font FONT_REGULAR_11 = FONT_REG_11;
    public static final Font FONT_REG_10  = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    public static final Font FONT_REGULAR_10 = FONT_REG_10;

    // Vector Icon Drawing Helpers (decoupled from root Draw to resolve IDE package warnings)
    public static void drawSearchIcon(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size * 0.12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float r = size * 0.32f;
        g2.draw(new java.awt.geom.Ellipse2D.Float(x + size * 0.15f, y + size * 0.15f, r * 2, r * 2));
        g2.draw(new java.awt.geom.Line2D.Float(x + size * 0.58f, y + size * 0.58f, x + size * 0.85f, y + size * 0.85f));
        g2.dispose();
    }

    public static void drawChevronDown(Graphics2D g, int x, int y, int size, Color color, float angle) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + size / 2.0, y + size / 2.0);
        if (angle != 0f) {
            g2.rotate(Math.toRadians(angle));
        }
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
        p.moveTo(-size * 0.3f, -size * 0.15f);
        p.lineTo(0, size * 0.15f);
        p.lineTo(size * 0.3f, -size * 0.15f);
        g2.draw(p);
        g2.dispose();
    }
}
