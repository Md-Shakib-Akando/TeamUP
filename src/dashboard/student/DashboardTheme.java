import java.awt.*;

/**
 * Shared styling constants, colors, and design tokens for TeamUp Dashboard.
 * 100% Harmonized with TeamUp Landing Page's Aurora Glass & Dribbble Brand Palette.
 */
public class DashboardTheme {
    // Canvas & Surface
    public static final Color BG_CANVAS = new Color(0xF8, 0xF7, 0xF4); // Intercepted by ThemedGraphics for Aurora Glass
    public static final Color SIDEBAR_BG = ThemedGraphics.SURFACE_GLASS; // Frosted glass sentinel
    public static final Color BORDER_COLOR = new Color(0xEB, 0xE7, 0xE1); // Soft subtle card border

    // Landing Page Aurora Brand Palette
    public static final Color BRAND_PURPLE = new Color(0xEA, 0x4C, 0x89); // Theme.BRAND_PINK (#EA4C89)
    public static final Color BRAND_PURPLE_HOVER = new Color(0xD8, 0x3A, 0x77); // Hover state (#D83A77)
    public static final Color PURPLE_TINT_BG = new Color(0xFD, 0xEE, 0xF4); // Pastel Pink Tint (#FDEEF4)

    public static final Color ACCENT_VIOLET = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6 Violet
    public static final Color ACCENT_CYAN   = new Color(0x22, 0xD3, 0xEE); // #22D3EE Cyan
    public static final Color ACCENT_MINT   = new Color(0x34, 0xD3, 0x99); // #34D399 Mint
    public static final Color ACCENT_AMBER  = new Color(0xFB, 0xBF, 0x24); // #FBBF24 Amber

    // Landing Page Typography Ink
    public static final Color TEXT_DARK = new Color(0x0D, 0x0C, 0x22); // Theme.INK_DARK (#0D0C22)
    public static final Color TEXT_MUTED = new Color(0x56, 0x55, 0x66); // Theme.LIGHT_TEXT_SECONDARY (#565566)
    public static final Color TEXT_LIGHT = new Color(0x9E, 0x9E, 0xAE); // Theme.LIGHT_TEXT_MUTED (#9E9EAE)

    public static final Font FONT_HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONT_BOLD_13 = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_BOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font FONT_BOLD_11 = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    public static final Font FONT_BOLD_10 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    public static final Font FONT_REG_13 = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_REG_12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_REG_11 = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
}
