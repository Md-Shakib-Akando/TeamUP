import java.awt.*;

/**
 * Styling constants, color palette, and typography tokens for TeamUp Admin Dashboard.
 * Harmonized 100% with TeamUp Landing Page signature Dribbble Pink theme (Theme.java):
 * - Canvas: #F8F7F4 (Theme.CANVAS_LIGHT)
 * - Brand Pink: #EA4C89 (Theme.BRAND_PINK)
 * - Hover Pink: #D83A77
 * - Pastel Pink Tint: #FDEEF4 (Theme.PASTEL_PINK)
 * - Ink Dark: #0D0C22 (Theme.INK_DARK)
 * - Secondary Text: #565566 (Theme.LIGHT_TEXT_SECONDARY)
 * - Card Border: #EBE7E1
 */
public class AdminTheme {

    // 100% Matching Landing Page Canvas & Theme Tokens (Theme.java)
    public static final Color BG_CANVAS     = new Color(0xF8, 0xF7, 0xF4); // Theme.CANVAS_LIGHT (#F8F7F4)
    public static final Color SIDEBAR_BG    = Color.WHITE;
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color BORDER_COLOR  = new Color(0xEB, 0xE7, 0xE1); // Soft subtle landing card border

    // Landing Page Dribbble Brand Pink Palette
    public static final Color BRAND_PRIMARY       = new Color(0xEA, 0x4C, 0x89); // Theme.BRAND_PINK (#EA4C89)
    public static final Color BRAND_PRIMARY_HOVER = new Color(0xD8, 0x3A, 0x77); // Hover state (#D83A77)
    public static final Color BRAND_TINT          = new Color(0xFD, 0xEE, 0xF4); // Pastel Pink Tint (#FDEEF4)

    // Aliases ensuring seamless backwards compatibility across subviews
    public static final Color BRAND_GREEN_DARK  = BRAND_PRIMARY;       // #EA4C89 Brand Pink
    public static final Color BRAND_GREEN_MID   = BRAND_PRIMARY;       // #EA4C89 Brand Pink
    public static final Color BRAND_GREEN_TINT  = BRAND_TINT;          // #FDEEF4 Pastel Pink Tint
    public static final Color BRAND_GREEN_HOVER = BRAND_PRIMARY_HOVER; // #D83A77 Pink Hover

    // Chart Palette (Dribbble Brand Pink & Violet duo)
    public static final Color CHART_LINE     = new Color(0xEA, 0x4C, 0x89); // #EA4C89 Brand Pink
    public static final Color CHART_BAR_DARK = new Color(0xEA, 0x4C, 0x89); // #EA4C89 Created bars
    public static final Color CHART_BAR_TEAL = new Color(0x7C, 0x5C, 0xFF); // #7C5CFF Accent Violet (Theme.ACCENT_VIOLET)
    public static final Color CHART_GRID     = new Color(0xEE, 0xEA, 0xE2); // Subtle warm grid lines

    // Status Pill Colors
    public static final Color STATUS_PENDING_BG   = new Color(0xFE, 0xF3, 0xC7); // Soft amber
    public static final Color STATUS_PENDING_TXT  = new Color(0xB4, 0x53, 0x09);
    public static final Color STATUS_REVIEW_BG    = new Color(0xF3, 0xF0, 0xFF); // Soft pastel violet
    public static final Color STATUS_REVIEW_TXT   = new Color(0x7C, 0x5C, 0xFF); // Accent violet
    public static final Color STATUS_GREEN_BG     = new Color(0xDC, 0xFC, 0xE7); // Soft mint
    public static final Color STATUS_GREEN_TEXT   = new Color(0x16, 0x65, 0x34);
    public static final Color STATUS_RED_BG       = new Color(0xFE, 0xE2, 0xE2); // Soft red
    public static final Color STATUS_RED_TEXT     = new Color(0xEF, 0x44, 0x44); // Error red
    public static final Color STATUS_GRAY_BG      = new Color(0xF1, 0xF5, 0xF9);
    public static final Color STATUS_GRAY_TEXT    = new Color(0x64, 0x74, 0x8B);

    // Landing Page Typography Ink
    public static final Color TEXT_DARK  = new Color(0x0D, 0x0C, 0x22); // Theme.INK_DARK (#0D0C22)
    public static final Color TEXT_MAIN  = TEXT_DARK;
    public static final Color TEXT_MUTED = new Color(0x56, 0x55, 0x66); // Theme.LIGHT_TEXT_SECONDARY (#565566)
    public static final Color TEXT_LIGHT = new Color(0x9E, 0x9E, 0xAE); // Theme.LIGHT_TEXT_MUTED (#9E9EAE)

    // Typography Tokens
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
