import java.awt.*;

/**
 * Styling constants, color palette, and typography tokens for TeamUp Admin Dashboard.
 * 100% Harmonized with TeamUp Landing Page's Aurora Glass & Dribbble Brand Palette.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminTheme {

    // Canvas & Surface
    public static final Color BG_CANVAS    = new Color(0xF8, 0xFA, 0xFC); // Intercepted by ThemedGraphics for Aurora Glass
    public static final Color SIDEBAR_BG   = ThemedGraphics.SURFACE_GLASS; // Frosted glass sentinel
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0);

    // =========================================================================
    // TeamUp Aurora Brand Palette (Harmonized with Landing Page)
    // =========================================================================
    public static final Color BRAND_PRIMARY     = new Color(0xEA, 0x4C, 0x89); // #EA4C89 Brand Pink
    public static final Color BRAND_SECONDARY   = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6 Violet
    public static final Color BRAND_ACCENT      = new Color(0x22, 0xD3, 0xEE); // #22D3EE Cyan
    public static final Color BRAND_MINT        = new Color(0x34, 0xD3, 0x99); // #34D399 Mint
    public static final Color BRAND_AMBER       = new Color(0xFB, 0xBF, 0x24); // #FBBF24 Amber
    public static final Color BRAND_TINT        = new Color(0xFD, 0xEE, 0xF4); // Pastel Pink Tint
    public static final Color BRAND_HOVER       = new Color(0xD8, 0x3A, 0x77); // Hover state (#D83A77)

    // Backward-compatible aliases to seamlessly support existing call-sites
    public static final Color BRAND_GREEN_DARK  = BRAND_PRIMARY;
    public static final Color BRAND_GREEN_MID   = BRAND_SECONDARY;
    public static final Color BRAND_GREEN_TINT  = BRAND_TINT;
    public static final Color BRAND_GREEN_HOVER = BRAND_HOVER;

    // =========================================================================
    // Chart Palette (Matching Aurora Pink / Violet / Cyan)
    // =========================================================================
    public static final Color CHART_LINE      = BRAND_PRIMARY;                // #EA4C89 Brand pink
    public static final Color CHART_BAR_DARK  = BRAND_SECONDARY;              // #8B5CF6 Violet created bars
    public static final Color CHART_BAR_TEAL  = BRAND_ACCENT;                 // #22D3EE Cyan completed bars
    public static final Color CHART_GRID      = new Color(0xF1, 0xF5, 0xF9); // Subtle grid lines

    // =========================================================================
    // Status Pill Colors
    // =========================================================================
    public static final Color STATUS_PENDING_BG  = new Color(0xFE, 0xF3, 0xC7); // Soft amber
    public static final Color STATUS_PENDING_TXT = new Color(0xB4, 0x53, 0x09);
    public static final Color STATUS_REVIEW_BG   = new Color(0xEE, 0xF2, 0xFF); // Soft indigo/violet
    public static final Color STATUS_REVIEW_TXT  = new Color(0x63, 0x66, 0xF1);
    public static final Color STATUS_GREEN_BG    = new Color(0xDC, 0xFC, 0xE7); // Soft mint
    public static final Color STATUS_GREEN_TEXT  = new Color(0x05, 0x96, 0x69);
    public static final Color STATUS_RED_BG      = new Color(0xFF, 0xE4, 0xE6); // Soft coral
    public static final Color STATUS_RED_TEXT    = new Color(0xE1, 0x1D, 0x48);
    public static final Color STATUS_GRAY_BG     = new Color(0xF1, 0xF5, 0xF9);
    public static final Color STATUS_GRAY_TEXT   = new Color(0x64, 0x74, 0x8B);

    // =========================================================================
    // Text Palette
    // =========================================================================
    public static final Color TEXT_DARK  = new Color(0x0F, 0x17, 0x2A);
    public static final Color TEXT_MAIN  = TEXT_DARK;
    public static final Color TEXT_MUTED = new Color(0x56, 0x55, 0x66); // Theme.LIGHT_TEXT_SECONDARY (#565566)
    public static final Color TEXT_LIGHT = new Color(0x9E, 0x9E, 0xAE); // Theme.LIGHT_TEXT_MUTED (#9E9EAE)

    // =========================================================================
    // Typography
    // =========================================================================
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
}
